package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction22s;
import org.jf.util.NibbleUtils;

public class BufferedInstruction22s extends BufferedInstruction implements Instruction22s {

	private final int registerA;
	private final int registerB;
	private final int narrowLiteral;
	
	public BufferedInstruction22s(final DexBuffer buffer, final Opcode opcode, final int instructionStart) {
		super(opcode, instructionStart);
		
		this.registerA = NibbleUtils.extractLowUnsignedNibble(buffer.readByte(instructionStart + 1));
		this.registerB = NibbleUtils.extractHighUnsignedNibble(buffer.readByte(instructionStart + 1));
		this.narrowLiteral = buffer.readShort(instructionStart + 2);
	}

    @Override
    public int getRegisterA() {
        return this.registerA;
    }

    @Override
    public int getRegisterB() {
        return this.registerB;
    }

    @Override public int getNarrowLiteral() { return this.narrowLiteral; }
    @Override public long getWideLiteral() { return getNarrowLiteral(); }
}