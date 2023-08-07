package com.topper.dex.decompilation.pipeline;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.StageException;

public class DecompilationDriver {

	@NonNull
	private Pipeline pipeline;
	
	public DecompilationDriver() {
		this.pipeline = Pipeline.createDefaultPipeline();
	}
	
	@NonNull
	public final PipelineResult decompile(@NonNull final PipelineArgs args) throws StageException {
		return this.pipeline.execute(args);
	}
	
	public final void setPipeline(@NonNull final Pipeline pipeline) {
		this.pipeline = pipeline;
	}
}