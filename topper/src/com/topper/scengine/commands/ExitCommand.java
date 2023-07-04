package com.topper.scengine.commands;

import java.io.IOException;

import com.topper.sstate.ScriptContext;

public final class ExitCommand implements ScriptCommand {

	@Override
	public final void execute(final ScriptContext context) throws IOException {
		
		// Say good bye
		context.getIO().output("Thank you for traveling with Deutsche Bahn!\n");
		
		// This command always triggers a transition into TerminationState!
	}
}