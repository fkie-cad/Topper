package com.topper.dex.ehandling;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.helpers.BufferHelper;

/**
 * Code item describing a method header. It precedes the actual instructions of
 * a method.
 * 
 * Based on <a href=
 * "https://source.android.com/docs/core/runtime/dex-format#code-item">specification</a>
 * 
 * @author Pascal Kühnemann
 * @since 05.09.2023
 */
public final class MethodCodeItem implements Bytable {

	/**
	 * Default code item size.
	 */
	public static final int CODE_ITEM_SIZE = 0x10;

	/**
	 * Number of virtual registers used by the method associated with this code
	 * item.
	 */
	private int registersSize;

	/**
	 * Number of virtual registers used as parameters by this method.
	 */
	private int insSize;

	/**
	 * Number of words of outgoing argument space required by this method for method
	 * invocation.
	 */
	private int outsSize;

	/**
	 * Number of {@link TryItem}s for this method. If non-zero, then these appear as
	 * an array just after the instructions of this method.
	 */
	private int triesSize;

	/**
	 * Offset from the start of the .dex associated with this method to the debug
	 * info. <code>0</code> indicates no information.
	 */
	private long debugInfoOffset;

	/**
	 * Size of the instruction list in code units. Thus <code>insnsSize >> 1</code>
	 * is the number of instruction bytes of this method.
	 */
	private long insnsSize;

	/**
	 * Creates a code item given a full description. As all values are either
	 * unsigned short or unsigned int, their Java counterparts are encoded using the
	 * next larger quantity, i.e. signed int and signed long, respectively.
	 * 
	 * @param registersSize   Number of virtual registers used by the method
	 *                        associated with this code item.
	 * @param insSize         Number of virtual registers used as parameters by this
	 *                        method.
	 * @param outsSize        Number of words of outgoing argument space required by
	 *                        this method for method invocation.
	 * @param triesSize       Number of {@link TryItem}s for this method. If
	 *                        non-zero, then these appear as an array just after the
	 *                        instructions of this method.
	 * @param debugInfoOffset Offset from the start of the .dex associated with this
	 *                        method to the debug info. <code>0</code> indicates no
	 *                        information.
	 * @param insnsSize       Size of the instruction list in code units. Thus
	 *                        <code>insnsSize >> 1</code> is the number of
	 *                        instruction bytes of this method.
	 * @throws IllegalArgumentException If any of the parameters exceeds their
	 *                                  respective unsigned short/int bounds.
	 */
	public MethodCodeItem(final int registersSize, final int insSize, final int outsSize, final int triesSize,
			final long debugInfoOffset, final long insnsSize) {
		this.checkFields(registersSize, insSize, outsSize, triesSize, debugInfoOffset, insnsSize);
		this.registersSize = registersSize & 0xffff;
		this.insSize = insSize & 0xffff;
		this.outsSize = outsSize & 0xffff;
		this.triesSize = triesSize & 0xffff;
		this.debugInfoOffset = debugInfoOffset & 0xffffffff;
		this.insnsSize = insnsSize & 0xffffffff;
	}

	/**
	 * Creates a code item from a buffer.
	 * 
	 * @param data Buffer, from which to extract the code item.
	 * @throws IllegalArgumentException If <code>data</code> contains to few bytes
	 *                                  to hold a code item.
	 */
	public MethodCodeItem(final byte @NonNull [] data) {
		if (data.length < this.getByteSize()) {
			throw new IllegalArgumentException("Given buffer is too small to hold a code item.");
		}

		final ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
		this.registersSize = Short.toUnsignedInt(buf.getShort());
		this.insSize = Short.toUnsignedInt(buf.getShort());
		this.outsSize = Short.toUnsignedInt(buf.getShort());
		this.triesSize = Short.toUnsignedInt(buf.getShort());
		this.debugInfoOffset = Integer.toUnsignedLong(buf.getInt());
		this.insnsSize = Integer.toUnsignedLong(buf.getInt());

		this.checkFields(this.registersSize, this.insSize, this.outsSize, this.triesSize, this.debugInfoOffset,
				this.insnsSize);
	}

	private final void checkFields(final int registersSize, final int insSize, final int outsSize, final int triesSize,
			final long debugInfoOffset, final long insnsSize) {
		if (!BufferHelper.isUnsignedShort(registersSize)) {
			throw new IllegalArgumentException("registersSize exceeds 2-byte unsigned bounds.");
		}
		if (!BufferHelper.isUnsignedShort(insSize)) {
			throw new IllegalArgumentException("insSize exceeds 2-byte unsigned bounds.");
		}
		if (!BufferHelper.isUnsignedShort(outsSize)) {
			throw new IllegalArgumentException("outsSize exceeds 2-byte unsigned bounds.");
		}
		if (!BufferHelper.isUnsignedShort(triesSize)) {
			throw new IllegalArgumentException("triesSize exceeds 2-byte unsigned bounds.");
		}
		if (!BufferHelper.isUnsignedInt(debugInfoOffset)) {
			throw new IllegalArgumentException("debugInfoOffset exceeds 2-byte unsigned bounds.");
		}
		if (!BufferHelper.isUnsignedInt(insnsSize)) {
			throw new IllegalArgumentException("insnsSize exceeds 2-byte unsigned bounds.");
		}
	}

	@Override
	public final byte @NonNull [] getBytes() {

		final ByteBuffer buf = ByteBuffer.allocate(this.getByteSize()).order(ByteOrder.LITTLE_ENDIAN);

		buf.putShort((short) (this.registersSize & 0xffff));
		buf.putShort((short) (this.insSize & 0xffff));
		buf.putShort((short) (this.outsSize & 0xffff));
		buf.putShort((short) (this.triesSize & 0xffff));
		buf.putInt((int) (this.debugInfoOffset & 0xffffffff));
		buf.putInt((int) (this.insnsSize & 0xffffffff));

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

	/**
	 * Get the number of virtual registers used by the method associated with this
	 * code item.
	 */
	public final int getRegistersSize() {
		return this.registersSize;
	}

	/**
	 * Gets the number of virtual registers used as parameters by this method.
	 */
	public final int getInsSize() {
		return this.insSize;
	}

	/**
	 * Gets the number of words of outgoing argument space required by this method
	 * for method invocation.
	 */
	public final int getOutsSize() {
		return this.outsSize;
	}

	/**
	 * Gets the number of {@link TryItem}s for this method.
	 */
	public final int getTriesSize() {
		return this.triesSize;
	}

	/**
	 * Gets the offset from the start of the .dex associated with this method to the
	 * debug info. <code>0</code> indicates no information.
	 */
	public final long getDebugInfoOffset() {
		return this.debugInfoOffset;
	}

	/**
	 * Gets the size of the instruction list in code units. Thus
	 * <code>insnsSize >> 1</code> is the number of instruction bytes of this
	 * method.
	 */
	public final long getInsnsSize() {
		return this.insnsSize;
	}
}