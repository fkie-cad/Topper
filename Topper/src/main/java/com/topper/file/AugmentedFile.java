package com.topper.file;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;

/**
 * File augmented with additional information like an
 * underlying buffer and a list of methods. It is used
 * to represent .dex and .vdex files.
 * 
 * @author Pascal Kühnemann
 * @since 09.08.2023
 * */
public interface AugmentedFile {

	/**
	 * Gets the {@link String} - id of this file.
	 * */
	@NonNull
	String getId();
	
	/**
	 * Gets a buffer associated with this file. The buffer
	 * does not necessarily represent the file contents.
	 * */
	byte @NonNull [] getBuffer();
	
	/**
	 * Gets a list of methods associated with this file.
	 * 
	 * @see DexMethod
	 * */
	@NonNull
	ImmutableList<@NonNull DexMethod> getMethods();
}