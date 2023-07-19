package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction21c;
import org.jf.dexlib2.iface.reference.Reference;

import com.topper.dex.decompiler.references.BufferedReference;

public class BufferedInstruction21c extends BufferedInstruction implements Instruction21c {

	private final int registerA;
	private final Reference reference;
	private final int referenceType;
	
	public BufferedInstruction21c(final DexBuffer buffer, final Opcode opcode, final int instructionStart, final DexBackedDexFile file) {
		super(opcode, instructionStart);
		
		this.registerA = buffer.readUbyte(instructionStart + 1);
		this.reference = BufferedReference.makeReference(buffer, this.getOpcode().referenceType, buffer.readUshort(instructionStart + 2), file);
		this.referenceType = this.getOpcode().referenceType;
	}

	@Override
	public int getRegisterA() {
		return this.registerA;
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
