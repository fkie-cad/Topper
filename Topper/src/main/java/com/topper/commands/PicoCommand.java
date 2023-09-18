package com.topper.commands;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.commands.CommandException;
import com.topper.sstate.CommandContext;
import com.topper.sstate.CommandState;

/**
 * Abstract command description required to implement commands that fit in with
 * this system.
 * 
 * @author Pascal Kühnemann
 * @since 18.09.2023
 */
public abstract class PicoCommand implements Callable<Integer> {

	/**
	 * Success and error codes for {@code PicoCommand#call()}.
	 */
	public static final int SUCCESS = 0;
	public static final int ERROR = 1;

	/**
	 * Invokes this command's implementation. It is required that commands are
	 * subclasses of {@link PicoCommand} and provide a {@link CommandLink}
	 * annotation describing the {@link CommandState}s the commands may be called
	 * in.
	 * 
	 * @return Integer indicating success or failure. Failure either originates from
	 *         invalid command class setups or error that occur during command
	 *         execution.
	 */
	@Override
	public final Integer call() {

		final Map<@NonNull Class<? extends CommandState>, @NonNull Set<@NonNull Class<? extends PicoCommand>>> map = CommandManager
				.getStateCommandMap();

		final TopLevelCommand parent = this.getTopLevel();
		final CommandState current = this.getContext().getCurrentState();
		if (!map.containsKey(current.getClass()) || !map.get(current.getClass()).contains(this.getClass())) {
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

	/**
	 * Gets the {@link CommandContext} of this application.
	 * */
	@NonNull
	public final CommandContext getContext() {
		return this.getTopLevel().getContext();
	}

	/**
	 * Implements the underlying semantics of this command.
	 * 
	 * @param context Current {@link CommandContext} of this application.
	 * @throws CommandException If execution fails.
	 * */
	public abstract void execute(@NonNull final CommandContext context) throws CommandException;

	/**
	 * Determines the next {@link CommandState} <b>after</b> executing this command.
	 * */
	@NonNull
	public abstract CommandState next();

	/**
	 * Gets the {@link TopLevelCommand} that groups all available commands
	 * and provides global information to sub-commands.
	 * */
	@NonNull
	public abstract TopLevelCommand getTopLevel();
}