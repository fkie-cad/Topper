package com.topper.dex.ehandling;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Based on <a href="https://source.android.com/docs/core/runtime/dex-format#encoded-catch-handler">dex format specification</a>.
 * */
public final class EncodedCatchHandler implements Bytable {

	private int size;

	@NonNull
	private List<@NonNull EncodedTypeAddrPair> handlers;

	private int catchAllAddr;

	public EncodedCatchHandler(final int size, @NonNull final List<@NonNull EncodedTypeAddrPair> handlers,
			final int catchAllAddr) {
		this.size = size;
		this.handlers = handlers;
		this.catchAllAddr = catchAllAddr;
	}

	@Override
	public byte @NonNull [] getBytes() {
		final ByteBuffer buffer = ByteBuffer.allocate(this.getByteSize()).order(ByteOrder.LITTLE_ENDIAN);

		Leb128.writeSignedLeb128(buffer, this.size);
		
		for (@NonNull final EncodedTypeAddrPair handler : this.handlers) {
			buffer.put(handler.getBytes());
		}
		
		Leb128.writeUnsignedLeb128(buffer, this.catchAllAddr);
		
		return buffer.array();
	}

	@Override
	public int getByteSize() {
		return Leb128.signedLeb128Size(this.size) + Leb128.unsignedLeb128Size(this.catchAllAddr)
				+ this.handlers.stream().mapToInt(handler -> handler.getByteSize()).sum();
	}
	
	@Override
	public final String toString() {
		final StringBuilder b = new StringBuilder();
		final ByteBuffer buf = ByteBuffer.wrap(this.getBytes()).order(ByteOrder.LITTLE_ENDIAN);
		
		b.append("Encoded Catch Handler:" + System.lineSeparator());
		b.append(String.format("- size: %#x", Leb128.readSignedLeb128(buf)) + System.lineSeparator());
		int sum = 0;
		for (@NonNull final EncodedTypeAddrPair handler : this.handlers) {
			b.append(handler.toString());
			sum += handler.getByteSize();
		}
		buf.position(buf.position() + sum);
		b.append(String.format("- catch_all_addr: %#x", Leb128.readUnsignedLeb128(buf)) + System.lineSeparator());
		
		return b.toString();
	}

	public final int getSize() {
		return this.size;
	}

	public final void setSize(final int size) {
		this.size = size;
	}

	@NonNull
	public final List<@NonNull EncodedTypeAddrPair> getHandlers() {
		return this.handlers;
	}

	public final void setHandlers(@NonNull final List<@NonNull EncodedTypeAddrPair> handlers) {
		this.handlers = handlers;
	}

	public final int getCatchAllAddr() {
		return this.catchAllAddr;
	}

	public final void setCatchAllAddr(final int catchAllAddr) {
		this.catchAllAddr = catchAllAddr;
	}
}