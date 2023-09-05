package com.topper.configuration;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Opcodes;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompiler.Decompiler;
import com.topper.dex.decompiler.SmaliDecompiler;
import com.topper.exceptions.InvalidConfigException;

/**
 * Configuration used by {@link Decompiler}.
 * 
 * @author Pascal Kühnemann
 * @since 14.08.2023
 */
public class DecompilerConfig extends Config {

	/**
	 * Threshold for .dex file sizes in a .vdex file. If this is exceeded, then
	 * analysis will skip the respective .dex file. Negative value indicates no
	 * threshold. 0 can be used to disable analysis for all .dex files.
	 */
	private int dexSkipThreshold;

	/**
	 * Dex version that determines what {@link Opcodes} to use. It is used by
	 * {@link SmaliDecompiler}.
	 */
	private int dexVersion;

	/**
	 * Whether or not to replace unknown opcodes with a NOP in order to continue
	 * decompilation.
	 */
	private boolean nopUnknownInstruction;

	/**
	 * {@link Opcodes} chosen based on {@code dexVersion}.
	 */
	private Opcodes opcodes;

	/**
	 * Gets threshold for .dex file sizes in a .vdex file. If this is exceeded, then
	 * analysis will skip the respective .dex file. Negative value indicates no
	 * threshold. 0 can be used to disable analysis for all .dex files.
	 * 
	 * Defaults to <code>500000</code>.
	 * 
	 * @throws UnsupportedOperationException If {@link Config#load} has not been
	 *                                       executed yet or execution has not been
	 *                                       successful.
	 */
	public final int getDexSkipThreshold() {
		this.check();
		return this.dexSkipThreshold;
	}

	/**
	 * Sets threshold for .dex file sizes in a .vdex file. If this is exceeded, then
	 * analysis will skip the respective .dex file. Negative value indicates no
	 * threshold. 0 can be used to disable analysis for all .dex files.
	 */
	public final void setDexSkipThreshold(final int threshold) {
		this.dexSkipThreshold = threshold;
	}

	/**
	 * Gets the dex version to use for selecting {@link Opcodes}. It is mainly used
	 * by {@link Decompiler}s.
	 * 
	 * Defaults to <code>39</code>.
	 * 
	 * @throws UnsupportedOperationException If {@link Config#load} has not been
	 *                                       executed yet or execution has not been
	 *                                       successful.
	 */
	public final int getDexVersion() {
		this.check();
		return this.dexVersion;
	}

	/**
	 * Sets the dex version to use for selecting {@link Opcodes}.
	 * 
	 * @throws InvalidConfigException If dex version is invalid.
	 */
	public final void setDexVersion(final int version) throws InvalidConfigException {
		try {
			this.opcodes = Opcodes.forDexVersion(version);
			this.dexVersion = version;
		} catch (final RuntimeException e) {
			throw new InvalidConfigException("Dex version is invalid.", e);
		}
	}

	/**
	 * Gets {@link Opcodes} based on provided dex version.
	 * 
	 * Defaults to <code>Opcodes</code> linked to dex version <code>39</code>.
	 * 
	 * @throws UnsupportedOperationException If {@link Config#load} has not been
	 *                                       executed yet or execution has not been
	 *                                       successful.
	 */
	@NonNull
	public final Opcodes getOpcodes() {
		this.check();
		return this.opcodes;
	}

	/**
	 * Determine whether or not unknown opcodes should result in a NOP instead of an
	 * exception. Defaults to {@code false}.
	 * 
	 * Defaults to <code>false</code>.
	 * 
	 * @throws UnsupportedOperationException If {@link Config#load} has not been
	 *                                       executed yet or execution has not been
	 *                                       successful.
	 */
	public final boolean shouldNopUnknownInstruction() {
		this.check();
		return this.nopUnknownInstruction;
	}

	/**
	 * Determines whether or not unknown opcodes should be nopped.
	 */
	public final void setNopUnknownInstruction(final boolean nopUnknownInstruction) {
		this.nopUnknownInstruction = nopUnknownInstruction;
	}

	/**
	 * Gets the <code>"decompiler"</code> tag.
	 */
	@Override
	@NonNull
	public String getTag() {
		return "decompiler";
	}

	/**
	 * Gets a list of valid {@link Decompiler} configurations. E.g.
	 * <ul>
	 * <li>dexSkipThreshold(int)</li>
	 * <li>dexVersion(int)</li>
	 * <li>shouldNopUnknownInstruction(boolean)</li>
	 * </ul>
	 */
	@SuppressWarnings("null")
	@Override
	@NonNull
	public ImmutableList<@NonNull ConfigElement<?>> getElements() {
		return ImmutableList.of(new ConfigElement<Integer>("dexSkipThreshold", 500000, this::setDexSkipThreshold),
				new ConfigElement<Integer>("dexVersion", 39, this::setDexVersion),
				new ConfigElement<Boolean>("shouldNopUnknownInstruction", false, this::setNopUnknownInstruction));
	}
	
	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append("Decompiler Config:" + System.lineSeparator());
		b.append("- dexSkipThreshold: " + this.getDexSkipThreshold() + System.lineSeparator());
		b.append("- dexVersion: " + this.getDexVersion() + System.lineSeparator());
		b.append("- shouldNopUnknownInstruction: " + this.shouldNopUnknownInstruction() + System.lineSeparator());
		return b.toString();
	}
}