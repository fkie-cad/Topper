package com.topper.dex.pipeline;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.dex.staticanalyser.Gadget;
import com.topper.exceptions.pipeline.StageException;

/**
 * Driver that unites all decompilation steps into a single
 * interface (like a Facade pattern). It is used to execute
 * decompilation {@link Pipeline}s that are responsible for
 * {@link Gadget} extraction.
 * 
 * Initially, a default pipeline is set to ensure correct
 * execution. However, it is possible to replace the default
 * pipeline with any other custom pipeline. Notice that
 * a custom pipeline must adhere to pipeline constraints.
 * 
 * @author Pascal Kühnemann
 * @since 07.08.2023
 * */
public class DecompilationDriver {

	/**
	 * <code>Pipeline</code> to use when decompiling bytecode.
	 * */
	@NonNull
	private Pipeline pipeline;
	
	public DecompilationDriver() {
		this.pipeline = Pipeline.createDefaultPipeline();
	}
	
	/**
	 * Attempts to decompile whatever is specified in <code>args</code>.
	 * 
	 * Depending on the underlying implementations, this method can
	 * be time consuming.
	 * 
	 * @param args Description of the arguments required for pipeline execution.
	 * 	Different {@link Stage}s may require different arguments.
	 * @return Result representing the output of the pipeline.
	 * @throws StageException If an error occurs within a {@link Stage} in the {@code Pipeline}.
	 * */
	@NonNull
	public final PipelineResult decompile(@NonNull final PipelineArgs args) throws StageException {
		return this.pipeline.execute(args);
	}
	
	/**
	 * Overwrites the current pipeline with a custom pipeline.
	 * */
	public final void setPipeline(@NonNull final Pipeline pipeline) {
		this.pipeline = pipeline;
	}
}