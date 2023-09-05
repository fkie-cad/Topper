package com.topper.file;

import java.util.concurrent.ExecutorService;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.Config;
import com.topper.configuration.ConfigManager;
import com.topper.configuration.TopperConfig;
import com.topper.dex.decompilation.decompiler.Decompiler;
import com.topper.dex.decompilation.staticanalyser.CFGAnalyser;

/**
 * Dex file representation based on dexlib2. It parses the contents of a given
 * buffer into a {@link DexBackedDexFile}.
 * 
 * @author Pascal Kühnemann
 * @since 09.08.2023
 */
public class DexFile implements ComposedFile {

	/**
	 * Id of this file. It should refer to the .dex file, whose contents reside in
	 * <code>buffer</code>, but it is not mandatory.
	 */
	@NonNull
	private final String id;

	/**
	 * Associated data of this file. Often the file contents.
	 */
	private final byte @NonNull [] buffer;

	/**
	 * Offset of this .dex file.
	 */
	private final int offset;

	/**
	 * Parsed .dex file used to extract e.g. method information.
	 */
	@NonNull
	private final DexBackedDexFile dexFile;

	/**
	 * Creates a .dex file using a {@link String} - id and a <code>buffer</code>.
	 * 
	 * It uses the {@link ConfigManager} to obtain valid {@link Opcodes}, which are
	 * used by {@link DexBackedDexFile}.
	 * 
	 * @param id     Id of the augmented file.
	 * @param buffer Raw bytes that represent a valid .dex file.
	 * @param offset Offset of this .dex file relative to some other object (e.g.
	 *               .vdex).
	 * @param config Configuration to use during .dex file creation.
	 * @throws IllegalArgumentException If the buffer is empty or does not contain a
	 *                                  valid .dex file.
	 */
	public DexFile(@NonNull final String id, final byte @NonNull [] buffer, final int offset,
			@NonNull final TopperConfig config) {
		if (buffer.length == 0) {
			throw new IllegalArgumentException("buffer must not be empty");
		}

		this.id = id;
		this.buffer = buffer;
		this.offset = offset;

		try {
			final Opcodes opcodes = ConfigManager.get().getDecompilerConfig().getOpcodes();
			this.dexFile = new DexBackedDexFile(opcodes, buffer);
		} catch (final RuntimeException e) {
			throw new IllegalArgumentException("buffer must represent a valid .dex file.", e);
		}
	}

	@Override
	@NonNull
	public final String getId() {
		return this.id;
	}

	@Override
	public byte @NonNull [] getBuffer() {
		return this.buffer;
	}

	@Override
	public final int getOffset() {
		return this.offset;
	}

	/**
	 * Gets a list of this single {@link DexFile}.
	 */
	@SuppressWarnings("null")
	@Override
	@NonNull
	public final ImmutableList<@NonNull DexFile> getDexFiles() {
		return ImmutableList.of(this);
	}

	/**
	 * Gets the underlying {@link DexBackedDexFile} of this file. A {@link DexFile}
	 * is in a 1:1 correspondence with <code>DexBackedDexFile</code>, whereas
	 * {@link VDexFile} is in a 1:n relation.
	 */
	@NonNull
	public final DexBackedDexFile getDexFile() {
		return this.dexFile;
	}
}