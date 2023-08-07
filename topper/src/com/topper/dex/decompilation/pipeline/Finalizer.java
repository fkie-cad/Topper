package com.topper.dex.decompilation.pipeline;

import java.util.Map;

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
public interface Finalizer<@NonNull T extends Map<@NonNull String, @NonNull StageInfo>> {

	/**
	 * Computes a function of all intermediate {@code Stage} results and returns a
	 * single {@link PipelineResult}. The function may be of arbitrary complexity.
	 * 
	 * @param results Mapping of identifiers -> {@link StageInfo} that represents
	 *                all intermediate results. Beware that if two stages in the
	 *                {@code Pipeline} use the same identifier, then only the last
	 *                {@code StageInfo} will be stored.
	 * @return A summary/function of all the intermediate {@code Stage} results.
	 */
	@NonNull
	PipelineResult<T> finalize(@NonNull final T results);
}