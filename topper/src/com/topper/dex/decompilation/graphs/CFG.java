package com.topper.dex.decompilation.graphs;

import com.google.common.collect.ImmutableList;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public class CFG {

	private final MutableGraph<CFG.BasicBlock> graph;
	
	public CFG() {
		this.graph = GraphBuilder.directed().build();
	}
	
	public final MutableGraph<CFG.BasicBlock> getGraph() {
		return this.graph;
	}
	
	public static final class BasicBlock {
		
		private final ImmutableList<DecompiledInstruction> instructions;
		
		public BasicBlock(final ImmutableList<DecompiledInstruction> instructions) {
			this.instructions = instructions;
		}
		
		public final ImmutableList<DecompiledInstruction> getInstructions() {
			return this.instructions;
		}
	}
}