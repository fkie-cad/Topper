package com.topper.file;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;

/**
 * File composed of {@link DexFile}s and additional information like an
 * underlying buffer and an identifier. It is used to represent .dex and .vdex
 * files, but also binary files.
 * 
 * @author Pascal Kühnemann
 * @since 09.08.2023
 */
public interface ComposedFile {

	/**
	 * Gets the {@link String} - id of this file.
	 */
	@NonNull
	String getId();

	/**
	 * Gets a buffer associated with this file. The buffer does not necessarily
	 * represent the file contents.
	 */
	byte @NonNull [] getBuffer();

	/**
	 * Gets the offset of this file relative to another object.
	 */
	int getOffset();

	/**
	 * Gets a list of associated {@link DexFile}s of this file. These
	 * <code>DexFile</code>s are part of this file.
	 */
	@NonNull
	ImmutableList<@NonNull DexFile> getDexFiles();
}