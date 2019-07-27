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

import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
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
		
		if (line.getArguments(args, 1, 2)) {
			channel.name = args.get(0);
			
			if (args.size() == 2) {
//				if (channel.name.equals("root")) {
//					stream.addError(line.createErrorForArgument("Cannot specify pctp capability for 'root' channel.", 1));
//				}
//				else if (args.get(1).length() > 4) {
//					stream.addError(line.createErrorForArgument("pctp capability can only have up to 4 characters", 1));
//				}
//				else {
//					channel.capability = args.get(1);
//				}
				
				if (args.get(1).length() > 4) {
					stream.addError(line.createErrorForArgument("pctp capability can only have up to 4 characters", 1));
				}
				
				channel.capability = args.get(1).trim();
				
				if (channel.capability.equals("root")) {
					channel.capability = null;
				}
			}
//			else if (!channel.name.equals("root")) {
//				stream.addError(line.createErrorForArgument("Must specify pctp capability for this channel.", 1));
//			}
			else {
				// It's root, uses this  // not really?
				//channel.field_8C = 1;
			}
		}
		
		channel.field_8C = 0;
		
		if (line.getOptionArguments(args, "selectX", 1)) {
			channel.field_8C |= AnimationChannel.ENUM_SELECTX.get(args, 0);
		}
		if (line.getOptionArguments(args, "selectY", 1)) {
			channel.field_8C |= AnimationChannel.ENUM_SELECTY.get(args, 0);
		}
		if (line.getOptionArguments(args, "selectZ", 1)) {
			channel.field_8C |= AnimationChannel.ENUM_SELECTZ.get(args, 0);
		}
		
		Number value;
		if (line.getOptionArguments(args, "field_88", 1) &&
				(value = stream.parseInt(args, 0)) != null) {
			channel.field_88 = value.intValue();
		}
		
		if (line.getOptionArguments(args, "field_8C", 1) &&
				(value = stream.parseInt(args, 0)) != null) {
			channel.field_8C |= value.intValue();
		}
		
		if (line.getOptionArguments(args, "field_9C", 1) &&
				(value = stream.parseInt(args, 0)) != null) {
			channel.field_9C = value.intValue();
		}
		
		if (line.getOptionArguments(args, "field_AC", 1) &&
				(value = stream.parseInt(args, 0)) != null) {
			channel.field_AC = value.intValue();
		}
			
		stream.startBlock(this);
	}

	@Override public void setData(ArgScriptStream<SPAnimation> stream, SPAnimation data) {
		super.setData(stream, data);
		
		this.addParser(InfoComponent.KEYWORD, InfoComponent.createParser(this));
		this.addParser(PosComponent.KEYWORD, PosComponent.createParser(this));
		this.addParser(RotComponent.KEYWORD, RotComponent.createParser(this));
		this.addParser(RigblockComponent.KEYWORD, RigblockComponent.createParser(this));
	}
	
	@Override public void onBlockEnd() {
		if (!channel.components.isEmpty()) {
			channel.keyframeCount = channel.components.get(0).keyframes.size();
			
		}
	}
}
