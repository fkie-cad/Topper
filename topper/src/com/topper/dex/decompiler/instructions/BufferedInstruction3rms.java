package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction3rms;

public class BufferedInstruction3rms extends BufferedInstruction implements Instruction3rms {

	private final int registerCount;
	private final int startRegister;
	private final int vtableIndex;
	
	public BufferedInstruction3rms(final DexBuffer buffer, final Opcode opcode, final int instructionStart) {
		super(opcode, instructionStart);
		
		this.registerCount = buffer.readUbyte(instructionStart + 1);
		this.startRegister = buffer.readUshort(instructionStart + 4);
		this.vtableIndex = buffer.readUshort(instructionStart + 2);
	}
	
    @Override public int getRegisterCount() {
        return this.registerCount;
    }

    @Override
    public int getStartRegister() {
        return this.startRegister;
    }

    @Override
    public int getVtableIndex() {
        return this.vtableIndex;
    }
}