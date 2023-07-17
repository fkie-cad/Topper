package com.topper.dex.decompiler.references;

import org.jf.dexlib2.base.reference.BaseFieldReference;
import org.jf.dexlib2.dexbacked.raw.FieldIdItem;

public final class FieldReference extends BaseFieldReference {

	private static final String unknown = "<unknown>";
	
    private final int fieldIndex;

    public FieldReference(int fieldIndex) {
        this.fieldIndex = fieldIndex;
    }
    
    public final int getFieldIndex() {
    	return this.fieldIndex;
    }

    @Override
    public String getDefiningClass() {
        return FieldReference.unknown;
    }

    @Override
    public String getName() {
        return FieldReference.unknown;
    }

    @Override
    public String getType() {
        return FieldReference.unknown;
    }

    /**
     * Calculate and return the private size of a field reference.
     *
     * Calculated as: class_idx + type_idx + name_idx
     *
     * @return size in bytes
     */
    public int getSize() {
        return FieldIdItem.ITEM_SIZE;
    }
}