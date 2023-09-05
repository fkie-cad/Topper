package com.topper.tests.dex.ehandling;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.topper.dex.ehandling.EncodedCatchHandlerList;
import com.topper.dex.ehandling.EncodedTypeAddrPair;
import com.topper.dex.ehandling.Leb128;
import com.topper.helpers.BufferHelper;

public final class TestEncodedCatchHandlerList {

	private static final int AMOUNT_HANDLERS = 3;
	
	private void validate(final List<@NonNull EncodedCatchHandler> expectedHandlers) {
		
		final EncodedCatchHandlerList l = new EncodedCatchHandlerList(expectedHandlers);
		final ByteBuffer buffer = ByteBuffer.wrap(l.getBytes()).order(ByteOrder.LITTLE_ENDIAN);
		
		assertEquals(expectedHandlers.size(), Leb128.readUnsignedLeb128(buffer.position(0)));
		
		final int base = Leb128.unsignedLeb128Size(expectedHandlers.size());
		final byte @NonNull [] slice = BufferHelper.copyBuffer(buffer.array(), base, buffer.array().length);
		assertArrayEquals(listToBuffer(expectedHandlers), slice);
		
		assertEquals(expectedHandlers, l.getList());
		assertEquals(expectedHandlers.size(), l.getSize());
	}
	
	private byte @NonNull [] listToBuffer(final List<@NonNull ? extends Bytable> expectedList) {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (@NonNull final Bytable b : expectedList) {
			out.writeBytes(b.getBytes());
		}
		return out.toByteArray();
	}
	
	@Test
	public void Given_EmptyList_When_Creating_Expect_ValidList() {
		validate(ImmutableList.of());
	}
	
	@Test
	public void Given_MultipleHandlers_When_Creating_Expect_ValidList() {
		final List<@NonNull EncodedCatchHandler> handlers = new LinkedList<>();
		for (int i = 0; i < AMOUNT_HANDLERS; i++) {
			handlers.add(new EncodedCatchHandler(1, ImmutableList.of(new EncodedTypeAddrPair(i, i)), i));
		}
		validate(handlers);
	}
	
	@Test
	public void Given_MultipleHandlers_When_stringify_Expect_ValidString() {
		final List<@NonNull EncodedCatchHandler> handlers = new LinkedList<>();
		for (int i = 0; i < AMOUNT_HANDLERS; i++) {
			handlers.add(new EncodedCatchHandler(1, ImmutableList.of(new EncodedTypeAddrPair(i, i)), i));
		}
		final EncodedCatchHandlerList l = new EncodedCatchHandlerList(handlers);
		final String s = l.toString();
		assertNotNull(s);
		assertTrue(s.length() > 0);
	}
}