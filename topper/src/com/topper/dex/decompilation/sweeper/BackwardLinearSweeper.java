package com.topper.dex.decompilation.sweeper;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Format;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.ConfigManager;
import com.topper.configuration.TopperConfig;
import com.topper.dex.decompilation.DecompilationResult;
import com.topper.dex.decompilation.SmaliDecompiler;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

/**
 * Decompiler built on top of <code>SmaliDecompiler</code>, which
 * performs a linear sweep, only backwards. I.e. this decompiler
 * tries to solve the following problem:
 * <blockquote>Given a valid starting instruction, obtain at most the first N instructions that precede the given instruction.</blockquote>
 * 
 * @author Pascal Kühnemann
 * */
public class BackwardLinearSweeper implements Sweeper {
	
	private static final int CODE_UNIT_SIZE = 2;

	/**
	 * Performs a linear backward sweep on <code>buffer</code>.
	 * 
	 * It does <b>not</b> verify that <code>offset</code> is even,
	 * i.e. a multiple of the code unit size, because the <code>buffer</code>
	 * could be a partial view of a larger buffer. If <code>buffer</code>
	 * started at an odd offset, then having an odd <code>offset</code>
	 * would be valid wrt. the larger buffer. There is no way to tell
	 * whether <code>offset</code> is correct from inside this code.
	 * 
	 * The general approach is to abuse the fact that instructions
	 * are always a multiple of two in size. Therefore, if an
	 * instruction precedes the one pointed to by <code>offset</code>,
	 * then its opcode byte must be located somewhere at offset - 2 * i.
	 * Then, try to decompile all instructions located at offset - 2 * 1,
	 * offset - 2 * 2 etc. until one matches and it directly adjacent
	 * to the pivot instruction. This is repeated over and over until
	 * either the buffer is exhausted, or the upper bound on the
	 * number of instructions is hit.
	 * 
	 * @param buffer Buffer, from which to extract instructions.
	 * @param offset Starting point of the sweep relative to the beginning of the
	 * 	buffer. It must point to a valid instruction.
	 * @return List of lists of decompiled instructions preceding the instruction
	 * 	located at <code>offset</code> in <code>buffer</code>. The instruction
	 * 	pointer to by <code>offset</code> is also part of this list.
	 * */
	@Override
	public ImmutableList<@NonNull ImmutableList<@NonNull DecompiledInstruction>> sweep(final byte[] buffer, final int offset) {
		
		final SmaliDecompiler decompiler = new SmaliDecompiler();
		@SuppressWarnings("null")	// ByteBuffer.wrap should not return null...
		@NonNull final ByteBuffer wrappedBuffer = ByteBuffer.wrap(buffer);
		final int currentSize = ConfigManager.getInstance().getConfig().getPivotInstruction().format.size;
		final int maxSizes = ConfigManager.getInstance().getConfig().getSweeperMaxNumberInstructions();
		final List<Integer> checkedGadgetSizes = new ArrayList<Integer>(maxSizes);
		checkedGadgetSizes.add(currentSize);
		final int depth = 1;
		
		return this.recursiveSweepImpl(decompiler, wrappedBuffer, offset, currentSize, checkedGadgetSizes, depth);
	}
	
	/**
	 * Recursive algorithm to extract instructions starting at a given
	 * <code>offset</code> from <code>buffer</code>. The recursion
	 * stops in either of the following cases:
	 * 1. <code>depth</code> exceeds the configured upper bound on
	 * 	the number of instructions allowed in a gadget.
	 * 2. Regardless of what gadget size is used, all result in
	 * 	decompilation errors.
	 * 
	 * The <code>checkedGadgetSizes</code> is used to keep track of
	 * what gadget sizes have been checked so far. E.g. it must be
	 * impossible to first find a 4 - byte instruction followed by
	 * a 6 - byte instruction, if this algorithm already finds a
	 * 6 - byte instruction followed by a 4 - byte instruction. If
	 * this was possible, then the resulting gadgets would both
	 * start at the same offset within <code>buffer</code>, but their
	 * starting opcodes would have to differ. Assuming that <code>buffer</code>
	 * remains unmodified, this is a contradiction.
	 * 
	 * @param buffer Buffer, in which to search for gadgets.
	 * @param offset Starting point relative to the beginning of
	 * 	<code>buffer</code> determining the pivot instruction
	 * 	that signals the end of a gadget.
	 * @param checkedGadgetSizes List of sums of instruction sizes
	 * 	already checked. As it is impossible to find two gadgets
	 * 	with the same starting point, i.e. the same size, further
	 * 	decompilations can be avoided.
	 * @param depth Depth of the recursion used to compare against
	 * 	the upper bound on the number of instructions allowed in
	 * 	a gadget. Initially 1, because it includes the pivot instruction.
	 * */
	@SuppressWarnings("null")	// ImmutableList.of() or ByteBuffer.slice are not expected to be null...
	@NonNull
	private final ImmutableList<@NonNull ImmutableList<@NonNull DecompiledInstruction>> recursiveSweepImpl(
			@NonNull final SmaliDecompiler decompiler,
			@NonNull final ByteBuffer buffer,
			final int offset,
			final int currentSize,
			@NonNull final List<Integer> checkedGadgetSizes,
			final int depth) {
		
		// Extract next instruction(s) and recursively
		// continue extraction until enough instructions
		// were found.
		
		// Check if maximum number of instructions is reached.
		final TopperConfig config = ConfigManager.getInstance().getConfig();
		if (depth == config.getSweeperMaxNumberInstructions()) {
			return ImmutableList.of();
		}
		
		// Check buffer bounds.
		if (offset < 0 || offset + currentSize - 1 >= buffer.flip().remaining()) {
			return ImmutableList.of();
		}
		
		// Currently all instructions take up currentSize
		// bytes (initially ConfigManager.getInstance().getConfig().getPivotInstruction().format.size,
		// i.e. probably 2...). As this sweeper moves towards
		// smaller offsets, candidate instructions may be located
		// at offset - 2 * i for i=1..5 (compute from ...format.size).
		// If currentSize + 2 * i is in checkedGadgetSizes, then
		// the i must not be checked, because it will be invalid.
		//List<ImmutableList<DecompiledInstruction>> sequences = new LinkedList<ImmutableList<DecompiledInstruction>>();
		
		ImmutableList.Builder<ImmutableList<DecompiledInstruction>> sequences
			= new ImmutableList.Builder<ImmutableList<DecompiledInstruction>>();
		
		int instructionSize;
		int totalSize;
		DecompilationResult result;
		ImmutableList<DecompiledInstruction> instructions;
		for (int i = 0; i < this.getMaxInstructionSize() / CODE_UNIT_SIZE; i++) {
			
			instructionSize = CODE_UNIT_SIZE * i;
			totalSize = currentSize + instructionSize;
			
			// Perform bounds check. If it fails, any future iteration will as well.
			if (offset - instructionSize < 0) {
				return ImmutableList.of();
			}
			
			// Skip checked size.
			if (checkedGadgetSizes.contains(totalSize)) {
				continue;
			}
			
			// Decompile instruction.
			try {
				
				// Using a ByteBuffer to avoid unnecessary copying of the buffer.
				result = decompiler.decompile(buffer.slice(offset - instructionSize, instructionSize).array());
				instructions = result.getInstructions();
				
				// Check instructions. If invalid, then this instruction size can be ignored/is not valid.
				if (instructions.size() == 1 && instructions.get(0).getByteCode().length == instructionSize) {
					
					// Because decompilation was successful, no other
					// combination of instructions with the same totalSize
					// starting at the same offset can be valid
					checkedGadgetSizes.add(totalSize);
					
					// Good instructions can be used as base for next instruction
					sequences.addAll(recursiveSweepImpl(
							decompiler, 
							buffer.slice(0, offset - instructionSize),
							offset - instructionSize,
							totalSize,
							checkedGadgetSizes,
							depth + 1
					));
				}
				
				
			} catch (final Exception e) {}	// TODO: Check for decompilation - specific exceptions
		}
		
		return sequences.build();
	}
	
	/**
	 * Computes the largest amount of bytes required to encode a
	 * non - payload opcode. The size of payload instructions
	 * in the <code>Format</code> enum is set to <code>-1</code>
	 * and can therefore not be a valid maximum.
	 * 
	 * @return Larget amount of bytes required to encode a non-payload
	 * 	instruction.
	 * */
	private final int getMaxInstructionSize() {
		
		int max = Format.values()[0].size;
		
		for (int i = 1; i < Format.values().length; i++) {
			if (max < Format.values()[i].size) {
				max = Format.values()[i].size;
			}
		}
		
		return max;
	}
}