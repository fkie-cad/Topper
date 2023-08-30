package com.topper.dex.ehandling;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNull;

public final class MethodCodeItem implements Bytable {
	
	public static final int CODE_ITEM_SIZE = 0x10;

	private int registersSize;
	private int insSize;
	private int outsSize;
	private int triesSize;
	private long debugInfoOffset;
	private long insnsSize;

	public MethodCodeItem(final int registersSize, final int insSize, final int outsSize, final int triesSize,
			final long debugInfoOffset, final long insnsSize) {
		this.registersSize = registersSize & 0xffff;
		this.insSize = insSize & 0xffff;
		this.outsSize = outsSize & 0xffff;
		this.triesSize = triesSize & 0xffff;
		this.debugInfoOffset = debugInfoOffset & 0xffffffff;
		this.insnsSize = insnsSize & 0xffffffff;
	}

	public MethodCodeItem(final byte @NonNull [] data) {
		if (data.length < this.getByteSize()) {
			throw new IllegalArgumentException("Given buffer is too small to hold a code item.");
		}
		
		final ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
		this.registersSize = buf.getShort() & 0xffff;
		this.insSize = buf.getShort() & 0xffff;
		this.outsSize = buf.getShort() & 0xffff;
		this.triesSize = buf.getShort() & 0xffff;
		this.debugInfoOffset = buf.getInt() & 0xffffffff;
		this.insnsSize = buf.getInt() & 0xffffffff;
	}
	
	@Override
	public final byte @NonNull [] getBytes() {
		
		final ByteBuffer buf = ByteBuffer.allocate(this.getByteSize()).order(ByteOrder.LITTLE_ENDIAN);
		
		buf.putShort((short)(this.registersSize & 0xffff));
		buf.putShort((short)(this.insSize & 0xffff));
		buf.putShort((short)(this.outsSize & 0xffff));
		buf.putShort((short)(this.triesSize & 0xffff));
		buf.putInt((int)(this.debugInfoOffset & 0xffffffff));
		buf.putInt((int)(this.insnsSize & 0xffffffff));
		
		return buf.array();
	}

	@Override
	public final int getByteSize() {
		return CODE_ITEM_SIZE;
	}
	
	@Override
	public final String toString() {
		final StringBuilder b = new StringBuilder();
		
		b.append("CodeItem:" + System.lineSeparator());
		b.append("- registers_size: 0x" + Integer.toHexString(this.registersSize) + System.lineSeparator());
		b.append("- ins_size:       0x" + Integer.toHexString(this.insSize) + System.lineSeparator());
		b.append("- outs_size:      0x" + Integer.toHexString(this.outsSize) + System.lineSeparator());
		b.append("- tries_size:     0x" + Integer.toHexString(this.triesSize) + System.lineSeparator());
		b.append("- debug_info_off: 0x" + Long.toHexString(this.debugInfoOffset) + System.lineSeparator());
		b.append("- insns_size:     0x" + Long.toHexString(this.insnsSize) + System.lineSeparator());
		
		
		return b.toString();
	}

	public final int getRegistersSize() {
		return this.registersSize;
	}

	public final void setRegistersSize(final int registersSize) {
		this.registersSize = registersSize & 0xffff;
	}

	public final int getInsSize() {
		return this.insSize;
	}

	public final void setInsSize(final int insSize) {
		this.insSize = insSize & 0xffff;
	}

	public final int getOutsSize() {
		return this.outsSize;
	}

	public final void setOutsSize(final int outsSize) {
		this.outsSize = outsSize & 0xffff;
	}

	public final int getTriesSize() {
		return this.triesSize;
	}

	public final void setTriesSize(final int triesSize) {
		this.triesSize = triesSize & 0xffff;
	}

	public final long getDebugInfoOffset() {
		return this.debugInfoOffset;
	}

	public final void setDebugInfoOffset(final long debugInfoOffset) {
		this.debugInfoOffset = debugInfoOffset & 0xffffffff;
	}

	public final long getInsnsSize() {
		return this.insnsSize;
	}

	public final void setInsnsSize(final long insnsSize) {
		this.insnsSize = insnsSize & 0xffffffff;
	}
}