package com.topper.dex.decompiler.references;

import java.util.List;

import org.jf.dexlib2.base.reference.BaseMethodReference;
import org.jf.dexlib2.dexbacked.raw.MethodIdItem;

import com.google.common.collect.ImmutableList;

public final class MethodReference extends BaseMethodReference {
	
	private static final String unknown = "<unknown>";
	
    private final int methodIndex;

    public MethodReference(final int methodIndex) {
        this.methodIndex = methodIndex;
    }
    
    public final int getMethodIndex() {
    	return this.methodIndex;
    }
    
    @Override
    public String getDefiningClass() {
        return MethodReference.unknown;
    }

    @Override
    public String getName() {
        return MethodReference.unknown;
    }

    @Override
    public List<String> getParameterTypes() {
        return ImmutableList.of();
    }

    @Override
    public String getReturnType() {
    	return MethodReference.unknown;
    }

    /**
     * Calculate and return the private size of a method reference.
     *
     * Calculated as: class_idx + proto_idx + name_idx
     *
     * @return size in bytes
     */
    public int getSize() {
        return MethodIdItem.ITEM_SIZE; //ushort + ushort + uint for indices
    }
}
