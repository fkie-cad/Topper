package com.topper.tests.dex.decompilation.staticanalyser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.OffsetInstruction;
import org.jf.dexlib2.iface.instruction.SwitchPayload;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.decompiler.DecompilationResult;
import com.topper.dex.decompilation.decompiler.Decompiler;
import com.topper.dex.decompilation.decompiler.SmaliDecompiler;
import com.topper.dex.decompilation.graphs.BasicBlock;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompilation.staticanalyser.BFSCFGAnalyser;
import com.topper.dex.decompilation.staticanalyser.CFGAnalyser;
import com.topper.dex.decompilation.staticanalyser.StaticAnalyser;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;
import com.topper.exceptions.InvalidConfigException;
import com.topper.tests.utility.DexLoader;
import com.topper.tests.utility.TestConfig;

public class TestBFSCFGAnalyser {

	private static ImmutableList<@NonNull DecompiledInstruction> validInstructions;

	@BeforeAll
	public static final void loadInstructions() throws IOException, IllegalArgumentException, IllegalAccessException,
			NoSuchFieldException, SecurityException, InvalidConfigException {
		
		final byte[] bytes = DexLoader.get().getMethodBytes();
		assertNotNull(bytes);
		
		final Decompiler decompiler = new SmaliDecompiler();
		final DecompilationResult result = decompiler.decompile(bytes, null, TestConfig.getDefault());
		validInstructions = result.getInstructions();
		assertNotNull(validInstructions);
	}

	private static final BFSCFGAnalyser createAnalyser() {
		return new BFSCFGAnalyser();
	}

	private static final int getAmountTargets(@NonNull final CFG cfg, final int target,
			@NonNull final DecompiledInstruction instruction) {
		return extractBranchTargets(cfg, target, instruction).size();
	}

	@NonNull
	private static final List<Integer> extractBranchTargets(@NonNull final CFG cfg, final int target,
			@NonNull final DecompiledInstruction instruction) {

		List<Integer> branchTargets;

		if (cfg.getOffsetInstructionLookup().containsKey(target)) {

			final DecompiledInstruction targetInstruction = cfg.getInstruction(target);
			if (targetInstruction == null) {
				return new ArrayList<>();
			}

			// Ensure that switch and payload match
			final OffsetInstruction insn = (OffsetInstruction) instruction.getInstruction();
			if ((insn.getOpcode().equals(Opcode.PACKED_SWITCH)
					&& targetInstruction.getInstruction().getOpcode().equals(Opcode.PACKED_SWITCH_PAYLOAD))
					|| (insn.getOpcode().equals(Opcode.SPARSE_SWITCH)
							&& targetInstruction.getInstruction().getOpcode().equals(Opcode.SPARSE_SWITCH_PAYLOAD))) {
				// packed-/sparse - switch
				final SwitchPayload payload = (SwitchPayload) targetInstruction.getInstruction();

				// Iterate over switch elements an compute target offsets
				// relative to beginning of instructions
				branchTargets = new ArrayList<Integer>(payload.getSwitchElements().size());
				payload.getSwitchElements().stream().mapToInt(e -> e.getOffset() * 2 + instruction.getOffset())
						.forEach(branchTargets::add);

			} else if (StaticAnalyser.isIf(insn.getOpcode())) {
				// if-<cond> has two outgoing edges
				branchTargets = new ArrayList<Integer>(2);
				// Target of if statement (if body)
				branchTargets.add(target);
				// Instruction following if statement (else body)
				branchTargets.add(instruction.getOffset() + instruction.getByteCode().length);
			} else if (StaticAnalyser.isGoto(insn.getOpcode())) {
				// goto
				branchTargets = new ArrayList<>(1);
				branchTargets.add(target);
			} else {
				branchTargets = new ArrayList<>();
			}
		} else {
			branchTargets = new ArrayList<>();
		}

		// Check whether targets are correct
		branchTargets.removeIf(t -> cfg.getInstruction(target) == null);

		return branchTargets;
	}

	@Test
	public void Given_ValidBytecode_When_AnalyseValidBytecode_Expect_ValidCFG() {

		final CFGAnalyser analyser = createAnalyser();
		final CFG cfg = analyser.extractCFG(validInstructions, 0x10);
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
		int target;
		for (final BasicBlock block : cfg.getGraph().nodes()) {

			targets = cfg.getGraph().successors(block);
			instruction = block.getBranchInstruction();

			if (OffsetInstruction.class.isAssignableFrom(instruction.getInstruction().getClass())) {
				target = instruction.getOffset()
						+ 2 * ((OffsetInstruction) instruction.getInstruction()).getCodeOffset();
				assertEquals(getAmountTargets(cfg, target, instruction), targets.size(), instruction.toString());
			} else {
				opcode = instruction.getInstruction().getOpcode();
				if (StaticAnalyser.isThrow(opcode) || StaticAnalyser.isReturn(opcode)) {
					assertEquals(0, targets.size(), instruction.toString());
				} else {
					target = instruction.getOffset() + instruction.getByteCode().length;
					assertEquals(cfg.getInstruction(target) != null ? 1 : 0, targets.size(), instruction.toString());
				}
			}
		}
	}

	@Test
	public void Given_ValidBytecode_When_OffsetInvalid_Expect_IllegalArgumentException() {

		final CFGAnalyser analyser = createAnalyser();
		final DecompiledInstruction insn = validInstructions.get(validInstructions.size() - 1);
		final int oob = insn.getOffset() + insn.getByteCode().length;
		assertThrowsExactly(IllegalArgumentException.class, () -> analyser.extractCFG(validInstructions, oob));
	}

	@Test
	public void Given_ValidBytecode_When_OffsetNegative_Expect_IllegalArgumentException() {

		final CFGAnalyser analyser = createAnalyser();
		final int oob = -1;
		assertThrowsExactly(IllegalArgumentException.class, () -> analyser.extractCFG(validInstructions, oob));
	}

	@Test
	public void Given_EmptyBytecode_When_EmptyBytecode_Expect_IllegalArgumentException() {

		final CFGAnalyser analyser = createAnalyser();
		final int oob = -1;
		assertThrowsExactly(IllegalArgumentException.class, () -> analyser.extractCFG(ImmutableList.of(), oob));
	}

	@Test
	public void Given_ValidBytecode_When_AnalyseFromMethodEntry_Expect_ExactlyOneBasicBlockAtEntry() {

		final int entry = 0x0;
		final CFGAnalyser analyser = createAnalyser();
		final CFG cfg = analyser.extractCFG(validInstructions, entry);

		assertTrue(cfg.getGraph().nodes().stream().anyMatch(bb -> bb.getOffset() == entry));
	}

	@Test
	public void Given_ValidBytecode_When_AnalyseFromMethodMiddle_Expect_ExactlyOneBasicBlockAtEntry() {

		final int entry = validInstructions.get(validInstructions.size() / 2).getOffset();
		final CFGAnalyser analyser = createAnalyser();
		final CFG cfg = analyser.extractCFG(validInstructions, entry);

		assertTrue(cfg.getGraph().nodes().stream().anyMatch(bb -> bb.getOffset() == entry));
	}

}