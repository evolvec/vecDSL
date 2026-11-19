"""The statement sections of vecdsl/generate through vecdsl-lsp's own protocol (lsp_client.Session), on the samples:
every top-level statement of the 14 *_demo samples for each of the 14 targets and 3 host languages (opened without
their targets line, so that every generator writes code for them), and, of the 14 *_full samples, the first statement
of each statement keyword for the sample's own target and the 3 languages. Checked per request: no errors; at least
one section; the first section starts, after comment, annotation and decorator lines, with a member header (Python: a
line ending in a colon; TypeScript and Java: a line ending in an opening brace) and ends with the member's last line
(Python: the next line is blank or indented less; TypeScript and Java: a closing brace). A request the server answers
with an error (a generator failing on the program) is counted apart. With --record, every answer is also written to the
file, one JSON line per request (the request, then the sections, errors, unsupported reasons and the md5 of each
file's text, or the error message), so that two servers can be compared answer by answer. Standard library only; run
with -I.

    python -I preview_sweep.py --workspace <ws> [--record <file>] -- <server command>
"""

import argparse
import collections
import hashlib
import json
import pathlib
import re
import shutil
import sys
import tempfile
import time

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import lsp_client  # noqa: E402

TARGETS = ["milvus", "pinecone", "weaviate", "qdrant", "chroma", "pgvector", "redis", "mongodb", "oracledb",
           "elasticsearch", "opensearch", "vespa", "lancedb", "duckdb"]
LANGUAGES = ["py", "ts", "java"]
LEADERS = ("@", "/*", "*", "//", "#")


def member_ok(lines, section, language):
    body = lines[section["start"]:section["end"]]
    header = next((line for line in body if not line.strip().startswith(LEADERS)), "")
    if language == "py":
        after = lines[section["end"]] if section["end"] < len(lines) else ""
        indent = len(header) - len(header.lstrip())
        return header.rstrip().endswith(":") and (not after.strip() or len(after) - len(after.lstrip()) <= indent)
    return header.rstrip().endswith("{") and body[-1].strip().startswith("}")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--workspace", required=True)
    parser.add_argument("--record")
    parser.add_argument("server", nargs=argparse.REMAINDER)
    args = parser.parse_args()
    command = args.server[1:] if args.server[:1] == ["--"] else args.server
    workspace = pathlib.Path(args.workspace).resolve()
    root = pathlib.Path(tempfile.mkdtemp(prefix="vecdsl-sweep-")).resolve()
    started = time.monotonic()
    session = lsp_client.Session(command, workspace, root)
    counts = collections.Counter()
    failures = []
    refused = []
    record = open(args.record, "w", encoding="utf-8") if args.record else None
    try:
        for sample in sorted((workspace / lsp_client.SAMPLES).glob("*.vecDSL")):
            demo = sample.name.endswith("_demo.vecDSL")
            text = sample.read_text("utf-8")
            if demo:
                text = re.sub(r"(?m)^targets \[[^\]]*\]\n", "", text)
            path = root / sample.name
            path.write_text(text, "utf-8")
            uri, _ = session.open(path)
            outline = session.server.request("vecdsl/generate", {"uri": uri})
            lines = text.splitlines()
            seen = set()
            for index, statement in enumerate(outline["statements"]):
                start = statement["range"]["start"]
                verb = re.sub(r"^\s*[A-Za-z_]\w*:\s*", "", lines[start["line"]][start["character"]:]).split(" ")[0]
                if not demo and verb in seen:
                    continue
                seen.add(verb)
                counts["statements"] += 1
                for target in TARGETS if demo else outline["targets"]:
                    for language in LANGUAGES:
                        result = session.server.request("vecdsl/generate", {
                            "uri": uri, "target": target, "language": language, "range": statement["range"]})
                        counts["requests"] += 1
                        label = "%s #%d %s (%s) %s %s" % (sample.name, index, statement["name"], verb, target, language)
                        if record:
                            record.write(json.dumps({"request": label, "answer": {
                                "error": result.get("error", {}).get("message")} if "files" not in result else {
                                "sections": result["sections"], "errors": result["errors"],
                                "unsupported": result["unsupported"], "files": {
                                    f["path"]: hashlib.md5(f["text"].encode("utf-8")).hexdigest()
                                    for f in result["files"]}}}, sort_keys=True) + "\n")
                        if "files" not in result:
                            refused.append("%s: %s" % (label, result.get("error", {}).get("message", result)))
                            continue
                        files = {f["path"]: f["text"].splitlines() for f in result["files"]}
                        sections = result["sections"]
                        ok = not result["errors"] and sections and all(
                            0 <= s["start"] < s["end"] <= len(files[s["path"]]) for s in sections) \
                            and member_ok(files[sections[0]["path"]], sections[0], language)
                        counts["sections"] += len(sections)
                        if ok:
                            counts["passed"] += 1
                        else:
                            failures.append("%s: %s" % (label, result["errors"][:1] or sections[:2]))
            session.close(uri)
    finally:
        if record:
            record.close()
        status = session.shutdown()
        shutil.rmtree(root, ignore_errors=True)
    for failure in failures[:40]:
        print("FAIL " + failure)
    for request in refused[:3]:
        print("ERROR " + request)
    print("%d statements, %d requests, %d sections, %d passed, %d failed, %d answered with an error (the generator "
          "failed, as vecdsl gen does on the same program); exit status %s; %.0f s" % (
              counts["statements"], counts["requests"], counts["sections"], counts["passed"], len(failures),
              len(refused), status, time.monotonic() - started))
    return 0 if not failures and status == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
