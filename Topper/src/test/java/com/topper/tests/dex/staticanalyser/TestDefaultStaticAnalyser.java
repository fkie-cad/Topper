package com.topper.tests.dex.staticanalyser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.TopperConfig;
import com.topper.dex.graphs.CFG;
import com.topper.dex.graphs.DFG;
import com.topper.dex.pipeline.PipelineArgs;
import com.topper.dex.pipeline.PipelineContext;
import com.topper.dex.pipeline.SeekerInfo;
import com.topper.dex.pipeline.StaticInfo;
import com.topper.dex.pipeline.SweeperInfo;
import com.topper.dex.seeker.PivotSeeker;
import com.topper.dex.seeker.Seeker;
import com.topper.dex.staticanalyser.DefaultStaticAnalyser;
import com.topper.dex.staticanalyser.Gadget;
import com.topper.dex.staticanalyser.StaticAnalyser;
import com.topper.dex.sweeper.BackwardLinearSweeper;
import com.topper.dex.sweeper.Sweeper;
import com.topper.exceptions.InvalidConfigException;
import com.topper.exceptions.pipeline.DuplicateInfoIdException;
import com.topper.exceptions.pipeline.MissingStageInfoException;
import com.topper.exceptions.pipeline.StageException;
import com.topper.tests.utility.DexLoader;
import com.topper.tests.utility.TestConfig;

public class TestDefaultStaticAnalyser {

	private static TopperConfig config;

	@NonNull
	private static final PipelineArgs createArgs(final byte @NonNull [] bytecode) {
		return new PipelineArgs(config, bytecode);
	}

	@NonNull
	private static final PipelineContext createContext(final byte @NonNull [] bytecode)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException,
			InvalidConfigException, IOException, StageException {
		final PipelineArgs args = createArgs(bytecode);
		final Seeker seeker = new PivotSeeker();
		final PipelineContext context = new PipelineContext(args);
		seeker.execute(context);
		final Sweeper sweeper = new BackwardLinearSweeper();
		sweeper.execute(context);
		return context;
	}

	private static final StaticAnalyser create() {
		return new DefaultStaticAnalyser();
	}

	@BeforeAll
	public static void init() throws InvalidConfigException {
		config = TestConfig.getDefault();
	}

	@BeforeEach
	public void reset() {
		config.getStaticAnalyserConfig().setSkipCFG(false);
		config.getStaticAnalyserConfig().setSkipDFG(false);
	}

	public static final void verifyResults(@NonNull final PipelineContext context, @NonNull final TopperConfig config)
			throws MissingStageInfoException {
		// Assumption: Static analyser has been executed successfully.

		final PipelineArgs args = context.getArgs();
		assertNotNull(args);

		final SeekerInfo seekerInfo = context.getSeekerInfo(SeekerInfo.class.getSimpleName());
		assertNotNull(seekerInfo);

		final SweeperInfo sweeperInfo = context.getSweeperInfo(SweeperInfo.class.getSimpleName());
		assertNotNull(sweeperInfo);

		final StaticInfo staticInfo = context.getStaticInfo(StaticInfo.class.getSimpleName());
		assertNotNull(staticInfo);

		for (@NonNull
		final Gadget gadget : staticInfo.getGadgets()) {

			verifyGadget(gadget, config, context);
		}
	}

	public static final void verifyGadget(@NonNull final Gadget gadget, @NonNull final TopperConfig config,
			@NonNull final PipelineContext context) throws MissingStageInfoException {

		final PipelineArgs args = context.getArgs();
		final SeekerInfo seekerInfo = context.getSeekerInfo(SeekerInfo.class.getSimpleName());
		final SweeperInfo sweeperInfo = context.getSweeperInfo(SweeperInfo.class.getSimpleName());
		final StaticInfo staticInfo = context.getStaticInfo(StaticInfo.class.getSimpleName());
		
		// Each gadget must at least contain the pivot instruction
		assertTrue(gadget.getInstructions().size() >= 1);

		// Completeness: Check that each gadget covers an instruction sequence of the
		// sweeper
		assertEquals(1, sweeperInfo.getInstructionSequences().stream()
				.filter(sequence -> sequence.equals(gadget.getInstructions())).count());

		// Seeker offsets must contain precisely one instruction from gadget, i.e. the
		// pivot gadget.
		assertEquals(1, seekerInfo.getPivotOffsets().stream().filter(offset -> gadget.getInstructions().stream()
				.mapToInt(insn -> insn.getOffset()).anyMatch(o -> o == offset)).count());

		// Correctness: Check that basic block and entry coincide.
		final CFG cfg = gadget.getCFG();

		// Check: shouldSkipCFG <=> (cfg == null)
		assertEquals(config.getStaticAnalyserConfig().shouldSkipCFG(), cfg == null);

		if (cfg != null) {
			assertTrue(gadget.hasCFG()); // cfg != null
			assertEquals(gadget.getInstructions().get(0).getOffset(), cfg.getEntry());
			assertEquals(1, cfg.getGraph().nodes().stream().filter(bb -> bb.getOffset() == cfg.getEntry()).count());
		}

		final DFG dfg = gadget.getDFG();
		assertEquals(config.getStaticAnalyserConfig().shouldSkipDFG(), dfg == null);

		if (dfg != null) {
			assertTrue(gadget.hasDFG());
		}
	}

	@Test
	public void Given_PartialContext_When_Executing_Expect_MissingStageInfoException() throws NoSuchFieldException,
			SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException {
		// Reason: Static analyser must verify that required information is available.

		// Seeker and Sweeper is missing!
		final PipelineArgs args = new PipelineArgs(config, DexLoader.get().getMethodBytes());
		final PipelineContext context = new PipelineContext(args);

		final StaticAnalyser analyser = create();
		assertThrowsExactly(MissingStageInfoException.class, () -> analyser.execute(context));
	}

	@Test
	public void Given_ValidContext_When_ExecutingAll_Expect_ValidStaticInfo()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException,
			InvalidConfigException, IOException, StageException {
		// Reason: Static analyser must add a valid result to context.

		final byte[] buffer = DexLoader.get().getMethodBytes();
		final PipelineContext c = createContext(buffer);

		final StaticAnalyser analyser = create();

		// Must not throw
		analyser.execute(c);

		verifyResults(c, config);
	}

	@Test
	public void Given_ValidContext_When_ExecutingNoCFG_Expect_CFGNullValid()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException,
			InvalidConfigException, IOException, StageException {
		// Reason: Explicitly ignoring CFG extraction via configuration must not
		// interrupt analysis.

		// No skipping
		config.getStaticAnalyserConfig().setSkipCFG(true);
		config.getStaticAnalyserConfig().setSkipDFG(false);

		final byte[] buffer = DexLoader.get().getMethodBytes();
		final PipelineContext c = createContext(buffer);

		final StaticAnalyser analyser = create();

		// Must not throw
		analyser.execute(c);

		verifyResults(c, config);
	}

	@Test
	public void Given_ValidContext_When_ExecutingNoDFG_Expect_DFGNullValid()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException,
			InvalidConfigException, IOException, StageException {
		// Reason: Explicitly ignoring DFG extraction via configuration must not
		// interrupt analysis.

		// No skipping
		config.getStaticAnalyserConfig().setSkipCFG(false);
		config.getStaticAnalyserConfig().setSkipDFG(true);

		final byte[] buffer = DexLoader.get().getMethodBytes();
		final PipelineContext c = createContext(buffer);

		final StaticAnalyser analyser = create();

		// Must not throw
		analyser.execute(c);

		verifyResults(c, config);
	}

	@Test
	public void Given_EmptyInstructionSequence_When_Executing_Expect_IllegalArgumentException()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException,
			InvalidConfigException, IOException, DuplicateInfoIdException {
		// Reason: Static analyser must check correctness of its inputs. If instruction
		// sequence
		// is missing a pivot instruction and is thus empty, something must have gone
		// wrong.

		// Setup input
		final byte[] buffer = DexLoader.get().getMethodBytes();
		final PipelineArgs args = createArgs(buffer);
		final SweeperInfo info = new SweeperInfo(ImmutableList.of(ImmutableList.of()));
		final PipelineContext context = new PipelineContext(args);
		context.putInfo(SweeperInfo.class.getSimpleName(), info);

		final StaticAnalyser analyser = create();

		assertThrowsExactly(IllegalArgumentException.class, () -> analyser.execute(context));
	}
}