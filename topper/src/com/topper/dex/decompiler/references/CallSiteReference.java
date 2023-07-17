package com.topper.dex.decompiler.references;

import java.util.List;

import org.jf.dexlib2.base.reference.BaseCallSiteReference;
import org.jf.dexlib2.iface.reference.MethodHandleReference;
import org.jf.dexlib2.iface.reference.MethodProtoReference;
import org.jf.dexlib2.iface.value.EncodedValue;

public final class CallSiteReference extends BaseCallSiteReference {

	private static final String unknown = "<unknown>";

    private final int callSiteIndex;

    public CallSiteReference(final int callSiteIndex) {
        this.callSiteIndex = callSiteIndex;
    }
    
    public final int getCallSiteIndex() {
    	return this.callSiteIndex;
    }

    @Override
    public String getName() {
        return String.format("call_site_%d", this.getCallSiteIndex());
    }

    @Override
    public MethodHandleReference getMethodHandle() {
    	return null;
    }

    @Override
    public String getMethodName() {
        return CallSiteReference.unknown;
    }

    @Override
    public MethodProtoReference getMethodProto() {
        return null;
    }

    @Override
    public List<? extends EncodedValue> getExtraArguments() {
        return null;
    }
}
