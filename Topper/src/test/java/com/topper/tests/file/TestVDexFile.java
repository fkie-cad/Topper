package com.topper.tests.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.stream.StreamSupport;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.TopperConfig;
import com.topper.exceptions.InvalidConfigException;
import com.topper.file.DexFile;
import com.topper.file.DexMethod;
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
	public void Given_ValidVDexFile_When_GettingMethods_Expect_CorrectAmount()
			throws IOException, InterruptedException {
		// Reason: All methods covered by .dex files in .vdex must be covered.

		config.getDecompilerConfig().setDexSkipThreshold(-1);
		final File f = new File(VALID_VDEX_FILE_PATH);
		final VDexFile vdex = new VDexFile(VALID_VDEX_FILE_PATH, getFileContents(f), config);
		final ImmutableList<@NonNull DexMethod> methods = vdex.getMethods();

		int total = 0;
		for (@NonNull
		final DexFile dex : vdex.getDexFiles()) {

			// Count methods using dexlib2
			final DexBackedDexFile check = new DexBackedDexFile(Opcodes.getDefault(), dex.getBuffer());

			total += check.getClasses().stream()
					.mapToInt(cls -> (int) StreamSupport.stream(cls.getMethods().spliterator(), false).count()).sum();
		}

		// Note: Iterating through all classes and their methods and cross checking
		// whether each method is contained in vdex.getMethods is infeasible, as
		// usually there is one very large .dex file with 25.000 methods and ~10
		// small ones with < 300 methods => multi - threading is not easy to apply.
		// This check minimizes the probability to have an error.
		assertEquals(total, methods.size());
	}

	@Test
	public void Given_ValidVDexFile_When_GettingMethods_Expect_CorrectMethodDexFileMapping() throws IOException {
		// Reason: Each method must reference exactly one .dex file from .vdex file.

		config.getDecompilerConfig().setDexSkipThreshold(-1);
		final File f = new File(VALID_VDEX_FILE_PATH);
		final VDexFile vdex = new VDexFile(VALID_DEX_FILE_PATH, getFileContents(f), config);
		final ImmutableList<@NonNull DexMethod> methods = vdex.getMethods();

		for (@NonNull
		final DexMethod method : methods) {
			// Technically, this also checks that .dex files are unique.
			assertEquals(1, vdex.getDexFiles().stream().filter(df -> df.equals(method.getDexFile())).count());
		}
	}

	@Test
	public void Given_ValidVDexFile_When_GettingMethods_Expect_AnalysedMethodsValid() throws IOException {
		// Reason: Methods stored in sufficiently small .dex files must be analysed.

		final File f = new File(VALID_VDEX_FILE_PATH);
		final int threshold = config.getDecompilerConfig().getDexSkipThreshold();
		final VDexFile vdex = new VDexFile(VALID_VDEX_FILE_PATH, getFileContents(f), config);
		final ImmutableList<@NonNull DexMethod> methods = vdex.getMethods();

		int flags;
		for (@NonNull
		final DexMethod method : methods) {

			if (method.getDexFile().getBuffer().length <= threshold) {

				// Method must be analysed
				flags = method.getMethod().getAccessFlags();

				// Check if method has been analysed, taking into account abstract or native
				// methods.
				// abstract or native <=> (buffer == null)
				assertEquals(isAbstractOrNative(flags), method.getBuffer() == null, method.getMethod().toString());
			} else {
				assertNull(method.getBuffer());
			}
		}
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