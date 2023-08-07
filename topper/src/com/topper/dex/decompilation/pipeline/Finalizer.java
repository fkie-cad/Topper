package com.topper.dex.decompilation.pipeline;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

public interface Finalizer<@NonNull T extends Map<@NonNull String, @NonNull StageInfo>> {

	@NonNull
	PipelineResult<T> finalize(@NonNull final T results);
}