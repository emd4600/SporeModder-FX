package sporemodder.file.gait;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.util.Vector3;
import sporemodder.util.Vector4;

public class GaitPathData {

	public static class GaitSplineParams
	{
		public final float[] spl = new float[2];
		public final int[] tanFl = new int[2];  // byte
		public int herFl;  // short
		
		public void read(StreamReader in) throws IOException
		{
			in.readLEFloats(spl);
			tanFl[0] = in.readByte();
			tanFl[1] = in.readByte();
			herFl = in.readLEShort();
			if (in.readLEInt() != 0xCDCDCDCD) {
				throw new IOException("GAIT Error: 4th spline param is not 0, file offset: " + in.getFilePointer());
			}
		}
		
		public void write(StreamWriter out) throws IOException
		{
			out.writeLEFloats(spl);
			out.writeByte(tanFl[0]);
			out.writeByte(tanFl[1]);
			out.writeLEShort(herFl);
			out.writeLEInt(0xCDCDCDCD);
		}
	}
	
	public static class GaitPathPosKey
	{
		public int tick;
		public final Vector3 pos = new Vector3();
		public final GaitSplineParams[] spl = new GaitSplineParams[3];
		
		public void read(StreamReader in) throws IOException
		{
			in.readLEInt();  // vftable
			tick = in.readLEInt();
			pos.readLE(in);
			for (int i = 0; i < spl.length; i++) {
				spl[i] = new GaitSplineParams();
				spl[i].read(in);
			}
		}
		
		public void write(StreamWriter out) throws IOException
		{
			out.writeLEInt(0xCDCDCDCD);
			out.writeLEInt(tick);
			pos.writeLE(out);
			for (int i = 0; i < spl.length; i++) {
				spl[i].write(out);
			}
		}
	}
	
	public static class GaitPathRotKey
	{
		public int tick;
		public final Vector4 rot = new Vector4();
		public final GaitSplineParams[] spl = new GaitSplineParams[4];
		
		public void read(StreamReader in) throws IOException
		{
			in.readLEInt();  // vftable
			tick = in.readLEInt();
			rot.readLE(in);
			for (int i = 0; i < spl.length; i++) {
				spl[i] = new GaitSplineParams();
				spl[i].read(in);
			}
		}
		
		public void write(StreamWriter out) throws IOException
		{
			out.writeLEInt(0xCDCDCDCD);
			out.writeLEInt(tick);
			rot.writeLE(out);
			for (int i = 0; i < spl.length; i++) {
				spl[i].write(out);
			}
		}
	}
	
	public static class GaitPathBndKey
	{
		public int tick;
		public float bnd;
		public final GaitSplineParams spl = new GaitSplineParams();
		
		public void read(StreamReader in) throws IOException
		{
			in.readLEInt();  // vftable
			tick = in.readLEInt();
			bnd = in.readLEFloat();
			spl.read(in);
		}
		
		public void write(StreamWriter out) throws IOException
		{
			out.writeLEInt(0xCDCDCDCD);
			out.writeLEInt(tick);
			out.writeLEFloat(bnd);
			spl.write(out);
		}
	}
	
	public final List<GaitPathPosKey> posKeys = new ArrayList<>();
	public final List<GaitPathRotKey> rotKeys = new ArrayList<>();
	public final List<GaitPathBndKey> bndKeys = new ArrayList<>();
	
	public void read(StreamReader in) throws IOException
	{
		int numPosKeys = in.readLEInt();
		int numRotKeys = in.readLEInt();
		int numBndKeys = in.readLEInt();
		
		for (int i = 0; i < numPosKeys; i++) {
			GaitPathPosKey key = new GaitPathPosKey();
			key.read(in);
			posKeys.add(key);
		}
		
		for (int i = 0; i < numRotKeys; i++) {
			GaitPathRotKey key = new GaitPathRotKey();
			key.read(in);
			rotKeys.add(key);
		}
		
		for (int i = 0; i < numBndKeys; i++) {
			GaitPathBndKey key = new GaitPathBndKey();
			key.read(in);
			bndKeys.add(key);
		}
	}
	
	public void write(StreamWriter out) throws IOException
	{
		out.writeLEInt(posKeys.size());
		out.writeLEInt(rotKeys.size());
		out.writeLEInt(bndKeys.size());
		
		for (GaitPathPosKey key : posKeys) key.write(out);
		for (GaitPathRotKey key : rotKeys) key.write(out);
		for (GaitPathBndKey key : bndKeys) key.write(out);
	}
	
	private static void splineParamsToArgScript(ArgScriptWriter writer, GaitSplineParams ... params) {
		writer.command("spl");
		for (GaitSplineParams param : params) writer.floats(param.spl);
		
		writer.command("tanFl");
		for (GaitSplineParams param : params) writer.ints(param.tanFl);
		
		writer.command("herFl");
		for (GaitSplineParams param : params) writer.ints(param.herFl);
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("pathData").startBlock();
		
		for (GaitPathPosKey key : posKeys) {
			writer.blankLine();
			writer.command("tick").ints(key.tick);
			writer.command("pos").vector3(key.pos);
			splineParamsToArgScript(writer, key.spl);
		}
		
		for (GaitPathRotKey key : rotKeys) {
			writer.blankLine();
			writer.command("tick").ints(key.tick);
			writer.command("rot").vector4(key.rot);
			splineParamsToArgScript(writer, key.spl);
		}
		
		for (GaitPathBndKey key : bndKeys) {
			writer.blankLine();
			writer.command("tick").ints(key.tick);
			writer.command("bnd").floats(key.bnd);
			splineParamsToArgScript(writer, key.spl);
		}
		
		writer.endBlock().commandEND();
	}
	
	public static ArgScriptBlock<GaitFile> createArgScriptBlock(Supplier<List<GaitPathData>> dstList) {
		return new ArgScriptBlock<GaitFile>()
		{
			GaitPathData pathData;
			int lastTick;
			String lastCommandType;  // pos, rot, bnd
			
			@Override
			public void parse(ArgScriptLine line) {
				pathData = new GaitPathData();
				dstList.get().add(pathData);
				lastTick = -1;
				lastCommandType = null;
				
				stream.startBlock(this);
			}
	
			@Override
			public void setData(ArgScriptStream<GaitFile> stream, GaitFile data) {
				super.setData(stream, data);
				
				final ArgScriptArguments args = new ArgScriptArguments();
				stream.addParser("tick", ArgScriptParser.create((parser, line) -> {
					Number n;
					if (line.getArguments(args, 1) && (n = stream.parseUInt(args, 0)) != null) {
						lastTick = n.intValue();
						lastCommandType = null;
					}
				}));
				
				stream.addParser("pos", ArgScriptParser.create((parser, line) -> {
					if (lastTick == -1) {
						line.createError("Tick has not been set yet");
					}
					GaitPathPosKey key = new GaitPathPosKey();
					key.tick = lastTick;
					for (int i = 0; i < key.spl.length; i++) key.spl[i] = new GaitSplineParams(); 
					lastTick = -1;
					lastCommandType = "pos";
					
					float[] values = new float[3];
					if (line.getArguments(args, 1) && stream.parseVector3(args, 0, values)) {
						key.pos.set(new Vector3(values));
						pathData.posKeys.add(key);
					}
				}));
				
				stream.addParser("rot", ArgScriptParser.create((parser, line) -> {
					if (lastTick == -1) {
						line.createError("Tick has not been set yet");
					}
					GaitPathRotKey key = new GaitPathRotKey();
					key.tick = lastTick;
					for (int i = 0; i < key.spl.length; i++) key.spl[i] = new GaitSplineParams(); 
					lastTick = -1;
					lastCommandType = "rot";
					
					float[] values = new float[4];
					if (line.getArguments(args, 1) && stream.parseVector4(args, 0, values)) {
						key.rot.set(new Vector4(values));
						pathData.rotKeys.add(key);
					}
				}));
				
				stream.addParser("bnd", ArgScriptParser.create((parser, line) -> {
					if (lastTick == -1) {
						line.createError("Tick has not been set yet");
					}
					GaitPathBndKey key = new GaitPathBndKey();
					key.tick = lastTick;
					lastTick = -1;
					lastCommandType = "bnd";
					
					Number n;
					if (line.getArguments(args, 1) && (n = stream.parseFloat(args, 0)) != null) {
						key.bnd  = n.floatValue();
						pathData.bndKeys.add(key);
					}
				}));
				
				stream.addParser("spl", ArgScriptParser.create((parser, line) -> {
					if (lastCommandType == null) {
						line.createError("Need to specify 'pos', 'rot' or 'bnd' before");
						return;
					}
					GaitSplineParams[] params = null;
					if (lastCommandType.equals("pos")) {
						params = pathData.posKeys.get(pathData.posKeys.size() - 1).spl;
					}
					else if (lastCommandType.equals("rot")) {
						params = pathData.rotKeys.get(pathData.rotKeys.size() - 1).spl;
					}
					else if (lastCommandType.equals("bnd")) {
						params = new GaitSplineParams[] { pathData.bndKeys.get(pathData.bndKeys.size() - 1).spl };
					}
					
					Number n1, n2;
					if (line.getArguments(args, 2 * params.length)) {
						for (int i = 0; i < params.length; i++) {
							if ((n1 = stream.parseFloat(args, i*2)) != null && 
									(n2 = stream.parseFloat(args, i*2 + 1)) != null) {
								params[i].spl[0] = n1.floatValue();
								params[i].spl[1] = n2.floatValue();
							}
						}
					}
				}));
				
				stream.addParser("tanFl", ArgScriptParser.create((parser, line) -> {
					if (lastCommandType == null) {
						line.createError("Need to specify 'pos', 'rot' or 'bnd' before");
						return;
					}
					GaitSplineParams[] params = null;
					if (lastCommandType.equals("pos")) {
						params = pathData.posKeys.get(pathData.posKeys.size() - 1).spl;
					}
					else if (lastCommandType.equals("rot")) {
						params = pathData.rotKeys.get(pathData.rotKeys.size() - 1).spl;
					}
					else if (lastCommandType.equals("bnd")) {
						params = new GaitSplineParams[] { pathData.bndKeys.get(pathData.bndKeys.size() - 1).spl };
					}
					
					Number n1, n2;
					if (line.getArguments(args, 2 * params.length)) {
						for (int i = 0; i < params.length; i++) {
							if ((n1 = stream.parseInt(args, i*2, -128, 127)) != null && 
									(n2 = stream.parseInt(args, i*2, -128, 127)) != null) {
								params[i].tanFl[0] = n1.intValue();
								params[i].tanFl[1] = n2.intValue();
							}
						}
					}
				}));
				
				stream.addParser("herFl", ArgScriptParser.create((parser, line) -> {
					if (lastCommandType == null) {
						line.createError("Need to specify 'pos', 'rot' or 'bnd' before");
						return;
					}
					GaitSplineParams[] params = null;
					if (lastCommandType.equals("pos")) {
						params = pathData.posKeys.get(pathData.posKeys.size() - 1).spl;
					}
					else if (lastCommandType.equals("rot")) {
						params = pathData.rotKeys.get(pathData.rotKeys.size() - 1).spl;
					}
					else if (lastCommandType.equals("bnd")) {
						params = new GaitSplineParams[] { pathData.bndKeys.get(pathData.bndKeys.size() - 1).spl };
					}
					
					Number n1;
					if (line.getArguments(args, params.length)) {
						for (int i = 0; i < params.length; i++) {
							if ((n1 = stream.parseInt(args, i, -32768, 32767)) != null) {
								params[i].herFl = n1.intValue();
							}
						}
					}
				}));
			}
		};
	}
}
