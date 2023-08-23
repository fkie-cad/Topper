package com.topper.scengine.commands;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.scripting.CommandException;
import com.topper.sstate.ScriptContext;

/**
 * All implementations of {@link ScriptCommand} must be located
 * in the same (or sub-) package as <code>ScriptCommandParser</code>,
 * because {@link CommandManager} loads all classes from this package.
 * This enables adding commands and their parsers without having to
 * modify multiple points in code.
 * */
public interface ScriptCommand {

	/**
	 * Execute this command. The semantics of this method depend on
	 * the implementing subclass and the <code>context</code>.
	 * 
	 * @param context Execution context of this command.
	 * @throws CommandException If execution fails.
	 * @throws IOException If IO - related errors occur.
	 * */
	void execute(@NonNull final ScriptContext context) throws CommandException, IOException;
}