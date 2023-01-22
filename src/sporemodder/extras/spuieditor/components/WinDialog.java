package sporemodder.extras.spuieditor.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import sporemodder.extras.spuieditor.ComponentChooser;
import sporemodder.extras.spuieditor.PanelUtils;
import sporemodder.extras.spuieditor.ResourceLoader;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.StyleSheet;
import sporemodder.extras.spuieditor.StyleSheetInstance;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.LocalizedText;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIComplexSections.ListSectionContainer;
import sporemodder.files.formats.spui.SPUIComplexSections.SectionText;
import sporemodder.files.formats.spui.SPUINumberSections.SectionFloat;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt;
import sporemodder.files.formats.spui.SPUINumberSections.SectionShort;
import sporemodder.files.formats.spui.SPUIObject;
import sporemodder.userinterface.JLabelLink;
import sporemodder.utilities.Hasher;

public class WinDialog extends Window {
	
	public static final int TYPE = 0x0F0B8B73;
	
	public static final int DIALOG_FLAG_SHOWTITLE = 0x4;
	public static final int DIALOG_FLAG_SHOWTITLEBAR = 0x8;
	public static final int DIALOG_FLAG_SHOWCLOSEBUTTON = 0x10;
	public static final int DIALOG_FLAG_SHOWBORDERS = 0x20;
	
	public static final int DIALOG_BORDER_SIZE = 2;
	
	private int dialogFlags;

	private float minWidth;
	private float minHeight;
	private float maxWidth;
	private float maxHeight;
	
	private SPUIDrawable closeButtonDrawable;
	private final float[] closeButtonMargins = new float[4];
	
	private final float[] clientAreaBorder = new float[4];
	
	private LocalizedText titleText;
	private StyleSheetInstance titleStyle;
	private String titleStyleName;
	private Color titleColor;
	private final float[] titleMargins = new float[4];
	
	private final WinButton closeButton = new WinButton() {
		@Override
		public int getFlags() {
			return parent.getFlags();
		}
		
		@Override
		public void revalidate() {
			bounds.x = Math.round(-bounds.width - closeButtonMargins[RIGHT]);
			bounds.y = 0;
			
			if ((dialogFlags & DIALOG_FLAG_SHOWBORDERS) == DIALOG_FLAG_SHOWBORDERS) {
				bounds.x -= DIALOG_BORDER_SIZE;
				bounds.y += DIALOG_BORDER_SIZE;
			}
			
			super.revalidate();
		}
	};
	
	public WinDialog(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		dialogFlags = SectionInt.getValues(block, 0xEEC2A002, new int[] {0}, 1)[0];
		
		short drawableIndex = SectionShort.getValues(block.getSection(0xEEC2A001, SectionShort.class), new short[] {-1}, 1)[0];
		if (drawableIndex != -1) {
			closeButtonDrawable = (SPUIDrawable) ResourceLoader.getComponent(block.getParent().get(drawableIndex));
		}
		
		minWidth = SectionFloat.getValues(block.getSection(0xEEC2A009, SectionFloat.class), new float[] {0}, 1)[0];
		minHeight = SectionFloat.getValues(block.getSection(0xEEC2A00A, SectionFloat.class), new float[] {0}, 1)[0];
		maxWidth = SectionFloat.getValues(block.getSection(0xEEC2A00B, SectionFloat.class), new float[] {0}, 1)[0];
		maxHeight = SectionFloat.getValues(block.getSection(0xEEC2A00C, SectionFloat.class), new float[] {0}, 1)[0];
		
		parseMarginsSections(block, 0xEEC2A003, clientAreaBorder);
		
		parseMarginsSections(block, 0xEEC2A004, closeButtonMargins);
		
		titleText = SectionText.getValues(block.getSection(0xEEC2A005, SectionText.class), new LocalizedText[] {null}, 1)[0];
		
		int titleStyleID = SectionInt.getValues(block, 0xEEC2A006, new int[] {0}, 1)[0];
		titleStyle = StyleSheet.getActiveStyleSheet().getInstance(titleStyleID);
		
		titleStyleName = titleStyle == null ? Hasher.getFileName(titleStyleID) : titleStyle.getName();
		
		titleColor = PanelUtils.decodeColor(SectionInt.getValues(block, 0xEEC2A007, new int[] {0xffffffff}, 1)[0]);
		
		parseMarginsSections(block, 0xEEC2A008, titleMargins);
		
		initCloseButton();
	}
	
	public WinDialog(SPUIViewer viewer) {
		super(viewer);
		
		backgroundColor = new Color(0xFFACA899);
		
		clientAreaBorder[0] = clientAreaBorder[1] = clientAreaBorder[2] = clientAreaBorder[3] = 3.0f;
		closeButtonMargins[0] = closeButtonMargins[1] = closeButtonMargins[2] = closeButtonMargins[3] = 3.0f;
		
		titleMargins[0] = 3.0f;
		titleMargins[1] = titleMargins[2] = titleMargins[3] = 0;
		
		titleColor = Color.white;
		
		minWidth = 0;
		minHeight = 0;
		maxWidth = 1000;
		maxHeight = 1000;
		dialogFlags = 67;  // ?
		titleStyleName = "DefaultStyle";
		titleStyle = StyleSheet.getActiveStyleSheet().getInstance(titleStyleName);
		
		initCloseButton();
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		SPUIBlock block = super.saveComponent(builder);
		
		builder.addInt(block, 0xEEC2A002, new int[] {dialogFlags});
		
		builder.addReference(block, 0xEEC2A001, new SPUIObject[] {closeButtonDrawable == null ? null : builder.addComponent(closeButtonDrawable)});
		
		builder.addFloat(block, 0xEEC2A009, new float[] {minWidth});
		builder.addFloat(block, 0xEEC2A00A, new float[] {minHeight});
		builder.addFloat(block, 0xEEC2A00B, new float[] {maxWidth});
		builder.addFloat(block, 0xEEC2A00C, new float[] {maxHeight});
		
		builder.addSectionList(block, 0xEEC2A003, new ListSectionContainer[] {saveMarginsSections(clientAreaBorder)}, 50);
		
		builder.addSectionList(block, 0xEEC2A004, new ListSectionContainer[] {saveMarginsSections(closeButtonMargins)}, 50);
		
		if (titleText != null) {
			builder.addText(block, 0xEEC2A005, new LocalizedText[] {titleText});
		}
		builder.addInt(block, 0xEEC2A006, new int[] {
				titleStyle == null ? Hasher.getFileHash(titleStyleName) : titleStyle.getStyleID()});
		builder.addInt(block, 0xEEC2A007, new int[] {PanelUtils.encodeColor(titleColor)});
		builder.addSectionList(block, 0xEEC2A008, new ListSectionContainer[] {saveMarginsSections(titleMargins)}, 50);
		
		return block;
	}
	
	private void initCloseButton() {
		closeButton.parent = this;
		closeButton.drawable = closeButtonDrawable;
		closeButton.modifiers.add(new SimpleLayout(viewer, SimpleLayout.FLAG_RIGHT));
		
		if (closeButtonDrawable != null) {
			Dimension titleBarDim = closeButtonDrawable.getDimensions(SPUIDrawable.IMAGE_MAIN);
			if (titleBarDim != null) {
				closeButton.bounds.setSize(titleBarDim);
			}
		}
	}
	
	@Override
	public WinDialog copyComponent(boolean propagateIndependent) {
		WinDialog other = new WinDialog();
		copyComponent(other, propagateIndependent);
		
		if (closeButtonDrawable != null) {
			other.closeButtonDrawable = propagateIndependent ? closeButtonDrawable.copyComponent(propagateIndependent) : closeButtonDrawable;
		}
		
		other.closeButton.parent = other;
		other.closeButton.drawable = other.closeButtonDrawable;
		other.maxWidth = maxWidth;
		other.maxHeight = maxHeight;
		other.minHeight = minHeight;
		other.titleStyle = titleStyle;
		other.titleStyleName = titleStyleName;
		other.titleText = new LocalizedText(titleText);
		other.titleColor = titleColor;
		other.dialogFlags = dialogFlags;
		
		System.arraycopy(clientAreaBorder, 0, other.clientAreaBorder, 0, clientAreaBorder.length);
		System.arraycopy(closeButtonMargins, 0, other.closeButtonMargins, 0, closeButtonMargins.length);
		System.arraycopy(titleMargins, 0, other.titleMargins, 0, titleMargins.length);
		
		return other;
	}

	protected WinDialog() {
		super();
		initCloseButton();
	}
	
	public int getDialogFlags() {
		return dialogFlags;
	}
	
	@Override
	public void revalidate() {
		super.revalidate();
		
		if (closeButton != null) {
			closeButton.revalidate();
		}
	}
	
	@Override
	public void setSPUIViewer(SPUIViewer viewer) {
		super.setSPUIViewer(viewer);
		
		if (closeButton != null) {
			closeButton.setSPUIViewer(viewer);
		}
	}
	
	@Override
	public WinComponent getComponentInCoords(Point p) {
		if (viewer != null && viewer.isPreview()) {
			if ((flags & FLAG_VISIBLE) == FLAG_VISIBLE) {
				if (realBounds.contains(p)) {
					return this;
				}
				else {
					if ((dialogFlags & DIALOG_FLAG_SHOWTITLE) == DIALOG_FLAG_SHOWTITLE &&
							closeButton.realBounds.contains(p)) {
						return closeButton;
					}
					else {
						return null;
					}
				}
			}
			else {
				return null;
			}
		}
		else {
			return super.getComponentInCoords(p);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends ActionableComponent> T getComponentInCoords(Point p, Class<T> type) {
		if (viewer != null && viewer.isPreview()) {
			if ((flags & FLAG_VISIBLE) == FLAG_VISIBLE) {
				if (type != WinButton.class) {
					if (realBounds.contains(p) && type.isInstance(this)) {
						return (T) this;
					}
					else {
						return null;
					}
				}
				else {
					if ((dialogFlags & DIALOG_FLAG_SHOWTITLE) == DIALOG_FLAG_SHOWTITLE &&
							closeButton.realBounds.contains(p)) {
						return (T) closeButton;
					}
					else {
						return null;
					}
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
				
				case 0xEEC2A002: return dialogFlags;
				case 0xEEC2A001: return closeButtonDrawable;
				case 0xEEC2A009: return minWidth;
				case 0xEEC2A00A: return minHeight;
				case 0xEEC2A00B: return maxWidth;
				case 0xEEC2A00C: return maxHeight;
				case 0xEEC2A003: return clientAreaBorder;
				case 0xEEC2A004: return closeButtonMargins;
				case 0xEEC2A005: return titleText;
				case 0xEEC2A006: return titleStyleName;
				case 0xEEC2A007: return titleColor;
				case 0xEEC2A008: return titleMargins;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				
				case 0xEEC2A002: dialogFlags = (int) value; break;
				case 0xEEC2A001: 
					ComponentChooser.showChooserAction(WinDialog.this, "closeButtonDrawable", 
							ComponentFactory.getComponentChooser(property.getType(), viewer),
							(JLabelLink) value, viewer, false);
					break;
					
				case 0xEEC2A009: minWidth = (float) value; break;
				case 0xEEC2A00A: minHeight = (float) value; break;
				case 0xEEC2A00B: maxWidth = (float) value; break;
				case 0xEEC1D00C: maxHeight = (float) value; break;
				case 0xEEC1D003: System.arraycopy((float[]) value, 0, clientAreaBorder, 0, clientAreaBorder.length); break;
				case 0xEEC2A004: System.arraycopy((float[]) value, 0, closeButtonMargins, 0, closeButtonMargins.length); break;
				case 0xEEC2A005: 
					if (titleText == null) {
						titleText = new LocalizedText((LocalizedText) value);
					} else {
						titleText.copy((LocalizedText) value); 
					}
					break;
				case 0xEEC2A006:
					titleStyleName = (String) value;
					if (titleStyleName == null || titleStyleName.isEmpty()) {
						titleStyleName = "DefaultStyle";
					}
					
					titleStyle = StyleSheet.getActiveStyleSheet().getStyleInstance(titleStyleName);
					
					break;
				case 0xEEC2A007: titleColor = (Color) value; break;
				case 0xEEC2A008: System.arraycopy((float[]) value, 0, titleMargins, 0, titleMargins.length); break;
				}
				
				viewer.repaint();
				
				super.setValue(property, value, index);
			}
		};
	}

	public SPUIDrawable getCloseButtonDrawable() {
		return closeButtonDrawable;
	}

	public float[] getCloseButtonMargins() {
		return closeButtonMargins;
	}

	public LocalizedText getTitleText() {
		return titleText;
	}

	public StyleSheetInstance getTitleStyle() {
		return titleStyle;
	}

	public Color getTitleColor() {
		return titleColor;
	}

	public float[] getTitleMargins() {
		return titleMargins;
	}
	
	public WinButton getCloseButton() {
		return closeButton;
	}
	
	public Rectangle getTitleBarBounds(Graphics2D graphics, Rectangle bounds) {
		if (bounds == null) {
			bounds = realBounds;
		}
		
		Rectangle rect = new Rectangle();
		Rectangle2D titleRect = StyleSheetInstance.getStringBounds(graphics, titleStyle, viewer.getString(titleText));
		
		rect.x = bounds.x;
		rect.y = bounds.y;
		rect.width = bounds.width;
		rect.height = (int) Math.round(titleRect.getHeight());
		
		if (closeButtonDrawable != null) {
			Dimension titleBarDim = closeButtonDrawable.getDimensions(SPUIDrawable.IMAGE_MAIN);
			if (titleBarDim != null) {
				if (titleBarDim.height > rect.height) {
					rect.height = titleBarDim.height;
				}
			}
		}
		
		if ((dialogFlags & DIALOG_FLAG_SHOWBORDERS) == DIALOG_FLAG_SHOWBORDERS) {
			rect.x += DIALOG_BORDER_SIZE;
			rect.y += DIALOG_BORDER_SIZE;
			rect.width -= 2*DIALOG_BORDER_SIZE;
			rect.height -= DIALOG_BORDER_SIZE;
		}
		
		return rect;
	}
}
