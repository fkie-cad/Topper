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
public final class RawFile implements ComposedFile {

	/**
	 * Identifier of this file. Often the file name.
	 */
	@NonNull
	private final String id;

	/**
	 * Associated file data.
	 */
	private final byte @NonNull [] buffer;

	/**
	 * Creates a raw binary file using only a {@link String} - id and a
	 * <code>buffer</code>.
	 * 
	 * As this is a binary file, no assumptions are made on its structure.
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
	 * Gets the default offset <code>0</code> of this file.
	 */
	@Override
	public final int getOffset() {
		return 0;
	}

	/**
	 * Gets an empty list of {@link DexFile}s.
	 */
	@SuppressWarnings("null")
	@Override
	@NonNull
	public final ImmutableList<@NonNull DexFile> getDexFiles() {
		return ImmutableList.of();
	}
}