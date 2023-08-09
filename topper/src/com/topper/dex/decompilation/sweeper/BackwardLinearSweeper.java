package com.topper.dex.decompilation.sweeper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Format;
import org.jf.util.ExceptionWithContext;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.TopperConfig;
import com.topper.dex.decompilation.decompiler.DecompilationResult;
import com.topper.dex.decompilation.decompiler.Decompiler;
import com.topper.dex.decompilation.pipeline.PipelineArgs;
import com.topper.dex.decompilation.pipeline.StageInfo;
import com.topper.dex.decompilation.pipeline.SweeperInfo;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;
import com.topper.exceptions.SweeperException;

/**
 * Decompiler built on top of <code>SmaliDecompiler</code>, which performs a
 * linear sweep, only backwards. I.e. this decompiler tries to solve the
 * following problem: <blockquote>Given a valid starting instruction, obtain at
 * most the first N instructions that precede the given
 * instruction.</blockquote>
 * 
 * @author Pascal Kühnemann
 */
public class BackwardLinearSweeper<@NonNull T extends Map<@NonNull String, @NonNull StageInfo>> extends Sweeper<T> {

	private static final int CODE_UNIT_SIZE = 2;

	/**
	 * Performs a linear backward sweep on <code>buffer</code>.
	 * 
	 * It does <b>not</b> verify that <code>offset</code> is even, i.e. a multiple
	 * of the code unit size, because the <code>buffer</code> could be a partial
	 * view of a larger buffer. If <code>buffer</code> started at an odd offset,
	 * then having an odd <code>offset</code> would be valid wrt. the larger buffer.
	 * There is no way to tell whether <code>offset</code> is correct from inside
	 * this code.
	 * 
	 * The general approach is to abuse the fact that instructions are always a
	 * multiple of two in size. Therefore, if an instruction precedes the one
	 * pointed to by <code>offset</code>, then its opcode byte must be located
	 * somewhere at offset - 2 * i. Then, try to decompile all instructions located
	 * at offset - 2 * 1, offset - 2 * 2 etc. until one matches and it directly
	 * adjacent to the pivot instruction. This is repeated over and over until
	 * either the buffer is exhausted, or the upper bound on the number of
	 * instructions is hit.
	 * 
	 * @param buffer Buffer, from which to extract instructions.
	 * @param offset Starting point of the sweep relative to the beginning of the
	 *               buffer. It must point to a valid instruction. It should point
	 *               to a pivot instruction.
	 * @return List of instruction sequences preceding the instruction located at
	 *         <code>offset</code> in <code>buffer</code>. The instruction pointed
	 *         to by <code>offset</code> is also part of this list.
	 * @throws SweeperException If <code>offset</code> does not point to a pivot
	 *                          instruction, or is out of bounds wrt.
	 *                          <code>buffer</code>.
	 */
	@Override
	public final T execute(@NonNull final T results) throws SweeperException {

		final PipelineArgs args = (PipelineArgs) results.get(PipelineArgs.class.getSimpleName());
		final int offset = args.getOffset();
		final byte[] buffer = args.getBuffer();
		final TopperConfig config = args.getConfig();

		// Perform bound checks on buffer and offset.
		final int currentSize = config.getPivotInstruction().format.size;
		if (offset + currentSize > buffer.length) {
			throw new SweeperException("buffer is too small to hold pivot instruction at " + offset + ".");
		} else if (offset < 0) {
			throw new SweeperException("offset must not be negative.");
		}

		final Decompiler decompiler = this.getDecompiler();
		final int maxSizes = config.getSweeperMaxNumberInstructions();
		final List<Integer> checkedGadgetSizes = new ArrayList<Integer>(maxSizes);
		checkedGadgetSizes.add(currentSize);
		final int depth = 1;

		// Try to decompile pivot instruction pointed to by offset.
		try {
			final DecompilationResult result = decompiler
					.decompile(Arrays.copyOfRange(buffer, offset, offset + currentSize), null);
			final ImmutableList<DecompiledInstruction> instructions = result.getInstructions();
			final DecompiledInstruction instruction = instructions.get(0);

			// Thoroughly check pivot instruction, because its format is known in advance.
			if (instructions.size() != 1 || instruction.getByteCode().length != currentSize
					|| !instruction.getInstruction().getOpcode().equals(config.getPivotInstruction())) {
				throw new SweeperException("Pivot instruction (" + instruction.getInstructionString() + " at offset "
						+ offset + " is invalid.");
			}

			// Use pivot instruction as base for recursive sweep.
			final ImmutableList<@NonNull ImmutableList<@NonNull DecompiledInstruction>> sequences = this
					.recursiveSweepImpl(decompiler, buffer, offset, currentSize, instructions, checkedGadgetSizes,
							depth, config);
			results.put(SweeperInfo.class.getSimpleName(), new SweeperInfo(sequences));
			return results;

		} catch (final ExceptionWithContext | ArrayIndexOutOfBoundsException e) {
			throw new SweeperException("Failed to decompile pivot instruction.");
		}
	}

	/**
	 * Recursive algorithm to extract instructions starting at a given
	 * <code>offset</code> from <code>buffer</code>. The recursion stops in either
	 * of the following cases: 1. <code>depth</code> exceeds the configured upper
	 * bound on the number of instructions allowed in a gadget. 2. Regardless of
	 * what gadget size is used, all result in decompilation errors.
	 * 
	 * In a particular call, assuming more than 1 instruction is requested, this
	 * method tests all possible instruction candidates. This is achieved by
	 * iterating over even instruction sizes and subtracting these sizes from the
	 * current <code>offset</code>. The resulting offset will be interpreted as a
	 * new instruction base and decompiled, if all optimisation checks are passed.
	 * If decompilation succeeds and the instruction is valid, then it will be added
	 * to the total result. An instruction is considered valid, iff. the decompiler
	 * produces only this single instruction and the instruction size matches the
	 * size of the current iteration.
	 * 
	 * The <code>checkedGadgetSizes</code> is used to keep track of what gadget
	 * sizes have been checked so far. E.g. it should be impossible to first find a
	 * 4 - byte instruction followed by a 6 - byte instruction, if this algorithm
	 * already found a 6 - byte instruction followed by a 4 - byte instruction. If
	 * this was possible, then the resulting gadgets would both start at the same
	 * offset within <code>buffer</code>, but their starting opcodes would have to
	 * differ. Assuming that <code>buffer</code> remains unmodified, this is a
	 * contradiction.
	 * 
	 * @param buffer               Buffer, in which to search for gadgets.
	 * @param offset               Starting point relative to the beginning of
	 *                             <code>buffer</code>. Initially it refers to the
	 *                             pivot instruction that signals the end of a
	 *                             gadget.
	 * @param currentSize          Sum of the lengths of all previously observed
	 *                             instructions before reaching this call level.
	 *                             This enables computing a total size of the gadget
	 *                             by adding the size of the next valid instruction.
	 *                             The total size can be used with
	 *                             <code>checkGadgetSizes</code>.
	 * @param previousInstructions List of previously observed instructions, before
	 *                             reaching this call level. Initialized to the
	 *                             pivot instruction determined by
	 *                             <code>TopperConfig</code>.
	 * @param checkedGadgetSizes   List of sums of instruction sizes already
	 *                             checked. As it is impossible to find two gadgets
	 *                             with the same starting point, i.e. the same size,
	 *                             further decompilations can be avoided.
	 * @param depth                Depth of the recursion used to compare against
	 *                             the upper bound on the number of instructions
	 *                             allowed in a gadget. Initially 1, because it
	 *                             includes the pivot instruction.
	 * @param config               Configuration to be used by this sweeper.
	 * @return List of instruction sequences. In case the upper bound on the
	 *         instructions is 1, only the pivot instruction is returned.
	 */
	@SuppressWarnings("null") // ImmutableList.Builder.build() is not expected to be null...
	@NonNull
	private final ImmutableList<@NonNull ImmutableList<@NonNull DecompiledInstruction>> recursiveSweepImpl(
			@NonNull final Decompiler decompiler, final byte @NonNull [] buffer, final int offset,
			final int currentSize, @NonNull final List<DecompiledInstruction> previousInstructions,
			@NonNull final List<Integer> checkedGadgetSizes, final int depth, @NonNull final TopperConfig config) {

		final ImmutableList.Builder<ImmutableList<DecompiledInstruction>> sequences = new ImmutableList.Builder<ImmutableList<DecompiledInstruction>>();

		// If this is the first call, then the first instruction must be added.
		if (depth == 1) {
			sequences.add(ImmutableList.copyOf(previousInstructions));
		}

		// Check if maximum number of instructions is reached.
		if (depth >= config.getSweeperMaxNumberInstructions()) {
			return sequences.build();
		}

		// Check buffer bounds. offset = buffer.length is explicitly
		// allowed, because it represents the starting point of the
		// next (sub)sweep. Further checks on offset prevent invalid
		// indices.
		if (offset < 0 || offset > buffer.length) {
			return sequences.build();
		}

		// Path on this recursion level requires at most as many
		// instructions as the maximum allowed instructions minus
		// the current depth.
		final ArrayList<DecompiledInstruction> path = new ArrayList<DecompiledInstruction>(
				config.getSweeperMaxNumberInstructions() - (depth - 1));

		int instructionSize;
		int totalSize;
		ImmutableList<DecompiledInstruction> instructions;
		for (int i = 1; i <= this.getMaxInstructionSize() / CODE_UNIT_SIZE; i++) {

			instructionSize = CODE_UNIT_SIZE * i;
			totalSize = currentSize + instructionSize;

			// Perform bounds check. If it fails, any future iteration will as well.
			if (offset - instructionSize < 0) {
				break;
			}

			// Skip checked size.
			if (checkedGadgetSizes.contains(totalSize)) {
				continue;
			}

			try {

				// Decompile instruction.
				instructions = decompiler.decompile(Arrays.copyOfRange(buffer, offset - instructionSize, offset), null)
						.getInstructions();

				// Check instructions. If invalid, then this instruction can be ignored/is not
				// valid.
				if (instructions.size() != 1 || instructions.get(0).getByteCode().length != instructionSize) {
					continue;
				}

				// Because decompilation was successful, no other
				// combination of instructions with the same totalSize
				// starting at the same offset can be valid.
				checkedGadgetSizes.add(totalSize);

				// Construct instruction sequence from previously observed instructions,
				// and the decompiled instruction.
				path.clear();
				path.addAll(previousInstructions);
				path.addAll(instructions);

				// As this is a new sequence of instructions, add it to total result.
				sequences.add(ImmutableList.copyOf(path));

				// Compute instruction paths that follow the decompiled instruction.
				// Add resulting paths to this path.
				sequences.addAll(recursiveSweepImpl(decompiler, Arrays.copyOfRange(buffer, 0, offset - instructionSize),
						offset - instructionSize, // offset points behind last byte
						totalSize, path, checkedGadgetSizes, depth + 1, config));

			} catch (final ExceptionWithContext | ArrayIndexOutOfBoundsException e) {
			}
		}

		return sequences.build();
	}

	/**
	 * Computes the largest amount of bytes required to encode a non - payload
	 * opcode. The size of payload instructions in the <code>Format</code> enum is
	 * set to <code>-1</code> and can therefore not be a valid maximum.
	 * 
	 * @return Larget amount of bytes required to encode a non-payload instruction.
	 */
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