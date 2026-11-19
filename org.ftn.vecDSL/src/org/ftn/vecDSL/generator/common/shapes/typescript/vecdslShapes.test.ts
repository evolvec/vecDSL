/** The cases of ../fixture.json against vecdslShapes. Run: tsc -p . && node --test dist/vecdslShapes.test.js */
import { test } from "node:test";
import { strict as assert } from "node:assert";
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { Json, shape, UnsupportedShape, windowSize } from "./vecdslShapes";

type Obj = { [key: string]: Json };

interface Case {
  name: string;
  spec: Obj;
  candidates: Json[];
  expected?: Json;
  error?: { kind: string; message: string };
}

const fixture = JSON.parse(readFileSync(join(__dirname, "..", "..", "fixture.json"), "utf-8")) as {
  cases: Case[];
  windows: Array<{ name: string; spec: Obj; expected: number | null }>;
};

function decode(item: Json): Json {
  if (Array.isArray(item)) return item.map(decode);
  if (typeof item === "object" && item !== null) {
    const keys = Object.keys(item);
    if (keys.length === 1 && keys[0] === "$number") return Number(item.$number);
    const result: Obj = {};
    for (const key of keys) result[key] = decode(item[key]);
    return result;
  }
  return item;
}

for (const entry of fixture.cases) {
  test(entry.name, () => {
    const candidates = decode(entry.candidates) as Json[];
    const spec = decode(entry.spec) as Obj;
    if (entry.error !== undefined) {
      const kind = entry.error.kind;
      const message = entry.error.message;
      assert.throws(() => shape(candidates, spec), (error: unknown) => {
        assert.ok(error instanceof Error);
        assert.equal(error instanceof UnsupportedShape, kind === "unsupported");
        assert.equal(error.message, message);
        return true;
      });
    } else {
      assert.equal(JSON.stringify(shape(candidates, spec)), JSON.stringify(entry.expected));
    }
  });
}

for (const entry of fixture.windows) {
  test(`window ${entry.name}`, () => {
    assert.equal(windowSize(entry.spec), entry.expected);
  });
}

test("the input is not changed", () => {
  const entry = fixture.cases.find((c) => c.name === "pgvector boosted") as Case;
  const candidates = decode(entry.candidates) as Json[];
  const before = JSON.stringify(candidates);
  shape(candidates, decode(entry.spec) as Obj);
  assert.equal(JSON.stringify(candidates), before);
});
