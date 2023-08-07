package com.topper.dex.decompilation.pipeline;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.staticanalyser.Gadget;

public class StaticInfo extends StageInfo {
	
	@NonNull
	private final ImmutableList<@NonNull Gadget> gadgets;
	
	public StaticInfo(@NonNull ImmutableList<@NonNull Gadget> gadgets) {
		this.gadgets = gadgets;
	}
	
	@NonNull
	public final ImmutableList<@NonNull Gadget> getGadgets() {
		return this.gadgets;
	}
}