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

public class VDexFile implements AugmentedFile {

	@NonNull
	private final File file;

	private final byte @NonNull [] buffer;

	@NonNull
	private final ImmutableList<@NonNull DexFile> files;
	
	public VDexFile(@NonNull final File file, final byte @NonNull [] buffer) {
		this(file, buffer, null, null);
	}

	public VDexFile(@NonNull final File file, final byte @NonNull [] buffer,
			@Nullable final Decompiler decompiler, @Nullable final CFGAnalyser analyser) {
		if (buffer.length == 0) {
			throw new IllegalArgumentException("buffer must not be empty.");
		}
		
		this.file = file;
		this.buffer = buffer;

		final VDexFileHeader fileHeader = new VDexFileHeader(buffer, 0);

		if (!fileHeader.isValid()) {
			this.files = ImmutableList.of();
		} else {
			// Propagate IllegalArgumentExceptions to caller
			this.files = this.loadFiles(fileHeader, file, buffer, decompiler, analyser);
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
	
	@NonNull
	public final ImmutableList<@NonNull DexFile> getFiles() {
		return this.files;
	}
	
	@NonNull
	private final ImmutableList<@NonNull DexFile> loadFiles(@NonNull final VDexFileHeader fileHeader,
			@NonNull final File file, final byte @NonNull [] buffer, @Nullable final Decompiler decompiler,
			@Nullable final CFGAnalyser analyser) {

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

			while (dexStart >= header.getSectionOffset() + header.getSectionSize()) {

				// Parse partial dex header
				dexHeader = new PartialDexHeader(buffer, dexStart);

				// Parse dex file and store it into list
				builder.add(
						new DexFile(file, Arrays.copyOfRange(buffer, dexStart, dexStart + dexHeader.getFileSize()),
								decompiler, analyser));

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