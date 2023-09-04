package com.topper.tests.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.AccessFlags;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.topper.configuration.TopperConfig;
import com.topper.exceptions.InvalidConfigException;
import com.topper.file.DexFile;
import com.topper.file.VDexFile;
import com.topper.tests.utility.TestConfig;

public class TestVDexFile {

	private static final String VALID_DEX_FILE_PATH = "./src/test/java/resources/classes7.dex";
	private static final String VALID_VDEX_FILE_PATH = "./src/test/java/resources/base.vdex";
	private static final String CORRUPTED_VDEX_FILE_PATH = "./src/test/java/resources/corrupted.vdex";

	private static final int VALID_VDEX_AMOUNT_DEX_FILES = 11;
	
	private static TopperConfig config;
	
	@BeforeEach
	public void init() throws InvalidConfigException {
		config = TestConfig.getDefault();
	}

	private static final byte @NonNull [] getFileContents(@NonNull final File file) throws IOException {
		final FileInputStream input = new FileInputStream(file);
		final byte[] content = input.readAllBytes();
		input.close();
		assertNotNull(content);
		return content;
	}

	private boolean isAbstractOrNative(final int flags) {
		return (flags & AccessFlags.ABSTRACT.getValue()) != 0 || (flags & AccessFlags.NATIVE.getValue()) != 0;
	}

	@Test
	public void Given_ValidVDexFile_When_GettingFiles_Expect_CorrectAmount() throws IOException {
		// Reason: All .dex files covered by a .vdex must be extracted.
		// TODO

		config.getDecompilerConfig().setDexSkipThreshold(-1);
		final File f = new File(VALID_VDEX_FILE_PATH);
		final VDexFile vdex = new VDexFile(VALID_VDEX_FILE_PATH, getFileContents(f), config);
		assertEquals(VALID_VDEX_AMOUNT_DEX_FILES, vdex.getDexFiles().size());
	}

	@Test
	public void Given_ValidVDexFile_When_GettingFiles_Expect_UniqueFiles() throws IOException {
		// Reason: Each .dex file must be unique (no duplicates).
		// Flaky: Using HashSet to remove possible duplicates can suffer from
		// collisions.

		config.getDecompilerConfig().setDexSkipThreshold(-1);
		final File f = new File(VALID_VDEX_FILE_PATH);
		final VDexFile vdex = new VDexFile(VALID_VDEX_FILE_PATH, getFileContents(f), config);
		assertEquals(vdex.getDexFiles().size(), new HashSet<@NonNull DexFile>(vdex.getDexFiles()).size());
	}
	
	@Test
	public void Given_DexFile_When_Loading_Expect_IllegalArgumentException() {
		
		config.getDecompilerConfig().setDexSkipThreshold(-1);
		final File f = new File(VALID_DEX_FILE_PATH);
		assertThrowsExactly(IllegalArgumentException.class, () -> new VDexFile(VALID_DEX_FILE_PATH, getFileContents(f), config));
	}
	
	@Test
	public void Given_CorruptedVDexFile_When_Loading_Expect_IllegalArgumentException() {
		
		config.getDecompilerConfig().setDexSkipThreshold(-1);
		final File f = new File(CORRUPTED_VDEX_FILE_PATH);
		assertThrowsExactly(IllegalArgumentException.class, () -> new VDexFile(CORRUPTED_VDEX_FILE_PATH, getFileContents(f), config));
	}
}