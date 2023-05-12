package sporemodder.file.gait;

import java.io.IOException;
import java.util.List;

import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.argscript.ParserUtils;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

public class GaitStepLengthKey {
	public float speedi;
	public float stepLength;
	
	public void read(StreamReader in) throws IOException {
		speedi = in.readLEFloat();
		stepLength = in.readLEFloat();
	}
	
	public void write(StreamWriter out) throws IOException {
		out.writeLEFloat(speedi);
		out.writeLEFloat(stepLength);
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("stepLengthKey").startBlock();
		writer.command("speedi").floats(speedi);
		writer.command("stepLength").floats(stepLength);
		writer.endBlock().commandEND();
	}
	
	public static ArgScriptBlock<GaitFile> createArgScriptBlock(List<GaitStepLengthKey> dstList) {
		return new ArgScriptBlock<GaitFile>() {
			GaitStepLengthKey key;
			
			@Override
			public void parse(ArgScriptLine line) {
				key = new GaitStepLengthKey();
				dstList.add(key);
				
				stream.startBlock(this);
			}
			
			@Override
			public void setData(ArgScriptStream<GaitFile> stream, GaitFile data) {
				super.setData(stream, data);
				
				ParserUtils.createFloatParser("speedi", stream, value -> { key.speedi = value; });
				ParserUtils.createFloatParser("stepLength", stream, value -> { key.stepLength = value; });
			}
		};
	}
}
