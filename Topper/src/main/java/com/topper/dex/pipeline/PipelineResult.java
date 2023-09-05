package com.topper.dex.pipeline;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Output of the entire {@link Pipeline} resulting from applying a
 * {@link Finalizer} to the {@link PipelineContext} after all {@link Stage}s
 * have been executed.
 * 
 * @author Pascal Kühnemann
 * @since 16.08.2023
 */
public class PipelineResult {

	/**
	 * {@link PipelineContext} of the {@link Pipeline} after all stages have been
	 * executed.
	 */
	@NonNull
	private final PipelineContext context;

	/**
	 * Creates a {@link PipelineResult} by storing a {@link PipelineContext}.
	 */
	public PipelineResult(@NonNull final PipelineContext context) {
		this.context = context;
	}

	/**
	 * Gets the {@link PipelineContext} that emerged from {@link Pipeline}
	 * execution.
	 */
	@NonNull
	public final PipelineContext getContext() {
		return this.context;
	}
}