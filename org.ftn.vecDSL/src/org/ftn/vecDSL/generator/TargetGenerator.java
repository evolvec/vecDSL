package org.ftn.vecDSL.generator;

import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.ftn.vecDSL.vecDSL.Script;

/** Code generator for one target vendor; writes into the output configuration named by {@link #vendor()}. */
public interface TargetGenerator {

	String vendor();

	void generate(Script script, IFileSystemAccess2 fsa);
}
