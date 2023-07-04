package com.topper.scengine;

import java.io.IOException;

import com.topper.sstate.ScriptContext;

public final class ExitCommand implements ScriptCommand {

	@Override
	public final void execute(final ScriptContext context) {
		
		// Say good bye
		try {
			context.getIO().output("Thank you for traveling with Deutsche Bahn!\n");
		} catch (final IOException ignored) {}
		
		// Terminate interactive loop
		context.getCurrentState().terminate();
	}
}