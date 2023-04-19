package sporemodder.file.gait;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.file.DocumentError;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

public class GaitGroupKey {
	public int groupIndex = 0; // always 0?
	public float speedi;  // 0x1C4
	public float stepHeight;  // 0x120
	public int taxon;  // 0x124
	public final List<Float> triggers = new ArrayList<Float>();
	public final List<Float> dutyFactors = new ArrayList<Float>();
	public float stepGallop;
	public float stepPhaseBiasH;
	public float stepPhaseBiasV;
	public float stepSkew;
	public float footTilt;
	public float tiltPoint;
	public float toeCurlMax;
	public float toeCurlBegin;
	public float toeCurlEnd;
	public float swayAmplitude;
	public float swayPhase;
	public float trackWidthReduction;
	public float verticalPhaseOffset;
	public float sagittalPhaseOffset;
	public float lateralPhaseOffset;
	public float maxVerticalOffset;
	public float verticalDist;
	public float walkRunShape;
	public float maxSagittalOffset;
	public float maxLateralOffset;
	public float yawPhaseOffset;
	public float pitchPhaseOffset;
	public float rollPhaseOffset;
	public float maxYawAngle;
	public float maxPitchAngle;
	public float maxRollAngle;
	public final List<GaitPathData> pathData = new ArrayList<>();
	
	public void read(StreamReader in) throws IOException {
		speedi = in.readLEFloat();
		stepHeight = in.readLEFloat();
		taxon = in.readLEInt();
		stepGallop = in.readLEFloat();
		stepPhaseBiasH = in.readLEFloat();
		stepPhaseBiasV = in.readLEFloat();
		stepSkew = in.readLEFloat();
		footTilt = in.readLEFloat();
		tiltPoint = in.readLEFloat();
		toeCurlMax = in.readLEFloat();
		toeCurlBegin = in.readLEFloat();
		toeCurlEnd = in.readLEFloat();
		swayAmplitude = in.readLEFloat();
		swayPhase = in.readLEFloat();
		trackWidthReduction = in.readLEFloat();
		verticalPhaseOffset = in.readLEFloat();
		sagittalPhaseOffset = in.readLEFloat();
		lateralPhaseOffset = in.readLEFloat();
		maxVerticalOffset = in.readLEFloat();
		verticalDist = in.readLEFloat();
		walkRunShape = in.readLEFloat();
		maxSagittalOffset = in.readLEFloat();
		maxLateralOffset = in.readLEFloat();
		yawPhaseOffset = in.readLEFloat();
		pitchPhaseOffset = in.readLEFloat();
		rollPhaseOffset = in.readLEFloat();
		maxYawAngle = in.readLEFloat();
		maxPitchAngle = in.readLEFloat();
		maxRollAngle = in.readLEFloat();
		
		int taxonCount = in.readLEInt();
		int pathDataCount = in.readLEInt();
		for (int f = 0; f < taxonCount; f++) {
			triggers.add(in.readLEFloat());
			dutyFactors.add(in.readLEFloat());
		}
		for (int i = 0; i < pathDataCount; i++) {
			GaitPathData path = new GaitPathData();
			path.read(in);
			pathData.add(path);
		}
	}
	
	public void write(StreamWriter out) throws IOException {
		out.writeLEFloat(speedi);
		out.writeLEFloat(stepHeight);
		out.writeLEInt(taxon);
		out.writeLEFloat(stepGallop);
		out.writeLEFloat(stepPhaseBiasH);
		out.writeLEFloat(stepPhaseBiasV);
		out.writeLEFloat(stepSkew);
		out.writeLEFloat(footTilt);
		out.writeLEFloat(tiltPoint);
		out.writeLEFloat(toeCurlMax);
		out.writeLEFloat(toeCurlBegin);
		out.writeLEFloat(toeCurlEnd);
		out.writeLEFloat(swayAmplitude);
		out.writeLEFloat(swayPhase);
		out.writeLEFloat(trackWidthReduction);
		out.writeLEFloat(verticalPhaseOffset);
		out.writeLEFloat(sagittalPhaseOffset);
		out.writeLEFloat(lateralPhaseOffset);
		out.writeLEFloat(maxVerticalOffset);
		out.writeLEFloat(verticalDist);
		out.writeLEFloat(walkRunShape);
		out.writeLEFloat(maxSagittalOffset);
		out.writeLEFloat(maxLateralOffset);
		out.writeLEFloat(yawPhaseOffset);
		out.writeLEFloat(pitchPhaseOffset);
		out.writeLEFloat(rollPhaseOffset);
		out.writeLEFloat(maxYawAngle);
		out.writeLEFloat(maxPitchAngle);
		out.writeLEFloat(maxRollAngle);
		
		out.writeLEInt(triggers.size());
		out.writeLEInt(pathData.size());
		
		for (int i = 0; i < triggers.size(); i++) {
			out.writeLEFloat(triggers.get(i));
			out.writeLEFloat(dutyFactors.get(i));
		}
		
		for (int i = 0; i < pathData.size(); i++) {
			pathData.get(i).write(out);
		}
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("gaitGroupKey").startBlock();
		
		writer.command("groupIndex").ints(groupIndex);
		writer.command("speedi").floats(speedi);
		writer.command("stepHeight").floats(stepHeight);
		writer.command("stepGallop").floats(stepGallop);
		writer.command("stepPhaseBiasH").floats(stepPhaseBiasH);
		writer.command("stepPhaseBiasV").floats(stepPhaseBiasV);
		writer.command("stepSkew").floats(stepSkew);
		writer.command("footTilt").floats(footTilt);
		writer.command("tiltPoint").floats(tiltPoint);
		writer.command("toeCurlMax").floats(toeCurlMax);
		writer.command("toeCurlBegin").floats(toeCurlBegin);
		writer.command("toeCurlEnd").floats(toeCurlEnd);
		writer.command("swayAmplitude").floats(swayAmplitude);
		writer.command("swayPhase").floats(swayPhase);
		writer.command("trackWidthReduction").floats(trackWidthReduction);
		writer.command("verticalPhaseOffset").floats(verticalPhaseOffset);
		writer.command("sagittalPhaseOffset").floats(sagittalPhaseOffset);
		writer.command("lateralPhaseOffset").floats(lateralPhaseOffset);
		writer.command("maxVerticalOffset").floats(maxVerticalOffset);
		writer.command("verticalDist").floats(verticalDist);
		writer.command("walkRunShape").floats(walkRunShape);
		writer.command("maxSagittalOffset").floats(maxSagittalOffset);
		writer.command("maxLateralOffset").floats(maxLateralOffset);
		writer.command("yawPhaseOffset").floats(yawPhaseOffset);
		writer.command("pitchPhaseOffset").floats(pitchPhaseOffset);
		writer.command("rollPhaseOffset").floats(rollPhaseOffset);
		writer.command("maxYawAngle").floats(maxYawAngle);
		writer.command("maxPitchAngle").floats(maxPitchAngle);
		writer.command("maxRollAngle").floats(maxRollAngle);
		
		writer.blankLine();
		writer.command("taxon").ints(taxon);
		for (int i = 0; i < triggers.size(); i++) {
			writer.command("trigger").floats(triggers.get(i));
			writer.command("dutyFactor").floats(dutyFactors.get(i));
		}
		
		for (int i = 0; i < pathData.size(); i++)
		{
			writer.blankLine();
			GaitPathData path = pathData.get(i);
			path.toArgScript(writer);
		}
		
		writer.endBlock().commandEND();
	}
	
	public static ArgScriptBlock<GaitFile> createArgScriptBlock(List<GaitGroupKey> dstList) {
		return new ArgScriptBlock<GaitFile>() {
			GaitGroupKey key;
			DocumentError notequalError;
			
			@Override
			public void parse(ArgScriptLine line) {
				key = new GaitGroupKey();
				dstList.add(key);
				
				notequalError = line.createError("'taxon' block must have the same amount of 'trigger' and 'dutyFactor' commands");
				
				stream.startBlock(this);
			}
			
			@Override
			public void setData(ArgScriptStream<GaitFile> stream, GaitFile data) {
				super.setData(stream, data);
				
				addParser("groupIndex", ArgScriptParser.create((parser_, line) -> {
					Number value = null;
					ArgScriptArguments args = new ArgScriptArguments();
					if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
						key.groupIndex = value.intValue();
					}
				}));
				addParser("taxon", ArgScriptParser.create((parser_, line) -> {
					Number value = null;
					ArgScriptArguments args = new ArgScriptArguments();
					if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
						key.taxon = value.intValue();
					}
				}));
				
				addParser("trigger", GaitFile.createFloatParser(stream, value -> { key.triggers.add(value); }));
				addParser("dutyFactor", GaitFile.createFloatParser(stream, value -> { key.dutyFactors.add(value); }));
				
				addParser("speedi", GaitFile.createFloatParser(stream, value -> { key.speedi = value; }));
				addParser("stepHeight", GaitFile.createFloatParser(stream, value -> { key.stepHeight = value; }));
				addParser("stepGallop", GaitFile.createFloatParser(stream, value -> { key.stepGallop = value; }));
				addParser("stepPhaseBiasH", GaitFile.createFloatParser(stream, value -> { key.stepPhaseBiasH = value; }));
				addParser("stepPhaseBiasV", GaitFile.createFloatParser(stream, value -> { key.stepPhaseBiasV = value; }));
				addParser("stepSkew", GaitFile.createFloatParser(stream, value -> { key.stepSkew = value; }));
				addParser("footTilt", GaitFile.createFloatParser(stream, value -> { key.footTilt = value; }));
				addParser("tiltPoint", GaitFile.createFloatParser(stream, value -> { key.tiltPoint = value; }));
				addParser("toeCurlMax", GaitFile.createFloatParser(stream, value -> { key.toeCurlMax = value; }));
				addParser("toeCurlBegin", GaitFile.createFloatParser(stream, value -> { key.toeCurlBegin = value; }));
				addParser("toeCurlEnd", GaitFile.createFloatParser(stream, value -> { key.toeCurlEnd = value; }));
				addParser("swayAmplitude", GaitFile.createFloatParser(stream, value -> { key.swayAmplitude = value; }));
				addParser("swayPhase", GaitFile.createFloatParser(stream, value -> { key.swayPhase = value; }));
				addParser("trackWidthReduction", GaitFile.createFloatParser(stream, value -> { key.trackWidthReduction = value; }));
				addParser("verticalPhaseOffset", GaitFile.createFloatParser(stream, value -> { key.verticalPhaseOffset = value; }));
				addParser("sagittalPhaseOffset", GaitFile.createFloatParser(stream, value -> { key.sagittalPhaseOffset = value; }));
				addParser("lateralPhaseOffset", GaitFile.createFloatParser(stream, value -> { key.lateralPhaseOffset = value; }));
				addParser("maxVerticalOffset", GaitFile.createFloatParser(stream, value -> { key.maxVerticalOffset = value; }));
				addParser("verticalDist", GaitFile.createFloatParser(stream, value -> { key.verticalDist = value; }));
				addParser("walkRunShape", GaitFile.createFloatParser(stream, value -> { key.walkRunShape = value; }));
				addParser("maxSagittalOffset", GaitFile.createFloatParser(stream, value -> { key.maxSagittalOffset = value; }));
				addParser("maxLateralOffset", GaitFile.createFloatParser(stream, value -> { key.maxLateralOffset = value; }));
				addParser("yawPhaseOffset", GaitFile.createFloatParser(stream, value -> { key.yawPhaseOffset = value; }));
				addParser("pitchPhaseOffset", GaitFile.createFloatParser(stream, value -> { key.pitchPhaseOffset = value; }));
				addParser("rollPhaseOffset", GaitFile.createFloatParser(stream, value -> { key.rollPhaseOffset = value; }));
				addParser("maxYawAngle", GaitFile.createFloatParser(stream, value -> { key.maxYawAngle = value; }));
				addParser("maxPitchAngle", GaitFile.createFloatParser(stream, value -> { key.maxPitchAngle = value; }));
				addParser("maxRollAngle", GaitFile.createFloatParser(stream, value -> { key.maxRollAngle = value; }));
				
				addParser("pathData", GaitPathData.createArgScriptBlock(() -> key.pathData));
			}
			
			public void onBlockEnd() {
				if (key.triggers.size() != key.dutyFactors.size()) {
					stream.addError(notequalError);
				}
			}
		};
	}
}
