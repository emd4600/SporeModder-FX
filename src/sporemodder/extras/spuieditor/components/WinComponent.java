package sporemodder.extras.spuieditor.components;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.tree.TreePath;

import sporemodder.extras.spuieditor.StyleSheetInstance;
import sporemodder.files.formats.LocalizedText;

public interface WinComponent extends SPUIComponent {
	
	public static final String INTERFACE_NAME = "IWindow";
	
	public static final int FLAG_VISIBLE = 0x1;
	public static final int FLAG_ENABLED = 0x2;
	
	@Override
	public WinComponent copyComponent(boolean propagateIndependent);
	
	public String getName();
	public void setName(String name);

	public void revalidate();
	
	public List<WinComponent> getChildren();
	public List<SPUIWinProc> getModifiers();
	
	public Rectangle getRealBounds();
	public Rectangle getBounds();
	public void setBounds(Rectangle bounds);
	public void translate(int dx, int dy);
	
	public Color getTintColor();
	public void setTintColor(Color color);
	public Color getBackgroundColor();
	public void setBackgroundColor(Color color);
	
	public StyleSheetInstance getStyle();
	public void setStyle(StyleSheetInstance style);
	
	public int getFlags();
	public void setFlag(int flag, boolean value);
	
	public LocalizedText getText();
	public void setText(LocalizedText text);
	
	public SPUIDrawable getDrawable();
	public void setDrawable(SPUIDrawable drawable);
	
	public void removeChildComponent(SPUIComponent component);
	public void insertChildComponent(SPUIComponent component, int childIndex);
	public int getIndexOfChild(SPUIComponent component);
	
	public WinComponent getParent();
	public void setParent(WinComponent parent);
	
	public void paintComponent(Graphics2D graphics);
	
	public boolean isActionableComponent();
	public boolean isLayoutWindow();
	public boolean isMoveable();
	
	public WinComponent getComponentInCoords(Point p);
	public <T extends ActionableComponent> T getComponentInCoords(Point p, Class<T> type);
	
	public TreePath getHierarchyTreePath();

}
