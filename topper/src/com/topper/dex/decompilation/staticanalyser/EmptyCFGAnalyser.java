package com.topper.dex.decompilation.staticanalyser;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public class EmptyCFGAnalyser implements CFGAnalyser {

	@Override
	public CFG extractCFG(ImmutableList<DecompiledInstruction> instructions, final int entry) {
		return new CFG();
	}
}