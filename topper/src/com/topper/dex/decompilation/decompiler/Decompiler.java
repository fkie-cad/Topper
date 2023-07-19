package com.topper.dex.decompilation.decompiler;

import com.topper.dex.decompilation.DecompilationResult;

public interface Decompiler {

	DecompilationResult decompile(final byte[] bytes);
}