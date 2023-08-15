package com.topper.tests.dex.decompilation.sweeper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Opcode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.TopperConfig;
import com.topper.dex.decompilation.pipeline.PipelineArgs;
import com.topper.dex.decompilation.pipeline.PipelineContext;
import com.topper.dex.decompilation.pipeline.SeekerInfo;
import com.topper.dex.decompilation.pipeline.SweeperInfo;
import com.topper.dex.decompilation.sweeper.BackwardLinearSweeper;
import com.topper.dex.decompilation.sweeper.Sweeper;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;
import com.topper.exceptions.InvalidConfigException;
import com.topper.exceptions.StageException;
import com.topper.exceptions.SweeperException;
import com.topper.tests.utility.TestConfig;

public class TestBackwardLinearSweeper {

	private static final byte @NonNull [] VALID_BYTECODE = new byte[] { 0x54, 0x30, 0x24, 0x8, 0x1d, 0x0, 0x52, 0x31,
			0x25, 0x8, (byte) 0xd8, 0x1, 0x1, (byte) 0xff, 0x59, 0x31, 0x25, 0x8, 0x12, (byte) 0xf2, 0x33, 0x21, 0xb,
			0x0, 0x54, 0x31, 0x23, 0x8, 0x38, 0x1, 0x7, 0x0, 0x6e, 0x10, (byte) 0xf0, 0x14, 0x3, 0x0, 0x28, 0x2, 0xd,
			0x1, 0x1e, 0x0, 0xe, 0x0, 0xd, 0x1, 0x1e, 0x0, 0x27, 0x1, 0x3, 0x0, 0x0, 0x0, 0xb, 0x0, 0x1, 0x0, 0x10, 0x0,
			0x0, 0x0, 0x3, 0x0, 0x3, 0x0, 0x15, 0x0, 0x0, 0x0, 0x4, 0x0, 0x1, 0x0, 0x2, 0x0, 0x17, 0x7f, (byte) 0x91,
			0x1, 0x14, 0x17, 0x2, 0x0, 0x1, 0x0, 0x1, 0x0, 0x0, 0x0, (byte) 0xfb, 0xd, 0x2f, 0x0, 0x18, 0x0, 0x0, 0x0,
			0x54, 0x10, 0x23, 0x8, 0x38, 0x0, 0x12, 0x0, 0x54, 0x10, 0x23, 0x8 };
	private static final int VALID_BYTECODE_THROW_OFFSET = 0x32;
	private static final int VALID_BYTECODE_NUMBER_INSTRUCTIONS_IN_GADGET = 17;

	private static final byte @NonNull [] SHORT_VALID_BYTECODE = new byte[] { 0x27, 0x1 };
	private static final byte @NonNull [] MEDIUM_VALID_BYTECODE = new byte[] { 0x4, 0x11, 0x4, 0x22, 0x27, 0x1 };
	private static final int MEDIUM_VALID_BYTECODE_THROW_OFFSET = 4;
	private static final int MEDIUM_VALID_BYTECODE_AMOUNT_INSTRUCTIONS = 3;

	// Sweeper is stateless
	private static final Sweeper sweeper = new BackwardLinearSweeper();

	private static TopperConfig config;
	
	@BeforeAll
	public static void init() throws InvalidConfigException {
		config = TestConfig.getDefault();
	}
	
	@BeforeEach
	public final void initClass() throws InvalidConfigException {
		
		config.getSweeperConfig().setMaxNumberInstructions(VALID_BYTECODE_NUMBER_INSTRUCTIONS_IN_GADGET);
		config.getSweeperConfig().setPivotOpcode("THROW");
	}

	@NonNull
	private static PipelineContext createContext(final byte[] bytecode, final int offset) throws StageException {
		
		final PipelineArgs args = new PipelineArgs(config, bytecode);
		final PipelineContext context = new PipelineContext(args);
		context.putResult(SeekerInfo.class.getSimpleName(), new SeekerInfo(ImmutableList.of(offset)));
		
		return context;
	}

	// Method: sweep(buffer : byte[], offset : int) :
	// List<List<DecompiledInstruction>>
	@Test
	public void Given_Sweeper_When_ValidByteCodeAndOffset_Expect_CorrectSweep() throws StageException {

		final PipelineContext context = createContext(VALID_BYTECODE, VALID_BYTECODE_THROW_OFFSET);
		sweeper.execute(context);
		final SweeperInfo info = context.getResult(SweeperInfo.class.getSimpleName());
		final ImmutableList<@NonNull ImmutableList<@NonNull DecompiledInstruction>> sequences = info
				.getInstructionSequences();

		// Expecting at least one result (hard to predict though)
		assertTrue(sequences.size() >= 1);

		// Because bytecode is valid, expecting to either hit upper bound
		// and lower bound (pivot only)
		boolean[] exist = new boolean[VALID_BYTECODE_NUMBER_INSTRUCTIONS_IN_GADGET];
		for (int i = 0; i < exist.length; i++) {
			exist[i] = false;
		}

		for (final ImmutableList<@NonNull DecompiledInstruction> sequence : sequences) {
			exist[sequence.size() - 1] = true;
		}

		for (int i = 0; i < exist.length; i++) {
			assertTrue(exist[i]);
		}
		
		// All ending instructions must be pivot instructions.
		// No intermediate instruction must be a pivot instruction.
		for (final ImmutableList<@NonNull DecompiledInstruction> sequence : sequences) {
			
			assertEquals(config.getSweeperConfig().getPivotOpcode(), sequence.get(sequence.size() - 1).getInstruction().getOpcode());
			for (int i = 0; i < sequence.size() - 1; i++) {
				
				assertNotEquals(config.getSweeperConfig().getPivotOpcode(), sequence.get(i).getInstruction().getOpcode());
			}
		}

		// Expecting different sizes
		int firstSum, secondSum;
		for (final ImmutableList<DecompiledInstruction> first : sequences) {
			for (final ImmutableList<DecompiledInstruction> second : sequences) {

				if (first == second) {
					continue;
				}

				firstSum = first.stream().mapToInt(insn -> insn.getByteCode().length).sum();
				secondSum = second.stream().mapToInt(insn -> insn.getByteCode().length).sum();
				assertTrue(firstSum != secondSum);
			}
		}
	}

	@Test
	public void Given_Sweeper_When_OffsetOutOfBoundsBefore_Expect_SweeperException() {
		assertThrowsExactly(SweeperException.class, () -> sweeper.execute(createContext(VALID_BYTECODE, -1)));
	}

	@Test
	public void Given_Sweeper_When_OffsetOutOfBoundsAfter_Expect_SweeperException() {
		assertThrowsExactly(SweeperException.class,
				() -> sweeper.execute(createContext(VALID_BYTECODE, VALID_BYTECODE.length)));
	}

	@Test
	public void Given_Sweeper_When_OffsetZeroThrowAtZero_Expect_NopSequence() throws StageException {
		final PipelineContext context = createContext(SHORT_VALID_BYTECODE, 0);
		sweeper.execute(context);
		final SweeperInfo info = context.getResult(SweeperInfo.class.getSimpleName());
		final ImmutableList<@NonNull ImmutableList<@NonNull DecompiledInstruction>> sequences = info
				.getInstructionSequences();
		assertEquals(1, sequences.size());
		assertEquals(1, sequences.get(0).size());

		final DecompiledInstruction insn = sequences.get(0).get(0);
		assertEquals(Opcode.THROW, insn.getInstruction().getOpcode());
	}

	@Test
	public void Given_Sweeper_When_OffsetToInvalidInstruction_Expect_SweeperException() {
		assertThrowsExactly(SweeperException.class, () -> sweeper.execute(createContext(VALID_BYTECODE, 0)));
	}

	@Test
	public void Given_Sweeper_When_OffsetToIncompleteThrow_Expect_SweeperException() {
		final byte[] incompleteThrow = Arrays.copyOf(SHORT_VALID_BYTECODE, 1);
		assertThrowsExactly(SweeperException.class, () -> sweeper.execute(createContext(incompleteThrow, 0)));
	}

	@Test
	public void Given_Sweeper_When_CannotCheckAllSizes_Expect_AllSequences() throws StageException, InvalidConfigException {

		config.getSweeperConfig().setMaxNumberInstructions(MEDIUM_VALID_BYTECODE_AMOUNT_INSTRUCTIONS + 1);
		final PipelineContext context = createContext(MEDIUM_VALID_BYTECODE, MEDIUM_VALID_BYTECODE_THROW_OFFSET);
		sweeper.execute(context);
		final SweeperInfo info = context.getResult(SweeperInfo.class.getSimpleName());
		final ImmutableList<@NonNull ImmutableList<@NonNull DecompiledInstruction>> sequences = info
				.getInstructionSequences();

		// Expecting all sizes between 1 and max amount instructions in bytecode
		boolean[] exist = new boolean[MEDIUM_VALID_BYTECODE_AMOUNT_INSTRUCTIONS];
		for (int i = 0; i < exist.length; i++) {
			exist[i] = false;
		}

		for (final ImmutableList<@NonNull DecompiledInstruction> sequence : sequences) {
			exist[sequence.size() - 1] = true;
		}

		for (int i = 0; i < exist.length; i++) {
			assertTrue(exist[i]);
		}
	}

	@Test
	public void Given_Sweeper_When_EmptyBuffer_Expect_SweeperException() {
		assertThrowsExactly(SweeperException.class, () -> sweeper.execute(createContext(new byte[0], 0)));
	}
}