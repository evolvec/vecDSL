"""Exports derived from the vecDSL Xtext grammar: clause admissibility per query kind, admitted
SearchParams keys per query form, enumerations, fragment and datatype rules, and size and
structure metrics (Power and Malloy, 2004).

    python grammar_report.py GRAMMAR [BASELINE_GRAMMAR] --admissibility PROPERTIES --out DIR

Everything is computed from the grammar text; the SearchParams table also reads the validator's
admissibility properties, which is the single source of truth for per-form key admissibility.
"""
import argparse
import csv
import re
from collections import defaultdict
from pathlib import Path

QUERY_KINDS = [("vector-search", "VectorSearch"), ("search", "KeywordSearch"), ("hybrid-search", "HybridSearch"),
               ("recommend", "RecommendSearch"), ("get", "GetById"), ("generate", "GenerativeSearch"),
               ("distance-matrix", "DistanceMatrix"), ("count", "Count"), ("scroll", "Scroll"),
               ("sample", "Sample"), ("batch-query", "BatchQuery")]
CLAUSES = [("Paging", ("fragment", "Paging")), ("ReadScope", ("fragment", "ReadScope")),
           ("ScoreSpec", ("fragment", "ScoreSpec")), ("mmr", ("feature", "mmr")), ("boost", ("feature", "boost")),
           ("rerank", ("feature", "reranker")), ("group-by", ("feature", "groupBy")),
           ("with-vectors", ("feature", "withVectors")), ("with-total", ("feature", "withTotal")),
           ("exclude", ("feature", "excluded")), ("highlight", ("feature", "highlighted")),
           ("sort", ("feature", "sortClauses")), ("aggregate", ("feature", "aggregations")),
           ("params", ("feature", "params")), ("prefetch", ("feature", "prefetches")),
           ("output", ("feature", "output")), ("filter", ("feature", "filter")),
           ("extensions", ("feature", "extensions"))]
PARAM_FORMS = [kind for kind in QUERY_KINDS] + [("vector leg", "VectorLeg"), ("prefetch stage", "Prefetch")]
BUILTIN_TERMINALS = {"ID", "INT", "STRING", "ML_COMMENT", "SL_COMMENT", "WS", "ANY_OTHER"}


# ---------------------------------------------------------------- grammar model

def strip_comments(text):
    out, i, quote = [], 0, None
    while i < len(text):
        c = text[i]
        if quote:
            out.append(c)
            if c == "\\":
                out.append(text[i + 1])
                i += 2
                continue
            if c == quote:
                quote = None
            i += 1
            continue
        if c in "'\"":
            quote = c
        elif text.startswith("//", i):
            j = text.find("\n", i)
            i = len(text) if j < 0 else j
            continue
        elif text.startswith("/*", i):
            i = text.find("*/", i) + 2
            continue
        out.append(c)
        i += 1
    return "".join(out)


def split_rules(text):
    text = re.sub(r"^(grammar|import|generate) .*$", "", strip_comments(text), flags=re.M)
    rules, buf, quote = [], "", None
    for c in text:
        if quote:
            buf += c
            if c == quote:
                quote = None
            continue
        if c == "'":
            quote = c
        if c == ";":
            rules.append(buf.strip())
            buf = ""
            continue
        buf += c
    return [r for r in rules if r]


def tokenize(body):
    tokens, i = [], 0
    while i < len(body):
        c = body[i]
        if c.isspace():
            i += 1
        elif c == "'":
            j = i + 1
            while body[j] != "'":
                j += 2 if body[j] == "\\" else 1
            tokens.append(("kw", body[i + 1:j]))
            i = j + 1
        elif body.startswith("+=", i) or body.startswith("?=", i):
            tokens.append(("op", body[i:i + 2]))
            i += 2
        elif c.isalpha() or c == "_" or c == "^":
            j = i + 1
            while j < len(body) and (body[j].isalnum() or body[j] == "_" or body.startswith("::", j)):
                j += 2 if body.startswith("::", j) else 1
            tokens.append(("id", body[i:j]))
            i = j
        else:
            tokens.append(("sym", c))
            i += 1
    return tokens


class Parser:
    """Rule body to tree: seq, alt, opt, star, plus, kw, call, assign(feature, op, element), xref, action."""

    def __init__(self, tokens):
        self.tokens, self.pos = tokens, 0

    def peek(self, offset=0):
        k = self.pos + offset
        return self.tokens[k] if k < len(self.tokens) else None

    def parse(self):
        tree = self.alternatives()
        assert self.pos == len(self.tokens), self.tokens[self.pos:]
        return tree

    def alternatives(self):
        branches = [self.sequence()]
        while self.peek() == ("sym", "|"):
            self.pos += 1
            branches.append(self.sequence())
        return branches[0] if len(branches) == 1 else ("alt", branches)

    def sequence(self):
        items = []
        while self.peek() not in (None, ("sym", "|"), ("sym", ")")):
            items.append(self.postfix())
        return ("seq", items)

    def postfix(self):
        element = self.element()
        token = self.peek()
        if token in (("sym", "?"), ("sym", "*"), ("sym", "+")):
            self.pos += 1
            return ({"?": "opt", "*": "star", "+": "plus"}[token[1]], element)
        return element

    def element(self):
        token = self.peek()
        nxt = self.peek(1)
        if token[0] == "id" and nxt in (("sym", "="), ("op", "+="), ("op", "?=")):
            self.pos += 2
            return ("assign", token[1], nxt[1], self.atom())
        return self.atom()

    def atom(self):
        token = self.peek()
        self.pos += 1
        if token == ("sym", "("):
            inner = self.alternatives()
            assert self.peek() == ("sym", ")")
            self.pos += 1
            return inner
        if token == ("sym", "{"):
            depth, start = 1, self.pos
            while depth:
                depth += {("sym", "{"): 1, ("sym", "}"): -1}.get(self.peek(), 0)
                self.pos += 1
            return ("action", " ".join(v for _, v in self.tokens[start:self.pos - 1]))
        if token == ("sym", "["):
            inner = []
            while self.peek() != ("sym", "]"):
                inner.append(self.peek())
                self.pos += 1
            self.pos += 1
            return ("xref", inner[0][1])
        if token[0] == "kw":
            return ("kw", token[1])
        if token[0] == "id":
            return ("call", token[1])
        raise ValueError(token)


def load(path):
    rules = {}
    for text in split_rules(Path(path).read_text(encoding="utf-8")):
        m = re.match(r"(enum|terminal|fragment)?\s*([A-Za-z_]\w*)(\*)?\s*(returns\s+([\w:]+))?\s*:(.*)$", text, re.S)
        kind, name, returns, body = m.group(1) or "parser", m.group(2), m.group(5), m.group(6)
        rule = {"name": name, "kind": kind, "returns": returns, "body": body}
        if kind == "enum":
            rule["literals"] = [lit.split("=")[0].strip() for lit in body.split("|")]
            rule["keywords"] = re.findall(r"'([^']*)'", body)
        elif kind != "terminal":
            rule["tree"] = Parser(tokenize(body)).parse()
        rules[name] = rule
    mark_datatype_rules(rules)
    return rules


def mark_datatype_rules(rules):
    """A parser rule is a datatype rule when it has no assignments or actions and calls only keywords,
    terminals and other datatype rules."""
    candidates = {n for n, r in rules.items() if r["kind"] == "parser" and not has_assignment(r["tree"])}
    changed = True
    while changed:
        changed = False
        for name in sorted(candidates):
            calls = [node[1] for node in walk(rules[name]["tree"]) if node[0] == "call"]
            if any(c in rules and rules[c]["kind"] != "terminal" and c not in candidates for c in calls):
                candidates.discard(name)
                changed = True
    for name in candidates:
        rules[name]["kind"] = "datatype"


def walk(tree):
    yield tree
    tag = tree[0]
    if tag in ("seq", "alt"):
        for child in tree[1]:
            yield from walk(child)
    elif tag in ("opt", "star", "plus"):
        yield from walk(tree[1])
    elif tag == "assign":
        yield from walk(tree[3])


def has_assignment(tree):
    return any(node[0] in ("assign", "action", "xref") for node in walk(tree))


# ---------------------------------------------------------------- admissibility

def inline_fragments(tree, rules):
    tag = tree[0]
    if tag == "call" and rules.get(tree[1], {}).get("kind") == "fragment":
        return ("fragment", tree[1], inline_fragments(rules[tree[1]]["tree"], rules))
    if tag in ("seq", "alt"):
        return (tag, [inline_fragments(child, rules) for child in tree[1]])
    if tag in ("opt", "star", "plus"):
        return (tag, inline_fragments(tree[1], rules))
    if tag == "assign":
        return (tag, tree[1], tree[2], inline_fragments(tree[3], rules))
    return tree


def matches(node, clause):
    kind, name = clause
    return (kind == "feature" and node[0] == "assign" and node[1] == name) or (
        kind == "fragment" and node[0] == "fragment" and node[1] == name)


def present(tree, clause):
    if matches(tree, clause):
        return True
    tag = tree[0]
    if tag in ("seq", "alt"):
        return any(present(child, clause) for child in tree[1])
    if tag in ("opt", "star", "plus"):
        return present(tree[1], clause)
    if tag == "fragment":
        return present(tree[2], clause)
    if tag == "assign":
        return present(tree[3], clause)
    return False


def required(tree, clause):
    if matches(tree, clause):
        return clause[0] == "feature" or not nullable(tree[2])
    tag = tree[0]
    if tag == "seq":
        return any(required(child, clause) for child in tree[1])
    if tag == "alt":
        return all(required(child, clause) for child in tree[1])
    if tag == "plus":
        return required(tree[1], clause)
    if tag == "fragment":
        return required(tree[2], clause)
    return False


def nullable(tree):
    tag = tree[0]
    if tag == "seq":
        return all(nullable(child) for child in tree[1])
    if tag == "alt":
        return any(nullable(child) for child in tree[1])
    if tag in ("opt", "star", "action"):
        return True
    if tag == "plus":
        return nullable(tree[1])
    if tag == "fragment":
        return nullable(tree[2])
    if tag == "assign":
        return nullable(tree[3])
    return False


def clause_matrix(rules):
    rows = []
    for kind, rule in QUERY_KINDS:
        tree = inline_fragments(rules[rule]["tree"], rules)
        row = {"query kind": kind, "rule": rule}
        for clause, spec in CLAUSES:
            row[clause] = ("required" if required(tree, spec) else "admitted") if present(tree, spec) else "excluded"
        rows.append(row)
    return rows


def read_admissibility(path):
    table = {}
    for line in Path(path).read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            form, keys = line.split("=", 1)
            table[form.strip()] = [key.strip() for key in keys.split(",")]
    return table


def search_param_keys(rules):
    keys = []
    for node in walk(rules["SearchParams"]["tree"]):
        if node[0] == "seq" and node[1] and node[1][0][0] == "kw" and node[1][0][1] not in ("{", "}"):
            keys.append(node[1][0][1])
        elif node[0] == "assign" and node[3][0] == "kw":
            keys.append(node[3][1])
    keys = list(dict.fromkeys(keys))
    if any(node[0] == "assign" and node[1] == "extensions" for node in walk(rules["SearchParams"]["tree"])):
        keys.append("extension")
    return keys


def params_matrix(rules, admissibility):
    keys = search_param_keys(rules)
    rows = []
    for kind, rule in PARAM_FORMS:
        has_params = present(inline_fragments(rules[rule]["tree"], rules), ("feature", "params"))
        row = {"form": kind, "rule": rule}
        for key in keys:
            row[key] = "n/a" if not has_params else ("admitted" if key in admissibility.get(rule, []) else "excluded")
        rows.append(row)
    return keys, rows


# ---------------------------------------------------------------- metrics

def symbols(tree):
    return [node for node in walk(tree) if node[0] in ("kw", "call", "xref")]


def metrics(rules):
    parser_like = [r for r in rules.values() if r["kind"] in ("parser", "fragment", "datatype")]
    enums = [r for r in rules.values() if r["kind"] == "enum"]
    terminals = [r for r in rules.values() if r["kind"] == "terminal"]
    keywords = set()
    for rule in parser_like:
        keywords |= {node[1] for node in walk(rule["tree"]) if node[0] == "kw"}
    for rule in enums:
        keywords |= set(rule["keywords"])
    terminal_rules = {r["name"] for r in terminals} | {"ID", "INT", "STRING"}
    nonterminals = {r["name"] for r in parser_like + enums}

    # successor graph over nonterminals; cross-references count as uses of the token rule (ValidID or ID)
    graph = defaultdict(set)
    rhs_size = 0
    decisions = 0
    for rule in parser_like:
        for node in walk(rule["tree"]):
            if node[0] == "call" and node[1] in nonterminals:
                graph[rule["name"]].add(node[1])
            if node[0] == "alt":
                decisions += len(node[1]) - 1
            if node[0] in ("opt", "star", "plus"):
                decisions += 1
        rhs_size += len(symbols(rule["tree"]))
    for rule in enums:
        decisions += max(len(rule["literals"]) - 1, 0)
        rhs_size += len(rule["literals"])

    levels = strongly_connected(nonterminals, graph)
    level_of = {n: i for i, level in enumerate(levels) for n in level}
    dag = defaultdict(set)
    for a, targets in graph.items():
        for b in targets:
            if level_of[a] != level_of[b]:
                dag[level_of[a]].add(level_of[b])
    height = longest_path(level_of["Script"], dag)
    n = len(nonterminals)
    edges = sum(len(t) for t in graph.values())
    return {
        "parser rules (incl. fragments and datatype rules)": len(parser_like),
        "fragments": sum(r["kind"] == "fragment" for r in parser_like),
        "datatype rules": sum(r["kind"] == "datatype" for r in parser_like),
        "enumerations": len(enums),
        "enumeration literals": sum(len(r["literals"]) for r in enums),
        "terminal rules (declared)": len(terminals),
        "keywords (distinct)": len(keywords),
        "TERM": len(keywords | terminal_rules),
        "VAR": n,
        "MCC": n + decisions,
        "AVS": round(rhs_size / n, 2),
        "HEI": height,
        "CLEV": round(100 * len(levels) / n, 2),
        "NSLEV": sum(len(level) > 1 for level in levels),
        "DEP": max(len(level) for level in levels),
        "TIMP": round(100 * 2 * (edges - n + 1) / ((n - 1) * (n - 2)), 2),
    }


def strongly_connected(nodes, graph):
    index, low, stack, on_stack, result, counter = {}, {}, [], set(), [], [0]

    def visit(v):
        index[v] = low[v] = counter[0]
        counter[0] += 1
        stack.append(v)
        on_stack.add(v)
        for w in graph.get(v, ()):
            if w not in index:
                visit(w)
                low[v] = min(low[v], low[w])
            elif w in on_stack:
                low[v] = min(low[v], index[w])
        if low[v] == index[v]:
            component = set()
            while True:
                w = stack.pop()
                on_stack.discard(w)
                component.add(w)
                if w == v:
                    break
            result.append(component)

    import sys
    sys.setrecursionlimit(10000)
    for v in sorted(nodes):
        if v not in index:
            visit(v)
    return result


def longest_path(start, dag):
    memo = {}

    def depth(v):
        if v not in memo:
            memo[v] = 1 + max((depth(w) for w in dag.get(v, ())), default=0)
        return memo[v]

    return depth(start)


# ---------------------------------------------------------------- output

def write_csv(path, header, rows):
    with open(path, "w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=header)
        writer.writeheader()
        writer.writerows(rows)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("grammar")
    parser.add_argument("baseline", nargs="?")
    parser.add_argument("--admissibility", required=True)
    parser.add_argument("--out", required=True)
    args = parser.parse_args()
    out = Path(args.out)
    out.mkdir(parents=True, exist_ok=True)
    rules = load(args.grammar)

    write_csv(out / "query-clauses.csv", ["query kind", "rule"] + [c for c, _ in CLAUSES], clause_matrix(rules))
    keys, rows = params_matrix(rules, read_admissibility(args.admissibility))
    write_csv(out / "search-params.csv", ["form", "rule"] + keys, rows)
    write_csv(out / "enumerations.csv", ["enumeration", "literal count", "literals"],
              [{"enumeration": r["name"], "literal count": len(r["literals"]), "literals": " ".join(r["keywords"])}
               for r in rules.values() if r["kind"] == "enum"])
    write_csv(out / "fragments-and-datatype-rules.csv", ["rule", "kind", "returns"],
              [{"rule": r["name"], "kind": r["kind"], "returns": r["returns"] or ""}
               for r in rules.values() if r["kind"] in ("fragment", "datatype")])

    columns = {"v7.1": metrics(rules)}
    if args.baseline:
        columns = {"v7.0": metrics(load(args.baseline)), **columns}
    names = list(next(iter(columns.values())))
    write_csv(out / "metrics.csv", ["metric"] + list(columns),
              [{"metric": name, **{version: values[name] for version, values in columns.items()}} for name in names])
    for name in names:
        print(f"{name:52s}" + "".join(f"{str(values[name]):>10s}" for values in columns.values()))


if __name__ == "__main__":
    main()
