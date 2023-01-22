package sporemodder.files.formats.spui;

import java.io.IOException;

import sporemodder.HashManager;
import sporemodder.MainApp;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.utilities.Hasher;
import sporemodder.utilities.names.NameRegistry;
import sporemodder.files.formats.spui.SPUIObject.SPUIDefaultObject;

public class SPUIStructResource extends SPUIDefaultObject implements SPUIResource {
	// this resource probably points to the structure/class that will be used for a determined block
	
	protected int hash;
	
	@Override
	public String getTypeString() {
		return getHashString();
	}
	
	public int getObjectType() {
		return hash;
	}
	
	@Override
	public void read(InputStreamAccessor in, int version) throws IOException {
		hash = in.readLEInt();
	}

	@Override
	public void write(OutputStreamAccessor out, int version) throws IOException {
		out.writeLEInt(hash);
	}

	@Override
	public String getString() {
		return "StructResource " + Hasher.getName(hash, HashManager.get().getSpuiRegistry());
	}
	
	@Override
	public void parse(String str) throws IOException {
		// hash
		hash = Hasher.getHash(str.trim(), HashManager.get().getSpuiRegistry());
	}

	@Override
	public RESOURCE_TYPE getType() {
		return SPUIResource.RESOURCE_TYPE.STRUCT;
	}

	public int getHash() {
		return hash;
	}
	
	public String getHashString() {
		return Hasher.getName(hash, HashManager.get().getSpuiRegistry());
	}

	public void setHash(int hash) {
		this.hash = hash;
	}

	@Override
	public void parse(ArgScriptCommand c) throws ArgScriptException,
			IOException {
		
		Hasher.getHash(c.getLastArgument(), HashManager.get().getSpuiRegistry());
	}

	@Override
	public ArgScriptCommand toCommand() {
		return new ArgScriptCommand("StructResource", Hasher.getName(hash, HashManager.get().getSpuiRegistry()));
	}
	
	@Override
	public int getBlockIndex() {
		if (parent == null || parent.getResources() == null) {
			return -1;
		}
		return parent.getResources().indexOf(this);
	}
}
