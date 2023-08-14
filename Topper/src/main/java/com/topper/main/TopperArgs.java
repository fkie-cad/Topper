package com.topper.main;

import java.nio.file.Path;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Wrapper for holding command line arguments.
 * 
 * @author Pascal Kühnemann
 * @since 14.08.2023
 * */
public final class TopperArgs {

	@NonNull
	private final Path configPath;
	
	public TopperArgs(@NonNull final Path configPath) {
		this.configPath = configPath;
	}
}