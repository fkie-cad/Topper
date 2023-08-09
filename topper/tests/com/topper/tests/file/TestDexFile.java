package com.topper.tests.file;

import static com.topper.tests.utility.ConditionalAsserts.assertIf;
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
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.decompiler.Decompiler;
import com.topper.dex.decompilation.decompiler.SmaliDecompiler;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompilation.staticanalyser.BFSCFGAnalyser;
import com.topper.dex.decompilation.staticanalyser.CFGAnalyser;
import com.topper.file.DexFile;
import com.topper.file.DexMethod;

public class TestDexFile {

	private static final String VALID_DEX_FILE_PATH = "tests/resources/classes7.dex";
	private static final String VALID_VDEX_FILE_PATH = "tests/resources/base.vdex";

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
	public final void Given_ValidDexFileNoCFG_When_GettingMethods_Expect_AllMethods() throws IOException {
		// Reason: All methods must be covered exactly once.

		final File f = new File(VALID_DEX_FILE_PATH);
		final DexFile file = new DexFile(f, getFileContents(f));
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
	public final void Given_ValidDexFileWithCFG_When_GettingMethods_Expect_AllMethods() throws IOException {
		// Reason: Ensure that CFG extraction does not affect method extraction

		final File f = new File(VALID_DEX_FILE_PATH);
		final Decompiler decompiler = new SmaliDecompiler();
		final CFGAnalyser analyser = new BFSCFGAnalyser();
		final DexFile file = new DexFile(f, getFileContents(f), decompiler, analyser);
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
	public final void Given_ValidDexFileWithCFG_When_GettingMethods_Expect_NoCFGAbstractNativeMethods() throws IOException {
		// Reason: CFG is invalid, iff. method is abstract or native (i.e. nothing to decompile).
		
		final File f = new File(VALID_DEX_FILE_PATH);
		final Decompiler decompiler = new SmaliDecompiler();
		final CFGAnalyser analyser = new BFSCFGAnalyser();
		final DexFile file = new DexFile(f, getFileContents(f), decompiler, analyser);
		final ImmutableList<@NonNull DexMethod> methods = file.getMethods();
		
		int flags;
		
		for (@NonNull final DexMethod method : methods) {
			
			flags = method.getMethod().getAccessFlags();
			
			// (Method Abstract or Native) => (CFG == null)
			assertIf(isAbstractOrNative(flags), method.getCFG() == null);
			
			// (CFG == null) => method abstract or native
			assertIf(method.getCFG() == null, isAbstractOrNative(flags));
		}
	}
	
	@Test
	public final void Given_ValidDexFileWithCFG_When_GettingMethods_Expect_AtLeastOneBasicBlock() throws IOException {
		// Reason: Each method must consist of at least a single basic block that starts at the
		//		   beginning of the method.
		
		final File f = new File(VALID_DEX_FILE_PATH);
		final Decompiler decompiler = new SmaliDecompiler();
		final CFGAnalyser analyser = new BFSCFGAnalyser();
		final DexFile file = new DexFile(f, getFileContents(f), decompiler, analyser);
		final ImmutableList<@NonNull DexMethod> methods = file.getMethods();
		
		for (@NonNull final DexMethod method : methods) {
			
			final CFG cfg = method.getCFG();
			if (cfg != null) {
				
				// Grab basic block with given entry.
				assertTrue(cfg.getGraph().nodes().size() >= 1, method.getMethod().toString());
				assertTrue(cfg.getGraph().nodes().stream().anyMatch(bb -> bb.getOffset() == cfg.getEntry()), method.getMethod().toString());
			}
		}
	}
	
	@Test
	public final void Given_ValidDexFile_When_GettingMethods_Expect_CorrectFileMethodMapping() throws IOException {
		// Reason: Each method must reference the .dex file it comes from.
		
		final File f = new File(VALID_DEX_FILE_PATH);
		final DexFile file = new DexFile(f, getFileContents(f));
		final ImmutableList<@NonNull DexMethod> methods = file.getMethods();
		
		for (@NonNull final DexMethod method : methods) {
			assertTrue(method.getDexFile().equals(file));
		}
	}
	
	@Test
	public final void Given_ValidDexFile_When_BufferEmpty_Expect_IllegalArgumentException() {
		
		final File f = new File(VALID_DEX_FILE_PATH);
		assertThrowsExactly(IllegalArgumentException.class, () -> new DexFile(f, new byte[0]));
	}
	
	@Test
	public final void Given_Input_When_NotValidDexFile_Expect_IllegalArgumentException() {
		
		final File f = new File(VALID_VDEX_FILE_PATH);
		assertThrowsExactly(IllegalArgumentException.class, () -> new DexFile(f, getFileContents(f)));
	}
}