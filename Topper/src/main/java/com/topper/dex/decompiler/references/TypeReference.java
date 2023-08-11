package com.topper.dex.decompiler.references;

import org.jf.dexlib2.base.reference.BaseTypeReference;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.raw.TypeIdItem;

public final class TypeReference extends BaseTypeReference {

	private static final String unknown = "<unknown>";
	
	private final int typeIndex;
	private final String type;
	
	public TypeReference(final DexBackedDexFile file, final int typeIndex) {
		this.typeIndex = typeIndex;
		
		if (file != null) {
			// https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/reference/DexBackedTypeReference.java;l=49;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4
			this.type = new String(file.getTypeSection().get(this.typeIndex));
		} else {
			this.type = TypeReference.unknown;
		}
	}
	
	@Override
	public final String getType() {
		return this.type;
	}
	
	public final int getSize() {
		return TypeIdItem.ITEM_SIZE;
	}
	
	public final int getTypeIndex() {
		return this.typeIndex;
	}
}