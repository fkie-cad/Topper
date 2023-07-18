package com.topper.dex.decompilation.staticanalyser;

import java.util.List;

import com.topper.dex.decompilation.graphs.DFG;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public class EmptyDFGAnalyser implements DFGAnalyser {

	@Override
	public DFG extractDFG(List<DecompiledInstruction> instructions) {
		return null;
	}
}