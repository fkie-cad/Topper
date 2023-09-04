package com.topper.file;

import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.dexbacked.DexReader;
import org.jf.dexlib2.iface.MethodParameter;

public class DexFileHelper {

	public static final int CODE_ITEM_SIZE = 0x10;
	
	@NonNull
	private static final String FIELD_NAME_CODE_OFFSET = "codeOffset";
	
	public static final int getMethodOffset(@NonNull final DexBackedMethod method) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		final Field offsetField = method.getClass().getDeclaredField(FIELD_NAME_CODE_OFFSET);
		offsetField.setAccessible(true);
		final int offset = offsetField.getInt(method);
		offsetField.setAccessible(false);
		return offset;
	}
	
	public static final int getMethodSize(@NonNull final DexBackedMethod method, final int offset) {
		
		// Reader points at insns_size in code item
		final DexReader reader = new DexReader(method.dexFile.getBuffer(), offset + 12);
		return reader.readInt() * 2;
	}
	
	public static interface Callback<T> {
		void run(@NonNull final T value);
	}
	public static final void iterateMethods(@NonNull final DexBackedDexFile file, @NonNull final Callback<DexBackedMethod> callback) {
		
		for (final DexBackedClassDef cls : file.getClasses()) {
			if (cls == null) {
				continue;
			}
			
			for (final DexBackedMethod method : cls.getMethods()) {
				if (method == null) {
					continue;
				}
				callback.run(method);
			}
		}
	}
	
	public static final void iterateClasses(@NonNull final DexBackedDexFile file, @NonNull final Callback<DexBackedClassDef> callback) {
		
		for (final DexBackedClassDef cls : file.getClasses()) {
			if (cls == null) {
				continue;
			}
			callback.run(cls);
		}
	}
	
	/**
	 * Converts a {@link DexBackedMethod} to a human readable string. The format is
	 * ReturnType MethodName(Params)
	 * */
	@SuppressWarnings("null")	// StringBuilder::toString()
	@NonNull
	public static final String prettyMethod(@NonNull final DexBackedMethod method) {
		
		final StringBuilder b = new StringBuilder();
		
		int offset;
		try {
			offset = getMethodOffset(method);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			offset = -1;
		}
		
		b.append(String.format("[Index = %#x, Offset = %#x]: ", method.getMethodIndex(), offset));
		b.append(unknown(AccessFlags.formatAccessFlagsForMethod(method.getAccessFlags())) + " ");
		b.append(translateType(method.getReturnType()) + " ");
		b.append(unknown(translateType(method.getDefiningClass())) + "::");
		b.append(unknown(method.getName()) + "(");
		
		final List<@NonNull ? extends MethodParameter> params = method.getParameters();
		for (@NonNull final MethodParameter param : params) {
			b.append(translateType(param.getType()) + " ");
			b.append(unknown(param.getName()));
			if (!params.get(params.size() - 1).equals(param)) {
				b.append(", ");
			}
		}
		b.append(")");
		
		return b.toString();
	}
	
	@NonNull
	private static final String unknown(@Nullable final String s) {
		if (s == null) {
			return "<unknown>";
		}
		return s;
	}
	
	private static final String translateType(String returnType) {
		if (returnType == null) {
			return "<unknown>";
		}
		
		final String suffix;
		if (returnType.startsWith("[") ) {
			suffix = "[]";
			returnType = returnType.substring(1);
		} else {
			suffix = "";
		}
		
		if (returnType.endsWith(";")) {
			returnType = returnType.substring(0, returnType.length() - 1);
		}
		
		final String name;
		if (returnType.startsWith("L")) {
			// L<object>
			name = returnType.substring(1);
		} else {
			name = translatePrimitive(returnType);
		}
		return name + suffix;
	}
	
	private static final String translatePrimitive(@NonNull final String primitive) {
		if (primitive.equals("V")) {
			return "void";
		} else if (primitive.equals("Z")) {
			return "boolean";
		} else if (primitive.equals("B")) {
			return "byte";
		} else if (primitive.equals("S")) {
			return "short";
		} else if (primitive.equals("C")) {
			return "char";
		} else if (primitive.equals("I")) {
			return "int";
		} else if (primitive.equals("J")) {
			return "long";
		} else if (primitive.equals("F")) {
			return "float";
		} else if (primitive.equals("D")) {
			return "double";
		}
		return primitive;
	}
}