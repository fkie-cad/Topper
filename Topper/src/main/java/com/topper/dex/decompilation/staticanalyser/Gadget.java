package com.topper.dex.decompilation.staticanalyser;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompilation.graphs.DFG;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public class Gadget {

	@NonNull
	private final ImmutableList<@NonNull DecompiledInstruction> instructions;
	
	@Nullable
	private final CFG cfg;
	
	@Nullable
	private final DFG dfg;
	
	public Gadget(@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions, @Nullable final CFG cfg, @Nullable DFG dfg) {
		this.instructions = instructions;
		this.cfg = cfg;
		this.dfg = dfg;
	}
	
	@NonNull
	public final ImmutableList<@NonNull DecompiledInstruction> getInstructions() {
		return this.instructions;
	}
	
	@Nullable
	public final CFG getCFG() {
		return this.cfg;
	}
	
	public final boolean hasCFG() {
		return this.cfg != null;
	}
	
	@Nullable
	public final DFG getDFG() {
		return this.dfg;
	}
	
	public final boolean hasDFG() {
		return this.dfg != null;
	}
	
	@Override
	public final String toString() {
		final StringBuilder b = new StringBuilder();
		
		// Print entry
		b.append(String.format("Entry: %#08x" + System.lineSeparator(), (this.cfg != null) ? this.cfg.getEntry() : this.instructions.get(0).getOffset()));
		
		// Print instructions
		for (final DecompiledInstruction insn : this.instructions) {
			b.append(insn.getInstructionString() + System.lineSeparator());
		}
		
		return b.toString();
	}
}