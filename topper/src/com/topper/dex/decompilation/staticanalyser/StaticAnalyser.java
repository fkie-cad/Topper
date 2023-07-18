package com.topper.dex.decompilation.staticanalyser;

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
	
	public final Gadget analyse(final ImmutableList<DecompiledInstruction> instructions) {
		
		// Extract CFG
		final CFG cfg = this.cfgAnalyser.extractCFG(instructions);
		
		// Extract DFG. Maybe this requires CFG as well.
		final DFG dfg = this.dfgAnalyser.extractDFG(instructions);
		
		return new Gadget(instructions, cfg, dfg);
	}
	
	public final void setCFGAnalyser(final CFGAnalyser ca) {
		if (ca == null) {
			throw new IllegalArgumentException("CFG analyser must not be null.");
		}
		this.cfgAnalyser = ca;
	}
	
	public final void setDFGAnalyser(final DFGAnalyser da) {
		if (da == null) {
			throw new IllegalArgumentException("DFG analyser must not be null.");
		}
		this.dfgAnalyser = da;
	}
}