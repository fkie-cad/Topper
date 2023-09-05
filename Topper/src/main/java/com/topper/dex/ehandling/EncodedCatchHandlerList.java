package com.topper.dex.ehandling;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

/**
 * List of {@link EncodedCatchHandler}s that lies right after {@link TryItem}.
 * 
 * @author Pascal Kühnemann
 * @since 05.09.2023
 * */
public final class EncodedCatchHandlerList implements Bytable {

	/**
	 * Size of this list in entries. Notice that in the .dex specification,
	 * this field is actually an unsigned int.
	 * */
	private final int size;

	/**
	 * List of {@link EncodedCatchHandler}s.
	 * */
	@NonNull
	private final List<@NonNull EncodedCatchHandler> list;

	/**
	 * Creates a list of {@link EncodedCatchHandler}s.
	 * 
	 * @param List of {@link EncodedCatchHandler}s to wrap. Its size is a
	 * 	signed value, whereas the specification states that the size must be unsigned.
	 * 	This is a technical limitation.
	 * */
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
	
	@Override
	public final String toString() {
		final StringBuilder b = new StringBuilder();
		final ByteBuffer buf = ByteBuffer.wrap(this.getBytes());
		
		b.append("Encoded Handler List:" + System.lineSeparator());
		b.append(String.format("- size: %#x", Leb128.readUnsignedLeb128(buf)) + System.lineSeparator());
		for (@NonNull final EncodedCatchHandler handler : this.list) {
			b.append(handler.toString());
		}
		
		return b.toString();
	}

	/**
	 * Gets the amount of entries in this list.
	 * */
	public final int getSize() {
		return this.size;
	}

	/**
	 * Gets the list of {@link EncodedCatchHandler}s wrapped.
	 * */
	@NonNull
	public final List<@NonNull EncodedCatchHandler> getList() {
		return this.list;
	}
}