package com.topper.tests.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;

import com.topper.file.VDexFile;

public class TestVDexFile {

	private static final String VALID_DEX_FILE_PATH = "tests/resources/classes7.dex";
	private static final String VALID_VDEX_FILE_PATH = "tests/resources/base.vdex";
	
	private static final int VALID_VDEX_AMOUNT_DEX_FILES = 11;

	private static final byte @NonNull [] getFileContents(@NonNull final File file) throws IOException {
		final FileInputStream input = new FileInputStream(file);
		final byte[] content = input.readAllBytes();
		input.close();
		assertNotNull(content);
		return content;
	}
	
	@Test
	public void Given_ValidVDexFile_When_GettingFiles_Expect_CorrectAmount() throws IOException {
		
		// TODO: CONTINUE HERE
		final File f = new File(VALID_VDEX_FILE_PATH);
		final VDexFile vdex = new VDexFile(f, getFileContents(f));
		assertEquals(VALID_VDEX_AMOUNT_DEX_FILES, vdex.getFiles().size());
	}
}