package com.topper.helpers;

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
	
	public static final byte @NonNull [] intToByteArray(final int value) {
		return new byte[] { (byte) (value >>> 0), (byte) (value >>> 8), (byte) (value >>> 16), (byte) (value >>> 24) };
	}
	
	public static final byte @NonNull [] shortToByteArray(final short value) {
		return new byte[] { (byte) (value >>> 0), (byte) (value >>> 8) };
	}
	
	public static final byte @NonNull [] copyBuffer(final byte @NonNull [] src, final int from, final int to) {
		final byte @NonNull [] dst = new byte[src.length];
		System.arraycopy(src, from, dst, 0, to  - from);
		return dst;
	}
}