package com.topper.dex.decompilation.staticanalyser;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public interface CFGAnalyser {

	@Nullable
	CFG extractCFG(@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions);
}