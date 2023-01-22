package sporemodder.extras.spuieditor.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import sporemodder.extras.spuieditor.PanelUtils;
import sporemodder.extras.spuieditor.PanelUtils.BooleanValueAction;
import sporemodder.extras.spuieditor.PanelUtils.FloatValueAction;
import sporemodder.extras.spuieditor.PropertiesPanel;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerElement;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.extras.spuieditor.uidesigner.UIDesigner;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIComplexSections.ListSectionContainer;
import sporemodder.files.formats.spui.SPUIComplexSections.SectionSectionList;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt2;
import sporemodder.files.formats.spui.SPUISection;

public class FrameDrawable extends SPUIDefaultComponent implements SPUIDrawable {
	
	public static final int TYPE = 0x030D54B9;
	
	private static final int BORDER_SOLID_2 = 0;
	private static final int BORDER_SOLID = 2;
	private static final int BORDER_DOTTED = 3;
	private static final int BORDER_DASHED = 4;
	private static final int BORDER_INSET = 5;
	private static final int BORDER_OUTSET = 6;
	private static final int BORDER_GROOVE = 7;
	private static final int BORDER_RIDGE = 8;
	// same as inset and outset but width a black line
	private static final int BORDER_OUTSET_LINE = 9;
	private static final int BORDER_INSET_LINE = 10;
	
	private class BorderDrawable {
		int mode;
		Color color;
	}
	
	private final BorderDrawable[] borderStates = new BorderDrawable[8];
	private final boolean[] definedStates = new boolean[8]; 
	private final float[] borderSizes = new float[4];

	public FrameDrawable(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		ListSectionContainer[] values = null;
		
		values = SectionSectionList.getValues(block.getSection(0x030D0001, SectionSectionList.class), null, 8, -1);
		if (values != null) {
			for (int i = 0; i < borderStates.length; i++) {
				if (values[i] != null) {
					List<SPUISection> sections = values[i].getSections();
					
					if (!sections.isEmpty()) {
						borderStates[i] = new BorderDrawable();
						definedStates[i] = true;
						for (int j = 0; j < sections.size(); j++) {
							if (sections.get(j).getChannel() == 1) {
								borderStates[i].mode = ((SectionInt2) sections.get(j)).data[0];
							}
							else if (sections.get(j).getChannel() == 2) {
								borderStates[i].color = PanelUtils.decodeColor(SectionInt.getValues(sections.get(j), new int[] {0}, 1)[0]);
							}
						}
						
						continue;
					}
				}
				
				definedStates[i] = false;
			}
		}
		
		
		parseMarginsSections(block, 0x030D0000, borderSizes);
	}
	
	public FrameDrawable(SPUIViewer viewer) {
		super(viewer);
		
		borderSizes[0] = borderSizes[1] = borderSizes[2] = borderSizes[3] = 2.0f;
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
		
		ListSectionContainer[] stateSections = new ListSectionContainer[borderStates.length];
		for (int i = 0; i < stateSections.length; i++) {
			stateSections[i] = new ListSectionContainer();
			
			if (definedStates[i] && borderStates[i] != null) {
				
				builder.addInt2(stateSections[i], 0x00000001, new int[] {borderStates[i].mode});
				builder.addInt(stateSections[i], 0x00000002, new int[] {PanelUtils.encodeColor(borderStates[i].color)});
			}
		}
		builder.addSectionList(block, 0x030D0001, stateSections, 40);
		
		builder.addSectionList(block, 0x030D0000, new ListSectionContainer[] {saveMarginsSections(borderSizes)}, 50);
		
		return block;
	}
	
	private FrameDrawable() {
		super();
	}

	@Override
	public FrameDrawable copyComponent(boolean propagateIndependent) {
		FrameDrawable other = new FrameDrawable();
		super.copyComponent(other, propagateIndependent);
		
		for (int i = 0; i < borderSizes.length; i++) {
			other.borderSizes[i] = borderSizes[i];
		}
		for (int i = 0; i < borderStates.length; i++) {
			if (borderStates[i] != null) {
				other.borderStates[i] = new BorderDrawable();
				other.borderStates[i].color = borderStates[i].color;
				other.borderStates[i].mode = borderStates[i].mode;
			}
			other.definedStates[i] = definedStates[i];
		}
		
		return other;
	}
	
	protected BorderDrawable getBorderState(WinComponent component) {
		int index = -1;
		
		if (viewer.isPreview()) {
			int state = component.isActionableComponent() ? ((ActionableComponent) component).getState() : 0;
			
			if ((component.getFlags() & WinComponent.FLAG_ENABLED) != WinComponent.FLAG_ENABLED) {
				index = (state & ActionableComponent.STATE_SELECTED) == ActionableComponent.STATE_SELECTED ? 
						INDEX_SELECTED_DISABLED : 
							INDEX_DISABLED;
			}
			else if ((state & ActionableComponent.STATE_CLICK) == ActionableComponent.STATE_CLICK) {
				index = (state & ActionableComponent.STATE_SELECTED) == ActionableComponent.STATE_SELECTED ? 
						INDEX_SELECTED_CLICK : 
							INDEX_CLICKED;
			}
			else if ((state & ActionableComponent.STATE_HOVER) == ActionableComponent.STATE_HOVER) {
				index = (state & ActionableComponent.STATE_SELECTED) == ActionableComponent.STATE_SELECTED ? 
						INDEX_SELECTED_HOVER : 
							INDEX_HOVER;
			}
			else {
				index = (state & ActionableComponent.STATE_SELECTED) == ActionableComponent.STATE_SELECTED ? 
						INDEX_SELECTED : 
							INDEX_IDLE;
			}
		} else {
			index = INDEX_IDLE;
		}
		
		if (index == -1 || !definedStates[index]) {
			index = INDEX_IDLE;
		}
		
		return borderStates[index];
	}

	@Override
	public void draw(Graphics2D graphics, Rectangle bounds, WinComponent component) {
		
		BorderDrawable border = getBorderState(component);
		
		if (border == null) {
			return;
		}
		
		float[] finalSizes = new float[4];
		finalSizes[LEFT] = borderSizes[LEFT];
		finalSizes[TOP] = borderSizes[TOP];
		finalSizes[RIGHT] = borderSizes[RIGHT];
		finalSizes[BOTTOM] = borderSizes[BOTTOM];
		
		Rectangle drawBounds = new Rectangle(bounds);

		if (finalSizes[LEFT] + finalSizes[RIGHT] > bounds.width) {
			int newSize = (int) (bounds.width * 0.5f);
			
			finalSizes[LEFT] = finalSizes[LEFT] > newSize ? newSize : (int) finalSizes[LEFT];
			finalSizes[RIGHT] = bounds.width - finalSizes[LEFT];
		}
		
		if (finalSizes[TOP] + finalSizes[BOTTOM] > bounds.width) {
			int newSize = (int) (bounds.width * 0.5f);
			
			finalSizes[TOP] = finalSizes[TOP] > newSize ? newSize : (int) finalSizes[TOP];
			finalSizes[BOTTOM] = bounds.width - finalSizes[TOP];
		}
		
		if (border.mode == BORDER_INSET_LINE) {
			drawBounds.x += 1;
			drawBounds.y += 1;
			drawBounds.width -= 2;
			drawBounds.height -= 2;
		}
		
		Color tintColor = component.getTintColor();
		Color color = new Color(
				Math.round(border.color.getRed() * tintColor.getRed() / 255.0f),
				Math.round(border.color.getGreen() * tintColor.getGreen() / 255.0f),
				Math.round(border.color.getBlue() * tintColor.getBlue() / 255.0f),
				Math.round(border.color.getAlpha() * tintColor.getAlpha() / 255.0f));
		
		switch(border.mode) {
		case BORDER_SOLID_2:
		case BORDER_SOLID:
			drawSolidBorder(graphics, finalSizes, drawBounds, color);
			break;
			
		case BORDER_DOTTED:
			drawDashedBorder(graphics, finalSizes, drawBounds, color, 1.0f);
			break;
			
		case BORDER_DASHED:
			drawDashedBorder(graphics, finalSizes, drawBounds, color, 3.0f);
			break;
			
		case BORDER_GROOVE:
			drawBevelBorder(graphics, finalSizes, drawBounds, brighten(color, 0.6f), darken(color, 0.2f));
			break;
			
		case BORDER_RIDGE:
			drawBevelBorder(graphics, finalSizes, drawBounds, darken(color, 0.2f), brighten(color, 0.6f));
			break;
			
		case BORDER_INSET:
			drawGradientBorder(graphics, finalSizes, drawBounds, color, darken(color, 0.6f), brighten(color, 0.6f), color);
			break;
			
		case BORDER_OUTSET:
			drawGradientBorder(graphics, finalSizes, drawBounds, color, brighten(color, 0.6f), darken(color, 0.6f), color);
			break;
			
		case BORDER_INSET_LINE:
			drawGradientBorder(graphics, finalSizes, drawBounds, color, darken(color, 0.6f), brighten(color, 0.6f), color);
			graphics.setColor(Color.BLACK);
			graphics.setStroke(new BasicStroke());
			graphics.drawRect(
					drawBounds.x + (int)finalSizes[LEFT], drawBounds.y + (int)finalSizes[TOP], 
					drawBounds.width - (int)finalSizes[LEFT] - (int)finalSizes[RIGHT], drawBounds.height - (int)finalSizes[TOP] - (int)finalSizes[BOTTOM]);
			break;
			
		case BORDER_OUTSET_LINE:
			drawGradientBorder(graphics, finalSizes, drawBounds, color, brighten(color, 0.6f), darken(color, 0.6f), color);
			graphics.setColor(Color.BLACK);
			graphics.setStroke(new BasicStroke());
			graphics.drawRect(drawBounds.x, drawBounds.y, drawBounds.width, drawBounds.height);
			break;
		}
		
		graphics.setStroke(new BasicStroke());
	}
	
	private static void drawGradientBorder(Graphics2D graphics, float[] borderSizes, Rectangle drawBounds, Color firstGradient1, Color firstGradient2, Color secondGradient1, Color secondGradient2) {
		int leftWidth = Math.round(borderSizes[LEFT]);
		int topWidth = Math.round(borderSizes[TOP]);
		int bottomWidth = Math.round(borderSizes[BOTTOM]);
		int rightWidth = Math.round(borderSizes[RIGHT]);
		
		// Top
		graphics.setPaint(new GradientPaint(
				drawBounds.x + drawBounds.width/2, drawBounds.y, firstGradient1, 
				drawBounds.x + drawBounds.width/2, drawBounds.y + topWidth, firstGradient2));
		graphics.fill(createTrapezium(
				new Point(drawBounds.x, drawBounds.y),
				new Point(drawBounds.x + leftWidth, drawBounds.y + topWidth),
				new Point(drawBounds.x + drawBounds.width - rightWidth, drawBounds.y + topWidth),
				new Point(drawBounds.x + drawBounds.width, drawBounds.y)));
		
		// Left
		graphics.setPaint(new GradientPaint(
				drawBounds.x, drawBounds.y + drawBounds.height/2, firstGradient1, 
				drawBounds.x + leftWidth, drawBounds.y + drawBounds.height/2, firstGradient2));
		graphics.fill(createTrapezium(
				new Point(drawBounds.x, drawBounds.y),
				new Point(drawBounds.x + leftWidth, drawBounds.y + topWidth),
				new Point(drawBounds.x + leftWidth, drawBounds.y + drawBounds.height - bottomWidth),
				new Point(drawBounds.x, drawBounds.y + drawBounds.height)));
		
		// Bottom
		graphics.setPaint(new GradientPaint(
				drawBounds.x + drawBounds.width/2, drawBounds.y + drawBounds.height, secondGradient1, 
				drawBounds.x + drawBounds.width/2, drawBounds.y + drawBounds.height - bottomWidth, secondGradient2));
		graphics.fill(createTrapezium(
				new Point(drawBounds.x, drawBounds.y + drawBounds.height),
				new Point(drawBounds.x + leftWidth, drawBounds.y + drawBounds.height - bottomWidth),
				new Point(drawBounds.x + drawBounds.width - rightWidth, drawBounds.y + drawBounds.height - bottomWidth),
				new Point(drawBounds.x + drawBounds.width, drawBounds.y + drawBounds.height)));
		
		// Right
		graphics.setPaint(new GradientPaint(
				drawBounds.x + drawBounds.width, drawBounds.y + drawBounds.height/2, secondGradient1, 
				drawBounds.x + drawBounds.width - rightWidth, drawBounds.y + drawBounds.height/2, secondGradient2));
		graphics.fill(createTrapezium(
				new Point(drawBounds.x + drawBounds.width, drawBounds.y),
				new Point(drawBounds.x + drawBounds.width - rightWidth, drawBounds.y + topWidth),
				new Point(drawBounds.x + drawBounds.width - rightWidth, drawBounds.y + drawBounds.height - bottomWidth),
				new Point(drawBounds.x + drawBounds.width, drawBounds.y + drawBounds.height)));
	}
	
	private static void drawBevelBorder(Graphics2D graphics, float[] borderSizes, Rectangle drawBounds, Color firstColor, Color secondColor) {
		int leftWidth = Math.round(borderSizes[LEFT]);
		int topWidth = Math.round(borderSizes[TOP]);
		int bottomWidth = Math.round(borderSizes[BOTTOM]);
		int rightWidth = Math.round(borderSizes[RIGHT]);
		
		int leftHalf = (int) (borderSizes[LEFT] / 2.0f);
		int leftHalf2 = leftWidth - leftHalf;
		int rightHalf = (int) (borderSizes[RIGHT] / 2.0f);
		int rightHalf2 = rightWidth - rightHalf;
		int topHalf = (int) (borderSizes[TOP] / 2.0f);
		int bottomHalf = (int) (borderSizes[BOTTOM] / 2.0f);
		int bottomHalf2 = bottomWidth - bottomHalf;
		
		graphics.setColor(firstColor);
		
		// Bottom bottom
		graphics.fill(createTrapezium(
				new Point(drawBounds.x, drawBounds.y + drawBounds.height),
				new Point(drawBounds.x + leftHalf, drawBounds.y + drawBounds.height - bottomHalf2),
				new Point(drawBounds.x + drawBounds.width - leftHalf2, drawBounds.y + drawBounds.height - bottomHalf2),
				new Point(drawBounds.x + drawBounds.width, drawBounds.y + drawBounds.height)));
		
		// Right right
		graphics.fill(createTrapezium(
				new Point(drawBounds.x + drawBounds.width, drawBounds.y + drawBounds.height),
				new Point(drawBounds.x + drawBounds.width - rightHalf2, drawBounds.y + drawBounds.height - bottomHalf2),
				new Point(drawBounds.x + drawBounds.width - rightHalf2, drawBounds.y + topHalf),
				new Point(drawBounds.x + drawBounds.width, drawBounds.y)));
		
		// Top bottom 
		graphics.fill(createTrapezium(
				new Point(drawBounds.x + leftHalf, drawBounds.y + topHalf),
				new Point(drawBounds.x + leftWidth, drawBounds.y + topWidth),
				new Point(drawBounds.x + drawBounds.width - rightWidth, drawBounds.y + topWidth),
				new Point(drawBounds.x + drawBounds.width - rightHalf2, drawBounds.y + topHalf)));
		
		// Left right
		graphics.fill(createTrapezium(
				new Point(drawBounds.x + leftHalf, drawBounds.y + topHalf),
				new Point(drawBounds.x + leftWidth, drawBounds.y + topWidth),
				new Point(drawBounds.x + leftWidth, drawBounds.y + drawBounds.height - bottomWidth),
				new Point(drawBounds.x + leftHalf, drawBounds.y + drawBounds.height - bottomHalf2)));
		
		
		graphics.setColor(secondColor);
		
		// Bottom top
		graphics.fill(createTrapezium(
				new Point(drawBounds.x + leftHalf, drawBounds.y + drawBounds.height - bottomHalf2),
				new Point(drawBounds.x + leftWidth, drawBounds.y + drawBounds.height - bottomWidth),
				new Point(drawBounds.x + drawBounds.width - rightWidth, drawBounds.y + drawBounds.height - bottomWidth),
				new Point(drawBounds.x + drawBounds.width - rightHalf2, drawBounds.y + drawBounds.height - bottomHalf2)));
		
		// Right left
		graphics.fill(createTrapezium(
				new Point(drawBounds.x + drawBounds.width - rightHalf2, drawBounds.y + drawBounds.height - bottomHalf2),
				new Point(drawBounds.x + drawBounds.width - rightWidth, drawBounds.y + drawBounds.height - bottomWidth),
				new Point(drawBounds.x + drawBounds.width - rightWidth, drawBounds.y + topWidth),
				new Point(drawBounds.x + drawBounds.width - rightHalf2, drawBounds.y + topHalf)));
		
		// Left left
		graphics.fill(createTrapezium(
				new Point(drawBounds.x, drawBounds.y),
				new Point(drawBounds.x + leftHalf, drawBounds.y + topHalf),
				new Point(drawBounds.x + leftHalf, drawBounds.y + drawBounds.height - bottomHalf2),
				new Point(drawBounds.x, drawBounds.y + drawBounds.height)));
		
		// Top top
		graphics.fill(createTrapezium(
				new Point(drawBounds.x, drawBounds.y),
				new Point(drawBounds.x + leftHalf, drawBounds.y + topHalf),
				new Point(drawBounds.x + drawBounds.width - rightHalf2, drawBounds.y + topHalf),
				new Point(drawBounds.x + drawBounds.width, drawBounds.y)));
	}
	
	private static void drawSolidBorder(Graphics2D graphics, float[] finalSizes, Rectangle drawBounds, Color color) {
		graphics.setColor(color);
		
		if (finalSizes[TOP] != 0) {
			graphics.setStroke(new BasicStroke(finalSizes[TOP], BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
			graphics.drawLine(
					drawBounds.x, drawBounds.y + (int)(finalSizes[TOP]/2), 
					drawBounds.x + drawBounds.width, drawBounds.y + (int)(finalSizes[TOP]/2));
		}
		
		if (finalSizes[LEFT] != 0) {
			graphics.setStroke(new BasicStroke(finalSizes[LEFT], BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
			graphics.drawLine(
					drawBounds.x + (int)(finalSizes[LEFT]/2), drawBounds.y, 
					drawBounds.x + (int)(finalSizes[LEFT]/2), drawBounds.y + drawBounds.height);
		}
		
		if (finalSizes[RIGHT] != 0) {
			graphics.setStroke(new BasicStroke(finalSizes[RIGHT], BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
			graphics.drawLine(
					drawBounds.x + drawBounds.width - (int)(finalSizes[RIGHT]/2), drawBounds.y, 
					drawBounds.x + drawBounds.width - (int)(finalSizes[RIGHT]/2), drawBounds.y + drawBounds.height);
		}
		
		if (finalSizes[BOTTOM] != 0) {
			graphics.setStroke(new BasicStroke(finalSizes[BOTTOM], BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
			graphics.drawLine(
					drawBounds.x, drawBounds.y + drawBounds.height - (int)(finalSizes[BOTTOM]/2), 
					drawBounds.x + drawBounds.width, drawBounds.y + drawBounds.height - (int)(finalSizes[BOTTOM]/2));
		}
	}
	
	private static void drawDashedBorder(Graphics2D graphics, float[] finalSizes, Rectangle drawBounds, Color color, float dashFactor) {
		graphics.setColor(color);
		
		if (finalSizes[TOP] != 0) {
			graphics.setStroke(new BasicStroke(finalSizes[TOP], BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {finalSizes[TOP] * dashFactor, finalSizes[TOP]}, 0));
			graphics.drawLine(
					drawBounds.x, drawBounds.y + (int)(finalSizes[TOP]/2), 
					drawBounds.x + drawBounds.width, drawBounds.y + (int)(finalSizes[TOP]/2));
		}
		
		if (finalSizes[LEFT] != 0) {
			graphics.setStroke(new BasicStroke(finalSizes[LEFT], BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {finalSizes[LEFT] * dashFactor, finalSizes[LEFT]}, 0));
			graphics.drawLine(
					drawBounds.x + (int)(finalSizes[LEFT]/2), drawBounds.y, 
					drawBounds.x + (int)(finalSizes[LEFT]/2), drawBounds.y + drawBounds.height);
		}
		
		if (finalSizes[RIGHT] != 0) {
			graphics.setStroke(new BasicStroke(finalSizes[RIGHT], BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {finalSizes[RIGHT] * dashFactor, finalSizes[RIGHT]}, 0));
			graphics.drawLine(
					drawBounds.x + drawBounds.width - (int)(finalSizes[RIGHT]/2), drawBounds.y, 
					drawBounds.x + drawBounds.width - (int)(finalSizes[RIGHT]/2), drawBounds.y + drawBounds.height);
		}
		
		if (finalSizes[BOTTOM] != 0) {
			graphics.setStroke(new BasicStroke(finalSizes[BOTTOM], BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {finalSizes[BOTTOM] * dashFactor, finalSizes[BOTTOM]}, 0));
			graphics.drawLine(
					drawBounds.x, drawBounds.y + drawBounds.height - (int)(finalSizes[BOTTOM]/2), 
					drawBounds.x + drawBounds.width, drawBounds.y + drawBounds.height - (int)(finalSizes[BOTTOM]/2));
		}
	}
	

	@Override
	public PropertiesPanel getPropertiesPanel() {
		PropertiesPanel panel = new PropertiesPanel();
		setDefaultPropertiesPanel(panel);
		
		panel.addFloatValue("Left width", borderSizes[LEFT], 0.0f, null, 1.0f, new FloatValueAction() {
			@Override
			public void valueChanged(float value) {
				borderSizes[LEFT] = value;
				viewer.repaint();
			}
		}, viewer.getEditor());
		panel.addFloatValue("Top width", borderSizes[TOP], 0.0f, null, 1.0f, new FloatValueAction() {
			@Override
			public void valueChanged(float value) {
				borderSizes[TOP] = value;
				viewer.repaint();
			}
		}, viewer.getEditor());
		panel.addFloatValue("Right width", borderSizes[RIGHT], 0.0f, null, 1.0f, new FloatValueAction() {
			@Override
			public void valueChanged(float value) {
				borderSizes[RIGHT] = value;
				viewer.repaint();
			}
		}, viewer.getEditor());
		panel.addFloatValue("Bottom widht", borderSizes[BOTTOM], 0.0f, null, 1.0f, new FloatValueAction() {
			@Override
			public void valueChanged(float value) {
				borderSizes[BOTTOM] = value;
				viewer.repaint();
			}
		}, viewer.getEditor());
		
		DesignerClass struct = UIDesigner.Designer.getClass("FrameStyle");
		HashMap<Integer, String> names = UIDesigner.Designer.getClass("FrameDrawable").getProperty(0x030d0001).getArrayIndices();
		
		final PropertiesPanel[] panels = new PropertiesPanel[borderStates.length];
		
		for (int i = 0; i < borderStates.length; i++) {
			final int borderIndex = i;
			panels[borderIndex] = new PropertiesPanel(names.get(borderIndex));
			
			panel.addPanel(panels[borderIndex]);
			panel.addBooleanValue(names.get(borderIndex), definedStates[borderIndex], new BooleanValueAction() {
				@Override
				public void valueChanged(boolean isSelected) {
					definedStates[borderIndex] = isSelected;
					if (isSelected) {
						if (borderStates[borderIndex] == null) {
							borderStates[borderIndex] = new BorderDrawable();
							borderStates[borderIndex].color = Color.BLACK;
							borderStates[borderIndex].mode = BORDER_SOLID;
						}
//						else {
//							gbColor.setColor(borderStates[index].color);
//							cbMode.setSelectedIndex(getModeEnumIndex(borderStates[index]));
//						}
					}
					panels[borderIndex].setVisible(isSelected);
					
					if (borderIndex == 0) {
						viewer.repaint();
					}
				}
			}, viewer.getEditor());
			
			if (!definedStates[borderIndex]) {
				panels[borderIndex].setVisible(false);
			}
			
			struct.fillPropertiesPanel(panels[borderIndex], new DesignerClassDelegate() {

				@Override
				public boolean isValid(DesignerElement element) {
					return true;
				}

				@Override
				public void setValue(DesignerProperty property, Object value, int index) {
					switch(property.getProxyID()) {
					case 1: borderStates[borderIndex].mode = (int) value; break;
					case 2: borderStates[borderIndex].color = (Color) value; break;
					}
					
					if (borderIndex == 0) {
						viewer.repaint();
					}
				}

				@Override
				public Object getValue(DesignerProperty property) {
					switch(property.getProxyID()) {
					case 1: return borderStates[borderIndex] == null ? 0 : borderStates[borderIndex].mode;
					case 2: return borderStates[borderIndex] == null ? Color.BLACK : borderStates[borderIndex].color;
					}
					return null;
				}

				@Override
				public void propertyComponentAdded(DesignerProperty property, Object component) {
				}
			}, viewer.getEditor());
		}
		
		return panel;
	}
	
	private static GeneralPath createTrapezium(Point2D a, Point2D b, Point2D c, Point2D d) {
		GeneralPath path = new GeneralPath();
		
		path.moveTo(a.getX(), a.getY());
		path.lineTo(b.getX(), b.getY());
		path.lineTo(c.getX(), c.getY());
		path.lineTo(d.getX(), d.getY());
		path.lineTo(a.getX(), a.getY());
		
		path.closePath();
		
		return path;
	}
	
	private static Color brighten(Color color, float factor) {
		return new Color(
				Math.min(255, Math.round(color.getRed() + (255 - color.getRed()) * factor)),
				Math.min(255, Math.round(color.getGreen() + (255 - color.getGreen()) * factor)),
				Math.min(255, Math.round(color.getBlue() + (255 - color.getBlue()) * factor)));
	}
	
	private static Color darken(Color color, float factor) {
		return new Color(
				Math.max(0, Math.round(color.getRed() - color.getRed() * factor)),
				Math.max(0, Math.round(color.getGreen() - color.getGreen() * factor)),
				Math.max(0, Math.round(color.getBlue() - color.getBlue() * factor)));
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		final Color color = Color.BLUE;
		final float[] borderSizes = new float[] {15.0f, 15.0f, 15.0f, 15.0f};
		final Rectangle bounds = new Rectangle(50, 50, 64, 64);
		
		frame.add(new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				
				drawGradientBorder(g2d, borderSizes, bounds, color, darken(color, 0.6f), brighten(color, 0.6f), color);
//				drawGradientBorder(g2d, borderSizes, bounds, color, brighten(color, 0.6f), darken(color, 0.6f), color);
			}
		});
		
		frame.setSize(400, 400);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	@Override
	public Dimension getDimensions(int imageIndex) {
		return null;
	}
	
	@Override
	public boolean isValidPoint(Point p, Rectangle bounds) {
		return true;
	}
}
