package com.topper.dex.decompilation.staticanalyser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.OffsetInstruction;
import org.jf.dexlib2.iface.instruction.SwitchPayload;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.graphs.BasicBlock;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public class BFSCFGAnalyser implements CFGAnalyser {

	@Override
	@NonNull
	public CFG extractCFG(@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions, final int entry) {

		if (entry < 0) {
			throw new IllegalArgumentException("entry must be non - negative.");
		} else if (instructions.isEmpty()) {
			throw new IllegalArgumentException("List of instructions must not be empty.");
		}

		final CFG cfg = new CFG(entry);

		// Setup offset -> instruction index lookup (O(log n) lookup using TreeMap)
		DecompiledInstruction current;
		final TreeMap<Integer, Integer> offsetToIndex = new TreeMap<Integer, Integer>();
		boolean entryValid = false;
		for (int i = 0; i < instructions.size(); i++) {
			current = (@NonNull DecompiledInstruction) instructions.get(i);
			cfg.addOffsetInstructionLookup(current.getOffset(), current);
			offsetToIndex.put(current.getOffset(), i);
			if (current.getOffset() == entry) {
				entryValid = true;
			}
		}

		if (!entryValid) {
			throw new IllegalArgumentException(
					"entry does not point to an instruction in the provided instruction list.");
		}

		// Handle special offset that is just beyond all instructions
		final DecompiledInstruction last = (@NonNull DecompiledInstruction) instructions.get(instructions.size() - 1);
		offsetToIndex.put(last.getOffset() + last.getByteCode().length, instructions.size());

		// Perform actual extraction
		this.extractCFGBFS(cfg, instructions, offsetToIndex, entry);

		// Finally add instruction -> block lookup.
		for (final BasicBlock block : cfg.getGraph().nodes()) {
			for (final DecompiledInstruction insn : block.getInstructions()) {
				cfg.addInstructionBlockLookup(insn, block);
			}
		}

		return cfg;
	}

	/**
	 * Assumptions: 1. <code>offset</code> points to a valid instruction.
	 */
	private final void extractCFGBFS(@NonNull final CFG cfg,
			@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions,
			@NonNull final TreeMap<Integer, Integer> offsetToIndex, final int offset) {

		// Set up BFS
		final BasicBlock startNode = this.findNextBlock(instructions, offsetToIndex.get(offset));
		cfg.getGraph().addNode(startNode);

		final Queue<@NonNull BasicBlock> todo = new LinkedBlockingQueue<@NonNull BasicBlock>();
		todo.add(startNode);

		final Map<Integer, BasicBlock> visitedNodes = new TreeMap<Integer, BasicBlock>();
		visitedNodes.put(offset, startNode);

		// Local variables used for on-the-fly graph creation
		BasicBlock current;
		DecompiledInstruction branch;
		OffsetInstruction insn;
		int target;
		List<Integer> branches;
		BasicBlock child;

		while (!todo.isEmpty()) {

			// Invariant: todo contains at least one element.
			current = (@NonNull BasicBlock) todo.poll();

			// Obtain branch instruction
			branch = current.getBranchInstruction();

			// Identify branch targets
			if (OffsetInstruction.class.isAssignableFrom(branch.getInstruction().getClass())) {

				insn = (OffsetInstruction) branch.getInstruction();
				target = insn.getCodeOffset() * 2 + branch.getOffset();

				// Determine branch targets and skip invalid branch instructions
				branches = this.extractBranchTargets(cfg.getOffsetInstructionLookup(), target, branch);
				if (branches.isEmpty()) {
					continue;
				}

				// Ensure branch targets refer to valid instructions
				// Construct basic block for each branch
				for (final DecompiledInstruction targetInstruction : this
						.stripBranches(cfg.getOffsetInstructionLookup(), branches)) {

					// If child is known basic block
					child = visitedNodes.get(targetInstruction.getOffset());
					if (child == null) {

						// Process it on next depth level
						child = this.addToGraph(cfg, instructions, offsetToIndex, targetInstruction.getOffset());
						if (child != null) {
							cfg.getGraph().putEdge(current, child);
							todo.add(child);
							visitedNodes.put(targetInstruction.getOffset(), child);
						}
					} else {

						// If targetInstruction belongs to existing block, then add edge
						cfg.getGraph().putEdge(current, child);
					}
				}
			}
		}

		// Eventually strip all dead nodes
		final List<@NonNull BasicBlock> unreachable = cfg.getGraph().nodes().stream()
				.filter(block -> cfg.getGraph().inDegree(block) == 0 && cfg.getGraph().outDegree(block) == 0)
				.collect(Collectors.toList());
		for (@NonNull
		final BasicBlock block : unreachable) {
			cfg.getGraph().removeNode(block);
		}
	}

	/**
	 * Add a new {@link BasicBlock} starting at {@code offset} to the {@link CFG}.
	 * As this is a helper method handling {@code BasicBlock} creation while
	 * traversing the {@code CFG}, three cases have to be considered when adding a
	 * new {@code BasicBlock}:
	 * <ul>
	 * <li>{@code offset} points into an existing {@code BasicBlock}: Split existing
	 * {@code BasicBlock} two blocks. The first block is the shortened existing
	 * {@code BasicBlock} with an outgoing edge to the new {@code BasicBlock}
	 * starting at {@code offset}. The new block takes over all outgoing edges of
	 * the old existing {@code BasicBlock}.</li>
	 * <li>{@code offset} precedes an existing {@code BasicBlock}: If the
	 * {@code BasicBlock} starting at {@code offset} overlaps with its immediate
	 * successor (wrt. offset) {@BasicBlock}, then the new block is shortened.
	 * Otherwise only the new block is added.</li>
	 * <li>{@code offset} succeeds an existing {@code BasicBlock}: Because the above
	 * two cases are checked before reaching this case, {@code offset} does not
	 * point into any existing {@code BasicBlock} and thus is only limited in size
	 * wrt. the total size given by {@code instructions}.</li>
	 * </ul>
	 * 
	 * Updating the {@code CFG} does not include adding the {@code BasicBlock}
	 * induced by {@code offset}. Adding the block must be done by the caller, as it
	 * knows what parent block refers to this new block.
	 * 
	 * @param cfg           The {@code CFG} to update wrt. the new
	 *                      {@code BasicBlock}.
	 * @param instructions  List of instructions linked to {@code CFG}. It contains
	 *                      all instructions used by the new {@code BasicBlock}.
	 * @param offsetToIndex Offset -> Index map to avoid having a O(#instructions)
	 *                      lookup. Notice that this mapping is required, as the
	 *                      function between index and offset of an instruction is
	 *                      not linear. (Possible to speed this up even more by
	 *                      approximating coefficients of a polynomial p s.t. for
	 *                      all offsets o of instructions at index i it holds that
	 *                      p(o) = i)
	 * @param offset        Offset into an underlying buffer containing the
	 *                      {@code instructions}.
	 * @return A new {@code BasicBlock} to be added to {@code CFG} by the caller and
	 *         an implicitly updated {@code CFG}; {@code null} in case that
	 *         {@code offset} refers to an existing {@code BasicBlock}.
	 */
	@Nullable
	private final BasicBlock addToGraph(@NonNull final CFG cfg,
			@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions,
			@NonNull final TreeMap<Integer, Integer> offsetToIndex, final int offset) {

		// Invariant: Basic blocks do not overlap.
		assert (!cfg.getGraph().nodes().stream().anyMatch(first -> {
			return cfg.getGraph().nodes().stream().anyMatch(second -> {
				if (first.equals(second)) {
					return false;
				}
				return (first.getOffset() <= second.getOffset()
						&& first.getOffset() + first.getSize() > second.getOffset())
						|| (second.getOffset() <= first.getOffset()
								&& second.getOffset() + second.getSize() > first.getOffset());
			});
		}));

		// Three cases:
		// 1. Offset points into an existing basic block -> split block
		// 2. Offset points above an existing basic block -> precede block
		// 3. Offset points beneath an existing basic block -> new block

		final int index = offsetToIndex.get(offset);
		BasicBlock child = null;

		// Indicates offset of next basic block coming after offset.
		// Therefore nextOffset > offset.
		int nextOffset = Integer.MAX_VALUE;
		BasicBlock nextBlock = null;

		// 1. offset points into existing basic block (unique). Equality
		// with existing basic block impossible, because this is just
		// the same basic block.
		final List<@NonNull Edge<@NonNull BasicBlock>> additions = new LinkedList<>();
		final List<@NonNull Edge<@NonNull BasicBlock>> deletions = new LinkedList<>();
		for (final BasicBlock block : cfg.getGraph().nodes()) {

			// Quickly check if offset points to existing block.
			if (block.getOffset() == offset) {
				return null; // no child
			}

			// block offset must be above offset, but still better than current
			// best nextOffset.
			if (block.getOffset() > offset && block.getOffset() < nextOffset) {
				nextOffset = block.getOffset();
				nextBlock = block;
			}

			// Found unique matching block. Like the containing block, offset
			// would induce a block that stops at least at the same instruction as block,
			// if not later.
			if (offset > block.getOffset() && offset < block.getOffset() + block.getSize()) {

				// Create new block starting at instruction,
				// which offset is pointing to
				child = new BasicBlock(
						instructions.subList(index, offsetToIndex.get(block.getOffset() + block.getSize())));

				// Set instructions of block to cover only instructions up to child.
				block.setInstructions(instructions.subList(offsetToIndex.get(block.getOffset()), index));

				// block must reference child, and child must take over the references
				// i.e. outgoing edges of block.
				additions.add(new Edge<@NonNull BasicBlock>(block, child));
				for (final BasicBlock reference : cfg.getGraph().successors(block)) {
					deletions.add(new Edge<@NonNull BasicBlock>(block, reference));
					additions.add(new Edge<@NonNull BasicBlock>(child, reference));
				}

				break; // unique
			}
		}

		// Check if case 1 occurred, i.e. if there is at
		// least an edge to add between block and child.
		if (additions.size() > 0) {

			for (final Edge<@NonNull BasicBlock> e : additions) {
				cfg.getGraph().putEdge(e.getSource(), e.getDestination());
			}

			for (final Edge<@NonNull BasicBlock> e : deletions) {
				cfg.getGraph().removeEdge(e.getSource(), e.getDestination());
			}

			return child;
		}

		// Invariant: offset does not point into existing basic block.
		assert (!cfg.getGraph().nodes().stream()
				.anyMatch(b -> b.getOffset() <= offset && b.getOffset() + b.getSize() > offset));

		// 2. offset points above existing basic block (visually above; unique).
		// As nextOffset refers to the next basic block located (visually) below
		// offset, a new basic block can be searched for. If they overlap, then just
		// create a basic block that immediately precedes the existing one
		if (nextOffset != Integer.MAX_VALUE && nextBlock != null) {

			// Find next block.
			child = this.findNextBlock(instructions, index);

			// If block overlaps with next block, child must be shortened.
			if (child.getOffset() + child.getSize() > nextOffset) {

				child.setInstructions(instructions.subList(index, offsetToIndex.get(nextOffset)));
				cfg.getGraph().putEdge(child, nextBlock);
			}

			// Otherwise there is no connection between child and next block.
			return child;
		}

		// Invariant: There does not exist a block with block.getOffset() >= offset.
		assert (!cfg.getGraph().nodes().stream().anyMatch(b -> b.getOffset() >= offset));

		// 3. offset points below existing basic block (visually below; unique).
		// As whatever block precedes offset stopped before the instruction
		// referenced by offset, a new block can be created without any
		// connections whatsoever, only limited in size.
		child = this.findNextBlock(instructions, index);

		return child;
	}

	/**
	 * Performs a forwards sweep in {@code instructions} until reaching a terminal
	 * instruction (branch instruction or last instruction). The sweep starts at a
	 * given {@code index}. The resulting {@link BasicBlock} contains all
	 * instructions starting from {@code index} until the first terminal
	 * instruction.
	 * 
	 * A branch instruction is any of the following:
	 * <ul>
	 * <li>if-*</li>
	 * <li>goto*</li>
	 * <li>packed-/sparse-switch</li>
	 * <li>throw</li>
	 * <li>return</li>
	 * </ul>
	 * 
	 * @param instructions List of instructions, from which to construct a
	 *                     {@code BasicBlock}.
	 * @param index        Start index referencing the start instruction of the new
	 *                     {@code BasicBlock}.
	 * @return A new {@code BasicBlock} starting at {@code index} wrt.
	 *         {@code instructions} and stopping at the first terminal instruction
	 *         (branch or last).
	 */
	@NonNull
	private final BasicBlock findNextBlock(@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions,
			final int index) {

		DecompiledInstruction instruction;
		Opcode opcode;
		for (int i = index; i < instructions.size(); i++) {

			instruction = instructions.get(i);
			opcode = instruction.getInstruction().getOpcode();

			switch (opcode.format) {
			case Format10t:
			case Format20t:
			case Format21t:
			case Format22t:
			case Format30t:
			case Format31t:
				// fill-array-data uses 31t format
				if (!instruction.getInstruction().getOpcode().equals(Opcode.FILL_ARRAY_DATA)) {
					return new BasicBlock(instructions.subList(index, i + 1));
				}
				break;
			default:
				if (StaticAnalyser.isReturn(opcode) || StaticAnalyser.isThrow(opcode)) {
					return new BasicBlock(instructions.subList(index, i + 1));
				}
				break;
			}

		}

		return new BasicBlock(instructions.subList(index, instructions.size()));
	}

	/**
	 * Extracts all branch targets given a branch {@code instruction}. The resulting
	 * list of targets depends on the {@code instruction}:
	 * <ul>
	 * <li>if-*: {@code target} and offset pointing right behind
	 * {@code instruction}.</li>
	 * <li>goto*: {@code target}.</li>
	 * <li>packed-/sparse-switch: {@code target} references a {@link SwitchPayload}.
	 * Thus all targets listed in that payload.</li>
	 * <li>otherwise: empty</li>
	 * </ul>
	 * 
	 * @param lookup      Offset -> Instruction lookup to avoid O(#instructions).
	 * @param target      Offset referenced by {@code instruction}.
	 * @param instruction {@link DecompiledInstruction}, for which to extract the
	 *                    branch targets.
	 * @return List of branch targets. The list may be empty.
	 */
	@NonNull
	private final List<Integer> extractBranchTargets(
			@NonNull final TreeMap<Integer, @NonNull DecompiledInstruction> lookup, final int target,
			@NonNull final DecompiledInstruction instruction) {

		List<Integer> branchTargets;

		if (lookup.containsKey(target)) {

			final DecompiledInstruction targetInstruction = lookup.get(target);

			// Ensure that switch and payload match
			final OffsetInstruction insn = (OffsetInstruction) instruction.getInstruction();
			if ((insn.getOpcode().equals(Opcode.PACKED_SWITCH)
					&& targetInstruction.getInstruction().getOpcode().equals(Opcode.PACKED_SWITCH_PAYLOAD))
					|| (insn.getOpcode().equals(Opcode.SPARSE_SWITCH)
							&& targetInstruction.getInstruction().getOpcode().equals(Opcode.SPARSE_SWITCH_PAYLOAD))) {
				// packed-/sparse - switch
				final SwitchPayload payload = (SwitchPayload) targetInstruction.getInstruction();

				// Iterate over switch elements an compute target offsets
				// relative to beginning of instructions
				branchTargets = new ArrayList<Integer>(payload.getSwitchElements().size());
				payload.getSwitchElements().stream().mapToInt(e -> e.getOffset() * 2 + instruction.getOffset())
						.forEach(branchTargets::add);

			} else if (StaticAnalyser.isIf(insn.getOpcode())) {
				// if-<cond> has two outgoing edges
				branchTargets = new ArrayList<Integer>(2);
				// Target of if statement (if body)
				branchTargets.add(target);
				// Instruction following if statement (else body)
				branchTargets.add(instruction.getOffset() + instruction.getByteCode().length);

			} else if (StaticAnalyser.isGoto(insn.getOpcode())) {
				// goto
				branchTargets = new ArrayList<>(1);
				branchTargets.add(target);
			} else {
				branchTargets = new ArrayList<>();
			}
		} else {
			branchTargets = new ArrayList<>();
		}

		return branchTargets;
	}

	/**
	 * Strips invalid branch targets and creates a list of valid target
	 * instructions. A branch {@code target} is invalid, iff. {@code target} does
	 * not refer to a valid instruction.
	 * 
	 * @param lookup  Offset -> Instruction lookup to avoid O(#instructions).
	 * @param targets List of branch targets to check.
	 * @return List of valid target instructions.
	 */
	@NonNull
	private final List<@NonNull DecompiledInstruction> stripBranches(
			@NonNull final TreeMap<Integer, @NonNull DecompiledInstruction> lookup,
			@NonNull final List<Integer> targets) {

		int target;
		final List<@NonNull DecompiledInstruction> instructions = new LinkedList<@NonNull DecompiledInstruction>();
		DecompiledInstruction instruction;
		for (int i = 0; i < targets.size(); i++) {

			target = targets.get(i);

			// If target is invalid, ignore it
			instruction = (@NonNull DecompiledInstruction) lookup.get(target);
			if (instruction != null) {
				instructions.add(instruction);
			}
		}

		return instructions;
	}

	/**
	 * Simple directed edge implementation, because {@link EndpointPair} is
	 * abstract.
	 */
	private static final class Edge<T> {
		private final T source;
		private final T destination;

		public Edge(@NonNull final T source, @NonNull final T destination) {
			this.source = source;
			this.destination = destination;
		}

		public final T getSource() {
			return this.source;
		}

		public final T getDestination() {
			return this.destination;
		}
	}
}