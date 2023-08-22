package com.topper.tests.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.TopperConfig;
import com.topper.exceptions.InvalidConfigException;
import com.topper.file.DexFile;
import com.topper.file.DexMethod;
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
	
	private boolean isAbstractOrNative(final int flags) {
		return (flags & AccessFlags.ABSTRACT.getValue()) != 0 || (flags & AccessFlags.NATIVE.getValue()) != 0;
	}
	
	@BeforeEach
	public void init() throws InvalidConfigException {
		config = TestConfig.getDefault();
	}

	@Test
	public final void Given_ValidDexFile_When_GettingMethods_Expect_AllMethods() throws IOException {
		// Reason: All methods must be covered exactly once.

		final File f = new File(VALID_DEX_FILE_PATH);
		final DexFile file = new DexFile(f, getFileContents(f), config);
		final ImmutableList<@NonNull DexMethod> methods = file.getMethods();

		// Check whether all methods are there
		final DexBackedDexFile df = DexFileFactory.loadDexFile(VALID_DEX_FILE_PATH, Opcodes.getDefault());
		for (final DexBackedClassDef cls : df.getClasses()) {

			if (cls == null) {
				continue;
			}

			for (final DexBackedMethod method : cls.getMethods()) {

				if (method == null) {
					continue;
				}

				assertTrue(
						methods.stream()
							   .map(m -> m.getMethod())
							   .filter(m -> m.toString().equals(method.toString()))
							   .count() == 1
				);
			}
		}
	}
	
	@Test
	public final void Given_ValidDexFile_When_GettingMethods_Expect_NoBufferAbstractNativeMethods() throws IOException {
		// Reason: Buffer is invalid, iff. method is abstract or native (i.e. nothing to decompile).
		
		final File f = new File(VALID_DEX_FILE_PATH);
		final DexFile file = new DexFile(f, getFileContents(f), config);
		final ImmutableList<@NonNull DexMethod> methods = file.getMethods();
		
		int flags;
		
		for (@NonNull final DexMethod method : methods) {
			
			flags = method.getMethod().getAccessFlags();
			
			// (Method Abstract or Native) <=> (buffer == null)
			assertEquals(isAbstractOrNative(flags), method.getBuffer() == null);
		}
	}
	
	@Test
	public final void Given_ValidDexFile_When_GettingMethods_Expect_CorrectFileMethodMapping() throws IOException {
		// Reason: Each method must reference the .dex file it comes from.
		
		final File f = new File(VALID_DEX_FILE_PATH);
		final DexFile file = new DexFile(f, getFileContents(f), config);
		final ImmutableList<@NonNull DexMethod> methods = file.getMethods();
		
		for (@NonNull final DexMethod method : methods) {
			assertTrue(method.getDexFile().equals(file));
		}
	}
	
	@Test
	public final void Given_ValidDexFile_When_BufferEmpty_Expect_IllegalArgumentException() {
		
		final File f = new File(VALID_DEX_FILE_PATH);
		assertThrowsExactly(IllegalArgumentException.class, () -> new DexFile(f, new byte[0], config));
	}
	
	@Test
	public final void Given_Input_When_NotValidDexFile_Expect_IllegalArgumentException() {
		
		final File f = new File(VALID_VDEX_FILE_PATH);
		assertThrowsExactly(IllegalArgumentException.class, () -> new DexFile(f, getFileContents(f), config));
	}
}