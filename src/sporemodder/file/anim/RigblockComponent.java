/****************************************************************************
* Copyright (C) 2019 Eric Mor
*
* This file is part of SporeModder FX.
*
* SporeModder FX is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
****************************************************************************/
package sporemodder.file.anim;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.file.DocumentError;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptSpecialBlock;
import sporemodder.file.argscript.ArgScriptWriter;

public class RigblockComponent implements AbstractComponentKeyframe {
	
	public static final int TYPE = 3;
	public static final String KEYWORD = "rigblock";
	private static final String[] INTERPOLATOR_NAMES = new String[]{"value", "weight"};
	
	private static final Map<String, List<Integer>> RIGBLOCK_DEFORMS = new HashMap<>();
	static {
		RIGBLOCK_DEFORMS.put("grsp", Arrays.asList(0x89E06A31, 0x75D4C8CD));
		RIGBLOCK_DEFORMS.put("skrs", Arrays.asList(0x89E06A31, 0x998BBF67));
		RIGBLOCK_DEFORMS.put("gest", Arrays.asList(0x89E06A31, 0x998BBF67));
		RIGBLOCK_DEFORMS.put("mout", Arrays.asList(0x89E06A31, 0x5D8D0055, 0x892788C6, 0xDD0DCEF4));
		RIGBLOCK_DEFORMS.put("eye", Arrays.asList(0x89E06A31, 0x9891EEC7, 0x30EE8F49));
		RIGBLOCK_DEFORMS.put("wing", Arrays.asList(0xAC04E296, 0x47F0B3DC));
		RIGBLOCK_DEFORMS.put("slsh", Arrays.asList(0x998BBF67));
		RIGBLOCK_DEFORMS.put("ear", Arrays.asList(0x39E912E1));
		RIGBLOCK_DEFORMS.put("foot", Arrays.asList(0x47F0B3DC));
		RIGBLOCK_DEFORMS.put("root", Arrays.asList(0x70E47545, 
				0x98C942B6, 0x98C942B5, 0x98C942B4, 0x98C942B3, 0x98C942B2));
		RIGBLOCK_DEFORMS.put("liqd", Arrays.asList(0x998BBF67));
		RIGBLOCK_DEFORMS.put("slid", Arrays.asList(0x998BBF67));
		RIGBLOCK_DEFORMS.put("shot", Arrays.asList(0x998BBF67));
		RIGBLOCK_DEFORMS.put("mtcl", Arrays.asList(0x89E06A31, 0x114BB90C, 0x15054351, 0xC2299AA7));
		RIGBLOCK_DEFORMS.put("mvcl", Arrays.asList(0x6FB760FF, 0xB37B55B2, 0x15054351, 0xC2299AA7, 0x2F056C5D));
		RIGBLOCK_DEFORMS.put("eycl", Arrays.asList(0x89E06A31, 0x15054351, 0xC2299AA7, 0x9891EEC7, 0x30EE8F49));
		RIGBLOCK_DEFORMS.put("wpcl", Arrays.asList(0x6FB760FF, 0x15054351, 0xC2299AA7, 0x0AC4AEED));
		RIGBLOCK_DEFORMS.put("wpch", Arrays.asList(0x15054351, 0xC2299AA7, 0xFD29BD8E, 0x2D7AE6C2, 0x0EFD249C));
		RIGBLOCK_DEFORMS.put("wpps", Arrays.asList(0x15054351, 0xC2299AA7, 0xFD29BD8E, 0x2D7AE6C2, 0x0EFD249C));
		RIGBLOCK_DEFORMS.put("wpel", Arrays.asList(0x15054351, 0xC2299AA7, 0xFD29BD8E, 0x2D7AE6C2, 0x0EFD249C));
	}
	
	public static List<Integer> getDeforms(String capability) {
		if (capability == null) return Collections.emptyList();
		List<Integer> list = RIGBLOCK_DEFORMS.get(capability);
		if (list == null) {
			throw new UnsupportedOperationException(capability + " is not a supported deformable capability");
		}
		return list;
	}
	
	public static int getDeform(String capability, int index) {
		List<Integer> list = getDeforms(capability);
		if (index >= list.size()) {
			throw new UnsupportedOperationException(index + " is not a valid deform index for capability " + capability);
		}
		return list.get(index);
	}
	
	// The ID is the rigblock animation to play
	
	public float value;
	public float weight = 1.0f;
	public final Interpolator[] interpolators = new Interpolator[2];
	
	public RigblockComponent() {
		for (int i = 0; i < interpolators.length; ++i) {
			interpolators[i] = new Interpolator();
		}
	}
	
	@Override public void read(StreamReader stream) throws IOException {
		value = stream.readLEFloat();
		weight = stream.readLEFloat();
		for (Interpolator interpolator : interpolators) {
			interpolator.read(stream);
		}
	}
	
	@Override public void write(StreamWriter stream) throws IOException {
		stream.writeLEFloat(value);
		stream.writeLEFloat(weight);
		for (Interpolator interpolator : interpolators) {
			interpolator.write(stream);
		}
	}
	
	@Override public void toArgScript(ArgScriptWriter writer, SPAnimation animation) {
		writer.floats(value);
		if (weight != 1.0f) writer.floats(weight);
		
		for (int i = 0; i < interpolators.length; ++i) {
			if (!interpolators[i].isDefault()) {
				interpolators[i].toArgScript(writer, INTERPOLATOR_NAMES[i]);
			}
		}
	}
	
	public static ArgScriptParser<SPAnimation> createParser(AnimChannelParser channelParser) {
		return new ArgScriptSpecialBlock<SPAnimation>() {
			
			final ArgScriptArguments args = new ArgScriptArguments();
			AnimationComponentData compData;
			// We want to show an error in the component line when keyframes number don't match,
			// but we only know that after parsing the whole component
			DocumentError keyframesError;

			@Override public void onBlockEnd() {
				if (!channelParser.channel.components.isEmpty() 
						&& compData.keyframes.size() != channelParser.channel.components.get(0).keyframes.size()) {
					stream.addError(keyframesError);
				}
					
				stream.endSpecialBlock();
			}

			@Override public void parse(ArgScriptLine line) {
				compData = new AnimationComponentData();
				compData.flags = TYPE;
				channelParser.channel.components.add(compData);
				
				compData.parseFlags(line);
				
				if (line.getArguments(args, 1)) {
					
					Integer id = stream.parseFileID(args, 0);
					
					if (id != null) {
						compData.id = id;
						
						String cap = channelParser.channel.primaryContext.getCapability();
						if (cap == null) cap = "";
						List<Integer> deforms = RIGBLOCK_DEFORMS.get(cap);
						if (deforms == null) {
							stream.addError(line.createErrorForArgument("Cannot add deforms for capability '" + cap + "', not supported.", 0));
						} else {
							int indexOf = deforms.indexOf(compData.id);
							if (indexOf == -1) {
								stream.addError(line.createErrorForArgument(args.get(0) + " is not a valid deform of the capability " + cap, 0));
							}
							else {
								compData.index = 3 + indexOf;
							}
						}
					}
				}
				
				Number value;
				if (line.getOptionArguments(args, "flags", 1) &&
						(value = stream.parseInt(args, 0)) != null) {
					compData.flags |= value.intValue();
				}
				
				// Save for later
				keyframesError = line.createError(AnimChannelParser.KEYFRAMES_ERROR);
				keyframesError.setLine(stream.getCurrentLine());
				
				stream.startSpecialBlock(this, "end");
			}
			
			@Override public boolean processLine(String line) {
				ArgScriptLine l = preprocess(line);
				l.getSplitsAsArguments(args);
				l.setHasKeyword(false);
				
				RigblockComponent c = new RigblockComponent();
				compData.keyframes.add(c);
				
				Number value;
				if ((value = stream.parseFloat(args, 0)) != null) {
					c.value = value.floatValue();
				}
				
				if (args.size() > 1 && !args.get(1).startsWith("-")) {
					if ((value = stream.parseFloat(args, 1)) != null) {
						c.weight = value.floatValue();
					}
				}
				
				for (int i = 0; i < c.interpolators.length; ++i) {
					c.interpolators[i].parse(l, INTERPOLATOR_NAMES[i]);
				}
				
				stream.addSyntax(l, false);
				
				return true;
			}
		};
	}
}
