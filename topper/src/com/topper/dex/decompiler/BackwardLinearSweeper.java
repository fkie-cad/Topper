package com.topper.dex.decompiler;

import java.util.LinkedList;
import java.util.List;

import org.jf.dexlib2.dexbacked.DexBuffer;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.TopperConfig;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

/**
 * Decompiler built on top of <code>SmaliDecompiler</code>, which
 * performs a linear sweep, only backwards. I.e. this decompiler
 * tries to solve the following problem:
 * <blockquote>Given a valid starting instruction, obtain at most the first N instructions that precede the given instruction.</blockquote>
 * 
 * @author Pascal Kühnemann
 * */
public class BackwardLinearSweeper extends Sweeper {

	public BackwardLinearSweeper(final TopperConfig config) {
		super(config);
	}

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
	 * @return List of decompiled instructions preceding the instruction
	 * 	located at <code>offset</code> in <code>buffer</code>. The instruction
	 * 	pointer to by <code>offset</code> is also part of this list.
	 * @throws 
	 * */
	@Override
	public ImmutableList<DecompiledInstruction> sweep(final DexBuffer buffer, final int offset) {
		
		// TODO: Perform thorough tests! This must work correctly and efficiently!
		
		// Create decompiler
		final SmaliDecompiler decompiler = new SmaliDecompiler();
		
		// Extract upper - bound on number of instructions to find
		// from config. This includes the pivot instruction.
		final int maxNumberInstructions = this.getConfig().getSweeperMaxNumberInstructions();
		
		// If only a single instruction is requested, sweeping is obsolete.
		if (maxNumberInstructions == 1) {
			return decompiler.decompile(buffer.readByteRange(offset, this.getConfig().getPivotInstruction().format.size)).getInstructions();
		}
		
		// Need at least one additional instruction that precedes the
		// instruction pointed to by offset.
		
		// Either buffer is exhausted or maximum number of instructions is reached.
		final LinkedList<DecompiledInstruction> instructions = new LinkedList<DecompiledInstruction>();
		int i = 1;
		int size = Sweeper.CODE_ITEM_SIZE * i;
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
			size = Sweeper.CODE_ITEM_SIZE * i;
		}
		
		return ImmutableList.copyOf(instructions);
	}
}