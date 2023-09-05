package com.topper.tests.dex.ehandling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.jupiter.api.Test;

import com.topper.dex.ehandling.EncodedTypeAddrPair;
import com.topper.dex.ehandling.Leb128;

public final class TestEncodedTypeAddrPair {

	private void validate(final long expectedTypeIndex, final long expectedAddress) {
		
		final EncodedTypeAddrPair p = new EncodedTypeAddrPair(expectedTypeIndex, expectedAddress);
		final ByteBuffer buffer = ByteBuffer.wrap(p.getBytes()).order(ByteOrder.LITTLE_ENDIAN);
		
		assertEquals(expectedTypeIndex, Leb128.readUnsignedLeb128Long(buffer.position(0)));
		
		final int base = Leb128.unsignedLeb128Size(expectedTypeIndex);
		assertEquals(expectedAddress, Leb128.readUnsignedLeb128Long(buffer.position(base)));
		
		assertEquals(expectedTypeIndex, p.getTypeIndex());
		assertEquals(expectedAddress, p.getAddress());
	}
	
	@Test
	public void Given_ValidTypeAddressZero_When_Creating_Expect_ValidPair() {
		validate(0, 0);
	}
	
	@Test
	public void Given_ValidTypeAddress_When_Creating_Expect_ValidPair() {
		validate(0x42, 0x42);
	}
	
	@Test
	public void Given_AllNonUnsignedIntCases_When_Creating_Expect_IllegalArgumentExceptions() {
		
		// Tight bounds
		final long[] indices = { -1L, 1L << Integer.SIZE };
		
		for (final long typeIndex : indices) {
			for (final long address : indices) {
				assertThrowsExactly(IllegalArgumentException.class, () -> validate(typeIndex, address), "TypeIndex: " + typeIndex + ", Address: " + address);
			}
		}
	}
	
	@Test
	public void Given_ValidPair_When_stringify_Expect_ValidString() {
		
		final EncodedTypeAddrPair p = new EncodedTypeAddrPair(0, 0);
		final String s = p.toString();
		assertNotNull(s);
		assertTrue(s.length() > 0);
	}
}