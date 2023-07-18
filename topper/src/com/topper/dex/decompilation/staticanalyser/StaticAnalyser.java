package com.topper.dex.decompilation.staticanalyser;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.Gadget;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompilation.graphs.DFG;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public final class StaticAnalyser {

	private CFGAnalyser cfgAnalyser;
	private DFGAnalyser dfgAnalyser;
	
	public StaticAnalyser() {
		this.cfgAnalyser = new DefaultCFGAnalyser();
		this.dfgAnalyser = new DefaultDFGAnalyser();
	}
	
	@NonNull
	public final Gadget analyse(@NonNull final ImmutableList<DecompiledInstruction> instructions) {
		
		// Extract CFG
		final CFG cfg = this.cfgAnalyser.extractCFG(instructions);
		
		// Extract DFG. Maybe this requires CFG as well.
		final DFG dfg = this.dfgAnalyser.extractDFG(instructions);
		
		return new Gadget(instructions, cfg, dfg);
	}
	
	public final void setCFGAnalyser(@NonNull final CFGAnalyser ca) {
		this.cfgAnalyser = ca;
	}
	
	public final void setDFGAnalyser(@NonNull final DFGAnalyser da) {
		this.dfgAnalyser = da;
	}
}