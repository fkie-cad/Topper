package com.topper.dex.staticanalyser;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;
import com.topper.dex.graphs.CFG;

public interface CFGAnalyser {

	@NonNull
	CFG extractCFG(@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions, final int entry);
}