package com.topper.tests.fuzzing.pipeline;



import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.junit.jupiter.api.BeforeAll;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.google.common.collect.ImmutableList;
import com.topper.configuration.Config;
import com.topper.configuration.DecompilerConfig;
import com.topper.configuration.GeneralConfig;
import com.topper.configuration.StaticAnalyserConfig;
import com.topper.configuration.SweeperConfig;
import com.topper.configuration.TopperConfig;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;
import com.topper.dex.pipeline.Pipeline;
import com.topper.dex.pipeline.PipelineArgs;
import com.topper.dex.pipeline.PipelineResult;
import com.topper.dex.pipeline.StaticInfo;
import com.topper.dex.pipeline.SweeperInfo;
import com.topper.dex.staticanalyser.Gadget;
import com.topper.exceptions.InvalidConfigException;
import com.topper.exceptions.pipeline.MissingStageInfoException;
import com.topper.tests.dex.decompilation.staticanalyser.TestDefaultStaticAnalyser;

public class TestFuzzPipeline {
	
	private static Pipeline defaultPipeline;
	
	
	private static final void enableConfig(final Config config) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		
		final Field loaded = config.getClass().getSuperclass().getDeclaredField("hasLoaded");
		loaded.setAccessible(true);
		loaded.setBoolean(config, true);
		loaded.setAccessible(false);
	}
	
	private static final void compareOpcodes(@NonNull final Opcodes a, @NonNull final Opcodes b) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		
		final Field ops = a.getClass().getDeclaredField("opcodesByValue");
		ops.setAccessible(true);
		
		final Opcode[] aOps = (Opcode[])ops.get(a);
		final Opcode[] bOps = (Opcode[])ops.get(b);
		
		assertEquals(aOps.length, bOps.length);
		assertArrayEquals(aOps, bOps);
		
		ops.setAccessible(false);
		
		assertEquals(a.api, b.api);
		assertEquals(a.artVersion, b.artVersion);
		for (int i = 0; i < aOps.length; i++) {
			assertEquals(a.getOpcodeByValue(i), b.getOpcodeByValue(i));
			assertEquals(a.getOpcodeValue(a.getOpcodeByValue(i)), b.getOpcodeValue(b.getOpcodeByValue(i)));
		}
	}

	private static final TopperConfig configFromBytes(@NonNull final FuzzedDataProvider data) throws InvalidConfigException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		
		// Decompiler
		final DecompilerConfig decompiler = new DecompilerConfig();
		enableConfig(decompiler);
		decompiler.setDexSkipThreshold(data.consumeInt(0, Integer.MAX_VALUE));
		decompiler.setDexVersion(data.consumeShort((short) 0, Short.MAX_VALUE));
		decompiler.setNopUnknownInstruction(data.consumeBoolean());

		// Sweeper
		final SweeperConfig sweeper = new SweeperConfig();
		enableConfig(sweeper);
		sweeper.setMaxNumberInstructions(data.consumeInt(1, Integer.MAX_VALUE));
//		sweeper.setPivotOpcode("THROW");
		
		final int opcodeValue = (int)(data.consumeByte() & 0xff);
		final Opcode op = Opcodes.forDexVersion(decompiler.getDexVersion()).getOpcodeByValue(opcodeValue);
		
		// It is not the goal to fuzz config construction!
		if (op == null) {
			throw new InvalidConfigException("");
		}
		sweeper.setPivotOpcode(op.name);
		
		// Static
		final StaticAnalyserConfig sa = new StaticAnalyserConfig();
		enableConfig(sa);
		sa.setSkipCFG(data.consumeBoolean());
		sa.setSkipDFG(data.consumeBoolean());

		// General
		final GeneralConfig general = new GeneralConfig();
		enableConfig(general);
		general.setDefaultAmountThreads(data.consumeInt(1, Integer.MAX_VALUE));
		
		final TopperConfig config = new TopperConfig(general, sa, sweeper, decompiler);
		return config;
	}
	
	private static final PipelineArgs argsFromBytes(@NonNull final TopperConfig config,
			final byte @NonNull [] buffer) {
		return new PipelineArgs(config, buffer);
	}

	private static final void checkConfigAdherence(@NonNull final TopperConfig config,
			@NonNull final PipelineResult result) throws MissingStageInfoException {

		// Decompiler
		// Check opcodes -> dex version
		final Opcodes opcodes = Opcodes.forDexVersion(config.getDecompilerConfig().getDexVersion());
		assertDoesNotThrow(() -> compareOpcodes(opcodes, config.getDecompilerConfig().getOpcodes()));
		final Opcode pivot = config.getSweeperConfig().getPivotOpcode();
		
		Opcode opcode;
		for (@NonNull
		final Gadget gadget : result.getContext().getStaticInfo(StaticInfo.class.getSimpleName()).getGadgets()) {

			// Static
			// Quickly check existence of cfg
			assertEquals(config.getStaticAnalyserConfig().shouldSkipCFG(), gadget.getCFG() == null);
			assertEquals(config.getStaticAnalyserConfig().shouldSkipCFG(), !gadget.hasCFG());

			assertEquals(config.getStaticAnalyserConfig().shouldSkipDFG(), gadget.getDFG() == null);
			assertEquals(config.getStaticAnalyserConfig().shouldSkipDFG(), !gadget.hasDFG());

			for (@NonNull
			final DecompiledInstruction insn : gadget.getInstructions()) {

				// Without 0xff, this would be sign - extended!!!!
				int opcodeValue = insn.getByteCode()[0] & 0xff;

				if (opcodeValue == 0) {
					opcodeValue = ByteBuffer.wrap(insn.getByteCode(), 0, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
				}
				
				opcode = opcodes.getOpcodeByValue(opcodeValue);
				if (opcode == null) {
					assertTrue(
							config.getDecompilerConfig().shouldNopUnknownInstruction(),
							"Instruction: " + insn.toString() + System.lineSeparator() + 
							String.format("Opcode: %#02x", insn.getByteCode()[0]) + System.lineSeparator() + 
							"opcodes: " + opcodes
					);
					assertEquals(Opcode.NOP, insn.getInstruction().getOpcode(), insn.toString() + String.format("Opcode: %#02x", insn.getByteCode()[0]));
				} else {
					assertEquals(opcode, insn.getInstruction().getOpcode(), String.format("Opcode: %#02x", insn.getByteCode()[0]));
				}
			}
			
			// Verify correctness of CFG and thus static analyser stage
			TestDefaultStaticAnalyser.verifyGadget(gadget, config, result.getContext());
			
			// Check that last instruction is a pivot instruction
			assertEquals(pivot, gadget.getInstructions().get(gadget.getInstructions().size() - 1).getInstruction().getOpcode());
		}

		// Sweeper
		// Check max number of instructions
		final SweeperInfo info = result.getContext().getInfo(SweeperInfo.class.getSimpleName());
		final ImmutableList<@NonNull ImmutableList<@NonNull DecompiledInstruction>> sequences = info
				.getInstructionSequences();

		for (@NonNull
		final ImmutableList<@NonNull DecompiledInstruction> sequence : sequences) {
			assertTrue(config.getSweeperConfig().getMaxNumberInstructions() >= sequence.size());
			assertTrue(1 <= sequence.size());
		}
	}
	
	@BeforeAll
	public static void init() {
		defaultPipeline = Pipeline.createDefaultPipeline();
	}

	@FuzzTest(maxDuration = "10m")
	public void Given_GeneratedConfig_When_ExecutingDefaultPipeline_Expect_NoErrors(final FuzzedDataProvider data) throws InvalidConfigException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		try {
			final TopperConfig config = configFromBytes(data);
			assertNotNull(config);
			
			final byte[] buffer = data.consumeRemainingAsBytes();
			
			final PipelineArgs args = argsFromBytes(config, buffer);

			// Run pipeline
			final PipelineResult result = assertDoesNotThrow(() -> defaultPipeline.execute(args), config.toString());
			assertNotNull(result);

			// Check adherence to config
			assertDoesNotThrow(() -> checkConfigAdherence(config, result), config.toString());
		} catch (final InvalidConfigException ignored) {
		}
	}
}