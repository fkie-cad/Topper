package com.topper.dex.decompilation.staticanalyser;

import java.util.List;

import com.topper.dex.decompilation.graphs.DFG;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public interface DFGAnalyser {

	DFG extractDFG(final List<DecompiledInstruction> instructions);
}