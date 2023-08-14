package com.topper.scengine.commands;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.dex.decompilation.staticanalyser.Gadget;
import com.topper.exceptions.CommandException;
import com.topper.exceptions.IllegalCommandException;
import com.topper.sstate.ScriptContext;

public final class SearchCommand implements ScriptCommand {

	@NonNull
	private final String expression;
	
	public SearchCommand(@NonNull final String expression) {
		this.expression = expression;
	}
	
	@Override
	public final void execute(@NonNull final ScriptContext context) throws CommandException, IOException {
		
		if (expression.length() == 0) {
			// Print all gadgets
			for (@NonNull final Gadget gadget : context.getSession().getGadgets()) {
				
				context.getIO().output(gadget.toString() + System.lineSeparator());
			}
		} else {
			throw new IllegalCommandException("Expression search not yet supported.");
		}
	}
}