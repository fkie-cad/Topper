package com.topper.dex.decompiler.references;

import org.jf.dexlib2.base.reference.BaseStringReference;

public final class StringReference extends BaseStringReference {

	private static final String unknown = "<unknown>";
	
	private final int stringIndex;
	
	public StringReference(final int stringIndex) {
		this.stringIndex = stringIndex;
	}
	
	@Override
	public final String getString() {
		return StringReference.unknown;
	}
	
	public final int getSize() {
		return StringReference.unknown.length();
	}
	
	public final int getStringIndex() {
		return this.stringIndex;
	}
}
