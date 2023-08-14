package com.topper.dex.decompilation.pipeline;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.configuration.TopperConfig;

public class PipelineArgs extends StageInfo {

	private final int offset;
	
	private final byte @NonNull [] buffer;
	
	@NonNull
	private final TopperConfig config;
	
	public PipelineArgs(@NonNull final TopperConfig config, final int offset, final byte @NonNull [] buffer) {
		this.config = config;
		this.offset = offset;
		this.buffer = buffer;
	}
	
	@NonNull
	public final TopperConfig getConfig() {
		return this.config;
	}
	
	public final int getOffset() {
		return this.offset;
	}
	
	public final byte @NonNull [] getBuffer() {
		return this.buffer;
	}
}