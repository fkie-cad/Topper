package com.topper.sstate;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.topper.commands.file.BasedGadget;
import com.topper.commands.file.PicoFileCommand;
import com.topper.exceptions.commands.IllegalSessionState;
import com.topper.exceptions.commands.InternalExecutionException;
import com.topper.file.ComposedFile;
import com.topper.file.DexFile;
import com.topper.main.InteractiveTopper;

/**
 * Wrapper for holding all application - specific data like loaded files etc.
 * 
 * @author Pascal Kühnemann
 * @since 14.08.2023
 */
public final class Session {

	/**
	 * Dex - based file loaded via {@link PicoFileCommand}.
	 */
	@Nullable
	private ComposedFile loadedFile;

	/**
	 * List of loaded gadgets, if any.
	 */
	@Nullable
	private ImmutableList<@NonNull BasedGadget> gadgets;

	/**
	 * Gets current {@link ComposedFile} loaded via {@link PicoFileCommand}.
	 * 
	 * @throws InternalExecutionException If loaded file is <code>null</code>.
	 */
	@NonNull
	public final ComposedFile getLoadedFile() throws IllegalSessionState {
		if (this.loadedFile != null) {
			return this.loadedFile;
		}
		throw new IllegalSessionState("Missing loaded file.");
	}

	/**
	 * Updates current {@link ComposedFile} with <code>loadedFile</code>.
	 */
	public final void setLoadedFile(@NonNull final ComposedFile loadedFile) {
		this.loadedFile = loadedFile;
	}

	/**
	 * Gets current list of {@link BasedGadget}. This list is linked to
	 * {@link Session#getLoadedFile()}, i.e. gadgets always belong to the currently
	 * loaded {@link ComposedFile}.
	 * 
	 * @throws IllegalSessionState If the list of {@link BasedGadget}s is
	 *                             <code>null</code>.
	 */
	@NonNull
	public final ImmutableList<@NonNull BasedGadget> getGadgets() throws IllegalSessionState {
		if (this.gadgets != null) {
			return this.gadgets;
		}
		throw new IllegalSessionState("Missing list of gadgets.");
	}

	/**
	 * Updates current list of {@link BasedGadget} with <code>gadgets</code>.
	 */
	public final void setGadgets(@NonNull final ImmutableList<@NonNull BasedGadget> gadgets) {
		this.gadgets = gadgets;
	}

	/**
	 * Gets current session id used in {@link InteractiveTopper} as a line prefix.
	 * It is based on {@link ComposedFile#getId()}.
	 */
	@NonNull
	public final String getSessionId() {

		final ComposedFile loaded = this.loadedFile;
		if (loaded == null) {
			return "";
		}

		return loaded.getId();
	}

	/**
	 * Gets current list of loaded {@link DexFile}s of
	 * {@link Session#getLoadedFile()}.
	 * 
	 * @throws IllegalSessionState If the list of {@link DexFile}s is
	 *                             <code>null</code>.
	 */
	@NonNull
	public final ImmutableList<@NonNull DexFile> getDexFiles() throws IllegalSessionState {
		if (this.loadedFile != null) {
			return this.loadedFile.getDexFiles();
		}
		throw new IllegalSessionState("Missing list of dex files.");
	}

	/**
	 * Discards all references, which invalidates {@link Session#getLoadedFile()}
	 * and {@link Session#getGadgets()}.
	 */
	public final void clear() {
		this.loadedFile = null;
		this.gadgets = null;
	}
}