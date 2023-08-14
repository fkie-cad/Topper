package com.topper.dex.decompilation.pipeline;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.configuration.TopperConfig;

public class PipelineArgs extends StageInfo {

	private final byte @NonNull [] buffer;
	
	@NonNull
	private final TopperConfig config;
	
	public PipelineArgs(@NonNull final TopperConfig config, final byte @NonNull [] buffer) {
		this.config = config;
		this.buffer = buffer;
	}
	
	@NonNull
	public final TopperConfig getConfig() {
		return this.config;
	}
	
	public final byte @NonNull [] getBuffer() {
		return this.buffer;
	}
}