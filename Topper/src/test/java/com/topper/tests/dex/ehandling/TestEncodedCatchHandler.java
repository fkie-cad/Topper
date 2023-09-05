package com.topper.tests.dex.ehandling;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.topper.dex.ehandling.Bytable;
import com.topper.dex.ehandling.EncodedCatchHandler;
import com.topper.dex.ehandling.EncodedTypeAddrPair;
import com.topper.dex.ehandling.Leb128;
import com.topper.helpers.BufferHelper;

public final class TestEncodedCatchHandler {
	
	private static final int SINGLE_TYPE = 1;
	private static final int SINGLE_TYPE_ALL = -SINGLE_TYPE;
	private static final int CATCH_ALL = 0;
	private static final int MULTIPLE_TYPES = 3;
	private static final int MULTIPLE_TYPES_ALL = -MULTIPLE_TYPES;
	
	private static final int CATCH_ALL_ADDR = 0x42;
	
	private void validate(final int expectedSize, final List<@NonNull EncodedTypeAddrPair> expectedList, final long expectedAddr) {
		
		final EncodedCatchHandler h = new EncodedCatchHandler(expectedSize, expectedList, expectedAddr);
		final ByteBuffer buf = ByteBuffer.wrap(h.getBytes()).order(ByteOrder.LITTLE_ENDIAN);
		
		assertEquals(expectedSize, Leb128.readSignedLeb128(buf.position(0)));
		
		final int base = Leb128.signedLeb128Size(expectedSize);
		final int size = buf.array().length - Leb128.signedLeb128Size(expectedSize) - ((expectedSize <= 0) ? Leb128.unsignedLeb128Size(expectedAddr) : 0);
		final byte @NonNull [] list = listToBuffer(expectedList);
		final byte @NonNull [] slice = BufferHelper.copyBuffer(buf.array(), base, base + size);
		assertArrayEquals(list, slice, "Expected: " + BufferHelper.bytesToString(list) + ", Got: " + BufferHelper.bytesToString(slice));
		
		if (expectedSize <= 0) {
			// If catch - all is around, then it must be checked.
			assertEquals((int)expectedAddr, Leb128.readUnsignedLeb128(buf.position(base + size)));
		}
		
		assertEquals(h.getByteSize(), h.getBytes().length);
		assertEquals(expectedSize, h.getSize());
		assertEquals(expectedList, h.getHandlers());
		assertEquals(expectedAddr, h.getCatchAllAddr());
	}
	
	private byte @NonNull [] listToBuffer(final List<@NonNull ? extends Bytable> expectedList) {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (@NonNull final Bytable b : expectedList) {
			out.writeBytes(b.getBytes());
		}
		return out.toByteArray();
	}
	
	@Test
	public void Given_SingleType_When_CreatingHandler_Expect_ValidHandler() {
		final EncodedTypeAddrPair ap = new EncodedTypeAddrPair(0, 0);
		validate(SINGLE_TYPE, ImmutableList.of(ap), 0);
	}
	
	@Test
	public void Given_NegativeSingleType_When_CreatingHandler_Expect_TwoHandlers() {
		final EncodedTypeAddrPair ap = new EncodedTypeAddrPair(0, 0);
		validate(SINGLE_TYPE_ALL, ImmutableList.of(ap), 0);
	}
	
	@Test
	public void Given_NegativeSingleTypeValidAllAddr_When_CreatingHandler_Expect_TwoHandlers() {
		final EncodedTypeAddrPair ap = new EncodedTypeAddrPair(0, 0);
		validate(SINGLE_TYPE_ALL, ImmutableList.of(ap), CATCH_ALL_ADDR);
	}
	
	@Test
	public void Given_OnlyAll_When_CreatingHandler_Expect_ValidHandler() {
		validate(CATCH_ALL, ImmutableList.of(), CATCH_ALL_ADDR);
	}
	
	@Test
	public void Given_MultipleTypes_When_CreatingHandler_Expect_MultipleHandlers() {
		
		final List<@NonNull EncodedTypeAddrPair> handlers = new LinkedList<>();
		for (int i = 0; i < MULTIPLE_TYPES; i++) {
			handlers.add(new EncodedTypeAddrPair(i, i));
		}
		validate(MULTIPLE_TYPES, handlers, 0);
	}
	
	@Test
	public void Given_MultipleTypesAll_When_CreatingHandler_Expect_MultipleHandlers() {
		
		final List<@NonNull EncodedTypeAddrPair> handlers = new LinkedList<>();
		for (int i = 0; i < Math.abs(MULTIPLE_TYPES_ALL); i++) {
			handlers.add(new EncodedTypeAddrPair(i, i));
		}
		validate(MULTIPLE_TYPES_ALL, handlers, CATCH_ALL_ADDR);
	}
	
	@Test
	public void Given_LessHandlersThanSize_When_CreatingHandler_Expect_IllegalArgumentException() {
		assertThrowsExactly(IllegalArgumentException.class, () -> validate(0, ImmutableList.of(new EncodedTypeAddrPair(0, 0)), 0));
	}
	
	@Test
	public void Given_MoreHandlersThanSize_When_CreatingHandler_Expect_IllegalArgumentException() {
		assertThrowsExactly(IllegalArgumentException.class, () -> validate(2, ImmutableList.of(new EncodedTypeAddrPair(0, 0)), 0));
	}
	
	@Test
	public void Given_ValidHandler_When_stringify_Expect_ValidString() {
		
		final List<@NonNull EncodedTypeAddrPair> handlers = new LinkedList<>();
		for (int i = 0; i < MULTIPLE_TYPES; i++) {
			handlers.add(new EncodedTypeAddrPair(i, i));
		}
		final EncodedCatchHandler h = new EncodedCatchHandler(MULTIPLE_TYPES_ALL, handlers, CATCH_ALL_ADDR);
		final String s = h.toString();
		assertNotNull(s);
		assertTrue(s.length() > 0);
	}
	
	// NOTE: Leb128.unsignedLeb128Size cannot handle negative values -> infinite loop
	@Test
	public void Given_MaxAddrAll_When_CreatingHandler_Expect_ValidHandler() {
		
		// -1_10 = 0xffffffff_16
		validate(CATCH_ALL, ImmutableList.of(), Integer.toUnsignedLong(-1));
	}
	
	@Test
	public void Given_NegativeAddr_When_CreatingHandler_Expect_IllegalArgumentException() {
		assertThrowsExactly(IllegalArgumentException.class, () -> validate(CATCH_ALL, ImmutableList.of(), -1));
	}
	
	@Test
	public void Given_LongAddr_When_CreatingHandler_Expect_IllegalArgumentException() {
		assertThrowsExactly(IllegalArgumentException.class, () -> validate(CATCH_ALL, ImmutableList.of(), Long.MAX_VALUE));
	}
}