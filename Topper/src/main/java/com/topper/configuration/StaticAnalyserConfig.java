package com.topper.configuration;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;

public class StaticAnalyserConfig extends Config {
	
	/**
	 * Whether or not to skip Control Flow Graph extraction in static analysis.
	 * */
	private boolean skipCFG;
	
	/**
	 * Whether or not to skip Data Flow Graph extraction in static analysis.
	 * */
	private boolean skipDFG;
	
	/**
	 * Determines whether or not to skip CFG extraction in static analysis.
	 * Defaults to {@code false}.
	 * */
	public final boolean shouldSkipCFG() {
		this.check();
		return this.skipCFG;
	}
	
	public final void setSkipCFG(final boolean skip) {
		this.skipCFG = skip;
	}
	
	/**
	 * Determines whether or not to skip DFG extraction in static analysis.
	 * Defaults to {@code false}.
	 * */
	public final boolean shouldSkipDFG() {
		return this.skipDFG;
	}
	
	public final void setSkipDFG(final boolean skip) {
		this.skipDFG = skip;
	}

	@Override
	@NonNull 
	public String getTag() {
		return "staticAnalyser";
	}

	@Override
	@NonNull 
	public ImmutableList<@NonNull ConfigElement<?>> getElements() {
		return ImmutableList.of(
				new ConfigElement<Boolean>("skipCfg", false, this::setSkipCFG),
				new ConfigElement<Boolean>("skipDfg", false, this::setSkipDFG)
		);
	}
}