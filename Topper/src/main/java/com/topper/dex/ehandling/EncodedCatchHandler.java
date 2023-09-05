package com.topper.dex.ehandling;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.helpers.BufferHelper;

/**
 * Catch handler mapped over a given buffer.
 * 
 * Based on <a href=
 * "https://source.android.com/docs/core/runtime/dex-format#encoded-catch-handler">dex
 * format specification</a>.
 * 
 * @author Pascal Kühnemann
 * @since 05.09.2023
 */
public final class EncodedCatchHandler implements Bytable {

	/**
	 * Number of catch types in {@link EncodedCatchHandler#handlers}. If
	 * non-positive, then this is the negative of the number of catch types, and the
	 * catches are followed by a catch-all handler. For example: A size of 0 means
	 * that there is a catch-all but no explicitly typed catches. A size of 2 means
	 * that there are two explicitly typed catches and no catch-all. And a size of
	 * -1 means that there is one typed catch along with a catch-all.
	 */
	private final int size;

	/**
	 * Ordered list of types to be caught by this handler.
	 */
	@NonNull
	private List<@NonNull EncodedTypeAddrPair> handlers;

	/**
	 * Bytecode address of the catch - all handler.This element is only present if
	 * {@link EncodedCatchHandler#size} is non-positive.
	 */
	private final long catchAllAddr;

	/**
	 * Creates a catch handler based on <code>size</code>, <code>handlers</code> and
	 * <code>catchAllAddr</code>.
	 * 
	 * @param size     Number of catch types in <code>handlers</code>. If
	 *                 non-positive, then this is the negative of the number of
	 *                 catch types, and the catches are followed by a catch-all
	 *                 handler. For example: A size of 0 means that there is a
	 *                 catch-all but no explicitly typed catches. A size of 2 means
	 *                 that there are two explicitly typed catches and no catch-all.
	 *                 And a size of -1 means that there is one typed catch along
	 *                 with a catch-all.
	 * @param handlers Ordered list of types to be caught by this handler.
	 * @param Bytecode address of the catch - all handler. This element should only
	 *                 be present if <code>size</code> is non-positive.
	 * @throws IllegalArgumentException If <code>abs(size)</code> does not match
	 *                                  <code>handlers.size()</code>, or
	 *                                  <code>catchAllAddr</code> exceeds unsigned
	 *                                  integer bounds.
	 */
	public EncodedCatchHandler(final int size, @NonNull final List<@NonNull EncodedTypeAddrPair> handlers,
			final long catchAllAddr) {
		if (Math.abs(size) != handlers.size()) {
			throw new IllegalArgumentException("abs(size) must match amount of handlers.");
		}
		if (!BufferHelper.isUnsignedInt(catchAllAddr)) {
			throw new IllegalArgumentException("catchAllAddr exceeds 4-byte unsigned bounds.");
		}
		this.size = size;
		this.handlers = handlers;
		this.catchAllAddr = catchAllAddr;
	}

	@Override
	public byte @NonNull [] getBytes() {
		final ByteBuffer buffer = ByteBuffer.allocate(this.getByteSize()).order(ByteOrder.LITTLE_ENDIAN);

		Leb128.writeSignedLeb128(buffer, this.size);

		for (@NonNull
		final EncodedTypeAddrPair handler : this.handlers) {
			buffer.put(handler.getBytes());
		}

		if (this.size <= 0) {
			Leb128.writeUnsignedLeb128(buffer, this.catchAllAddr);
		}

		return buffer.array();
	}

	@Override
	public int getByteSize() {
		final int allSize = (this.size <= 0) ? Leb128.unsignedLeb128Size(this.catchAllAddr) : 0;
		return Leb128.signedLeb128Size(this.size) + allSize
				+ this.handlers.stream().mapToInt(handler -> handler.getByteSize()).sum();
	}

	@NonNull
	@Override
	public final String toString() {
		final StringBuilder b = new StringBuilder();
		final ByteBuffer buf = ByteBuffer.wrap(this.getBytes()).order(ByteOrder.LITTLE_ENDIAN);

		b.append("Encoded Catch Handler:" + System.lineSeparator());
		b.append(String.format("- size: %#x", Leb128.readSignedLeb128(buf)) + System.lineSeparator());
		int sum = 0;
		for (@NonNull
		final EncodedTypeAddrPair handler : this.handlers) {
			b.append(handler.toString());
			sum += handler.getByteSize();
		}
		buf.position(buf.position() + sum);
		if (this.size <= 0) {
			b.append(String.format("- catch_all_addr: %#x", Leb128.readUnsignedLeb128Long(buf))
					+ System.lineSeparator());
		} else {
			b.append("- catch_all_addr: none" + System.lineSeparator());
		}

		return "" + b.toString();
	}

	public final int getSize() {
		return this.size;
	}

	@NonNull
	public final List<@NonNull EncodedTypeAddrPair> getHandlers() {
		return this.handlers;
	}

	public final long getCatchAllAddr() {
		return this.catchAllAddr;
	}
}