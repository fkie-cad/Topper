package com.topper.dex.decompilation.graphs;

import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public class CFG {

	@NonNull
	private final MutableGraph<@NonNull BasicBlock> graph;
	
	@NonNull
	private final TreeMap<Integer, @NonNull DecompiledInstruction> offsetToInstruction;
	
	@NonNull
	private final TreeMap<@NonNull DecompiledInstruction, @NonNull BasicBlock> instructionToBlock;
	
	@SuppressWarnings("null")
	public CFG() {
		this.graph = GraphBuilder.directed().build();
		this.offsetToInstruction = new TreeMap<Integer, @NonNull DecompiledInstruction>();
		this.instructionToBlock = new TreeMap<@NonNull DecompiledInstruction, @NonNull BasicBlock>();
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
	@Nullable
	public final DecompiledInstruction getInstruction(final int offset) {
		return this.offsetToInstruction.get(offset);
	}
	
	@Nullable
	public final BasicBlock getBlock(@NonNull final DecompiledInstruction instruction) {
		return this.instructionToBlock.get(instruction);
	}
	
	@NonNull
	public final MutableGraph<@NonNull BasicBlock> getGraph() {
		return this.graph;
	}
	
	@Override
	public final String toString() {
		final StringBuilder b = new StringBuilder();
		
		for (final BasicBlock block : this.getGraph().nodes()) {
			b.append(block);
			b.append("Outgoing Edges:" + System.lineSeparator());
			for (final BasicBlock ref : this.graph.successors(block)) {
				b.append(String.format("  To: %#x" + System.lineSeparator(), ref.getOffset()));
			}
			
			b.append(System.lineSeparator());
		}
		
		return b.toString();
	}
}