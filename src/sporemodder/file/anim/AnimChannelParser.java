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

import sporemodder.file.anim.AnimationChannel.ContextSelector;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;

public class AnimChannelParser extends ArgScriptBlock<SPAnimation> {
	
	public static final String KEYFRAMES_ERROR = "All components in the channel must have the same amount of keyframes.";
	
	public AnimationChannel channel;
	final ArgScriptArguments args = new ArgScriptArguments();
	

	@Override public void parse(ArgScriptLine line) {
		if (getData().length == 0) {
			stream.addError(line.createError("Must specify a valid 'length' for this animation before describing channels."));
		}
		
		channel = new AnimationChannel();
		getData().channels.add(channel);
		
		stream.startBlock(this);
		
		parseSelector(line, channel.primaryContext, false);
		
		if (!line.hasFlag("noInterpolate")) {
			channel.bindFlags |= AnimationChannel.BIND_FLAG_INTERPOLATE;
		}
		if (line.hasFlag("require")) {
			channel.bindFlags |= AnimationChannel.BIND_FLAG_REQUIRE;
		}
		
		if (line.hasFlag("groundRelative")) {
			channel.movementFlags |= AnimationChannel.MOVEMENT_FLAG_GROUND_RELATIVE;
		}
		if (line.hasFlag("secondaryDirectionalOnly")) {
			channel.movementFlags |= AnimationChannel.MOVEMENT_FLAG_SECONDARY_DIRECTIONAL_ONLY;
		}
		if (line.hasFlag("rotRelativeExtTarg")) {
			channel.movementFlags |= AnimationChannel.MOVEMENT_FLAG_LOOKAT;
		}
		
		Number value;
		
		if (line.getOptionArguments(args, "blendGroup", 1) &&
				(value = stream.parseInt(args, 0, 0, 255)) != null) {
			channel.bindFlags |= value.intValue() << 16;
		}
		if (line.getOptionArguments(args, "variantGroup", 1) &&
				(value = stream.parseInt(args, 0, 0, 255)) != null) {
			channel.bindFlags |= value.intValue() << 24;
		}
		
		if ((line.getOptionArguments(args, "field_88", 1) || line.getOptionArguments(args, "movementFlags", 1)) &&
				(value = stream.parseInt(args, 0)) != null) {
			channel.movementFlags |= value.intValue();
		}
		
		if (line.getOptionArguments(args, "field_8C", 1) &&
				(value = stream.parseInt(args, 0)) != null) {
			channel.primaryContext.flags |= value.intValue();
		}
		
		if (line.getOptionArguments(args, "field_9C", 1) &&
				(value = stream.parseInt(args, 0)) != null) {
			channel.secondaryContext.flags |= value.intValue();
		}
		
		if ((line.getOptionArguments(args, "field_AC", 1) || line.getOptionArguments(args, "bindFlags", 1)) &&
				(value = stream.parseInt(args, 0)) != null) {
			channel.bindFlags |= value.intValue();
		}
	}
	
	private void parseSelector(ArgScriptLine line, ContextSelector context, boolean isSecondary)
	{
		context.flags = 0;
		
		if (line.hasFlag("noSelect")) {
			line.getArguments(args, 0);
			context.flags = AnimationChannel.SELECT_TYPE_NONE;
			return;
		}
		
		int capArgIndex = -1;
		
		if (isSecondary) 
		{
			if (line.getArguments(args, 0, 1)) {
				if (args.size() == 1 && !Character.isDigit(args.get(0).charAt(0))) capArgIndex = 0;
			}
		}
		else {
			if (line.getArguments(args, 1, 2)) {
				channel.name = args.get(0);
				if (args.size() == 2) capArgIndex = 1;
			}
		}
		
		if (capArgIndex != -1) 
		{
			String arg = args.get(capArgIndex).trim();
			
			if (!arg.equals("frameRoot") && !arg.equals("frame") && arg.length() > 4) {
				stream.addError(line.createErrorForArgument("Capability can only be 'frame', 'frameRoot', or a PCTP code of 4 charaters or less", capArgIndex));
			}
			
			context.capability = null;
			
			if (arg.equals("root")) {
				context.flags = AnimationChannel.SELECT_TYPE_ROOT;
			}
			else if (arg.equals("frame")) {
				context.flags = AnimationChannel.SELECT_TYPE_FRAME;
			}
			else if (arg.equals("frameRoot")) {
				context.flags = AnimationChannel.SELECT_TYPE_FRAME_ROOT;
			}
			else {
				context.capability = arg;
				context.flags = AnimationChannel.SELECT_TYPE_CAP;
			}
		}
		else {
			context.flags = AnimationChannel.SELECT_TYPE_NOCAP;
			
			if (isSecondary && line.getArguments(args, 0, 1) && args.size() == 1) {
				// If it has an argument but it's not a capability, then it's the external target index
				Number value = null;
				if ((value = stream.parseInt(args, 0)) != null) {
					context.id = value.intValue();
				}
			}
		}
		
		
		if (line.getOptionArguments(args, "selectX", 1)) {
			context.flags |= AnimationChannel.ENUM_SELECTX.get(args, 0);
		}
		if (line.getOptionArguments(args, "selectY", 1)) {
			context.flags |= AnimationChannel.ENUM_SELECTY.get(args, 0);
		}
		if (line.getOptionArguments(args, "selectZ", 1)) {
			context.flags |= AnimationChannel.ENUM_SELECTZ.get(args, 0);
		}
		if (line.getOptionArguments(args, "extent", 1)) {
			context.flags |= AnimationChannel.ENUM_EXTENT.get(args, 0);
		}
		
		Number value = null;
		
		if (line.getOptionArguments(args, "limb", 1) &&
				(value = stream.parseInt(args, 0)) != null) {
			context.flags |= value.intValue() & AnimationChannel.SELECT_LIMB_MASK;
		}
		
		if (line.getOptionArguments(args, "selectFlags", 1) &&
				(value = stream.parseInt(args, 0)) != null) {
			context.flags |= value.intValue();
		}
	}

	@Override public void setData(ArgScriptStream<SPAnimation> stream, SPAnimation data) {
		super.setData(stream, data);
		
		this.addParser(InfoComponent.KEYWORD, InfoComponent.createParser(this));
		this.addParser(PosComponent.KEYWORD, PosComponent.createParser(this));
		this.addParser(RotComponent.KEYWORD, RotComponent.createParser(this));
		this.addParser(RigblockComponent.KEYWORD, RigblockComponent.createParser(this));
		
		this.addParser("secondary", ArgScriptParser.create((parser, line) -> {
			parseSelector(line, channel.secondaryContext, true);
			
			channel.movementFlags |= AnimationChannel.MOVEMENT_FLAG_SECONDARY;
		}));
	}
	
	@Override public void onBlockEnd() {
		if (!channel.components.isEmpty()) {
			channel.keyframeCount = channel.components.get(0).keyframes.size();
			
			AnimationComponentData info = channel.components.stream().filter(c -> (c.flags & 0xF) == InfoComponent.TYPE).findFirst().orElse(null);
			if (info != null) {
				boolean hasVFX = info.keyframes.stream().anyMatch(c -> ((InfoComponent)c).eventCount > 0);
				if (hasVFX) channel.bindFlags |= AnimationChannel.BIND_FLAG_EVENT;
			}
		}
	}
}
