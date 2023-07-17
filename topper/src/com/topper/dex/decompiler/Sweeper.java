package com.topper.dex.decompiler;

import org.jf.dexlib2.dexbacked.DexBuffer;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.TopperConfig;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

/**
 * Abstract decompiler that operates on a buffer. Given a starting
 * point, this decompiler tries to extract instructions.
 * 
 * @author Pascal Kühnemann
 * */
public abstract class Sweeper {
	
	/**
	 * Size of a code item.
	 * */
	public static final int CODE_ITEM_SIZE = 2;

	/**
	 * Configuration used by this sweeper. E.g. it may determine an upper bound
	 * on the number of instructions returned by <code>sweep</code>.
	 * */
	private final TopperConfig config;
	
	public Sweeper(final TopperConfig config) {
		this.config = config;
	}
	
	/**
	 * Gets the config assigned to this sweeper.
	 * */
	public final TopperConfig getConfig() {
		return this.config;
	}

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
	 * @return List of decompiled instructions retrieved from the sweep.
	 * */
	public abstract ImmutableList<DecompiledInstruction> sweep(final DexBuffer buffer, final int offset);
}
