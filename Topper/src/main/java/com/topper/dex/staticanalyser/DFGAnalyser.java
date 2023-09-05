package com.topper.dex.staticanalyser;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;
import com.topper.dex.graphs.DFG;

public interface DFGAnalyser {

	@Nullable
	DFG extractDFG(@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions);
}