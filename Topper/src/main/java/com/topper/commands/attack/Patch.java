package com.topper.commands.attack;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.dex.decompilation.DexHelper;

public final class Patch {

	private final int offset;
	
	private final byte @NonNull [] data;
	
	public Patch(final int offset, final byte @NonNull [] data) {
		this.offset = offset;
		this.data = data;
	}

	public final int getOffset() {
		return this.offset;
	}

	public final byte @NonNull [] getData() {
		return this.data;
	}
	
	public final int getSize() {
		return this.data.length;
	}
	
	@Override
	public final String toString() {
		
		final StringBuilder b = new StringBuilder();
		
		b.append("[" + Integer.toHexString(this.offset) + "] = ");
		b.append(DexHelper.bytesToString(this.data));
		b.append(System.lineSeparator());
		
		return b.toString();
	}
}