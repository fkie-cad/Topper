package com.topper.dex.decompilation.semanticanalyser;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.dex.decompilation.pipeline.StageInfo;
import com.topper.exceptions.StageException;

public final class DefaultSemanticAnalyser<@NonNull T extends Map<@NonNull String, @NonNull StageInfo>> implements SemanticAnalyser<T> {

	@Override
	@NonNull
	public T execute(@NonNull final T results) throws StageException {
		return results;
	}
}