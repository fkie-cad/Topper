package com.topper.interactive;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;
import org.fusesource.jansi.AnsiConsole;
import org.jline.builtins.ConfigurationPath;
import org.jline.console.SystemRegistry;
import org.jline.console.impl.Builtins;
import org.jline.console.impl.SystemRegistryImpl;
import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.MaskingCallback;
import org.jline.reader.Parser;
import org.jline.reader.Reference;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.widget.TailTipWidgets;

import com.topper.commands.PicoTopLevelCommand;
import com.topper.sstate.ScriptContext;

import picocli.CommandLine;
import picocli.shell.jline3.PicocliCommands;
import picocli.shell.jline3.PicocliCommands.PicocliCommandsFactory;

/**
 * Manager that will run the main loop, if Topper is started in interactive
 * mode.
 * 
 * @author Pascal Kühnemann
 */
public final class InteractiveTopper {

	private static InteractiveTopper instance;

	private ScriptContext context;

	/**
	 * Format string to write before requesting user input.
	 */
	@NonNull
	private static final String LINE_PREFIX = "%s> ";

	private InteractiveTopper() {

	}

	public static final InteractiveTopper get() {
		if (InteractiveTopper.instance == null) {
			InteractiveTopper.instance = new InteractiveTopper();
		}
		return InteractiveTopper.instance;
	}

	/**
	 * Main loop for an interactive session. It manages IO as well as command
	 * parsing and execution.
	 * 
	 * @throws IOException If IO in interactive mode fails. This is a fatal error,
	 *                     from which recovery is not possible (probably).
	 */
	public final void mainLoop(@NonNull final ScriptContext context) throws IOException {

		this.context = context;

		AnsiConsole.systemInstall();
		try {
			final Supplier<Path> workDir = () -> Paths.get(System.getProperty("user.dir"));
			
			// set up JLine built-in commands
			// No idea why configPath must be non-null, but setting all its
			// params to null still works...
			final Builtins builtins = new Builtins(workDir, new ConfigurationPath(null, null), null);
			builtins.rename(Builtins.Command.TTOP, "top");
			builtins.alias("zle", "widget");
			builtins.alias("bindkey", "keymap");

			// set up picocli commands
			final PicoTopLevelCommand commands = new PicoTopLevelCommand(context);
			final PicocliCommandsFactory factory = new PicocliCommandsFactory();
			final CommandLine cmd = new CommandLine(commands, factory);
			cmd.setTrimQuotes(true);
			cmd.registerConverter(Integer.class, Integer::decode);
			cmd.registerConverter(Integer.TYPE, Integer::decode);
			final PicocliCommands picocliCommands = new PicocliCommands(cmd);

			final Parser parser = new DefaultParser();
			try (Terminal terminal = TerminalBuilder.builder().build()) {
				final SystemRegistry systemRegistry = new SystemRegistryImpl(parser, terminal, workDir, null);
				systemRegistry.setCommandRegistries(builtins, picocliCommands);
				systemRegistry.register("help", picocliCommands);

				final LineReader reader = LineReaderBuilder.builder().terminal(terminal)
						.completer(systemRegistry.completer()).parser(parser).variable(LineReader.LIST_MAX, 50) // max
																												// tab
																												// completion
																												// candidates
						.build();
				builtins.setLineReader(reader);
				commands.setReader(reader);
				factory.setTerminal(terminal);
				TailTipWidgets widgets = new TailTipWidgets(reader, systemRegistry::commandDescription, 5,
						TailTipWidgets.TipType.COMPLETER);
				widgets.enable();
				KeyMap<Binding> keyMap = reader.getKeyMaps().get("main");
				keyMap.bind(new Reference("tailtip-toggle"), KeyMap.alt("s"));

				final String rightPrompt = null;

				// start the shell and process input until the user quits with Ctrl-D
				String line;
				while (true) {
					try {
						systemRegistry.cleanUp();
						line = reader.readLine(String.format(LINE_PREFIX, this.context.getSession().getSessionId()),
								rightPrompt, (MaskingCallback) null, null);
						systemRegistry.execute(line);
					} catch (UserInterruptException e) {
						// Ignore
					} catch (EndOfFileException e) {
						return;
					} catch (Exception e) {
						systemRegistry.trace(e);
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			AnsiConsole.systemUninstall();
		}
	}
}