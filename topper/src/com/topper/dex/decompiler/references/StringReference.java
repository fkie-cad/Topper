package com.topper.dex.decompiler.references;

import org.jf.dexlib2.base.reference.BaseStringReference;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexReader;
import org.jf.dexlib2.dexbacked.raw.StringIdItem;

public final class StringReference extends BaseStringReference {

	private static final String unknown = "<unknown>";
	
	private final int stringIndex;
	
	private final String string;
	private final int size;
	
	public StringReference(final DexBackedDexFile file, final int stringIndex) {
		this.stringIndex = stringIndex;
		
		if (file != null) {
			
			// Got a valid file, so extract reference
			// https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/reference/DexBackedStringReference.java;l=51;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4
			this.string = new String(file.getStringSection().get(this.stringIndex));
			
			// https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/reference/DexBackedStringReference.java;l=62;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4
			int tmpSize = StringIdItem.ITEM_SIZE;
			int stringOffset = file.getStringSection().getOffset(this.stringIndex);
			int stringDataOffset = file.getBuffer().readSmallUint(stringOffset);
			final DexReader<?> reader = file.getDataBuffer().readerAt(stringDataOffset);
			tmpSize += reader.peekSmallUleb128Size();
			int utf16Length = reader.readSmallUleb128();
			tmpSize += reader.peekStringLength(utf16Length);
			this.size = tmpSize;
		} else {
			
			this.string = StringReference.unknown;
			this.size = this.string.length(); 
		}
	}
	
	@Override
	public final String getString() {
		return this.string;
	}
	
	public final int getSize() {
		return this.size;
	}
	
	public final int getStringIndex() {
		return this.stringIndex;
	}
}
