package com.topper.tests.dex.ehandling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.topper.dex.ehandling.TryItem;

public final class TestTryItem {

	private void validate(final long startAddress, final int insnCount, final int handlerOffset) {

		final TryItem t = new TryItem(startAddress, insnCount, handlerOffset);
		final ByteBuffer buffer = ByteBuffer.wrap(t.getBytes()).order(ByteOrder.LITTLE_ENDIAN);

		assertEquals(startAddress, Integer.toUnsignedLong(buffer.getInt()));
		assertEquals(insnCount, Short.toUnsignedInt(buffer.getShort()));
		assertEquals(handlerOffset, Short.toUnsignedInt(buffer.getShort()));

		assertEquals(startAddress, t.getStartAddress());
		assertEquals(insnCount, t.getInsnCount());
		assertEquals(handlerOffset, t.getHandlerOffset());
	}
	
	@Test
	public void Given_ValidArgsZeroes_When_Creating_Expect_ValidTryItem() {
		this.validate(0, 0, 0);
	}

	@Test
	public void Given_ValidArgsNonZero_When_Creating_Expect_ValidTryItem() {
		this.validate(0x42, 0x42, 0x42);
	}

	private static Stream<Arguments> providerBounds(final int[] shorts, final long[] ints) {

		final int NUM_BITS = 6;
		final List<Arguments> args = new LinkedList<>();
		for (int i = 0; i < (1 << NUM_BITS); i++) {
			args.add(Arguments.of(ints[(i & 1)], shorts[(i & 2) >> 1], shorts[(i & 4) >> 2]));
		}

		return args.stream();
	}

	private static Stream<Arguments> providerOutOfBoundsInts() {
		final int[] shorts = { 0, 1 << Short.SIZE };
		final long[] ints = { 0, 1L << Integer.SIZE };
		return providerBounds(shorts, ints);
	}

	@ParameterizedTest
	@MethodSource("providerOutOfBoundsInts")
	public void Given_OutOfBoundsAllCombs_When_Creating_Expect_IllegalArgumentException(final long startAddress, final int insnCount, final int handlerOffset) {
		if (startAddress == 0 && insnCount == 0 && handlerOffset == 0) {
			return;
		}
		assertThrowsExactly(IllegalArgumentException.class,
				() -> new TryItem(startAddress, insnCount, handlerOffset));
	}

	private static Stream<Arguments> providerMaxBoundsInts() {
		final int[] shorts = { 0, (1 << Short.SIZE) - 1 };
		final long[] ints = { 0, (1 << Integer.SIZE) - 1 };
		return providerBounds(shorts, ints);
	}

	@ParameterizedTest
	@MethodSource("providerMaxBoundsInts")
	public void Given_BoundsValuesAllCombs_When_Creating_Expect_ValidTryItem(final long startAddress, final int insnCount, final int handlerOffset) {
		validate(startAddress, insnCount, handlerOffset);
	}
	
	@Test
	public void Given_ValidCodeItem_When_stringify_Expect_ValidString() {
		final TryItem t = new TryItem(0, 0, 0);
		final String s = t.toString();
		assertNotNull(s);
		assertTrue(s.length() > 0);
	}
}