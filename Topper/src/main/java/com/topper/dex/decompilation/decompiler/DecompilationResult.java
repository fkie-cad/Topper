package com.topper.dex.decompilation.decompiler;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.dexbacked.DexBuffer;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.DexHelper;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

/**
 * Output of a {@link Decompiler} after decompilation succeeded. It wraps, among
 * other things, decompiled instructions.
 * 
 * @author Pascal Kühnemann
 * @since 11.08.2023
 */
public class DecompilationResult {

	/**
	 * Underlying buffer of decompiled instructions.
	 */
	@NonNull
	private final DexBuffer buffer;

	/**
	 * List of decompiled instructions taken from {@code buffer}.
	 */
	@NonNull
	private final ImmutableList<@NonNull DecompiledInstruction> instructions;

	public DecompilationResult(@NonNull final DexBuffer buffer,
			@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions) {
		this.buffer = buffer;
		this.instructions = instructions;
	}

	/**
	 * Gets the buffer that was used to extract the decompiled instructions.
	 */
	@NonNull
	public final DexBuffer getBuffer() {
		return this.buffer;
	}

	/**
	 * Gets the list of decompiled instructions taken from an underlying buffer.
	 */
	@NonNull
	public final ImmutableList<@NonNull DecompiledInstruction> getInstructions() {
		return this.instructions;
	}

	/**
	 * Converts the list of decompiled instructions to a human - readable
	 * instruction dump.
	 */
	@NonNull
	public final String getPrettyInstructions() {
		return DexHelper.instructionsToString(this.instructions);
	}
}