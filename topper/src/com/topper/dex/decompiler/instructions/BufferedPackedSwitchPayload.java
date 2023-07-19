package com.topper.dex.decompiler.instructions;

import java.util.LinkedList;
import java.util.List;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.PackedSwitchPayload;

public class BufferedPackedSwitchPayload extends BufferedInstruction implements PackedSwitchPayload {

    private static final int ELEMENT_COUNT_OFFSET = 2;
    private static final int FIRST_KEY_OFFSET = 4;
    private static final int TARGETS_OFFSET = 8;
    
    private final int elementCount;
    private final List<BufferedSwitchElement> switchElements;
    private final int codeUnits;
	
	public BufferedPackedSwitchPayload(final DexBuffer buffer, final int instructionStart) {
		super(Opcode.PACKED_SWITCH_PAYLOAD, instructionStart);
		
        this.elementCount = buffer.readUshort(instructionStart + ELEMENT_COUNT_OFFSET);
        
        final int firstKey = buffer.readInt(instructionStart + FIRST_KEY_OFFSET);
        
        this.switchElements = new LinkedList<BufferedSwitchElement>();
        for (int index = 0; index < this.elementCount; index++) {
        	this.switchElements.add(new BufferedSwitchElement(
        			firstKey + index,
        			buffer.readInt(instructionStart + TARGETS_OFFSET + index*4)
        	));
        }
        
        this.codeUnits = 4 + this.getElementCount()*2;
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