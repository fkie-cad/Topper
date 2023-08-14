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
}