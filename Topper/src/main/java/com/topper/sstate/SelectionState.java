package com.topper.sstate;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Application state that allows selecting files that form the basis for core
 * execution features of Topper like e.g. analysis - related commands.
 * 
 * @author Pascal Kühnemann
 * @since 05.09.2023
 */
public final class SelectionState extends CommandState {
	public SelectionState(@NonNull final CommandContext context) {
		super(context);
	}
}