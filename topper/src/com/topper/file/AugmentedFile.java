package com.topper.file;

import java.io.File;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;

public interface AugmentedFile {

	@NonNull
	File getFile();
	
	byte @NonNull [] getBuffer();
	
	@NonNull
	ImmutableList<@NonNull DexMethod> getMethods();
}