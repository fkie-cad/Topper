package com.topper.dex.decompilation.graphs;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public class CFG {

	@NonNull
	private final MutableGraph<CFG.@NonNull BasicBlock> graph;
	
	@SuppressWarnings("null")
	public CFG() {
		this.graph = GraphBuilder.directed().build();
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