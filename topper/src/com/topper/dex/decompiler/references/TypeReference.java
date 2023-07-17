package com.topper.dex.decompiler.references;

import org.jf.dexlib2.base.reference.BaseTypeReference;
import org.jf.dexlib2.dexbacked.raw.TypeIdItem;

public final class TypeReference extends BaseTypeReference {

	private static final String unknown = "<unknown>";
	
	private final int typeIndex;
	
	public TypeReference(final int typeIndex) {
		this.typeIndex = typeIndex;
	}
	
	@Override
	public final String getType() {
		// TODO Auto-generated method stub
		return TypeReference.unknown;
	}
	
	public final int getSize() {
		return TypeIdItem.ITEM_SIZE;
	}
	
	public final int getTypeIndex() {
		return this.typeIndex;
	}
}