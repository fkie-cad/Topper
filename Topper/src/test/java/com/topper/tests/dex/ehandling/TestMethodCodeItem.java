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

import com.topper.dex.ehandling.MethodCodeItem;
import com.topper.helpers.BufferHelper;

public final class TestMethodCodeItem {

	private void validate(final int registersSize, final int insSize, final int outsSize, final int triesSize,
			final long debugInfoOffset, final long insnsSize) {

		final MethodCodeItem c = new MethodCodeItem(registersSize, insSize, outsSize, triesSize, debugInfoOffset,
				insnsSize);
		final ByteBuffer buffer = ByteBuffer.wrap(c.getBytes()).order(ByteOrder.LITTLE_ENDIAN);

		assertEquals(registersSize, Short.toUnsignedInt(buffer.getShort()));
		assertEquals(insSize, Short.toUnsignedInt(buffer.getShort()));
		assertEquals(outsSize, Short.toUnsignedInt(buffer.getShort()));
		assertEquals(triesSize, Short.toUnsignedInt(buffer.getShort()));
		assertEquals(debugInfoOffset, Integer.toUnsignedLong(buffer.getInt()));
		assertEquals(insnsSize, Integer.toUnsignedLong(buffer.getInt()));

		assertEquals(registersSize, c.getRegistersSize());
		assertEquals(insSize, c.getInsSize());
		assertEquals(outsSize, c.getOutsSize());
		assertEquals(triesSize, c.getTriesSize());
		assertEquals(debugInfoOffset, c.getDebugInfoOffset());
		assertEquals(insnsSize, c.getInsnsSize());
	}
	
	@Test
	public void Given_ValidArgsZeroes_When_Creating_Expect_ValidCodeItem() {
		this.validate(0, 0, 0, 0, 0, 0);
	}

	@Test
	public void Given_ValidArgsNonZero_When_Creating_Expect_ValidCodeItem() {
		this.validate(0x42, 0x42, 0x42, 0x42, 0x42, 0x42);
	}

	private static Stream<Arguments> providerBounds(final int[] shorts, final long[] ints) {

		final int NUM_BITS = 6;
		final List<Arguments> args = new LinkedList<>();
		for (int i = 0; i < (1 << NUM_BITS); i++) {
			args.add(Arguments.of(shorts[(i & 1)], shorts[(i & 2) >> 1], shorts[(i & 4) >> 2], shorts[(i & 8) >> 3],
					ints[(i & 16) >> 4], ints[(i & 32) >> 5]));
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
	public void Given_OutOfBoundsAllCombs_When_Creating_Expect_IllegalArgumentException(final int registersSize,
			final int insSize, final int outsSize, final int triesSize, final long debugInfoOffset,
			final long insnsSize) {
		if (registersSize == 0 && insSize == 0 && outsSize == 0 && triesSize == 0 && debugInfoOffset == 0 && insnsSize == 0) {
			return;
		}
		assertThrowsExactly(IllegalArgumentException.class,
				() -> new MethodCodeItem(registersSize, insSize, outsSize, triesSize, debugInfoOffset, insnsSize));
	}

	private static Stream<Arguments> providerMaxBoundsInts() {
		final int[] shorts = { 0, (1 << Short.SIZE) - 1 };
		final long[] ints = { 0, (1 << Integer.SIZE) - 1 };
		return providerBounds(shorts, ints);
	}

	@ParameterizedTest
	@MethodSource("providerMaxBoundsInts")
	public void Given_BoundsValuesAllCombs_When_Creating_Expect_ValidCodeItem(final int registersSize,
			final int insSize, final int outsSize, final int triesSize, final long debugInfoOffset,
			final long insnsSize) {
		validate(registersSize, insSize, outsSize, triesSize, debugInfoOffset, insnsSize);
	}

	@Test
	public void Given_TooSmallBuffer_When_Creating_Expect_IllegalArgumentException() {
		assertThrowsExactly(IllegalArgumentException.class,
				() -> new MethodCodeItem(new byte[MethodCodeItem.CODE_ITEM_SIZE - 1]));
	}
	
	private static Stream<Arguments> providerMaxBoundsBuffer() {
		return providerMaxBoundsInts().map(arg -> Arguments.of(BufferHelper.intArrayToByteArray(arg.get())));
	}
	
	@ParameterizedTest
	@MethodSource("providerMaxBoundsBuffer")
	public void Given_MaxBoundsBufferAllCombs_When_Creating_Expect_ValidCodeItem(final byte[] buffer) {
		new MethodCodeItem(buffer);
	}
	
	@Test
	public void Given_ValidCodeItem_When_stringify_Expect_ValidString() {
		final MethodCodeItem c = new MethodCodeItem(0, 0, 0, 0, 0, 0);
		final String s = c.toString();
		assertNotNull(s);
		assertTrue(s.length() > 0);
	}
}