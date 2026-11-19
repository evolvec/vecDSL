package org.ftn.vecDSL.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

/** The hand-written argument parser: flags, valued options in both spellings, aliases, operands and every error. */
public class ArgsTest {

	private static final Args.Spec SPEC = Args.Spec.of(Set.of("--check"), Set.of("--width", "--output"))
			.alias("-o", "--output");

	private static Args parse(String... words) throws UsageException {
		return Args.parse(List.of(words), SPEC);
	}

	@Test
	public void flagsOptionsAndOperandsInAnyOrder() throws UsageException {
		Args args = parse("a.vecDSL", "--width", "80", "--check", "b.vecDSL", "--output=out");
		assertTrue(args.flag("--check"));
		assertEquals(Map.of("--width", "80", "--output", "out"), args.options());
		assertEquals(List.of("a.vecDSL", "b.vecDSL"), args.operands());
	}

	@Test
	public void aliasStandsForTheLongOption() throws UsageException {
		assertEquals("dir", parse("-o", "dir").option("--output"));
		assertEquals("dir", parse("-o=dir").option("--output"));
	}

	@Test
	public void doubleDashEndsTheOptions() throws UsageException {
		Args args = parse("--", "--check", "-o");
		assertFalse(args.flag("--check"));
		assertEquals(List.of("--check", "-o"), args.operands());
	}

	@Test
	public void singleDashIsAnOperand() throws UsageException {
		assertEquals(List.of("-"), parse("-").operands());
	}

	@Test
	public void errors() {
		assertEquals("unknown option --wide", assertThrows(UsageException.class, () -> parse("--wide")).getMessage());
		assertEquals("--width needs a value", assertThrows(UsageException.class, () -> parse("--width")).getMessage());
		assertEquals("--width needs a value", assertThrows(UsageException.class, () -> parse("--width=")).getMessage());
		assertEquals("--check takes no value",
				assertThrows(UsageException.class, () -> parse("--check=yes")).getMessage());
		assertEquals("--output is given twice",
				assertThrows(UsageException.class, () -> parse("-o", "a", "--output", "b")).getMessage());
	}

	@Test
	public void operandCounts() throws UsageException {
		assertEquals("missing program file",
				assertThrows(UsageException.class, () -> parse().operands(1, 1, "program file")).getMessage());
		assertEquals("too many operands: c d",
				assertThrows(UsageException.class, () -> parse("a", "b", "c", "d").operands(1, 2, "files"))
						.getMessage());
		assertEquals(List.of("a"), parse("a").operands(1, 1, "file"));
	}

	@Test
	public void positiveIntegers() throws UsageException {
		assertEquals(100, parse().positive("--width", 100));
		assertEquals(80, parse("--width", "80").positive("--width", 100));
		assertEquals("--width needs a positive integer, not '0'",
				assertThrows(UsageException.class, () -> parse("--width", "0").positive("--width", 100))
						.getMessage());
		assertEquals("--width needs a positive integer, not 'wide'",
				assertThrows(UsageException.class, () -> parse("--width", "wide").positive("--width", 100))
						.getMessage());
	}

	@Test
	public void commaSeparatedLists() throws UsageException {
		assertEquals(List.of("milvus", "qdrant"), parse("--width", " milvus, ,qdrant ").list("--width"));
		assertEquals(List.of(), parse().list("--width"));
	}
}
