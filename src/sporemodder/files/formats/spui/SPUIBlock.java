package sporemodder.files.formats.spui;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.HashManager;
import sporemodder.MainApp;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOptionable;
import sporemodder.files.formats.spui.SPUIObject.SPUIDefaultObject;
import sporemodder.utilities.Hasher;
import sporemodder.utilities.names.Condition;
import sporemodder.util.NameRegistry;

public class SPUIBlock extends SPUIDefaultObject implements SPUISectionContainer {
	private static final int ROOT_FLAG = 0x8000;
	
	private SPUIStructResource resource;
	private boolean isRoot; // sectionCount & 0x8000 ?
	private final List<SPUISection> sections = new ArrayList<SPUISection>();
	
	public SPUIBlock(SPUIMain parent) {
		this.parent = parent;
	}
	
	@Override
	public String getTypeString() {
		return resource.getHashString();
	}
	
	@Override
	public int getObjectType() {
		return resource.getHash();
	}
	
	public void read(InputStreamAccessor in, int resourceIndex) throws IOException {
		int num = in.readLEUShort();
		
		isRoot = (num & ROOT_FLAG) == ROOT_FLAG;
		int sectionCount = num & ~ROOT_FLAG;
		
		resource = parent.getResources().get(resourceIndex, SPUIStructResource.class);
		
		for (int i = 0; i < sectionCount; i++) {
			sections.add(SPUISection.readSection(in));
		}
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeLEShort(0x5FF5); // magic
		out.writeLEShort(getBlockIndex());
		out.writeLEShort(sections.size() | (isRoot ? ROOT_FLAG : 0));
		for (SPUISection section : sections) {
			section.write(out);
		}
	}
	
	public void writeTxt(BufferedWriter out) throws IOException {
		out.write("block " + getBlockIndex() + (isRoot ? " unk=true " : " ") + resource.getString());
		out.newLine();
		// We set this to allow conditions in name registry
		Condition.setObject(((SPUIStructResource)resource).hash);
		for (SPUISection section : sections) {
			section.writeTxt(out, "\t");
		}
		out.write("end");
		out.newLine(); 
	}

//	public void print() {
//		System.out.println("resourceIndex: " + resourceIndex + (unknwon ? "\tunk: true" : ""));
//		for (SPUISection section : sections) {
//			section.print();
//		}
//	}
	
//	protected void updateIndex(List<SPUIResource> resources) {
//		resourceIndex = resources.indexOf(resource);
//	}

//	public void parse(List<String> lines) throws IOException {
//		
//		SPUIParser parser = new SPUIParser();
//		parser.e = 0;
//		// blockIndex resourceType resource
//		// blockIndex resourceIndex
//		// blockIndex unk=true resourceIndex
//		
//		String[] splits = lines.get(parser.e++).split(" ");
//		
//		int i = 1;
//		if (splits[i].startsWith("unk")) {
//			isRoot = Boolean.parseBoolean(splits[i++].split("=")[1]);
//		}
//		
//		if (splits[i].equals("StructResource")) {
//			SPUIStructResource res = new SPUIStructResource();
//			res.parse(splits[i+1]);
//			resourceIndex = parent.getResources().getNextStructIndex();
//			parent.getResources().add(res);
//			resource = res;
//		} 
//		
//		sections.clear();
//		int len = lines.size();
//		Condition.setObject(((SPUIStructResource)resource).hash);
//		while (parser.e < len) {
//			// to keep track of line number
//			SPUISection section = SPUISection.readSectionTxt(lines, parser);
//			if (section != null) {
//				sections.add(section);
//			}
//		}
//	}
	
	public void parse(ArgScriptBlock block) throws ArgScriptException, IOException {
		// there's an optional argument, the block index (which is just ignored for now, but might be used as a name in a future SPUI editor)
		// there's an optional argument, 'root' (formerly called 'unk=true')
		// there's an optional argument, 'StructResource'/'type', which is just ignored now (they are always StructResources)
		List<String> args = block.getArguments(1, 4);
		int count = args.size();
		
		for (int i = 0; i < count - 1; i++) {
			String arg = args.get(i);
			if (arg.equals("root") || arg.equals("unk=true")) {
				isRoot = true;
			}
		}
		
		resource = new SPUIStructResource();
		resource.parse(args.get(count - 1));
//		resourceIndex = parent.getResources().getNextStructIndex();
		parent.getResources().add(resource);
		
		Condition.setObject(resource.hash);
		
		List<ArgScriptOptionable> commands = block.getAllOptionables();
		for (ArgScriptOptionable c : commands) {
			sections.add(SPUISection.parseSection(c));
		}
		
	}
	
	public ArgScriptBlock toBlock() {
		ArgScriptBlock block = new ArgScriptBlock("block", Integer.toString(getBlockIndex()));
		if (isRoot) {
			block.addArgument("root");
		}
		block.addArgument("type");
		block.addArgument(resource.getHashString());
		
		Condition.setObject(((SPUIStructResource)resource).hash);
		
		for (SPUISection section : sections) {
			block.putOptionable(section.toArgScript());
		}
		
		return block;
	}

	public SPUIMain getParent() {
		return parent;
	}

	public void setParent(SPUIMain parent) {
		this.parent = parent;
	}

	public SPUIStructResource getResource() {
		return resource;
	}

	public void setResource(SPUIStructResource resource) {
		this.resource = resource;
//		if (this.resource == null) {
//			// we must add a new resource
//			if (parent == null) {
//				throw new UnsupportedOperationException("Need a parent in order to update resource in SPUIBlock.");
//			}
////			resourceIndex = parent.getResources().getNextStructIndex();
//			parent.getResources().add(resource);
//			this.resource = resource;
//		} else {
//			// just edit the existing one
//			this.resource.setHash(resource.getHash());
//		}
	}

	public boolean isRoot() {
		return isRoot;
	}

	public void setIsRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

	@Override
	public int getBlockIndex() {
		if (parent == null || parent.getResources() == null || resource == null) {
			return -1;
		}
		// this might return the same index for different objects??
		//return parent.getResources().indexOf(resource);
		
		return parent.getResources().getValidResourcesCount() + parent.getBlocks().indexOf(this);
	}

	@Override
	public List<SPUISection> getSections() {
		return sections;
	}
	
	@Override
	public SPUISection getSection(int channel) {
		for (SPUISection section : sections) {
			if (section.channel == channel) {
				return section;
			}
		}
		return null;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends SPUISection> T getSection(int channel, Class<T> clazz) {
		SPUISection sec = getSection(channel);
		if (sec == null) {
			return null;
		}
		if (!clazz.isInstance(sec)) {
			throw new IllegalArgumentException("No section with channel " + Hasher.getName(channel, HashManager.get().getSpuiRegistry()) +
					" of type " + clazz.getSimpleName() + " found.");
		}
		return (T) sec;
	}
}
