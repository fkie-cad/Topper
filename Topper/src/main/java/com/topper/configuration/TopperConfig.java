package com.topper.configuration;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Configuration wrapper for all configuration classes.
 * 
 * @author Pascal Kühnemann
 * @since 14.08.2023
 * */
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
	
	/**
	 * Gets currently loaded {@link GeneralConfig}.
	 * */
	@NonNull
	public final GeneralConfig getGeneralConfig() {
		this.generalConfig.check();
		return this.generalConfig;
	}
	
	/**
	 * Gets currently loaded {@link StaticAnalyserConfig}.
	 * */
	@NonNull
	public final StaticAnalyserConfig getStaticAnalyserConfig() {
		this.staticAnalyserConfig.check();
		return this.staticAnalyserConfig;
	}
	
	/**
	 * Gets currently loaded {@link SweeperConfig}.
	 * */
	@NonNull
	public final SweeperConfig getSweeperConfig() {
		this.sweeperConfig.check();
		return this.sweeperConfig;
	}
	
	/**
	 * Gets currently loaded {@link DecompilerConfig}.
	 * */
	@NonNull
	public final DecompilerConfig getDecompilerConfig() {
		this.decompilerConfig.check();
		return this.decompilerConfig;
	}
	
	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append(this.generalConfig.toString());
		b.append(this.decompilerConfig.toString());
		b.append(this.sweeperConfig.toString());
		b.append(this.staticAnalyserConfig.toString());
		return b.toString();
	}
}