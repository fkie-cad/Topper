package com.topper.sstate;

import org.eclipse.jdt.annotation.NonNull;

public final class TerminationState extends CommandState {
	public TerminationState(@NonNull final ScriptContext context) {
		super(context);
	}
}