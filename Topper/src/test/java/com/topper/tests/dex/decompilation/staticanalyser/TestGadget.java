package com.topper.tests.dex.decompilation.staticanalyser;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.TopperConfig;
import com.topper.dex.decompilation.decompiler.Decompiler;
import com.topper.dex.decompilation.decompiler.SmaliDecompiler;
import com.topper.dex.decompilation.staticanalyser.Gadget;
import com.topper.exceptions.InvalidConfigException;
import com.topper.tests.utility.TestConfig;

public class TestGadget {

	private static TopperConfig config;

	@BeforeAll
	public static void init() throws InvalidConfigException {
		config = TestConfig.getDefault();
	}

	@Test
	public void Given_EmptyList_When_CreatingGadget_Expect_IllegalArgumentException() {
		// Reason: Gadgets must be made up of at least a pivot instruction.

		assertThrowsExactly(IllegalArgumentException.class, () -> new Gadget(ImmutableList.of(), null, null));
	}

	@Test
	public void Given_NoPivot_When_CreatingGadget_Expect_IllegalArgumentException() {
		// Reason: Gadgets must end with a pivot instruction.

		final Decompiler decompiler = new SmaliDecompiler();
		assertThrowsExactly(IllegalArgumentException.class,
				() -> new Gadget(decompiler.decompile(new byte[] { 0, 0 }, null, config).getInstructions(), null,
						null));
	}
}