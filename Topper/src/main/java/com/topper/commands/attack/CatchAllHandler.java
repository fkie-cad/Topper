package com.topper.commands.attack;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.dex.ehandling.Bytable;
import com.topper.dex.ehandling.EncodedCatchHandler;
import com.topper.dex.ehandling.EncodedCatchHandlerList;
import com.topper.dex.ehandling.TryItem;

public final class CatchAllHandler implements Bytable {

	private int targetMethodOffset;
	private int dispatcherOffset;

	@NonNull
	private EncodedCatchHandler encodedHandler;

	@NonNull
	private EncodedCatchHandlerList handlerList;

	@NonNull
	private TryItem tryItem;

	/**
	 * Creates a new catch - all exception handler for .dex files.
	 * 
	 * @param targetMethodOffset Offset of the method to be augmented with this
	 *                           handler. It must be relative to the beginning of
	 *                           the .dex file.
	 * @param dispatcherOffset   Offset of the {@link Dispatcher} relative to the
	 *                           beginning of the .dex file.
	 * @throws IllegalArgumentException If either of the parameters is negative, or
	 *                                  the relative offset of the dispatcher to the
	 *                                  beginning of the method is not divisible by
	 *                                  two, i.e. cannot be expressed in code units.
	 */
	public CatchAllHandler(final int targetMethodOffset, final int dispatcherOffset) {

		if (targetMethodOffset < 0) {
			throw new IllegalArgumentException("Method offset must be non - negative.");
		}
		if (dispatcherOffset < 0) {
			throw new IllegalArgumentException("Dispatcher offset must be non - negative.");
		}

		this.targetMethodOffset = targetMethodOffset;
		this.dispatcherOffset = dispatcherOffset;

		// Create encoded catch handler
		int handlerOffset = this.dispatcherOffset - (this.targetMethodOffset + 0x10);
		if (handlerOffset % 2 != 0) {
			throw new IllegalArgumentException("Handler offset cannot be expressed in terms of code units.");
		}
		handlerOffset >>= 1;
		this.encodedHandler = new EncodedCatchHandler(0, new LinkedList<>(), handlerOffset);

		// Create handler list
		final List<@NonNull EncodedCatchHandler> list = new LinkedList<>();
		list.add(this.encodedHandler);
		this.handlerList = new EncodedCatchHandlerList(list);

		// Create try item
		this.tryItem = new TryItem(Integer.toUnsignedLong(0), Short.toUnsignedInt((short) -1), Short.toUnsignedInt((short) 1));
	}

	/**
	 * Gets the offset of the method to be augmented with this handler. The offset
	 * is relative to a given .dex file.
	 */
	public final int getTargetMethodOffset() {
		return this.targetMethodOffset;
	}

	/**
	 * Gets the offset of the {@link Dispatcher} relative to the beginning of a
	 * given .dex file.
	 */
	public final int getDispatcherOffset() {
		return this.dispatcherOffset;
	}

	@SuppressWarnings("null")
	@Override
	public final byte @NonNull [] getBytes() {
		final ByteBuffer buffer = ByteBuffer.allocate(this.getByteSize()).order(ByteOrder.LITTLE_ENDIAN);

		buffer.put(this.tryItem.getBytes());
		buffer.put(this.handlerList.getBytes());

		return buffer.array();
	}

	@Override
	public final int getByteSize() {
		return this.tryItem.getByteSize() + this.handlerList.getByteSize();
	}

	/**
	 * Gets an upper bound on the number of bytes required by this handler.
	 */
	public static final int getByteSizeBound() {
		return 0x20;
	}

	@Override
	@NonNull
	public final String toString() {
		final StringBuilder b = new StringBuilder();
		b.append("Catch All Handler:" + System.lineSeparator());
		b.append(this.tryItem.toString());
		b.append(this.handlerList.toString());
		return "" + b.toString();
	}
}