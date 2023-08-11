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
	 * If {@code augmentation} is not {@code null}, then it must be used for
	 * resolving references in order to augment analysis with type names etc.
	 * 
	 * @param bytes                 Raw bytes to decompile into dex instructions.
	 * @param augmentation          Dex file representation to use for resolving
	 *                              references. This can be used to view instruction
	 *                              in different execution contexts.
	 * @param opcodes               Set of opcodes to use for decompilation. This is
	 *                              application-specific.
	 * @param nopUnknownInstruction Indicates how unknown instruction must be
	 *                              handled. Either an unknown instruction is nop`ed
	 *                              out ({@code true}), or an exception is thrown
	 *                              and all decompilation results discarded
	 *                              ({@code false}).
	 * @return Result wrapping decompiled instructions and further information.
	 */
	@NonNull
	DecompilationResult decompile(final byte @NonNull [] bytes, @Nullable final DexBackedDexFile augmentation,
			@NonNull final Opcodes opcodes, final boolean nopUnknownInstruction);
}