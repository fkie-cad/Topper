package com.topper.dex.decompilation.sweeper;

import java.util.LinkedList;
import java.util.List;

import org.jf.dexlib2.Format;
import org.jf.dexlib2.dexbacked.DexBuffer;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.ConfigManager;
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
	
	private static final int CODE_ITEM_SIZE = 2;

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
	public ImmutableList<ImmutableList<DecompiledInstruction>> sweep(final byte[] buffer, final int offset) {
		
		// TODO: Perform thorough tests! This must work correctly and efficiently!
		
		// Create decompiler
		final SmaliDecompiler decompiler = new SmaliDecompiler();
		
		// Extract upper - bound on number of instructions to find
		// from config. This includes the pivot instruction.
		final int maxNumberInstructions = ConfigManager.getInstance().getConfig().getSweeperMaxNumberInstructions();
		
		// If only a single instruction is requested, sweeping is obsolete.
		if (maxNumberInstructions == 1) {
			return decompiler.decompile(buffer.readByteRange(offset, ConfigManager.getInstance().getConfig().getPivotInstruction().format.size)).getInstructions();
		}
		
		// Need at least one additional instruction that precedes the
		// instruction pointed to by offset.
		
		// Either buffer is exhausted or maximum number of instructions is reached.
		final LinkedList<DecompiledInstruction> instructions = new LinkedList<DecompiledInstruction>();
		int i = 1;
		int size = CODE_ITEM_SIZE * i;
		int currentOffset = offset;
		DecompilationResult result;
		while (currentOffset - size >= 0 && instructions.size() < maxNumberInstructions) {
			
			// Grab next candidate
			try {
				result = decompiler.decompile(buffer.readByteRange(currentOffset - size, size));
				
				// If given byte region contains another or zero instructions, or
				// this one instruction is not adjacent to the previous instruction,
				// then the gadget cannot be any bigger without being invalidated.
				if (result.getInstructions().size() != 1 || result.getInstructions().get(0).getByteCode().length != size) {
					return ImmutableList.copyOf(instructions);
				}
				
				// Otherwise a valid instruction has been found.
				instructions.push(result.getInstructions().get(0));
				
				// Move currentOffset to the start of the newly found
				// instruction and reset i
				currentOffset -= size;
				i = 1;
				
			} catch (final Exception e) {
				// If something went wrong, it is because of a decompilation error
				// TODO: Only catch decompilation exception!
				i += 1;
			}
			
			// Update size
			size = CODE_ITEM_SIZE * i;
		}
		
		return ImmutableList.copyOf(instructions);
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
	 * 	a gadget.
	 * */
	@SuppressWarnings("unused")
	private final List<DecompiledInstruction> recursiveSweepImpl(
			final DexBuffer buffer,
			final int offset,
			final int currentSize,
			final List<Integer> checkedGadgetSizes,
			final int depth) {
		
		// Extract next instruction(s) and recursively
		// continue extraction until enough instructions
		// were found.
		
		//if (depth)
		
		return null;
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