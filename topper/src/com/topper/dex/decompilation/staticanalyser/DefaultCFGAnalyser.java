package com.topper.dex.decompilation.staticanalyser;

import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.iface.instruction.OffsetInstruction;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public final class DefaultCFGAnalyser implements CFGAnalyser {

	@SuppressWarnings("null") // ImmutableList.get/subList are not expected to return null...
	@Override
	@NonNull
	public CFG extractCFG(@NonNull ImmutableList<@NonNull DecompiledInstruction> instructions) {

		// Create empty cfg
		final CFG cfg = new CFG();

		// Iterate through instructions and split on branch instructions.
		DecompiledInstruction instruction;
		Format format;
		int base = 0;
		int target;
		for (int i = 0; i < instructions.size(); i++) {

			instruction = instructions.get(i);
			
			// Reset target to be illegal.
			target = 0;

			// Branch instructions have specific opcode formats.
			// TODO: Filter instructions that use below formats, but are not branch instructions
			// TODO: Make sure that conditional instructions like if-g have two
			//		 outgoing edges: one to the instruction that comes right after
			//		 the "if", and one to the target (the latter is covered in second loop)
			format = instruction.getInstruction().getOpcode().format;
			switch (format) {
				case Format10t:
				case Format20t:
				case Format21t:
				case Format22t:
				case Format30t:
				case Format31t:
					cfg.getGraph().addNode(new CFG.BasicBlock(instructions.subList(base, i + 1)));
					break;
				default:
					break;
			}
		}
		
		// Iterate through all basic blocks and split on branch targets.
		CFG.BasicBlock first;
		CFG.BasicBlock second;
		for (final CFG.BasicBlock bb : cfg.getGraph().nodes()) {
			
			instruction = bb.getInstructions().get(bb.getInstructions().size() - 1);
			format = instruction.getInstruction().getOpcode().format;
			switch (format) {
				case Format10t:
				case Format20t:
				case Format21t:
				case Format22t:
				case Format30t:
				case Format31t:
					// Code offset in instruction has to be converted to get the
					// actual target offset. Its relative to branch instruction
					// and in code units.
					final OffsetInstruction insn = (OffsetInstruction)instruction.getInstruction();
					target = instruction.getOffset() + insn.getCodeOffset() * 2;
					
					// Is there another basic block that this basic block targets?
					for (final CFG.BasicBlock targetBB : cfg.getGraph().nodes()) {
						
						for (final DecompiledInstruction candidate : targetBB.getInstructions()) {
							
							if (candidate.getOffset() == target) {
								
								// If branch points to beginning of a basic block,
								// only an edge must be added.
								if (candidate.equals(targetBB.getInstructions().get(0))) {
									cfg.getGraph().putEdge(bb, targetBB);
								} else {
								
									// Remove current node from CFG and add two nodes.
									cfg.getGraph().removeNode(targetBB);
									
									// Add splitted node.
									first = new CFG.BasicBlock(
											targetBB.getInstructions().subList(0, target)
									);
									second = new CFG.BasicBlock(
											targetBB.getInstructions().subList(target, targetBB.getInstructions().size())
									);
									cfg.getGraph().addNode(first);
									cfg.getGraph().addNode(second);
									
									// Add edges from first to second, and from bb to second.
									cfg.getGraph().putEdge(first, second);
									cfg.getGraph().putEdge(bb, second);
								}
							}
						}
					}
					
					break;
				default:
					break;
			}
		}

		return cfg;
	}
}