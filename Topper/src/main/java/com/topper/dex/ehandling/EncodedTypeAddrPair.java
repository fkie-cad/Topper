package com.topper.dex.ehandling;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.dex.decompilation.DexHelper;

/**
 * Based on <a href="https://source.android.com/docs/core/runtime/dex-format#encoded-type-addr-pair">dex format specification</a>.
 * */
public final class EncodedTypeAddrPair implements Bytable {

	private int typeIndex;
	
	private int address;
	
	public EncodedTypeAddrPair(final int typeIndex, final int address) {
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
		b.append(String.format("- type_idx: %#x", Leb128.readUnsignedLeb128(buf)) + System.lineSeparator());
		b.append(String.format("- addr:     %#x", Leb128.readUnsignedLeb128(buf)) + System.lineSeparator());
		
		return b.toString();
	}
	
	public final int getTypeIndex() {
		return this.typeIndex;
	}

	public final void setTypeIndex(final int typeIndex) {
		this.typeIndex = typeIndex;
	}

	public final int getAddress() {
		return this.address;
	}

	public final void setAddress(final int address) {
		this.address = address;
	}
}