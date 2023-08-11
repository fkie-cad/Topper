package com.topper.tests.utility;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedMethod;

import com.topper.file.DexHelper;

public class DexLoader {

	private static DexLoader instance;
	
	private String fileName;
	private String className;
	private String methodName;
	
	private DexBackedDexFile file;
	
	private DexLoader() throws IOException {
		this.fileName = "./src/test/java/resources/classes7.dex";
		this.className = "Lcom/damnvulnerableapp/networking/messages/PlainMessageParser;";
		this.methodName = "parseFromBytes";
		final Opcodes opcodes = Opcodes.forDexVersion(TestConfig.getDefault().getDecompilerConfig().getDexVersion());
		this.file = DexFileFactory.loadDexFile(this.fileName, opcodes);
	}
	
	@NonNull
	public static final DexLoader get() throws IOException {
		if (DexLoader.instance == null) {
			DexLoader.instance = new DexLoader();
		}
		return DexLoader.instance;
	}
	
	public final byte[] getMethodBytes() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		
		for (final DexBackedClassDef cls : this.file.getClasses()) {
			
			if (cls == null || !cls.getType().equals(this.className)) {
				continue;
			}
			
			for (final DexBackedMethod method : cls.getMethods()) {
				
				if (method == null || !method.getName().equals(this.methodName)) {
					continue;
				}
				
				final int offset = DexHelper.getMethodOffset(method);
				final int size = DexHelper.getMethodSize(method, offset);
				return file.getBuffer().readByteRange(offset + 0x10, size);
			}
		}
		
		return null;
	}
	
	public final DexBackedDexFile loadFile(@NonNull final String fileName) throws IOException {
		return DexFileFactory.loadDexFile(fileName, Opcodes.forDexVersion(TestConfig.getDefault().getDecompilerConfig().getDexVersion()));
	}
	
	public final DexBackedDexFile getFile() {
		return this.file;
	}
}