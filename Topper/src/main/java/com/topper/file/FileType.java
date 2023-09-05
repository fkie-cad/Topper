package com.topper.file;

/**
 * Description of a file type supported by Topper.
 * 
 * @author Pascal Kühnemann
 * @since 05.09.2023
 */
public enum FileType {
	/**
	 * Represents a {@link RawFile}.
	 */
	RAW,
	/**
	 * Represents a {@link DexFile}.
	 */
	DEX,
	/**
	 * Represents a {@link VDexFile}.
	 */
	VDEX
}