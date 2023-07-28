package com.topper.dex.decompilation.graphs;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public class BasicBlock implements Comparable<BasicBlock> {

	private ImmutableList<@NonNull DecompiledInstruction> instructions;
	
//	private BranchInstruction branch;
	
	private int offset;
	
	private int size;
	
	public BasicBlock(
		@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions) {
		assert(instructions.size() >= 1);	// at least on instruction
		this.setInstructions(instructions);
	}
	
	@NonNull
	public final ImmutableList<@NonNull DecompiledInstruction> getInstructions() {
		return this.instructions;
	}
	
	public final void setInstructions(@NonNull ImmutableList<@NonNull DecompiledInstruction> instructions) {
		if (instructions.size() == 0) {
			throw new IllegalArgumentException("Basic block must contain at least one instruction.");
		}
		this.instructions = instructions;
		this.size = instructions.stream().mapToInt(i -> i.getByteCode().length).sum();
		this.offset = instructions.get(0).getOffset();
	}
	
	@NonNull
	public final ImmutableList<@NonNull DecompiledInstruction> subInstructions(final int offsetFrom, final int offsetTo) {
		assert(offsetFrom < offsetTo);
		
		int start = -1;
		int end = -1;
		
		DecompiledInstruction current;
		
		for (int i = 0; i < this.instructions.size(); i++) {
			
			current = this.instructions.get(i);
			
			if (current.getOffset() < offsetFrom) {
				// Allow >= offsetFrom
				start = i;
			} else if (offsetTo > current.getOffset()) {
				// Allow < offsetTo
				end = i;
			}
		}
		
		return this.instructions.subList(start, end);
	}
	
	
//	public final BranchInstruction getBranch() {
//		return this.branch;
//	}
//	
//	public final void setBranch(@NonNull final BranchInstruction branch) {
//		this.branch = branch;
//	}
//	
//	public final boolean hasBranch() {
//		return this.branch != null;
//	}
	
	public final int getOffset() {
		return this.offset;
	}
	
	/**
	 * Get size in bytes of this block.
	 * */
	public final int getSize() {
		return this.size;
	}
	
//	@Nullable
//	public final BlockType getType() {
//		if (this.branch == null) {
//			return BlockType.UNKNOWN;
//		}
//		return this.branch.getType();
//	}
	
	@Override
	public final boolean equals(final Object other) {
		
		if (other == null || !BasicBlock.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		
		return this.getOffset() == ((BasicBlock)other).getOffset();
	}
	
	@Override
	public final String toString() {
		final StringBuilder b = new StringBuilder();
		
		b.append("Offset: ");
		b.append(String.format("%#x", this.getOffset()));
		b.append(System.lineSeparator() + "Size: ");
		b.append(String.format("%#x", this.getSize()) + System.lineSeparator());
//		b.append(System.lineSeparator() + this.getBranch());
		for (final DecompiledInstruction instruction : this.getInstructions()) {
			b.append(instruction);
			b.append(System.lineSeparator());
		}
		
		return b.toString();
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
	
	public static enum BlockType {
		IF,
		SWITCH,
		GOTO,
		RETURN,
		THROW,
		SPLITTED,
		UNKNOWN,
	}
	
//	public static class BranchInstruction {
//		
//		@NonNull
//		private final DecompiledInstruction instruction;
//		
//		@NonNull
//		private final ImmutableList<@NonNull BasicBlock> targets;
//		
////		@NonNull
////		private final BlockType type;
//		
//		public BranchInstruction(
//				@NonNull final DecompiledInstruction instruction,
//				@NonNull final ImmutableList<@NonNull BasicBlock> targets/*,
//				@NonNull final BlockType type*/) {
//			this.instruction = instruction;
//			this.targets = targets;
////			this.type = type;
//		}
//		
//		@NonNull
//		public final DecompiledInstruction getInstruction() {
//			return this.instruction;
//		}
//		
//		@NonNull
//		public final ImmutableList<@NonNull BasicBlock> getTargets() {
//			return this.targets;
//		}
//		
////		@NonNull
////		public final BlockType getType() {
////			return this.type;
////		}
//		
//		@Override
//		public final String toString() {
//			final StringBuilder b = new StringBuilder();
//			
//			b.append("Branch: " + this.getInstruction() + System.lineSeparator());
//			b.append("Targets:" + System.lineSeparator());
//			for (final BasicBlock target : this.getTargets()) {
//				b.append("  To: " + String.format("%#x", target.getOffset()) + System.lineSeparator());
//			}
//			
//			return b.toString();
//		}
//	}

}