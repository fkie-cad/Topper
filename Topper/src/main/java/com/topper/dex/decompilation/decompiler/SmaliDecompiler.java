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
import org.jf.util.ExceptionWithContext;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.TopperConfig;
import com.topper.dex.decompiler.instructions.BufferedInstruction;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

/**
 * 
 * Decompiler for dex bytecode. It aims to solve the following problem:
 * <blockquote>Given an array of bytes, interpret these bytes as dex bytecode
 * and decompile them into Smali.</blockquote> This is a greedy decompiler, i.e.
 * it continues decompiling bytes into instructions until an error occurs, or
 * there are no more bytes available. If an error occurs, it will discard the
 * results and throw an error, or nop out invalid instructions.
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
	 * @param bytecode     Byte array to interpret as bytecode and to decompile.
	 * @param augmentation Dex file representation to use for resolving references.
	 *                     This can be used to view instruction in different
	 *                     execution contexts.
	 * @param config       Configuration to use during decompilation.
	 * @return Wrapper holding information on the decompilation. Among other things,
	 *         it holds the decompiled instructions.
	 * @throws IndexOutOfBoundsException If an instruction requires an out - of -
	 *                                   bounds read.
	 * @throws ExceptionWithContext      If an unknown instruction is met, or an
	 *                                   internal logic error occurs like too large
	 *                                   integer values for reference indices.
	 * @throws IllegalArgumentException  If the buffer length is not a multiple of
	 *                                   two.
	 */
	@NonNull
	@Override
	public final DecompilationResult decompile(final byte @NonNull [] bytecode,
			@Nullable final DexBackedDexFile augmentation, @NonNull final TopperConfig config)
			throws IndexOutOfBoundsException, ExceptionWithContext {

		if ((bytecode.length % 2) != 0) {
			throw new IllegalArgumentException("bytecode buffer must contain an even amount of bytes.");
		}

		final DexBuffer buffer = new DexBuffer(bytecode);

		int offset = 0;
		int size;
		final List<@NonNull DecompiledInstruction> decompiledInstructions = new LinkedList<DecompiledInstruction>();
		byte[] buf;
		for (final @NonNull BufferedInstruction instruction : this.getInstructions(buffer, augmentation,
				config.getDecompilerConfig().getOpcodes(),
				config.getDecompilerConfig().shouldNopUnknownInstruction())) {

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
	 * @param buffer                Byte buffer, from which to extract smali
	 *                              instructions.
	 * @param file                  Dex file representation to use for resolving
	 *                              references. This can be used to view instruction
	 *                              in different execution contexts.
	 * @param opcodes               Set of opcodes to use for decompilation. This is
	 *                              application-specific.
	 * @param nopUnknownInstruction Indicates how unknown instruction must be
	 *                              handled. Either an unknown instruction is nop`ed
	 *                              out ({@code true}), or an exception is thrown
	 *                              and all decompilation results discarded
	 *                              ({@code false}).
	 * @return List of extracted instructions.
	 * @throws IndexOutOfBoundsException If an instruction requires an out - of -
	 *                                   bounds read.
	 * @throws ExceptionWithContext      If an unknown instruction is met, or an
	 *                                   internal logic error occurs like too large
	 *                                   integer values for reference indices.
	 */
	@NonNull
	private final Iterable<? extends BufferedInstruction> getInstructions(@NonNull final DexBuffer buffer,
			@Nullable final DexBackedDexFile file, @NonNull final Opcodes opcodes, final boolean nopUnknownInstruction)
			throws IndexOutOfBoundsException, ExceptionWithContext {
		final int instructionsStartOffset = 0;
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