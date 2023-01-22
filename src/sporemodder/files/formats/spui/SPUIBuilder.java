package sporemodder.files.formats.spui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.extras.spuieditor.components.SPUIComponent;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.LocalizedText;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOptionable;
import sporemodder.files.formats.spui.SPUIComplexSections.ListSectionContainer;
import sporemodder.files.formats.spui.SPUIComplexSections.SectionSectionList;
import sporemodder.files.formats.spui.SPUIComplexSections.SectionText;
import sporemodder.files.formats.spui.SPUINumberSections.SectionBoolean;
import sporemodder.files.formats.spui.SPUINumberSections.SectionByte;
import sporemodder.files.formats.spui.SPUINumberSections.SectionByte2;
import sporemodder.files.formats.spui.SPUINumberSections.SectionFloat;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt2;
import sporemodder.files.formats.spui.SPUINumberSections.SectionIntName;
import sporemodder.files.formats.spui.SPUINumberSections.SectionShort;
import sporemodder.files.formats.spui.SPUIVectorSections.SectionDimension;
import sporemodder.files.formats.spui.SPUIVectorSections.SectionVec2;
import sporemodder.files.formats.spui.SPUIVectorSections.SectionVec4;

public class SPUIBuilder {
	
	private SPUIMain spui;
	private final List<SPUIComponent> addedComponents = new ArrayList<SPUIComponent>();
	
	public SPUIBuilder() {
		spui = new SPUIMain();
	}
	
	public SPUIMain generateSPUI() {
		return spui;
	}
	
	public SPUIObject addObject(SPUIObject object) {
		if (object instanceof SPUIBlock) {
			addBlock((SPUIBlock) object);
			return object;
		}
		else {
			object.setParent(spui);
			spui.getResources().add((SPUIResource) object); 
			return object;
		}
	}
	
	public SPUIObject addComponent(SPUIComponent component) {
		if (component == null) {
			return null;
		}
		if (component.isUnique()) {
			for (SPUIComponent comp : addedComponents) {
				if (comp == component) {
					return component.getObject();
				}
			}
			SPUIObject obj = component.saveComponent(this);
			addObject(obj);
			addedComponents.add(component);
			return obj;
		}
		else {
			SPUIObject obj = component.saveComponent(this);
			addObject(obj);
			return obj;
		}
	}
	
	public void addBlock(SPUIBlock block) {
		block.setParent(spui);
		block.getResource().parent = spui;
		if (!spui.getBlocks().contains(block)) {
			spui.getBlocks().add(block);
			spui.getResources().add(block.getResource());
		}
	}
	
	public SPUIBlock createBlock(int type, boolean isRoot) {
		SPUIBlock block = new SPUIBlock(spui);
		SPUIStructResource res = new SPUIStructResource();
		res.parent = spui;
		res.hash = type;
		block.setResource(res);
		block.setIsRoot(isRoot);
		
//		spui.getBlocks().add(block);
//		spui.getResources().add(res);
		
		return block;
	}
	
	public void addInt(SPUISectionContainer block, int property, int[] values) {
		SectionInt sec = block.getSection(property, SectionInt.class);
		if (sec != null) {
			sec.count = values.length;
			sec.data = values;
		}
		else {
			sec = new SectionInt();
			sec.channel = property;
			sec.type = SectionInt.TYPE;
			sec.count = values.length;
			sec.data = values;
			block.getSections().add(sec);
		}
	}
	
	public void addIntName(SPUISectionContainer block, int property, String[] values) {
		SectionIntName sec = block.getSection(property, SectionIntName.class);
		if (sec != null) {
			sec.count = values.length;
			sec.data = values;
		}
		else {
			sec = new SectionIntName();
			sec.channel = property;
			sec.type = SectionIntName.TYPE;
			sec.count = values.length;
			sec.data = values;
			block.getSections().add(sec);
		}
	}
	
	public void addInt2(SPUISectionContainer block, int property, int[] values) {
		SectionInt2 sec = block.getSection(property, SectionInt2.class);
		if (sec != null) {
			sec.count = values.length;
			sec.data = values;
		}
		else {
			sec = new SectionInt2();
			sec.channel = property;
			sec.type = SectionInt2.TYPE;
			sec.count = values.length;
			sec.data = values;
			block.getSections().add(sec);
		}
	}
	public void addFloat(SPUISectionContainer block, int property, float[] values) {
		SectionFloat sec = block.getSection(property, SectionFloat.class);
		if (sec != null) {
			sec.count = values.length;
			sec.data = values;
		}
		else {
			sec = new SectionFloat();
			sec.channel = property;
			sec.type = SectionFloat.TYPE;
			sec.count = values.length;
			sec.data = values;
			block.getSections().add(sec);
		}
	}
	public void addByte(SPUISectionContainer block, int property, byte[] values) {
		SectionByte sec = block.getSection(property, SectionByte.class);
		if (sec != null) {
			sec.count = values.length;
			sec.data = values;
		}
		else {
			sec = new SectionByte();
			sec.channel = property;
			sec.type = SectionByte.TYPE;
			sec.count = values.length;
			sec.data = values;
			block.getSections().add(sec);
		}
	}
	public void addByte2(SPUISectionContainer block, int property, byte[] values) {
		SectionByte2 sec = block.getSection(property, SectionByte2.class);
		if (sec != null) {
			sec.count = values.length;
			sec.data = values;
		}
		else {
			sec = new SectionByte2();
			sec.channel = property;
			sec.type = SectionByte2.TYPE;
			sec.count = values.length;
			sec.data = values;
			block.getSections().add(sec);
		}
	}
	public void addBoolean(SPUISectionContainer block, int property, boolean[] values) {
		SectionBoolean sec = block.getSection(property, SectionBoolean.class);
		if (sec != null) {
			sec.count = values.length;
			sec.data = values;
		}
		else {
			sec = new SectionBoolean();
			sec.channel = property;
			sec.type = SectionBoolean.TYPE;
			sec.count = values.length;
			sec.data = values;
			block.getSections().add(sec);
		}
	}
	public void addVec2(SPUISectionContainer block, int property, float[][] values) {
		SectionVec2 sec = block.getSection(property, SectionVec2.class);
		if (sec != null) {
			sec.count = values.length;
			sec.data = values;
		}
		else {
			sec = new SectionVec2();
			sec.channel = property;
			sec.type = SectionVec2.TYPE;
			sec.count = values.length;
			sec.data = values;
			block.getSections().add(sec);
		}
	}
	public void addVec4(SPUISectionContainer block, int property, float[][] values) {
		SectionVec4 sec = block.getSection(property, SectionVec4.class);
		if (sec != null) {
			sec.count = values.length;
			sec.data = values;
		}
		else {
			sec = new SectionVec4();
			sec.channel = property;
			sec.type = SectionVec4.TYPE;
			sec.count = values.length;
			sec.data = values;
			block.getSections().add(sec);
		}
	}
	public void addDimension(SPUISectionContainer block, int property, int[][] values) {
		SectionDimension sec = block.getSection(property, SectionDimension.class);
		if (sec != null) {
			sec.count = values.length;
			sec.data = values;
		}
		else {
			sec = new SectionDimension();
			sec.channel = property;
			sec.type = SectionDimension.TYPE;
			sec.count = values.length;
			sec.data = values;
			block.getSections().add(sec);
		}
	}
	public void addText(SPUISectionContainer block, int property, LocalizedText[] values) {
		SectionText sec = block.getSection(property, SectionText.class);
		if (sec != null) {
			sec.count = values.length;
			sec.data = values;
		}
		else {
			sec = new SectionText();
			sec.channel = property;
			sec.type = SectionText.TYPE;
			sec.count = values.length;
			sec.data = values;
			block.getSections().add(sec);
		}
	}
	public void addSectionList(SPUISectionContainer block, int property, ListSectionContainer[] values, int listType) {
		SectionSectionList sec = block.getSection(property, SectionSectionList.class);
		if (sec != null) {
			sec.count = values.length;
			sec.unk = listType;
			sec.sections = values;
		}
		else {
			sec = new SectionSectionList();
			sec.channel = property;
			sec.type = SectionSectionList.TYPE;
			sec.count = values.length;
			sec.unk = listType;
			sec.sections = values;
			block.getSections().add(sec);
		}
	}
	public void addReference(SPUIBlock block, int property, SPUIObject[] values) {
		SPUISection originalSec = block.getSection(property);
		ReferenceSection sec = new ReferenceSection(block);
		sec.channel = property;
		sec.type = SectionShort.TYPE;
		sec.count = values.length;
		sec.data = values;
		
		if (originalSec == null) {
			block.getSections().add(sec);
		}
		else {
			block.getSections().set(block.getSections().indexOf(originalSec), sec);
		}
	}
	
	// placeholder section for 'short' sections
	private static class ReferenceSection extends SPUISection {
		
		private SPUIBlock parent;
		private SPUIObject[] data;
		
		@SuppressWarnings("unused")
		public static final int TYPE = SectionShort.TYPE;
		
		private ReferenceSection(SPUIBlock parent) {
			this.parent = parent;
		}
		
		@Override
		public void read(InputStreamAccessor in) throws IOException {
			short[] data = new short[count];
			in.readLEShorts(data);
			
			this.data = new SPUIObject[count];
			for (int i = 0; i < count; i++) {
				this.data[i] = parent.getParent().get(data[i]);
			}
		}

		@Override
		public void write(OutputStreamAccessor out) throws IOException {
			writeGeneral(out);
			for (int i = 0; i < count; i++) {
				out.writeLEShort(data[i] == null ? -1 : data[i].getBlockIndex());
			}
		}

		@Override
		public String getString() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void parse(String str) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void parse(ArgScriptOptionable as) throws ArgScriptException, IOException {
			// TODO Auto-generated method stub
			
		}

		private String createList() {
			StringBuilder b = new StringBuilder();
			
			if (data == null) {
	            b.append("null");
				return b.toString();
			}
	        int iMax = data.length - 1;
	        if (iMax == -1) {
	            b.append("[]");
	            return b.toString();
	        }

	        b.append('[');
	        for (int i = 0; ; i++) {
	            b.append(Integer.toString(data[i] == null ? -1 : data[i].getBlockIndex()));
	            if (i == iMax)
	                return b.append(']').toString();
	            b.append(", ");
	        }
		}
		
		@Override
		public ArgScriptOptionable toArgScript() {
			return new ArgScriptCommand(getChannelString(), SectionShort.TEXT_CODE, createList());
		}
		
	}
}
