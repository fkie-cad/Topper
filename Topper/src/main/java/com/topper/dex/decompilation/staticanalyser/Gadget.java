package com.topper.dex.decompilation.staticanalyser;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.jf.dexlib2.Opcode;

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
		int max = 0;
		for (@NonNull final DecompiledInstruction insn : this.instructions) {
			if (insn.getInstruction().getOpcode().equals(Opcode.PACKED_SWITCH_PAYLOAD) ||
					insn.getInstruction().getOpcode().equals(Opcode.SPARSE_SWITCH_PAYLOAD) ||
					insn.getInstruction().getOpcode().equals(Opcode.ARRAY_PAYLOAD)) {
				continue;
			}
			
			if (insn.getByteCode().length > max) {
				max = insn.getByteCode().length;
			}
		}
		
		for (final DecompiledInstruction insn : this.instructions) {
			
			if (insn.getByteCode().length <= max) {
				// Non - payload instruction, or small payload
				for (final byte raw : insn.getByteCode()) {
					b.append(String.format("%02x ", raw));
				}
				
				// Fill up with spaces
				for (int i = 0; i < (max - insn.getByteCode().length) * 3; i++) {
					b.append(' ');
				}
				b.append(insn.getInstructionString() + System.lineSeparator());
			} else {
				b.append(insn.getInstructionString() + System.lineSeparator());
			}
		}
		
		return b.toString();
	}
}