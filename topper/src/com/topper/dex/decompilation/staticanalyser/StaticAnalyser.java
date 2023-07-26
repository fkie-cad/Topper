package com.topper.dex.decompilation.staticanalyser;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.Gadget;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public abstract class StaticAnalyser {

	@NonNull
	private CFGAnalyser cfgAnalyser;
	
	@NonNull
	private DFGAnalyser dfgAnalyser;
	
	public StaticAnalyser() {
		this.cfgAnalyser = new DefaultCFGAnalyser();
		this.dfgAnalyser = new DefaultDFGAnalyser();
	}
	
//	@NonNull
//	public final Gadget analyse(@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions) {
//		
//		// Extract CFG
//		final CFG cfg = this.cfgAnalyser.extractCFG(instructions);
//		
//		// Extract DFG. Maybe this requires CFG as well.
//		final DFG dfg = this.dfgAnalyser.extractDFG(instructions);
//		
//		return new Gadget(instructions, cfg, dfg);
//	}
	
	@NonNull public abstract Gadget analyse(@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions, final int entry);
	
	@NonNull
	public final CFGAnalyser getCFGAnalyser() {
		return this.cfgAnalyser;
	}
	
	public final void setCFGAnalyser(@NonNull final CFGAnalyser ca) {
		this.cfgAnalyser = ca;
	}
	
	@NonNull
	public final DFGAnalyser getDFGAnalyser() {
		return this.dfgAnalyser;
	}
	
	public final void setDFGAnalyser(@NonNull final DFGAnalyser da) {
		this.dfgAnalyser = da;
	}
}