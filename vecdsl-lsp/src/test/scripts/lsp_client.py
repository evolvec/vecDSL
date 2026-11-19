"""Scripted LSP session against vecdsl-lsp, independent of LSP4J: Content-Length framed JSON-RPC over the server's
standard input and output, Python standard library only. Run with python -I.

    python -I lsp_client.py --workspace <ws> --fmt <vecdsl-cli command> [--quick] -- <server command>

The server command is everything after "--", for example: java -jar target/vecdsl-lsp.jar. The vecdsl-cli command
(for example "java -jar ../vecdsl-cli/target/vecdsl-cli.jar") gives the expected texts of formatting (vecdsl fmt) and
of vecdsl/generate (vecdsl gen). Without --quick every check runs (all 28 samples for formatting); with --quick
the session on pinecone_demo.vecDSL prints its diagnostics, one hover, one completion list, one code action and
"formatting equals fmt: True|False". Both end with a second session initialized with neither rootUri nor workspace
folders. Every wait has a timeout. Exit status 0 when every check passed.
"""

import argparse
import json
import os
import pathlib
import queue
import shlex
import shutil
import subprocess
import sys
import tempfile
import threading
import time
import urllib.parse

TIMEOUT = 60.0
EXIT_TIMEOUT = 20.0
SAMPLES = "org.ftn.vecDSL.tests/samples"
CONFORMANCE = "spec/conformance"
REJECTED = ["linking_unknown_field", "syntax_params_order", "t_vf_ambiguous", "wf_create_top_alias",
            "wf_delete_two_selectors", "wf_recall_one", "wf_update_no_selector"]
UNSUPPORTED = "vecdsl.coverage.unsupported"
PINECONE_STATEMENTS = ["connectCloud", "dropOld", "createProducts", "seed", "similar", "keywords", "hybrid", "byId",
                       "cheapBooks", "reprice", "similarToFirst", "browse", "removeOne", "remaining"]
PINECONE_PROGRAM = "pinecone/python/product_catalog.py"

# Expected values of IdeSamplesTest, 0-based LSP positions.
PINECONE_DIAGNOSTICS = [
    ((14, 4, 12), "unsupported on pinecone: Pinecone configures text analysis on each full-text field when it creates "
                  "the index."),
    ((20, 4, 16), "unsupported on pinecone: the Pinecone index schema is fixed at creation. in a collection declaration "
                  "the float_vector field is the dense vector"),
    ((23, 4, 16), "unsupported on pinecone: the Pinecone index schema is fixed at creation. in a collection declaration "
                  "the float_vector field is the dense vector"),
    ((25, 4, 12), "unsupported on pinecone: the Pinecone index schema is fixed at creation. inside a collection "
                  "declaration a bm25 function makes its inputs full-text fields"),
]
CHROMA_DIAGNOSTICS = [
    ((18, 4, 16), "unsupported on chroma: a Chroma collection holds one embedding per record. in a collection "
                  "declaration the single float_vector field is the embedding"),
]
HOVERS = {
    "pinecone_demo.vecDSL": ((35, 9), (35, 9, 22), [
        "**vector-search**, construct `Method VectorSearch`", "Nearest-neighbour search over one vector field.",
        "| vendor | status | Python | TypeScript | Java | reference |",
        "| pinecone | native | `rt.vector_search` | `rt.vectorSearch` | `vectorSearch` |"]),
    "chroma_demo.vecDSL": ((31, 9), (31, 9, 22), [
        "**vector-search**, construct `Method VectorSearch`", "Nearest-neighbour search over one vector field.",
        "| chroma | native | `rt.vector_search` | `rt.vectorSearch` | `vectorSearch` |"]),
}
COMPLETIONS = {
    "pinecone_demo.vecDSL": ((36, 4), {"metric": "pinecone: native", "output": "pinecone: native",
                                       "score": "pinecone: concealed",
                                       "sort": "unsupported on pinecone: Pinecone orders search matches by score"}),
    "chroma_demo.vecDSL": ((32, 4), {"metric": "chroma: native", "output": "chroma: native",
                                     "score": "chroma: concealed",
                                     "sort": "unsupported on chroma: Chroma orders query results by distance"}),
}
CODE_ACTIONS = {
    "pinecone_demo.vecDSL": ("pinecone", "targets [ pinecone, milvus ]", "Remove 'pinecone' from targets",
                             "targets [ milvus ]"),
    "chroma_demo.vecDSL": ("chroma", "targets [ chroma, qdrant ]", "Remove 'chroma' from targets",
                           "targets [ qdrant ]"),
}


def key(uri):
    """The file a URI names, so that two spellings of one file (a drive letter, %3A) meet."""
    path = urllib.parse.unquote(urllib.parse.urlparse(uri).path)
    if os.name == "nt":
        path = path.lstrip("/")
    return os.path.normcase(os.path.normpath(path))


def split(command):
    return shlex.split(command, posix=os.name != "nt")


class Server:
    """One server process; messages are read on a thread into a queue, server requests are answered with null."""

    def __init__(self, command):
        self.process = subprocess.Popen(command, stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                                        stderr=subprocess.PIPE)
        self.messages = queue.Queue()
        self.diagnostics = {}
        self.condition = threading.Condition()
        self.next_id = 0
        self.stderr = []
        threading.Thread(target=self._read, daemon=True).start()
        threading.Thread(target=self._drain, daemon=True).start()

    def _drain(self):
        for line in self.process.stderr:
            self.stderr.append(line.decode("utf-8", "replace").rstrip("\r\n"))

    def _read(self):
        stream = self.process.stdout
        while True:
            length = None
            while True:
                line = stream.readline()
                if not line:
                    self.messages.put(None)
                    return
                line = line.strip()
                if not line:
                    break
                name, _, value = line.decode("ascii").partition(":")
                if name.lower() == "content-length":
                    length = int(value.strip())
            message = json.loads(stream.read(length).decode("utf-8"))
            if "method" in message and "id" in message:
                self._send({"jsonrpc": "2.0", "id": message["id"], "result": None})
            elif message.get("method") == "textDocument/publishDiagnostics":
                with self.condition:
                    params = message["params"]
                    self.diagnostics.setdefault(key(params["uri"]), []).append(params["diagnostics"])
                    self.condition.notify_all()
            else:
                self.messages.put(message)

    def _send(self, message):
        body = json.dumps(message).encode("utf-8")
        self.process.stdin.write(b"Content-Length: %d\r\n\r\n" % len(body) + body)
        self.process.stdin.flush()

    def request(self, method, params):
        self.next_id += 1
        self._send({"jsonrpc": "2.0", "id": self.next_id, "method": method, "params": params})
        deadline = time.monotonic() + TIMEOUT
        while True:
            message = self.messages.get(timeout=max(0.1, deadline - time.monotonic()))
            if message is None:
                raise RuntimeError("the server closed its output during " + method)
            if message.get("id") == self.next_id:
                if "error" in message:
                    return message
                return message.get("result")

    def notify(self, method, params):
        self._send({"jsonrpc": "2.0", "method": method, "params": params})

    def mark(self, uri):
        with self.condition:
            return len(self.diagnostics.get(key(uri), []))

    def published(self, uri, after):
        """The first diagnostics published for the uri after the given count, waiting at most TIMEOUT."""
        with self.condition:
            if not self.condition.wait_for(lambda: len(self.diagnostics.get(key(uri), [])) > after, TIMEOUT):
                raise TimeoutError("no diagnostics for " + uri)
            return self.diagnostics[key(uri)][after]

    def exit_status(self):
        try:
            return self.process.wait(EXIT_TIMEOUT)
        except subprocess.TimeoutExpired:
            self.process.kill()
            return None


class Session:
    """A server for documents under root; rooted=False initializes with neither rootUri nor workspace folders."""

    def __init__(self, command, workspace, root, rooted=True):
        self.server = Server(command)
        self.workspace = workspace
        self.root = root
        self.versions = {}
        self.texts = {}
        self.capabilities = self.server.request("initialize", {
            "processId": os.getpid(), "rootUri": root.as_uri() if rooted else None, "capabilities": {},
            "workspaceFolders": None})["capabilities"]
        self.server.notify("initialized", {})

    def copy(self, source, name=None, newline=None):
        target = self.root / (name or source.name)
        text = source.read_bytes().decode("utf-8")
        if newline:
            text = text.replace("\r\n", "\n").replace("\n", newline)
        target.write_bytes(text.encode("utf-8"))
        return target

    def open(self, path):
        uri = path.as_uri()
        text = path.read_bytes().decode("utf-8")
        mark = self.server.mark(uri)
        self.versions[uri] = 1
        self.texts[uri] = text
        self.server.notify("textDocument/didOpen", {"textDocument": {
            "uri": uri, "languageId": "vecdsl", "version": 1, "text": text}})
        return uri, self.server.published(uri, mark)

    def change(self, uri, text):
        mark = self.server.mark(uri)
        self.versions[uri] += 1
        self.texts[uri] = text
        self.server.notify("textDocument/didChange", {"textDocument": {"uri": uri, "version": self.versions[uri]},
                                                      "contentChanges": [{"text": text}]})
        return self.server.published(uri, mark)

    def close(self, uri):
        self.server.notify("textDocument/didClose", {"textDocument": {"uri": uri}})

    def shutdown(self):
        self.server.request("shutdown", None)
        self.server.notify("exit", None)
        return self.server.exit_status()


def offset(text, position):
    line, character = position["line"], position["character"]
    index = 0
    for _ in range(line):
        newline = text.find("\n", index)
        index = len(text) if newline < 0 else newline + 1
    return index + character


def apply(text, edits):
    for edit in sorted(edits, key=lambda e: offset(text, e["range"]["start"]), reverse=True):
        text = text[:offset(text, edit["range"]["start"])] + edit["newText"] + text[offset(text, edit["range"]["end"]):]
    return text


def span(rng):
    return (rng["start"]["line"], rng["start"]["character"], rng["end"]["character"])


class Checks:
    def __init__(self, quick):
        self.results = []
        self.quick = quick

    def check(self, name, passed, detail=""):
        self.results.append((name, passed))
        if not self.quick or not passed:
            print(("PASS " if passed else "FAIL ") + name + ("" if passed or not detail else ": " + detail))
        return passed


def fmt(cli, path, indent):
    command = split(cli) + ["fmt", str(path)] + (["--indent", indent] if indent else [])
    result = subprocess.run(command, capture_output=True, timeout=TIMEOUT)
    return result.stdout.decode("utf-8")


def formatting(session, uri, insert_spaces):
    edits = session.server.request("textDocument/formatting", {
        "textDocument": {"uri": uri}, "options": {"tabSize": 4, "insertSpaces": insert_spaces}})
    return apply(session.texts[uri], edits)


def diagnostics_match(published, expected):
    return len(published) == len(expected) and all(
        d["code"] == UNSUPPORTED and d["severity"] == 2 and span(d["range"]) == e[0] and d["message"].startswith(e[1])
        for d, e in zip(published, expected))


def run_quick(session, checks, cli):
    sample = session.workspace / SAMPLES / "pinecone_demo.vecDSL"
    uri, published = session.open(session.copy(sample))
    print("diagnostics (%d):" % len(published))
    for d in published:
        print("  %d:%d-%d:%d %s %s" % (d["range"]["start"]["line"], d["range"]["start"]["character"],
                                       d["range"]["end"]["line"], d["range"]["end"]["character"],
                                       d["code"], d["message"][:90]))
    checks.check("diagnostics", diagnostics_match(published, PINECONE_DIAGNOSTICS))
    position, _, fragments = HOVERS["pinecone_demo.vecDSL"]
    hover = session.server.request("textDocument/hover", {"textDocument": {"uri": uri},
                                                          "position": {"line": position[0], "character": position[1]}})
    value = hover["contents"]["value"]
    print("hover at %d:%d:" % position)
    for line in value.splitlines()[:6]:
        print("  " + line)
    checks.check("hover", all(f in value for f in fragments))
    position, proposals = COMPLETIONS["pinecone_demo.vecDSL"]
    items = session.server.request("textDocument/completion", {
        "textDocument": {"uri": uri}, "position": {"line": position[0], "character": position[1]}})
    items = items["items"] if isinstance(items, dict) else items
    print("completion at %d:%d (%d items):" % (position[0], position[1], len(items)))
    for item in items:
        print("  %-14s %s" % (item["label"], item.get("detail", "")))
    details = {item["label"]: item.get("detail") for item in items}
    checks.check("completion", all(details.get(label) == detail for label, detail in proposals.items()))
    vendor, targets, title, after = CODE_ACTIONS["pinecone_demo.vecDSL"]
    text = session.texts[uri]
    changed = session.change(uri, text.replace("targets [ %s ]" % vendor, targets))
    first = next(d for d in changed if d["message"].startswith("unsupported on %s:" % vendor))
    actions = session.server.request("textDocument/codeAction", {
        "textDocument": {"uri": uri}, "range": first["range"], "context": {"diagnostics": [first]}})
    titles = [a["title"] for a in actions]
    edited = apply(session.texts[uri], actions[0]["edit"]["changes"][uri]) if actions else ""
    print("code action after '%s': %s -> line 2 '%s'" % (targets, titles, edited.splitlines()[1] if edited else ""))
    checks.check("code action", titles == [title] and edited.splitlines()[1] == after)
    session.change(uri, text)
    formatted = formatting(session, uri, False)
    equal = formatted == fmt(cli, sample, "tab")
    print("formatting equals fmt: %s" % equal)
    checks.check("formatting", equal)
    session.close(uri)


def run_all(session, checks, cli, workspace):
    caps = session.capabilities
    checks.check("capabilities", caps.get("hoverProvider") is True and caps.get("documentFormattingProvider") is True
                 and caps.get("codeActionProvider") is True and caps.get("completionProvider") is not None
                 and caps.get("textDocumentSync") == 2
                 and caps.get("experimental", {}).get("vecdsl", {}).get("generate") == "vecdsl/generate"
                 and caps["experimental"]["vecdsl"].get("generateVersion") == 2
                 and len(caps["experimental"]["vecdsl"].get("targets", [])) == 14,
                 json.dumps(caps)[:300])
    samples = workspace / SAMPLES
    for name, expected in [("pinecone_demo.vecDSL", PINECONE_DIAGNOSTICS), ("chroma_demo.vecDSL", CHROMA_DIAGNOSTICS)]:
        uri, published = session.open(session.copy(samples / name))
        checks.check("diagnostics " + name, diagnostics_match(published, expected), json.dumps(published)[:400])
        position, rng, fragments = HOVERS[name]
        hover = session.server.request("textDocument/hover", {
            "textDocument": {"uri": uri}, "position": {"line": position[0], "character": position[1]}})
        checks.check("hover " + name, span(hover["range"]) == rng and hover["contents"]["kind"] == "markdown"
                     and all(f in hover["contents"]["value"] for f in fragments), json.dumps(hover)[:300])
        position, proposals = COMPLETIONS[name]
        items = session.server.request("textDocument/completion", {
            "textDocument": {"uri": uri}, "position": {"line": position[0], "character": position[1]}})
        items = items["items"] if isinstance(items, dict) else items
        details = {item["label"]: item.get("detail", "") for item in items}
        flags = [item.get("detail", "").startswith("unsupported on") for item in items]
        last = flags.index(True) if True in flags else len(flags)
        checks.check("completion " + name, all(details.get(k) == v for k, v in proposals.items())
                     and all(flags[last:]), json.dumps(details)[:300])
        vendor, targets, title, after = CODE_ACTIONS[name]
        text = session.texts[uri]
        first = next(d for d in published if d["message"].startswith("unsupported on %s:" % vendor))
        none = session.server.request("textDocument/codeAction", {
            "textDocument": {"uri": uri}, "range": first["range"], "context": {"diagnostics": [first]}})
        changed = session.change(uri, text.replace("targets [ %s ]" % vendor, targets))
        first = next(d for d in changed if d["message"].startswith("unsupported on %s:" % vendor))
        actions = session.server.request("textDocument/codeAction", {
            "textDocument": {"uri": uri}, "range": first["range"], "context": {"diagnostics": [first]}})
        edited = apply(session.texts[uri], actions[0]["edit"]["changes"][uri]) if actions else ""
        checks.check("code action " + name, none == [] and [a["title"] for a in actions] == [title]
                     and edited.splitlines()[1] == after, json.dumps(actions)[:300])
        session.close(uri)
    for name in REJECTED:
        folder = workspace / CONFORMANCE / name
        expected = json.loads((folder / "diagnostics.json").read_text("utf-8"))["diagnostics"]
        uri, published = session.open(session.copy(folder / "program.vecDSL", name + ".vecDSL"))
        errors = [d for d in published if d["severity"] == 1]
        got = [(d["code"], d["message"], d["range"]["start"]["line"] + 1, d["range"]["start"]["character"] + 1)
               for d in errors]
        want = [(e["code"], e["message"], e["line"], e["column"]) for e in expected]
        checks.check("rejected " + name, got == want, "%s != %s" % (got, want))
        session.close(uri)
    for newline, label in [(None, "LF"), ("\r\n", "CRLF")]:
        failures = []
        for sample in sorted(samples.glob("*.vecDSL")):
            path = session.copy(sample, label + "-" + sample.name, newline)
            uri, _ = session.open(path)
            for insert_spaces, indent in [(True, None), (False, "tab")]:
                if formatting(session, uri, insert_spaces) != fmt(cli, path, indent):
                    failures.append("%s %s" % (sample.name, "spaces" if insert_spaces else "tab"))
            session.close(uri)
        checks.check("formatting equals fmt, 28 samples x 2 indentations, %s" % label,
                     not failures and len(list(samples.glob("*.vecDSL"))) == 28, ", ".join(failures))
    uri, _ = session.open(session.copy(samples / "chroma_demo.vecDSL", "broken.vecDSL"))
    text = session.texts[uri]
    broken = session.change(uri, text.replace("vector-search in", "vector-search inn", 1))
    repaired = session.change(uri, text)
    checks.check("broken edit and repair",
                 any(d["severity"] == 1 and d["code"] == "org.eclipse.xtext.diagnostics.Diagnostic.Syntax"
                     for d in broken) and not any(d["severity"] == 1 for d in repaired),
                 json.dumps(broken)[:300])
    session.close(uri)
    path = session.copy(samples / "pinecone_demo.vecDSL", "preview.vecDSL")
    uri, published = session.open(path)
    result = session.server.request("vecdsl/generate", {"uri": uri, "target": "pinecone", "language": "py",
                                                        "range": {"start": {"line": 35, "character": 2},
                                                                  "end": {"line": 35, "character": 2}}})
    out = pathlib.Path(tempfile.mkdtemp(prefix="vecdsl-gen-"))
    subprocess.run(split(cli) + ["gen", str(path), "--target", "pinecone", "--lang", "py", "-o", str(out)],
                   capture_output=True, timeout=TIMEOUT)
    written = {p.relative_to(out).as_posix(): p.read_bytes().decode("utf-8") for p in out.rglob("*") if p.is_file()}
    preview = {f["path"]: f["text"] for f in result["files"]}
    checks.check("vecdsl/generate equals vecdsl gen", preview == written and result["statement"] == "similar"
                 and result["errors"] == [] and len(preview) > 0,
                 "%s vs %s, statement %s" % (sorted(preview), sorted(written), result.get("statement")))
    outline = session.server.request("vecdsl/generate", {"uri": uri})
    names = [statement["name"] for statement in outline["statements"]]
    checks.check("vecdsl/generate without target outlines the script",
                 outline["files"] == [] and outline["targets"] == ["pinecone"] and names == PINECONE_STATEMENTS
                 and outline["statements"][4]["range"]["start"] == {"line": 35, "character": 0},
                 json.dumps(outline)[:300])
    lines = written.get(PINECONE_PROGRAM, "").splitlines()
    sections = result["sections"]
    create = session.server.request("vecdsl/generate", {"uri": uri, "target": "pinecone", "language": "py",
                                                        "range": outline["statements"][2]["range"]})
    checks.check("vecdsl/generate sections and unsupported",
                 len(sections) == 2 and all(s["path"] == PINECONE_PROGRAM for s in sections)
                 and lines[sections[0]["start"]] == "    def similar(self, query_vector, max_price):"
                 and lines[sections[0]["end"] - 1] == "        )" and lines[sections[0]["end"]] == ""
                 and "program.similar(" in lines[sections[1]["start"]]
                 and [(span(u["range"]), u["message"]) for u in create["unsupported"]]
                 == [(span(d["range"]), d["message"]) for d in published]
                 and diagnostics_match(published, PINECONE_DIAGNOSTICS),
                 "sections %s, unsupported %s" % (sections, json.dumps(create["unsupported"])[:300]))
    bad = session.server.request("vecdsl/generate", {"uri": uri, "target": "nosuchvendor"})
    checks.check("vecdsl/generate rejects an unknown target", isinstance(bad, dict) and "error" in bad
                 and bad["error"]["code"] == -32602, json.dumps(bad)[:200])
    shutil.rmtree(out, ignore_errors=True)
    session.close(uri)


def run_rootless(command, workspace, checks):
    """A second session initialized without root: diagnostics, hover and vecdsl/generate of an opened file."""
    name = "initialize without root: diagnostics, hover and vecdsl/generate of an opened file, exit status 0"
    root = pathlib.Path(tempfile.mkdtemp(prefix="vecdsl-lsp-rootless-")).resolve()
    try:
        session = Session(command, workspace, root, rooted=False)
    except (KeyError, TypeError) as error:
        shutil.rmtree(root, ignore_errors=True)
        checks.check(name, False, "initialize answered without capabilities (%r)" % error)
        return
    try:
        uri, published = session.open(session.copy(workspace / SAMPLES / "pinecone_demo.vecDSL"))
        position, _, fragments = HOVERS["pinecone_demo.vecDSL"]
        hover = session.server.request("textDocument/hover", {
            "textDocument": {"uri": uri}, "position": {"line": position[0], "character": position[1]}})
        result = session.server.request("vecdsl/generate", {
            "uri": uri, "target": "pinecone", "language": "py",
            "range": {"start": {"line": 35, "character": 0}, "end": {"line": 35, "character": 0}}})
        session.close(uri)
    finally:
        status = session.shutdown()
        shutil.rmtree(root, ignore_errors=True)
    value = (hover or {}).get("contents", {}).get("value", "")
    checks.check(name, diagnostics_match(published, PINECONE_DIAGNOSTICS) and all(f in value for f in fragments)
                 and result.get("statement") == "similar" and len(result.get("sections", [])) == 2 and status == 0,
                 "diagnostics %d, statement %s, status %s" % (len(published), result.get("statement"), status))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--workspace", required=True)
    parser.add_argument("--fmt", required=True, help="the vecdsl-cli command line")
    parser.add_argument("--quick", action="store_true")
    parser.add_argument("server", nargs=argparse.REMAINDER)
    args = parser.parse_args()
    command = args.server[1:] if args.server[:1] == ["--"] else args.server
    workspace = pathlib.Path(args.workspace).resolve()
    root = pathlib.Path(tempfile.mkdtemp(prefix="vecdsl-lsp-")).resolve()
    checks = Checks(args.quick)
    started = time.monotonic()
    session = Session(command, workspace, root)
    try:
        if args.quick:
            run_quick(session, checks, args.fmt)
        else:
            run_all(session, checks, args.fmt, workspace)
    finally:
        status = session.shutdown()
        checks.check("shutdown and exit, status 0 within %ds" % EXIT_TIMEOUT, status == 0, "status %s" % status)
        shutil.rmtree(root, ignore_errors=True)
    run_rootless(command, workspace, checks)
    passed = sum(1 for _, ok in checks.results if ok)
    print("%d of %d checks passed in %.0f s; server stderr lines: %d" % (
        passed, len(checks.results), time.monotonic() - started, len(session.server.stderr)))
    return 0 if passed == len(checks.results) else 1


if __name__ == "__main__":
    sys.exit(main())
