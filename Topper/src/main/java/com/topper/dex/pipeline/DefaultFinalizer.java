package com.topper.dex.pipeline;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Default finalizer implementation that simply forwards all
 * {@link Stage} results.
 * 
 * @author Pascal Kühnemann
 * @since 07.08.2023
 * */
public class DefaultFinalizer implements Finalizer {

	/**
	 * Finalizes the pipeline by wrapping all {@link Stage} results.
	 * */
	@Override
	@NonNull
	public final PipelineResult finalize(@NonNull final PipelineContext context) {
		return new PipelineResult(context);
	}
}