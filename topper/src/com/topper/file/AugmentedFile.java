package com.topper.file;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;

public interface AugmentedFile {

	@NonNull
	String getFilePath();
	
	byte @NonNull [] getBuffer();
	
	@NonNull
	ImmutableList<@NonNull DexMethod> getMethods();
}