package com.topper.commands;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.commands.CommandException;
import com.topper.sstate.CommandState;
import com.topper.sstate.ScriptContext;

public abstract class PicoCommand implements Runnable {

	@Override
	public final void run() {

		final Map<@NonNull Class<? extends CommandState>, @NonNull Set<@NonNull Class<? extends PicoCommand>>> map = CommandManager
				.getStateCommandMap();

		final PicoTopLevelCommand parent = this.getTopLevel();
		final CommandState current = this.getContext().getCurrentState();
		if (!map.containsKey(current.getClass())
				|| !map.get(current.getClass()).contains(this.getClass())) {
			parent.out().println("State error: Cannot run " + this.getClass().getSimpleName() + " in state "
					+ this.getContext().getCurrentState().getClass().getSimpleName());
			return;
		}

		try {
			// Execute this command.
			this.execute(this.getContext());
			
			// Move to next state.
			this.getContext().changeState(this.next());
		} catch (final CommandException e) {
			this.getTopLevel().out().println(e.getMessage());
		}
	}

	@NonNull
	public final ScriptContext getContext() {
		return this.getTopLevel().getContext();
	}
	
	public abstract void execute(@NonNull final ScriptContext context) throws CommandException;
	
	@NonNull
	public abstract CommandState next();
	
	@NonNull
	public abstract PicoTopLevelCommand getTopLevel();
}