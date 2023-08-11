package com.topper.dex.decompilation.staticanalyser;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.graphs.DFG;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public class EmptyDFGAnalyser implements DFGAnalyser {

	@Override
	public DFG extractDFG(ImmutableList<DecompiledInstruction> instructions) {
		return null;
	}
}