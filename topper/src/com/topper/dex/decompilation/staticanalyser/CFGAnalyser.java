package com.topper.dex.decompilation.staticanalyser;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public interface CFGAnalyser {

	@Nullable
	CFG extractCFG(@NonNull final List<@NonNull DecompiledInstruction> instructions);
}