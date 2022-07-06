package sporemodder.file.lvl;

import java.io.IOException;

import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

public abstract class GameplayMarkerData {
	
	public GameplayMarker marker;
	public int group;
	public int propertyCount;
	
	public GameplayMarkerData(GameplayMarker marker) {
		this.marker = marker;
	}
	
	public abstract void read(StreamReader stream) throws IOException;  
	
	public abstract void write(StreamWriter stream) throws IOException;
	
	public abstract void toArgScript(ArgScriptWriter writer);
	
	public abstract void addParsers(ArgScriptBlock<LevelDefinition> block, ArgScriptStream<LevelDefinition> stream);
}
