package com.topper.dex.decompilation.graphs;

import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public class CFG {

	@NonNull
	private final MutableGraph<@NonNull BasicBlock> graph;
	
	private final TreeMap<Integer, DecompiledInstruction> offsetToInstruction;
	private final TreeMap<DecompiledInstruction, BasicBlock> instructionToBlock;
	
	@SuppressWarnings("null")
	public CFG() {
		this.graph = GraphBuilder.directed().build();
		this.offsetToInstruction = new TreeMap<Integer, DecompiledInstruction>();
		this.instructionToBlock = new TreeMap<DecompiledInstruction, BasicBlock>();
	}
	
	public final void addOffsetInstructionLookup(final int offset, @NonNull final DecompiledInstruction instruction) {
		this.offsetToInstruction.put(offset, instruction);
	}
	
	public final void addInstructionBlockLookup(@NonNull final DecompiledInstruction instruction, final @NonNull BasicBlock block) {
		this.instructionToBlock.put(instruction, block);
	}
	
	@NonNull
	public final TreeMap<Integer, @NonNull DecompiledInstruction> getOffsetInstructionLookup() {
		return this.offsetToInstruction;
	}
	
	@NonNull
	public final TreeMap<@NonNull DecompiledInstruction, @NonNull BasicBlock> getInstructionBlockLookup() {
		return this.instructionToBlock;
	}

	/**
	 * Tries to obtain the instruction at <code>offset</code>.
	 * 
	 * @param offset Offset in bytes, for which to get the instruction.
	 * @return Instruction at <code>offset</code>, if it exists; <code>null</code> otherwise.
	 * */
	public final DecompiledInstruction getInstruction(final int offset) {
		return this.offsetToInstruction.get(offset);
	}
	
	public final BasicBlock getBlock(@NonNull final DecompiledInstruction instruction) {
		return this.instructionToBlock.get(instruction);
	}
	
	@NonNull
	public final MutableGraph<@NonNull BasicBlock> getGraph() {
		return this.graph;
	}
}