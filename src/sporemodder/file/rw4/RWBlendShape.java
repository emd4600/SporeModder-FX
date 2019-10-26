package sporemodder.file.rw4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;

public class RWBlendShape extends RWObject {

	public static final int TYPE_CODE = 0xff0002;
	public static final int ALIGNMENT = 4;
	
	public int id;
	public final List<Integer> shapeIDs = new ArrayList<>();
	
	public RWBlendShape(RenderWare renderWare) {
		super(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		stream.readLEInt();  // gets replaced by code
		stream.readLEInt();  // Pointer to a function, gets replaced
		
		stream.readLEInt();  // Index to subreference in this same object
		int shapeCount = stream.readLEInt();
		stream.readLEInt();  // Index to subreference in this same object
		if (shapeCount != stream.readLEInt()) {
			throw new IOException("Malformed BlendShape.");
		}
		id = stream.readLEInt();
		
		// This is a buffer for the shape times, gets replaced by code
		stream.skip(4 * shapeCount);
		
		for (int i = 0; i < shapeCount; ++i) {
			shapeIDs.add(stream.readLEInt());
		}
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(0);
		stream.writeLEInt(0);
		stream.writeLEInt(renderWare.addReference(this, 0x1C));
		stream.writeLEInt(shapeIDs.size());
		stream.writeLEInt(renderWare.addReference(this, 0x1C + shapeIDs.size() * 4));
		stream.writeLEInt(shapeIDs.size());
		stream.writeLEInt(id);
		stream.writePadding(shapeIDs.size() * 4);
		for (int i : shapeIDs) stream.writeLEInt(i);
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
