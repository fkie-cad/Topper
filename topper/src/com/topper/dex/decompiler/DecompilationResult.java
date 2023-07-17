package com.topper.dex.decompiler;

import org.jf.dexlib2.dexbacked.DexBuffer;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public class DecompilationResult {
	
	private final DexBuffer buffer;
	
	private final ImmutableList<DecompiledInstruction> instructions;

	public DecompilationResult(final DexBuffer buffer, final ImmutableList<DecompiledInstruction> instructions) {
		this.buffer = buffer;
		this.instructions = instructions;
	}
	
	public final DexBuffer getBuffer() {
		return this.buffer;
	}
	
	public final ImmutableList<DecompiledInstruction> getInstructions() {
		return this.instructions;
	}
}