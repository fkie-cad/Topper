package com.topper.dex.decompilation.staticanalyser;

import java.util.List;

import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public interface CFGAnalyser {

	CFG extractCFG(final List<DecompiledInstruction> instructions);
}