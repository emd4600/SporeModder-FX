package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIComplexSections.SectionSectionList;

public class ImageCursorProvider extends SPUIDefaultComponent {
	
	public static final int TYPE = 0x02CDE37E;
	
	//TODO ??

	public ImageCursorProvider(SPUIBlock block) throws InvalidBlockException {
		super(block);

		addUnassigned(block, 0x02CD0000, null);
		int default1;
	}

	private ImageCursorProvider() {
		super();
	}
	
	public ImageCursorProvider(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x02CD0000, null);
	}

	@Override
	public ImageCursorProvider copyComponent(boolean propagateIndependent) {
		ImageCursorProvider other = new ImageCursorProvider();
		copyComponent(other, propagateIndependent);
		return other;
	}

	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
		
		if (unassignedProperties.get(0x02CD0000) != null) {
			SectionSectionList sectionList = (SectionSectionList) unassignedProperties.get(0x02CD0000);
			
			builder.addSectionList(block, 0x02CD0000, sectionList.sections, sectionList.unk);
		}
		
		return block;
	}
}
