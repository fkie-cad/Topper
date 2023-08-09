package com.topper.dex.decompilation.decompiler;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;

import com.topper.dex.decompiler.instructions.DecompiledInstruction;

/**
 * Decompiler interface for dex bytecode.
 * 
 * The default implementation is the {@link SmaliDecompiler}.
 * 
 * @author Pascal Kühnemann
 * @since 08.08.2023
 * @see SmaliDecompiler
 */
public interface Decompiler {

	/**
	 * Decompiles given {@code bytes} into {@link DecompiledInstruction}s wrapped in
	 * a {@link DecompilationResult}.
	 * 
	 * If {@code bytes} contains a valid .dex file, then it must be interpreted as a
	 * {@link DexBackedDexFile} to augment analysis with type names etc.
	 * 
	 * @param bytes        Raw bytes to decompile into dex instructions.
	 * @param augmentation Optional .dex file representation. This allows using type
	 *                     names and resolving references.
	 * @param opcodes Opcodes to use during decompilation.
	 * @return Result wrapping decompiled instructions and further information.
	 */
	@NonNull
	DecompilationResult decompile(final byte @NonNull [] bytes, @Nullable final DexBackedDexFile augmentation,
			@NonNull final Opcodes opcodes, final boolean nopUnknownInstruction);
}