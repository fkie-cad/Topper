package com.topper.sstate;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.topper.commands.file.BasedGadget;
import com.topper.file.AugmentedFile;
import com.topper.file.DexFile;
import com.topper.file.VDexFile;

/**
 * Wrapper for holding all application - specific data like loaded files etc.
 * 
 * @author Pascal Kühnemann
 * @since 14.08.2023
 * */
public final class SessionInfo {

	private AugmentedFile loadedFile;
	
	private ImmutableList<@NonNull BasedGadget> gadgets;
	
	private DexFile currentDex;
	
	public final AugmentedFile getLoadedFile() {
		return this.loadedFile;
	}

	public final void setLoadedFile(@NonNull final AugmentedFile loadedFile) {
		this.loadedFile = loadedFile;
	}

	public final ImmutableList<@NonNull BasedGadget> getGadgets() {
		return this.gadgets;
	}

	public final void setGadgets(@NonNull final ImmutableList<@NonNull BasedGadget> gadgets) {
		this.gadgets = gadgets;
	}
	
	@NonNull
	public final String getSessionId() {
		
		String id = "";
		if (this.loadedFile == null) {
			return id;
		}
		
		id += this.loadedFile.getId();
		if (this.loadedFile instanceof VDexFile) {
			if (this.currentDex != null) {
				id += "/" + this.currentDex.getId();
			}
		}
		
		return id;
	}
	
	public final void setCurrentDex(@Nullable final DexFile currentDex) {
		this.currentDex = currentDex;
	}
	
	@Nullable
	public final DexFile getCurrentDex() {
		return this.currentDex;
	}
}