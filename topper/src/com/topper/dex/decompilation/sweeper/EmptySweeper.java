package com.topper.dex.decompilation.sweeper;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public class EmptySweeper implements Sweeper {

	@Override
	public ImmutableList<ImmutableList<DecompiledInstruction>> sweep(byte[] buffer, int offset) {
		return ImmutableList.of();
	}
}