package com.topper.dex.staticanalyser;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;
import com.topper.dex.graphs.DFG;

public class EmptyDFGAnalyser implements DFGAnalyser {

	@Override
	public DFG extractDFG(ImmutableList<DecompiledInstruction> instructions) {
		return null;
	}
}