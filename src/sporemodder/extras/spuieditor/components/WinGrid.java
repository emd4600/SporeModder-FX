package sporemodder.extras.spuieditor.components;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sporemodder.extras.spuieditor.PanelUtils;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.LocalizedText;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIComplexSections.ListSectionContainer;
import sporemodder.files.formats.spui.SPUIComplexSections.SectionSectionList;
import sporemodder.files.formats.spui.SPUIComplexSections.SectionText;
import sporemodder.files.formats.spui.SPUINumberSections.SectionFloat;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt2;
import sporemodder.files.formats.spui.SPUISectionContainer;

public class WinGrid extends Window {
	
	private class ActionableRow implements ActionableComponent {

		private int rowIndex;
		
		public ActionableRow(int rowIndex) {
			this.rowIndex = rowIndex;
		}

		@Override
		public void setState(int state) {
			int oldState = rowStates.getOrDefault(rowIndex, 0);
			if (oldState != state) {
				
				if ((oldState & STATE_CLICK) != STATE_CLICK && (state & STATE_CLICK) == STATE_CLICK) {
					boolean isSelected = (oldState & STATE_SELECTED) == STATE_SELECTED;
					state &= ~STATE_SELECTED;
					if (!isSelected) {
						state |= STATE_SELECTED;
					}
				}
				
				rowStates.put(rowIndex, state);
				viewer.repaint();
			}
		}

		@Override
		public int getState() {
			return rowStates.getOrDefault(rowIndex, 0);
		}

		@Override
		public Rectangle getRealBounds() {
			
			Rectangle rect = new Rectangle();
			rect.x = realBounds.x;
			rect.y = Math.round(realBounds.y + rowIndex * (cellHeight + CELL_SEPARATION));
			rect.width = realBounds.width;
			rect.height = (int) (rect.y - realBounds.y + cellHeight > bounds.height ? bounds.height - (rect.y - realBounds.y) : cellHeight);
			
			return rect;
		}
	}
	
	public static final int TYPE = 0x6F1F1AA6;
	
	public static final int COLOR_IDLE_TEXT = 0;
	public static final int COLOR_IDLE_BACKGROUND = 1;
	public static final int COLOR_DISABLED_TEXT = 2;
	public static final int COLOR_DISABLED_BACKGROUND = 3;
	public static final int COLOR_HOVER_TEXT = 4;
	public static final int COLOR_HOVER_BACKGROUND = 5;
	public static final int COLOR_SELECTED_TEXT = 6;
	public static final int COLOR_SELECTED_BACKGROUND = 7;
	
	private static final int CELL_SEPARATION = 1;
	
	private int gridFlags;
	private int gridCountX;
	private int gridCountY;
	private Color borderColor;
	private Color rowSeparatorColor;
	private Color columnSeparatorColor;
	private float cellWidth;
	private float cellHeight;
	
	private final OutlineFormat cellTextOutline = new OutlineFormat();
	private final CellFormat cellFormat = new CellFormat();
	
	private final float[] gridMargins = new float[4];
	private final float[] textMargins = new float[4];
	
	private final List<LocalizedText> p_AF58F7D1 = new ArrayList<LocalizedText>();
	private final List<LocalizedText> p_AF58F7D2 = new ArrayList<LocalizedText>();
	
	private final HashMap<Integer, Integer> rowStates = new HashMap<Integer, Integer>();

	public WinGrid(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		gridFlags = SectionInt.getValues(block, 0xEEC2B000, new int[] {0}, 1)[0];
		
		parseMarginsSections(block, 0xEEC2B001, gridMargins);
		parseMarginsSections(block, 0xEEC2B005, textMargins);
		
		cellWidth = SectionFloat.getValues(block.getSection(0xEEC2B00C, SectionFloat.class), new float[] {0}, 1)[0];
		cellHeight = SectionFloat.getValues(block.getSection(0xEEC2B00D, SectionFloat.class), new float[] {0}, 1)[0];
		gridCountX = SectionInt2.getValues(block.getSection(0xEEC2B00E, SectionInt2.class), new int[] {-1}, 1)[0];
		gridCountY = SectionInt2.getValues(block.getSection(0xEEC2B00F, SectionInt2.class), new int[] {-1}, 1)[0];
		
		borderColor = PanelUtils.decodeColor(SectionInt.getValues(block, 0xEEC2B009, new int[] {-1}, 1)[0]);
		columnSeparatorColor = PanelUtils.decodeColor(SectionInt.getValues(block, 0xEEC2B00A, new int[] {-1}, 1)[0]);
		rowSeparatorColor = PanelUtils.decodeColor(SectionInt.getValues(block, 0xEEC2B00B, new int[] {-1}, 1)[0]);
		
		SPUISectionContainer[] property_039A69E6 = SectionSectionList.getValues(block.getSection(0x039A69E6, SectionSectionList.class), null, 1, -1);
		
		if (property_039A69E6 != null) {
			cellTextOutline.parse(property_039A69E6[0]);
		}
		
		LocalizedText[] p_AF58F7D1 = SectionText.getValues(block.getSection(0xAF58F7D1, SectionText.class), null, -1);
		LocalizedText[] p_AF58F7D2 = SectionText.getValues(block.getSection(0xAF58F7D2, SectionText.class), null, -1);
		
		if (p_AF58F7D1 != null) {
			for (LocalizedText t : p_AF58F7D1) this.p_AF58F7D1.add(t);
		}
		if (p_AF58F7D2 != null) {
			for (LocalizedText t : p_AF58F7D2) this.p_AF58F7D2.add(t);
		}
		
		SPUISectionContainer[] property_EEC2B010 = SectionSectionList.getValues(block.getSection(0xEEC2B010, SectionSectionList.class), null, 1, -1);
		if (property_EEC2B010 != null) {
			cellFormat.parse(property_EEC2B010[0]);
		}
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
		
		builder.addInt(block, 0xEEC2B009, new int[] {gridFlags});
		
		builder.addSectionList(block, 0xEEC2B001, new ListSectionContainer[] {saveMarginsSections(gridMargins)}, 50);
		builder.addSectionList(block, 0xEEC2B005, new ListSectionContainer[] {saveMarginsSections(textMargins)}, 50);
		
		builder.addInt(block, 0xEEC2B009, new int[] {PanelUtils.encodeColor(borderColor)});
		builder.addInt(block, 0xEEC2B00A, new int[] {PanelUtils.encodeColor(columnSeparatorColor)});
		builder.addInt(block, 0xEEC2B00B, new int[] {PanelUtils.encodeColor(rowSeparatorColor)});
		
		builder.addFloat(block, 0xEEC2B00C, new float[] {cellWidth});
		builder.addFloat(block, 0xEEC2B00D, new float[] {cellHeight});
		builder.addInt2(block, 0xEEC2B00E, new int[] {gridCountX});
		builder.addInt2(block, 0xEEC2B00F, new int[] {gridCountY});
		
		builder.addSectionList(block, 0xEEC2B010, new ListSectionContainer[] {cellFormat.saveComponent(builder)}, 105);
		builder.addSectionList(block, 0x039A69E6, new ListSectionContainer[] {cellTextOutline.saveComponent(builder)}, 122);
		
		builder.addText(block, 0xAF58F7D1, (LocalizedText[]) p_AF58F7D1.toArray(new LocalizedText[p_AF58F7D1.size()]));
		builder.addText(block, 0xAF58F7D2, (LocalizedText[]) p_AF58F7D2.toArray(new LocalizedText[p_AF58F7D2.size()]));
		
		return block;
	}

	public WinGrid(SPUIViewer viewer) {
		super(viewer);
		
		for (int i = 0; i < gridMargins.length; i++) gridMargins[i] = 5.0f;
		for (int i = 0; i < textMargins.length; i++) textMargins[i] = 1.0f;
		
		cellWidth = 120;
		cellHeight = 15;
		gridCountX = 1;
		gridCountY = 1;
		gridFlags = 105952;
		
		borderColor = Color.black;
		columnSeparatorColor = Color.black;
		rowSeparatorColor = Color.black;
		
		// ??
		flags |= 0x8;
	}

	private WinGrid() {
		super();
	}
	
	@Override
	public WinGrid copyComponent(boolean propagateIndependent) {
		WinGrid other = new WinGrid();
		copyComponent(other, propagateIndependent);
		
		other.gridFlags = gridFlags;
		other.cellWidth = cellWidth;
		other.cellHeight = cellHeight;
		other.gridCountX = gridCountX;
		other.gridCountY = gridCountY;
		other.borderColor = borderColor;
		other.columnSeparatorColor = columnSeparatorColor;
		other.rowSeparatorColor = rowSeparatorColor;
		
		System.arraycopy(gridMargins, 0, other.gridMargins, 0, gridMargins.length);
		System.arraycopy(textMargins, 0, other.textMargins, 0, textMargins.length);
		
		cellFormat.copyComponent(other.cellFormat);
		cellTextOutline.copyComponent(other.cellTextOutline);
		
		for (LocalizedText t : p_AF58F7D1) other.p_AF58F7D1.add(new LocalizedText(t));
		for (LocalizedText t : p_AF58F7D2) other.p_AF58F7D2.add(new LocalizedText(t));
		
		return other;
	}

	@Override
	public void setSPUIViewer(SPUIViewer viewer) {
		super.setSPUIViewer(viewer);
		
		rowStates.clear();
	}
	
	@Override
	protected boolean shouldUseFillColor() {
		return false;
	}
	
	@Override
	protected void paintBasic(Graphics2D graphics, Rectangle drawBounds) {
		super.paintBasic(graphics, drawBounds);
		
		Rectangle gridBounds = new Rectangle(
				Math.round(drawBounds.x + gridMargins[LEFT]),
				Math.round(drawBounds.y + gridMargins[TOP]),
				Math.round(drawBounds.width - gridMargins[LEFT] - gridMargins[RIGHT]),
				Math.round(drawBounds.height + gridMargins[TOP] - gridMargins[BOTTOM]));
		
		int rowCount = gridCountY;
		if (rowCount < 0 || rowCount*cellHeight > gridBounds.height) {
			rowCount = (int) Math.floor(gridBounds.height / cellHeight) + 1;
		}
		
		int columnCount = gridCountX;
		if (columnCount < 0 || columnCount*cellWidth > gridBounds.width) {
			columnCount = (int) Math.floor(gridBounds.width / cellWidth) + 1;
		}
		
		for (int i = 0; i < rowCount; i++) {
			int state = rowStates.getOrDefault(i, 0);
			int colorIndex = COLOR_IDLE_BACKGROUND;
			
			if ((flags & FLAG_ENABLED) == FLAG_ENABLED) 
			{
				if ((state & ActionableComponent.STATE_SELECTED) == ActionableComponent.STATE_SELECTED) {
					colorIndex = COLOR_SELECTED_BACKGROUND;
				}
				else if ((state & ActionableComponent.STATE_HOVER) == ActionableComponent.STATE_HOVER) {
					colorIndex = COLOR_HOVER_BACKGROUND;
				}
			}
			else {
				colorIndex = COLOR_DISABLED_BACKGROUND;
			}
			
			graphics.setColor(cellFormat.getColors()[colorIndex]);
			
			int y = Math.round(i * cellHeight);
			
			for (int j = 0; j < columnCount; j++) {
				
				int x = Math.round(j * cellWidth);
				
				graphics.fillRect(
						gridBounds.x + x, 
						gridBounds.y + y, 
						(x + cellWidth > gridBounds.width ? gridBounds.width - x : Math.round(cellWidth)) - CELL_SEPARATION,
						(y + cellHeight > gridBounds.height ? gridBounds.height - y : Math.round(cellHeight)) - CELL_SEPARATION);
			}
		}
		
		if (rowCount > 1 && columnCount > 1) {
			// draw column separator
			graphics.setColor(columnSeparatorColor);
			int y2 = Math.round(rowCount * cellHeight);
			if (y2 > gridBounds.height) {
				y2 = gridBounds.height;
			}
			
			for (int i = 1; i < columnCount; i++) {
				// if we don't subtract 1, it doesn't look well
				int x = Math.round(gridBounds.x + i * cellWidth) - 1;
				
				graphics.drawLine(
						x, gridBounds.y, 
						x, gridBounds.y + y2);
			}
			
			// draw row separator
			graphics.setColor(rowSeparatorColor);
			int x2 = Math.round(columnCount * cellWidth);
			if (x2 > gridBounds.width) {
				x2 = gridBounds.width;
			}
			
			for (int i = 1; i < rowCount; i++) {
				// if we don't subtract 1, it doesn't look well
				int y = Math.round(gridBounds.y + i * cellHeight) - 1;
				
				graphics.drawLine(
						gridBounds.x, y, 
						gridBounds.x + x2, y);
			}
		}
		
		graphics.setColor(borderColor);
		graphics.draw(drawBounds);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends ActionableComponent> T getComponentInCoords(Point p, Class<T> type) {
		if (viewer != null && viewer.isPreview()) {
			if ((flags & FLAG_VISIBLE) == FLAG_VISIBLE) {
				if (realBounds.contains(p)) {
					
					int rowIndex = (int) Math.floor((p.y - realBounds.y) / (cellHeight + CELL_SEPARATION));
					
					if (gridCountY < 0 || rowIndex < gridCountY) {
						if (type.isAssignableFrom(ActionableRow.class)) {
							return (T) new ActionableRow(rowIndex);
						}
						else {
							return null;
						}
					}
					else {
						return null;
					}
				}
				else {
					return null;
				}
			}
			else {
				return null;
			}
		}
		else {
			return super.getComponentInCoords(p, type);
		}
	}
	
	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new WindowDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				
				case 0xEEC2B000: return gridFlags;
				case 0xEEC2B001: return gridMargins;
				case 0xEEC2B005: return textMargins;
				case 0xEEC2B00C: return cellWidth;
				case 0xEEC2B00D: return cellHeight;
				case 0xEEC2B00E: return gridCountX;
				case 0xEEC2B00F: return gridCountY;
				case 0xEEC2B009: return borderColor;
				case 0xEEC2B00A: return columnSeparatorColor;
				case 0xEEC2B00B: return rowSeparatorColor;
				case 0x039A69E6: return cellTextOutline;
				case 0xAF58F7D1: return p_AF58F7D1;
				case 0xAF58F7D2: return p_AF58F7D2;
				case 0xEEC2B010: return cellFormat;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				
				case 0xEEC2B000: gridFlags = (int) value; break;
				case 0xEEC2B001: System.arraycopy((float[]) value, 0, gridMargins, 0, gridMargins.length); break;
				case 0xEEC2B005: System.arraycopy((float[]) value, 0, textMargins, 0, textMargins.length); break;
				case 0xEEC2B00C: cellWidth = (float) value; break;
				case 0xEEC2B00D: cellHeight = (float) value; break;
				case 0xEEC2B00E: gridCountX = (int) value; break;
				case 0xEEC2B00F: gridCountY = (int) value; break;
				case 0xEEC2B009: borderColor = (Color) value; break;
				case 0xEEC2B00A: columnSeparatorColor = (Color) value; break;
				case 0xEEC2B00B: rowSeparatorColor = (Color) value; break;
				}
				
				viewer.repaint();
				
				super.setValue(property, value, index);
			}
		};
	}
}
