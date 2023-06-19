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
package sporemodder.file.effects;

import java.io.IOException;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;

public class SplitterResource extends EffectResource {
	
	public static class SplitKernelCylinder {
		public final float[] origin = new float[3];
		public final float[] direction = new float[3];
		public float radius;
	}
	
	public static class SplitKernelPlane {
		public final float[] field_0 = new float[3];  // normal / origin
		public float field_C;  // offset / radius
	}

	public static final String KEYWORD = "splitter";
	public static final int TYPE_CODE = 0x10;
	
	public static final EffectResourceFactory FACTORY = new Factory();
	
	public static final int PLANE = 0;
	public static final int SPHERE = 1;
	public static final int CYLINDER = 2;
	
	public static final ArgScriptEnum ENUM_TYPE = new ArgScriptEnum();
	static {
		ENUM_TYPE.add(PLANE, "plane");
		ENUM_TYPE.add(SPHERE, "sphere");
		ENUM_TYPE.add(CYLINDER, "cylinder");
	}
	
	public String name;
	public int type = -1;
	public final SplitKernelPlane[] plane = new SplitKernelPlane[2];
	public final SplitKernelCylinder[] cylinder = new SplitKernelCylinder[2];
	
	public SplitterResource(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public String getName() {
		return name;
	}
	
	@Override public void read(StreamReader in) throws IOException {
		type = in.readInt();
		
		if (type == PLANE || type == SPHERE) {
			for (int i = 0; i < 2; i++) {
				plane[i] = new SplitKernelPlane();
				in.readLEFloats(plane[i].field_0);
				plane[i].field_C = in.readFloat();
			}
		} else {
			for (int i = 0; i < 2; i++) {
				cylinder[i] = new SplitKernelCylinder();
				in.readLEFloats(cylinder[i].origin);
				in.readLEFloats(cylinder[i].direction);
				cylinder[i].radius = in.readFloat();
			}
		}
	}
	
	@Override public void write(StreamWriter out) throws IOException {
		out.writeInt(type);
		if (type == PLANE || type == SPHERE) {
			for (int i = 0; i < 2; i++) {
				if (plane[i] == null) plane[i] = new SplitKernelPlane();
				out.writeLEFloats(plane[i].field_0);
				out.writeFloat(plane[i].field_C);
			}
		} else {
			for (int i = 0; i < 2; i++) {
				if (cylinder[i] == null) cylinder[i] = new SplitKernelCylinder();
				out.writeLEFloats(cylinder[i].origin);
				out.writeLEFloats(cylinder[i].direction);
				out.writeFloat(cylinder[i].radius);
			}
		}
	}
	
	
	protected static class Parser extends ArgScriptBlock<EffectUnit> {
		protected SplitterResource resource;
		
		@Override
		public void parse(ArgScriptLine line) {
			resource = new SplitterResource(data.getEffectDirectory(), FACTORY.getMaxVersion());
			
			ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 1)) {
				resource.name = args.get(0);
				if (getData().hasResource(resource.name)) {
					stream.addError(line.createErrorForArgument("A resource with this name already exists in this file.", 0));
				}
			}
			
			data.setPosition(resource, stream.getLinePositions().get(stream.getCurrentLine()));
			
			stream.startBlock(this);
		}

		@Override
		public void onBlockEnd() {
			data.addResource(resource.name, resource);
		}
		
		@Override
		public void setData(ArgScriptStream<EffectUnit> stream, EffectUnit data) {
			super.setData(stream, data);
			
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("type", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					if (resource.type != -1) {
						stream.addError(line.createError("Type has already been specified."));
					}
					else {
						resource.type = ENUM_TYPE.get(args, 0);
					}
				}
			}));
			
			this.addParser("offset", ArgScriptParser.create((parser, line) -> {
				if (resource.type == -1) {
					stream.addError(line.createError("Type has not been specified yet, use the command 'type'."));
					return;
				}
				else if (resource.type != PLANE) {
					stream.addError(line.createError("Only plane splitters support the 'offset' command."));
					return;
				}
				
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
					if (resource.plane[0] == null) resource.plane[0] = new SplitKernelPlane();
					if (resource.plane[1] == null) resource.plane[1] = new SplitKernelPlane();
					resource.plane[0].field_C = value.floatValue();
					
					if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
						resource.plane[1].field_C = value.floatValue();
					}
				}
			}));
			
			this.addParser("radius", ArgScriptParser.create((parser, line) -> {
				if (resource.type == -1) {
					stream.addError(line.createError("Type has not been specified yet, use the command 'type'."));
					return;
				}
				else if (resource.type != SPHERE && resource.type != CYLINDER) {
					stream.addError(line.createError("Only sphere and cylinder splitters support the 'radius' command."));
					return;
				}
				
				Number value = null;
				
				if (resource.type == SPHERE) {
					if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
						if (resource.plane[0] == null) resource.plane[0] = new SplitKernelPlane();
						if (resource.plane[1] == null) resource.plane[1] = new SplitKernelPlane();
						resource.plane[0].field_C = value.floatValue();
						
						if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
							resource.plane[1].field_C = value.floatValue();
						}
					}
				} 
				else if (resource.type == CYLINDER) {
					if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
						if (resource.cylinder[0] == null) resource.cylinder[0] = new SplitKernelCylinder();
						if (resource.cylinder[1] == null) resource.cylinder[1] = new SplitKernelCylinder();
						resource.cylinder[0].radius = value.floatValue();
						
						if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
							resource.cylinder[1].radius = value.floatValue();
						}
					}
				}
			}));
			
			this.addParser("normal", ArgScriptParser.create((parser, line) -> {
				if (resource.type == -1) {
					stream.addError(line.createError("Type has not been specified yet, use the command 'type'."));
				}
				else if (resource.type != PLANE) {
					stream.addError(line.createError("Only plane splitters support the 'normal' command."));
				}
				else {
					if (line.getArguments(args, 1)) {
						if (resource.plane[0] == null) resource.plane[0] = new SplitKernelPlane();
						if (resource.plane[1] == null) resource.plane[1] = new SplitKernelPlane();
						stream.parseVector3(args, 0, resource.plane[0].field_0);
						
						if (line.getOptionArguments(args, "vary", 1)) {
							stream.parseVector3(args, 0, resource.plane[1].field_0);
						}
					}
				}
			}));
			
			this.addParser("direction", ArgScriptParser.create((parser, line) -> {
				if (resource.type == -1) {
					stream.addError(line.createError("Type has not been specified yet, use the command 'type'."));
				}
				else if (resource.type != CYLINDER) {
					stream.addError(line.createError("Only cylinder splitters support the 'direction' command."));
				}
				else {
					if (line.getArguments(args, 1)) {
						if (resource.cylinder[0] == null) resource.cylinder[0] = new SplitKernelCylinder();
						if (resource.cylinder[1] == null) resource.cylinder[1] = new SplitKernelCylinder();
						stream.parseVector3(args, 0, resource.cylinder[0].direction);
						
						if (line.getOptionArguments(args, "vary", 1)) {
							stream.parseVector3(args, 0, resource.cylinder[1].direction);
						}
					}
				}
			}));
			
			this.addParser("origin", ArgScriptParser.create((parser, line) -> {
				if (resource.type == -1) {
					stream.addError(line.createError("Type has not been specified yet, use the command 'type'."));
				}
				else if (resource.type != SPHERE && resource.type != CYLINDER) {
					stream.addError(line.createError("Only sphere and cylinder splitters support the 'origin' command."));
				}
				else if (resource.type == SPHERE) {
					if (line.getArguments(args, 1)) {
						if (resource.plane[0] == null) resource.plane[0] = new SplitKernelPlane();
						if (resource.plane[1] == null) resource.plane[1] = new SplitKernelPlane();
						stream.parseVector3(args, 0, resource.plane[0].field_0);
						
						if (line.getOptionArguments(args, "vary", 1)) {
							stream.parseVector3(args, 0, resource.plane[1].field_0);
						}
					}
				}
				else if (resource.type == CYLINDER) {
					if (line.getArguments(args, 1)) {
						if (resource.cylinder[0] == null) resource.cylinder[0] = new SplitKernelCylinder();
						if (resource.cylinder[1] == null) resource.cylinder[1] = new SplitKernelCylinder();
						stream.parseVector3(args, 0, resource.cylinder[0].origin);
						
						if (line.getOptionArguments(args, "vary", 1)) {
							stream.parseVector3(args, 0, resource.cylinder[1].origin);
						}
					}
				}
			}));
		}
	}
	
	
	public static class Factory implements EffectResourceFactory {
		
		@Override
		public int getTypeCode() {
			return TYPE_CODE;
		}

		@Override
		public void addParser(ArgScriptStream<EffectUnit> stream) {
			stream.addParser(KEYWORD, new Parser());
		}

		@Override
		public int getMinVersion() {
			return 0;
		}

		@Override
		public int getMaxVersion() {
			return 0;
		}

		@Override
		public EffectResource create(EffectDirectory effectDirectory, int version) {
			return new SplitterResource(effectDirectory, version);
		}
		
		@Override public String getKeyword() {
			return KEYWORD;
		}
	}
	
	@Override
	public EffectResourceFactory getFactory() {
		return FACTORY;
	}

	@Override
	public void toArgScript(ArgScriptWriter writer) {
		writer.command(KEYWORD).arguments(getName()).startBlock();
		
		writer.command("type").arguments(ENUM_TYPE.get(type));
		
		if (type == PLANE) {
			writer.command("normal").vector(plane[0].field_0);
			if (plane[1].field_0[0] != 0 || plane[1].field_0[1] != 0 || plane[1].field_0[2] != 0) {
				writer.option("vary").vector(plane[1].field_0);
			}
			
			writer.command("offset").floats(plane[0].field_C);
			if (plane[1].field_C != 0) {
				writer.option("vary").floats(plane[1].field_C);
			}
		} else if (type == SPHERE) {
			writer.command("origin").vector(plane[0].field_0);
			if (plane[1].field_0[0] != 0 || plane[1].field_0[1] != 0 || plane[1].field_0[2] != 0) {
				writer.option("vary").vector(plane[1].field_0);
			}
			
			writer.command("radius").floats(plane[0].field_C);
			if (plane[1].field_C != 0) {
				writer.option("vary").floats(plane[1].field_C);
			}
		} else if (type == CYLINDER) {
			writer.command("origin").vector(cylinder[0].origin);
			if (cylinder[1].origin[0] != 0 || cylinder[1].origin[1] != 0 || cylinder[1].origin[2] != 0) {
				writer.option("vary").vector(cylinder[1].origin);
			}
			
			writer.command("direction").vector(cylinder[0].direction);
			if (cylinder[1].direction[0] != 0 || cylinder[1].direction[1] != 0 || cylinder[1].direction[2] != 0) {
				writer.option("vary").vector(cylinder[1].direction);
			}
			
			writer.command("radius").floats(cylinder[0].radius);
			if (cylinder[1].radius != 0) {
				writer.option("vary").floats(cylinder[1].radius);
			}
		}
		
		writer.endBlock().commandEND();
	}
}
