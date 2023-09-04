package com.topper.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jdt.annotation.NonNull;

public final class FileUtil {

	@NonNull
	public static final File openIfValid(@NonNull final String path) {
		
		try {
			final Path filePath = Paths.get(path).toRealPath();
			final File file = filePath.toFile();
			if (!file.canRead()) {
				throw new IllegalArgumentException(String.format("%s is not readable.", filePath));
			}
			
			if (file.isDirectory()) {
				throw new IllegalArgumentException(String.format("%s is a directory.", filePath));
			}
			
			if (!file.isFile()) {
				throw new IllegalArgumentException(String.format("%s is not a normal file.", filePath));
			}
		
			// Up to this point, the file seems fine
			return file;
			
		} catch (final IOException e) {
			throw new IllegalArgumentException("Verification of file " + path + " failed.", e);
		}
	}
	
	public static final byte @NonNull [] readContents(@NonNull final File file) {
		
		try {
			final FileInputStream in = new FileInputStream(file);
			final byte[] content = in.readAllBytes();
			if (content == null) {
				throw new IOException();
			}
			return content;
		} catch (final IOException e) {
			throw new IllegalArgumentException("Reading " + file.getPath() + " failed.", e);
		}
	}
}