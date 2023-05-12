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
import sporemodder.file.argscript.ParserUtils;
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
				
				ParserUtils.createFloatParser("trigger", stream, value -> { key.triggers.add(value); });
				ParserUtils.createFloatParser("dutyFactor", stream, value -> { key.dutyFactors.add(value); });
				
				ParserUtils.createFloatParser("speedi", stream, value -> { key.speedi = value; });
				ParserUtils.createFloatParser("stepHeight", stream, value -> { key.stepHeight = value; });
				ParserUtils.createFloatParser("stepGallop", stream, value -> { key.stepGallop = value; });
				ParserUtils.createFloatParser("stepPhaseBiasH", stream, value -> { key.stepPhaseBiasH = value; });
				ParserUtils.createFloatParser("stepPhaseBiasV", stream, value -> { key.stepPhaseBiasV = value; });
				ParserUtils.createFloatParser("stepSkew", stream, value -> { key.stepSkew = value; });
				ParserUtils.createFloatParser("footTilt", stream, value -> { key.footTilt = value; });
				ParserUtils.createFloatParser("tiltPoint", stream, value -> { key.tiltPoint = value; });
				ParserUtils.createFloatParser("toeCurlMax", stream, value -> { key.toeCurlMax = value; });
				ParserUtils.createFloatParser("toeCurlBegin", stream, value -> { key.toeCurlBegin = value; });
				ParserUtils.createFloatParser("toeCurlEnd", stream, value -> { key.toeCurlEnd = value; });
				ParserUtils.createFloatParser("swayAmplitude", stream, value -> { key.swayAmplitude = value; });
				ParserUtils.createFloatParser("swayPhase", stream, value -> { key.swayPhase = value; });
				ParserUtils.createFloatParser("trackWidthReduction", stream, value -> { key.trackWidthReduction = value; });
				ParserUtils.createFloatParser("verticalPhaseOffset", stream, value -> { key.verticalPhaseOffset = value; });
				ParserUtils.createFloatParser("sagittalPhaseOffset", stream, value -> { key.sagittalPhaseOffset = value; });
				ParserUtils.createFloatParser("lateralPhaseOffset", stream, value -> { key.lateralPhaseOffset = value; });
				ParserUtils.createFloatParser("maxVerticalOffset", stream, value -> { key.maxVerticalOffset = value; });
				ParserUtils.createFloatParser("verticalDist", stream, value -> { key.verticalDist = value; });
				ParserUtils.createFloatParser("walkRunShape", stream, value -> { key.walkRunShape = value; });
				ParserUtils.createFloatParser("maxSagittalOffset", stream, value -> { key.maxSagittalOffset = value; });
				ParserUtils.createFloatParser("maxLateralOffset", stream, value -> { key.maxLateralOffset = value; });
				ParserUtils.createFloatParser("yawPhaseOffset", stream, value -> { key.yawPhaseOffset = value; });
				ParserUtils.createFloatParser("pitchPhaseOffset", stream, value -> { key.pitchPhaseOffset = value; });
				ParserUtils.createFloatParser("rollPhaseOffset", stream, value -> { key.rollPhaseOffset = value; });
				ParserUtils.createFloatParser("maxYawAngle", stream, value -> { key.maxYawAngle = value; });
				ParserUtils.createFloatParser("maxPitchAngle", stream, value -> { key.maxPitchAngle = value; });
				ParserUtils.createFloatParser("maxRollAngle", stream, value -> { key.maxRollAngle = value; });
				
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
