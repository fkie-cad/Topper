package com.topper.dex.decompilation.pipeline;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.StageException;

public interface Stage<@NonNull T extends Map<@NonNull String, @NonNull StageInfo>> {
	@NonNull
	T execute(@NonNull final T results) throws StageException;
}