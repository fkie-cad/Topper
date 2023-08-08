package com.topper.file;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;

public class RawFile implements AugmentedFile {

	@NonNull
	private final String filePath;
	
	private final byte @NonNull [] buffer;
	
	public RawFile(@NonNull final String filePath, final byte @NonNull [] buffer) {
		this.filePath = filePath;
		this.buffer = buffer;
	}
	
	@Override
	@NonNull
	public final String getFilePath() {
		return this.filePath;
	}
	
	@Override
	public final byte @NonNull [] getBuffer() {
		return this.buffer;
	}

	@Override
	public @NonNull ImmutableList<@NonNull DexMethod> getMethods() {
		return ImmutableList.of();
	}
}