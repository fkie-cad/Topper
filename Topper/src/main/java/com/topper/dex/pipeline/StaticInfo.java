package com.topper.dex.pipeline;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.dex.staticanalyser.Gadget;

/**
 * Output of a {@link StaticAnalyser}.
 * 
 * @author Pascal Kühnemann
 * @since 16.08.2023
 * */
public class StaticInfo extends StageInfo {
	
	/**
	 * List of {@link Gadget}s extracted by {@link StaticAnalyser}.
	 * */
	@NonNull
	private final ImmutableList<@NonNull Gadget> gadgets;
	
	/**
	 * Creates a {@link StaticInfo} by storing a list of {@link Gadget}s.
	 * */
	public StaticInfo(@NonNull final ImmutableList<@NonNull Gadget> gadgets) {
		this.gadgets = gadgets;
	}
	
	/**
	 * Gets a list of {@link Gadget}s.
	 * */
	@NonNull
	public final ImmutableList<@NonNull Gadget> getGadgets() {
		return this.gadgets;
	}
}