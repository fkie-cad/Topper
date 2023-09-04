package com.topper.commands.attack;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;

import com.topper.configuration.ConfigManager;
import com.topper.dex.ehandling.Bytable;
import com.topper.helpers.BufferHelper;

public final class PackedSwitchDispatcher implements Dispatcher {

	@NonNull
	private static final Opcodes opcodes = ConfigManager.get().getDecompilerConfig().getOpcodes();

	private final int offset;
	private final int exceptionVregIndex;
	private final int exceptionTypeIndex;
	private final int pcVregIndex;

	@NonNull
	private final List<@NonNull Integer> gadgets;
	
	private int dispatcherOffset;

	public PackedSwitchDispatcher(final int offset, final int exceptionVregIndex, final int exceptionTypeIndex,
			final int pcVregIndex, @NonNull final List<@NonNull Integer> gadgets) {
		if (offset < 0) {
			throw new IllegalArgumentException("Offset of dispatcher must be non - negative.");
		}
		if (exceptionVregIndex < 0) {
			throw new IllegalArgumentException("Exception vreg must be non - negative.");
		}
		if (exceptionTypeIndex < 0) {
			throw new IllegalArgumentException("Exception type must be non - negative.");
		}
		if (pcVregIndex < 0) {
			throw new IllegalArgumentException("Virtual program counter vreg must be non - negative.");
		}

		this.offset = offset;
		this.exceptionVregIndex = exceptionVregIndex;
		this.exceptionTypeIndex = exceptionTypeIndex;
		this.pcVregIndex = pcVregIndex;
		this.gadgets = gadgets;
		
		this.dispatcherOffset = -1;
	}

	public PackedSwitchDispatcher(final int offset, final int exceptionTypeIndex,
			@NonNull final List<@NonNull Integer> gadgets) {
		this(offset, 0, exceptionTypeIndex, 1, gadgets);
	}

	@SuppressWarnings("null")
	@Override
	public final byte @NonNull [] payload() {
		final ByteArrayOutputStream buf = new ByteArrayOutputStream();

		Bytable insn;

		// new-instance v{exceptionVregIndex}, {exceptionTypeIndex}
		insn = new NewInstance((byte) (this.exceptionVregIndex & 0xff), (short) (this.exceptionTypeIndex & 0xffff));
		buf.writeBytes(insn.getBytes());

		// const/4 v{pcVregIndex}, #-1
		insn = new Const4((byte) this.pcVregIndex, (byte) -1);
		buf.writeBytes(insn.getBytes());

		this.dispatcherOffset = buf.size();

		// add-int/lit8 v{pcVregIndex}, v{pcVregIndex}, #+1
		insn = new AddIntLit8((byte) (this.pcVregIndex & 0xff), (byte) (this.pcVregIndex & 0xff), (byte) 1);
		buf.writeBytes(insn.getBytes());

		final int switchOffset = this.offset + buf.size();

		// packed-switch v{pcVregIndex}, #+3
		insn = new PackedSwitch((byte)this.pcVregIndex, 3);
		buf.writeBytes(insn.getBytes());
		
		// Generate jump table
		final ByteBuffer table = ByteBuffer.allocate(this.gadgets.size() << 2).order(ByteOrder.LITTLE_ENDIAN);
		for (final Integer gadget : this.gadgets) {
			try {
				table.put(toRelativeOffset(gadget, switchOffset));
			} catch (final IllegalArgumentException e) {
				throw new IllegalArgumentException(
						String.format("Gadget at %#08x is invalid: " + e.getMessage(), gadget));
			}
		}
		insn = new PackedSwitchPayload(table.array());
		buf.writeBytes(insn.getBytes());

		return buf.toByteArray();
	}
	
	@Override
	public final int dispatcherOffset() {
		if (this.dispatcherOffset < 0) {
			throw new UnsupportedOperationException("Cannot access dispatcher offset before payload generation.");
		}
		return this.dispatcherOffset;
	}

	private static final byte @NonNull [] toRelativeOffset(final int off, final int base) {
		if ((off - base) % 2 != 0) {
			throw new IllegalArgumentException(String.format(
					"Distance of offset %#08x to base %#08x cannot be expressed in code units (not divisible by 2).",
					off, base));
		}
		return BufferHelper.intToByteArray((off - base) / 2);
	}

	private static class NewInstance implements Bytable {

		private final byte vregIndex;
		private final short typeIndex;

		public NewInstance(final byte vregIndex, final short typeIndex) {
			this.vregIndex = vregIndex;
			this.typeIndex = typeIndex;
		}

		@SuppressWarnings("null")
		@Override
		public byte @NonNull [] getBytes() {
			return ByteBuffer.allocate(this.getByteSize()).order(ByteOrder.LITTLE_ENDIAN)
					.put((byte) (opcodes.getOpcodeValue(Opcode.NEW_INSTANCE) & 0xff)).put(this.vregIndex)
					.putShort(this.typeIndex).array();
		}

		@Override
		public int getByteSize() {
			return Opcode.NEW_INSTANCE.format.size;
		}
	}

	private static class Const4 implements Bytable {

		private final byte vregIndex;
		private final byte constant;

		public Const4(final byte vregIndex, final byte constant) {
			this.vregIndex = (byte) (vregIndex & 0xf);
			this.constant = (byte) (constant & 0xf);
		}

		@SuppressWarnings("null")
		@Override
		public byte @NonNull [] getBytes() {
			return ByteBuffer.allocate(this.getByteSize()).order(ByteOrder.LITTLE_ENDIAN)
					.put((byte) (opcodes.getOpcodeValue(Opcode.CONST_4) & 0xff))
					.put((byte) ((this.constant << 4) | this.vregIndex)).array();
		}

		@Override
		public int getByteSize() {
			return Opcode.CONST_4.format.size;
		}
	}

	private static class AddIntLit8 implements Bytable {

		private final byte indexA;
		private final byte indexB;
		private final byte constant;

		public AddIntLit8(final byte indexA, final byte indexB, final byte constant) {
			this.indexA = indexA;
			this.indexB = indexB;
			this.constant = constant;
		}

		@SuppressWarnings("null")
		@Override
		public byte @NonNull [] getBytes() {
			return ByteBuffer.allocate(this.getByteSize()).order(ByteOrder.LITTLE_ENDIAN)
					.put((byte) (opcodes.getOpcodeValue(Opcode.ADD_INT_LIT8) & 0xff)).put(this.indexA).put(this.indexB)
					.put(this.constant).array();
		}

		@Override
		public int getByteSize() {
			return Opcode.ADD_INT_LIT8.format.size;
		}
	}
	
	private static class PackedSwitch implements Bytable {

		private final byte vregIndex;
		private final int offset;

		public PackedSwitch(final byte vregIndex, final int offset) {
			this.vregIndex = vregIndex;
			this.offset = offset;
		}

		@SuppressWarnings("null")
		@Override
		public byte @NonNull [] getBytes() {
			return ByteBuffer.allocate(this.getByteSize()).order(ByteOrder.LITTLE_ENDIAN)
					.put((byte) (opcodes.getOpcodeValue(Opcode.PACKED_SWITCH) & 0xff))
					.put(this.vregIndex)
					.putInt(this.offset)
					.array();
		}

		@Override
		public int getByteSize() {
			return Opcode.PACKED_SWITCH.format.size;
		}
	}
	
	private static class PackedSwitchPayload implements Bytable {
		
		private final byte @NonNull [] contents;

		public PackedSwitchPayload(final byte @NonNull [] contents) {
			this.contents = contents;
		}

		@SuppressWarnings("null")
		@Override
		public byte @NonNull [] getBytes() {
			return ByteBuffer.allocate(this.getByteSize()).order(ByteOrder.LITTLE_ENDIAN)
					.putShort(opcodes.getOpcodeValue(Opcode.PACKED_SWITCH_PAYLOAD))
					.putShort((short)(this.contents.length / 4))
					.putInt(0)
					.put(this.contents)
					.array();
		}

		@Override
		public int getByteSize() {
			return 2 + 2 + 4 + this.contents.length;
		}
	}
}