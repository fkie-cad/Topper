package com.topper.dex.decompilation.sweeper;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.dex.decompilation.pipeline.StageInfo;
import com.topper.exceptions.StageException;

public class EmptySweeper<@NonNull T extends Map<@NonNull String, @NonNull StageInfo>> extends Sweeper<T> {

	@Override
	@NonNull
	public T execute(@NonNull final T results) throws StageException {
		return results;
	}
}