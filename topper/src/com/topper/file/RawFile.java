package com.topper.file;

import java.io.File;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;

import com.google.common.collect.ImmutableList;

public class RawFile implements AugmentedFile {

	@NonNull
	private final File file;
	
	private final byte @NonNull [] buffer;
	
	public RawFile(@NonNull final File file, final byte @NonNull [] buffer) {
		this.file = file;
		this.buffer = buffer;
	}
	
	@Override
	@NonNull
	public final File getFile() {
		return this.file;
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