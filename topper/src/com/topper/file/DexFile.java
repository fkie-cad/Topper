package com.topper.file;

import java.io.File;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedMethod;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.decompiler.Decompiler;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompilation.staticanalyser.CFGAnalyser;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public class DexFile implements AugmentedFile {

	@NonNull
	private final File file;

	private final byte @NonNull [] buffer;

	@NonNull
	private final DexBackedDexFile dexFile;

	@NonNull
	private final ImmutableList<@NonNull DexMethod> methods;

	public DexFile(@NonNull final File file, final byte @NonNull [] buffer) {
		this(file, buffer, null, null);
	}

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

	@NonNull
	private final ImmutableList<@NonNull DexMethod> loadMethods(@NonNull final DexBackedDexFile file,
			@Nullable final Decompiler decompiler, @Nullable final CFGAnalyser analyser) {

		ImmutableList.Builder<@NonNull DexMethod> methods = new ImmutableList.Builder<>();
		ImmutableList<@NonNull DecompiledInstruction> instructions;
		int offset;
		int size;
		CFG cfg;

		// Iterate over methods
		for (final DexBackedClassDef cls : file.getClasses()) {

			if (cls == null) {
				continue;
			}

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
							instructions = decompiler
									.decompile(file.getBuffer().readByteRange(offset + DexHelper.CODE_ITEM_SIZE, size),
											file)
									.getInstructions();
							
							// Grab control flow graph
							cfg = analyser.extractCFG(instructions, 0);
						}
					}
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException
						| IndexOutOfBoundsException ignored) {}

				methods.add(new DexMethod(method, cfg));
			}
		}

		return methods.build();
	}
}