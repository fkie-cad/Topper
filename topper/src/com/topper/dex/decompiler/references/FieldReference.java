package com.topper.dex.decompiler.references;

import org.jf.dexlib2.base.reference.BaseFieldReference;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.raw.FieldIdItem;

public final class FieldReference extends BaseFieldReference {

	private static final String unknown = "<unknown>";
	
    private final int fieldIndex;
    
    private final String definingClass;
    private final String name;
    private final String type;

    public FieldReference(final DexBackedDexFile file, int fieldIndex) {
        this.fieldIndex = fieldIndex;
        
        if (file != null) {
        	
        	// https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/reference/DexBackedFieldReference.java;l=50;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4
        	this.definingClass = new String(file.getTypeSection().get(
                    file.getBuffer().readUshort(
                            file.getFieldSection().getOffset(fieldIndex) + FieldIdItem.CLASS_OFFSET)));
        	
        	// https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/reference/DexBackedFieldReference.java;l=58;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4
        	this.name = new String(file.getStringSection().get(file.getBuffer().readSmallUint(
                    file.getFieldSection().getOffset(fieldIndex) + FieldIdItem.NAME_OFFSET)));
        	
        	// https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/reference/DexBackedFieldReference.java;l=65;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4
        	this.type = new String(file.getTypeSection().get(file.getBuffer().readUshort(
                    file.getFieldSection().getOffset(fieldIndex) + FieldIdItem.TYPE_OFFSET)));
        } else {
        	this.definingClass = FieldReference.unknown;
        	this.name = FieldReference.unknown;
        	this.type = FieldReference.unknown;
        }
    }
    
    public final int getFieldIndex() {
    	return this.fieldIndex;
    }

    @Override
    public String getDefiningClass() {
        return this.definingClass; 
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getType() {
        return this.type;
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