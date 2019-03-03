package sporemodder.file.anim;

import java.io.IOException;

public class ChannelComponent {
	
	public static final int TYPE_INFO = 0x4F464E49;
	public static final int TYPE_POS = 0x534F50;
	public static final int TYPE_ROT = 0x544F52;

	public int id;
	public int keyframeOffset;
	public int keyframeStride;
	
	public void read(DataStructure stream, long dataPtr) throws IOException {
		id = stream.getInt(4);
		keyframeOffset = stream.getInt(8);
		keyframeStride = stream.getInt(12);
		
		System.out.println("\t0x" + Long.toHexString(dataPtr + keyframeOffset) + "\tid: 0x" + Integer.toHexString(id));
	}
}
