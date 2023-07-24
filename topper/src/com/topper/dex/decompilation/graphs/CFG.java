package com.topper.dex.decompilation.graphs;

import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public class CFG {

	@NonNull
	private final MutableGraph<CFG.@NonNull BasicBlock> graph;
	
	private final TreeMap<Integer, DecompiledInstruction> offsetToInstruction;
	private final TreeMap<DecompiledInstruction, CFG.BasicBlock> instructionToBlock;
	
	@SuppressWarnings("null")
	public CFG() {
		this.graph = GraphBuilder.directed().build();
		this.offsetToInstruction = new TreeMap<Integer, DecompiledInstruction>();
		this.instructionToBlock = new TreeMap<DecompiledInstruction, CFG.BasicBlock>();
	}
	
	public final void addOffsetInstructionLookup(final int offset, @NonNull final DecompiledInstruction instruction) {
		this.offsetToInstruction.put(offset, instruction);
	}
	
	public final void addInstructionBlockLookup(@NonNull final DecompiledInstruction instruction, final CFG.@NonNull BasicBlock block) {
		this.instructionToBlock.put(instruction, block);
	}
	
	@NonNull
	public final TreeMap<Integer, @NonNull DecompiledInstruction> getOffsetInstructionLookup() {
		return this.offsetToInstruction;
	}
	
	@NonNull
	public final TreeMap<@NonNull DecompiledInstruction, CFG.@NonNull BasicBlock> getInstructionBlockLookup() {
		return this.instructionToBlock;
	}
	
	@NonNull
	public final DecompiledInstruction getInstruction(final int offset) {
		return this.offsetToInstruction.get(offset);
	}
	
	public final CFG.@NonNull BasicBlock getBlock(@NonNull final DecompiledInstruction instruction) {
		return this.instructionToBlock.get(instruction);
	}
	
	@NonNull
	public final MutableGraph<CFG.@NonNull BasicBlock> getGraph() {
		return this.graph;
	}
	
	public static final class BasicBlock {
		
		@NonNull
		private final ImmutableList<@NonNull DecompiledInstruction> instructions;
		
		public BasicBlock(@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions) {
			this.instructions = instructions;
		}
		
		@NonNull
		public final ImmutableList<@NonNull DecompiledInstruction> getInstructions() {
			return this.instructions;
		}
	}
}