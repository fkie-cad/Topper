package com.topper.tests.dex.decompilation.staticanalyser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.topper.configuration.TopperConfig;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompilation.pipeline.DecompilationDriver;
import com.topper.dex.decompilation.pipeline.Pipeline;
import com.topper.dex.decompilation.pipeline.PipelineArgs;
import com.topper.dex.decompilation.pipeline.StageInfo;
import com.topper.dex.decompilation.pipeline.StaticInfo;
import com.topper.dex.decompilation.pipeline.SweeperInfo;
import com.topper.dex.decompilation.seeker.PivotSeeker;
import com.topper.dex.decompilation.staticanalyser.DefaultStaticAnalyser;
import com.topper.dex.decompilation.staticanalyser.Gadget;
import com.topper.dex.decompilation.staticanalyser.StaticAnalyser;
import com.topper.dex.decompilation.sweeper.BackwardLinearSweeper;
import com.topper.exceptions.InvalidConfigException;
import com.topper.exceptions.MissingStageInfoException;
import com.topper.exceptions.StageException;
import com.topper.tests.utility.DexLoader;
import com.topper.tests.utility.TestConfig;

public class TestDefaultStaticAnalyser {
	
	private static TopperConfig config;
	
	@BeforeAll
	public static void init() throws InvalidConfigException {
		config = TestConfig.getDefault();
	}

	private static final StaticAnalyser<@NonNull TreeMap<@NonNull String, @NonNull StageInfo>> create() {
		return new DefaultStaticAnalyser<@NonNull TreeMap<@NonNull String, @NonNull StageInfo>>();
	}

	/**
	 * Simulates {@link Pipeline} until reaching static analysis.
	 * @throws InvalidConfigException 
	 */
	private static final TreeMap<@NonNull String, @NonNull StageInfo> createResults() throws NoSuchFieldException,
			SecurityException, IllegalArgumentException, IllegalAccessException, IOException, StageException, InvalidConfigException {
		final PipelineArgs args = new PipelineArgs(config, DexLoader.get().getMethodBytes());
		
		final Pipeline<@NonNull TreeMap<@NonNull String, @NonNull StageInfo>> pipeline = new Pipeline(TreeMap::new);
		pipeline.addStage(new PivotSeeker());
		pipeline.addStage(new BackwardLinearSweeper());
		final DecompilationDriver driver = new DecompilationDriver();
		driver.setPipeline(pipeline);
		final TreeMap<@NonNull String, @NonNull StageInfo> results = (TreeMap<@NonNull String, @NonNull StageInfo>) driver.decompile(args).getResults();

		assertTrue(results.containsKey(PipelineArgs.class.getSimpleName()));
		assertTrue(results.containsKey(BackwardLinearSweeper.class.getSimpleName()));

		return results;
	}

	private static final void verifyResults(@NonNull final TreeMap<@NonNull String, @NonNull StageInfo> results) {
		// Assumption: Static analyser has been executed successfully.

		assertTrue(results.containsKey(PipelineArgs.class.getSimpleName()));
		assertTrue(results.containsKey(SweeperInfo.class.getSimpleName()));
		assertTrue(results.containsKey(StaticInfo.class.getSimpleName()));

		final PipelineArgs args = (PipelineArgs) results.get(PipelineArgs.class.getSimpleName());
		final SweeperInfo sweeperInfo = (SweeperInfo) results.get(SweeperInfo.class.arrayType().getSimpleName());
		final StaticInfo staticInfo = (StaticInfo) results.get(StaticInfo.class.getSimpleName());

		for (@NonNull
		final Gadget gadget : staticInfo.getGadgets()) {
			// Completeness: Check that each gadget covers an instruction sequence of the
			// sweeper
			assertEquals(1, sweeperInfo.getInstructionSequences().stream()
					.filter(sequence -> sequence.equals(gadget.getInstructions())).count());
			
			// Correctness: Check that basic block and entry coincide.
			final CFG cfg = gadget.getCFG();
			assertNotNull(cfg);
			assertTrue(gadget.hasCFG());	// cfg != null
			assertEquals(gadget.getInstructions().get(0).getOffset(), cfg.getEntry());
			assertEquals(1, cfg.getGraph().nodes().stream().filter(bb -> bb.getOffset() == cfg.getEntry()).count());
		}
	}

	@Test
	public void Given_EmptyMap_When_Executing_Expect_MissingStageInfoException() {
		// Reason: Static analyser must verify that required information is available.

		final StaticAnalyser<@NonNull TreeMap<@NonNull String, @NonNull StageInfo>> analyser = create();
		assertThrowsExactly(MissingStageInfoException.class,
				() -> analyser.execute(new TreeMap<@NonNull String, @NonNull StageInfo>()));
	}

	@Test
	public void Given_PartialMap_When_Executing_Expect_MissingStageInfoException() {
		// Reason: Static analyser must verify that ALL required information is
		// available.

		final StaticAnalyser<@NonNull TreeMap<@NonNull String, @NonNull StageInfo>> analyser = create();
		final TreeMap<@NonNull String, @NonNull StageInfo> results = new TreeMap();
		results.put(PipelineArgs.class.getSimpleName(), new PipelineArgs(config, new byte[0]));
		assertThrowsExactly(MissingStageInfoException.class,
				() -> analyser.execute(new TreeMap<@NonNull String, @NonNull StageInfo>()));
	}

	@Test
	public void Given_ValidMap_When_Executing_Expect_ValidStaticInfo() {
		// Reason: Static analyser must add a valid result to total result map.
	}
}