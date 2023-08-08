package com.topper.file;

import java.lang.reflect.Field;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.dexbacked.DexReader;

public class DexHelper {

	public static final int CODE_ITEM_SIZE = 0x10;
	
	@NonNull
	private static final String FIELD_NAME_CODE_OFFSET = "codeOffset";
	
	public static final int getMethodOffset(@NonNull final DexBackedMethod method) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		final Field offsetField = method.getClass().getDeclaredField(FIELD_NAME_CODE_OFFSET);
		offsetField.setAccessible(true);
		final int offset = offsetField.getInt(method);
		offsetField.setAccessible(false);
		return offset;
	}
	
	public static final int getMethodSize(@NonNull final DexBackedMethod method, final int offset) {
		
		// Reader points at insns_size in code item
		final DexReader reader = new DexReader(method.dexFile.getBuffer(), offset + 2);
		return reader.readUshort() * 2;
	}
}