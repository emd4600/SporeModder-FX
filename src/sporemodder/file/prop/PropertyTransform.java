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
package sporemodder.file.prop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptSpecialBlock;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.util.Matrix;
import sporemodder.util.Transform;

public class PropertyTransform extends BaseProperty {
	
	public static final int TYPE_CODE = 0x0038;
	public static final String KEYWORD = "transform";
	public static final int ARRAY_SIZE = 56;

	private Transform[] values;
	
	public PropertyTransform() {
		super(TYPE_CODE, 0);
	}
	
	public PropertyTransform(Transform value) {
		// Transform properties are only supported as arrays
		this(new Transform[] {value});
	}
	
	public PropertyTransform(Transform ... values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.length);
		this.values = values;
	}
	
	public PropertyTransform(List<Transform> values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.size());
		this.values = values.toArray(new Transform[values.size()]);
	}
	
	public Transform[] getValues() {
		return values;
	}
	
	@Override
	public void read(StreamReader stream, int itemCount) throws IOException {
		values = new Transform[itemCount];
		for (int i = 0; i < itemCount; i++) {
			Transform value = new Transform();
			value.readComplete(stream);
			values[i] = value;
		}
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		for (Transform value : values) {
			value.writeComplete(stream);
		}
	}
	
	public static void fastConvertXML(StreamWriter stream, Attributes attributes, String text) throws SAXException, IOException {
		final float[] offset = new float[3];
		float scale = 1.0f;
		final Matrix matrix = Matrix.getIdentity();
		
		final double[] euler = new double[3];  // alternative way to set rotation
		boolean bUsesEuler = false;
		
		int flags = 0x06000300;  // this means all the transformations are processed
		
		String value = null;  // for attributes;
		
		
		value = attributes.getValue("flags");
		if (value != null) flags = HashManager.get().int32(value);
		
		value = attributes.getValue("pos");
		if (value == null) value = attributes.getValue("offset");
		if (value != null) {
			String[] splits = value.split(",\\s*?");
			if (splits.length != 3) {
				throw new SAXException("PROP File: Expected 3 values in attribute 'offset' in property type 'transform'.");
			}
			
			
			offset[0] = Float.parseFloat(splits[0]);
			offset[1] = Float.parseFloat(splits[1]);
			offset[2] = Float.parseFloat(splits[2]);
		}
		
		value = attributes.getValue("scale");
		if (value != null) scale = Float.parseFloat(value);
		
		
		// euler angles
		value = attributes.getValue("euler");
		if (value != null) {
			String[] splits = value.split(",\\s*?");
			if (splits.length != 3) {
				throw new SAXException("PROP File: Expected 3 values in attribute 'euler' in property type 'transform'.");
			}
			
			euler[0] = Math.toRadians(Double.parseDouble(splits[0]));
			euler[1] = Math.toRadians(Double.parseDouble(splits[1]));
			euler[2] = Math.toRadians(Double.parseDouble(splits[2]));
			bUsesEuler = true;
		}
		
		value = attributes.getValue("rot_x");
		if (value == null) value = attributes.getValue("rotX");
		if (value != null) {
			euler[0] = Math.toRadians(Double.parseDouble(value));
			bUsesEuler = true;
		}
		
		value = attributes.getValue("rot_y");
		if (value == null) value = attributes.getValue("rotY");
		if (value != null) {
			euler[1] = Math.toRadians(Double.parseDouble(value));
			bUsesEuler = true;
		}
		
		value = attributes.getValue("rot_z");
		if (value == null) value = attributes.getValue("rotZ");
		if (value != null) {
			euler[2] = Math.toRadians(Double.parseDouble(value));
			bUsesEuler = true;
		}
		
		if (bUsesEuler) {
			matrix.rotate(euler[0], euler[1], euler[2]);
		}
		
		// raw matrix
		value = attributes.getValue("row1");
		if (value != null) {
			String[] splits = value.split(",\\s*?");
			if (splits.length != 3) {
				throw new SAXException("PROP File: Expected 3 values in attribute 'row1' in property type 'transform'.");
			}
			
			matrix.m[0][0] = Float.parseFloat(splits[0]);
			matrix.m[0][1] = Float.parseFloat(splits[1]);
			matrix.m[0][2] = Float.parseFloat(splits[2]);
		}
		
		value = attributes.getValue("row2");
		if (value != null) {
			String[] splits = value.split(",\\s*?");
			if (splits.length != 3) {
				throw new SAXException("PROP File: Expected 3 values in attribute 'row2' in property type 'transform'.");
			}
			
			matrix.m[1][0] = Float.parseFloat(splits[0]);
			matrix.m[1][1] = Float.parseFloat(splits[1]);
			matrix.m[1][2] = Float.parseFloat(splits[2]);
		}
		
		value = attributes.getValue("row3");
		if (value != null) {
			String[] splits = value.split(",\\s*?");
			if (splits.length != 3) {
				throw new SAXException("PROP File: Expected 3 values in attribute 'row3' in property type 'transform'.");
			}
			
			matrix.m[2][0] = Float.parseFloat(splits[0]);
			matrix.m[2][1] = Float.parseFloat(splits[1]);
			matrix.m[2][2] = Float.parseFloat(splits[2]);
		}
		

		stream.writeInt(flags);
		stream.writeLEFloats(offset);
		stream.writeLEFloat(scale);
		for (int i = 0; i < 3; i++) stream.writeLEFloat(matrix.m[i][0]);
		for (int i = 0; i < 3; i++) stream.writeLEFloat(matrix.m[i][1]);
		for (int i = 0; i < 3; i++) stream.writeLEFloat(matrix.m[i][2]);
	}
	
	@Override
	public void writeArgScript(String propertyName, ArgScriptWriter writer) {
		if (isArray) {
			writer.command(KEYWORD + "s").arguments(propertyName);
			writer.startBlock();
			for (Transform value : values) {
				writer.indentNewline();
				value.toArgScript(writer, true);
			}
			writer.endBlock();
			writer.commandEND();
		} 
		else {
			writer.command(KEYWORD).arguments(propertyName);
			values[0].toArgScript(writer, true);
		}
	}
	
	public static void addParser(ArgScriptStream<PropertyList> stream) {
		final ArgScriptArguments args = new ArgScriptArguments();
		
		stream.addParser(KEYWORD, ArgScriptParser.create((parser, line) -> {
			line.createError("Transform properties are only available in array format.");
		}));
		
		stream.addParser(KEYWORD + "s", new ArgScriptSpecialBlock<PropertyList>() {
			String propertyName;
			final ArrayList<Transform> values = new ArrayList<Transform>();
			final ArgScriptLine line = new ArgScriptLine(stream);
			
			@Override
			public void parse(ArgScriptLine line) {
				values.clear();
				stream.startSpecialBlock(this, "end");
				
				if (line.getArguments(args, 1)) {
					propertyName = args.get(0);
				}
			}
			
			@Override
			public boolean processLine(String text) {
				Transform dst = new Transform();
				line.fromLine(text, null);
				dst.parse(stream, line);
				values.add(dst);
				
				stream.addSyntax(line, false);
				
				return true;
			}
			
			@Override
			public void onBlockEnd() {
				stream.getData().add(propertyName, new PropertyTransform(values));
				stream.endSpecialBlock();
			}
		});
	}
}
