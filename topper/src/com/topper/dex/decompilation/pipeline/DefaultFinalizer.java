package com.topper.dex.decompilation.pipeline;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

public class DefaultFinalizer<@NonNull T extends Map<@NonNull String, @NonNull StageInfo>> implements Finalizer<T> {

	@Override
	public @NonNull PipelineResult<T> finalize(@NonNull final T results) {
		return new PipelineResult<T>(results);
	}
}