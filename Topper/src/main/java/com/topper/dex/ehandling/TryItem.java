package com.topper.dex.ehandling;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNull;

public final class TryItem implements Bytable {

	private int startAddress;
	private short insnCount;
	private short handlerOffset;
	
	public TryItem(final int startAddress, final short insnCount, final short handlerOffset) {
		this.startAddress = startAddress;
		this.insnCount = insnCount;
		this.handlerOffset = handlerOffset;
	}
	
	@Override
	public final byte @NonNull [] getBytes() {
		final ByteBuffer buffer = ByteBuffer.allocate(this.getByteSize()).order(ByteOrder.LITTLE_ENDIAN);
		
		buffer.putInt(this.startAddress)
			  .putShort(this.insnCount)
			  .putShort(this.handlerOffset);
		
		return buffer.array();
	}

	@Override
	public final int getByteSize() {
		return Integer.BYTES + Short.BYTES + Short.BYTES;
	}
	
	public final int getStartAddress() {
		return this.startAddress;
	}

	public final void setStartAddress(final int startAddress) {
		this.startAddress = startAddress;
	}

	public final short getInsnCount() {
		return this.insnCount;
	}

	public final void setInsnCount(final short insnCount) {
		this.insnCount = insnCount;
	}

	public final short getHandlerOffset() {
		return this.handlerOffset;
	}

	public final void setHandlerOffset(final short handlerOffset) {
		this.handlerOffset = handlerOffset;
	}
}