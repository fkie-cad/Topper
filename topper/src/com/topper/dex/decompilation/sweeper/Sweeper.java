package com.topper.dex.decompilation.sweeper;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

/**
 * Abstract decompiler that operates on a buffer. Given a starting
 * point, this decompiler tries to extract instructions.
 * 
 * @author Pascal Kühnemann
 * */
public interface Sweeper {
	
	/**
	 * Performs a sweep on <code>buffer</code>, starting at <code>offset</code>.
	 * It is not assumed that <code>buffer</code> consists of valid bytecode.
	 * 
	 * A "sweep" is an operation that involves decompiling the contents of
	 * a buffer. Whether this sweep is linear, direct or what not depends on
	 * the specific implementation.
	 * 
	 * @param buffer Bytes to sweep in.
	 * @param offset Starting point relative to the beginning of <code>buffer</code>.
	 * @return A list of lists of decompiled instructions retrieved from the sweep.
	 * 	This is due to the fact that sweeping backwards for N instructions has
	 * 	multiple solutions.
	 * */
	public abstract ImmutableList<ImmutableList<DecompiledInstruction>> sweep(final byte[] buffer, final int offset);
}
