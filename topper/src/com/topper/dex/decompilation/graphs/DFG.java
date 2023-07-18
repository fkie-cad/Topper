package com.topper.dex.decompilation.graphs;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

public final class DFG {

	private final MutableGraph<DFG.DFGNode> graph;
	
	public DFG() {
		this.graph = GraphBuilder.directed().build();
	}
	
	public final MutableGraph<DFG.DFGNode> getGraph() {
		return this.graph;
	}
	
	public static final class DFGNode {
		
	}
}