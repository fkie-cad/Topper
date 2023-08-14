package com.topper.sstate;

import java.io.File;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.staticanalyser.Gadget;

/**
 * Wrapper for holding all application - specific data like loaded files etc.
 * 
 * @author Pascal Kühnemann
 * @since 14.08.2023
 * */
public final class SessionInfo {

	private File loadedFile;
	
	private ImmutableList<@NonNull Gadget> gadgets;
	
	public final File getLoadedFile() {
		return this.loadedFile;
	}

	public final void setLoadedFile(@NonNull final File loadedFile) {
		this.loadedFile = loadedFile;
	}

	public final ImmutableList<@NonNull Gadget> getGadgets() {
		return this.gadgets;
	}

	public final void setGadgets(@NonNull final ImmutableList<@NonNull Gadget> gadgets) {
		this.gadgets = gadgets;
	}
}