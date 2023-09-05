package com.topper.dex.pipeline;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

/**
 * Output of a {@link Sweeper}.
 * 
 * @author Pascal Kühnemann
 * @since 16.08.2023
 * */
public class SweeperInfo extends StageInfo {

	/**
	 * List of {@link DecompiledInstruction} sequences.
	 * */
	@NonNull
	private final ImmutableList<@NonNull ImmutableList<@NonNull DecompiledInstruction>> instructionSequences;
	
	/**
	 * Creates a new {@link SweeperInfo} by storing a list of {@link DecompiledInstruction} sequences.
	 * */
	public SweeperInfo(@NonNull final ImmutableList<@NonNull ImmutableList<@NonNull DecompiledInstruction>> sequences) {
		this.instructionSequences = sequences;
	}
	
	/**
	 * Gets a list of {@link DecompiledInstruction} sequences.
	 * */
	@NonNull
	public final ImmutableList<@NonNull ImmutableList<@NonNull DecompiledInstruction>> getInstructionSequences() {
		return this.instructionSequences;
	}
}