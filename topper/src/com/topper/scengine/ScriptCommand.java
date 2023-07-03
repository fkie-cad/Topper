package com.topper.scengine;

public interface ScriptCommand {

	/**
	 * Execute this command. The semantics of this method depend on
	 * the implementing subclass.
	 * */
	void execute();
}