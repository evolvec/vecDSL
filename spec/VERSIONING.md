# vecDSL versioning

## What is versioned together

The **language** (grammar, metamodel, well-formedness judgments of the validator, semantics) and the **IR** (schema and
normalization rules) are versioned together as **vecDSL x.y**. vecDSL 1.0 is grammar 7.1 frozen on 16 Nov 2026 with
IR 1.0. An IR document names its version in `"vecdsl-ir"`.

The **toolchain** (editor, vecdsl-cli, generators), the **VDBC drivers**, the **capability manifests** and the **TCK** have
their own versions. Each declares the vecDSL range it implements (for example "vecDSL >=1.0,<2.0"). A generator release
that changes the code it prints, a manifest that reclassifies a capability, or a new TCK case moves no language version.

## Patch, minor, major

| change | version | examples |
|---|---|---|
| clarification: no program changes meaning, no IR changes | patch (1.0.x) | a grammar comment, a SEMANTICS.md definition of something every target already does alike, a corrected citation |
| additive: every program of the previous version parses with the same meaning; IR gains optional content; every previous IR still validates and reads | minor (1.y) | a new optional clause, a new enumeration literal, a new VendorId, a new `$norm` entry, a new validator rule only if it rejects no program the previous version accepted |
| anything else | major (x.0) | a removed or renamed construct, a narrowed rule (D16 was such a change), a new validator rule that rejects programs the previous version accepted, a changed default, a namespace URI change, a changed IR key |

A minor release keeps the EMF namespace URI when the metamodel is unchanged or only extended; a major release changes it.
1.0 keeps `http://www.ftn.org/vecDSL/VecDSL/7`.

## MAGMA categories

MAGMA, the maintenance of the generators under vendor API evolution, classifies a change as Category A (template-only)
or Category B (touching the grammar or the metamodel). Category A changes never move the language version: they change
generator templates, runtimes, manifests and COVERAGE.md rows, and the toolchain's own version. Category B changes are
RFCs: minor when additive, major otherwise.

## The RFC process

An RFC states the problem with its evidence, the scope, the grammar diff, the effect on the parser, the serializer, the
generators, the samples and tests, and the adoption steps. Within a release line an RFC is accepted only if it is needed
(without it the IR would be ambiguous, a program would have no faithful IR, or a validator rule would contradict the
grammar), additive, and leaves the constructs the generators map unaffected; otherwise it waits for the next minor or major
release. It is applied only together with:

1. a sample (a program in `org.ftn.vecDSL.tests/samples` or a vendor test) that uses the construct;
2. a test that parses it and checks its model, and a normalization test if a rule changes;
3. the IR schema change (`spec/tools/ir_schema.py`, regenerated) and the normalization rule in SEMANTICS.md;
4. a CHANGELOG.md line with the version, the RFC number and the date;
5. for a grammar change, GenerateVecDSL.mwe2 run in Eclipse with no ANTLR warnings, and the full test project green.

Deferred RFCs are reconsidered at the start of each minor release, in the order they were raised.
