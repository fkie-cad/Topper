package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction3rc;
import org.jf.dexlib2.iface.reference.Reference;

import com.topper.dex.decompiler.references.BufferedReference;

public class BufferedInstruction3rc extends BufferedInstruction implements Instruction3rc {
	
	private final int registerCount;
	private final int startRegister;
	private final Reference reference;
	private final int referenceType;

	public BufferedInstruction3rc(final DexBuffer buffer, final Opcode opcode, final int instructionStart, final DexBackedDexFile file) {
		super(opcode, instructionStart);
		
		this.registerCount = buffer.readUbyte(instructionStart + 1);
		this.startRegister = buffer.readUshort(instructionStart + 4);
		this.reference = BufferedReference.makeReference(buffer, this.getOpcode().referenceType,
                buffer.readUshort(instructionStart + 2), file);
		this.referenceType = this.getOpcode().referenceType;
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
}