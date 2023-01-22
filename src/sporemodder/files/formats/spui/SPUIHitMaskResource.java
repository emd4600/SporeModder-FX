package sporemodder.files.formats.spui;

import java.awt.Graphics;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JPanel;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.spui.SPUIObject.SPUIDefaultObject;
import sporemodder.utilities.Hasher;

public class SPUIHitMaskResource extends SPUIDefaultObject implements SPUIResource {
	
	private static final Pattern PATTERN = Pattern.compile("\\[unk1=(\\d+),\\s*unk2=(\\d+),\\s*unk3=\\[([0-9,\\-\\s]*)\\],\\s*file=([^\\]]*)\\]");
	
	// Only supported in version 3
	// most SPUIs use these
//	private boolean useRLEHitMask;
	private int width;
	private int height;
	private int[] rleHitMask;
	// if not useUnk
	private SPUIFileResource file; // ?
	
	public SPUIHitMaskResource() {
		
	}
	
	public SPUIHitMaskResource(SPUIHitMaskResource other) {
		other.width = width;
		other.height = height;
		
		if (rleHitMask != null) {
			other.rleHitMask = new int[rleHitMask.length];
			System.arraycopy(rleHitMask, 0, other.rleHitMask, 0, rleHitMask.length);
		}
		if (file != null) {
			other.file = new SPUIFileResource(file);
		}
	}
	
	@Override
	public String getTypeString() {
		return "HitMaskResource";
	}
	
	@Override
	public int getObjectType() {
		return Hasher.stringToFNVHash("ResourceType3");
	}
	
	@Override
	public void read(InputStreamAccessor in, int version) throws IOException {
		boolean useRLEHitMask = false;
		if (version >= 3) {
			useRLEHitMask = in.readBoolean();
			if (useRLEHitMask) {
				width = in.readLEInt();
				height = in.readLEInt();
				rleHitMask = new int[in.readLEInt()];
				in.readLEUShorts(rleHitMask);
			}
		}
		// if version < 3 or useUnk == false
		if (!useRLEHitMask) {
			file = new SPUIFileResource();
			file.read(in, version);
		}
	}
	
	@Override
	public void write(OutputStreamAccessor out, int version) throws IOException {
		if (version >= 3) {
			out.writeBoolean(file == null);
			if (file == null) {
				out.writeLEInt(width);
				out.writeLEInt(height);
				out.writeLEInt(rleHitMask.length);
				out.writeLEUShorts(rleHitMask);
			}
			else {
				file.write(out, version);
			}
		}
		else {
			if (file == null) {
				file = new SPUIFileResource();
			}
			file.write(out, version);
		}
	}
	
	@Override
	public String getString() {
		StringBuilder sb = new StringBuilder("ResourceType3 [unk1=" + width + ", unk2=" + height + ", unk3="
				+ Arrays.toString(rleHitMask) + ", file=");
		if (file == null) {
			sb.append("null");
		} else {
			sb.append("[");
			sb.append(file.getStringSimple());
			sb.append("]");
		}
		sb.append("]");
		return sb.toString();
	}
	
	@Override
	public ArgScriptCommand toCommand() {
		StringBuilder sb = new StringBuilder("[unk1=" + width + ", unk2=" + height + ", unk3="
				+ Arrays.toString(rleHitMask) + ", file=");
		if (file == null) {
			sb.append("null");
		} else {
			sb.append("[");
			sb.append(file.getStringSimple());
			sb.append("]");
		}
		sb.append("]");
		return new ArgScriptCommand("ResourceType3", sb.toString());
	}

	@Override
	public void parse(String str) throws IOException {
		// [unk1=x, unk2=y, unk3=[z, w, ....], file=null] 
		//									   file=[group!file.type]];
		
		// Remove all white spaces, since we don't need them here and it will make parsing easier
		String trimmed = str.trim().replaceAll("\\s", "");
		String contents = trimmed.substring(1, trimmed.length()-1);
		
		// split by comma, if it isn't followed by a digit (so contents inside unk3 are not split)
		String[] split = contents.split(",(?!\\d)");
		
		for (String s : split) 
		{
			String[] spl = s.split("=");
			if (spl.length != 2) {
				System.err.println("Error parsing ResourceType3");
				return;
			}
			String var = spl[0];
			String value = spl[1];
			
			if (var.equals("unk1")) {
				width = Integer.decode(value);
			}
			else if (var.equals("unk2")) {
				height = Integer.decode(value);
			}
			else if (var.equals("unk3")) {
				// [x,y,z,...]
				// First we remove the [], and then we split by comma
				String[] values = value.substring(1, value.length()-1).split(",");
				rleHitMask = new int[values.length];
				for (int i = 0; i < values.length; i++) {
					rleHitMask[i] = Integer.decode(values[i]);
				}
			}
			else if (var.equals("file")) {
				if (value.equals("null")) {
					file = null;
				}
				else {
					// [group!file.type]
					file = new SPUIFileResource();
					// We must remove the [];
					file.parseSimple(value.substring(1, value.length()-1));
				}
			}
		}
	}

	@Override
	public RESOURCE_TYPE getType() {
		return SPUIResource.RESOURCE_TYPE.TYPE3;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int[] getRLEHitMask() {
		return rleHitMask;
	}

	public void setRLEHitMask(int[] rleHitMask) {
		this.rleHitMask = rleHitMask;
	}

	public SPUIFileResource getFile() {
		return file;
	}

	public void setFile(SPUIFileResource file) {
		this.file = file;
	}

	@Override
	public void parse(ArgScriptCommand c) throws ArgScriptException, IOException {
		String str = c.getSingleArgument();
		Matcher matcher = PATTERN.matcher(str);
		matcher.find();
		
		//TODO improve this?
		
		width = (int) (Long.decode(matcher.group(1)) & 0xFFFFFFFF);
		height = (int) (Long.decode(matcher.group(2)) & 0xFFFFFFFF);
		
		String[] splits = matcher.group(3).split(",\\s*");
		// for some reason when the string is empty it still returns an split
		if (splits.length != 1 || splits[0].length() != 0) {
			rleHitMask = new int[splits.length];
			for (int i = 0; i < splits.length; i++) {
				if (splits[i].length() > 0) {
					rleHitMask[i] = Integer.decode(splits[i]);
				}
			}
		} else {
			rleHitMask = new int[0];
		}
		
		String fileStr = matcher.group(4);
		if (fileStr.equals("null")) {
			file = null;
		}
		else {
			file = new SPUIFileResource();
			file.parseSimple(fileStr);
		}
	}
	
	@Override
	public int getBlockIndex() {
		if (parent == null || parent.getResources() == null) {
			return -1;
		}
		return parent.getResources().indexOf(this);
	}
	
	
	public static void main(String[] args) {
		
		final int width = 26;
		int height = 26;
		
		final int[] indices = new int[] {
			87, 93, 111, 121, 136, 148, 161, 175, 186, 202, 212, 228, 237, 255, 263, 
			281, 289, 307, 315, 333, 341, 359, 367, 385, 394, 411, 420, 436, 447, 
			462, 474, 487, 501, 511, 529, 536
		};
		
		System.out.println("width: " + width);
		System.out.println("height: " + height);
		System.out.println("length: " + indices.length);
		
		JFrame frame = new JFrame();
		frame.setSize(200, 200);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				
				for (int i = 0; i < indices.length; i+=2) {
					g.drawLine(
							indices[i] % width, 
							indices[i] / width, 
							indices[i + 1] % width, 
							indices[i] / width);
				}
			}
		};
		panel.setSize(width, height);
		
		frame.setContentPane(panel);
		
		frame.setVisible(true);
	}

}
