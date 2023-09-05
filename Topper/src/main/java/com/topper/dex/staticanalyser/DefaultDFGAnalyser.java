package com.topper.dex.staticanalyser;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;
import com.topper.dex.graphs.DFG;

public final class DefaultDFGAnalyser implements DFGAnalyser {

	@Override
	@NonNull
	public DFG extractDFG(ImmutableList<DecompiledInstruction> instructions) {
		return new DFG();
	}
}