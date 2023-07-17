package com.topper.dex.decompiler.references;

import java.util.List;

import org.jf.dexlib2.ValueType;
import org.jf.dexlib2.base.reference.BaseCallSiteReference;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.util.EncodedArrayItemIterator;
import org.jf.dexlib2.iface.reference.MethodHandleReference;
import org.jf.dexlib2.iface.reference.MethodProtoReference;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.iface.value.MethodHandleEncodedValue;
import org.jf.dexlib2.iface.value.MethodTypeEncodedValue;
import org.jf.dexlib2.iface.value.StringEncodedValue;

import com.google.common.collect.Lists;

public final class CallSiteReference extends BaseCallSiteReference {

	private static final String unknown = "<unknown>";

    private final int callSiteIndex;
    
    private final String name;
    private final MethodHandleReference methodHandle;
    private final String methodName;
    private final MethodProtoReference methodProto;
    private final List<? extends EncodedValue> extraArguments;

    public CallSiteReference(final DexBackedDexFile file, final int callSiteIndex) {
        this.callSiteIndex = callSiteIndex;
        
        if (file != null) {
        	
        	this.name = String.format("call_site_%d", callSiteIndex);
        	this.methodHandle = this.getMethodHandleImpl(file);
        	this.methodName = this.getMethodNameImpl(file);
        	this.methodProto = this.getMethodProtoImpl(file);
        	this.extraArguments = this.getExtraArgumentsImpl(file);
        } else {
        	
        	this.name = CallSiteReference.unknown;
        	this.methodHandle = null;
        	this.methodName = CallSiteReference.unknown;
        	this.methodProto = null;
        	this.extraArguments = null;
        }
    }
    
    public final int getCallSiteIndex() {
    	return this.callSiteIndex;
    }

    @Override
    public String getName() {
    	return this.name;
    }

    @Override
    public MethodHandleReference getMethodHandle() {
    	return this.methodHandle;
    }

    @Override
    public String getMethodName() {
        return this.methodName;
    }

    @Override
    public MethodProtoReference getMethodProto() {
        return this.methodProto;
    }

    @Override
    public List<? extends EncodedValue> getExtraArguments() {
        return this.extraArguments;
    }
    
    private final EncodedArrayItemIterator getCallSiteIterator(final DexBackedDexFile dexFile) {
    	// https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/reference/DexBackedCallSiteReference.java;l=149;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4
    	return EncodedArrayItemIterator.newOrEmpty(dexFile, getCallSiteOffset(dexFile));
    }
    
    private int getCallSiteOffset(final DexBackedDexFile dexFile) {
    	// https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/reference/DexBackedCallSiteReference.java;l=153;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4
        return dexFile.getBuffer().readSmallUint(dexFile.getCallSiteSection().getOffset(this.callSiteIndex));
    }
    
    private MethodHandleReference getMethodHandleImpl(final DexBackedDexFile file) {
    	// https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/reference/DexBackedCallSiteReference.java;l=69;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4
        EncodedArrayItemIterator iter = getCallSiteIterator(file);
        if (iter.getItemCount() < 3) {
        	return null;
        }

        EncodedValue encodedValue = getCallSiteIterator(file).getNextOrNull();
        assert encodedValue != null;
        if (encodedValue.getValueType() != ValueType.METHOD_HANDLE) {
        	return null;
        }
        return ((MethodHandleEncodedValue) encodedValue).getValue();
    }
    
    private String getMethodNameImpl(final DexBackedDexFile file) {
    	// https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/reference/DexBackedCallSiteReference.java;l=87;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4
        EncodedArrayItemIterator iter = getCallSiteIterator(file);
        if (iter.getItemCount() < 3) {
        	return CallSiteReference.unknown;
        }

        iter.skipNext();
        EncodedValue encodedValue = iter.getNextOrNull();
        assert encodedValue != null;
        if (encodedValue.getValueType() != ValueType.STRING) {
        	return CallSiteReference.unknown;
        }
        return new String(((StringEncodedValue) encodedValue).getValue());
    }

    private MethodProtoReference getMethodProtoImpl(final DexBackedDexFile file) {
    	// https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/reference/DexBackedCallSiteReference.java;l=106;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4
        EncodedArrayItemIterator iter = getCallSiteIterator(file);
        if (iter.getItemCount() < 3) {
        	return null;
        }

        iter.skipNext();
        iter.skipNext();
        EncodedValue encodedValue = iter.getNextOrNull();
        assert encodedValue != null;
        if (encodedValue.getValueType() != ValueType.METHOD_TYPE) {
        	return null;
        }
        return ((MethodTypeEncodedValue) encodedValue).getValue();
    }
    
    private List<? extends EncodedValue> getExtraArgumentsImpl(final DexBackedDexFile file) {
    	// https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/reference/DexBackedCallSiteReference.java;l=126;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4
        List<EncodedValue> values = Lists.newArrayList();

        EncodedArrayItemIterator iter = getCallSiteIterator(file);
        if (iter.getItemCount() < 3) {
        	return null;
        }
        if (iter.getItemCount() == 3) {
            return values;
        }

        iter.skipNext();
        iter.skipNext();
        iter.skipNext();

        EncodedValue item = iter.getNextOrNull();
        while (item != null) {
            values.add(item);
            item = iter.getNextOrNull();
        }
        return values;
    }
}
