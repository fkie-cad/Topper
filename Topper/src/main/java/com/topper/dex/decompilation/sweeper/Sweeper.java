package com.topper.dex.decompilation.sweeper;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.dex.decompilation.decompiler.Decompiler;
import com.topper.dex.decompilation.decompiler.SmaliDecompiler;
import com.topper.dex.decompilation.pipeline.Stage;
import com.topper.dex.decompilation.pipeline.StageInfo;

/**
 * Abstract decompiler that operates on a buffer. Given a starting point, this
 * decompiler tries to extract instructions.
 * 
 * @author Pascal Kühnemann
 */
public abstract class Sweeper<@NonNull T extends Map<@NonNull String, @NonNull StageInfo>> implements Stage<T> {

	/**
	 * Decompiler to use in <code>sweep</code>.
	 */
	private Decompiler decompiler;

	/**
	 * Initialises this sweeper to use <code>SmaliDecompiler</code>.
	 * 
	 * @see SmaliDecompiler
	 */
	public Sweeper() {
		this.decompiler = new SmaliDecompiler();
	}

	/**
	 * Get current decompiler implementation.
	 */
	@SuppressWarnings("null")	// If constructor is called, this cannot be null
	@NonNull
	public final Decompiler getDecompiler() {
		return this.decompiler;
	}

	/**
	 * Replace current decompiler implementation with a new one.
	 * 
	 * @param decompiler New decompiler implementation to use from this point
	 *                   onwards.
	 */
	public final void setDecompiler(@NonNull final Decompiler decompiler) {
		this.decompiler = decompiler;
	}

	/**
	 * Performs a sweep on <code>buffer</code>, starting at <code>offset</code>. It
	 * is not assumed that <code>buffer</code> consists of valid bytecode.
	 * 
	 * A "sweep" is an operation that involves decompiling the contents of a buffer.
	 * Whether this sweep is linear, direct or something else depends on the
	 * specific implementation.
	 * 
	 * @param buffer Bytes to sweep in.
	 * @param offset Starting point relative to the beginning of
	 *               <code>buffer</code>.
	 * @return A list of lists of decompiled instructions retrieved from the sweep.
	 *         This is due to the fact that sweeping backwards for N instructions
	 *         has multiple solutions. To put it in other words, this method returns
	 *         a list of instruction sequences obtained from linear backward
	 *         sweeping. If not instruction sequences are found, then the returned
	 *         list must be empty.
	 * @throws SweeperException If sweeping fails.
	 */
	
	
//	@NonNull
//	public abstract ImmutableList<@NonNull ImmutableList<@NonNull DecompiledInstruction>> sweep(
//			final byte @NonNull [] buffer, final int offset) throws SweeperException:w
//	;
}