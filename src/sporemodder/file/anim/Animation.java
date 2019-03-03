package sporemodder.file.anim;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import emord.filestructures.MemoryStream;
import emord.filestructures.StreamReader;
import sporemodder.MainApp;
import sporemodder.file.argscript.ArgScriptWriter;

public class Animation {

	private static final int MAGIC = 0x4D494E41;
	
	private final List<AnimationChannel> channels = new ArrayList<>();
	
	public void read(StreamReader stream) throws IOException {
		int magic = stream.readLEInt();
		if (magic != MAGIC) {
			throw new IOException("Unsupported animation magic: 0x" + Integer.toHexString(magic));
		}
		
		DataStructure data = new DataStructure(stream);
		data.setPointer(0);
		
		int channelCount = data.getInt(0x144);
		long channelPtr = data.getUInt(0x148);
		
		for (int i = 0; i < channelCount; ++i) {
			data.setPointer(channelPtr);
			long ptr = data.getUInt(4 * i);
			
			data.setPointer(ptr);
			AnimationChannel channel = new AnimationChannel();
			channel.read(data);
			channels.add(channel);
		}
	}
	
	public void toArgScript(ArgScriptWriter writer, StreamReader stream) throws IOException {
		DataStructure data = new DataStructure(stream);
		data.setPointer(0);
		
		for (AnimationChannel channel : channels) {
			channel.toArgScript(writer, data);
			writer.blankLine();
		}
	}
	
	public static void main(String[] args) throws IOException {
		String path = "C:\\Users\\Eric\\Desktop\\#30EF4216.animation";
		MainApp.testInit();
		
		try (MemoryStream stream = new MemoryStream(Files.readAllBytes(new File(path).toPath()))) {
			
			Animation animation = new Animation();
			animation.read(stream);
			
			ArgScriptWriter writer = new ArgScriptWriter();
			animation.toArgScript(writer, stream);
			
			System.out.println(writer.toString());
		}
	}
}
