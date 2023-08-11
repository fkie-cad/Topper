package com.topper.dex.decompilation.decompiler;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.dexbacked.DexReader;
import org.jf.dexlib2.dexbacked.util.VariableSizeLookaheadIterator;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompiler.instructions.BufferedInstruction;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

/**
 * 
 * Decompiler for dex bytecode. It aims to solve the following problem:
 * <blockquote>Given an array of bytes, interpret these bytes as dex bytecode
 * and decompile them into Smali.</blockquote> This is a greedy decompiler, i.e.
 * it continues decompiling bytes into instructions until an error occurs, or
 * there are no more bytes available.
 * 
 * Its implementation is based on <a href=
 * "https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/DexBackedMethodImplementation.java;l=76;drc=e6b4ff2c19b7138f9db078234049194ce663d5b2">AOSP's
 * dexlib2</a>
 * 
 * @author Pascal Kühnemann
 */
public final class SmaliDecompiler implements Decompiler {

	/**
	 * Decompiles a given byte array into smali instructions.
	 * 
	 * @param bytecode Byte array to interpret as bytecode and to decompile.
	 * @return Wrapper holding information on the decompilation. Among other things,
	 *         it holds the decompiled instructions.
	 * @throws IndexOutOfBoundsException If an instruction requires an out - of -
	 *                                   bounds read.
	 */
//	@SuppressWarnings("null") // endOfData() returns null, but this is accounted for in for-each
	@NonNull
	@Override
	public final DecompilationResult decompile(final byte @NonNull [] bytecode,
			@Nullable final DexBackedDexFile augmentation, @NonNull final Opcodes opcodes,
			final boolean nopUnknownInstruction) {

		if ((bytecode.length % 2) != 0) {
			throw new IllegalArgumentException("bytecode buffer must contain an even amount of bytes.");
		}

		final DexBuffer buffer = new DexBuffer(bytecode);

		int offset = 0;
		int size;
		final List<@NonNull DecompiledInstruction> decompiledInstructions = new LinkedList<DecompiledInstruction>();
		byte[] buf;
		for (final @NonNull BufferedInstruction instruction : this.getInstructions(buffer, augmentation, opcodes,
				nopUnknownInstruction)) {

			size = instruction.getCodeUnits() * 2;
			buf = new byte[size];
			System.arraycopy(bytecode, offset, buf, 0, size);
			decompiledInstructions.add(new DecompiledInstruction(instruction, buf));

			offset += size;
		}

		return new DecompilationResult(buffer, ImmutableList.copyOf(decompiledInstructions));
	}

	/**
	 * Retrieves instructions from a given <code>buffer</code>.
	 * 
	 * @param buffer Byte buffer, from which to extract smali instructions.
	 * @return List of extracted instructions.
	 */
	@NonNull
	private final Iterable<? extends BufferedInstruction> getInstructions(@NonNull final DexBuffer buffer,
			@Nullable final DexBackedDexFile file, @NonNull final Opcodes opcodes,
			final boolean nopUnknownInstruction) {
		// instructionsSize is the number of 16-bit code units in the instruction list,
		// not the number of instructions
		final int instructionsStartOffset = 0; // codeOffset + CodeItem.INSTRUCTION_START_OFFSET;
		final int endOffset = instructionsStartOffset + buffer.getBuf().length;

		return new Iterable<BufferedInstruction>() {
			@Override
			public Iterator<BufferedInstruction> iterator() {
				return new VariableSizeLookaheadIterator<BufferedInstruction>(buffer, instructionsStartOffset) {
					@Override
					protected BufferedInstruction readNextItem(@SuppressWarnings("rawtypes") final DexReader reader) {
						if (reader.getOffset() >= endOffset) {
							return endOfData();
						}

						final BufferedInstruction instruction = BufferedInstruction.readFrom(reader, file, opcodes,
								nopUnknownInstruction);

						// Does the instruction extend past the end of the method?
						final int offset = reader.getOffset();
						if (offset > endOffset || offset < 0) {
							return endOfData();
							// throw new ExceptionWithContext("The last instruction is truncated");
						}
						return instruction;
					}
				};
			}
		};
	}
}