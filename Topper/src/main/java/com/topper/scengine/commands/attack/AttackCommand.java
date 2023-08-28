package com.topper.scengine.commands.attack;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.scripting.CommandException;
import com.topper.scengine.commands.ScriptCommand;
import com.topper.sstate.ScriptContext;

public final class AttackCommand implements ScriptCommand {

	@Override
	public final void execute(@NonNull final ScriptContext context) throws CommandException, IOException {
		throw new CommandException("Missing sub - command.");
	}
}