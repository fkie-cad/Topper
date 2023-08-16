package com.topper.dex.decompilation.staticanalyser;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.graphs.DFG;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public final class DefaultDFGAnalyser implements DFGAnalyser {

	@Override
	@NonNull
	public DFG extractDFG(ImmutableList<DecompiledInstruction> instructions) {
		return new DFG();
	}
}