package com.topper.configuration;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.VersionMap;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.decompiler.Decompiler;
import com.topper.dex.decompilation.decompiler.SmaliDecompiler;
import com.topper.exceptions.InvalidConfigException;

public class DecompilerConfig extends Config {

	/**
	 * Threshold for .dex file sizes in a .vdex file. If this is exceeded, then
	 * analysis will skip the respective .dex file. Negative value indicates no threshold.
	 * 0 can be used to disable analysis for all .dex files.
	 */
	private int dexSkipThreshold;
	
	/**
	 * Dex version that determines what {@link Opcodes} to use. It is used
	 * by {@link SmaliDecompiler}.
	 * */
	private int dexVersion;
	
	/**
	 * Whether or not to replace unknown opcodes with a NOP in order
	 * to continue decompilation.
	 * */
	private boolean nopUnknownInstruction;
	
	/**
	 * {@link Opcodes} chosen based on {@code dexVersion}.
	 * */
	private Opcodes opcodes;
	
	public DecompilerConfig() {
		
	}
	
	/**
	 * Gets threshold for .dex file sizes in a .vdex file. If this is exceeded, then
	 * analysis will skip the respective .dex file. Negative value indicates no threshold.
	 * 0 can be used to disable analysis for all .dex files.
	 * Defaults to 500000 bytes.
	 * */
	public final int getDexSkipThreshold() {
		this.check();
		return this.dexSkipThreshold;
	}
	
	public final void setDexSkipThreshold(final int threshold) {
		this.dexSkipThreshold = threshold;
	}

	/**
	 * Gets the dex version to use for selecting {@link Opcodes}. It is mainly
	 * used by {@link Decompiler}s.
	 * Defaults to version 29.
	 * */
	public final int getDexVersion() {
		this.check();
		return this.dexVersion;
	}
	
	public final void setDexVersion(final int version) throws InvalidConfigException {
		try {
			this.opcodes = Opcodes.forDexVersion(version);
			this.dexVersion = version;
		} catch (final RuntimeException e) {
			throw new InvalidConfigException("Dex version is invalid.", e);
		}
	}
	
	@NonNull
	public final Opcodes getOpcodes() {
		this.check();
		return this.opcodes;
	}

	/**
	 * Determine whether or not unknown opcodes should result in a NOP
	 * instead of an exception.
	 * Defaults to {@code false}.
	 * */
	public final boolean shouldNopUnknownInstruction() {
		this.check();
		return this.nopUnknownInstruction;
	}
	
	public final void setNopUnknownInstruction(final boolean nopUnknownInstruction) {
		this.nopUnknownInstruction = nopUnknownInstruction;
	}

	@Override
	@NonNull 
	public String getTag() {
		return "decompiler";
	}

	@Override
	@NonNull 
	public ImmutableList<@NonNull ConfigElement<?>> getElements() {
		return ImmutableList.of(
				new ConfigElement<Integer>("dexSkipThreshold", 500000, this::setDexSkipThreshold),
				new ConfigElement<Integer>("dexVersion", 29, this::setDexVersion),
				new ConfigElement<Boolean>("shouldNopUnknownInstruction", false, this::setNopUnknownInstruction)
		);
	}
}