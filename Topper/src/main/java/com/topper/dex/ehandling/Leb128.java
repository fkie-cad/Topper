package com.topper.dex.ehandling;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Reads and writes DWARFv3 LEB 128 signed and unsigned integers. See DWARF v3
 * section 7.6.
 * 
 * Taken and adjusted from: https://android.googlesource.com/platform/libcore/+/522b917/dex/src/main/java/com/android/dex/Leb128.java
 */
public final class Leb128 {
    private Leb128() {
    }
    
    /**
     * Gets the number of bytes in the unsigned LEB128 encoding of the
     * given value.
     *
     * @param value the value in question
     * @return its write size, in bytes
     */
    public static int unsignedLeb128Size(int value) {
        // TODO: This could be much cleverer.
        int remaining = value >> 7;
        int count = 0;
        while (remaining != 0) {
            remaining >>= 7;
            count++;
        }
        return count + 1;
    }
    
    /**
     * Gets the number of bytes in the unsigned LEB128 encoding of the
     * given value.
     *
     * @param value The long value in question.
     * @return its write size, in bytes
     */
    public static int unsignedLeb128Size(final long value) {
        // TODO: This could be much cleverer.
        long remaining = value >> 7;
        int count = 0;
        while (remaining != 0) {
            remaining >>= 7;
            count++;
        }
        return count + 1;
    }
    
    /**
     * Gets the number of bytes in the signed LEB128 encoding of the
     * given value.
     *
     * @param value the value in question
     * @return its write size, in bytes
     */
    public static int signedLeb128Size(int value) {
        // TODO: This could be much cleverer.
        int remaining = value >> 7;
        int count = 0;
        boolean hasMore = true;
        int end = ((value & Integer.MIN_VALUE) == 0) ? 0 : -1;
        while (hasMore) {
            hasMore = (remaining != end)
                || ((remaining & 1) != ((value >> 6) & 1));
            value = remaining;
            remaining >>= 7;
            count++;
        }
        return count;
    }
    
     /**
     * Gets the number of bytes in the signed LEB128 encoding of the
     * given value.
     *
     * @param value the value in question
     * @return its write size, in bytes
     */
    public static int signedLeb128Size(long value) {
        // TODO: This could be much cleverer.
        long remaining = value >> 7;
        int count = 0;
        boolean hasMore = true;
        int end = ((value & Integer.MIN_VALUE) == 0) ? 0 : -1;
        while (hasMore) {
            hasMore = (remaining != end)
                || ((remaining & 1) != ((value >> 6) & 1));
            value = remaining;
            remaining >>= 7;
            count++;
        }
        return count;
    }
    
    /**
     * Reads an signed integer from {@code in}.
     */
    public static int readSignedLeb128(@NonNull final ByteBuffer in) {
        int result = 0;
        int cur;
        int count = 0;
        int signBits = -1;
        do {
            cur = in.get() & 0xff;
            result |= (cur & 0x7f) << (count * 7);
            signBits <<= 7;
            count++;
        } while (((cur & 0x80) == 0x80) && count < 5);
        if ((cur & 0x80) == 0x80) {
            throw new IllegalArgumentException("invalid LEB128 sequence");
        }
        // Sign extend if appropriate
        if (((signBits >> 1) & result) != 0 ) {
            result |= signBits;
        }
        return result;
    }
    
    /**
     * Reads an signed integer from {@code in}.
     */
    public static long readSignedLeb128Long(@NonNull final ByteBuffer in) {
        long result = 0;
        int cur;
        int count = 0;
        int signBits = -1;
        do {
            cur = in.get() & 0xff;
            result |= (cur & 0x7f) << (count * 7);
            signBits <<= 7;
            count++;
        } while (((cur & 0x80) == 0x80) && count < 5);
        if ((cur & 0x80) == 0x80) {
            throw new IllegalArgumentException("invalid LEB128 sequence");
        }
        // Sign extend if appropriate
        if (((signBits >> 1) & result) != 0 ) {
            result |= signBits;
        }
        return result;
    }
    
    /**
     * Reads an unsigned integer from {@code in}.
     */
    public static int readUnsignedLeb128(@NonNull final ByteBuffer in) {
        int result = 0;
        int cur;
        int count = 0;
        do {
            cur = in.get() & 0xff;
            result |= (cur & 0x7f) << (count * 7);
            count++;
        } while (((cur & 0x80) == 0x80) && count < 5);
        if ((cur & 0x80) == 0x80) {
            throw new IllegalArgumentException("invalid LEB128 sequence");
        }
        return result;
    }
    
    /**
     * Reads an unsigned integer from {@code in}.
     */
    public static long readUnsignedLeb128Long(@NonNull final ByteBuffer in) {
        long result = 0;
        int cur;
        int count = 0;
        do {
            cur = in.get() & 0xff;
            result |= (cur & 0x7f) << (count * 7);
            count++;
        } while (((cur & 0x80) == 0x80) && count < 5);
        if ((cur & 0x80) == 0x80) {
            throw new IllegalArgumentException("invalid LEB128 sequence");
        }
        return result;
    }
    
    /**
     * Writes {@code value} as an unsigned integer to {@code out}, starting at
     * {@code offset}. Returns the number of bytes written.
     */
    public static void writeUnsignedLeb128(@NonNull final ByteBuffer out, int value) {
        int remaining = value >>> 7;
        while (remaining != 0) {
            out.put((byte) ((value & 0x7f) | 0x80));
            value = remaining;
            remaining >>>= 7;
        }
        out.put((byte) (value & 0x7f));
    }
    
    /**
     * Writes {@code value} as an unsigned integer to {@code out}, starting at
     * {@code offset}. Returns the number of bytes written.
     */
    public static void writeUnsignedLeb128(@NonNull final ByteBuffer out, long value) {
        long remaining = value >>> 7;
        while (remaining != 0) {
            out.put((byte) ((value & 0x7f) | 0x80));
            value = remaining;
            remaining >>>= 7;
        }
        out.put((byte) (value & 0x7f));
    }
    
    /**
     * Writes {@code value} as a signed integer to {@code out}, starting at
     * {@code offset}. Returns the number of bytes written.
     */
    public static void writeSignedLeb128(@NonNull final ByteBuffer out, long value) {
        long remaining = value >> 7;
        boolean hasMore = true;
        int end = ((value & Integer.MIN_VALUE) == 0) ? 0 : -1;
        while (hasMore) {
            hasMore = (remaining != end)
                    || ((remaining & 1) != ((value >> 6) & 1));
            out.put((byte) ((value & 0x7f) | (hasMore ? 0x80 : 0)));
            value = remaining;
            remaining >>= 7;
        }
    }
}