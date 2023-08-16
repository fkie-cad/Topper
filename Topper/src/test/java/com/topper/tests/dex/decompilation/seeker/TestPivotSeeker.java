package com.topper.tests.dex.decompilation.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.TopperConfig;
import com.topper.dex.decompilation.pipeline.PipelineArgs;
import com.topper.dex.decompilation.pipeline.PipelineContext;
import com.topper.dex.decompilation.pipeline.SeekerInfo;
import com.topper.dex.decompilation.seeker.PivotSeeker;
import com.topper.exceptions.DuplicateInfoIdException;
import com.topper.exceptions.InvalidConfigException;
import com.topper.exceptions.MissingStageInfoException;
import com.topper.tests.utility.DexLoader;
import com.topper.tests.utility.TestConfig;

public class TestPivotSeeker {

	private static TopperConfig config;

	private static byte[] INCOMPLETE_PIVOT;
	private static byte[] PIVOT_ONLY;
	
	private static final void checkOffsets(final byte @NonNull [] buffer, @NonNull final ImmutableList<Integer> offsets, @NonNull final PipelineContext context) {
		
		final Opcode pivot = context.getArgs().getConfig().getSweeperConfig().getPivotOpcode();
		final int size = pivot.format.size;
		
		for (final int offset : offsets) {
			assertTrue(offset + size <= buffer.length);
			assertTrue(offset >= 0);
			assertTrue(size >= 0);
		}
	}

	@NonNull
	private static final PipelineContext createContext(final byte @NonNull [] bytecode) throws NoSuchFieldException,
			SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException {
		final PipelineArgs args = new PipelineArgs(config, bytecode);
		return new PipelineContext(args);
	}

	@BeforeAll
	public static void init() throws InvalidConfigException {
		config = TestConfig.getDefault();

		final Opcode pivot = config.getSweeperConfig().getPivotOpcode();
		final Opcodes opcodes = Opcodes.forDexVersion(config.getDecompilerConfig().getDexVersion());
		INCOMPLETE_PIVOT = new byte[] { (byte) (opcodes.getOpcodeValue(pivot) & 0xff) };

		PIVOT_ONLY = new byte[] { (byte) (opcodes.getOpcodeValue(pivot) & 0xff), 0x00 };

	}

	@Test
	public void Given_IncompleteOpcode_When_Seeking_Expect_EmptyOffsetList()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException,
			InvalidConfigException, IOException, DuplicateInfoIdException, MissingStageInfoException {
		// Reason: If the last byte of a buffer matches the pivot opcode,
		// then it must be ignored, as instructions are at least two bytes in size.

		final PipelineContext c = createContext(INCOMPLETE_PIVOT);
		final PivotSeeker s = new PivotSeeker();
		s.execute(c);

		final SeekerInfo info = c.getSeekerInfo(SeekerInfo.class.getSimpleName());
		assertEquals(0, info.getPivotOffsets().size());
	}

	@Test
	public void Given_EmptyBytecode_When_Seeking_Expect_EmptyOffsetList()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException,
			InvalidConfigException, IOException, MissingStageInfoException, DuplicateInfoIdException {
		// Reason: If given buffer is empty, then it must not hallucinate pivot opcodes.

		final PipelineContext c = createContext(new byte[0]);
		final PivotSeeker s = new PivotSeeker();
		s.execute(c);

		final SeekerInfo info = c.getSeekerInfo(SeekerInfo.class.getSimpleName());
		assertEquals(0, info.getPivotOffsets().size());
	}

	@Test
	public void Given_PivotOpcode_When_Seeking_Expect_OffsetOfPivotOpcode()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException,
			InvalidConfigException, IOException, DuplicateInfoIdException, MissingStageInfoException {
		// Reason: If only the pivot instruction is given, it must be recognized
		// (mitigate off by one in bounds checks).

		final PipelineContext c = createContext(PIVOT_ONLY);
		final PivotSeeker s = new PivotSeeker();
		s.execute(c);

		final SeekerInfo info = c.getSeekerInfo(SeekerInfo.class.getSimpleName());
		assertEquals(1, info.getPivotOffsets().size());
		checkOffsets(PIVOT_ONLY, info.getPivotOffsets(), c);
	}

	@Test
	public void Given_ValidBytecode_When_Seeking_Expect_AllFittingOffsets() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException, MissingStageInfoException, DuplicateInfoIdException {
		// Reason: Given some large buffer, find all potential, fitting pivot instructions.
		
		final byte[] buffer = DexLoader.get().getMethodBytes();
		final PipelineContext c = createContext(buffer);
		final Opcode pivot = c.getArgs().getConfig().getSweeperConfig().getPivotOpcode();
		final byte val = (byte)(Opcodes.forDexVersion(c.getArgs().getConfig().getDecompilerConfig().getDexVersion()).getOpcodeValue(pivot) & 0xff);
		final PivotSeeker s = new PivotSeeker();
		s.execute(c);

		final SeekerInfo info = c.getSeekerInfo(SeekerInfo.class.getSimpleName());
		
		// Observe: Only last bytes in buffer might be incomplete pivots.
		int matches = 0;
		for (int i = 0; i < buffer.length - pivot.format.size + 1; i++) {
			if (buffer[i] == val) {
				matches += 1;
			}
		}
		
		assertEquals(matches, info.getPivotOffsets().size());
		checkOffsets(buffer, info.getPivotOffsets(), c);
	}
}