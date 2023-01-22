package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.components.SPUIWinProc.SPUIDefaultWinProc;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.LocalizedText;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIComplexSections.SectionText;
import sporemodder.files.formats.spui.SPUIVectorSections.SectionVec2;

public class cSPUITooltipWinProc extends SPUIDefaultWinProc {
	
	//TODO show in preview?
	
	public static final int TYPE = 0x0372E920;
	
	private LocalizedText tooltipText;
	private final float[] offsetPosition = new float[] {0, 30};

	public cSPUITooltipWinProc(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		addUnassignedText(block, 0x03754D73, new LocalizedText("Tooltips"));
		addUnassignedIntName(block, 0x03754D74, null);
		addUnassignedIntName(block, 0x04EBD284, null);
		
		tooltipText = SectionText.getValues(block.getSection(0x03754D75, SectionText.class), new LocalizedText[] {null}, 1)[0];
		
		float[] values = SectionVec2.getValues(block.getSection(0x03754D76, SectionVec2.class), new float[][] {new float[] {0, 30}}, 1)[0];
		offsetPosition[0] = values[0];
		offsetPosition[1] = values[1];
		
		addUnassignedInt(block, 0x04DD1A0D, 0);
		
		addUnassignedText(block, 0x03754D53, null);  // not used usually
	}
	
	public cSPUITooltipWinProc(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x03754D73, new LocalizedText("Tooltips"));
		unassignedProperties.put(0x03754D74, null);
		unassignedProperties.put(0x04EBD284, null);
		unassignedProperties.put(0x04DD1A0D, (int) 0);
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
		
		saveText(builder, block, 0x03754D73);
		saveIntName(builder, block, 0x03754D74);
		saveIntName(builder, block, 0x04EBD284);
		
		if (tooltipText != null) {
			builder.addText(block, 0x03754D75, new LocalizedText[] {tooltipText});
		}
		
		builder.addVec2(block, 0x03754D76, new float[][] {offsetPosition});
		
		saveInt(builder, block, 0x04DD1A0D);
		if (unassignedProperties.containsKey(0x03754D53)) {
			saveText(builder, block, 0x03754D53);
		}
		
		return block;
	}

	private cSPUITooltipWinProc() {
		super();
	}


	@Override
	public cSPUITooltipWinProc copyComponent(boolean propagate) {
		cSPUITooltipWinProc other = new cSPUITooltipWinProc();
		copyComponent(other, propagate);
		
		other.tooltipText = new LocalizedText(tooltipText);
		System.arraycopy(offsetPosition, 0, other.offsetPosition, 0, offsetPosition.length);
		
		return other;
	}

	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new DefaultDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				
				case 0x03754D75: return tooltipText;
				case 0x03754D76: return offsetPosition;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				
				case 0x03754D75: 
					if (tooltipText == null) {
						tooltipText = new LocalizedText((LocalizedText) value);
					} else {
						tooltipText.copy((LocalizedText) value);
					}
					break;
					
				case 0x03754D76: 
					System.arraycopy((float[]) value, 0, offsetPosition, 0, offsetPosition.length); 
					break;
				}
					
				super.setValue(property, value, index);
			}
		};
	}
}
