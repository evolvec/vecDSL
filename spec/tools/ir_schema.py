#!/usr/bin/env python3
"""JSON Schema (draft 2020-12) of the vecDSL IR 1.0 (spec/ir/vecdsl-ir-1.0.schema.json).

The structural part is generated: one definition per EClass the parser can instantiate, its properties in the order of
EMF's eAllStructuralFeatures, read from model/generated/VecDSL.ecore; which classes the parser instantiates is read from
VecDSL.xtext (a rule whose assignments are not preceded by an action, or an action). Only NORM below is written by hand:
the "$norm" entries of the normalization rules N2 to N11 (spec/SEMANTICS.md), which mirror
org.ftn.vecDSL.ir.IrNormalizer.

Usage: ir_schema.py [--workspace DIR] [--out FILE] [--check IR_OR_DIAGNOSTICS.json ...]
  --check  validate the schema itself, then each file against it (needs the Python package jsonschema)
"""

import argparse
import json
import re
import sys
import xml.etree.ElementTree as ElementTree
from pathlib import Path

WORKSPACE = Path(__file__).resolve().parents[2]
XSI_TYPE = "{http://www.w3.org/2001/XMLSchema-instance}type"
GRAMMAR = Path("org.ftn.vecDSL/src/org/ftn/vecDSL/VecDSL.xtext")
ECORE = Path("org.ftn.vecDSL/model/generated/VecDSL.ecore")
REFERENCE = {"type": "string", "pattern": r"^[^.#/]+(\.[^.#/]+)*(#[1-9][0-9]*)?$"}
NULL = {"type": "null"}


def nullable(schema):
    return {"anyOf": [schema, NULL]}


def enum_ref(name):
    return {"$ref": f"#/$defs/{name}"}


# Hand-written: the "$norm" entries of each class (IrNormalizer); keys and value schemas.
SCORE = {"type": "object", "additionalProperties": False, "required": ["form", "higherIsBetter", "range"],
         "properties": {"form": {"enum": ["similarity", "native"]},
                        "higherIsBetter": {"anyOf": [{"const": True}, NULL]},
                        "range": nullable({"type": "array", "prefixItems": [nullable({"type": "number"}),
                                                                            nullable({"type": "number"})],
                                           "minItems": 2, "maxItems": 2})}}
SLOT = {"type": "object", "additionalProperties": False, "required": ["kind", "name", "type", "hasDefault"],
        "properties": {"kind": {"enum": ["param", "env"]}, "name": {"type": "string"},
                       "type": {"type": "string"}, "hasDefault": {"type": "boolean"}}}
PARAM_KEYS = {"enum": ["target-recall", "candidates", "probes", "scan-budget", "timeout", "oversampling", "rescore",
                       "filter-strategy", "exhaustive", "extension"]}
NORM = {
    "Script": {"restricted": {"type": "boolean"}},
    "VectorSearch": {"score": SCORE, "field": nullable(REFERENCE), "metric": nullable(enum_ref("EMetric"))},
    "KeywordSearch": {"score": SCORE},
    "HybridSearch": {"score": SCORE},
    "RecommendSearch": {"score": SCORE},
    "VectorLeg": {"field": nullable(REFERENCE), "metric": nullable(enum_ref("EMetric")),
                  "input": {"enum": ["own", "shared", None]}},
    "KeywordLeg": {"input": {"enum": ["own", "shared", None]}},
    "VectorField": {"name": {"type": "string"}, "index": nullable(REFERENCE)},
    "VectorIndex": {"field": nullable(REFERENCE), "family": enum_ref("EIndexFamily"),
                    "accelerator": nullable(enum_ref("EAccelerator")),
                    "quantization": nullable(enum_ref("EQuantizationKind"))},
    "Snapshot": {"snapshotName": nullable({"type": "string"})},
    "ParamRef": {"slot": SLOT},
    "EnvRef": {"slot": SLOT},
    "SearchParams": {"form": {"type": "string"}, "admitted": {"type": "array", "items": PARAM_KEYS},
                     "written": {"type": "array", "items": PARAM_KEYS}},
}


def ecore(path):
    classes, enums = {}, {}
    for classifier in ElementTree.parse(path).getroot().findall("eClassifiers"):
        kind = classifier.get(XSI_TYPE)
        name = classifier.get("name")
        if kind == "ecore:EEnum":
            enums[name] = [literal.get("literal") or literal.get("name") for literal in classifier.findall("eLiterals")]
        elif kind == "ecore:EClass":
            supers = [ref.split("//")[-1] for ref in classifier.get("eSuperTypes", "").split()]
            features = []
            for feature in classifier.findall("eStructuralFeatures"):
                features.append({"name": feature.get("name"), "kind": feature.get(XSI_TYPE).split(":")[-1],
                                 "type": feature.get("eType", "").split("//")[-1],
                                 "many": feature.get("upperBound") == "-1",
                                 "containment": feature.get("containment") == "true"})
            classes[name] = {"supers": supers, "features": features}
    return classes, enums


def all_features(classes, name):
    """EMF's eAllStructuralFeatures: supertypes in eSuperTypes order, recursively, then the class's own features."""
    result, seen = [], set()

    def visit(cls):
        for parent in classes[cls]["supers"]:
            visit(parent)
        for feature in classes[cls]["features"]:
            if feature["name"] not in seen:
                seen.add(feature["name"])
                result.append(feature)

    visit(name)
    return result


def subclasses(classes, name):
    result = {name}
    changed = True
    while changed:
        changed = False
        for cls, info in classes.items():
            if cls not in result and any(parent in result for parent in info["supers"]):
                result.add(cls)
                changed = True
    return result


def instantiable(grammar_path):
    """Classes the parser creates: actions, and parser rules with an assignment before any action."""
    sys.path.insert(0, str(WORKSPACE / "org.ftn.vecDSL/tools"))
    import grammar_report
    rules = grammar_report.load(grammar_path)
    text = grammar_report.strip_comments(grammar_path.read_text(encoding="utf-8"))
    created = set(re.findall(r"\{\s*([A-Za-z]\w*)\s*(?:\.|\})", text))

    def assigns_before_action(tree):
        tag = tree[0]
        if tag == "action":
            return None
        if tag in ("assign", "xref"):
            return True
        if tag == "seq":
            for item in tree[1]:
                found = assigns_before_action(item)
                if found is not None:
                    return found
            return None
        if tag == "alt":
            results = [assigns_before_action(branch) for branch in tree[1]]
            return True if any(r is True for r in results) else None
        if tag in ("opt", "star", "plus"):
            return True if assigns_before_action(tree[1]) else None
        if tag == "call":
            rule = rules.get(tree[1])
            return True if rule and rule["kind"] == "fragment" and grammar_report.has_assignment(rule["tree"]) else None
        return None

    for name, rule in rules.items():
        if rule["kind"] == "parser" and assigns_before_action(rule["tree"]):
            created.add(rule["returns"] or name)
    return created


def feature_schema(feature, enums):
    kind, ftype, many = feature["kind"], feature["type"], feature["many"]
    if kind == "EAttribute":
        if ftype in enums:
            item = enum_ref(ftype)
        elif ftype == "EString":
            item = {"type": "string"}
        elif ftype == "EInt":
            item = {"type": "integer"}
        elif ftype == "EDouble":
            item = {"type": "number"}
        elif ftype == "EBoolean":
            return {"type": "boolean"}
        else:
            raise ValueError(f"attribute type {ftype}")
        if feature["name"] in ("targets", "vendor") and ftype == "EString":
            item = enum_ref("VendorId")
        return {"type": "array", "items": item} if many else nullable(item)
    if ftype == "VendorExtension" and feature["containment"] and many:
        return {"type": "object", "propertyNames": enum_ref("VendorId"),
                "additionalProperties": {"type": "array", "items": {"type": "array",
                                                                     "items": {"$ref": "#/$defs/ExtensionItem.any"}}}}
    if feature["containment"]:
        item = {"$ref": f"#/$defs/{ftype}.any"}
        return {"type": "array", "items": item} if many else nullable(item)
    return {"type": "array", "items": REFERENCE} if many else nullable(REFERENCE)


def build(workspace):
    grammar_text = re.sub(r"//[^\n]*|/\*.*?\*/", "", (workspace / GRAMMAR).read_text(encoding="utf-8"), flags=re.S)
    classes, enums = ecore(workspace / ECORE)
    vendor_ids = re.findall(r"'([a-z]+)'", re.search(r"VendorId returns ecore::EString:(.*?);", grammar_text,
                                                     re.S).group(1))
    created = instantiable(workspace / GRAMMAR) & set(classes)
    defs = {"VendorId": {"enum": vendor_ids}}
    for name, literals in sorted(enums.items()):
        defs[name] = {"enum": literals}
    used = set()
    for name in sorted(created):
        features = all_features(classes, name)
        properties = {"$type": {"const": name}}
        for feature in features:
            properties[feature["name"]] = feature_schema(feature, enums)
            if feature["kind"] == "EReference" and feature["containment"]:
                used.add("ExtensionItem" if feature["type"] == "VendorExtension" else feature["type"])
        required = ["$type"] + [feature["name"] for feature in features]
        if name in NORM:
            properties["$norm"] = {"type": "object", "additionalProperties": False,
                                   "required": list(NORM[name]), "properties": NORM[name]}
        defs[name] = {"type": "object", "additionalProperties": False, "required": required,
                      "properties": properties}
    used.add("Script")
    for kind in sorted(used):
        members = sorted(subclasses(classes, kind) & created)
        defs[f"{kind}.any"] = {"oneOf": [{"$ref": f"#/$defs/{member}"} for member in members]}
    defs["span"] = {"type": "object", "additionalProperties": False,
                    "required": ["offset", "length", "line", "column", "endLine"],
                    "properties": {key: {"type": "integer", "minimum": 0}
                                   for key in ["offset", "length", "line", "column", "endLine"]}}
    defs["document"] = {"type": "object", "additionalProperties": False,
                        "required": ["vecdsl-ir", "program", "spans", "literals"],
                        "properties": {"vecdsl-ir": {"const": "1.0"}, "program": {"$ref": "#/$defs/Script"},
                                       "spans": {"type": "object", "additionalProperties": {"$ref": "#/$defs/span"}},
                                       "literals": {"type": "object", "additionalProperties": {"type": "string"}}}}
    defs["diagnostic"] = {"type": "object", "additionalProperties": False,
                          "required": ["severity", "code", "message", "line", "column"],
                          "properties": {"severity": {"const": "error"}, "code": nullable({"type": "string"}),
                                         "message": {"type": "string"}, "line": nullable({"type": "integer"}),
                                         "column": nullable({"type": "integer"})}}
    defs["diagnostics"] = {"type": "object", "additionalProperties": False, "required": ["vecdsl-ir", "diagnostics"],
                           "properties": {"vecdsl-ir": {"const": "1.0"},
                                          "diagnostics": {"type": "array", "items": {"$ref": "#/$defs/diagnostic"}}}}
    return {"$schema": "https://json-schema.org/draft/2020-12/schema",
            "$id": "urn:vecdsl:ir:1.0",
            "title": "vecDSL IR 1.0",
            "description": "Generated by spec/tools/ir_schema.py from VecDSL.ecore and VecDSL.xtext; the $norm entries "
                           "follow spec/SEMANTICS.md (N2 to N11). A document is an IR or a diagnostics document.",
            "oneOf": [{"$ref": "#/$defs/document"}, {"$ref": "#/$defs/diagnostics"}],
            "$defs": defs}


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--workspace", type=Path, default=WORKSPACE)
    parser.add_argument("--out", type=Path, default=WORKSPACE / "spec/ir/vecdsl-ir-1.0.schema.json")
    parser.add_argument("--check", nargs="*", type=Path)
    args = parser.parse_args()
    schema = build(args.workspace)
    args.out.parent.mkdir(parents=True, exist_ok=True)
    args.out.write_text(json.dumps(schema, indent=1) + "\n", encoding="utf-8", newline="\n")
    classes = sum(1 for key, value in schema["$defs"].items() if value.get("type") == "object" and "$type" in
                  value.get("properties", {}))
    print(f"{args.out}: {classes} element definitions, {len(schema['$defs'])} definitions in all")
    if args.check is None:
        return 0
    import jsonschema
    validator_class = jsonschema.Draft202012Validator
    validator_class.check_schema(schema)
    validator = validator_class(schema)
    failed = 0
    for path in args.check:
        errors = sorted(validator.iter_errors(json.loads(path.read_text(encoding="utf-8"))), key=lambda e: e.path)
        if errors:
            failed += 1
            print(f"INVALID {path}: {errors[0].message[:300]} at {'/'.join(map(str, errors[0].absolute_path))}")
    print(f"schema valid (draft 2020-12); {len(args.check) - failed} of {len(args.check)} documents valid")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
