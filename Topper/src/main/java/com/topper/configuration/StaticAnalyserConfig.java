package com.topper.configuration;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;

/**
 * Configuration used by {@link StaticAnalyser}.
 * 
 * @author Pascal Kühnemann
 * @since 14.08.2023
 * */
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
	 * Defaults to <code>false</code>.
	 * */
	public final boolean shouldSkipCFG() {
		this.check();
		return this.skipCFG;
	}
	
	/**
	 * Sets whether to skip CFG extraction.
	 * */
	public final void setSkipCFG(final boolean skip) {
		this.skipCFG = skip;
	}
	
	/**
	 * Determines whether or not to skip DFG extraction in static analysis.
	 * Defaults to <code>false</code>.
	 * */
	public final boolean shouldSkipDFG() {
		return this.skipDFG;
	}
	
	/**
	 * Sets whether to skip DFG extraction.
	 * */
	public final void setSkipDFG(final boolean skip) {
		this.skipDFG = skip;
	}

	/**
	 * Gets the <code>"staticAnalyser"</code> tag.
	 * */
	@Override
	@NonNull 
	public String getTag() {
		return "staticAnalyser";
	}

	/**
	 * Gets a list of valid {@link StaticAnalyser} configurations. E.g.
	 * <ul>
	 * <li>skipCfg(boolean)</li>
	 * <li>skipDfg(boolean)</li>
	 * </ul>
	 * */
	@Override
	@NonNull 
	public ImmutableList<@NonNull ConfigElement<?>> getElements() {
		return ImmutableList.of(
				new ConfigElement<Boolean>("skipCfg", false, this::setSkipCFG),
				new ConfigElement<Boolean>("skipDfg", false, this::setSkipDFG)
		);
	}
}