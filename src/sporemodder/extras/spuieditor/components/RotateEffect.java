package sporemodder.extras.spuieditor.components;

import java.awt.image.BufferedImage;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUINumberSections.SectionFloat;

public class RotateEffect extends UIEffect {
	
	public static final int TYPE = 0xCF2B2AD5;
	
	private float rotateAngle;
	//TODO the other three values are a vector, where the rotation is applied
	
	private BufferedImage componentBuffer;

	public RotateEffect(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		addUnassignedFloat(block, 0x4F2C0100, 0);
		addUnassignedFloat(block, 0x4F2C0101, 0);
		addUnassignedFloat(block, 0x4F2C0102, 1.0f);
		
		rotateAngle = SectionFloat.getValues(block.getSection(0x4F2C0103, SectionFloat.class), new float[] {0}, 1)[0];
	}
	
	public RotateEffect(SPUIViewer viewer) {
		super(viewer);
		
		rotateAngle = 0;
		
		unassignedProperties.put(0x4F2C0100, (float) 0);
		unassignedProperties.put(0x4F2C0101, (float) 0);
		unassignedProperties.put(0x4F2C0102, (float) 1.0f);
	}
	
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveFloat(builder, block, 0x4F2C0100);
		saveFloat(builder, block, 0x4F2C0101);
		saveFloat(builder, block, 0x4F2C0102);
		
		builder.addFloat(block, 0x4F2C0103, new float[] {rotateAngle});
		
		return block;
	}

	protected RotateEffect() {
		super();
	}
	
	@Override
	public RotateEffect copyComponent(boolean propagateIndependent) {
		RotateEffect eff = new RotateEffect();
		copyComponent(eff, propagateIndependent);
		eff.rotateAngle = rotateAngle;
		return eff;
	}
	
	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new DefaultDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				
				case 0x4F2C0103: return rotateAngle;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				
				case 0x4F2C0103: rotateAngle = (float) value; break;
				}
				
				viewer.repaint();
				
				super.setValue(property, value, index);
			}
		};
	}
	
	/* This has been commented because it has lots of problems. The object isn't rendered completely, Spore rotates around and axis and the rotation effect doesn't usually apply anyways */
//	
//	@Override
//	public Graphics2D modifyPreRender(WinComponent component, Graphics2D realGraphics, Rectangle drawBounds) {
//		// we must render the component in a different image
//		componentBuffer = new BufferedImage(drawBounds.width, drawBounds.height, BufferedImage.TYPE_INT_ARGB);
//		
//		drawBounds.x = 0;
//		drawBounds.y = 0;
//		return componentBuffer.createGraphics();
//	}
//
//	@Override
//	public void modifyPostRender(WinComponent component, Graphics2D realGraphics, Rectangle drawBounds) {
//		Rectangle realBounds = component.getRealBounds(); 
//		
////		realGraphics.drawImage(componentBuffer, realBounds.x, realBounds.y, null);
//		
//		AffineTransform at = new AffineTransform();
//
//	    // rotate 45 degrees around image center
//	    at.rotate(rotateAngle * Math.PI / 180.0, componentBuffer.getWidth() / 2.0, componentBuffer.getHeight() / 2.0);
//
//	    /*
//	     * translate to make sure the rotation doesn't cut off any image data
//	     */
////	    AffineTransform translationTransform;
////	    translationTransform = findTranslation(at, sourceBI);
////	    at.preConcatenate(translationTransform);
//
//	    // instantiate and apply affine transformation filter
//	    
//	    realGraphics.drawImage(componentBuffer, new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR), realBounds.x, realBounds.y);
//	}
//	
//	/*
//	   * find proper translations to keep rotated image correctly displayed
//	   */
//	  private AffineTransform findTranslation(AffineTransform at, BufferedImage bi) {
//	    Point2D p2din, p2dout;
//
//	    p2din = new Point2D.Double(0.0, 0.0);
//	    p2dout = at.transform(p2din, null);
//	    double ytrans = p2dout.getY();
//
//	    p2din = new Point2D.Double(0, bi.getHeight());
//	    p2dout = at.transform(p2din, null);
//	    double xtrans = p2dout.getX();
//
//	    AffineTransform tat = new AffineTransform();
//	    tat.translate(-xtrans, -ytrans);
//	    return tat;
//	  }
}
