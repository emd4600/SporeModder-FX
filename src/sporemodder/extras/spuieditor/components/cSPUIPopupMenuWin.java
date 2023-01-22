package sporemodder.extras.spuieditor.components;

import java.io.IOException;
import java.util.List;

import sporemodder.extras.spuieditor.ComponentChooser;
import sporemodder.extras.spuieditor.RemoveComponentAction;
import sporemodder.extras.spuieditor.ResourceLoader;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUINumberSections.SectionShort;
import sporemodder.files.formats.spui.SPUIObject;
import sporemodder.userinterface.JLabelLink;

public class cSPUIPopupMenuWin extends WinButton {
	
	public static final int TYPE = 0x04C04B4D;
	
	private SPUIDrawable itemDrawable;
	private SPUIDrawable arrowUpDrawable;
	private SPUIDrawable arrowDownDrawable;

	public cSPUIPopupMenuWin(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		short index = SectionShort.getValues(block.getSection(0x04C17815, SectionShort.class), new short[] {-1}, 1)[0];
		if (index != -1) {
			itemDrawable = (SPUIDrawable) ResourceLoader.getComponent(block.getParent().get(index));
		}
		
		index = SectionShort.getValues(block.getSection(0x04C9A86D, SectionShort.class), new short[] {-1}, 1)[0];
		if (index != -1) {
			arrowUpDrawable = (SPUIDrawable) ResourceLoader.getComponent(block.getParent().get(index));
		}
		
		index = SectionShort.getValues(block.getSection(0x04C9A86E, SectionShort.class), new short[] {-1}, 1)[0];
		if (index != -1) {
			arrowDownDrawable = (SPUIDrawable) ResourceLoader.getComponent(block.getParent().get(index));
		}
	}
	
	public cSPUIPopupMenuWin(SPUIViewer viewer) {
		super(viewer);
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		builder.addReference(block, 0x04C17815, new SPUIObject[] {
				itemDrawable == null ? null : builder.addComponent(itemDrawable)
		});
		builder.addReference(block, 0x04C9A86D, new SPUIObject[] {
				arrowUpDrawable == null ? null : builder.addComponent(arrowUpDrawable)
		});
		builder.addReference(block, 0x04C9A86E, new SPUIObject[] {
				arrowDownDrawable == null ? null : builder.addComponent(arrowDownDrawable)
		});
		
		return block;
	}
	
	private cSPUIPopupMenuWin() {
		super();
	}
	
	@Override
	public cSPUIPopupMenuWin copyComponent(boolean propagateIndependent) {
		cSPUIPopupMenuWin other = new cSPUIPopupMenuWin();
		super.copyComponent(other, propagateIndependent);

		other.itemDrawable = itemDrawable.copyComponent(propagateIndependent);
		other.arrowUpDrawable = arrowUpDrawable.copyComponent(propagateIndependent);
		other.arrowDownDrawable = arrowDownDrawable.copyComponent(propagateIndependent);
		
		return other;
	}
	
	@Override
	public void restoreRemovedComponent(RemoveComponentAction removeAction) {
		List<String> modifiedValues = removeAction.getModifiedValues(this);
		
		for (String value : modifiedValues) {
			if (value.equals("itemDrawable")) {
				itemDrawable = (SPUIDrawable) removeAction.getRemovedComponent();
			}
			if (value.equals("arrowUpDrawable")) {
				arrowUpDrawable = (SPUIDrawable) removeAction.getRemovedComponent();
			}
			if (value.equals("arrowDownDrawable")) {
				arrowDownDrawable = (SPUIDrawable) removeAction.getRemovedComponent();
			}
		}
	}
	
	@Override
	public List<String> removeComponent(RemoveComponentAction removeAction, boolean propagate) {
		List<String> modifiedValues = super.removeComponent(removeAction, propagate);
		SPUIComponent removedComp = removeAction.getRemovedComponent();
		
		if (itemDrawable == removedComp) {
			modifiedValues.add("itemDrawable");
			itemDrawable = null;
		}
		if (arrowUpDrawable == removedComp) {
			modifiedValues.add("arrowUpDrawable");
			arrowUpDrawable = null;
		}
		if (arrowDownDrawable == removedComp) {
			modifiedValues.add("arrowDownDrawable");
			arrowDownDrawable = null;
		}
		
		removeAction.putModifiedComponent(this, modifiedValues);
		
		return modifiedValues;
	}
	
	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new ButtonDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				
				case 0x04C17815: return itemDrawable;
				case 0x04C9A86D: return arrowUpDrawable;
				case 0x04C9A86E: return arrowDownDrawable;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
					
				case 0x04C17815: 
					ComponentChooser.showChooserAction(cSPUIPopupMenuWin.this, "itemDrawable", 
							ComponentFactory.getComponentChooser(property.getType(), viewer),
							(JLabelLink) value, viewer, false);
					break;
					
				case 0x04C9A86D: 
					ComponentChooser.showChooserAction(cSPUIPopupMenuWin.this, "arrowUpDrawable", 
							ComponentFactory.getComponentChooser(property.getType(), viewer),
							(JLabelLink) value, viewer, false);
					break;
					
				case 0x04C9A86E: 
					ComponentChooser.showChooserAction(cSPUIPopupMenuWin.this, "arrowDownDrawable", 
							ComponentFactory.getComponentChooser(property.getType(), viewer),
							(JLabelLink) value, viewer, false);
					break;
				}
				
				viewer.repaint();
				
				super.setValue(property, value, index);
			}
		};
	}
	
	//TODO draw component
	

}
