package com.topper.tests.dex.decompilation.staticanalyser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.DecompilationResult;
import com.topper.dex.decompilation.decompiler.Decompiler;
import com.topper.dex.decompilation.decompiler.SmaliDecompiler;
import com.topper.dex.decompilation.graphs.BasicBlock;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompilation.staticanalyser.BFSCFGAnalyser;
import com.topper.dex.decompilation.staticanalyser.CFGAnalyser;
import com.topper.dex.decompilation.staticanalyser.StaticAnalyser;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public class TestBFSCFGAnalyser {

	private static ImmutableList<@NonNull DecompiledInstruction> validInstructions;

	private static final String FIELD_NAME_CODE_OFFSET = "codeOffset";
	private static final String dexName = "./tests/resources/classes7.dex";
	private static final String clsNameCorrect = "Lcom/damnvulnerableapp/networking/messages/PlainMessageParser;";
	private static final String methodNameCorrect = "parseFromBytes";

	private static int getCodeOffset(final DexBackedMethod method)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		final Field offsetField = method.getClass().getDeclaredField(FIELD_NAME_CODE_OFFSET);
		offsetField.setAccessible(true);
		final int offset = offsetField.getInt(method);
		offsetField.setAccessible(false);
		return offset;
	}

	@BeforeAll
	public static final void loadInstructions() throws IOException, IllegalArgumentException, IllegalAccessException,
			NoSuchFieldException, SecurityException {

		final DexBackedDexFile file = DexFileFactory.loadDexFile(dexName, Opcodes.getDefault());
		for (@NonNull
		final DexBackedClassDef cls : file.getClasses()) {

			if (!clsNameCorrect.equals(cls.getType())) {
				continue;
			}

			for (@NonNull
			final DexBackedMethod method : cls.getMethods()) {

				if (!methodNameCorrect.equals(method.getName())) {
					continue;
				}

				final int offset = getCodeOffset(method);

				final Decompiler decompiler = new SmaliDecompiler();
				final DecompilationResult result = decompiler
						.decompile(file.getBuffer().readByteRange(offset, method.getSize()));
				validInstructions = result.getInstructions();
			}
		}

		assertNotNull(validInstructions);
	}

	private static final BFSCFGAnalyser createAnalyser() {
		return new BFSCFGAnalyser();
	}

	@Test
	public void Given_ValidBytecode_When_AnalyseValidBytecode_Expect_ValidCFG() {

		final CFGAnalyser analyser = createAnalyser();
		final CFG cfg = analyser.extractCFG(validInstructions, 0x10);

		System.out.println(cfg);
	}

	@Test
	public void Given_ValidBytecode_When_AnalyseValidBytecode_Expect_CorrectLookups() {

		final CFGAnalyser analyser = createAnalyser();
		final CFG cfg = analyser.extractCFG(validInstructions, 0x10);

		int i = 0;
		for (final BasicBlock block : cfg.getGraph().nodes()) {

			for (final DecompiledInstruction insn : block.getInstructions()) {

				
				assertEquals(insn, cfg.getInstruction(insn.getOffset()), "" + i);
				assertEquals(block, cfg.getBlock(insn), insn.toString() + " (i = " + i + ")");
				i += 1;
			}
		}
	}

	@Test
	public void Given_ValidBytecode_When_AnalyseValidBytecode_Expect_AtLeastOneTargetUnlessThrowReturn() {

		final CFGAnalyser analyser = createAnalyser();
		final CFG cfg = analyser.extractCFG(validInstructions, 0x10);

		final int largestOffset = cfg.getOffsetInstructionLookup().lastKey()
				+ cfg.getOffsetInstructionLookup().lastEntry().getValue().getByteCode().length;

		Set<BasicBlock> targets;
		DecompiledInstruction instruction;
		Opcode opcode;
		for (final BasicBlock block : cfg.getGraph().nodes()) {

			targets = cfg.getGraph().successors(block);
			instruction = block.getInstructions().get(block.getInstructions().size() - 1);
			
			opcode = instruction.getInstruction().getOpcode();
			if (StaticAnalyser.isGoto(opcode)) {
				assertEquals(1, targets.size(), instruction.toString());
			} else if (StaticAnalyser.isIf(opcode)) {
				assertEquals(2, targets.size(), instruction.toString());
			} else if (StaticAnalyser.isSwitch(opcode)) {
				assertTrue(targets.size() >= 1, instruction.toString());
			} else {
				if (instruction.getOffset() + instruction.getByteCode().length == largestOffset ||
						StaticAnalyser.isThrow(opcode) || StaticAnalyser.isReturn(opcode)) {
					assertEquals(0, targets.size(), instruction.toString());
				} else {
					assertEquals(1, targets.size(), instruction.toString());
				}
			}
		}
	}
}