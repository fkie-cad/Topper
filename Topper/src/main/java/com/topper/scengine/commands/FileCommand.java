package com.topper.scengine.commands;

import java.io.File;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.sstate.ScriptContext;

public final class FileCommand implements ScriptCommand {

	/**
	 * File referenced by the first parameter to the file command.
	 * */
	private final File file;
	
	public FileCommand(final File file) {
		this.file = file;
	}
	
	/**
	 * Executes the semantics linked to the "file [path]" command.
	 * */
	@Override
	public final void execute(final ScriptContext context) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Get the file linked to this file command.
	 * */
	public final File getFile() {
		return this.file;
	}
}