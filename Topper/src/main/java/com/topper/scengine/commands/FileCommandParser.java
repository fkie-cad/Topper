package com.topper.scengine.commands;

import com.topper.exceptions.scripting.IllegalCommandException;
import com.topper.file.FileUtil;

/**
 * 
 * */
public final class FileCommandParser implements ScriptCommandParser {

	/**
	 * Converts <code>tokens</code> from a <code>String</code> to
	 * a <code>FileCommand</code>.
	 * 
	 * The command looks like this:
	 * <code>> file [path to file]</code>
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
		if (tokens.length != 2) {
			throw new IllegalCommandException("Invalid usage: " + this.usage());
		}
		
		// Construct file path and check referenced file
		final String fileName = tokens[1];
		if (fileName == null) {
			throw new IllegalCommandException("Invalid usage: " + this.usage());
		}
		
		try {
			return new FileCommand(FileUtil.openIfValid(fileName));
		} catch (final IllegalArgumentException e) {
			throw new IllegalCommandException("File is invalid: " + e.getMessage());
		}
//		try {
//			final Path filePath = Paths.get(fileName).toRealPath();
//			final File file = filePath.toFile();
//			if (!file.canRead()) {
//				throw new IllegalCommandException(String.format("%s is not readable.", filePath));
//			}
//			
//			if (file.isDirectory()) {
//				throw new IllegalCommandException(String.format("%s is a directory.", filePath));
//			}
//			
//			if (!file.isFile()) {
//				throw new IllegalCommandException(String.format("%s is not a normal file.", filePath));
//			}
//		
//			// Up to this point, the file seems fine
//			return new FileCommand(file);
//			
//		} catch (final IOException e) {
//			throw new IllegalCommandException("Verification of file " + fileName + " failed.", e);
//		}
	}
	
	/**
	 * Determines the command format of this file command.
	 * 
	 * @return Human - readable file command format.
	 * */
	@Override
	public final String usage() {
		return this.command() + " [path to file]";
	}

	@Override
	public String command() {
		return "file";
	}
}