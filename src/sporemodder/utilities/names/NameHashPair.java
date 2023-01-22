package sporemodder.utilities.names;

import sporemodder.utilities.Hasher;

public class NameHashPair {
	private String name;
	private int hash;
	
	// it should only be used in this package
	NameHashPair(String name, int hash) {
		this.name = name;
		this.hash = hash;
	}

	public String getName() {
		return name;
	}

	public int getHash() {
		return hash;
	}
	
	@Override
	public String toString() {
		return name + ": 0x" + Hasher.fillZeroInHexString(hash);
	}
}
