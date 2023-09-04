package com.topper.commands.attack;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Abstract description of a bytecode dispatcher. Its task
 * is to manage the control flow of {@link Gadget}s.
 * 
 * @author Pascal Kühnemann
 * @since 04.09.2023
 * */
public interface Dispatcher {
	byte @NonNull [] payload();

	/**
	 * Gets the offset of this dispatcher relative to the beginning
	 * of the buffer returned by {@link Dispatcher#payload()}.
	 */
	int dispatcherOffset();
}