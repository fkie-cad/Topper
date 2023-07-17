package com.topper.dex.decompiler;

import org.jf.dexlib2.dexbacked.DexBuffer;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.TopperConfig;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

/**
 * Decompiler built on top of <code>SmaliDecompiler</code>, which
 * performs a linear sweep, only backwards. I.e. this decompiler
 * tries to solve the following problem:
 * <blockquote>Given a valid starting instruction, obtain at most the first N instructions that precede the given instruction.</blockquote>
 * 
 * @author Pascal Kühnemann
 * */
public class BackwardLinearSweeper extends Sweeper {

	public BackwardLinearSweeper(final TopperConfig config) {
		super(config);
	}

	/**
	 * Performs a linear backward sweep on <code>buffer</code>.
	 * 
	 * */
	@Override
	public ImmutableList<DecompiledInstruction> sweep(final DexBuffer buffer, final int offset) {
		return null;
	}
}