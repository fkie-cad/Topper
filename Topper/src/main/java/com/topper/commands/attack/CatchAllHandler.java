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

	private final int targetMethodOffset;
	private final int dispatcherOffset;
	
	@NonNull
	private final EncodedCatchHandler encodedHandler;
	
	@NonNull
	private final EncodedCatchHandlerList handlerList;
	
	@NonNull
	private final TryItem tryItem;
	
	/**
	 * Creates a new catch - all exception handler for .dex files.
	 * 
	 * @throws IllegalArgumentException If either of the parameters is negative, or
	 * 	the relative offset of the dispatcher to the beginning of the method is not
	 * 	divisible by two, i.e. cannot be expressed in code units.
	 * */
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
		this.tryItem = new TryItem(0, (short)-1, (short)1);
	}

	public final int getTargetMethodOffset() {
		return this.targetMethodOffset;
	}

	public final int getDispatcherOffset() {
		return this.dispatcherOffset;
	}

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
}