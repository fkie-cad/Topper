package com.topper.configuration;

import org.eclipse.jdt.annotation.NonNull;

public class TopperConfig {

	/**
	 * Below configurations are mandatory.
	 */
	@NonNull
	private GeneralConfig generalConfig;
	
	@NonNull
	private StaticAnalyserConfig staticAnalyserConfig;
	
	@NonNull
	private SweeperConfig sweeperConfig;
	
	@NonNull
	private DecompilerConfig decompilerConfig;

	public TopperConfig(@NonNull final GeneralConfig generalConfig, @NonNull final StaticAnalyserConfig staticAnalyserConfig,
			@NonNull final SweeperConfig sweeperConfig, @NonNull final DecompilerConfig decompilerConfig) {
		this.generalConfig = generalConfig;
		this.staticAnalyserConfig = staticAnalyserConfig;
		this.sweeperConfig = sweeperConfig;
		this.decompilerConfig = decompilerConfig;
	}
	
	@NonNull
	public final GeneralConfig getGeneralConfig() {
		this.generalConfig.check();
		return this.generalConfig;
	}
	
	@NonNull
	public final StaticAnalyserConfig getStaticAnalyserConfig() {
		this.staticAnalyserConfig.check();
		return this.staticAnalyserConfig;
	}
	
	@NonNull
	public final SweeperConfig getSweeperConfig() {
		this.sweeperConfig.check();
		return this.sweeperConfig;
	}
	
	@NonNull
	public final DecompilerConfig getDecompilerConfig() {
		this.decompilerConfig.check();
		return this.decompilerConfig;
	}
}