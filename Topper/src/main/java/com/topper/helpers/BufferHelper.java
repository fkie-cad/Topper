package com.topper.helpers;

import java.io.ByteArrayOutputStream;

import org.eclipse.jdt.annotation.NonNull;

public class BufferHelper {
	
	@SuppressWarnings("null")
	@NonNull
	public static final String bytesToString(final byte @NonNull [] buffer) {
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < buffer.length; i++) {
			final byte b = buffer[i];
			builder.append(String.format("%02x", b));
			
			if (i < buffer.length - 1) {
				builder.append(' ');
			}
		}
		return builder.toString();
	}
	
	@SuppressWarnings("null")
	@NonNull
	public static final String bytesToPythonString(final byte @NonNull [] buffer) {
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < buffer.length; i++) {
			final byte b = buffer[i];
			builder.append(String.format("\\x%02x", b));
		}
		return builder.toString();
	}
	
	public static final byte @NonNull [] longToByteArray(final long value) {
		return new byte[] { (byte) (value >>> 0), (byte) (value >>> 8), (byte) (value >>> 16), (byte) (value >>> 24),
				(byte) (value >>> 32), (byte) (value >>> 40), (byte) (value >>> 48), (byte) (value >>> 56)};
	}
	
	public static final byte @NonNull [] intToByteArray(final int value) {
		return new byte[] { (byte) (value >>> 0), (byte) (value >>> 8), (byte) (value >>> 16), (byte) (value >>> 24) };
	}
	
	public static final byte @NonNull [] shortToByteArray(final short value) {
		return new byte[] { (byte) (value >>> 0), (byte) (value >>> 8) };
	}
	
	public static final byte @NonNull [] copyBuffer(final byte @NonNull [] src, final int from, final int to) {
		if (from > to) {
			throw new IllegalArgumentException("from must not exceed to.");
		}
		final byte @NonNull [] dst = new byte[to - from];
		System.arraycopy(src, from, dst, 0, to  - from);
		return dst;
	}
	
	public static boolean isUnsignedInt(final long l) {
		return (l >= 0 && l <= (1L << Integer.SIZE) - 1);
	}
	
	public static boolean isUnsignedShort(final int i) {
		return (i >= 0 && i <= (1L << Short.SIZE) - 1);
	}
	
	public static byte @NonNull [] intArrayToByteArray(@NonNull final Object @NonNull [] objects) {
		
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (@NonNull final Object obj : objects) {
			if (obj instanceof Integer) {
				out.writeBytes(intToByteArray((int)obj));
			} else if (obj instanceof Long) {
				out.writeBytes(longToByteArray((long)obj));
			} else if (obj instanceof Short) {
				out.writeBytes(shortToByteArray((short)obj));
			} else if (obj instanceof Byte) {
				out.writeBytes(new byte[] {(byte)obj});
			} else {
				throw new IllegalArgumentException("Object " + obj + " is not an integer type or similar.");
			}
		}
		return out.toByteArray();
	}
}