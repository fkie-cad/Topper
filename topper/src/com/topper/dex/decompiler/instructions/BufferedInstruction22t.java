package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction22t;
import org.jf.util.NibbleUtils;

public class BufferedInstruction22t extends BufferedInstruction implements Instruction22t {

	private final int registerA;
	private final int registerB;
	private final int offset;
	
	public BufferedInstruction22t(final DexBuffer buffer, final Opcode opcode, final int instructionStartOffset) {
		super(opcode);
		
		this.registerA = NibbleUtils.extractLowUnsignedNibble(buffer.readByte(instructionStartOffset + 1));
		this.registerB = NibbleUtils.extractHighUnsignedNibble(buffer.readByte(instructionStartOffset + 1));
		this.offset = buffer.readShort(instructionStartOffset + 2);
	}

    @Override
    public int getRegisterA() {
        return this.registerA;
    }

    @Override
    public int getRegisterB() {
        return this.registerB;
    }

    @Override public int getCodeOffset() { return this.offset; }
}