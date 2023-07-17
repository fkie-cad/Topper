package com.topper.dex.decompiler.instructions;

import java.util.LinkedList;
import java.util.List;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.SparseSwitchPayload;

public class BufferedSparseSwitchPayload extends BufferedInstruction implements SparseSwitchPayload {

    private static final int ELEMENT_COUNT_OFFSET = 2;
    private static final int KEYS_OFFSET = 4;

    private final int elementCount;
    private final List<BufferedSwitchElement> switchElements;
    private final int codeUnits;

	public BufferedSparseSwitchPayload(final DexBuffer buffer, final int instructionStartOffset) {
		super(Opcode.SPARSE_SWITCH_PAYLOAD);
		
        this.elementCount = buffer.readUshort(instructionStartOffset + ELEMENT_COUNT_OFFSET);
        

        this.switchElements = new LinkedList<BufferedSwitchElement>();
        for (int index = 0; index < this.elementCount; index++) {
        	this.switchElements.add(new BufferedSwitchElement(
        			buffer.readInt(instructionStartOffset + KEYS_OFFSET + index*4),
        			buffer.readInt(instructionStartOffset + KEYS_OFFSET + elementCount*4 + index*4)
        	));
        }
        
        this.codeUnits = 2 + this.getElementCount()*4;
	}
	
	public final int getElementCount() {
		return this.elementCount;
	}

    @Override
    public List<BufferedSwitchElement> getSwitchElements() {
        return this.switchElements;
    }

    @Override public int getCodeUnits() { return this.codeUnits; }
}