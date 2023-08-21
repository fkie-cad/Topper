package com.topper.scengine.commands;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.dex.decompilation.staticanalyser.Gadget;
import com.topper.exceptions.scripting.CommandException;
import com.topper.sstate.ScriptContext;

public final class SearchCommand implements ScriptCommand {

	@NonNull
	private final String expression;
	
	public SearchCommand(@NonNull final String expression) {
		this.expression = expression;
	}
	
	@Override
	public final void execute(@NonNull final ScriptContext context) throws CommandException, IOException {
		
		if (this.expression.length() == 0) {
			// Print all gadgets
			for (@NonNull final Gadget gadget : context.getSession().getGadgets()) {
				
				context.getIO().output(gadget.toString() + System.lineSeparator());
			}
		} else {
			// Interpret expression as regex
			final Pattern pattern = Pattern.compile(this.expression, Pattern.CASE_INSENSITIVE);
			
			// Apply expression to each gadget's string representation
			Matcher matcher;
			for (@NonNull final Gadget gadget : context.getSession().getGadgets()) {
				
				matcher = pattern.matcher(gadget.toString());
				if (matcher.find()) {
					
					context.getIO().output(gadget.toString() + System.lineSeparator());
				}
			}
		}
	}
}