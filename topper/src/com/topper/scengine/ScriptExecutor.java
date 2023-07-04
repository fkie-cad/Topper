package com.topper.scengine;

import java.io.IOException;

import com.google.common.collect.ImmutableList;
import com.topper.exceptions.CommandException;
import com.topper.exceptions.InvalidStateTransitionException;
import com.topper.scengine.commands.ScriptCommand;
import com.topper.sstate.ScriptContext;

public final class ScriptExecutor {

	private final ScriptContext context;
	
	public ScriptExecutor(final ScriptContext context) {
		this.context = context;
	}

	/**
	 * Executes a list of <code>commands</code> one by one according
	 * to the order induced by the list wrt. the <code>context</code>.
	 * 
	 * The <code>context</code> somewhat determines what the state
	 * of the script engine looks like before executing <code>commands</code>.
	 * 
	 * A command may change the <code>context</code>.
	 * 
	 * @param context Context of the script engine before executing
	 * 	<code>commands</code>.
	 * @param commands List of <code>ScriptCommand</code>s to execute.
	 * @throws CommandException, IOException 
	 * @throws InvalidStateTransitionException 
	 * */
	public final void execute(final ScriptContext context, final ImmutableList<ScriptCommand> commands) throws CommandException, IOException, InvalidStateTransitionException {
		
		// Naively execute each command
		for (final ScriptCommand command : commands) {
			this.context.getCurrentState().execute(command);
		}
	}
}