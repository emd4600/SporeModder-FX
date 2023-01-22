package sporemodder.extras.spuieditor.components;

import java.awt.Rectangle;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.components.SPUIWinProc.SPUIDefaultWinProc;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUINumberSections.SectionFloat;

public class ProportionalLayout extends SPUIDefaultWinProc {
	
	public static final int TYPE = 0xAF3DF411;
	
	// we must be able to cast it to Object[]
	private final Float[] params = new Float[] {0f, 0f, 0f, 0f};

	public ProportionalLayout(SPUIBlock block) throws InvalidBlockException {
		super(block);

		float[] params = SectionFloat.getValues(block.getSection(0x0F3D0000, SectionFloat.class), new float[] {0, 0, 0, 0}, 4);
		this.params[0] = params[0];
		this.params[1] = params[1];
		this.params[2] = params[2];
		this.params[3] = params[3];
	}
	
	public ProportionalLayout(SPUIViewer viewer) {
		super(viewer);
	}
	
	public ProportionalLayout(SPUIViewer viewer, float left, float top, float right, float bottom) {
		super(viewer);
		params[LEFT] = left;
		params[TOP] = top;
		params[RIGHT] = right;
		params[BOTTOM] = bottom;
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
		
		float[] array = new float[params.length];
		for (int i = 0; i < array.length; i++) {
			array[i] = params[i];
		}
				
		builder.addFloat(block, 0x0F3D0000, array);
		
		return block;
	}
	
	private ProportionalLayout() {
		super();
	}
	
	@Override
	public ProportionalLayout copyComponent(boolean propagateIndependent) {
		ProportionalLayout other = new ProportionalLayout();
		super.copyComponent(other, propagateIndependent);
		other.params[0] = params[0];
		other.params[1] = params[1];
		other.params[2] = params[2];
		other.params[3] = params[3];
		return other;
	}
	

	@Override
	public void modify(WinComponent component) {
		WinComponent parent = component.getParent();
		if (parent == null) {
			return;
		}
		
		Rectangle parentBounds = parent.getRealBounds();
		
		Rectangle result = component.getRealBounds();
		
		result.x += Math.round(parentBounds.width * params[LEFT]);
		result.y += Math.round(parentBounds.height * params[TOP]);
		result.width += Math.round(parentBounds.width * params[RIGHT] - parentBounds.width * params[LEFT]);
		result.height += Math.round(parentBounds.height * params[BOTTOM] - parentBounds.height * params[TOP]);
	}
	
	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new DefaultDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				
				case 0x0F3D0000: return params;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				case 0x0F3D0000: 
					params[index] = (float) value; break;
				}
				
				parent.revalidate();
				viewer.repaint();
				
				super.setValue(property, value, index);
			}
		};
	}
}
