"""The cases of ../fixture.json against vecdsl_shapes. Run: python -I -m unittest discover -s <this folder>"""
import json
import math
import os
import sys
import unittest

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)

import vecdsl_shapes  # noqa: E402

FIXTURE = os.path.join(HERE, "..", "fixture.json")


def decode(value):
    if isinstance(value, dict):
        if set(value) == {"$number"}:
            return float(value["$number"])
        return {key: decode(item) for key, item in value.items()}
    if isinstance(value, list):
        return [decode(item) for item in value]
    return value


def same(expected, actual):
    """Equal values, number types aside, with object keys in the same order."""
    if isinstance(expected, bool) or isinstance(actual, bool) or expected is None or actual is None:
        return expected is actual or expected == actual and type(expected) is type(actual)
    if isinstance(expected, (int, float)) and isinstance(actual, (int, float)):
        return expected == actual or math.isnan(expected) and math.isnan(actual)
    if isinstance(expected, list) and isinstance(actual, list):
        return len(expected) == len(actual) and all(same(e, a) for e, a in zip(expected, actual))
    if isinstance(expected, dict) and isinstance(actual, dict):
        return list(expected) == list(actual) and all(same(expected[k], actual[k]) for k in expected)
    return type(expected) is type(actual) and expected == actual


with open(FIXTURE, encoding="utf-8") as source:
    FIXTURE_DATA = json.load(source)


class ShapeCases(unittest.TestCase):

    def test_cases(self):
        for case in FIXTURE_DATA["cases"]:
            with self.subTest(case["name"]):
                spec, candidates = decode(case["spec"]), decode(case["candidates"])
                if "error" in case:
                    kind = vecdsl_shapes.UnsupportedShape if case["error"]["kind"] == "unsupported" else ValueError
                    with self.assertRaises(kind) as raised:
                        vecdsl_shapes.shape(candidates, spec)
                    if kind is ValueError:
                        self.assertNotIsInstance(raised.exception, vecdsl_shapes.UnsupportedShape)
                    self.assertEqual(case["error"]["message"], str(raised.exception))
                else:
                    actual = vecdsl_shapes.shape(candidates, spec)
                    self.assertTrue(same(case["expected"], actual), f"\nexpected {json.dumps(case['expected'])}\nactual   {json.dumps(actual)}")

    def test_windows(self):
        for case in FIXTURE_DATA["windows"]:
            with self.subTest(case["name"]):
                self.assertEqual(case["expected"], vecdsl_shapes.window_size(case["spec"]))

    def test_input_is_not_changed(self):
        case = next(c for c in FIXTURE_DATA["cases"] if c["name"] == "pgvector boosted")
        candidates = decode(case["candidates"])
        before = json.dumps(candidates)
        vecdsl_shapes.shape(candidates, decode(case["spec"]))
        self.assertEqual(before, json.dumps(candidates))


if __name__ == "__main__":
    unittest.main()
