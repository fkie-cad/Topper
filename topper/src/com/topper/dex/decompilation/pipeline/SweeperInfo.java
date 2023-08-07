package com.topper.dex.decompilation.pipeline;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public class SweeperInfo extends StageInfo {

	@NonNull
	private final ImmutableList<@NonNull ImmutableList<@NonNull DecompiledInstruction>> instructionSequences;
	
	public SweeperInfo(@NonNull final ImmutableList<@NonNull ImmutableList<@NonNull DecompiledInstruction>> sequences) {
		this.instructionSequences = sequences;
	}
	
	@NonNull
	public final ImmutableList<@NonNull ImmutableList<@NonNull DecompiledInstruction>> getInstructionSequences() {
		return this.instructionSequences;
	}
}