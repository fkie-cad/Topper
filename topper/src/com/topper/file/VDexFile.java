package com.topper.file;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;

public class VDexFile implements AugmentedFile {
	
	private static final int VDEX_HEADER_AMOUNT_SECTIONS_OFFSET = 8; 
	
	@NonNull
	private final String filePath;
	
	private final byte @NonNull [] buffer;
	
	@NonNull
	private final ImmutableList<@NonNull DexFile> files;
	
	public VDexFile(@NonNull final String filePath, final byte @NonNull [] buffer) {
		this.filePath = filePath;
		this.buffer = buffer;
		
		final VDexFileHeader fileHeader = new VDexFileHeader(buffer, 0);
		
		if (!fileHeader.isValid()) {
			this.files = ImmutableList.of();
		} else {
			this.files = this.loadFiles(fileHeader, buffer);
		}
	}

	@Override
	public @NonNull String getFilePath() {
		return this.filePath;
	}

	@Override
	public byte @NonNull [] getBuffer() {
		return this.buffer;
	}

	@Override
	public @NonNull ImmutableList<@NonNull DexMethod> getMethods() {
		final ImmutableList.Builder<@NonNull DexMethod> methods = new ImmutableList.Builder<>();
		
		for (@NonNull final DexFile file : this.files) {
			
			methods.addAll(file.getMethods());
		}
		
		return methods.build();
	}
	
	@NonNull
	private final ImmutableList<@NonNull DexFile> loadFiles(@NonNull final VDexFileHeader fileHeader, final byte @NonNull [] buffer) {
		
		final ImmutableList.Builder<@NonNull DexFile> builder = new ImmutableList.Builder<>();
		
		final int base = VDexFileHeader.getTotalSize();
		VDexSectionHeader header;
		for (int i = 0; i < fileHeader.getNumberOfSections(); i++) {
			
			// Skip headers that do not contain dex files
			header = new VDexSectionHeader(buffer, base + i * VDexSectionHeader.getTotalSize());
			if (header.sectionKind != VDexSection.DexFileSection) {
				continue;
			}
			
			// Get dex file buffers
			
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
			this.magic = Arrays.copyOfRange(buffer, VDexFileHeaderStructure.MAGIC.getOffset() + start, VDexFileHeaderStructure.MAGIC.getEndOffset());
			this.vdexVersion = Arrays.copyOfRange(buffer, VDexFileHeaderStructure.VDEX_VERSION.getOffset() + start, VDexFileHeaderStructure.VDEX_VERSION.getEndOffset());
			this.numberOfSections = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getInt(VDexFileHeaderStructure.NUMBER_OF_SECTIONS.getOffset() + start);
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
		
		MAGIC(0, 4),
		VDEX_VERSION(MAGIC.getEndOffset(), 4),
		NUMBER_OF_SECTIONS(VDEX_VERSION.getEndOffset(), 4);
		
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
			
			this.sectionKind = VDexSection.valueOf(buf.getInt(VDexSectionHeaderStructure.SECTION_KIND.getOffset() + start));
			this.sectionOffset = buf.getInt(VDexSectionHeaderStructure.SECTION_OFFSET.getOffset() + start);
			this.sectionSize = buf.getInt(VDexSectionHeaderStructure.SECTION_SIZE.getOffset() + start);
		}
		
		@NonNull
		public final VDexSection getKind() {
			return this.sectionKind;
		}
		
		public final int getOffset() {
			return this.sectionOffset;
		}
		
		public final int getSize() {
			return this.sectionSize;
		}
		
		public static final int getTotalSize() {
			return VDexSectionHeaderStructure.values()[VDexSectionHeaderStructure.values().length - 1].getEndOffset();
		}
	}
	
	private enum VDexSectionHeaderStructure {
		SECTION_KIND(0, 4),
		SECTION_OFFSET(SECTION_KIND.getEndOffset(), 4),
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
		ChecksumSection(0),
		DexFileSection(1),
		VerifierDepsSection(2),
		TypeLookupTableSection(3),
		NumberOfSections(4);
		
		private final int value;
		private VDexSection(final int value) {
			this.value= value;
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
}