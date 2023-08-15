package com.topper.dex.decompilation.pipeline;

import org.eclipse.jdt.annotation.NonNull;

public class PipelineResult {

	@NonNull
	private final PipelineContext context;
	
	public PipelineResult(@NonNull final PipelineContext context) {
		this.context = context;
	}
	
	@NonNull
	public final PipelineContext getContext() {
		return this.context;
	}
}