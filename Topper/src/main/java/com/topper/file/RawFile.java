package com.topper.file;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;

/**
 * Representation of a raw binary file. It may be used if only a memory dump of
 * some memory region suspected to store bytecode is available. Another use case
 * is to only consider memory around e.g. a switch instruction.
 * 
 * @author Pascal Kühnemann
 * @since 09.08.2023
 */
public class RawFile implements AugmentedFile {

	@NonNull
	private final String id;

	private final byte @NonNull [] buffer;

	/**
	 * Creates a raw binary file using only a {@link String} - id and a
	 * {@code buffer}.
	 * 
	 * As this is a binary file, no assumptions are made on its structure. This
	 * implies that {@link AugmentedFile.getMethods()} only provides an empty list.
	 * 
	 * @param id     Id of the augmented file.
	 * @param buffer Raw bytes to link to {@code file}.
	 */
	public RawFile(@NonNull final String id, final byte @NonNull [] buffer) {
		this.id = id;
		this.buffer = buffer;
	}

	@Override
	@NonNull
	public final String getId() {
		return this.id;
	}

	@Override
	public final byte @NonNull [] getBuffer() {
		return this.buffer;
	}

	/**
	 * Gets an empty list of {@link DexMethod}s, because this file does not have any
	 * structure.
	 */
	@Override
	public @NonNull ImmutableList<@NonNull DexMethod> getMethods() {
		return ImmutableList.of();
	}
}