package sporemodder.files.formats;

import javax.swing.tree.TreePath;

import sporemodder.utilities.Hasher;

public class ResourceKey {
	
	private int groupID = -1;
	private int instanceID = -1;
	private int typeID = -1;
	
	public ResourceKey() {
		
	}
	
	public ResourceKey(ResourceKey other) {
		copy(other);
	}
	
	
	public ResourceKey(int groupID, int instanceID, int typeID) {
		this.groupID = groupID;
		this.instanceID = instanceID;
		this.typeID = typeID;
	}
	
	public ResourceKey(String groupID, String instanceID, String typeID) {
		this.groupID = Hasher.getFileHash(groupID);
		this.instanceID = Hasher.getFileHash(instanceID);
		this.typeID = Hasher.getTypeHash(typeID);
	}
	
	public void copy(ResourceKey other) {
		groupID = other.groupID;
		instanceID = other.instanceID;
		typeID = other.typeID;
	}
	
	@Override
	public String toString() {
		return Hasher.getFileName(groupID) + "!" + Hasher.getFileName(instanceID) + "." + Hasher.getTypeName(typeID);
	}
	
	public int getGroupID() {
		return groupID;
	}
	public int getInstanceID() {
		return instanceID;
	}
	public int getTypeID() {
		return typeID;
	}
	
	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}
	public void setGroupID(String groupID) {
		this.groupID = Hasher.getFileHash(groupID);
	}
	
	public void setInstanceID(int instanceID) {
		this.instanceID = instanceID;
	}
	public void setInstanceID(String instanceID) {
		this.instanceID = Hasher.getFileHash(instanceID);
	}
	
	public void setTypeID(int typeID) {
		this.typeID = typeID;
	}
	public void setTypeID(String typeID) {
		this.typeID = Hasher.getTypeHash(typeID);
	}

	public void parse(String str) {
		String[] spl = str.split("\\.");
		String[] groupFile = spl[0].split("!");
		groupID = Hasher.getFileHash(groupFile[0]);
		instanceID = Hasher.getFileHash(groupFile[1]);
		typeID = Hasher.getTypeHash(spl[1]);
	}
	
	public void parseTreePath(TreePath path) {
		if (path.getPathCount() != 3) {
			throw new IllegalArgumentException("TreePath must be formed of 3 objects (root, group, instance)");
		}
		
		groupID = Hasher.getFileHash(path.getPathComponent(1).toString());
		
		String[] splits = path.getPathComponent(2).toString().split("\\.", 2);
		instanceID = Hasher.getFileHash(splits[0]);
		typeID = Hasher.getTypeHash(splits[1]);
	}
	
	
	public static String getStringFromTreePath(TreePath path) {
		if (path.getPathCount() != 3) {
			throw new IllegalArgumentException("TreePath must be formed of 3 objects (root, group, instance)");
		}
		
		return path.getPathComponent(1).toString() + "!" + path.getPathComponent(2);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + groupID;
		result = prime * result + instanceID;
		result = prime * result + typeID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceKey other = (ResourceKey) obj;
		if (groupID != other.groupID)
			return false;
		if (instanceID != other.instanceID)
			return false;
		if (typeID != other.typeID)
			return false;
		return true;
	}
	
	
}
