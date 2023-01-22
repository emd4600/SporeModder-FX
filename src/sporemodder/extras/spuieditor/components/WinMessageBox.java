package sporemodder.extras.spuieditor.components;

import java.io.IOException;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIComplexSections.ListSectionContainer;

public class WinMessageBox extends WinDialog {
	
	public static final int TYPE = 0x6F38282B;
	
	private final float[] p_EEC2C003 = new float[4];
	private final float[] p_EEC2C007 = new float[4];

	public WinMessageBox(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		addUnassignedInt(block, 0xEEC2C000, 0);
		addUnassignedText(block, 0xEEC2C001, null);
		addUnassignedInt(block, 0xEEC2C002, 0);
		
		addUnassignedInt(block, 0xEEC2C00B, 0);
		addUnassignedFloat(block, 0xEEC2C00C, 0);
		addUnassignedFloat(block, 0xEEC2C00D, 0);
		addUnassignedFloat(block, 0xEEC2C00E, 0);
		addUnassignedInt(block, 0xEEC2C00F, 0);
		
		addUnassignedShort(block, 0xEEC2C010, null);
		addUnassignedFloat(block, 0xEEC2C011, 0);
		
		parseMarginsSections(block, 0xEEC2C003, p_EEC2C003);
		parseMarginsSections(block, 0xEEC2C007, p_EEC2C007);
	}
	
	public WinMessageBox(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0xEEC2C000, (int) 0);
		unassignedProperties.put(0xEEC2C001, null);
		unassignedProperties.put(0xEEC2C002, (int) 0);
		
		unassignedProperties.put(0xEEC2C00B, (int) 0);
		unassignedProperties.put(0xEEC2C00C, (float) 0);
		unassignedProperties.put(0xEEC2C00D, (float) 0);
		unassignedProperties.put(0xEEC2C00E, (float) 0);
		unassignedProperties.put(0xEEC2C00F, (int) 0);
		
		unassignedProperties.put(0xEEC2C010, null);
		unassignedProperties.put(0xEEC2C011, (float) 0);
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
		
		saveInt(builder, block, 0xEEC2C000);
		saveText(builder, block, 0xEEC2C001);
		saveInt(builder, block, 0xEEC2C002);
		
		builder.addSectionList(block, 0xEEC2C003, new ListSectionContainer[] {saveMarginsSections(p_EEC2C003)}, 50);
		
		builder.addSectionList(block, 0xEEC2C007, new ListSectionContainer[] {saveMarginsSections(p_EEC2C007)}, 50);
		
		saveInt(builder, block, 0xEEC2C00B);
		saveFloat(builder, block, 0xEEC2C00C);
		saveFloat(builder, block, 0xEEC2C00D);
		saveFloat(builder, block, 0xEEC2C00E);
		saveInt(builder, block, 0xEEC2C00F);
		
		saveReference(builder, block, 0xEEC2C010);
		saveFloat(builder, block, 0xEEC2C011);
		
		return block;
	}

	private WinMessageBox() {
		super();
	}

	@Override
	public WinMessageBox copyComponent(boolean propagateIndependent) {
		WinMessageBox other = new WinMessageBox();
		copyComponent(other, propagateIndependent);
		
		System.arraycopy(p_EEC2C003, 0, other.p_EEC2C003, 0, p_EEC2C003.length);
		System.arraycopy(p_EEC2C007, 0, other.p_EEC2C007, 0, p_EEC2C007.length);
		
		return other;
	}
	
	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new WindowDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				
				case 0xEEC2C003: return p_EEC2C003;
				case 0xEEC2C007: return p_EEC2C007;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				
				case 0xEEC2C003: System.arraycopy((float[]) value, 0, p_EEC2C003, 0, p_EEC2C003.length); break;
				case 0xEEC2C007: System.arraycopy((float[]) value, 0, p_EEC2C007, 0, p_EEC2C007.length); break;
				}
				
				viewer.repaint();
				
				super.setValue(property, value, index);
			}
		};
	}
}
