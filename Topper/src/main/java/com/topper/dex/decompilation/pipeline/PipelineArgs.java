package com.topper.dex.decompilation.pipeline;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;

import com.topper.configuration.TopperConfig;

public class PipelineArgs extends StageInfo {

	private final byte @NonNull [] buffer;
	
	@NonNull
	private final TopperConfig config;
	
	@Nullable
	private final DexBackedDexFile augmentation;
	
	public PipelineArgs(@NonNull final TopperConfig config, final byte @NonNull [] buffer) {
		this(config, buffer, null);
	}
	
	public PipelineArgs(@NonNull final TopperConfig config, final byte @NonNull [] buffer, @Nullable final DexBackedDexFile augmentation) {
		this.config = config;
		this.buffer = buffer;
		this.augmentation = augmentation;
	}
	
	@NonNull
	public final TopperConfig getConfig() {
		return this.config;
	}
	
	public final byte @NonNull [] getBuffer() {
		return this.buffer;
	}
	
	@Nullable
	public final DexBackedDexFile getAugmentation() {
		return this.augmentation;
	}
}