package sporemodder.file.rw4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;

public class RWBlendShapeBuffer extends RWObject {

	public static final int TYPE_CODE = 0x200af;
	public static final int ALIGNMENT = 16;
	
	public static final int INDEX_POSITION = 0;
	public static final int INDEX_NORMAL = 1;
	public static final int INDEX_TANGENT = 2;
	public static final int INDEX_TEXCOORD = 3;
	public static final int INDEX_BLENDINDICES = 9;
	public static final int INDEX_BLENDWEIGHTS = 10;
	
	// position, normal, tangent, texcoord, ?, ?, ?, ?, ?, blendIndices (as shorts), blendWeights (as floats)
	public final byte[][] data = new byte[11][];
	public int shapeCount;  //TODO not really! shapeCount - 1?
	public int vertexCount;
	public int unk;
	public int boneIndicesCount;
	
	public RWBlendShapeBuffer(RenderWare renderWare) {
		super(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		long baseOffset = stream.getFilePointer();
		
		int type = stream.readLEInt();
		if (type != 1) {
			// Other type is 0x40
			throw new IOException("Malfromed RWBlendShapeBuffer");
		}
		
		List<Long> endOffsets = new ArrayList<>();
		long[] offsets = new long[data.length];
		for (int i = 0; i < data.length; ++i) {
			offsets[i] = stream.readLEUInt();
			if (offsets[i] != 0) endOffsets.add(offsets[i]);
		}
		endOffsets.add((long) sectionInfo.size);
		shapeCount = stream.readLEInt();
		vertexCount = stream.readLEInt();
		unk = stream.readLEInt();
		boneIndicesCount = stream.readLEInt();
		
		Iterator<Long> it = endOffsets.iterator();
		it.next();
		
		for (int i = 0; i < data.length; ++i) {
			if (offsets[i] != 0) {
				int size = (int)(it.next() - offsets[i]);
				stream.seek(baseOffset + offsets[i]);
				
				data[i] = new byte[size];
				stream.read(data[i]);
			}
		}
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		long baseOffset = stream.getFilePointer();
		stream.writeLEInt(1);
		stream.writePadding(4 * data.length);
		stream.writeLEInt(shapeCount);
		stream.writeLEInt(vertexCount);
		stream.writeLEInt(unk);
		stream.writeLEInt(boneIndicesCount);
		
		long[] offsets = new long[data.length];
		
		for (int i = 0; i < data.length; ++i) {
			if (data[i] != null) {
				long offset = stream.getFilePointer();
				long alignedOffset = (offset + 16-1) & ~(16-1);
				offsets[i] = alignedOffset - baseOffset;
				stream.writePadding((int) (alignedOffset - offset));
				stream.write(data[i]);
			}
		}
		
		long endOffset = stream.getFilePointer();
		stream.seek(baseOffset + 4);
		stream.writeLEUInts(offsets);
		stream.seek(endOffset);
	}

	@Override
	public int getTypeCode() {
		return TYPE_CODE;
	}

	@Override
	public int getAlignment() {
		return ALIGNMENT;
	}
}
