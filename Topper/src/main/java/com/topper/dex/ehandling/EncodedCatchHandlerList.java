package com.topper.dex.ehandling;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

public final class EncodedCatchHandlerList implements Bytable {

	private int size;

	@NonNull
	private List<@NonNull EncodedCatchHandler> list;

	public EncodedCatchHandlerList(@NonNull final List<@NonNull EncodedCatchHandler> list) {
		this.size = list.size();
		this.list = list;
	}

	@Override
	public final byte @NonNull [] getBytes() {
		final ByteBuffer buffer = ByteBuffer.allocate(this.getByteSize()).order(ByteOrder.LITTLE_ENDIAN);
		
		Leb128.writeUnsignedLeb128(buffer, this.size);
		
		for (@NonNull final EncodedCatchHandler handler : this.list) {
			buffer.put(handler.getBytes());
		}
		
		return buffer.array();
	}
	
	@Override
	public final int getByteSize() {
		return Leb128.unsignedLeb128Size(this.size)
				+ this.list.stream().mapToInt(handler -> handler.getByteSize()).sum();
	}

	public final int getSize() {
		return this.size;
	}

	public final void setSize(final int size) {
		this.size = size;
	}

	@NonNull
	public final List<@NonNull EncodedCatchHandler> getList() {
		return this.list;
	}

	public final void setList(@NonNull List<@NonNull EncodedCatchHandler> list) {
		this.list = list;
	}
}