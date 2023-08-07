package com.topper.dex.decompilation.pipeline;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

public class PipelineResult<@NonNull T extends Map<@NonNull String, @NonNull StageInfo>> {

	@NonNull
	private final T results;
	
	public PipelineResult(@NonNull final T results) {
		this.results = results;
	}
	
	@NonNull
	public final T getResults() {
		return this.results;
	}
}