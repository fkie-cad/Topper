package com.topper.sstate;

import java.io.File;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.commands.file.BasedGadget;
import com.topper.dex.decompilation.staticanalyser.Gadget;

/**
 * Wrapper for holding all application - specific data like loaded files etc.
 * 
 * @author Pascal Kühnemann
 * @since 14.08.2023
 * */
public final class SessionInfo {

	private File loadedFile;
	
	private ImmutableList<com.topper.commands.file.BasedGadget> gadgets;
	
	public final File getLoadedFile() {
		return this.loadedFile;
	}

	public final void setLoadedFile(@NonNull final File loadedFile) {
		this.loadedFile = loadedFile;
	}

	public final ImmutableList<com.topper.commands.file.BasedGadget> getGadgets() {
		return this.gadgets;
	}

	public final void setGadgets(@NonNull final ImmutableList<com.topper.commands.file.BasedGadget> gadgets) {
		this.gadgets = gadgets;
	}
	
	@NonNull
	public final String getSessionId() {
		
		return (this.loadedFile != null) ? this.loadedFile.getName() : "";
	}
}