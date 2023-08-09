package com.topper.dex.decompilation.decompiler;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.dexbacked.DexBuffer;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public class DecompilationResult {
	
	@NonNull
	private final DexBuffer buffer;
	
	@NonNull
	private final ImmutableList<@NonNull DecompiledInstruction> instructions;

	public DecompilationResult(@NonNull final DexBuffer buffer, @NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions) {
		this.buffer = buffer;
		this.instructions = instructions;
	}
	
	@NonNull
	public final DexBuffer getBuffer() {
		return this.buffer;
	}
	
	@NonNull
	public final ImmutableList<@NonNull DecompiledInstruction> getInstructions() {
		return this.instructions;
	}
	
	@NonNull
	public final String getPrettyInstructions() {
		
		// Determine largest instruction that is neither array payload nor any switch
		int max = 0;
		for (final DecompiledInstruction instruction : this.getInstructions()) {
			
			if (max < instruction.getByteCode().length) {
				if (instruction.getInstruction().getOpcode().format != Format.ArrayPayload &&
						instruction.getInstruction().getOpcode().format != Format.PackedSwitchPayload && 
						instruction.getInstruction().getOpcode().format != Format.SparseSwitchPayload) {
					max = instruction.getByteCode().length;
				}
			}
		}
		
		// Add instructions to output
		final StringBuilder builder = new StringBuilder();
		int padding;
		int i;
		int offset = 0;
		for (final DecompiledInstruction instruction : this.getInstructions()) {
			
			builder.append(String.format("%04x: ", offset));
			offset += instruction.getByteCode().length;
			
			if (instruction.getInstruction().getOpcode().format != Format.ArrayPayload &&
						instruction.getInstruction().getOpcode().format != Format.PackedSwitchPayload && 
						instruction.getInstruction().getOpcode().format != Format.SparseSwitchPayload) {
				
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