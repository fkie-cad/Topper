package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.iface.instruction.SwitchElement;

public final class BufferedSwitchElement implements SwitchElement {
	
	private final int key;
	private final int offset;
	
	public BufferedSwitchElement(final int key, final int offset) {
		this.key = key;
		this.offset = offset;
	}

	@Override
	public final int getKey() {
		return this.key;
	}

	@Override
	public final int getOffset() {
		return this.offset;
	}
}
