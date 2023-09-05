package com.topper.dex.staticanalyser;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;
import com.topper.dex.graphs.CFG;

public class EmptyCFGAnalyser implements CFGAnalyser {

	@Override
	public CFG extractCFG(ImmutableList<DecompiledInstruction> instructions, final int entry) {
		return new CFG(entry);
	}
}