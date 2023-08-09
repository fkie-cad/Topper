package com.topper.dex.decompiler.instructions;

import java.util.List;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.dexbacked.util.FixedSizeList;
import org.jf.dexlib2.iface.instruction.formats.ArrayPayload;
import org.jf.util.ExceptionWithContext;

public class BufferedArrayPayload extends BufferedInstruction implements ArrayPayload {

    private static final int ELEMENT_WIDTH_OFFSET = 2;
    private static final int ELEMENT_COUNT_OFFSET = 4;
    private static final int ELEMENTS_OFFSET = 8;

    private final int elementWidth;
    private final int elementCount;
    private List<Number> arrayElements;

	public BufferedArrayPayload(final DexBuffer buffer, final int instructionStartOffset) {
		super(Opcode.ARRAY_PAYLOAD, instructionStartOffset);
		
        this.elementWidth = buffer.readUshort(instructionStartOffset + ELEMENT_WIDTH_OFFSET);
        this.elementCount = buffer.readSmallUint(instructionStartOffset + ELEMENT_COUNT_OFFSET);
        if (((long)elementWidth) * elementCount > Integer.MAX_VALUE) {
            throw new ExceptionWithContext("Invalid array-payload instruction: element width*count overflows");
        }
        
        //
        final int elementsStart = instructionStartOffset + ELEMENTS_OFFSET;

        abstract class ReturnedList extends FixedSizeList<Number> {
            @Override public int size() { return elementCount; }
        }

        switch (elementWidth) {
            case 1:
            	this.arrayElements =  new ReturnedList() {
                    @Override
                    public Number readItem(int index) {
                        return buffer.readByte(elementsStart + index);
                    }
                };
                break;
            case 2:
            	this.arrayElements =  new ReturnedList() {
                    @Override
                    public Number readItem(int index) {
                        return buffer.readShort(elementsStart + index*2);
                    }
                };
                break;
            case 4:
            	this.arrayElements =  new ReturnedList() {
                    @Override
                    public Number readItem(int index) {
                        return buffer.readInt(elementsStart + index*4);
                    }
                };
                break;
            case 8:
            	this.arrayElements =  new ReturnedList() {
                    @Override
                    public Number readItem(int index) {
                        return buffer.readLong(elementsStart + index*8);
                    }
                };
                break;
            default:
                throw new ExceptionWithContext("Invalid element width: %d", elementWidth);
        }

	}
	
	public final int getElementCount() {
		return this.elementCount;
	}

    @Override public int getElementWidth() { return elementWidth; }

    @Override
    public List<Number> getArrayElements() {
        return this.arrayElements;
    }

    @Override
    public int getCodeUnits() {
        return 4 + (this.elementWidth*this.elementCount + 1) / 2;
    }
}
