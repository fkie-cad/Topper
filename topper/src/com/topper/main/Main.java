package com.topper.main;

import java.io.IOException;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;

public class Main {

	public static void main(String[] args) throws IOException {

		final DexBackedDexFile file = DexFileFactory.loadDexFile("test123", null);

	}

}
