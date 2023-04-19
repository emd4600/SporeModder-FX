package sporemodder.file.gait;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.MainApp;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

public class GaitFile {
	private static final int VERSION = 11;
	private static final int MAGIC = 0x54494147;  //'GAIT' in little endian
	
	public int version = VERSION;
	public final List<GaitStepLengthKey> stepLengthKeys = new ArrayList<>();
	public final List<GaitGroupKey> gaitGroupKeys = new ArrayList<>();
	
	public void read(StreamReader in) throws IOException {
		//minharmo -> field_4
		if (in.readLEInt() != MAGIC)
			throw new IOException("GAIT Error: unexpected MAGIC, file pointer " + in.getFilePointer());
		
		if (in.readLEInt() != in.length())
			throw new IOException("GAIT Error: unexpected file size, file pointer " + in.getFilePointer());
		
		if (in.readLEInt() != VERSION)
			throw new IOException("GAIT Error: unexpected version, file pointer " + in.getFilePointer());
		
		if (in.readLEInt() != 1)
			throw new IOException("GAIT Error: unexpected 1 value, file pointer " + in.getFilePointer());
		
		int stepLengthKeysCount = in.readLEInt();
		int gaitGroupKeysCount = in.readLEInt();
		
		for (int i = 0; i < stepLengthKeysCount; i++) {
			GaitStepLengthKey stepLengthKey = new GaitStepLengthKey();
			stepLengthKey.read(in);
			stepLengthKeys.add(stepLengthKey);
		}
		
		for (int i = 0; i < gaitGroupKeysCount; i++) {
			GaitGroupKey gaitGroupKey = new GaitGroupKey();
			gaitGroupKey.read(in);
			gaitGroupKeys.add(gaitGroupKey);
		}
		
	}
	
	public void write(StreamWriter out) throws IOException {
		out.writeLEInt(MAGIC);
		out.writeLEInt(0);  // file size
		out.writeLEInt(VERSION);
		out.writeLEInt(1);  // ?
		
		out.writeLEInt(stepLengthKeys.size());
		out.writeLEInt(gaitGroupKeys.size());
		
		for (GaitStepLengthKey f : stepLengthKeys) {
			f.write(out);
		}
		
		for (GaitGroupKey f : gaitGroupKeys) {
			f.write(out);
		}
		
		out.seek(4);
		out.writeLEUInt(out.length());
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("version").ints(VERSION);
		writer.blankLine();
		
		for (GaitStepLengthKey f : stepLengthKeys) {
			f.toArgScript(writer);
			writer.blankLine();
		}
		
		for (GaitGroupKey f : gaitGroupKeys) {
			f.toArgScript(writer);
			writer.blankLine();
		}
	}
	
	public void clear() {
		stepLengthKeys.clear();
		gaitGroupKeys.clear();
	}
	
	public ArgScriptWriter toArgScript() {
		ArgScriptWriter writer = new ArgScriptWriter();
		toArgScript(writer);
		return writer;
	}
	
	public ArgScriptStream<GaitFile> generateStream() {
		ArgScriptStream<GaitFile> stream = new ArgScriptStream<>();
		stream.setData(this);
		stream.addDefaultParsers();
		
		stream.addParser("version", ArgScriptParser.create((parser, line) -> {
			Number value = null;
			final ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
				version = value.intValue();
				if (version != VERSION) {
					stream.addWarning(line.createErrorForArgument("Only version 11 is supported", 1));
				}
			}
		}));
		
		stream.addParser("stepLengthKey", GaitStepLengthKey.createArgScriptBlock(stepLengthKeys));
		stream.addParser("gaitGroupKey", GaitGroupKey.createArgScriptBlock(gaitGroupKeys));
		
		return stream;
	}
	
	@FunctionalInterface
	static interface QuickFloatParser {
		public void setValue(float value);
	}
	static ArgScriptParser<GaitFile> createFloatParser(ArgScriptStream<GaitFile> stream, QuickFloatParser parser) {
		return ArgScriptParser.create((parser_, line) -> {
			Number value = null;
			ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
				parser.setValue(value.floatValue());
			}
		}); 
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		MainApp.testInit();
		
		File baseFolder = new File("C:\\Users\\Eric\\Desktop\\gaits");
		for (File file : baseFolder.listFiles()) {
			if (file.getName().endsWith(".gait")) {
				System.out.println("Converting " + file.getName());
				
				try (StreamReader stream = new FileStream(file, "r")) {
					GaitFile gait = new GaitFile();
					gait.read(stream);
					gait.toArgScript().write(new File(baseFolder, file.getName() + ".gait_t"));
				}
			}
		}
	}
}
