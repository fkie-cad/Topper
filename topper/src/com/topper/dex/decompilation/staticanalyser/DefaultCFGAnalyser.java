package com.topper.dex.decompilation.staticanalyser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.OffsetInstruction;
import org.jf.dexlib2.iface.instruction.SwitchPayload;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public final class DefaultCFGAnalyser implements CFGAnalyser {

	private static final String INSN_IF = "if";

	@Override
	public CFG extractCFG(@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions) {

		// 1. Create initial CFG with only offset -> instruction mapping
		final CFG cfg = this.constructCFGWithMapping(instructions);

		// 2. Extract BranchInstructions
		final List<@NonNull BranchInstruction> branches = this.extractBranches(cfg, instructions);

		// 3. Create basic blocks from BranchInstructions
		this.constructFullCFG(cfg, branches, instructions);
		
		return cfg;
	}

	@NonNull
	private final CFG constructCFGWithMapping(
			final @NonNull ImmutableList<@NonNull DecompiledInstruction> instructions) {

		final CFG cfg = new CFG();

		for (final DecompiledInstruction instruction : instructions) {
			cfg.addOffsetInstructionLookup(instruction.getOffset(), instruction);
		}

		return cfg;
	}

	@NonNull
	private final List<@NonNull BranchInstruction> extractBranches(
			@NonNull final CFG cfg,
			@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions) {
		
		// 1. Iterate over all instructions and identify branches.
		final TreeMap<Integer, @NonNull DecompiledInstruction> lookup = cfg.getOffsetInstructionLookup();
		OffsetInstruction insn;
		int target;
		final List<@NonNull BranchInstruction> branches = new LinkedList<@NonNull BranchInstruction>();
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
					branches.add(
							new BranchInstruction(
									instruction,
									this.extractBranchTargets(lookup, target, instruction)
							)
					);
				}
				break;
			default:
				break;
			}
		}
		
		return branches;
	}
	
	private final void constructFullCFG(
			@NonNull final CFG cfg,
			@NonNull final List<@NonNull BranchInstruction> branches,
			@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions) {
		
		// BasicBlock:
		// - start: either 0, target of a branch instruction
		//			or right after a branch instruction.
		// - end: either on branch instruction or target
		
		// 1. Get all basic block starts
		// TreeSet is ordered at insertion
		DecompiledInstruction instruction;
		final TreeSet<Integer> blockStarts = new TreeSet<Integer>();
		blockStarts.add(0);
		for (final BranchInstruction branch : branches) {
			
			// Basic block right after if - instruction
			// (code located e.g. after goto is dead until
			// its a target of another branch)
			instruction = branch.getInstruction();
			if (instruction.getInstruction().getOpcode().name.startsWith(INSN_IF)) {
				blockStarts.add(instruction.getOffset() + instruction.getByteCode().length);
			}
			
			for (final int target : branch.getBranchTargets()) {
				
				// Basic block right at branch target
				blockStarts.add(target);
			}
		}
		
		// 2. Create basic blocks
		final int end = instructions.get(instructions.size() - 1).getByteCode().length + blockStarts.last();
		int size;
		List<@NonNull DecompiledInstruction> blockInstructions;
		CFG.BasicBlock block;
		
		final Integer[] starts = blockStarts.toArray(new Integer[0]);
		for (int i = 0; i < starts.length; i++) {
			
			if (i < starts.length - 1) {
				size = starts[i + 1] - starts[i];
			} else {
				size = end - starts[i];
			}
			
			// Get instructions
			blockInstructions = instructions.subList(starts[i], size);
			
			// Add new basic block
			block = new CFG.BasicBlock(instructions);
			cfg.getGraph().addNode(block);
			
			// Add mapping between instruction and block
			for (final DecompiledInstruction blockInstruction : blockInstructions) {
				cfg.addInstructionBlockLookup(blockInstruction, block);
			}
		}
		
		// TODO: Check above implementations. Add logic for finding edges!!!
	}


	@SuppressWarnings("null") // Stream.collect()...
	@NonNull
	private final List<Integer> extractBranchTargets(
			@NonNull final TreeMap<Integer, @NonNull DecompiledInstruction> lookup,
			final int target,
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
				branchTargets = payload.getSwitchElements().stream()
						.mapToInt(e -> e.getOffset() * 2 + instruction.getOffset()).boxed()
						.collect(Collectors.toList());
			} else {
				// if-<cond>, goto. Second branch of if-<cond> is handled below.
				branchTargets = new ArrayList<>(1);
				branchTargets.add(target);
			}
		} else {
			branchTargets = new ArrayList<Integer>(0);
		}

		return branchTargets;
	}

	private final class BranchInstruction {

		private final DecompiledInstruction instruction;
		private final List<Integer> targets;

		public BranchInstruction(final DecompiledInstruction instruction,
				final List<Integer> targets) {
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