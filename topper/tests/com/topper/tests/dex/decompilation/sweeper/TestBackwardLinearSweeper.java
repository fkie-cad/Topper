package com.topper.tests.dex.decompilation.sweeper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.ConfigManager;
import com.topper.dex.decompilation.sweeper.BackwardLinearSweeper;
import com.topper.dex.decompilation.sweeper.Sweeper;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;
import com.topper.exceptions.SweeperException;

public class TestBackwardLinearSweeper {
	
	private static final byte @NonNull [] VALID_BYTECODE = new byte[] { 0x54, 0x30, 0x24, 0x8, 0x1d, 0x0, 0x52, 0x31, 0x25, 0x8, (byte) 0xd8, 0x1, 0x1, (byte) 0xff, 0x59, 0x31, 0x25, 0x8, 0x12, (byte) 0xf2, 0x33, 0x21, 0xb, 0x0, 0x54, 0x31, 0x23, 0x8, 0x38, 0x1, 0x7, 0x0, 0x6e, 0x10, (byte) 0xf0, 0x14, 0x3, 0x0, 0x28, 0x2, 0xd, 0x1, 0x1e, 0x0, 0xe, 0x0, 0xd, 0x1, 0x1e, 0x0, 0x27, 0x1, 0x3, 0x0, 0x0, 0x0, 0xb, 0x0, 0x1, 0x0, 0x10, 0x0, 0x0, 0x0, 0x3, 0x0, 0x3, 0x0, 0x15, 0x0, 0x0, 0x0, 0x4, 0x0, 0x1, 0x0, 0x2, 0x0, 0x17, 0x7f, (byte) 0x91, 0x1, 0x14, 0x17, 0x2, 0x0, 0x1, 0x0, 0x1, 0x0, 0x0, 0x0, (byte) 0xfb, 0xd, 0x2f, 0x0, 0x18, 0x0, 0x0, 0x0, 0x54, 0x10, 0x23, 0x8, 0x38, 0x0, 0x12, 0x0, 0x54, 0x10, 0x23, 0x8};
	private static final int VALID_BYTECODE_THROW_OFFSET = 0x32;
	private static final int VALID_BYTECODE_NUMBER_INSTRUCTIONS_IN_GADGET = 17;

	private static final BackwardLinearSweeper createSweeper() {
		return new BackwardLinearSweeper();
	}
	
	@Test
	public void Given_Sweeper_When_ValidByteCodeAndOffset_Expect_NullPointerException() throws SweeperException {
		
		final Sweeper s = createSweeper();
		
		final ImmutableList<@NonNull ImmutableList<@NonNull DecompiledInstruction>> sequences
			= s.sweep(VALID_BYTECODE, VALID_BYTECODE_THROW_OFFSET);
		
		final int expectedNumber = Math.min(
				ConfigManager.getInstance().getConfig().getSweeperMaxNumberInstructions(),
				VALID_BYTECODE_NUMBER_INSTRUCTIONS_IN_GADGET
		);
		
		assertTrue(sequences.size() >= 1);
		
		for (final ImmutableList<DecompiledInstruction> i : sequences) {
			System.out.println(i);
		}
		assertEquals(expectedNumber, sequences.get(0).size());
	}
}