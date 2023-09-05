package com.topper.tests.dex.decompilation.decompiler;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.instruction.DualReferenceInstruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.reference.Reference.InvalidReferenceException;
import org.jf.util.ExceptionWithContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.topper.configuration.TopperConfig;
import com.topper.dex.decompiler.DecompilationResult;
import com.topper.dex.decompiler.Decompiler;
import com.topper.dex.decompiler.SmaliDecompiler;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;
import com.topper.exceptions.InvalidConfigException;
import com.topper.tests.utility.DexLoader;
import com.topper.tests.utility.TestConfig;

public class TestSmaliDecompiler {

	private static TopperConfig config;
	
	private static Opcodes opcodes;

	private static final byte @NonNull [] VALID_BYTECODE = new byte[] { 0x54, 0x30, 0x24, 0x8, 0x1d, 0x0, 0x52, 0x31,
			0x25, 0x8, (byte) 0xd8, 0x1, 0x1, (byte) 0xff, 0x59, 0x31, 0x25, 0x8, 0x12, (byte) 0xf2, 0x33, 0x21, 0xb,
			0x0, 0x54, 0x31, 0x23, 0x8, 0x38, 0x1, 0x7, 0x0, 0x6e, 0x10, (byte) 0xf0, 0x14, 0x3, 0x0, 0x28, 0x2, 0xd,
			0x1, 0x1e, 0x0, 0xe, 0x0, 0xd, 0x1, 0x1e, 0x0, 0x27, 0x1, 0x3, 0x0, 0x0, 0x0, 0xb, 0x0, 0x1, 0x0, 0x10, 0x0,
			0x0, 0x0, 0x3, 0x0, 0x3, 0x0, 0x15, 0x0, 0x0, 0x0, 0x4, 0x0, 0x1, 0x0, 0x2, 0x0, 0x17, 0x7f, (byte) 0x91,
			0x1, 0x14, 0x17, 0x2, 0x0, 0x1, 0x0, 0x1, 0x0, 0x0, 0x0, (byte) 0xfb, 0xd, 0x2f, 0x0, 0x18, 0x0, 0x0, 0x0,
			0x54, 0x10, 0x23, 0x8, 0x38, 0x0, 0x12, 0x0, 0x54, 0x10, 0x23, 0x8 };

	private static final byte @NonNull [] INVALID_OPCODE_BYTECODE = new byte[] { (byte) 0x3e, // unused
			0x42 // random follow - up, as instruction are at least 2 bytes in size
	};
	
	private static final byte @NonNull [] INCOMPLETE_NOP = new byte [] { 0x0 };

	private static byte[] OOB_BYTECODE;
	private static byte[] INVALID_INSTRUCTION_BYTECODE;
	private static byte[] VALID_BYTECODE_INVALID_END;
	private static byte[] INVALID_START_VALID_BYTECODE;
	
	private static byte[] DEX_FILE_BYTECODE;
	private static DexBackedDexFile DEX_FILE;
	
	@BeforeAll
	public static void init() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, IOException, InvalidConfigException {
		config = TestConfig.getDefault();
		opcodes = Opcodes.forDexVersion(config.getDecompilerConfig().getDexVersion());
		DEX_FILE_BYTECODE = DexLoader.get().getMethodBytes();
		DEX_FILE = DexLoader.get().getFile();
		
		OOB_BYTECODE = new byte[] {
			opcodes.getOpcodeValue(Opcode.INVOKE_VIRTUAL_RANGE).byteValue(), // requires at least 3 more bytes
			0x42 // 0x42 arguments
		};
		
		INVALID_INSTRUCTION_BYTECODE = new byte[] {
			opcodes.getOpcodeValue(Opcode.CONST_STRING_JUMBO).byteValue(), 0x0, // destination register
			(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff // string index
		};
		
		VALID_BYTECODE_INVALID_END = concatBytes(VALID_BYTECODE, OOB_BYTECODE);
		INVALID_START_VALID_BYTECODE = concatBytes(INVALID_OPCODE_BYTECODE, VALID_BYTECODE);
	}
	
	private static final byte @NonNull [] concatBytes(final byte @NonNull [] first, final byte @NonNull [] second) {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			out.write(first);
			out.write(second);
		} catch (IOException e) {
		}
		return out.toByteArray();
	}

	private void checkResult(@NonNull final DecompilationResult result, final byte @NonNull [] bytecode,
			final boolean checkOpcode) {
		
		// Compare buffers
		assertArrayEquals(bytecode, result.getBuffer().getBuf());

		DecompiledInstruction insn;
		DecompiledInstruction next;
		for (int i = 0; i < result.getInstructions().size() - 1; i++) {
			insn = result.getInstructions().get(i);
			next = result.getInstructions().get(i + 1);

			// Instructions must be adjacent.
			// Instructions must exactly cover the given bytes.
			assertArrayEquals(Arrays.copyOfRange(bytecode, insn.getOffset(), next.getOffset()), insn.getByteCode());

			// Ensure that instruction opcode matches byte in bytecode
			// --> tries to mitigate silent decompilation errors
			if (checkOpcode) {
				assertEquals(bytecode[insn.getOffset()],
						opcodes.getOpcodeValue(insn.getInstruction().getOpcode()).byteValue());
			}
		}

		// Cover last instruction.
		insn = result.getInstructions().get(result.getInstructions().size() - 1);
		assertArrayEquals(Arrays.copyOfRange(bytecode, insn.getOffset(), bytecode.length), insn.getByteCode());
		
		if (checkOpcode) {
			assertEquals(bytecode[insn.getOffset()], opcodes.getOpcodeValue(insn.getInstruction().getOpcode()).byteValue());
		}
	}

	@Test
	public void Given_EmptyBytecode_When_Decompiling_Expect_EmptyInstructionList() {
		// Reason: No bytes mean no instructions.

		config.getDecompilerConfig().setNopUnknownInstruction(false);
		final Decompiler decompiler = new SmaliDecompiler();
		final DecompilationResult result = decompiler.decompile(new byte[0], null, config);
		assertEquals(0, result.getInstructions().size());
	}

	@Test
	public void Given_InvalidOpcode_When_Decompiling_Expect_ExceptionWithContext() {
		// Reason: With RawFile, invalid opcodes are very likely.

		config.getDecompilerConfig().setNopUnknownInstruction(false);
		final Decompiler decompiler = new SmaliDecompiler();
		assertThrowsExactly(ExceptionWithContext.class,
				() -> decompiler.decompile(INVALID_OPCODE_BYTECODE, null, config));
	}
	
	@Test
	public void Given_InvalidOpcode_When_DecompilingWithNopUnknown_Expect_AllInstructions() {
		// Reason: Interpreters may treat unknown instructions as NOP.
		
		config.getDecompilerConfig().setNopUnknownInstruction(true);
		final Decompiler decompiler = new SmaliDecompiler();
		final DecompilationResult result = decompiler.decompile(INVALID_OPCODE_BYTECODE, null, config);
		
		this.checkResult(result, INVALID_OPCODE_BYTECODE, false);
	}

	@Test
	public void Given_OOBInstruction_When_Decompiling_Expect_ArrayIndexOutOfBoundsException() {
		// Reason: With RawFile, invalid instructions are very likely. An instruction
		// may go beyond the buffer.

		config.getDecompilerConfig().setNopUnknownInstruction(false);
		final Decompiler decompiler = new SmaliDecompiler();
		assertThrowsExactly(ArrayIndexOutOfBoundsException.class,
				() -> decompiler.decompile(OOB_BYTECODE, null, config/* or true */));
	}

	@Test
	public void Given_InvalidInstruction_When_Decompiling_Expect_ExceptionWithContext() {
		// Reason: With RawFile, invalid instruction are very likely. E.g. too large
		// integer values.

		config.getDecompilerConfig().setNopUnknownInstruction(false);
		final Decompiler decompiler = new SmaliDecompiler();
		assertThrowsExactly(ExceptionWithContext.class,
				() -> decompiler.decompile(INVALID_INSTRUCTION_BYTECODE, null, config));
	}

	@Test
	public void Given_ValidBytecode_When_Decompiling_Expect_AllInstructions() {
		// Reason: Bytes of correct bytecode must be fully covered and decompiled.

		config.getDecompilerConfig().setNopUnknownInstruction(false);
		final Decompiler decompiler = new SmaliDecompiler();
		final DecompilationResult result = decompiler.decompile(VALID_BYTECODE, null, config/* or true */);

		this.checkResult(result, VALID_BYTECODE, true);
	}

	@Test
	public void Given_ValidBytecodeInvalidEnd_When_Decompiling_Expect_ArrayIndexOutOfBoundsException() {
		// Reason: Decompiler must throw when finding an invalid instruction byte.
		// Two Cases: Either out - of - bounds or context(e.g. integer too large)
		// exception

		config.getDecompilerConfig().setNopUnknownInstruction(false);
		final Decompiler decompiler = new SmaliDecompiler();
		assertThrowsExactly(ArrayIndexOutOfBoundsException.class,
				() -> decompiler.decompile(VALID_BYTECODE_INVALID_END, null, config));
	}

	@Test
	public void Given_InvalidStartValidBytecode_When_DecompilingWithNopUnknown_Expect_AllInstructions() {
		// Reason: Decompiler must replace (first) invalid instruction with a NOP when requested.

		config.getDecompilerConfig().setNopUnknownInstruction(true);
		final Decompiler decompiler = new SmaliDecompiler();
		final DecompilationResult result = decompiler.decompile(INVALID_START_VALID_BYTECODE, null, config);

		this.checkResult(result, INVALID_START_VALID_BYTECODE, false);
	}
	
	@Test
	public void Given_OddAmountBytes_When_Decompiling_Expect_IllegalArgumentException() {
		// Reason: Decompiler must only consider an even amount of bytes for decompilation.
		
		config.getDecompilerConfig().setNopUnknownInstruction(false);
		final Decompiler decompiler = new SmaliDecompiler();
		assertThrowsExactly(IllegalArgumentException.class, () -> decompiler.decompile(INCOMPLETE_NOP, null, config)); 
	}
	
	@Test
	public void Given_ValidBytecode_When_DecompilingAugmented_Expect_AllInstructionsAugmented() throws InvalidReferenceException {
		// Reason: Decompiler must take into account a given .dex file, if requested.
		// All references must be valid!
		
		config.getDecompilerConfig().setNopUnknownInstruction(false);
		final Decompiler decompiler = new SmaliDecompiler();
		final DecompilationResult result = decompiler.decompile(DEX_FILE_BYTECODE, DEX_FILE, config);
		
		this.checkResult(result, DEX_FILE_BYTECODE, true);
		
		// Check references
		for (@NonNull final DecompiledInstruction insn : result.getInstructions()) {
			
			if (ReferenceInstruction.class.isAssignableFrom(insn.getInstruction().getClass())) {
				final ReferenceInstruction in = (ReferenceInstruction) insn.getInstruction();
				assertNotNull(in.getReference());
				in.getReference().validateReference();
			} else if (DualReferenceInstruction.class.isAssignableFrom(insn.getInstruction().getClass())) {
				final DualReferenceInstruction in = (DualReferenceInstruction) insn.getInstruction();
				assertNotNull(in.getReference());
				assertNotNull(in.getReference2());
				in.getReference().validateReference();
				in.getReference2().validateReference();
			}
		}
	}
	
	@Test
	public void Given_ValidBytecode_When_DecompilingWithNopWrongAugmentation_Expect_AllInstructionsMaybeAugmented() throws IOException, InvalidConfigException {
		// Reason: Decompiler must resolve as many references as possible given a context (augmentation file).
		// If resolution does not work, then it may be ignored.
		// Observe: Nopping unknown instructions should be used here to reflect what is done in practice. I.e.
		// using different .dex files to see what a gadget does in different contexts.
		
		config.getDecompilerConfig().setNopUnknownInstruction(true);
		final Decompiler decompiler = new SmaliDecompiler();
		final DecompilationResult result = decompiler.decompile(DEX_FILE_BYTECODE, DexLoader.get().loadFile("./src/test/java/resources/classes9.dex"), config);
		
		this.checkResult(result, DEX_FILE_BYTECODE, false);
	}
	
	@Test
	public void Given_ValidBytecode_When_DecompilingWrongAugmentation_Expect_AllInstructionsMaybeAugmented() throws IOException, InvalidConfigException {
		// Reason: Decompiler must resolve as many references as possible given a context (augmentation file).
		// If resolution does not work, then it may be ignored. No nopping for completeness sake.
		
		config.getDecompilerConfig().setNopUnknownInstruction(false);
		final Decompiler decompiler = new SmaliDecompiler();
		final DecompilationResult result = decompiler.decompile(DEX_FILE_BYTECODE, DexLoader.get().loadFile("./src/test/java/resources/classes9.dex"), config);
		
		this.checkResult(result, DEX_FILE_BYTECODE, true);
	}
}