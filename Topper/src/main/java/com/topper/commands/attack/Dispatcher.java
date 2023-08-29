package com.topper.commands.attack;

import org.eclipse.jdt.annotation.NonNull;

public interface Dispatcher {
	byte @NonNull [] payload();
	
	int dispatcherOffset();
}