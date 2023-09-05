package com.topper.dex.graphs;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

public final class DFG {

	@NonNull
	private final MutableGraph<DFG.@NonNull DFGNode> graph;
	
	@SuppressWarnings("null")
	public DFG() {
		this.graph = GraphBuilder.directed().build();
	}
	
	@NonNull
	public final MutableGraph<DFG.@NonNull DFGNode> getGraph() {
		return this.graph;
	}
	
	public static final class DFGNode {
		
	}
}