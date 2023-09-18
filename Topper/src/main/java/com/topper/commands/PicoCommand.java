package com.topper.commands;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.commands.CommandException;
import com.topper.sstate.CommandContext;
import com.topper.sstate.CommandState;

public abstract class PicoCommand implements Callable<Integer> {
	
	public static final int SUCCESS = 0;
	public static final int ERROR = 1;

	@Override
	public final Integer call() {

		final Map<@NonNull Class<? extends CommandState>, @NonNull Set<@NonNull Class<? extends PicoCommand>>> map = CommandManager
				.getStateCommandMap();

		final TopLevelCommand parent = this.getTopLevel();
		final CommandState current = this.getContext().getCurrentState();
		if (!map.containsKey(current.getClass())
				|| !map.get(current.getClass()).contains(this.getClass())) {
			parent.out().println("State error: Cannot run " + this.getClass().getSimpleName() + " in state "
					+ this.getContext().getCurrentState().getClass().getSimpleName());
			return PicoCommand.ERROR;
		}

		try {
			// Execute this command.
			this.execute(this.getContext());
			
			// Move to next state.
			this.getContext().changeState(this.next());
			return PicoCommand.SUCCESS;
		} catch (final CommandException | RuntimeException e) {
			this.getTopLevel().out().println(e.getMessage());
			return PicoCommand.ERROR;
		}
	}

	@NonNull
	public final CommandContext getContext() {
		return this.getTopLevel().getContext();
	}
	
	public abstract void execute(@NonNull final CommandContext context) throws CommandException;
	
	@NonNull
	public abstract CommandState next();
	
	@NonNull
	public abstract TopLevelCommand getTopLevel();
}