package com.topper.dex.decompilation.graphs;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.jf.dexlib2.Opcode;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.DexHelper;
import com.topper.dex.decompilation.staticanalyser.StaticAnalyser;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;


/**
 * Basic block consisting of {@link DecompiledInstruction}s extracted by
 * applying static analysis on a particular byte buffer. Each block forms
 * a node in a {@link MutableGraph}, i.e. in a {@link CFG}.
 * 
 * @author Pascal Kühnemann
 * @since 07.08.2023
 * */
public class BasicBlock implements Comparable<BasicBlock> {

	/**
	 * List of instructions covered by this block.
	 * */
	private ImmutableList<@NonNull DecompiledInstruction> instructions;
	
	/**
	 * Type of this block. This depends on the type of the last instruction.
	 * */
	private final BlockType type;
	
	/**
	 * Offset relative to an underlying buffer.
	 * */
	private int offset;
	
	/**
	 * Size of this block, in bytes.
	 * */
	private int size;
	
	/**
	 * Create a basic block with an initial list of {@code instructions}.
	 * 
	 * It requires at least one instruction in {@code instructions}, as empty
	 * basic blocks may as well be omitted.
	 * 
	 * @param instructions List of instructions that form this basic block.
	 * */
	public BasicBlock(
		@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions) {
		
		this.setInstructions(instructions);
		this.type = BasicBlock.typeFrom(this.getBranchInstruction().getInstruction().getOpcode());
	}
	
	/**
	 * Gives the list of instructions that makes up this basic block.
	 * */
	@NonNull
	public final ImmutableList<@NonNull DecompiledInstruction> getInstructions() {
		return this.instructions;
	}
	
	/**
	 * Overwrites the list of instructions that makes up this basic block.
	 * 
	 * Its main use is to handle splitting basic blocks due to branch targets.
	 * */
	public final void setInstructions(@NonNull ImmutableList<@NonNull DecompiledInstruction> instructions) {
		if (instructions.size() == 0) {
			throw new IllegalArgumentException("Basic block must contain at least one instruction.");
		}
		this.instructions = instructions;
		this.size = instructions.stream().mapToInt(i -> i.getByteCode().length).sum();
		this.offset = instructions.get(0).getOffset();
	}
	
	/**
	 * Gives the last instruction in the list of instructions that make up
	 * this basic block.
	 * */
	@NonNull
	public final DecompiledInstruction getBranchInstruction() {
		return this.instructions.get(instructions.size() - 1);
	}
	
	/**
	 * Gives the byte offset of the first instruction relative to the
	 * underlying buffer, from which the instructions have been fetched.
	 * */
	public final int getOffset() {
		return this.offset;
	}
	
	/**
	 * Get size in bytes of this block.
	 * */
	public final int getSize() {
		return this.size;
	}
	
	/**
	 * Gives the type of this block, determined by the last instruction.
	 * In case a basic block has been split and its last instruction is not
	 * an actual branch instruction (like if, goto etc.), then its type is
	 * {@code BlockType.UNKNOWN}.
	 * */
	@Nullable
	public final BlockType getType() {
		return this.type;
	}
	
	/**
	 * Compare two basic blocks based on their offsets. This assumes that
	 * those basic blocks originate from the same buffer.
	 * */
	@Override
	public final boolean equals(final Object other) {
		
		if (other == null || !BasicBlock.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		
		return this.getOffset() == ((BasicBlock)other).getOffset();
	}
	
	/**
	 * Compares two basic blocks. Two basic blocks are ordered by
	 * their offsets.
	 * 
	 * Assumptions:
	 * 1. This basic block and <code>other</code> originate from the same buffer.
	 * */
	@Override
	public int compareTo(final BasicBlock o) {
		if (o == null) {
			throw new NullPointerException();
		}
		
		return this.offset - o.offset;
	}
	
	/**
	 * Convert a basic block to a string.
	 * */
	@SuppressWarnings("null")	// instructions is ALWAYS non-null
	@Override
	public final String toString() {
		final StringBuilder b = new StringBuilder();
		
		b.append("Offset: ");
		b.append(String.format("%#x", this.getOffset()));
		b.append(System.lineSeparator() + "Size: ");
		b.append(String.format("%#x", this.getSize()) + System.lineSeparator());
		b.append(DexHelper.instructionsToString(this.instructions));
		
		return b.toString();
	}
	
	public static enum BlockType {
		IF,
		SWITCH,
		GOTO,
		RETURN,
		THROW,
		UNKNOWN,
	}
	
	@NonNull
	public static final BlockType typeFrom(final @NonNull Opcode opcode) {
		
		if (StaticAnalyser.isIf(opcode)) {
			return BlockType.IF;
		} else if (StaticAnalyser.isGoto(opcode)) {
			return BlockType.GOTO;
		} else if (StaticAnalyser.isSwitch(opcode)) {
			return BlockType.SWITCH;
		} else if (StaticAnalyser.isThrow(opcode)) {
			return BlockType.THROW;
		} else if (StaticAnalyser.isReturn(opcode)) {
			return BlockType.RETURN;
		}
		return BlockType.UNKNOWN;
	}
}