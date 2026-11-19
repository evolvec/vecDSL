#!/usr/bin/env python3
"""Adds org.ftn.vecDSL.ir to Export-Package of org.ftn.vecDSL/META-INF/MANIFEST.MF (read-modify-write, LF endings).

Usage: export_ir_package.py MANIFEST.MF [EXPECTED_MD5]   (refuses to write when the file's md5 differs from EXPECTED_MD5)
"""
import hashlib
import sys
from pathlib import Path

ENTRY = " org.ftn.vecDSL.ir"


def main():
    path = Path(sys.argv[1])
    data = path.read_bytes()
    if len(sys.argv) > 2 and hashlib.md5(data).hexdigest() != sys.argv[2]:
        sys.exit(f"{path}: md5 {hashlib.md5(data).hexdigest()} is not the expected {sys.argv[2]}; nothing written")
    text = data.decode("utf-8")
    if "\r" in text:
        sys.exit(f"{path}: CRLF line endings; expected LF; nothing written")
    lines = text.split("\n")
    if ENTRY.strip() + "," in text or any(line.strip() in (ENTRY.strip(), "Export-Package: " + ENTRY.strip()) for line in lines):
        print(f"{path}: already exports org.ftn.vecDSL.ir")
        return
    start = next(i for i, line in enumerate(lines) if line.startswith("Export-Package:"))
    end = start
    while end + 1 < len(lines) and lines[end + 1].startswith(" "):
        end += 1
    lines[end] = lines[end] + ","
    lines.insert(end + 1, ENTRY)
    path.write_bytes("\n".join(lines).encode("utf-8"))
    print(f"{path}: Export-Package gains org.ftn.vecDSL.ir after {lines[end].strip()}")


if __name__ == "__main__":
    main()
