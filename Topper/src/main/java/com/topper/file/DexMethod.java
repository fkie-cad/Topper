package com.topper.file;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.jf.dexlib2.dexbacked.DexBackedMethod;

import com.topper.dex.decompilation.graphs.CFG;

public class DexMethod {

	@NonNull
	private final DexFile file;
	
	@NonNull
	private final DexBackedMethod method;
	
	@Nullable
	private final CFG cfg;
	
	public DexMethod(@NonNull final DexFile file, @NonNull final DexBackedMethod method, @Nullable final CFG cfg) {
		this.file = file;
		this.method = method;
		this.cfg = cfg;
	}
	
	@NonNull
	public final DexFile getDexFile() {
		return this.file;
	}
	
	@NonNull
	public final DexBackedMethod getMethod() {
		return this.method;
	}
	
	@Nullable
	public final CFG getCFG() {
		return this.cfg;
	}
	
	public final boolean hasCFG() {
		return this.cfg != null;
	}
}