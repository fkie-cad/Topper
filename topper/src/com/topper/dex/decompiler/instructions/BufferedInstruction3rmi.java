package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction3rmi;

public class BufferedInstruction3rmi extends BufferedInstruction implements Instruction3rmi {

	private final int registerCount;
	private final int startRegister;
	private final int inlineIndex;
	
	public BufferedInstruction3rmi(final DexBuffer buffer, final Opcode opcode, final int instructionStartOffset) {
		super(opcode);
		
		this.registerCount = buffer.readUbyte(instructionStartOffset + 1);
		this.startRegister = buffer.readUshort(instructionStartOffset + 4);
		this.inlineIndex = buffer.readUshort(instructionStartOffset + 2);
	}
	
    @Override public int getRegisterCount() {
        return this.registerCount;
    }

    @Override
    public int getStartRegister() {
        return this.startRegister;
    }

    @Override
    public int getInlineIndex() {
        return this.inlineIndex;
    }
}