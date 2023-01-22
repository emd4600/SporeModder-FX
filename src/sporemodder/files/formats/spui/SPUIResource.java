package sporemodder.files.formats.spui;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;

public interface SPUIResource extends SPUIObject {
	
	public void parse(ArgScriptCommand c) throws ArgScriptException, IOException;
	public void parse(String str) throws IOException;
	public ArgScriptCommand toCommand();
	public String getString();
	public void read(InputStreamAccessor in, int version) throws IOException;
	public void write(OutputStreamAccessor out, int version) throws IOException;
	
	public static enum RESOURCE_TYPE {IMAGE, ATLAS, TYPE3, STRUCT};
	public RESOURCE_TYPE getType();
}
