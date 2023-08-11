package com.topper.sstate;

import java.io.IOException;

import com.google.common.collect.ImmutableList;
import com.topper.exceptions.CommandException;
import com.topper.exceptions.InvalidStateTransitionException;
import com.topper.scengine.commands.ScriptCommand;

public abstract class CommandState {
	
	/**
	 * Execution context of this state. It may be changed by commands.
	 * */
	private final ScriptContext context;
	
	/**
	 * Initialize this state.
	 * 
	 * @param context Execution context to assign to this state.
	 * */
	public CommandState(final ScriptContext context) {
		this.context = context;
	}

	/**
	 * Template method that precedes each command execution attempt
	 * with a white-list check to determine whether executing the
	 * requested command is allowed in this state or not.
	 * 
	 * @param command Command to check and execute if valid.
	 * @throws InvalidStateTransitionException If executing the
	 * 	requested command in this state is not allowed.
	 * @throws CommandException If execution of the command fails.
	 * @throws IOException If IO - related errors occur. If they
	 * 	make it until here they are fatal.
	 * */
	public final void execute(final ScriptCommand command) throws InvalidStateTransitionException, CommandException, IOException {
		
		// Command must pass whitelist check
		for (final Class<? extends ScriptCommand> allowed : this.getAvailableCommands()) {
			
			if (allowed.isAssignableFrom(command.getClass())) {
				
				// Run command
				this.executeCommand(command);
				return;
			}
		}
		
		// Otherwise trigger exception
		throw new InvalidStateTransitionException("Cannot execute command.");
	}
	
	/**
	 * Gets execution context assigned to this state.
	 * @see ScriptContext
	 * */
	public final ScriptContext getContext() {
		return this.context;
	}
	
	/**
	 * Describes what commands are available in this state. Therefore
	 * this is command whitelist. In this state, if <code>execute</code>
	 * is used to run a command, the command is only executed if its
	 * class is part of this whitelist.
	 * 
	 * This somewhat represents the transitions/edges in this finite
	 * state machine without the need of implementing a method for
	 * each possible subclass of <code>ScriptCommand</code>.
	 * 
	 * @return List of subclasses of <code>ScriptCommand</code> that are
	 * 	allowed to be executed in this state.
	 * */
	public abstract ImmutableList<Class<? extends ScriptCommand>> getAvailableCommands();
	
	/**
	 * Actual execution of <code>command</code>. Its semantics
	 * depend on the state.
	 * 
	 * @param command Command to execute in this state.
	 * */
	public abstract void executeCommand(final ScriptCommand command) throws InvalidStateTransitionException, CommandException, IOException;
}