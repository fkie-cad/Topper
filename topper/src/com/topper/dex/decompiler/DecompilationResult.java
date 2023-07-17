package com.topper.dex.decompiler;

import org.jf.dexlib2.dexbacked.DexBuffer;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompiler.instructions.BufferedInstruction;

public class DecompilationResult {
	
	private final DexBuffer buffer;
	
	private final ImmutableList<BufferedInstruction> instructions;

	public DecompilationResult(final DexBuffer buffer, final ImmutableList<BufferedInstruction> instructions) {
		this.buffer = buffer;
		this.instructions = instructions;
	}
	
	public final DexBuffer getBuffer() {
		return this.buffer;
	}
	
	public final ImmutableList<BufferedInstruction> getInstructions() {
		return this.instructions;
	}
}
