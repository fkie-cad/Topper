package com.topper.sstate;

import com.topper.exceptions.InvalidStateTransitionException;
import com.topper.scengine.FileCommand;
import com.topper.scengine.ScriptCommand;

public final class SelectionState extends CommandState {

	public SelectionState(final ExecutionDriver driver) {
		super(driver);
	}

	@Override
	public final void execute(final ScriptCommand command) throws InvalidStateTransitionException {

		if (!(command instanceof FileCommand)) {
			throw new InvalidStateTransitionException("Cannot execute command in selection state.");
		}
		
		// Only allowed command at the beginning is "file".
		command.execute();
		
		this.getContext().changeState(new ExecutionState(this.getContext()));
	}
}