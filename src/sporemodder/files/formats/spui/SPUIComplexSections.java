package sporemodder.files.formats.spui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import sporemodder.HashManager;
import sporemodder.MainApp;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.LocalizedText;
import sporemodder.files.formats.argscript.ArgScript.ArgScriptType;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOptionable;
import sporemodder.utilities.Hasher;
import sporemodder.utilities.names.NameRegistry;

public class SPUIComplexSections {
	public static class ListSectionContainer implements SPUISectionContainer {
		private final List<SPUISection> sections = new ArrayList<SPUISection>();
		
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
	
	public static class SectionSectionList extends SPUISection {
		public static final int TYPE = 0x14;
		public static final String TEXT_CODE = "sections";
		
		public int unk;
		public ListSectionContainer[] sections;
		
		public static ListSectionContainer[] getValues(SectionSectionList section, ListSectionContainer[] defaultValues, int count, int type) throws InvalidBlockException {
			if (section != null) {
				if (type != -1 && section.unk != type) {
					throw new InvalidBlockException("Wrong 'sections' type, expected " + type + ".", section);
				}
				if (count != -1 && section.sections.length != count) {
					throw new InvalidBlockException("Wrong section length, expected " + count + " values.", section);
				}
				return section.sections;
			} else {
				return defaultValues;
			}
		}
		
		@Override
		public void read(InputStreamAccessor in) throws IOException {
			unk = in.readLEInt();
			sections = new ListSectionContainer[count];
			for (int i = 0; i < sections.length; i++) {
				
				int count = in.readLEShort();
				sections[i] = new ListSectionContainer();
				for (int j = 0; j < count; j++) {
					sections[i].sections.add(SPUISection.readSection(in));
				}
				
			}
		}
		
		@Override
		public void write(OutputStreamAccessor out) throws IOException {
			writeGeneral(out);
			out.writeLEInt(unk);
			for (ListSectionContainer sec : sections) {
				out.writeLEShort(sec.sections.size());
				for (SPUISection s : sec.sections) {
					s.write(out);
				}
			}
		}
		
		@Override
		public String getString() {
			String lineSeparator = System.getProperty("line.separator");
			StringBuilder sb = new StringBuilder();
			sb.append(TEXT_CODE);
			sb.append(" ");
			sb.append(unk);
			
			for (ListSectionContainer sec : sections) {
				sb.append(lineSeparator);
				sb.append("\t\tblock");
				for (SPUISection s : sec.sections) {
					sb.append(lineSeparator);
					sb.append(s.toString("\t\t\t"));
				}
				sb.append(lineSeparator);
			}
			
			sb.append(lineSeparator);
			sb.append("\tend");
			
			return sb.toString();
		}

		@Override
		public void parse(String str) throws IOException {
		}

		@Override
		public void parse(ArgScriptOptionable as) throws ArgScriptException, IOException {
			if (as.getType() != ArgScriptType.BLOCK) {
				throw new ArgScriptException("Wrong format in 'sections' property, blocks must be used.");
			}
			unk = Integer.parseInt(as.getLastArgument());
			
			ArgScriptBlock block = (ArgScriptBlock) as; 
			
			Collection<ArgScriptBlock> blocks = block.getAllBlocks();
			// old version, doesn't use blocks
			if (blocks.size() == 0)
			{
				sections = new ListSectionContainer[1];
				count = 1;
				Collection<ArgScriptCommand> commands = block.getAllCommands();
				sections[0] = new ListSectionContainer();
				for (ArgScriptCommand c : commands) {
					sections[0].sections.add(SPUISection.parseSection(c)); 
				}
			}
			else
			{
				sections = new ListSectionContainer[blocks.size()];
				count = sections.length;
				int j = 0;
				for (ArgScriptBlock b : blocks) {
					List<ArgScriptOptionable> commands = b.getAllOptionables();
					sections[j] = new ListSectionContainer();
					for (ArgScriptOptionable c : commands) {
						sections[j].sections.add(SPUISection.parseSection(c)); 
					}
					j++;
				}
			}
		}

		@Override
		public ArgScriptOptionable toArgScript() {
			ArgScriptBlock block = new ArgScriptBlock(getChannelString(), TEXT_CODE, Integer.toString(unk));
			
			for (ListSectionContainer secs : sections) {
				ArgScriptBlock b = new ArgScriptBlock("block");
				for (SPUISection s : secs.sections) {
					b.putOptionable(s.toArgScript());
				}
				block.putBlock(b);
			}
			
			return block;
		}
	}
	
	public static class SectionText extends SPUISection {
		public static final int TYPE = 0x12;
		public static final String TEXT_CODE = "text";
		
		public LocalizedText[] data;
		
		private static LocalizedText readLocalizedText(InputStreamAccessor in) throws IOException {
			int len = in.readLEShort();
			if (len == -1) {
				return new LocalizedText(in.readLEInt(), in.readLEInt());
			} else {
				return new LocalizedText(in.readLEString16(len));
			}
		}
		private static void writeLocalizedText(LocalizedText text, OutputStreamAccessor out) throws IOException {
			if (text.text != null && text.tableID == -1 && text.instanceID == -1) {
				out.writeLEShort(text.text.length());
				out.writeLEString16(text.text);
			} else {
				out.writeLEShort(-1);
				out.writeLEInt(text.tableID);
				out.writeLEInt(text.instanceID);
			}
		}
		private static LocalizedText parseLocalizedText(String str) throws IOException {
			// (tableID!instanceID)
			// "This is my text"
			
			if (str.startsWith("(")) {
				// (tableID!instanceID)
				String[] splits = str.substring(1, str.length()-1).split("!");
				return new LocalizedText(Hasher.getFileHash(splits[0]), Hasher.getFileHash(splits[1]));
			}
			else {
				if (str.equals("null")) {
					return new LocalizedText((String) null);
				}
				return new LocalizedText(str.isEmpty() ? "" : str.substring(1, str.length()-1));
			}
		}
		
		public static LocalizedText[] getValues(SectionText section, LocalizedText[] defaultValues, int count) throws InvalidBlockException {
			if (section != null) {
				if (section.data.length != count && count != -1) {
					throw new InvalidBlockException("Wrong section length, expected " + count + " values.", section);
				}
				return section.data;
			} else {
				return defaultValues;
			}
		}
		 
		public void read(InputStreamAccessor in) throws IOException {
			data = new LocalizedText[count];
			for (int i = 0; i < count; i++) {
				data[i] = readLocalizedText(in);
			}
		}
		
		@Override
		public void write(OutputStreamAccessor out) throws IOException {
			writeGeneral(out);
			for (LocalizedText text : data) {
				writeLocalizedText(text, out);
			}
		}
		
		@Override
		public String getString() {
			StringBuilder b = new StringBuilder();
			b.append(TEXT_CODE);
			b.append(" ");
			b.append(Arrays.toString(data));
			return b.toString();
		}

		@Override
		public void parse(String str) throws IOException {
			// [(tableID!instanceID), "This is my text"]
			// Remove the [] and split by comma, if preceded by ]. 
			// We don't remove spaces to keep the text, but there must be some better way to do this
			//TODO There's no support for , inside text using this
			String[] splits = str.substring(1, str.length()-1).split(",");
			
			data = new LocalizedText[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// we remove the unnecessary spaces here, after splitting by comma, so we don't remove text spaces too
				data[i] = parseLocalizedText(splits[i].trim());
			}
		}

		@Override
		public void parse(ArgScriptOptionable as) throws ArgScriptException, IOException {
			
			String str = as.getLastArgument();
			String[] splits = str.substring(1, str.length() - 1).split(",\\s+");
			
			data = new LocalizedText[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// we remove the unnecessary spaces here, after splitting by comma, so we don't remove text spaces too
				data[i] = parseLocalizedText(splits[i].trim());
			}
		}

		@Override
		public ArgScriptOptionable toArgScript() {
			ArgScriptCommand c = new ArgScriptCommand(getChannelString(), TEXT_CODE, SPUIParser.createList(data));
			
			return c;
		}
	}
	
//	public static void main(String[] args) throws IOException {
//		NameRegistry.read();
//		String str = "[(0x012353!creature), \"Test text\"]";
//		
//		SectionText sec = new SectionText();
//		sec.parse(str);
//		System.out.println(sec.getString());
//	}
}
