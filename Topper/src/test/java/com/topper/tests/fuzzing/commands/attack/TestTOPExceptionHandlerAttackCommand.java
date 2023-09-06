package com.topper.tests.fuzzing.commands.attack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.impl.DefaultParser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.google.common.io.Files;
import com.topper.commands.PicoCommand;
import com.topper.commands.PicoTopLevelCommand;
import com.topper.commands.attack.Patch;
import com.topper.exceptions.InvalidConfigException;
import com.topper.helpers.FileUtil;
import com.topper.sstate.CommandContext;
import com.topper.tests.utility.IOHelper;
import com.topper.tests.utility.TestConfig;

import picocli.CommandLine;

/**
 * Idea: Use jazzer initialized with a single valid input and bombard the
 * "attack ctop" command.
 */
public final class TestTOPExceptionHandlerAttackCommand {

	// Force fuzzing ctop!
	private static final String COMMAND_CTOP = "attack ctop";

	private static final String COMMAND_FILE_VDEX = "file -f ./src/test/java/resources/base.vdex -t VDEX";

	private static final String VALID_COMMAND_LINE_PATH = "src/test/resources/com/topper/tests/fuzzing/commands/attack/TestTOPExceptionHandlerAttackCommand/Given_ValidState_When_ExecutingCTopAttack_Expect_NoErrors/valid_command_line";

	// Command line simulation
	private static PicoTopLevelCommand parent;
	private static Parser parser;
	private static CommandLine cmd;
	
	private static ByteArrayOutputStream out;
	private static ByteArrayOutputStream err;
	
	// Pattern matching
	private static final String TUPLE_LIST_PATTERN = "(?sm)patches = \\[.*\\]";
	private static final String TUPLE_PATTERN = "\\(([a-zA-Z0-9]+), b('(:?\\\\x[0-9][0-9])+')\\)";
	private static Pattern tupleListPattern;
	private static Pattern tuplePattern;

	@BeforeAll
	public static void initAll() throws InvalidConfigException, IOException {
		
		// Setup pattern
		tupleListPattern = Pattern.compile(TUPLE_LIST_PATTERN, Pattern.DOTALL);
		tuplePattern = Pattern.compile(TUPLE_PATTERN);

		// Overwrite valid command line
		Files.write(getValidCommandLine(), FileUtil.openIfValid(VALID_COMMAND_LINE_PATH));

		out = new ByteArrayOutputStream();
		err = new ByteArrayOutputStream();
		IOHelper.get().replaceOut(out);
		IOHelper.get().replaceErr(err);

		parent = new PicoTopLevelCommand(new CommandContext(TestConfig.getDefault()));
		cmd = new CommandLine(parent);
		cmd.setTrimQuotes(true);
		cmd.registerConverter(Integer.class, Integer::decode);
		cmd.registerConverter(Integer.TYPE, Integer::decode);
		parent.setOut(new PrintWriter(out));

		parser = new DefaultParser();

		// Get into execution state
		runCommand(COMMAND_FILE_VDEX);
	}
	
	@AfterAll
	public static void cleanAll() {
		IOHelper.get().restoreOut();
		IOHelper.get().restoreErr();
	}

	private static byte[] getValidCommandLine() {
		// attack ctop -g 0x594094, 0x5941b4, 0x594390, 0x5945b4 -m 0x580268 -t 0x29 -e
		// 0x0 -p 0x1 -x 0x100 -y 0x0 -a 0x8 -v -u
		final ByteBuffer buffer = ByteBuffer.allocate(1 + 4 * 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 1 + 1)
				.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put((byte) 0x4); // number of gadgets
		buffer.putInt(0x594094); // -g
		buffer.putInt(0x5941b4);
		buffer.putInt(0x594390);
		buffer.putInt(0x5945b4);

		buffer.putInt(0x580268); // -m
		buffer.putInt(0x29); // -t
		buffer.putInt(0x0); // -e
		buffer.putInt(0x1); // -p
		buffer.putInt(0x100); // -x
		buffer.putInt(0x0); // -y
		buffer.putInt(0x8); // -a
		buffer.put((byte) 0x1); // -v
		buffer.put((byte) 0x1); // -u

		return buffer.array();
	}

	private static String getCommandOutput() throws IOException {
		return out.toString();
	}
	
	private static String getCommandError() throws IOException {
		return err.toString();
	}
	
	private static void resetCommandStreams() {
		out.reset();
		err.reset();
	}

	private String commandFromData(final FuzzedDataProvider data) {
		// Trying to fuzz semantics of ctop attack, not picocli.
		// Therefore, fix the order of the command parameters.

		final StringBuilder b = new StringBuilder();

		// --gadgets a,b,c,d
		b.append("-g ");

		final int numGadgets = Byte.toUnsignedInt(data.consumeByte((byte)1, Byte.MAX_VALUE));
		for (int i = 0; i < numGadgets; i++) {
			b.append(String.format("%d", data.consumeInt()));
			if (i < numGadgets - 1) {
				b.append(",");
			}
		}
		b.append(" ");

		// --method-offset a
		b.append("-m " + String.format("%d", data.consumeInt()) + " ");

		// --exception-type-index
		b.append("-t " + String.format("%d", data.consumeInt()) + " ");

		// --exception-vreg-index
		b.append("-e " + String.format("%d", data.consumeInt()) + " ");

		// --pc-vreg-index
		b.append("-p " + String.format("%d", data.consumeInt()) + " ");

		// --method-handler-padding
		b.append("-x " + String.format("%d", data.consumeInt()) + " ");

		// --handler-dispatcher-padding
		b.append("-y " + String.format("%d", data.consumeInt()) + " ");

		// --alignemnt
		b.append("-a " + String.format("%d", data.consumeInt()) + " ");

		// --verbose (relevant, because it triggers a lot of .toString calls --> might
		// trigger errors)
		if (data.consumeBoolean()) {
			b.append("-v ");
		}

		// --tuple
		if (data.consumeBoolean()) {
			b.append("-u ");
		}

		return COMMAND_CTOP + " " + b.toString().stripTrailing();
	}

	private static int runCommand(@NonNull final String command) {
		final ParsedLine result = parser.parse(command, 0);
		final int exitCode = cmd.execute(result.words().toArray(new String[0]));
		parent.out().flush();
		return exitCode;
	}

	private static void checkOutput(final String command, final int exitCode, final String output, final String error) {
		
		// Stdout fuzzer infos.
		IOHelper.get().getOldOut().print(error);
		
		// Ignore errors for now, because they do not allow for semantic checks.
		if (exitCode == PicoCommand.ERROR) {
			return;
		}
		
		// Invariant: Execution is successful.
		final List<@NonNull Patch> patches = new LinkedList<>();
		final boolean tuplePrint = command.contains("-u");
		if (tuplePrint) {
			/**
			 * Format:
			 * patches = [
			 * 		(offset, bytes),
			 * 		...
			 * ]
			 * */
			// Match list of tuples.
			Matcher matcher = tupleListPattern.matcher(output);
			assertTrue(matcher.find());
			
			// Search for tuples.
			final String match = matcher.group();
			IOHelper.get().getOldOut().println(match.substring(0, 200));
			
			
			matcher = tuplePattern.matcher(match);
			assertTrue(matcher.groupCount() > 0);
			String offset;
			String bytes;
			for (int i = 0; i < matcher.groupCount(); i++) {
				offset = matcher.group(i + 1);
				IOHelper.get().getOldOut().println("Offset: " + offset);
				bytes = matcher.group(i + 2);
			}
		}
	}

	@FuzzTest
	public void Given_ValidState_When_ExecutingCTopAttack_Expect_NoErrors(final FuzzedDataProvider data)
			throws IOException {
		// Sometimes failure is inevitable, e.g. due to invalid inputs.

		// Interpret entire fuzz data as ctop parameters.
		final String command = commandFromData(data);
		data.consumeRemainingAsBytes();

		// Reset stdout and stderr to only consider the next command's output
		resetCommandStreams();
		
		// Execute the command.
		final int exitCode = runCommand(command);

		// Finally, check the output.
		checkOutput(command, exitCode, getCommandOutput().strip(), getCommandError().strip());
	}
}