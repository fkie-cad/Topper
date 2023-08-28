package com.topper.scengine.commands.attack;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.scripting.IllegalCommandException;
import com.topper.scengine.commands.AttackCommandParser;
import com.topper.scengine.commands.ScriptCommand;
import com.topper.scengine.commands.ScriptCommandParser;
import com.topper.scengine.commands.TopperCommandParser;
import com.topper.sstate.ExecutionState;

@TopperCommandParser(parent = AttackCommandParser.class, states = { ExecutionState.class })
public final class TOPExceptionHandlerAttackCommandParser implements ScriptCommandParser {

	public TOPExceptionHandlerAttackCommandParser() {
		
	}
	
	@Override
	@NonNull 
	public final ScriptCommand parse(@NonNull final String[] tokens) throws IllegalCommandException {
		return new TOPExceptionHandlerAttackCommand();
	}

	@Override
	@NonNull 
	public final String usage() {
		return this.command() + "";
	}

	@Override
	public @NonNull String command() {
		return "ctop";
	}

	@Override
	@NonNull 
	public Class<? extends ScriptCommand> commandType() {
		return TOPExceptionHandlerAttackCommand.class;
	}
}