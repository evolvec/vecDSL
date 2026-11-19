#!/usr/bin/env python3
"""Writes the vecDSL 1.0 header into a copy of VecDSL.xtext (7.1) and checks that nothing below the header changes.

Usage: freeze_header.py SOURCE TARGET   (TARGET may equal SOURCE; written only if the body is unchanged; line endings kept)
"""
import sys
from pathlib import Path

OLD = "// vecDSL textual concrete syntax, version 7.1"
NEW = "// vecDSL textual concrete syntax, vecDSL 1.0 (grammar 7.1 frozen as 1.0 on 16 Nov 2026)"
ANCHOR = "// Decisions applied: D1 to D14 (language designer, 5 Nov 2026), D15 (6 Nov 2026)."
ADDED = ["// vecDSL 1.0 = grammar 7.1 with this header; no rule changed (spec/README.md, \"What 1.0 is\").",
         "//   The EMF namespace URI stays",
         "//   http://www.ftn.org/vecDSL/VecDSL/7 because the metamodel is unchanged.",
         "//   Language and IR are versioned together (spec/VERSIONING.md); the IR is spec/ir/vecdsl-ir-1.0.schema.json."]


def main():
    source, target = Path(sys.argv[1]), Path(sys.argv[2])
    text = source.read_bytes().decode("utf-8")
    eol = "\r\n" if "\r\n" in text else "\n"
    if NEW in text:
        print(f"{source}: already carries the 1.0 header")
        new = text
    else:
        if text.count(OLD + eol) != 1 or text.count(ANCHOR + eol) != 1:
            sys.exit(f"{source}: header lines not found exactly once; nothing written")
        new = text.replace(OLD + eol, NEW + eol).replace(ANCHOR + eol, ANCHOR + eol + eol.join(ADDED) + eol)
    if new[new.index("\ngrammar "):] != text[text.index("\ngrammar "):]:
        sys.exit("the text below the header would change; nothing written")
    target.write_bytes(new.encode("utf-8"))
    print(f"{target}: written ({len(new) - len(text)} bytes added to the header)")


if __name__ == "__main__":
    main()
