package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction4rcc;
import org.jf.dexlib2.iface.reference.Reference;

import com.topper.dex.decompiler.references.BufferedReference;

public class BufferedInstruction4rcc extends BufferedInstruction implements Instruction4rcc {

	private final int registerCount;
	private final int startRegister;
	private final Reference reference;
	private final int referenceType;
	private final Reference reference2;
	private final int referenceType2;
	
	public BufferedInstruction4rcc(final DexBuffer buffer, final Opcode opcode, final int instructionStart, final DexBackedDexFile file) {
		super(opcode, instructionStart);
		
		this.registerCount = buffer.readUbyte(instructionStart + 1);
		this.startRegister = buffer.readUshort(instructionStart + 4);
		this.reference = BufferedReference.makeReference(buffer, this.getOpcode().referenceType,
                buffer.readUshort(instructionStart + 2), file);
		this.referenceType = this.getOpcode().referenceType;
		this.reference2 = BufferedReference.makeReference(buffer, this.getOpcode().referenceType2,
                buffer.readUshort(instructionStart + 6), file);
		this.referenceType2 = this.getOpcode().referenceType2;
	}
	
    @Override public int getRegisterCount() {
        return this.registerCount;
    }

    @Override
    public int getStartRegister() {
        return this.startRegister;
    }

    @Override
    public Reference getReference() {
        return this.reference;
    }

    @Override
    public int getReferenceType() {
    	return this.referenceType;
    }

    @Override
    public Reference getReference2() {
        return this.reference2;
    }

    @Override
    public int getReferenceType2() {
        return this.referenceType2;
    }
}