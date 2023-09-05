package com.topper.dex.ehandling;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.helpers.BufferHelper;
import com.topper.helpers.DexHelper;

/**
 * Pair of type and address. It describes the position of the handler
 * responsible for handling exceptions of type.
 * 
 * Based on <a href=
 * "https://source.android.com/docs/core/runtime/dex-format#encoded-type-addr-pair">dex
 * format specification</a>.
 * 
 * @author Pascal Kühnemann
 * @since 05.09.2023
 */
public final class EncodedTypeAddrPair implements Bytable {

	/**
	 * Index of the type of the exception to catch.
	 */
	private final long typeIndex;

	/**
	 * Bytecode address of the associated exception handler relative to the
	 * beginning of a method.
	 */
	private final long address;

	/**
	 * Creates a type - address pair describing an exception handler's position and
	 * exception type.
	 * 
	 * @param typeIndex Index of the type of the exception to catch.
	 * @param address   Bytecode address of the associated exception handler
	 *                  relative to the beginning of a method.
	 * @throws IllegalArgumentException If either <code>typeIndex</code> or
	 *                                  <code>address</code> exceeds 4-byte unsigned
	 *                                  integer bounds.
	 */
	public EncodedTypeAddrPair(final long typeIndex, final long address) {
		if (!BufferHelper.isUnsignedInt(typeIndex)) {
			throw new IllegalArgumentException("typeIndex exceeds 4-byte unsigned bounds.");
		}
		if (!BufferHelper.isUnsignedInt(address)) {
			throw new IllegalArgumentException("address exceeds 4-byte unsigned bounds.");
		}

		this.typeIndex = typeIndex;
		this.address = address;
	}

	@Override
	public final int getByteSize() {
		return Leb128.unsignedLeb128Size(this.typeIndex) + Leb128.unsignedLeb128Size(this.address);
	}

	@Override
	public byte @NonNull [] getBytes() {

		final ByteBuffer buffer = ByteBuffer.allocate(this.getByteSize()).order(ByteOrder.LITTLE_ENDIAN);

		Leb128.writeUnsignedLeb128(buffer, this.typeIndex);
		Leb128.writeUnsignedLeb128(buffer, this.address);

		return buffer.array();
	}

	@Override
	public final String toString() {
		final StringBuilder b = new StringBuilder();
		final ByteBuffer buf = ByteBuffer.wrap(this.getBytes()).order(ByteOrder.LITTLE_ENDIAN);

		b.append("Type - Address - Pair:" + System.lineSeparator());
		b.append(String.format("- type_idx: %#x", Leb128.readUnsignedLeb128Long(buf)) + System.lineSeparator());
		b.append(String.format("- addr:     %#x", Leb128.readUnsignedLeb128Long(buf)) + System.lineSeparator());

		return b.toString();
	}

	/**
	 * Gets the type index of the exception to catch.
	 */
	public final long getTypeIndex() {
		return this.typeIndex;
	}

	/**
	 * Gets the bytecode address of the exception handler, relative to the beginning
	 * of a method.
	 */
	public final long getAddress() {
		return this.address;
	}
}