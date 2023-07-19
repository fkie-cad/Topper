package com.topper.dex.decompilation.staticanalyser;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.Gadget;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompilation.graphs.DFG;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public final class DefaultStaticAnalyser extends StaticAnalyser {

	@NonNull
	public final Gadget analyse(@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions) {
		
		// Extract CFG
		final CFG cfg = this.getCFGAnalyser().extractCFG(instructions);
		
		// Extract DFG. Maybe this requires CFG as well.
		final DFG dfg = this.getDFGAnalyser().extractDFG(instructions);
		
		return new Gadget(instructions, cfg, dfg);
	}
}