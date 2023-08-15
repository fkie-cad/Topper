package com.topper.dex.decompilation.pipeline;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Function that is applied to all {@link Stage} results. Its main purpose is to
 * collect the most important results of the entire {@link Pipeline}. However,
 * it is explicitly allowed to perform computations that go beyond collecting
 * information.
 * 
 * @author Pascal Kühnemann
 * @since 07.08.2023
 */
public interface Finalizer {

	/**
	 * Computes a function of all intermediate {@link Stage} results and returns a
	 * single {@link PipelineResult}. The function may be of arbitrary complexity.
	 * 
	 * @param results Mapping of string identifiers to {@link StageInfo} that represents
	 *                all intermediate results. Beware that if two stages in the
	 *                {@link Pipeline} use the same identifier, then only the last
	 *                <code>StageInfo</code> will be stored.
	 * @return A summary/function of all the intermediate <code>Stage</code> results.
	 */
	@NonNull
	PipelineResult finalize(@NonNull final PipelineContext context);
}