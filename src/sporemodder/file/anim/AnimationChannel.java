package sporemodder.file.anim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import emord.filestructures.Stream.StringEncoding;
import sporemodder.file.argscript.ArgScriptWriter;

public class AnimationChannel {
	
	private final static int MAGIC = 0x4E414843;
	
	public String name;
	public int field_8C;
	public int keyframeCount;
	public long keyframePtr;
	public final List<ChannelComponent> components = new ArrayList<>();

	public void read(DataStructure stream) throws IOException {
		stream.getStream().seek(stream.getPointer());
		
		int magic = stream.getStream().readLEInt();
		if (magic != MAGIC) {
			throw new IOException("Unsupported channel magic: 0x" + Integer.toHexString(magic));
		}
		
		stream.getStream().skip(4);
		name = stream.getStream().readCString(StringEncoding.ASCII);
		
		field_8C = stream.getInt(0x8C);
		
		keyframeCount = stream.getInt(0xD4);  // ?
		keyframePtr = stream.getUInt(0xD8);
		int count = stream.getInt(0xDC);
		long ptr = stream.getUInt(0xE0);
		System.out.println(name + "  " + field_8C + "\tkeyframes[" + keyframeCount + "] 0x" + Integer.toHexString(stream.getInt(0xD8)));
		
		// each item of size 32
		
		for (int i = 0; i < count; ++i) {
			stream.setPointer(ptr + 32*i);
			
			ChannelComponent comp = new ChannelComponent();
			comp.read(stream, keyframePtr);
			components.add(comp);
		}
		
		System.out.println();
	}
	
	public void toArgScript(ArgScriptWriter writer, DataStructure stream) throws IOException {
		writer.command("channel").arguments(name).startBlock();
		
		for (int i = 0; i < components.size(); ++i) {
			ChannelComponent comp = components.get(i);

			writer.command("component");
			if (comp.id == ChannelComponent.TYPE_POS) {
				writer.arguments("POS").startBlock();
				
				for (int j = 0; j < keyframeCount; ++j) {
					stream.getStream().seek(keyframePtr + comp.keyframeStride*j + comp.keyframeOffset);
					float[] dst = new float[3];
					stream.getStream().readLEFloats(dst);
					
					writer.command("").vector(dst).floats(stream.getStream().readLEFloat(), stream.getStream().readLEFloat());
				}
				
				writer.endBlock().commandEND();
			}
		}
		
		writer.endBlock().commandEND();
	}
}
