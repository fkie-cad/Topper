package com.topper.scengine;

import com.google.common.collect.ImmutableList;
import com.topper.sstate.ScriptContext;

public final class ScriptExecutor {


	public ScriptExecutor() {
		
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
	 * */
	public final void execute(final ScriptContext context, final ImmutableList<ScriptCommand> commands) {
		
		// Naively execute each command
		for (final ScriptCommand command : commands) {
			command.execute(context);
		}
	}
}