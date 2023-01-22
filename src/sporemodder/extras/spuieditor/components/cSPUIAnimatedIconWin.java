package sporemodder.extras.spuieditor.components;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
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
import sporemodder.files.formats.spui.SPUINumberSections.SectionBoolean;
import sporemodder.files.formats.spui.SPUINumberSections.SectionFloat;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt;
import sporemodder.files.formats.spui.SPUINumberSections.SectionShort;
import sporemodder.files.formats.spui.SPUIObject;
import sporemodder.userinterface.JLabelLink;

public class cSPUIAnimatedIconWin extends Window {
	
	public static final int TYPE = 0x0106F14B;
	
	private static final int ORDER_FORWARD = 0;
	private static final int ORDER_BACKWARDS = 1;
	
	private int tileCountRows;
	private int tileCountColumns;
	private int tileCount;
	private int animationOrder;
	private float animationLength;
	private boolean isAnimated;
	private Image image;
	
	private final AnimatedIconThread iconThread = new AnimatedIconThread();

	public cSPUIAnimatedIconWin(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		animationLength = SectionFloat.getValues(block.getSection(0xEEC1D000, SectionFloat.class), new float[] {0}, 1)[0];
		
		animationOrder = SectionInt.getValues(block, 0xEEC1D002, new int[] {0}, 1)[0];  // 218h
		
		addUnassignedInt(block, 0xEEC1D001, 0);  // 21Ch
		addUnassignedInt(block, 0xEEC1D004, 0);  // 220h
		addUnassignedInt(block, 0xEEC1D006, 0);  // 234h
		
		isAnimated = SectionBoolean.getValues(block.getSection(0xEEC1D005, SectionBoolean.class), new boolean[] {true}, 1)[0];  // 244h
		
		tileCount = SectionInt.getValues(block, 0xEEC1D007, new int[] {0}, 1)[0];  // 230h
		tileCountRows = SectionInt.getValues(block, 0xEEC1D009, new int[] {0}, 1)[0];  // 23Ch
		tileCountColumns = SectionInt.getValues(block, 0xEEC1D008, new int[] {0}, 1)[0];  // 240h
		
		short index = SectionShort.getValues(block.getSection(0xEEC1D003, SectionShort.class), new short[] {-1}, 1)[0];
		if (index != -1) {
			SPUIObject object = block.getParent().get(index);
			Image.loadImagePath(block, object);
			image = (Image) ResourceLoader.getComponent(object);
		}
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder)	{
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
		
		builder.addReference(block, 0xEEC1D003, new SPUIObject[] { image != null ? builder.addComponent(image) : null });
		
		builder.addFloat(block, 0xEEC1D000, new float[] { animationLength });
		builder.addInt(block, 0xEEC1D002, new int[] { animationOrder });
		saveInt(builder, block, 0xEEC1D001);
		saveInt(builder, block, 0xEEC1D004);
		
		builder.addBoolean(block, 0xEEC1D005, new boolean[] {isAnimated});
		saveInt(builder, block, 0xEEC1D006);
		builder.addInt(block, 0xEEC1D007, new int[] { tileCount });
		builder.addInt(block, 0xEEC1D008, new int[] { tileCountColumns });
		builder.addInt(block, 0xEEC1D009, new int[] { tileCountRows });
		
		if (image != null && !image.isAtlasImage()) {
			image.addImagePath(builder, block);
		}
				
		return block;
	}
	
	public cSPUIAnimatedIconWin(SPUIViewer viewer) {
		super(viewer);
		
		animationLength = 1.0f;
		animationOrder = ORDER_FORWARD;
		isAnimated = true;
		tileCount = 1;
		tileCountRows = 1;
		tileCountColumns = 1;
		
		unassignedProperties.put(0xEEC1D001, (int) 0);
		unassignedProperties.put(0xEEC1D004, (int) 0);
		unassignedProperties.put(0xEEC1D006, (int) 0);
	}

	private cSPUIAnimatedIconWin() {
		super();
	}
	
	@Override
	public cSPUIAnimatedIconWin copyComponent(boolean propagateIndependent) {
		cSPUIAnimatedIconWin other = new cSPUIAnimatedIconWin();
		copyComponent(other, propagateIndependent);

		other.image = image == null ? null : (propagateIndependent ? image.copyComponent(propagateIndependent) : image);
		other.animationLength = animationLength;
		other.animationOrder = animationOrder;
		other.isAnimated = isAnimated;
		other.tileCount = tileCount;
		other.tileCountColumns = tileCountColumns;
		other.tileCountRows = tileCountRows;
		
		return other;
	}

	@Override
	public boolean usesComponent(SPUIComponent component) {
		if (super.usesComponent(component) == true) {
			return true;
		}
		else {
			if (image == component) {
				return true;
			}
			return false;
		}
	}

	@Override
	public void getComponents(List<SPUIComponent> resultList, SPUIComponentFilter filter) {
		super.getComponents(resultList, filter);
		
		if (image != null) {
			image.getComponents(resultList, filter);
		}
	}
	
	@Override
	public void restoreRemovedComponent(RemoveComponentAction removeAction) {
		super.restoreRemovedComponent(removeAction);
		
		List<String> modifiedValues = removeAction.getModifiedValues(this);
		
		for (String value : modifiedValues) {
			if (value.equals("image")) {
				image = (Image) removeAction.getRemovedComponent();
			}
		}
	}
	
	@Override
	public List<String> removeComponent(RemoveComponentAction removeAction, boolean propagate) {
		List<String> modifiedValues = super.removeComponent(removeAction, propagate);
		
		SPUIComponent removedComp = removeAction.getRemovedComponent();
		
		if (propagate && image != null) {
			image.removeComponent(removeAction, propagate);
		}
		if (image == removedComp) {
			modifiedValues.add("image");
			image = null;
		}
		
		removeAction.putModifiedComponent(this, modifiedValues);
		
		return modifiedValues;
	}
	
	@Override
	protected void paintBasic(Graphics2D graphics, Rectangle drawBounds) {
		
		if (Image.isValid(image)) {
			
			int tileCountColumns = this.tileCountColumns;
			int tileCountRows = this.tileCountRows;
			
			if (tileCountColumns == 0) {
				tileCountColumns = 1;
			}
			if (tileCountRows == 0) {
				tileCountRows = 1;
			}
			
			int currentTile = 0;
			
			if (iconThread.isAlive() && isAnimated && tileCount > 1) {
				currentTile = iconThread.frameCount % tileCount;
			}
			
			if (animationOrder == ORDER_BACKWARDS) {
				currentTile = tileCount - currentTile - 1;
			}
			
//			int tileX = currentTile % tileCountRows;
//			int tileY = currentTile / tileCountRows;
			
			int tileX = currentTile % tileCountColumns;
			int tileY = currentTile / tileCountColumns;
			
			int[] uvCoords = image.getImageUVCoords();
			int uvWidth = uvCoords[2] - uvCoords[0];
			int uvHeight = uvCoords[3] - uvCoords[1];
			
//			uvCoords[0] += tileX * (uvWidth / (float) tileCountRows);
//			uvCoords[1] += tileY * (uvHeight / (float) tileCountColumns);
//			uvCoords[2] = (int) (uvCoords[0] + (uvWidth / (float) tileCountRows));
//			uvCoords[3] = (int) (uvCoords[1] + (uvHeight / (float) tileCountColumns));
			
			uvCoords[0] += tileX * (uvWidth / (float) tileCountColumns);
			uvCoords[1] += tileY * (uvHeight / (float) tileCountRows);
			uvCoords[2] = (int) (uvCoords[0] + (uvWidth / (float) tileCountColumns));
			uvCoords[3] = (int) (uvCoords[1] + (uvHeight / (float) tileCountRows));
			
			graphics.drawImage(
					Image.drawTintedImage(image.getBufferedImage(), new Dimension(drawBounds.width, drawBounds.height), uvCoords, tintColor),
					drawBounds.x, drawBounds.y, null);
		}
		
	}
	

	@Override
	public void setSPUIViewer(SPUIViewer viewer) {
		super.setSPUIViewer(viewer);
		if (image != null) {
			image.setSPUIViewer(viewer);
		}
		if (viewer == null) {
			iconThread.interrupt();
		}
		else {
			if (!iconThread.isAlive()) {
				iconThread.start();
			}
		}
	}
	
	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new WindowDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				
				case 0xEEC1D000: return animationLength;
				case 0xEEC1D002: return animationOrder;
				case 0xEEC1D005: return isAnimated;
				case 0xEEC1D007: return tileCount;
				case 0xEEC1D009: return tileCountRows;
				case 0xEEC1D008: return tileCountColumns;
				case 0xEEC1D003: return image;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				
				case 0xEEC1D000: animationLength = (float) value; break;
				case 0xEEC1D002: animationOrder = (int) value; break;
				case 0xEEC1D005: isAnimated = (boolean) value; break;
				case 0xEEC1D007: tileCount = (int) value; break;
				case 0xEEC1D008: tileCountRows = (int) value; break;
				case 0xEEC1D009: tileCountColumns = (int) value; break;
					
				case 0xEEC1D003: 
					ComponentChooser.showChooserAction(cSPUIAnimatedIconWin.this, "image", 
							Image.getImageChooser(viewer), 
							(JLabelLink) value, viewer, false);
					break;
				}
				
				viewer.repaint();
				
				super.setValue(property, value, index);
			}
		};
	}
	
	private boolean recursiveVisibleCheck() {
		WinComponent comp = this;
		
		while ((comp = comp.getParent()) != null && !comp.isLayoutWindow()) {
			if ((comp.getFlags() & WinComponent.FLAG_VISIBLE) != WinComponent.FLAG_VISIBLE) {
				return false;
			}
		}
		
		return true;
	}
	
	private class AnimatedIconThread extends Thread {
		private int frameCount = 0;
		
		Object monitor = new Object();
		
		@Override
		public void run() {
			while (!isInterrupted()) {
				if (SPUIViewer.RENDER_ANIMATED_ICONS && viewer != null && isAnimated && tileCount > 1 && image != null && 
						!shouldSkipPaint() && (viewer.getShowInvisibleComponents() || recursiveVisibleCheck())) {
					float frameTime = animationLength*1000 / tileCount;
					
					try {
						synchronized(monitor) {
							monitor.wait((long) frameTime);
						}
						frameCount++;
//						System.out.println("Updating animated icon " + cSPUIAnimatedIconWin.this + " with update time " + (animationLength*1000f / tileCount));
						viewer.repaint(realBounds);
						
					} catch (InterruptedException e) {
						return;
					}
					
				}
				else {
					try {
						synchronized(monitor) {
							monitor.wait(1000);
						}
					} catch (InterruptedException e) {
						return;
					}
				}
				
			}
		}
	}
	
	@Override
	protected boolean shouldUseFillColor() {
		return false;
	}
}
