package sporemodder.files.formats.spui;

import java.io.IOException;
import java.util.List;

import javax.swing.tree.TreePath;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.ResourceKey;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.utilities.Hasher;
import sporemodder.files.formats.spui.SPUIObject.SPUIDefaultObject;

public class SPUIFileResource extends SPUIDefaultObject implements SPUIResource {
	
	public static final int TYPE = 0x10D1FBEB;  // Hasher.stringToFNVHash("FileResource");
	
	private final ResourceKey resourceKey = new ResourceKey();
	// is it the first or the second resource type? 
	protected boolean isAtlas;
	
	// to use when parsing .spui.spui_t
	private String realPath;
	
	public SPUIFileResource(ResourceKey key, boolean isAtlas) {
		this.isAtlas = isAtlas;
		if (key != null) {
			resourceKey.copy(key);
		}
	}

	public SPUIFileResource() {
		// TODO Auto-generated constructor stub
	}
	
	public SPUIFileResource(SPUIFileResource other) {
		this.resourceKey.copy(other.resourceKey);
		this.isAtlas = other.isAtlas;
		this.realPath = other.realPath;
	}
	
	public TreePath toTreePath(Object rootNode) {
		
		if (realPath == null) {
			return new TreePath(new Object[] {rootNode, 
					Hasher.getFileName(resourceKey.getGroupID()), Hasher.getFileName(resourceKey.getInstanceID()) + "." + Hasher.getTypeName(resourceKey.getTypeID())});
		}
		else {
			String[] splits = realPath.split("!", 2);
			return new TreePath(new Object[] {rootNode, splits[0], splits[1]});
		}
		
	}
	
	@Override
	public int getObjectType() {
		return TYPE;
	}
	
	@Override
	public String getTypeString() {
		return "FileResource";
	}

	@Override
	public void read(InputStreamAccessor in, int version) throws IOException {
		resourceKey.setInstanceID(in.readLEInt());
		resourceKey.setTypeID(in.readLEInt());
		resourceKey.setGroupID(in.readLEInt());
	}
	
	@Override
	public void write(OutputStreamAccessor out, int version) throws IOException {
		out.writeLEInt(resourceKey.getInstanceID());
		out.writeLEInt(resourceKey.getTypeID());
		out.writeLEInt(resourceKey.getGroupID());
	}
	
	@Override
	public String toString() {
		return realPath != null ? realPath : (Hasher.getFileName(resourceKey.getGroupID()) + "!" + Hasher.getFileName(resourceKey.getInstanceID()) + 
				"." + Hasher.getTypeName(resourceKey.getTypeID()));
	}
	
	@Override
	public String getString() {
		return "FileResource " + (isAtlas ? "atlas " : "") + (realPath != null ? realPath : Hasher.getFileName(resourceKey.getGroupID()) + "!" + Hasher.getFileName(resourceKey.getInstanceID()) + 
				"." + Hasher.getTypeName(resourceKey.getTypeID()));
	}
	
	// Gets string without resource data (resource name and isAtlas)
	public String getStringSimple() {
		return Hasher.getFileName(resourceKey.getGroupID()) + "!" + Hasher.getFileName(resourceKey.getInstanceID()) + 
				"." + Hasher.getTypeName(resourceKey.getTypeID());
	}
	
	// group!file.type
	protected void parseSimple(String str) throws IOException {
		String[] spl = str.split("\\.");
		String[] groupFile = spl[0].split("!");
		resourceKey.setGroupID(Hasher.getFileHash(groupFile[0]));
		resourceKey.setInstanceID(Hasher.getFileHash(groupFile[1]));
		resourceKey.setTypeID(Hasher.getTypeHash(spl[1]));
	}
	
	@Override
	public void parse(String str) throws IOException {
		// isAtlas group!file.type
		
		String[] splits = str.split(" ");
		
		int i = 0;
		// isAtlas
		if (splits.length >= 2) {
			i = 1;
			if (splits[0].equals("atlas")) {
				isAtlas = true;
			} else {
				System.err.println("Unknwon token \"" + splits[0] + "\"");
			}
		}
		
		parseSimple(splits[i]);
	}

	@Override
	public RESOURCE_TYPE getType() {
		if (isAtlas) return SPUIResource.RESOURCE_TYPE.ATLAS;
		else return SPUIResource.RESOURCE_TYPE.IMAGE;
	}

	public int getInstanceID() {
		return resourceKey.getInstanceID();
	}

	public void setFileID(int fileID) {
		resourceKey.setInstanceID(fileID);
	}

	public int getTypeID() {
		return resourceKey.getTypeID();
	}

	public void setTypeID(int typeID) {
		resourceKey.setTypeID(typeID);
	}

	public int getGroupID() {
		return resourceKey.getGroupID();
	}

	public void setGroupID(int groupID) {
		resourceKey.setGroupID(groupID);
	}
	
	public ResourceKey getResourceKey() {
		return resourceKey;
	}
	
	public void setResourceKey(ResourceKey key) {
		resourceKey.copy(key);
	}

	public boolean isAtlas() {
		return isAtlas;
	}

	public void setIsAtlas(boolean isAtlas) {
		this.isAtlas = isAtlas;
	}
	
	public String getRealPath() {
		return realPath;
	}
	
	public void setRealPath(String realPath) {
		this.realPath = realPath;
	}

//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + fileID;
//		result = prime * result + groupID;
//		result = prime * result + (isAtlas ? 1231 : 1237);
//		result = prime * result + typeID;
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		SPUIFileResource other = (SPUIFileResource) obj;
//		if (fileID != other.fileID)
//			return false;
//		if (groupID != other.groupID)
//			return false;
//		if (isAtlas != other.isAtlas)
//			return false;
//		if (typeID != other.typeID)
//			return false;
//		return true;
//	}

	@Override
	public void parse(ArgScriptCommand c) throws ArgScriptException {
		List<String> args = c.getArguments(1, 2);
		int index = 0;
		if (args.size() == 2) {
			if (args.get(index++).equals("atlas")) {
				isAtlas = true;
			}
		}
		
		realPath = args.get(index);
		resourceKey.parse(realPath);
	}

	@Override
	public ArgScriptCommand toCommand() {
		if (isAtlas) {
			return new ArgScriptCommand("FileResource", "atlas", realPath != null ? realPath : resourceKey.toString());
		} else {
			return new ArgScriptCommand("FileResource", realPath != null ? realPath : resourceKey.toString());
		}
	}

	@Override
	public int getBlockIndex() {
		if (parent == null || parent.getResources() == null) {
			return -1;
		}
		return parent.getResources().indexOf(this);
	}

}
