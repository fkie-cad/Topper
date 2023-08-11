package com.topper.file;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.util.ExceptionWithContext;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.ConfigManager;
import com.topper.dex.decompilation.decompiler.Decompiler;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompilation.staticanalyser.CFGAnalyser;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

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
	 * File to be augmented. It should refer to the .dex file, whose contents reside
	 * in {@code buffer}, but it is not mandatory.
	 */
	@NonNull
	private final File file;

	/**
	 * Contents of this file. They may be based on the contents of {@link file}.
	 */
	private final byte @NonNull [] buffer;

	/**
	 * Parsed .dex file used to extract e.g. method information.
	 */
	@NonNull
	private final DexBackedDexFile dexFile;

	/**
	 * List of all methods stored in this .dex file.
	 */
	@NonNull
	private final ImmutableList<@NonNull DexMethod> methods;

	/**
	 * Creates a .dex file using only a {@link File} and a {@code buffer}.
	 * Internally, methods extracted from {@code buffer} are not decompiled and no
	 * CFG is constructed.
	 * 
	 * @param file   File to be augmented.
	 * @param buffer Raw bytes that represent a valid .dex file.
	 * @throws IllegalArgumentException If the buffer is empty or does not contain a
	 *                                  valid .dex file.
	 */
	public DexFile(@NonNull final File file, final byte @NonNull [] buffer) {
		this(file, buffer, null, null);
	}

	/**
	 * Creates a .dex file using a {@link File} and a {@code buffer}. If both
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
	 * @param file       File to be augmented.
	 * @param buffer     Raw bytes that represent a valid .dex file.
	 * @param decompiler A {@code Decompiler} used in conjunction with
	 *                   {@code analyser} to decompile all methods.
	 * @param analyser   A {@code CFGAnalyser} used in conjunction with
	 *                   {@code decompiler} to extract Control Flow Graphs for all
	 *                   methods.
	 * @throws IllegalArgumentException If the buffer is empty or does not contain a
	 *                                  valid .dex file.
	 */
	public DexFile(@NonNull final File file, final byte @NonNull [] buffer, @Nullable final Decompiler decompiler,
			@Nullable final CFGAnalyser analyser) {
		if (buffer.length == 0) {
			throw new IllegalArgumentException("buffer must not be empty");
		}

		this.file = file;
		this.buffer = buffer;

		try {
			this.dexFile = new DexBackedDexFile(Opcodes.getDefault(), buffer);
		} catch (final RuntimeException e) {
			throw new IllegalArgumentException("buffer must represent a valid .dex file.", e);
		}
		this.methods = this.loadMethods(this.dexFile, decompiler, analyser);
	}

	@Override
	public @NonNull File getFile() {
		return this.file;
	}

	@Override
	public byte @NonNull [] getBuffer() {
		return this.buffer;
	}

	@Override
	public @NonNull ImmutableList<@NonNull DexMethod> getMethods() {
		return this.methods;
	}

	/**
	 * Loads methods from a given {@link DexBackedDexFile}. Optionally, a
	 * {@link Decompiler} and a {@link CFGAnalyser} are used to extract a Control
	 * Flow Graph(CFG) from each method.
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
	 * @param file       Parsed .dex file used for method enumeration.
	 * @param decompiler {@code Decompiler} to use for decompiling bytes into Smali.
	 * @param analyser   {@code CFGAnalyser} to use for constructing CFGs for
	 *                   decompiled methods.
	 * @return List of decompiled methods.
	 */
	@NonNull
	private final ImmutableList<@NonNull DexMethod> loadMethods(@NonNull final DexBackedDexFile file,
			@Nullable final Decompiler decompiler, @Nullable final CFGAnalyser analyser) {

		ImmutableList.Builder<@NonNull DexMethod> methods = new ImmutableList.Builder<>();

		// Use a thread pool to handle classes. This mainly helps
		// in case of large .dex files.
		final List<Future<List<@NonNull DexMethod>>> results = new ArrayList<>(file.getClasses().size());
		final ExecutorService pool = Executors
				.newFixedThreadPool(ConfigManager.get().getGeneralConfig().getDefaultAmountThreads());

		// Iterate over methods
		for (final DexBackedClassDef cls : file.getClasses()) {

			if (cls == null) {
				continue;
			}

			results.add((Future<List<@NonNull DexMethod>>) pool.submit(() -> {
				int offset;
				int size;
				CFG cfg;
				ImmutableList<@NonNull DecompiledInstruction> instructions;

				final List<@NonNull DexMethod> clsMethods = new LinkedList<>();
				for (final DexBackedMethod method : cls.getMethods()) {

					if (method == null) {
						continue;
					}

					// Extract control flow graph if requested. If an
					// error occurs, skip cfg extraction
					cfg = null;
					try {
						if (decompiler != null && analyser != null) {

							// Decompile method.
							// offset == 0: abstract or native method
							offset = DexHelper.getMethodOffset(method);
							if (offset != 0) {
								size = DexHelper.getMethodSize(method, offset);
								instructions = decompiler.decompile(
										file.getBuffer().readByteRange(offset + DexHelper.CODE_ITEM_SIZE, size), file, ConfigManager.get().getConfig())
										.getInstructions();

								// Grab control flow graph
								cfg = analyser.extractCFG(instructions, 0);
							}
						}
					} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
							| IllegalAccessException | IndexOutOfBoundsException | ExceptionWithContext ignored) {}

					clsMethods.add(new DexMethod(this, method, cfg));
				}

				return clsMethods;
			}));
		}

		// Gather all results
		for (final Future<List<@NonNull DexMethod>> result : results) {
			try {
				methods.addAll(result.get());
			} catch (InterruptedException | ExecutionException ignored) {
			}
		}

		return methods.build();
	}
}