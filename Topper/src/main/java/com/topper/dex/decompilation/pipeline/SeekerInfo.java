package com.topper.dex.decompilation.pipeline;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;

public class SeekerInfo extends StageInfo {
	
	@NonNull
	private final ImmutableList<Integer> pivotOffsets;
	
	public SeekerInfo(@NonNull final ImmutableList<Integer> pivotOffsets) {
		this.pivotOffsets = pivotOffsets;
	}
	
	@NonNull
	public final ImmutableList<Integer> getPivotOffsets() {
		return this.pivotOffsets;
	}
}