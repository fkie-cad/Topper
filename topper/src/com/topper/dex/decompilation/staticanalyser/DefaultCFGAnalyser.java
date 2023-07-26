package com.topper.dex.decompilation.staticanalyser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.OffsetInstruction;
import org.jf.dexlib2.iface.instruction.SwitchPayload;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.graphs.BasicBlock;
import com.topper.dex.decompilation.graphs.BasicBlock.BranchInstruction;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public final class DefaultCFGAnalyser implements CFGAnalyser {

	private static final String INSN_IF = "if";

	@Override
	public CFG extractCFG(@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions, final int ignored) {

		// 0. Check trivial case
		if (instructions.size() == 0) {
			return new CFG();
		}

		// 1. Create initial CFG with only offset -> instruction mapping
		// 	  and offset -> index mapping (solve two problems with one loop)
		final TreeMap<Integer, Integer> offsetToIndex = new TreeMap<Integer, Integer>();
		final CFG cfg = new CFG();
		this.constructMappings(cfg, offsetToIndex, instructions);

		// 2. Extract BranchInstructions and strip invalid branches.
		List<@NonNull BranchInfo> branches = this.extractBranches(cfg, instructions);
		branches = this.stripBranches(cfg, branches);

		// 3. Create basic blocks from BranchInstructions
		this.constructBasicBlocks(cfg, branches, instructions, offsetToIndex);

		// 4. Add branch edges to graph
		this.constructEdges(cfg, branches, instructions);

		return cfg;
	}

	/**
	 * Construct mappings (offset -> instruction) and
	 * (offset -> index) to speed up future algorithms.
	 * This is a time - memory trade - off, because
	 * finding the index of an abitrary offset in a list
	 * takes O(list.size()), whereas the mapping reduces
	 * this to O(log(list.sizes())).
	 * 
	 * @param cfg <code>CFG</code> that holds the mapping for
	 * 			  offset to instruction to fill.
	 * @param offsetToIndex Mapping from offset to index to fill.
	 * @param instructions List of <code>DecompiledInstruction</code>s
	 * 					   used to fill the mappings.
	 * */
	private final void constructMappings(
			@NonNull final CFG cfg,
			@NonNull final TreeMap<Integer, Integer> offsetToIndex,
			@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions) {

		DecompiledInstruction instruction;
		for (int i = 0; i < instructions.size(); i++) {
			instruction = instructions.get(i);
			cfg.addOffsetInstructionLookup(instruction.getOffset(), instruction);
			offsetToIndex.put(instruction.getOffset(), i);
		}
		
		instruction = instructions.get(instructions.size() - 1);
		offsetToIndex.put(instruction.getOffset() + instruction.getByteCode().length, instructions.size());
	}

	@NonNull
	private final List<@NonNull BranchInfo> extractBranches(@NonNull final CFG cfg,
			@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions) {

		// 1. Iterate over all instructions and identify branches.
		final TreeMap<Integer, @NonNull DecompiledInstruction> lookup = cfg.getOffsetInstructionLookup();
		OffsetInstruction insn;
		int target;
		final List<@NonNull BranchInfo> branches = new LinkedList<@NonNull BranchInfo>();
		for (final DecompiledInstruction instruction : instructions) {

			// 2. Check if instruction is branch instruction.
			switch (instruction.getInstruction().getOpcode().format) {
			case Format10t:
			case Format20t:
			case Format21t:
			case Format22t:
			case Format30t:
			case Format31t:

				// fill-array-data uses 31t format
				insn = (OffsetInstruction) instruction.getInstruction();
				if (!insn.getOpcode().equals(Opcode.FILL_ARRAY_DATA)) {

					// 3. Compute target offset.
					target = instruction.getOffset() + insn.getCodeOffset() * 2;

					// 4. Extract targets.
					branches.add(new BranchInfo(instruction, this.extractBranchTargets(lookup, target, instruction)));
				}
				break;
			default:
				break;
			}
		}

		return branches;
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

			} else if (insn.getOpcode().name.startsWith(INSN_IF)) {
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
	private final List<@NonNull BranchInfo> stripBranches(@NonNull final CFG cfg,
			@NonNull final List<@NonNull BranchInfo> branches) {

		int target;
		for (final BranchInfo branch : branches) {

			for (int i = 0; i < branch.getBranchTargets().size(); i++) {

				target = branch.getBranchTargets().get(i);

				// If target is invalid, ignore it
				if (cfg.getInstruction(target) == null) {
					branch.getBranchTargets().remove(i);
				}
			}

			// Remove entire branch instruction, if it does not have targets
			if (branch.getBranchTargets().size() == 0) {
				branches.remove(branch);
			}
		}

		return branches;
	}

	/**
	 * Creates all <code>CFG.BasicBlock</code> nodes in the CFG.
	 * 
	 * To that end, this algorithm takes into account all branch targets (e.g.
	 * packed - switch, if-g, goto etc.) and creates a basic block for each target
	 * that starts with a valid instruction. This implies that each basic block
	 * contains at least one instruction.
	 */
	private final void constructBasicBlocks(@NonNull final CFG cfg, @NonNull final List<@NonNull BranchInfo> branches,
			@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions,
			@NonNull TreeMap<Integer, Integer> offsetToIndex) {

		// BasicBlock:
		// - start: either 0, target of a branch instruction
		// or right after a branch instruction.
		// - end: either on branch instruction or target

		// 1. Get all basic block starts
		// TreeSet is ordered at insertion
//		DecompiledInstruction instruction;
		final TreeSet<Integer> blockStarts = new TreeSet<Integer>();
		blockStarts.add(0);
		branches.forEach(branch -> branch.getBranchTargets().forEach(blockStarts::add));

		// 2. Create basic blocks
		final int end = instructions.get(instructions.size() - 1).getByteCode().length + blockStarts.last();
		ImmutableList<@NonNull DecompiledInstruction> blockInstructions;
		BasicBlock block;

		int size;
		int endIndex;
		final Integer[] starts = blockStarts.toArray(new Integer[0]);

//		ImmutableList<DecompiledInstruction> candidates;
//		int startOff;
//		int endOff;
		for (int i = 0; i < starts.length; i++) {

			// Check size
			if (i < starts.length - 1) {
				// Not last block
				size = starts[i + 1] - starts[i];
				endIndex = offsetToIndex.get(starts[i + 1]);
			} else {
				size = end - starts[i];
				endIndex = instructions.size();
			}

			// "Less" should be impossible, because TreeSet is sorted in ascending order.
			// Also all blocks have at one instruction...
			if (size <= 0) {
				continue;
			}

			// Check whether new block overlaps with existing block
//			for (final CFG.BasicBlock candidate : cfg.getGraph().nodes()) {
//				
//				// Check whether current block starts in candidate.
//				// Because starts is sorted in ascending order, this is the only case.
//				// candidate.getOffset() <= starts[i] granted by ascending order.
//				if (candidate.getOffset() + candidate.getSize() > starts[i]) {
//					
//					// As starts[i] points to an actual target of a branch
//					// instruction, shrink candidate block.
//					startOff = candidate.getOffset();
//					endOff = starts[i];
//					blockInstructions = instructions.subList(offsetToIndex.get(startOff), offsetToIndex.get(endOff));
//					block = new CFG.BasicBlock(blockInstructions,startOff, endOff - startOff);
//					cfg.getGraph().addNode(block);
//					
//					// Remove block and update instruction mappings
//					cfg.getGraph().removeNode(candidate);
//					for (final DecompiledInstruction blockInstruction : blockInstructions) {
//						cfg.getInstructionBlockLookup().remove(blockInstruction, candidate);
//						cfg.addInstructionBlockLookup(blockInstruction, block);
//					}
//					
//				}
//			}
			// starts are non - overlapping

			// Get instructions (at least one)
			blockInstructions = instructions.subList(offsetToIndex.get(starts[i]), endIndex);

			// Add new basic block
			block = new BasicBlock(blockInstructions);
			cfg.getGraph().addNode(block);

			// Add mapping between instruction and block
//			for (final DecompiledInstruction blockInstruction : blockInstructions) {
//				cfg.addInstructionBlockLookup(blockInstruction, block);
//			}
		}

	}

	/**
	 * Constructs the edges and branch instructions for the given <code>cfg</code>.
	 * 
	 * Assumptions:
	 * 1. Each <code>BasicBlock</code> starts with a valid instruction.
	 * 2. Each target in <code>branches.get(i).getBranchTargets</code> refers to a
	 *    valid instruction by byte offset.
	 * 3. Each target in <code>branches.get(i).getBranchTargets</code> refers to a branch instruction
	 *    with <code>BasicBlock.BlockType</code> different from
	 *    <code>BasicBlock.BlockType.SPLITTED</code>.
	 */
	private final void constructEdges(@NonNull final CFG cfg, @NonNull final List<@NonNull BranchInfo> branches,
			@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions) {

		// Cases:
		// 1. Basic block ends in branch instruction -> use branches list
		// 2. Basic block ends in non - branch instruction, because the following
		// block is a target of a branch instruction -> trivial branch

		// Now that all blocks are initialized, create BranchInstructions.
		// In parallel construct edges in graph.
		Optional<@NonNull BranchInfo> info;
		List<@NonNull BasicBlock> targets;
		BasicBlock candidate;
		DecompiledInstruction instruction;

		// Let N := cfg.getGraph().nodes().size(). Then this
		// loop dominates the runtime of this method with
		// O(N * N * log(N)^2), which is hopefully better than O(N^3)
		for (final BasicBlock block : cfg.getGraph().nodes()) {

			// Assumption 1
			final DecompiledInstruction last = block.getInstructions().get(block.getInstructions().size() - 1);

			// Is there any info on the last instruction. Runs in O(N), because
			// there are as many basic blocks as there are unique branch targets.
			info = branches.stream().filter(branch -> branch.getInstruction().getOffset() == last.getOffset())
					.findFirst();
			if (!info.isEmpty()) {
				// BranchInfo is based on instructions list. Also offset
				// unique identifies an instruction.

				// Find all target blocks. Runs in O(N)
				targets = new LinkedList<@NonNull BasicBlock>();
				for (final int target : info.get().getBranchTargets()) {

					// Perform lookup: O(log(N)^2)
					// Assumption 2
					candidate = cfg.getBlock(cfg.getInstruction(target));

					// Ensure that target points to block start
					if (candidate.getOffset() == target) {

						targets.add(candidate);
						cfg.getGraph().putEdge(block, candidate);
					}
				}

				block.setBranch(new BranchInstruction(last, ImmutableList.copyOf(targets), this.getBranchType(last)));
			} else {
				// If there is no info on last instruction, then
				// it is a splitted block, i.e. the next block is a
				// branch target of some other block.

				// Get next block, if any. This is not necessarily a valid
				// branch target, so double - check (not covered by stripBranches)
				instruction = cfg.getInstruction(block.getOffset() + block.getSize());
				if (instruction != null) {
					candidate = cfg.getBlock(instruction);
					if (candidate != null) {

						cfg.getGraph().putEdge(block, candidate);
						block.setBranch(
								new BranchInstruction(last, ImmutableList.of(candidate), this.getBranchType(last)));
					}
				}
			}
		}
	}

	private final BasicBlock.@NonNull BlockType getBranchType(@NonNull final DecompiledInstruction instruction) {

		if (!OffsetInstruction.class.isAssignableFrom(instruction.getClass())) {
			return BasicBlock.BlockType.SPLITTED;
		}

		final OffsetInstruction insn = (OffsetInstruction) instruction.getInstruction();
		if (insn.getOpcode().equals(Opcode.PACKED_SWITCH) || insn.getOpcode().equals(Opcode.SPARSE_SWITCH)) {
			return BasicBlock.BlockType.SWITCH;
		} else if (insn.getOpcode().name.startsWith(INSN_IF)) {
			return BasicBlock.BlockType.IF;
		} else {
			return BasicBlock.BlockType.GOTO;
		}
	}

	private final class BranchInfo {

		private final DecompiledInstruction instruction;
		private final List<Integer> targets;

		public BranchInfo(final DecompiledInstruction instruction, final List<Integer> targets) {
			this.instruction = instruction;
			this.targets = targets;
		}

		public final DecompiledInstruction getInstruction() {
			return this.instruction;
		}

		public final List<Integer> getBranchTargets() {
			return this.targets;
		}
	}
}