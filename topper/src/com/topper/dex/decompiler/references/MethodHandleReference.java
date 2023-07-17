package com.topper.dex.decompiler.references;

import org.jf.dexlib2.base.reference.BaseMethodHandleReference;
import org.jf.dexlib2.iface.reference.Reference;

public final class MethodHandleReference extends BaseMethodHandleReference {

    public final int methodHandleIndex;

    public MethodHandleReference(int methodHandleIndex) {
        this.methodHandleIndex = methodHandleIndex;
    }
    
    public final int getMethodHandleIndex() {
    	return this.methodHandleIndex;
    }

    @Override
    public int getMethodHandleType() {
        return -1;
    }

    @Override
    public Reference getMemberReference() {
        return null;
    }
}