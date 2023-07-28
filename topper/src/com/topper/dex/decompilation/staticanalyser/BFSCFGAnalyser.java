package com.topper.dex.decompilation.staticanalyser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
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

		final CFG cfg = new CFG();

		DecompiledInstruction current;
		final TreeMap<Integer, Integer> offsetToIndex = new TreeMap<Integer, Integer>();
		for (int i = 0; i < instructions.size(); i++) {
			current = instructions.get(i);
			cfg.addOffsetInstructionLookup(current.getOffset(), current);
			offsetToIndex.put(current.getOffset(), i);
		}

		final DecompiledInstruction last = instructions.get(instructions.size() - 1);
		offsetToIndex.put(last.getOffset() + last.getByteCode().length, instructions.size());

		this.extractCFGBFS(cfg, instructions, offsetToIndex, entry);

		return cfg;
	}

	/**
	 * Assumptions: 1. <code>offset</code> points to a valid instruction. 2.
	 * <code>index</code> refers to next instruction.
	 */
	private final void extractCFGBFS(@NonNull final CFG cfg,
			@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions,
			@NonNull final TreeMap<Integer, Integer> offsetToIndex, final int offset) {

		// Set up BFS
		final BasicBlock startNode = this.findNextBlock(instructions, offsetToIndex.get(offset));

		cfg.getGraph().addNode(startNode);
		// this.addMapping(cfg, startNode);

		final Queue<@NonNull BasicBlock> todo = new LinkedBlockingQueue<@NonNull BasicBlock>();
		todo.add(startNode);

		final Set<@NonNull BasicBlock> visitedNodes = new TreeSet<@NonNull BasicBlock>();
		visitedNodes.add(startNode);

		// Local variables used for on-the-fly graph creation
		final DecompiledInstruction last = instructions.get(instructions.size() - 1);
		final int largestOffset = last.getOffset() + last.getByteCode().length;
		BasicBlock current;
		int target;
		DecompiledInstruction branch;
		OffsetInstruction insn;
		List<Integer> branches;
		List<@NonNull DecompiledInstruction> targetInstructions;
		int index;
		BasicBlock child;

		while (!todo.isEmpty()) {

			current = todo.poll();

			// Obtain branch instruction
			branch = current.getInstructions().get(current.getInstructions().size() - 1);

			// Skip non - offset instructions like return or end - of - instruction blocks.
//			if (StaticAnalyser.isReturn(branch.getInstruction().getOpcode())
//					|| StaticAnalyser.isThrow(branch.getInstruction().getOpcode())
//					) {//|| branch.getOffset() + branch.getByteCode().length == largestOffset) {
//				// Return, throw or end of instructions are the only cases with empty branches
//				continue;
//			}

			// Identify branch targets
			if (OffsetInstruction.class.isAssignableFrom(branch.getInstruction().getClass())) {

				insn = (OffsetInstruction) branch.getInstruction();
				target = insn.getCodeOffset() * 2 + branch.getOffset();
				branches = this.extractBranchTargets(cfg.getOffsetInstructionLookup(), target, branch);

				// Ensure branch targets refer to valid instructions
				targetInstructions = this.stripBranches(cfg, branches);

				// Construct basic block for each branch
				for (final DecompiledInstruction targetInstruction : targetInstructions) {

					// Check if basic block is already known
//					child = cfg.getBlock(targetInstruction);
//					if (child == null) {

					// Create child basic block with instruction -> block mapping
//					index = offsetToIndex.get(targetInstruction.getOffset());
//					child= this.findNextBlock(instructions, index);

					// Only add forward edge to child and go to next
//					if (cfg.getGraph().nodes().contains(child)) {
//						cfg.getGraph().putEdge(current, child);
//						continue;
//					}

					// If child is known basic block
					//if (!visitedNodes.contains(child)) {
					if (!cfg.getGraph().nodes().stream().anyMatch(bb -> bb.getOffset() == targetInstruction.getOffset())) {
						// Let child take "ownership" of all covered instructions
//						this.addMapping(cfg, child);

						// Add to graph and avoid overlapping blocks (implicit addition
						// of child node in splitAddOnOverlap
//						this.splitOnOverlap(cfg, instructions, offsetToIndex, targetInstruction.getOffset());
						// cfg.getGraph().addNode(child);

//						this.addToGraph(instructions, offsetToIndex, targetInstruction.getOffset());
						
						// Process it on next depth level
						child = this.addToGraph(cfg, instructions, offsetToIndex, targetInstruction.getOffset());
						if (child != null) {
							cfg.getGraph().putEdge(current, child);
							todo.add(child);
						}
						//visitedNodes.add(child);
					}

					// In any case, add an edge to child
//					cfg.getGraph().putEdge(current, child);
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
	
	@Nullable
	private final BasicBlock addToGraph(@NonNull final CFG cfg, @NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions,
			@NonNull final TreeMap<Integer, Integer> offsetToIndex, final int offset) {
		
		// Invariant: Basic blocks do not overlap.
		assert(!cfg.getGraph().nodes().stream().anyMatch(first -> {
			return cfg.getGraph().nodes().stream().anyMatch(second -> {
				if (first.equals(second)) {
					return false;
				}
				return  (first.getOffset() <= second.getOffset() && first.getOffset() + first.getSize() > second.getOffset()) ||
						(second.getOffset() <= first.getOffset() && second.getOffset() + second.getSize() > first.getOffset());
			});
		}));
		
		// Three cases:
		// 1. Offset points into an existing basic block -> split block
		// 2. Offset points above an existing basic block -> precede block
		// 3. Offset points beneath an existing basic block	-> new block
		
		final int index = offsetToIndex.get(offset);
		BasicBlock child = null;
		
		// Indicates offset of next basic block coming after offset.
		// Therefore nextOffset > offset.
		int nextOffset = Integer.MAX_VALUE;
		BasicBlock nextBlock = null;
		
		// 1. offset points into existing basic block (unique). Equality
		//	  with existing basic block impossible, because this is just
		//	  the same basic block.
		final List<@NonNull Edge<@NonNull BasicBlock>> additions = new LinkedList<>();
		final List<@NonNull Edge<@NonNull BasicBlock>> deletions = new LinkedList<>();
		for (final BasicBlock block : cfg.getGraph().nodes()) {
			
			// Quickly check if offset points to existing block.
			if (block.getOffset() == offset) {
				return null;	// no child
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
				child = new BasicBlock(instructions.subList(index, offsetToIndex.get(block.getOffset() + block.getSize())));
				
				// Set instructions of block to cover only instructions up to child.
				block.setInstructions(instructions.subList(offsetToIndex.get(block.getOffset()), index));
				
				// block must reference child, and child must take over the references
				// i.e. outgoing edges of block.
				additions.add(new Edge<@NonNull BasicBlock>(block, child));
				for (final BasicBlock reference : cfg.getGraph().successors(block)) {
					deletions.add(new Edge<@NonNull BasicBlock>(block, reference));
					additions.add(new Edge<@NonNull BasicBlock>(child, reference));
				}
				
				break;	// unique
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
		assert(!cfg.getGraph().nodes().stream().anyMatch(b -> b.getOffset() <= offset && b.getOffset() + b.getSize() > offset));
		
		// 2. offset points above existing basic block (visually above; unique).
		//	  As nextOffset refers to the next basic block located (visually) below
		//	  offset, a new basic block can be search for. If they overlap, then just
		//	  create a basic block that immediately precedes the existing one
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
		assert(!cfg.getGraph().nodes().stream().anyMatch(b -> b.getOffset() >= offset));
		
		// 3. offset points below existing basic block (visually below; unique).
		//	  As whatever block precedes offset stopped before the instruction
		//	  referenced by offset, a new block can be created without any
		//	  connections whatsoever, only limited in size.
		child = this.findNextBlock(instructions, index);
		
		return child;
	}

	/**
	 * Add block to cfg, if there is no other bb that block overlaps with. Otherwise
	 * split blocks.
	 * 
	 * Reason: It is possible that a branch goes right in middle of an existing
	 * block.
	 * 
	 * Provides following property: 1. The graph does not contain any pair of
	 * overlapping blocks
	 */
	@SuppressWarnings("null")
	private final void splitOnOverlap(@NonNull final CFG cfg,
			@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions,
			@NonNull final TreeMap<Integer, Integer> offsetToIndex, final int offset) {

//		final int offset = block.getOffset();
		final int offsetIndex = offsetToIndex.get(offset);
//		final int size = block.getSize();
		
		BasicBlock split;
		
		BasicBlock bb = null;
		
		for (final BasicBlock other : cfg.getGraph().nodes()) {
			if (other.getOffset() < offset && other.getOffset() + other.getSize() > offset) {
				bb = other;
				break;
			}
		}
		
		// It could be that offset points before a basic block, but not
		// into another basic block.
//		if ()
		
		if (bb == null) {
			return;
		}
		
		// There exists a basic block that contains the new jump target
		// ==> split it into two blocks
		split = new BasicBlock(instructions.subList(offsetIndex, offsetToIndex.get(bb.getOffset() + bb.getSize())));
		cfg.getGraph().addNode(split);
		
		// Adjust size of old block
		bb.setInstructions(instructions.subList(offsetToIndex.get(bb.getOffset()), offsetIndex));
		
		// Edges of splitted block are moved to last split
		
		final List<Edge<BasicBlock>> deletions = new LinkedList<>();
		final List<Edge<BasicBlock>> additions = new LinkedList<>();
		
		for (final BasicBlock reference : cfg.getGraph().successors(bb)) {
//			cfg.getGraph().removeEdge(bb, reference);
			deletions.add(new Edge<BasicBlock>(bb, reference));
//			cfg.getGraph().putEdge(split, reference);
			additions.add(new Edge<BasicBlock>(split, reference));
		}
		
		for (final Edge<BasicBlock> e: deletions) {
			cfg.getGraph().removeEdge(e.getSource(), e.getDestination());
		}
		
		for (final Edge<BasicBlock> e: additions) {
			cfg.getGraph().removeEdge(e.getSource(), e.getDestination());
		}
		
		// Insert edges that connects splitted block
		cfg.getGraph().putEdge(bb, split);
			
			
		
//		int end;
//
//		// Two cases:
//		// 1. block overlaps existing bb from below
//		// 2. block overlaps existing bb from above
//		// In any case, block starts indicate a jump target.
//
//		final List<Edge<BasicBlock>> additions = new LinkedList<>();
//		final List<Edge<BasicBlock>> deletions = new LinkedList<>();
//		for (final BasicBlock bb : cfg.getGraph().nodes()) {
//
//			// Case 1: bb contains a jump target block
//			// If offset == bb.getOffset, then its the same block, so ignore it
//			if (offset > bb.getOffset() && offset < bb.getOffset() + bb.getSize()) {
//				
//				// Remove outgoing edges from bb, as its going to be shortened/split
//				for (final BasicBlock referenced : cfg.getGraph().successors(bb)) {
//					deletions.add(new Edge<BasicBlock>(bb, referenced));
//				}
//
//				// Assign new instruction sublist
//				bb.setInstructions(instructions.subList(offsetToIndex.get(bb.getOffset()), offsetIndex));
//
//				// Add edge
//				additions.add(new Edge<BasicBlock>(bb, block));
//			}
//			// Case 2: block contains a jump target bb
//			else if (offset < bb.getOffset() && offset + size > bb.getOffset()) {
//
//				// Shrink block
//				end = offsetToIndex.get(bb.getOffset());
//				block.setInstructions(instructions.subList(offsetIndex, end));
//
//				// Remove overhanging instructions
////				for (final DecompiledInstruction instruction : instructions.subList(end,
////						offsetToIndex.get(block.getOffset() + block.getSize()))) {
////					cfg.addInstructionBlockLookup(instruction, bb);
////				}
//
//				additions.add(new Edge<BasicBlock>(block, bb));
//			}
//		}
//
//		for (final Edge<BasicBlock >addition : additions) {
//			cfg.getGraph().putEdge(addition.getSource(), addition.getDestination());
//		}
//
//		for (final Edge<BasicBlock> deletion : deletions) {
//			cfg.getGraph().removeEdge(deletion.getSource(), deletion.getDestination());
//			
//		}
	}

	private final void addMapping(@NonNull final CFG cfg, @NonNull final BasicBlock block) {
		for (final DecompiledInstruction instruction : block.getInstructions()) {
			cfg.addInstructionBlockLookup(instruction, block);
		}
	}

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

	@NonNull
	private final List<@NonNull DecompiledInstruction> stripBranches(@NonNull final CFG cfg,
			@NonNull final List<Integer> targets) {

		int target;
		final List<@NonNull DecompiledInstruction> instructions = new LinkedList<@NonNull DecompiledInstruction>();
		DecompiledInstruction instruction;
		for (int i = 0; i < targets.size(); i++) {

			target = targets.get(i);

			// If target is invalid, ignore it
			instruction = cfg.getInstruction(target);
			if (instruction != null) {
				instructions.add(instruction);
			}
		}

		return instructions;
	}

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