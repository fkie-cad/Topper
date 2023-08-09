package com.topper.file;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.decompiler.Decompiler;
import com.topper.dex.decompilation.staticanalyser.CFGAnalyser;

/**
 * CAUTION: This is an experimental feature. The .vdex file format changes a
 * lot, so newer versions may render this implementation useless. VDex file
 * representation based on {@link DexFile}. Its implementation is based on
 * <a href=
 * "https://cs.android.com/android/platform/superproject/main/+/main:art/runtime/vdex_file.h;l=45;drc=5b65e02b1cdce48da11a972ab6d75a7fb5c859bd">AOSP</a>.
 * 
 * As a .vdex file contains a list of .dex files combined with some addition
 * information, this class attempts to parse the .vdex file structure to such a
 * degree that the list of .dex files is parsable. Then {@code DexFile}s are
 * used to store the .dex files.
 * 
 * @author Pascal Kühnemann
 * @since 09.08.2023
 */
public class VDexFile implements AugmentedFile {

	/**
	 * File to augment. It may be related to {@code buffer}.
	 */
	@NonNull
	private final File file;

	/**
	 * Raw bytes that contain a valid .vdex file. It is used for parsing. Also it
	 * may be related to the contents of {@code file}.
	 */
	private final byte @NonNull [] buffer;

	/**
	 * List of {@code DexFile}s stored in the .vdex file parsed from {@code buffer}.
	 */
	@NonNull
	private final ImmutableList<@NonNull DexFile> files;

	/**
	 * Creates a new .vdex file representation using only a {@link File} and a
	 * {@code buffer}. It does not perform any static analysis on the resulting .dex
	 * files.
	 * 
	 * @param file   File to augment.
	 * @param buffer Raw bytes that represent a valid .vdex file.
	 * @throws IllegalArgumentException If the buffer is empty or does not contain a
	 *                                  valid .vdex file. Also if the .vdex file
	 *                                  contains a corrupted .dex file.
	 */
	public VDexFile(@NonNull final File file, final byte @NonNull [] buffer) {
		this(file, buffer, null, null, -1);
	}

	/**
	 * Creates a new .vdex file respresentation using a {@link File} and a
	 * {@buffer}. Optionally, it uses a {@link Decompiler} and a {@link CFGAnalyser}
	 * to extract Control Flow Graphs(CFG) for all methods of all .dex files.
	 * 
	 * If CFG extraction for a given method fails due to an internal error, or the
	 * method is abstract or native, or either one of {@code decompiler} or
	 * {@code analyser} is {@code null}, then the method will not be analysed.
	 * Otherwise all methods are analysed, which may be time consuming.
	 * 
	 * Some coarse checks are performed to ensure {@code buffer} contains a .vdex
	 * file. However, as versions of .vdex files change, mainly the magic bytes
	 * are verified.
	 * 
	 * @param file          File to augment.
	 * @param buffer        Raw bytes that represent a valid .vdex file.
	 * @param decompiler    A {@code Decompiler} used in conjunction with
	 *                      {@code analyser} to decompile all methods.
	 * @param analyser      A {@code CFGAnalyser} used in conjunction with
	 *                      {@code decompiler} to extract Control Flow Graphs for
	 *                      all methods.
	 * @param vdexThreshold A threshold for file sizes of .dex files in this .vdex
	 *                      file. If a .dex exceeds this threshold, then its methods
	 *                      will not be analysed using {@code decompiler} and
	 *                      {@code analyser}. This prevents being stuck on large
	 *                      library .dex files that may be irrelevant. 0 indicates
	 *                      to perform no analysis. A negative value indicates to
	 *                      analyse all .dex files regardless of their size.
	 * @throws IllegalArgumentException If the buffer is empty or does not contain a
	 *                                  valid .vdex file. Also if the .vdex file
	 *                                  contains a corrupted .dex file.
	 */
	public VDexFile(@NonNull final File file, final byte @NonNull [] buffer, @Nullable final Decompiler decompiler,
			@Nullable final CFGAnalyser analyser, final int vdexThreshold) {
		if (buffer.length == 0) {
			throw new IllegalArgumentException("buffer must not be empty.");
		}

		this.file = file;
		this.buffer = buffer;

		final VDexFileHeader fileHeader = new VDexFileHeader(buffer, 0);

		if (!fileHeader.isValid()) {
			throw new IllegalArgumentException("buffer must contain a valid .vdex file.");
		} else {
			// Propagate IllegalArgumentExceptions to caller
			this.files = this.loadFiles(fileHeader, file, buffer, decompiler, analyser, vdexThreshold);
		}
	}

	@Override
	public @NonNull File getFile() {
		return this.file;
	}

	@Override
	public byte @NonNull [] getBuffer() {
		return this.buffer;
	}

	@Override
	public @NonNull ImmutableList<@NonNull DexMethod> getMethods() {
		final ImmutableList.Builder<@NonNull DexMethod> methods = new ImmutableList.Builder<>();

		for (@NonNull
		final DexFile file : this.files) {
			methods.addAll(file.getMethods());
		}

		return methods.build();
	}

	/**
	 * Gets a list of all identified .dex files in this .vdex file.
	 * */
	@NonNull
	public final ImmutableList<@NonNull DexFile> getDexFiles() {
		return this.files;
	}

	/**
	 * Loads all .dex files from this .vdex file. An underlying assumption
	 * is that .dex files are adjacent to each other, i.e. offset_1 + size_1 = offset_2.
	 * 
	 * Internally, all .dex files are wrapped in a {@link DexFile}, which may
	 * imply CFG extraction depending on {@code decompiler} and {@code analyser}.
	 * 
	 * @param fileHeader Representation of a .vdex file header.
	 * @param file File to augment. It is forwarded to all {@code DexFile}s.
	 * @param buffer Raw bytes that contain the valid (and already verified) .vdex file. 
	 * 	It is sliced to extract .dex files and forwarded to its respective {@code DexFile}.
	 * @param decompiler {@code Decompiler} to use for decompiling bytes into Smali.
	 * @param analyser   {@code CFGAnalyser} to use for constructing CFGs for
	 *                   decompiled methods.
	 * @param vdexThreshold A threshold for file sizes of .dex files in this .vdex
	 *                      file. If a .dex exceeds this threshold, then its methods
	 *                      will not be analysed using {@code decompiler} and
	 *                      {@code analyser}. This prevents being stuck on large
	 *                      library .dex files that may be irrelevant.
	 * @return List of .dex file representations.
	 * @throws IllegalArgumentException If {@code DexFile} construction fails.
	 * */
	@NonNull
	private final ImmutableList<@NonNull DexFile> loadFiles(@NonNull final VDexFileHeader fileHeader,
			@NonNull final File file, final byte @NonNull [] buffer, @Nullable final Decompiler decompiler,
			@Nullable final CFGAnalyser analyser, final int vdexThreshold) {

		final ImmutableList.Builder<@NonNull DexFile> builder = new ImmutableList.Builder<>();

		final int base = VDexFileHeader.getTotalSize();
		VDexSectionHeader header;
		for (int i = 0; i < fileHeader.getNumberOfSections(); i++) {

			// Skip headers that do not contain dex files
			header = new VDexSectionHeader(buffer, base + i * VDexSectionHeader.getTotalSize());
			if (header.getSectionKind() != VDexSection.DexFileSection) {
				continue;
			}

			// Get dex file buffers
			int dexStart = header.getSectionOffset();
			PartialDexHeader dexHeader;

			while (dexStart < header.getSectionOffset() + header.getSectionSize()) {

				// Parse partial dex header
				dexHeader = new PartialDexHeader(buffer, dexStart);

				// Check if .dex file exceeds configured threshold
				if (dexHeader.getFileSize() <= vdexThreshold || vdexThreshold < 0) {

					// Parse dex file and store it into list
					builder.add(
							new DexFile(file, Arrays.copyOfRange(buffer, dexStart, dexStart + dexHeader.getFileSize()),
									decompiler, analyser));
				} else {
					// Parse dex file and store it into list, but do not extract CFGs.
					builder.add(new DexFile(file,
							Arrays.copyOfRange(buffer, dexStart, dexStart + dexHeader.getFileSize()), null, null));
				}

				// Move dexStart by size of current dex file
				dexStart += dexHeader.getFileSize();
			}

		}

		return builder.build();
	}

	private class VDexFileHeader {

		private static final byte @NonNull [] VDEX_MAGIC = "vdex".getBytes();
		private final byte @NonNull [] magic;
		private final byte @NonNull [] vdexVersion;
		private final int numberOfSections;

		public VDexFileHeader(final byte @NonNull [] buffer, final int start) {
			if (start + VDexFileHeader.getTotalSize() >= buffer.length) {
				throw new IllegalArgumentException("buffer is too small");
			}
			this.magic = Arrays.copyOfRange(buffer, VDexFileHeaderStructure.MAGIC.getOffset() + start,
					VDexFileHeaderStructure.MAGIC.getEndOffset());
			this.vdexVersion = Arrays.copyOfRange(buffer, VDexFileHeaderStructure.VDEX_VERSION.getOffset() + start,
					VDexFileHeaderStructure.VDEX_VERSION.getEndOffset());
			this.numberOfSections = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
					.getInt(VDexFileHeaderStructure.NUMBER_OF_SECTIONS.getOffset() + start);
		}

		public final byte @NonNull [] getMagic() {
			return this.magic;
		}

		public final byte @NonNull [] getVDexVersion() {
			return this.vdexVersion;
		}

		public final int getNumberOfSections() {
			return this.numberOfSections;
		}

		public final boolean isValid() {
			return Arrays.equals(this.magic, VDEX_MAGIC);
		}

		public static final int getTotalSize() {
			return VDexFileHeaderStructure.values()[VDexFileHeaderStructure.values().length - 1].getEndOffset();
		}
	}

	private enum VDexFileHeaderStructure {

		MAGIC(0, 4), VDEX_VERSION(MAGIC.getEndOffset(), 4), NUMBER_OF_SECTIONS(VDEX_VERSION.getEndOffset(), 4);

		private final int offset;
		private final int size;

		private VDexFileHeaderStructure(final int offset, final int size) {
			this.offset = offset;
			this.size = size;
		}

		public final int getOffset() {
			return this.offset;
		}

		public final int getSize() {
			return this.size;
		}

		public final int getEndOffset() {
			return this.offset + this.size;
		}
	}

	private class VDexSectionHeader {

		private final VDexSection sectionKind;
		private final int sectionOffset;
		private final int sectionSize;

		public VDexSectionHeader(final byte @NonNull [] buffer, final int start) {
			if (start + VDexSectionHeader.getTotalSize() >= buffer.length) {
				throw new IllegalArgumentException("buffer is too small.");
			}
			final ByteBuffer buf = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);

			this.sectionKind = VDexSection
					.valueOf(buf.getInt(VDexSectionHeaderStructure.SECTION_KIND.getOffset() + start));
			this.sectionOffset = buf.getInt(VDexSectionHeaderStructure.SECTION_OFFSET.getOffset() + start);
			this.sectionSize = buf.getInt(VDexSectionHeaderStructure.SECTION_SIZE.getOffset() + start);
		}

		@NonNull
		public final VDexSection getSectionKind() {
			return this.sectionKind;
		}

		public final int getSectionOffset() {
			return this.sectionOffset;
		}

		public final int getSectionSize() {
			return this.sectionSize;
		}

		public static final int getTotalSize() {
			return VDexSectionHeaderStructure.values()[VDexSectionHeaderStructure.values().length - 1].getEndOffset();
		}
	}

	private enum VDexSectionHeaderStructure {
		SECTION_KIND(0, 4), SECTION_OFFSET(SECTION_KIND.getEndOffset(), 4),
		SECTION_SIZE(SECTION_OFFSET.getEndOffset(), 4);

		private final int offset;
		private final int size;

		private VDexSectionHeaderStructure(final int offset, final int size) {
			this.offset = offset;
			this.size = size;
		}

		public final int getOffset() {
			return this.offset;
		}

		public final int getSize() {
			return this.size;
		}

		public final int getEndOffset() {
			return this.offset + this.size;
		}
	}

	private enum VDexSection {
		ChecksumSection(0), DexFileSection(1), VerifierDepsSection(2), TypeLookupTableSection(3), NumberOfSections(4);

		private final int value;

		private VDexSection(final int value) {
			this.value = value;
		}

		public final int getValue() {
			return this.value;
		}

		public static final VDexSection valueOf(final int value) {
			for (final VDexSection sec : VDexSection.values()) {
				if (sec.getValue() == value) {
					return sec;
				}
			}
			throw new IllegalArgumentException("Invalid section value");
		}
	}

	private class PartialDexHeader {

		private final byte @NonNull [] magic;
		private final int checksum;
		private final byte @NonNull [] signature;
		private final int fileSize;

		public PartialDexHeader(final byte @NonNull [] buffer, final int start) {
			final ByteBuffer buf = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
			this.magic = buf.slice(PartialDexHeaderStructure.MAGIC.getOffset() + start,
					PartialDexHeaderStructure.MAGIC.getSize()).array();
			this.checksum = buf.getInt(PartialDexHeaderStructure.CHECKSUM.getOffset() + start);
			this.signature = buf.slice(PartialDexHeaderStructure.SIGNATURE.getOffset() + start,
					PartialDexHeaderStructure.SIGNATURE.getSize()).array();
			this.fileSize = buf.getInt(PartialDexHeaderStructure.FILE_SIZE.getOffset() + start);
		}

		public final byte @NonNull [] getMagic() {
			return this.magic;
		}

		public final int getChecksum() {
			return this.checksum;
		}

		public final byte @NonNull [] getSignature() {
			return this.signature;
		}

		public final int getFileSize() {
			return this.fileSize;
		}

		public static final int getTotalSize() {
			return PartialDexHeaderStructure.values()[PartialDexHeaderStructure.values().length - 1].getEndOffset();
		}
	}

	private enum PartialDexHeaderStructure {
		MAGIC(0, 8), CHECKSUM(MAGIC.getEndOffset(), 4), SIGNATURE(CHECKSUM.getEndOffset(), 20),
		FILE_SIZE(SIGNATURE.getEndOffset(), 4);

		private final int offset;
		private final int size;

		private PartialDexHeaderStructure(final int offset, final int size) {
			this.offset = offset;
			this.size = size;
		}

		public final int getOffset() {
			return this.offset;
		}

		public final int getSize() {
			return this.size;
		}

		public final int getEndOffset() {
			return this.offset + this.size;
		}
	}
}