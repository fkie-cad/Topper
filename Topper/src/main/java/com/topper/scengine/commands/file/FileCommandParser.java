package com.topper.scengine.commands.file;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.scripting.IllegalCommandException;
import com.topper.file.FileType;
import com.topper.file.FileUtil;
import com.topper.scengine.commands.ScriptCommand;
import com.topper.scengine.commands.ScriptCommandParser;
import com.topper.scengine.commands.TopperCommandParser;
import com.topper.sstate.ExecutionState;
import com.topper.sstate.SelectionState;

/**
 * 
 * */
@TopperCommandParser(states = { SelectionState.class, ExecutionState.class })
public final class FileCommandParser implements ScriptCommandParser {

	private final Pattern typePattern;
	
	public FileCommandParser() {
		
		final StringBuilder b = new StringBuilder();
		b.append("(");
		for (int i = 0; i < FileType.values().length; i++) {
			b.append(FileType.values()[i].name());
			if (i < FileType.values().length - 1) {
				b.append("|");
			}
		}
		b.append(")");
		
		this.typePattern = Pattern.compile(b.toString(), Pattern.CASE_INSENSITIVE);
	}
	
	
	/**
	 * Converts <code>tokens</code> from a <code>String</code> to
	 * a <code>FileCommand</code>.
	 * 
	 * The command looks like this:
	 * <code>> file [raw|dex|vdex] [path to file]</code>
	 * 
	 * @param tokens Command to convert to a <code>FileCommand</code>.
	 * @return <code>FileCommand</code> object representing <code>tokens</code>.
	 * @throws IllegalCommandException If given <code>tokens</code> differ
	 * 	from command format of file command.
	 * @see FileCommand
	 * */
	@Override
	public final ScriptCommand parse(final String[] tokens) throws IllegalCommandException {
		
		// Check rough correctness
		if (tokens.length != 3) {
			throw new IllegalCommandException("Invalid usage: " + this.usage());
		}
		
		// Check file type.
		final Matcher matcher = typePattern.matcher(tokens[1]);
		if (!matcher.matches()) {
			throw new IllegalCommandException("Specified file type " + tokens[1] + " is unknown.");
		}
		
		// This should not blow up
		final FileType type = FileType.valueOf(matcher.group().toUpperCase());
		
		// Construct file path and check referenced file
		final String fileName = tokens[2];
		if (fileName == null) {
			throw new IllegalCommandException("Invalid usage: " + this.usage());
		}
		
		try {
			return new FileCommand(FileUtil.openIfValid(fileName), type);
		} catch (final IllegalArgumentException e) {
			throw new IllegalCommandException("File is invalid: " + e.getMessage());
		}
	}
	
	/**
	 * Determines the command format of this file command.
	 * 
	 * @return Human - readable file command format.
	 * */
	@Override
	public final String usage() {
		return this.command() + this.typePattern.toString() + " <path to file>";
	}

	@Override
	public String command() {
		return "file";
	}


	@Override
	@NonNull 
	public Class<? extends ScriptCommand> commandType() {
		return FileCommand.class;
	}
}