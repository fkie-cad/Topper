package com.topper.dex.pipeline;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;

/**
 * Output of the {@link Seeker}.
 * 
 * @author Pascal Kühnemann
 * @since 16.08.2023
 * */
public class SeekerInfo extends StageInfo {
	
	/**
	 * List of offsets referencing pivot opcodes.
	 * */
	@NonNull
	private final ImmutableList<Integer> pivotOffsets;
	
	/**
	 * Creates a {@link SeekerInfo} by storing a list of pivot opcode offsets.
	 * */
	public SeekerInfo(@NonNull final ImmutableList<Integer> pivotOffsets) {
		this.pivotOffsets = pivotOffsets;
	}
	
	/**
	 * Gets a list of pivot opcode offsets.
	 * */
	@NonNull
	public final ImmutableList<Integer> getPivotOffsets() {
		return this.pivotOffsets;
	}
}