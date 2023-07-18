package com.topper.dex.decompilation;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompilation.graphs.DFG;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public final class Gadget {

	private final ImmutableList<DecompiledInstruction> instructions;
	private final CFG cfg;
	private final DFG dfg;
	
	public Gadget(final ImmutableList<DecompiledInstruction> instructions, final CFG cfg, final DFG dfg) {
		this.instructions = instructions;
		this.cfg = cfg;
		this.dfg = dfg;
	}
	
	public final ImmutableList<DecompiledInstruction> getInstructions() {
		return this.instructions;
	}
	
	public final CFG getCFG() {
		return this.cfg;
	}
	
	public final DFG getDFG() {
		return this.dfg;
	}
}