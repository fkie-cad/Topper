package com.topper.dex.decompilation.pipeline;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Default finalizer implementation that simply forwards all
 * {@link Stage} results.
 * 
 * @author Pascal Kühnemann
 * @since 07.08.2023
 * */
public class DefaultFinalizer<@NonNull T extends Map<@NonNull String, @NonNull StageInfo>> implements Finalizer<T> {

	/**
	 * Finalizes the pipeline by wrapping all {@link Stage} results.
	 * */
	@Override
	public @NonNull PipelineResult<T> finalize(@NonNull final T results) {
		return new PipelineResult<T>(results);
	}
}