package com.topper.tests.file;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.AccessFlags;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.topper.configuration.TopperConfig;
import com.topper.exceptions.InvalidConfigException;
import com.topper.file.DexFile;
import com.topper.tests.utility.TestConfig;

public class TestDexFile {

	private static final String VALID_DEX_FILE_PATH = "./src/test/java/resources/classes7.dex";
	private static final String VALID_VDEX_FILE_PATH = "./src/test/java/resources/base.vdex";
	
	private static TopperConfig config;

	private static final byte @NonNull [] getFileContents(@NonNull final File file) throws IOException {
		final FileInputStream input = new FileInputStream(file);
		final byte[] content = input.readAllBytes();
		input.close();
		assertNotNull(content);
		return content;
	}
	
	@BeforeEach
	public void init() throws InvalidConfigException {
		config = TestConfig.getDefault();
	}
	
	@Test
	public final void Given_ValidDexFile_When_BufferEmpty_Expect_IllegalArgumentException() {
		
		final File f = new File(VALID_DEX_FILE_PATH);
		assertThrowsExactly(IllegalArgumentException.class, () -> new DexFile(VALID_DEX_FILE_PATH, new byte[0], 0, config));
	}
	
	@Test
	public final void Given_Input_When_NotValidDexFile_Expect_IllegalArgumentException() {
		
		final File f = new File(VALID_VDEX_FILE_PATH);
		assertThrowsExactly(IllegalArgumentException.class, () -> new DexFile(VALID_DEX_FILE_PATH, getFileContents(f), 0, config));
	}
}