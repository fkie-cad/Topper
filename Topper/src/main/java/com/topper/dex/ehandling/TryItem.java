package com.topper.dex.ehandling;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.helpers.BufferHelper;

/**
 * Try block covering parts of an associated method.
 * 
 * Based on <a href=
 * "https://source.android.com/docs/core/runtime/dex-format#type-item">specification</a>
 * 
 * @author Pascal Kühnemann
 * @since 05.09.2023
 */
public final class TryItem implements Bytable {

	/**
	 * Start address of the block of code covered by this try item. The address is a
	 * count of 16-bit code units to the start of the first covered instruction.
	 */
	private final long startAddress;

	/**
	 * Number of 16-bit code units covered by this entry. The last code unit covered
	 * (inclusive) is <code>startAddress + insnCount - 1</code>.
	 */
	private final int insnCount;

	/**
	 * Offset in bytes from the start of the associated
	 * {@link EncodedCatchHandlerList} to the {@link EncodedCatchHandler} for this
	 * entry. This must be an offset to the start of an
	 * <code>EncodedCatchHandler</code>.
	 */
	private final int handlerOffset;

	/**
	 * Create a try item given a full description. The fields are actually unsigned
	 * integers, thus the next larger quantity is used to encode them.
	 * 
	 * @param startAddress  Start address of the block of code covered by this try
	 *                      item. The address is a count of 16-bit code units to the
	 *                      start of the first covered instruction.
	 * @param insnCount     Number of 16-bit code units covered by this entry. The
	 *                      last code unit covered (inclusive) is
	 *                      <code>startAddress + insnCount - 1</code>.
	 * @param handlerOffset Offset in bytes from the start of the associated
	 *                      {@link EncodedCatchHandlerList} to the
	 *                      {@link EncodedCatchHandler} for this entry. This must be
	 *                      an offset to the start of an
	 *                      <code>EncodedCatchHandler</code>.
	 */
	public TryItem(final long startAddress, final int insnCount, final int handlerOffset) {
		if (!BufferHelper.isUnsignedInt(startAddress)) {
			throw new IllegalArgumentException("startAddress exceeds 4-byte unsigned bounds.");
		}
		if (!BufferHelper.isUnsignedShort(insnCount)) {
			throw new IllegalArgumentException("insnCount exceeds 2-byte unsigned bounds.");
		}
		if (!BufferHelper.isUnsignedShort(handlerOffset)) {
			throw new IllegalArgumentException("handlerOffset exceeds 2-byte unsigned bounds.");
		}
		this.startAddress = startAddress;
		this.insnCount = insnCount;
		this.handlerOffset = handlerOffset;
	}

	@Override
	public final byte @NonNull [] getBytes() {
		final ByteBuffer buffer = ByteBuffer.allocate(this.getByteSize()).order(ByteOrder.LITTLE_ENDIAN);

		buffer.putInt((int) (this.startAddress & 0xffffffff)).putShort((short) (this.insnCount & 0xffff))
				.putShort((short) (this.handlerOffset & 0xffff));

		return buffer.array();
	}

	@Override
	public final int getByteSize() {
		return Integer.BYTES + Short.BYTES + Short.BYTES;
	}

	@Override
	public final String toString() {
		final StringBuilder b = new StringBuilder();
		b.append("Try Item:" + System.lineSeparator());
		b.append(String.format("- start_addr:  %#x", this.startAddress) + System.lineSeparator());
		b.append(String.format("- insn_count:  %#x", this.insnCount) + System.lineSeparator());
		b.append(String.format("- handler_off: %#x", this.handlerOffset) + System.lineSeparator());
		return b.toString();
	}

	/**
	 * Gets the start address of the block of code covered by this try item. The
	 * address is a count of 16-bit code units to the start of the first covered
	 * instruction.
	 */
	public final long getStartAddress() {
		return this.startAddress;
	}

	/**
	 * Gets the number of 16-bit code units covered by this entry. The last code
	 * unit covered (inclusive) is <code>startAddress + insnCount - 1</code>.
	 */
	public final int getInsnCount() {
		return this.insnCount;
	}

	/**
	 * Gets the offset in bytes from the start of the associated
	 * {@link EncodedCatchHandlerList} to the {@link EncodedCatchHandler} for this
	 * entry. This must be an offset to the start of an
	 * <code>EncodedCatchHandler</code>.
	 */
	public final int getHandlerOffset() {
		return this.handlerOffset;
	}
}