package com.topper.dex.decompilation.decompiler;

import com.topper.dex.decompilation.DecompilationResult;

/**
 * Decompiler interface for dex bytecode.
 * 
 * The default implementation is the <code>SmaliDecompiler</code>.
 * 
 * @author Pascal Kühnemann
 * @see SmaliDecompiler
 */
public interface Decompiler {

	/**
	 * Decompiles given <code>bytes</code> into <code>DecompiledInstruction</code>s
	 * wrapped in a <code>DecompilationResult</code>.
	 * 
	 * @param bytes Raw bytes to decompile into dex instructions.
	 * @return Result wrapping decompiled instructions and further information.
	 */
	DecompilationResult decompile(final byte[] bytes);
}