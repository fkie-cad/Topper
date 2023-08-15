package com.topper.dex.decompilation.graphs;

import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

/**
 * Control - Flow - Graph implementation that manages {@link BasicBlock}s
 * in a {@link MutableGraph}. Given an entry point, a CFG depicts all
 * control flow paths starting from that entry and ending in terminal instructions.
 * 
 * Terminal instructions are either
 * <ul>
 * <li>Throw (unless configured otherwise)</li>
 * <li>Return</li>
 * <li>Out - of - bounds</li>
 * </ul>
 * 
 * A terminal instruction indicates a severe change in code execution. E.g.
 * a <code>throw</code> often pivots into an exception handler, or a <code> return</code>
 * returns into the calling method. In the latter case, if a basic block
 * refers to an offset that is beyond the buffer underlying the list of available
 * instructions, then there will not be a way to construct a basic block.
 * 
 * In addition to the graph representation, a <code>CFG</code> also provides
 * mappings to speed up lookups that otherwise would run in O(#instructions):
 * <ul>
 * <li>Offset -> Instruction: yields O(log(#instructions)) instead of O(#instructions).</li>
 * <li>Instruction -> BasicBlock: yields O(log(#instructions)) instead of O(#instructions).</li>
 * </ul>
 * where #instructions is the total number of instructions, from which this <code>CFG</code>
 * was constructed.
 * 
 * The graph allows self - references. However, it may be necessary to discard
 * such CFGs depending on the specifications.
 * 
 * @author Pascal Kühnemann
 * @since 07.08.2023
 * */
public class CFG {

	/**
	 * Offset, from which CFG extraction starts.
	 * */
	private final int entry;
	
	/**
	 * Graph representation containing {@link BasicBlock}s as nodes.
	 * It may be empty.
	 * */
	@NonNull
	private final MutableGraph<@NonNull BasicBlock> graph;
	
	/**
	 * Offset -> Instruction map to speed up lookups. This map applies to all
	 * instructions available at the {@link CFG} extraction stage. Therefore the
	 * number of instructions covered by the basic blocks may be less than the
	 * number of mappings stored in <code>offsetToInstruction</code>.
	 * */
	@NonNull
	private final TreeMap<Integer, @NonNull DecompiledInstruction> offsetToInstruction;
	
	/**
	 * Instruction -> BasicBlock map to speed up lookups.
	 * */
	@NonNull
	private final TreeMap<@NonNull DecompiledInstruction, @NonNull BasicBlock> instructionToBlock;
	
	public CFG(final int entry) {
		this.entry = entry;
		this.graph = GraphBuilder.directed().allowsSelfLoops(true).build();
		this.offsetToInstruction = new TreeMap<Integer, @NonNull DecompiledInstruction>();
		this.instructionToBlock = new TreeMap<@NonNull DecompiledInstruction, @NonNull BasicBlock>();
	}
	
	/**
	 * Add an entry into the offset -> instruction lookup tree.
	 * */
	public final void addOffsetInstructionLookup(final int offset, @NonNull final DecompiledInstruction instruction) {
		this.offsetToInstruction.put(offset, instruction);
	}
	
	/**
	 * Add an entry into the instruction -> basic block lookup tree.
	 * */
	public final void addInstructionBlockLookup(@NonNull final DecompiledInstruction instruction, final @NonNull BasicBlock block) {
		this.instructionToBlock.put(instruction, block);
	}
	
	/**
	 * Gives the offset -> instruction lookup.
	 * */
	@NonNull
	public final TreeMap<Integer, @NonNull DecompiledInstruction> getOffsetInstructionLookup() {
		return this.offsetToInstruction;
	}
	
	/**
	 * Gives the instruction -> basic block lookup.
	 * */
	@NonNull
	public final TreeMap<@NonNull DecompiledInstruction, @NonNull BasicBlock> getInstructionBlockLookup() {
		return this.instructionToBlock;
	}
	
	/**
	 * Gives the byte offset, from which <code>CFG</code> extraction starts.
	 * */
	public final int getEntry() {
		return this.entry;
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
	
	/**
	 * Tries to obtain the basic block containing <code>instruction</code>. Notice that
	 * using e.g. <code>cfg.getBlock(cfg.getInstruction(0x42))</code> does not work, because
	 * the offset -> instruction map may cover more instructions than all basic blocks
	 * combined.
	 * 
	 * @return {@code BasicBlock} containing {@code instruction}, if it exists; {@code null} otherwise.
	 * */
	@Nullable
	public final BasicBlock getBlock(@NonNull final DecompiledInstruction instruction) {
		return this.instructionToBlock.get(instruction);
	}
	
	/**
	 * Gives the graph containing the basic blocks.
	 * */
	@NonNull
	public final MutableGraph<@NonNull BasicBlock> getGraph() {
		return this.graph;
	}
	
	/**
	 * Converts this <code>CFG</code> to a readable string representation.
	 * */
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