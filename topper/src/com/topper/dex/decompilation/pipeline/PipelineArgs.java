package com.topper.dex.decompilation.pipeline;

import org.eclipse.jdt.annotation.NonNull;

public class PipelineArgs extends StageInfo {

	private final int offset;
	private final int entry;
	
	private final byte @NonNull [] buffer;
	
	public PipelineArgs(final int offset, final int entry, final byte @NonNull [] buffer) {
		this.offset = offset;
		this.entry = entry;
		this.buffer = buffer;
	}
	
	public final int getOffset() {
		return this.offset;
	}
	
	public final int getEntry() {
		return this.entry;
	}
	
	public final byte @NonNull [] getBuffer() {
		return this.buffer;
	}
}