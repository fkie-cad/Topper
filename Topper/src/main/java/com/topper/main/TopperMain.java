package com.topper.main;

import java.nio.file.Paths;
import java.util.logging.LogManager;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.configuration.ConfigManager;
import com.topper.configuration.TopperConfig;
import com.topper.exceptions.InvalidConfigException;
import com.topper.helpers.FileUtil;
import com.topper.sstate.CommandContext;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Manages command line arguments and kicks off execution.
 * 
 * @author Pascal Kühnemann
 * @since 14.08.2023
 * */
@Command(name = "topper", version = "0", mixinStandardHelpOptions = true)
public final class TopperMain implements Runnable {

	@Option(names = { "-c", "--config" }, required = true, paramLabel = "CONFIG", description = "path to .xml configuration file to use")
	private String configPath;
	
	@Option(names = { "-s", "--script" }, paramLabel = "SCRIPT", description = "path to script file to run in non-interactive mode")
	private String scriptPath;

	@Override
	public final void run() {
		
		// No logging on stdout/stderr!
		LogManager.getLogManager().reset();
		
		try {
			
			// Check config file. Its used in both, interactive and non - interactive mode.
			if (this.configPath != null) {
				try {
					FileUtil.openIfValid(this.configPath);
					ConfigManager.get().loadConfig(Paths.get(this.configPath));
				} catch (final InvalidConfigException | IllegalArgumentException e) {
					System.err.println("Config file " + this.configPath + " is invalid: " + e.getMessage());
					return;
				}
			}
			
			// Grab config
			final TopperConfig config = ConfigManager.get().getConfig();

			// Create context and executor
			final CommandContext context = new CommandContext(config);
			
			// Check script file. Only used in non - interactive
			final String path = this.scriptPath;
			if (path != null) {
				
				// Non - Interactive mode!
				final NonInteractiveTopper non = new NonInteractiveTopper();
				non.run(context, path);
			} else {
				
				// Interactive mode!
				final InteractiveTopper interactive = new InteractiveTopper();
				interactive.mainLoop(context);
			}
		
		} catch (final Exception ignored) {
			
			// Uncaught error...
			System.exit(42);
		}
	}
	
	public static void main(@NonNull final String[] args) {
		int exitCode = new CommandLine(new TopperMain()).execute(args);
		System.exit(exitCode);
	}
}