package com.topper.dex.decompilation;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Format;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

/**
 * Helper for managing tasks that work on internal
 * representations of dex bytecode, but are not
 * needed to perform those tasks.
 * 
 * E.g. it provides a way to convert a list of
 * instructions to a human - readable string.
 * 
 * @author Pascal Kühnemann
 * @since 15.08.2023
 * */
public final class DexHelper {

	
	@SuppressWarnings("null")	// builder.toString is not expected to return null...
	@NonNull
	public static final String instructionsToString(@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions) {
		
		// Determine largest instruction that is neither array payload nor any switch
		int max = 0;
		for (final DecompiledInstruction instruction : instructions) {

			if (max < instruction.getByteCode().length) {
				if (instruction.getInstruction().getOpcode().format != Format.ArrayPayload
						&& instruction.getInstruction().getOpcode().format != Format.PackedSwitchPayload
						&& instruction.getInstruction().getOpcode().format != Format.SparseSwitchPayload) {
					max = instruction.getByteCode().length;
				}
			}
		}

		// Add instructions to output
		final StringBuilder builder = new StringBuilder();
		int padding;
		int i;
		int offset = 0;
		for (final DecompiledInstruction instruction : instructions) {

			builder.append(String.format("%04x: ", offset));
			offset += instruction.getByteCode().length;

			if (instruction.getInstruction().getOpcode().format != Format.ArrayPayload
					&& instruction.getInstruction().getOpcode().format != Format.PackedSwitchPayload
					&& instruction.getInstruction().getOpcode().format != Format.SparseSwitchPayload) {

				// Format: <hex bytecode> <padding> <instruction string>
				for (final byte b : instruction.getByteCode()) {
					builder.append(String.format("%02x", b));
					builder.append(' ');
				}

				// Determine number of whitespace to use for padding
				// For each byte, two chars are needed for hex, one for space.
				padding = (max - instruction.getByteCode().length) * 3;

				for (i = 0; i < padding; i++) {
					builder.append(' ');
				}
			}

			// Finally append instruction string
			builder.append(instruction.getInstructionString());
			builder.append(System.lineSeparator());
		}

		return builder.toString();
	}
}