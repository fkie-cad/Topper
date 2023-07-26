package com.topper.dex.decompilation.graphs;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public class BasicBlock {

	@NonNull
	private ImmutableList<@NonNull DecompiledInstruction> instructions;
	
	private BranchInstruction branch;
	
	private final int offset;
	
	private final int size;
	
	public BasicBlock(
		@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions) {
		assert(instructions.size() >= 1);	// at least on instruction
		
		this.instructions = instructions;
		this.offset = this.instructions.get(0).getOffset();
		
		final DecompiledInstruction last = instructions.get(instructions.size() - 1);
		this.size = last.getOffset() + last.getByteCode().length;
	}
	
	@NonNull
	public final ImmutableList<@NonNull DecompiledInstruction> getInstructions() {
		return this.instructions;
	}
	
	public final void setInstructions(@NonNull ImmutableList<@NonNull DecompiledInstruction> instructions) {
		this.instructions = instructions;
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
	
	
	public final BranchInstruction getBranch() {
		return this.branch;
	}
	
	public final void setBranch(@NonNull final BranchInstruction branch) {
		this.branch = branch;
	}
	
	public final int getOffset() {
		return this.offset;
	}
	
	/**
	 * Get size in bytes of this block.
	 * */
	public final int getSize() {
		return this.size;
	}
	
	@Nullable
	public final BlockType getType() {
		if (this.branch == null) {
			return BlockType.UNKNOWN;
		}
		return this.branch.getType();
	}
	
	@Override
	public final boolean equals(final Object other) {
		
		if (other == null || !BasicBlock.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		
		return this.getOffset() == ((BasicBlock)other).getOffset();
	}
	
	public static enum BlockType {
		IF,
		SWITCH,
		GOTO,
		SPLITTED,
		UNKNOWN,
	}
	
	public static class BranchInstruction {
		
		@NonNull
		private final DecompiledInstruction instruction;
		
		@NonNull
		private final ImmutableList<@NonNull BasicBlock> targets;
		
		@NonNull
		private final BlockType type;
		
		public BranchInstruction(
				@NonNull final DecompiledInstruction instruction,
				@NonNull final ImmutableList<@NonNull BasicBlock> targets,
				@NonNull final BlockType type) {
			this.instruction = instruction;
			this.targets = targets;
			this.type = type;
		}
		
		@NonNull
		public final DecompiledInstruction getInstruction() {
			return this.instruction;
		}
		
		@NonNull
		public final ImmutableList<@NonNull BasicBlock> getTargets() {
			return this.targets;
		}
		
		@NonNull
		public final BlockType getType() {
			return this.type;
		}
	}
}