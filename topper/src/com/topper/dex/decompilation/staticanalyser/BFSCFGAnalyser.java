package com.topper.dex.decompilation.staticanalyser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.OffsetInstruction;
import org.jf.dexlib2.iface.instruction.SwitchPayload;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.graphs.BasicBlock;
import com.topper.dex.decompilation.graphs.BasicBlock.BranchInstruction;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public class BFSCFGAnalyser implements CFGAnalyser {

	@Override
	public @Nullable CFG extractCFG(@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions,
			final int entry) {

		final CFG cfg = new CFG();

		DecompiledInstruction current;
		final TreeMap<Integer, Integer> offsetToIndex = new TreeMap<Integer, Integer>();
		for (int i = 0; i < instructions.size(); i++) {
			current = instructions.get(i);
			cfg.addOffsetInstructionLookup(current.getOffset(), current);
			offsetToIndex.put(current.getOffset(), i);
		}

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

//		this.initMapping(cfg, instructions);

		final BasicBlock startNode = this.findNextBlock(instructions, offsetToIndex.get(offset));
		cfg.getGraph().addNode(startNode);
		this.addMapping(cfg, startNode);

		final HashSet<BasicBlock> visitedNodes = new HashSet<BasicBlock>();
		visitedNodes.add(startNode);

		final Queue<@NonNull BasicBlock> todo = new LinkedBlockingQueue<@NonNull BasicBlock>();
		todo.add(startNode);

		BasicBlock current;
		int target;
		DecompiledInstruction branch;
		OffsetInstruction insn;
		List<Integer> branches;
		List<@NonNull DecompiledInstruction> targetInstructions;
		int index;
		BasicBlock child;
		List<@NonNull BasicBlock> branchTargets;
		while (!todo.isEmpty()) {

			current = todo.poll();

			// Identify branches
			branch = current.getInstructions().get(current.getInstructions().size() - 1);
			insn = (OffsetInstruction) branch.getInstruction();
			target = insn.getCodeOffset() * 2 + branch.getOffset();
			branches = this.extractBranchTargets(cfg.getOffsetInstructionLookup(), target, branch);

			// Check branches
			targetInstructions = this.stripBranches(cfg, branches);

			// Prepare list to store branch targets
			branchTargets = new LinkedList<BasicBlock>();

			// Construct basic block for each branch
			for (final DecompiledInstruction targetInstruction : targetInstructions) {

				index = offsetToIndex.get(targetInstruction.getOffset());
				child = this.findNextBlock(instructions, index);

				// Add branch connection
				branchTargets.add(child);

				// Add edges
//				cfg.getGraph().addNode(child);
//				cfg.getGraph().putEdge(current, child);
				
				// New block found, so process it
				if (!visitedNodes.contains(child)) {
					
					// Add to graph and avoid overlapping blocks
					this.splitAddOnOverlap(cfg, instructions, offsetToIndex, child);
					
					// Process it on next depth level
					visitedNodes.add(child);
					todo.add(child);
				}
				cfg.getGraph().putEdge(current, child);
			}

			// Setup branch instruction
			current.setBranch(
					new BranchInstruction(branch, ImmutableList.copyOf(branchTargets), this.getBranchType(branch)));
			

		}
	}

	/**
	 * Add block to cfg, if there is no other bb that block overlaps with. Otherwise
	 * split blocks.
	 * 
	 * Reason: It is possible that a branch goes right in middle of an existing
	 * block.
	 * 
	 * Provides following property:
	 * 1. The graph does not contain any pair of overlapping blocks
	 */
	@SuppressWarnings("null")
	private final void splitAddOnOverlap(@NonNull final CFG cfg,
			@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions,
			@NonNull final TreeMap<Integer, Integer> offsetToIndex, @NonNull final BasicBlock block) {

		final int offset = block.getOffset();
		final int size = block.getSize();

		BasicBlock node;
		DecompiledInstruction last;

		ImmutableList<@NonNull DecompiledInstruction> subInsns;
		int end;

		// Two cases:
		// 1. block overlaps existing bb from below
		// 2. block overlaps existing bb from above
		// In any case, block starts indicate a jump target.

		for (final BasicBlock bb : cfg.getGraph().nodes()) {

			// Case 1: bb contains a jump target block
			if (offset >= bb.getOffset() && offset < bb.getOffset() + bb.getSize()) {

				// Remove outgoing edges from bb, as its going to be shortened/split
				for (final BasicBlock referenced : cfg.getGraph().successors(bb)) {
					cfg.getGraph().removeEdge(bb, referenced);
				}

				end = offsetToIndex.get(offset);
				subInsns = bb.getInstructions().subList(offsetToIndex.get(bb.getOffset()), end);

				// Remove mapping of overhanging instructions
				for (final DecompiledInstruction instruction : bb.getInstructions().subList(end,
						bb.getInstructions().size())) {
					cfg.getInstructionBlockLookup().remove(instructions, bb);
				}

				// Assign new instruction sublist
				bb.setInstructions(subInsns);

				// Overwrite branch instruction. It only
				// "branches" to block.
				last = bb.getInstructions().get(bb.getInstructions().size() - 1);
				bb.setBranch(new BranchInstruction(last, ImmutableList.of(block), this.getBranchType(last)));

				// Add edge
				cfg.getGraph().putEdge(bb, block);
			}
			// Case 2: block contains a jump target bb
			else if (offset <= bb.getOffset() && offset + size > bb.getOffset()) {

				// Create new block
				block.setInstructions(
						block.getInstructions().subList(offsetToIndex.get(offset), offsetToIndex.get(bb.getOffset())));
				cfg.getGraph().addNode(block);
				this.addMapping(cfg, block);

				last = block.getInstructions().get(block.getInstructions().size() - 1);
				block.setBranch(new BranchInstruction(last, ImmutableList.of(bb), this.getBranchType(last)));
				cfg.getGraph().putEdge(block, bb);
			}
		}
	}

	private final void initMapping(@NonNull final CFG cfg,
			@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions) {

		for (final DecompiledInstruction instruction : instructions) {
			cfg.addOffsetInstructionLookup(instruction.getOffset(), instruction);
		}
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
		for (int i = index; i < instructions.size(); i++) {

			instruction = instructions.get(i);

			switch (instruction.getInstruction().getOpcode().format) {
			case Format10t:
			case Format20t:
			case Format21t:
			case Format22t:
			case Format30t:
			case Format31t:
				// fill-array-data uses 31t format
				if (instruction.getInstruction().getOpcode().equals(Opcode.FILL_ARRAY_DATA)) {
					return new BasicBlock(instructions.subList(index, i + 1));
				}
				break;
			default:
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

			} else if (this.isIfStatement(insn.getOpcode())) {
				// if-<cond> has two outgoing edges
				branchTargets = new ArrayList<Integer>(2);
				// Target of if statement (if body)
				branchTargets.add(target);
				// Instruction following if statement (else body)
				branchTargets.add(instruction.getOffset() + instruction.getByteCode().length);
			} else {
				// goto
				branchTargets = new ArrayList<>(1);
				branchTargets.add(target);
			}
		} else {
			branchTargets = new ArrayList<Integer>(0);
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

	private final boolean isIfStatement(@NonNull final Opcode opcode) {
		switch (opcode) {
		case IF_EQ:
		case IF_EQZ:
		case IF_GE:
		case IF_GEZ:
		case IF_GT:
		case IF_GTZ:
		case IF_LE:
		case IF_LEZ:
		case IF_LT:
		case IF_LTZ:
		case IF_NE:
		case IF_NEZ:
			return true;
		default:
			return false;
		}
	}

	private final BasicBlock.@NonNull BlockType getBranchType(@NonNull final DecompiledInstruction instruction) {

		if (!OffsetInstruction.class.isAssignableFrom(instruction.getClass())) {
			return BasicBlock.BlockType.SPLITTED;
		}

		final OffsetInstruction insn = (OffsetInstruction) instruction.getInstruction();
		if (insn.getOpcode().equals(Opcode.PACKED_SWITCH) || insn.getOpcode().equals(Opcode.SPARSE_SWITCH)) {
			return BasicBlock.BlockType.SWITCH;
		} else if (this.isIfStatement(insn.getOpcode())) {
			return BasicBlock.BlockType.IF;
		} else {
			return BasicBlock.BlockType.GOTO;
		}
	}
}