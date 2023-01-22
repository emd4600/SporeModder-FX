package sporemodder.files.formats.spui;

import java.util.List;

public interface SPUISectionContainer {

	public List<SPUISection> getSections();
	
	public SPUISection getSection(int channel);
	
	public <T extends SPUISection> T getSection(int channel, Class<T> clazz);
}
