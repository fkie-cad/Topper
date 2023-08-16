package com.topper.tests.dex.decompilation.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.TopperConfig;
import com.topper.dex.decompilation.pipeline.PipelineArgs;
import com.topper.dex.decompilation.pipeline.PipelineContext;
import com.topper.dex.decompilation.pipeline.SeekerInfo;
import com.topper.dex.decompilation.pipeline.StaticInfo;
import com.topper.dex.decompilation.pipeline.SweeperInfo;
import com.topper.exceptions.DuplicateInfoIdException;
import com.topper.exceptions.InvalidConfigException;
import com.topper.exceptions.MissingStageInfoException;
import com.topper.tests.utility.DexLoader;
import com.topper.tests.utility.TestConfig;

public class TestPipelineContext {

	private static final String TEST_ID = "test";
	private static final String ARGS_ID = "PipelineArgs";
	
	private static TopperConfig config;
	
	@BeforeAll
	public static void init() throws InvalidConfigException {
		config = TestConfig.getDefault();
	}
	
	@NonNull
	private static final PipelineContext createContext() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException {
		final PipelineArgs args = new PipelineArgs(config, DexLoader.get().getMethodBytes());
		return new PipelineContext(args);
	}
	
	@Test
	public void Given_ArgsContext_When_GettingNonexistingKey_Expect_MissingStageInfoException() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException {
		// Reason: Requesting non - existing data must fail.
		
		final PipelineContext c = createContext();
		assertThrowsExactly(MissingStageInfoException.class, () -> c.getInfo(""));
	}
	
	@Test
	public void Given_ArgsContext_When_GettingArgs_Expect_ValidArgs() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException, MissingStageInfoException {
		// Reason: Fetching pipeline args must work, regardless of whether
		// a convenience method is used or not.
		
		final PipelineContext c = createContext();
		assertInstanceOf(PipelineArgs.class, c.getArgs());
		assertInstanceOf(PipelineArgs.class, c.getInfo(ARGS_ID));
	}
	
	@Test
	public void Given_ArgsContext_When_GettingArgsTypeMismatch_Expect_ClassCastException() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException, MissingStageInfoException {
		// Reason: If stage expects a particular type for a given key, but gets
		// a different type, then this must trigger a ClassCastException.
		
		final PipelineContext c = createContext();
		
		assertThrowsExactly(ClassCastException.class, () -> {
			final SeekerInfo info = c.getInfo(ARGS_ID);
		});
	}
	
	@Test
	public void Given_ArgsContext_When_GettingSeekerInfo_Expect_MissingStageInfoException() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException {
		// Reason: If dedicated getSeekerInfo method is used, then ClassCastExceptions
		// must be wrapped in MissingStageInfoException to prevent ClassCastExceptions
		// sneaking too far out.
		
		final PipelineContext c = createContext();
		
		assertThrowsExactly(MissingStageInfoException.class, () -> c.getSeekerInfo(ARGS_ID));
	}
	
	@Test
	public void Given_ArgsContext_When_GettingSweeperInfo_Expect_MissingStageInfoException() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException {
		final PipelineContext c = createContext();
		assertThrowsExactly(MissingStageInfoException.class, () -> c.getSweeperInfo(ARGS_ID));
	}
	
	@Test
	public void Given_ArgsContext_When_GettingStaticInfo_Expect_MissingStageInfoException() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException {
		final PipelineContext c = createContext();
		assertThrowsExactly(MissingStageInfoException.class, () -> c.getStaticInfo(ARGS_ID));
	}
	
	@Test
	public void Given_ArgsContext_When_PuttingExistingId_Expect_DuplicateInfoIdException() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException, DuplicateInfoIdException {
		// Reason: Context must not contain duplicate identifiers.
		
		final PipelineContext c = createContext();
		final SeekerInfo info = new SeekerInfo(ImmutableList.of());
		c.putInfo(TEST_ID, info);
		assertThrowsExactly(DuplicateInfoIdException.class, () -> c.putInfo(TEST_ID, info));
	}
	
	@Test
	public void Given_ArgsContext_When_PuttingDuplicateInfoDifferentId_Expect_Valid() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException, DuplicateInfoIdException {
		// Reason: Storing the same object under multiple identifiers might be relevant for compatibility reasons.
		
		final PipelineContext c = createContext();
		final SeekerInfo info = new SeekerInfo(ImmutableList.of());
		c.putInfo(TEST_ID, info);
		c.putInfo(TEST_ID + "1", info);
	}
	
	@Test
	public void Given_ArgsContext_When_PuttingAndRetrievingSameId_Expect_SameObject() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException, DuplicateInfoIdException, MissingStageInfoException {
		// Reason: Retrieving a previously stored StageInfo must give that StageInfo.
		
		final PipelineContext c = createContext();
		final PipelineArgs args = new PipelineArgs(config, new byte[0]);
		c.putInfo(TEST_ID, args);
		assertEquals(args, c.getInfo(TEST_ID));
	}
	
	@Test
	public void Given_ArgsContext_When_PuttingAndRetrievingSeeker_Expect_SameObject() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException, DuplicateInfoIdException, MissingStageInfoException {
		
		final PipelineContext c = createContext();
		final SeekerInfo info = new SeekerInfo(ImmutableList.of());
		c.putInfo(TEST_ID, info);
		assertEquals(info, c.getSeekerInfo(TEST_ID));
	}
	
	@Test
	public void Given_ArgsContext_When_PuttingAndRetrievingSweeper_Expect_SameObject() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException, DuplicateInfoIdException, MissingStageInfoException {
		
		final PipelineContext c = createContext();
		final SweeperInfo info = new SweeperInfo(ImmutableList.of());
		c.putInfo(TEST_ID, info);
		assertEquals(info, c.getSweeperInfo(TEST_ID));
	}
	
	@Test
	public void Given_ArgsContext_When_PuttingAndRetrievingStatic_Expect_SameObject() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException, DuplicateInfoIdException, MissingStageInfoException {
		
		final PipelineContext c = createContext();
		final StaticInfo info = new StaticInfo(ImmutableList.of());
		c.putInfo(TEST_ID, info);
		assertEquals(info, c.getStaticInfo(TEST_ID));
	}
	
}