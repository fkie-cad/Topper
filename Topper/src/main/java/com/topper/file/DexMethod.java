package com.topper.file;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.jf.dexlib2.dexbacked.DexBackedMethod;

public class DexMethod {

	@NonNull
	private final DexFile file;
	
	@NonNull
	private final DexBackedMethod method;
	
	private final byte @Nullable [] buffer;
	
	private final int offset;
	
	public DexMethod(@NonNull final DexFile file, @NonNull final DexBackedMethod method, final byte @Nullable [] buffer, final int offset) {
		this.file = file;
		this.method = method;
		this.buffer = buffer;
		this.offset = offset;
	}
	
	@NonNull
	public final DexFile getDexFile() {
		return this.file;
	}
	
	@NonNull
	public final DexBackedMethod getMethod() {
		return this.method;
	}
	
	public final byte[] getBuffer() {
		return this.buffer;
	}
	
	public final int getOffset() {
		return this.offset;
	}
}