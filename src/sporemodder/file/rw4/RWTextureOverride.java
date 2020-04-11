package sporemodder.file.rw4;

import java.io.IOException;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import emord.filestructures.Stream.StringEncoding;

public class RWTextureOverride extends RWObject {
	
	// This is actually the Light type, but Spore does not use it
	public static final int TYPE_CODE = 0x20008;
	public static final int ALIGNMENT = 4;
	
	public String name;
	
	public RWTextureOverride(RenderWare renderWare) {
		super(renderWare);
	}

	@Override public void read(StreamReader stream) throws IOException {
		if (stream.readLEInt() == 0xFB724FAA) {
			name = stream.readCString(StringEncoding.ASCII);
		}
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		if (name != null) {
			stream.writeLEInt(0xFB724FAA);
			stream.writeCString(name, StringEncoding.ASCII);
		}
	}

	@Override
	public int getTypeCode() {
		return TYPE_CODE;
	}
}
