package com.topper.sstate;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.topper.commands.file.BasedGadget;
import com.topper.file.AugmentedFile;
import com.topper.file.DexFile;

/**
 * Wrapper for holding all application - specific data like loaded files etc.
 * 
 * @author Pascal Kühnemann
 * @since 14.08.2023
 */
public final class Session {

	@Nullable
	private AugmentedFile loadedFile;

	@Nullable
	private ImmutableList<@NonNull BasedGadget> gadgets;

	@Nullable
	public final AugmentedFile getLoadedFile() {
		return this.loadedFile;
	}

	public final void setLoadedFile(@NonNull final AugmentedFile loadedFile) {
		this.loadedFile = loadedFile;
	}

	@Nullable
	public final ImmutableList<@NonNull BasedGadget> getGadgets() {
		return this.gadgets;
	}

	public final void setGadgets(@NonNull final ImmutableList<@NonNull BasedGadget> gadgets) {
		this.gadgets = gadgets;
	}

	@NonNull
	public final String getSessionId() {

		final AugmentedFile loaded = this.loadedFile;
		if (loaded == null) {
			return "";
		}

		return loaded.getId();
	}

	@Nullable
	public final ImmutableList<@NonNull DexFile> getDexFiles() {
		if (this.loadedFile != null) {
			return this.loadedFile.getDexFiles();
		}
		return null;
	}
}