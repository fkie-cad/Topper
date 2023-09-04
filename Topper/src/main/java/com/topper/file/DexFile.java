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
 * Dex file representation base on dexlib2. It parses the contents of a given
 * buffer into a {@link DexBackedDexFile}, extracts {@link DexMethod}s and, if
 * requested, performs static analysis to elevate the underlying bytecode into
 * Smali and to obtain a Control Flow Graph for each method.
 * 
 * @author Pascal Kühnemann
 * @since 09.08.2023
 */
public class DexFile implements AugmentedFile {

	/**
	 * Id of the augmented file. It should refer to the .dex file, whose contents reside
	 * in {@code buffer}, but it is not mandatory.
	 */
	@NonNull
	private final String id;

	/**
	 * Contents of this file. They may be based on the contents of {@link file}.
	 */
	private final byte @NonNull [] buffer;
	
	/**
	 * Offset of this .dex file.
	 * */
	private final int offset;

	/**
	 * Parsed .dex file used to extract e.g. method information.
	 */
	@NonNull
	private final DexBackedDexFile dexFile;

	/**
	 * Creates a .dex file using a {@link String} - id and a {@code buffer}. If both
	 * {@link Decompiler} and {@link CFGAnalyser} are valid, they will be used to
	 * analyse all methods stored in the .dex file in {@code buffer}.
	 * 
	 * If CFG extraction for a given method fails due to an internal error, or the
	 * method is abstract or native, or either one of {@code decompiler} or
	 * {@code analyser} is {@code null}, then the method will not be analysed.
	 * 
	 * Loading methods of a .dex file uses an {@link ExecutorService} with a
	 * configurable number of threads to speed up analysis. Adjust the global
	 * configuration {@link Config.setDefaultAmountThreads} using
	 * {@link ConfigManager} to change the number of threads used (at least 1).
	 * 
	 * @param id     Id of the augmented file.
	 * @param buffer Raw bytes that represent a valid .dex file.
	 * @param offset Offset of this .dex file relative to some other object (e.g. .vdex).
	 * @param config Configuration to use during .dex file creation.
	 * @throws IllegalArgumentException If the buffer is empty or does not contain a
	 *                                  valid .dex file.
	 */
	public DexFile(@NonNull final String id, final byte @NonNull [] buffer, final int offset, @NonNull final TopperConfig config) {
		if (buffer.length == 0) {
			throw new IllegalArgumentException("buffer must not be empty");
		}

		this.id = id;
		this.buffer = buffer;
		this.offset = offset;

		try {
			final Opcodes opcodes = Opcodes.forDexVersion(ConfigManager.get().getDecompilerConfig().getDexVersion());
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
	
	@SuppressWarnings("null")
	@Override
	@NonNull
	public final ImmutableList<@NonNull DexFile> getDexFiles() {
		return ImmutableList.of(this);
	}

	@NonNull
	public final DexBackedDexFile getDexFile() {
		return this.dexFile;
	}
}