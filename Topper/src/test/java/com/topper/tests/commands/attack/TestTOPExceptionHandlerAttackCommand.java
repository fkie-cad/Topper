package com.topper.tests.commands.attack;

import java.io.PrintWriter;

import org.eclipse.jdt.annotation.NonNull;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.impl.DefaultParser;
import org.junit.jupiter.api.BeforeAll;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.topper.commands.PicoTopLevelCommand;
import com.topper.exceptions.InvalidConfigException;
import com.topper.sstate.CommandContext;
import com.topper.tests.utility.TestConfig;

import picocli.CommandLine;

/**
 * Idea: Use jazzer initialized with a single valid input and bombard
 * the "attack ctop" command.
 * */
public final class TestTOPExceptionHandlerAttackCommand {

	// Force fuzzing ctop!
	private static final String COMMAND_CTOP = "attack ctop";
	
	private static final String COMMAND_FILE_VDEX = "file -f ./src/test/java/resources/base.vdex -t VDEX";
	
	// Command line simulation
	private static PicoTopLevelCommand parent;
	private static Parser parser;
	private static CommandLine cmd;
	
	@BeforeAll
	public static void initAll() throws InvalidConfigException {
		
		parent = new PicoTopLevelCommand(new CommandContext(TestConfig.getDefault()));
		cmd = new CommandLine(parent);
		cmd.setTrimQuotes(true);
		cmd.registerConverter(Integer.class, Integer::decode);
		cmd.registerConverter(Integer.TYPE, Integer::decode);
		parent.setOut(new PrintWriter(System.out));
		
		parser = new DefaultParser();
		
		// Get into execution state
		runCommand(COMMAND_FILE_VDEX);
	}
	
	private static void runCommand(@NonNull final String command) {
		final ParsedLine result = parser.parse(command, 0);
		cmd.execute(result.words().toArray(new String[0]));
	}
	
	private String commandFromData(final FuzzedDataProvider data) {
		// Trying to fuzz semantics of ctop attack, not picocli.
		// Therefore, fix the order of the command parameters.
		
		final StringBuilder b = new StringBuilder();
		
		// --gadgets a,b,c,d
		b.append("-g ");
		
		final int numGadgets = Byte.toUnsignedInt(data.consumeByte());
		for (int i = 0; i < numGadgets; i++) {
			b.append(String.format("%#x", data.consumeInt()));
			if (i < numGadgets - 1) {
				b.append(",");
			}
		}
		b.append(" ");
		
		// --method-offset a
		b.append("-m " + String.format("%#x", data.consumeInt()) + " ");
		
		// --exception-type-index
		b.append("-t " + String.format("%#x", data.consumeInt()) + " ");
		
		// --exception-vreg-index
		b.append("-e " + String.format("%#x", data.consumeInt()) + " ");
		
		// --pc-vreg-index
		b.append("-p " + String.format("%#x", data.consumeInt()) + " ");
		
		// --method-handler-padding
		b.append("-x " + String.format("%#x", data.consumeInt()) + " ");
		
		// --handler-dispatcher-padding
		b.append("-y " + String.format("%#x", data.consumeInt()) + " ");
		
		// --alignemnt
		b.append("-a " + String.format("%#x", data.consumeInt()) + " ");
		
		// --verbose (relevant, because it triggers a lot of .toString calls --> might trigger errors)
		if (data.consumeBoolean()) {
			b.append("-v ");
		}
		
		// --tuple
		if (data.consumeBoolean()) {
			b.append("-u ");
		}
		
		return COMMAND_CTOP + " " + b.toString().stripTrailing();
	}
	
	@FuzzTest
	public void Given_ValidState_When_ExecutingCTopAttack_Expect_NoErrors(final FuzzedDataProvider data) {
		// Sometimes failure is inevitable, e.g. due to invalid inputs.
		
		// Interpret entire fuzz data as ctop parameters
		final String command = commandFromData(data);
		
		runCommand(command);
	}
}