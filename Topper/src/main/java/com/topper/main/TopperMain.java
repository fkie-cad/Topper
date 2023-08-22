package com.topper.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.ConfigManager;
import com.topper.configuration.TopperConfig;
import com.topper.exceptions.InvalidConfigException;
import com.topper.exceptions.scripting.IllegalCommandException;
import com.topper.file.FileUtil;
import com.topper.interactive.IOManager;
import com.topper.interactive.InteractiveTopper;
import com.topper.scengine.ScriptExecutor;
import com.topper.scengine.ScriptParser;
import com.topper.scengine.commands.ExitCommandParser;
import com.topper.scengine.commands.FileCommandParser;
import com.topper.scengine.commands.HelpCommandParser;
import com.topper.scengine.commands.ScriptCommand;
import com.topper.scengine.commands.SearchCommandParser;
import com.topper.sstate.ScriptContext;

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

	@Option(names = { "-c", "--config" }, paramLabel = "CONFIG", description = "path to .xml configuration file to use")
	private String configPath;
	
	@Option(names = { "-s", "--script" }, paramLabel = "SCRIPT", description = "path to script file to run in non-interactive mode")
	private String scriptPath;

	@Override
	public final void run() {
		
		try {
		
			// Create IO manager for fast communication
			final IOManager io = new IOManager();
			
			// Check config file. Its used in both, interactive and non - interactive mode.
			if (this.configPath != null) {
				try {
					FileUtil.openIfValid(this.configPath);
					ConfigManager.get().loadConfig(Paths.get(this.configPath));
				} catch (final InvalidConfigException | IllegalArgumentException e) {
					try {
						io.error("Config file " + this.configPath + " is invalid: " + e.getMessage());
					} catch (final IOException ignored) {}
					return;
				}
			}
			
			// Grab config
			final TopperConfig config = ConfigManager.get().getConfig();
			
			// Regardless of the mode, parsing needs to be performed.
			final ScriptParser parser = new ScriptParser();

			// Create context and executor
			final ScriptContext context = new ScriptContext(config, io, parser);
			final ScriptExecutor executor = new ScriptExecutor();

			// Register commands with parser
			parser.registerParser(new FileCommandParser());
			parser.registerParser(new ExitCommandParser());
			parser.registerParser(new HelpCommandParser());
			parser.registerParser(new SearchCommandParser());
			
			// Check script file. Only used in non - interactive
			if (this.scriptPath != null) {
				
				// This means non - interactive mode!
				try {
					final File scriptFile = FileUtil.openIfValid(this.scriptPath);
					
					// Run script file
					@NonNull
					final ImmutableList<@NonNull ScriptCommand> commands = parser.parse(new String(FileUtil.readContents(scriptFile)));
					executor.execute(context, commands);
					
				} catch (final IllegalArgumentException | IllegalCommandException e) {
					try {
						io.error("Script file " + this.scriptPath + " is invalid: " + e.getMessage());
					} catch (final IOException ignored) {}
					return;
				}
			} else {
				
				// Interactive mode!
				final InteractiveTopper interactive = new InteractiveTopper();
				interactive.mainLoop(io, parser, executor, context);
			}
		
		} catch (final Exception ignored) {
			
			// DEBUG
			ignored.printStackTrace();
			// Uncaught error...
			System.exit(42);
		}
	}
	
	public static void main(@NonNull final String[] args) {
		int exitCode = new CommandLine(new TopperMain()).execute(args);
		System.exit(exitCode);
	}
}