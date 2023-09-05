package com.topper.dex.sweeper;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.dex.decompiler.Decompiler;
import com.topper.dex.decompiler.SmaliDecompiler;
import com.topper.dex.pipeline.Stage;

/**
 * Abstract decompiler that operates on a buffer. Given a starting point, this
 * decompiler tries to extract instructions.
 * 
 * @author Pascal Kühnemann
 * @since 15.08.2023
 */
public abstract class Sweeper implements Stage {
	/**
	 * Decompiler to use in <code>sweep</code>.
	 */
	private Decompiler decompiler;

	/**
	 * Initialises this sweeper to use {@link SmaliDecompiler}.
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
}