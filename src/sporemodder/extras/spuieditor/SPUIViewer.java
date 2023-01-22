package sporemodder.extras.spuieditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import sporemodder.extras.spuieditor.components.ActionableComponent;
import sporemodder.extras.spuieditor.components.SPUIComponent;
import sporemodder.extras.spuieditor.components.SPUIComponent.SPUIComponentFilter;
import sporemodder.extras.spuieditor.components.SPUIWinProc;
import sporemodder.extras.spuieditor.components.WinButton;
import sporemodder.extras.spuieditor.components.WinComponent;
import sporemodder.extras.spuieditor.components.Window;
import sporemodder.files.formats.LocaleFile;
import sporemodder.files.formats.LocalizedText;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIMain;

public class SPUIViewer extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7036907961349652172L;
	
	public static boolean RENDER_ANIMATED_ICONS = false;
	
	private static final Color MARKED_COLOR = new Color(0xFFe14b4b);
	private static final Color ACTIVE_COLOR = new Color(0xFF70E0AA);
	
	private static final int POINT_SIZE = 6;
	
	private SPUIEditor editor;
	private SPUIMain spui;
	//private final List<WinComponent> mainWindows = new ArrayList<WinComponent>();
	
	private final Window layoutWindow = new Window(this, true);
	
	private SPUIComponent activeComponent;
	
	private int mouseX;
	private int mouseY;
//	private boolean isDragging;
	private DraggableType draggableType;
	//private WinComponentAction winComponentAction;
	private int lastClickMouseX;
	private int lastClickMouseY;
	
	private int offsetX;
	private int offsetY;
	
	private boolean showInvisibleComponents;
	private boolean isPreview;
	
	private boolean isArtificialMoving;
	private boolean markUsingComponents;
	
	private final List<SPUIComponent> usingComponents = new ArrayList<SPUIComponent>();
	
	private boolean useLocaleFiles;
	
	// Warning: only used in previews
	private ActionableComponent hoveredComponent;
	
	public SPUIViewer(SPUIMain spui, SPUIEditor editor) throws InvalidBlockException, FileNotFoundException, IOException {
		this.spui = spui;
		this.editor = editor;
		
		for (SPUIBlock block : this.spui.getBlocks()) {
			if (block.isRoot()) {
				SPUIComponent component = ResourceLoader.getComponent(block);
				component.setSPUIViewer(this);
				if (component instanceof WinComponent) {
					WinComponent mainWindow = (WinComponent) component;
					mainWindow.setParent(layoutWindow);
					layoutWindow.getChildren().add(mainWindow);
				}
				else if (component instanceof SPUIWinProc) {
					SPUIWinProc modifier = (SPUIWinProc) component;
					modifier.setParent(layoutWindow);
					layoutWindow.getModifiers().add(modifier);
				}
			}
		}
		
		layoutWindow.revalidate();
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		
		this.setOpaque(true);
	}
	
	public SPUIViewer(SPUIViewer viewer) throws InvalidBlockException, IOException {
		
		useLocaleFiles = viewer.useLocaleFiles;
		isPreview = viewer.isPreview;
		markUsingComponents = viewer.markUsingComponents;
		showInvisibleComponents = viewer.showInvisibleComponents;
		
		for (SPUIWinProc modifier : viewer.layoutWindow.getModifiers()) {
			SPUIWinProc comp = modifier.copyComponent(true);
			comp.setSPUIViewer(this);
			layoutWindow.getModifiers().add(comp);
		}
		for (WinComponent window : viewer.layoutWindow.getChildren()) {
			WinComponent comp = window.copyComponent(true);
			comp.setSPUIViewer(this);
			layoutWindow.getChildren().add(comp);
		}
		
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	
	public void dispose() {
		// This will kill all necessary threads
		layoutWindow.setSPUIViewer(null);
	}
	
	public void reset() {
		layoutWindow.setSPUIViewer(this);
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension maxBounds = new Dimension();
		for (WinComponent mainWindow : layoutWindow.getChildren()) {
			Dimension dimension = mainWindow.getRealBounds().getSize();
			if (dimension.width > maxBounds.width) {
				maxBounds.width = dimension.width;
			}
			if (dimension.height > maxBounds.height) {
				maxBounds.height = dimension.height;
			}
		}
		
		return maxBounds;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.translate(offsetX, offsetY);
		
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
//		for (WinComponent mainWindow : mainWindows) {
//			mainWindow.paintComponent(g2d);
//		}
		
		layoutWindow.paintComponent(g2d);
		
		// if the outline is drawn in each component, some parts won't be visible
		if (activeComponent != null && activeComponent instanceof WinComponent) {
			Rectangle bounds = ((WinComponent) activeComponent).getRealBounds();
			g2d.setColor(ACTIVE_COLOR);
			g2d.draw(bounds);
			
			g2d.fill(DraggableType.RESIZE_TOP_LEFT.getPointRect(bounds, POINT_SIZE));
			g2d.fill(DraggableType.RESIZE_TOP_RIGHT.getPointRect(bounds, POINT_SIZE));
			g2d.fill(DraggableType.RESIZE_BOTTOM_RIGHT.getPointRect(bounds, POINT_SIZE));
			g2d.fill(DraggableType.RESIZE_BOTTOM_LEFT.getPointRect(bounds, POINT_SIZE));
			g2d.fill(DraggableType.RESIZE_TOP.getPointRect(bounds, POINT_SIZE));
			g2d.fill(DraggableType.RESIZE_BOTTOM.getPointRect(bounds, POINT_SIZE));
			g2d.fill(DraggableType.RESIZE_LEFT.getPointRect(bounds, POINT_SIZE));
			g2d.fill(DraggableType.RESIZE_RIGHT.getPointRect(bounds, POINT_SIZE));
		}
		
		if (markUsingComponents) {
			g2d.setColor(MARKED_COLOR);
			for (SPUIComponent comp : usingComponents) {
				if (comp != activeComponent  && comp instanceof WinComponent) {
					int flags = ((WinComponent) comp).getFlags();
					if (showInvisibleComponents || (flags & WinComponent.FLAG_VISIBLE) == WinComponent.FLAG_VISIBLE) {
						g2d.draw(((WinComponent) comp).getRealBounds());
					}
				}
			}
		}
	}
	
//	public List<WinComponent> getMainWindows() {
//		return mainWindows;
//	}
	
	public Window getLayoutWindow() {
		return layoutWindow;
	}

	public Rectangle getTotalBounds() {
		Rectangle bounds = null;
		for (WinComponent window : layoutWindow.getChildren()) {
			Rectangle winBounds = window.getRealBounds();
			if (bounds == null) {
				bounds = new Rectangle(winBounds);
			}
			else if (bounds.x > winBounds.x) bounds.x = winBounds.x;
			else if (bounds.y > winBounds.y) bounds.y = winBounds.y;
			else if (winBounds.x + winBounds.width > bounds.x + bounds.width) bounds.width = winBounds.x +  winBounds.width - bounds.x;
			else if (winBounds.y + winBounds.height > bounds.y + bounds.height) bounds.height = winBounds.y +  winBounds.height - bounds.y;
		}
		
		return bounds;
	}
	
	public void setWindowsSize(Dimension size) {
//		// ??
//		for (WinComponent window : mainWindows) {
//			window.getBounds().setSize(size);
//		}
		
		layoutWindow.getBounds().setSize(size);
		
		// if we don't do this, things like the creature editor won't get scaled
		if (layoutWindow.getChildren().size() == 1) {
			layoutWindow.getChildren().get(0).getBounds().setSize(size);
		}
	}
	
	public void revalidateWindows() {
//		for (WinComponent window : mainWindows) {
//			window.revalidate();
//		}
		layoutWindow.revalidate();
	}
	
	public SPUIComponent getActiveComponent() {
		return activeComponent;
	}
	
	private void fillUsingComponents() {
		usingComponents.clear();
		if (activeComponent != null) {
			layoutWindow.getComponents(usingComponents, new SPUIComponentFilter() {
				@Override
				public boolean accept(SPUIComponent component) {
					return component.usesComponent(activeComponent);
				}
			});
		}
	}
	
	public void setActiveComponent(SPUIComponent component, boolean generateUndoHistory) {
		if (component == activeComponent) {
			return;
		}
		if (editor != null && generateUndoHistory) {
			editor.addCommandAction(new SelectionAction(this, activeComponent, component));
		}
		activeComponent = component;
		if (component == null) {
			if (editor != null) {
				editor.updateMenus();
			}
			repaint();
			return;
		}
		
		if (markUsingComponents) {
			fillUsingComponents();
		}
		
		if (editor != null) {
			JPanel pPanel = component.getPropertiesPanel();
			if (pPanel != null) {
				JPanel panel = new JPanel();
				panel.setLayout(new BorderLayout());
				//panel.setMinimumSize(new Dimension(0, (int)panel.getMinimumSize().getHeight()));
				//panel.add(getPropertiesPanel);
				
				panel.add(pPanel);
				editor.getPropertiesPanel().setViewportView(panel);
			}
			editor.setSelectedComponent(component);
		}
		
		repaint();
	}
	
	public void setActiveComponent(SPUIComponent component) {
		setActiveComponent(component, true);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isMiddleMouseButton(e) && e.getClickCount() == 2) {
			offsetX = 0;
			offsetY = 0;
			repaint();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (isPreview) {
			if (hoveredComponent == null) {
				for (WinComponent mainWindow : layoutWindow.getChildren()) {
					hoveredComponent = mainWindow.getComponentInCoords(new Point(e.getX() - offsetX, e.getY() - offsetY), ActionableComponent.class);
					if (hoveredComponent != null) break;
				}
			}
			if (hoveredComponent != null) {
				hoveredComponent.setState(hoveredComponent.getState() | ActionableComponent.STATE_CLICK);
			}
		}
		else {
			if (!SwingUtilities.isMiddleMouseButton(e)) {
				if (activeComponent != null && activeComponent instanceof WinComponent) {
					if (SwingUtilities.isRightMouseButton(e) && draggableType == null) {
						Rectangle bounds = ((WinComponent) activeComponent).getRealBounds();
						Point p = e.getPoint();
						p.x -= offsetY;
						p.y -= offsetY;
						
						if (DraggableType.RESIZE_TOP_LEFT.getPointRect(bounds, POINT_SIZE).contains(p)) {
							draggableType = DraggableType.RESIZE_TOP_LEFT;
						}
						else if (DraggableType.RESIZE_TOP_RIGHT.getPointRect(bounds, POINT_SIZE).contains(p)) {
							draggableType = DraggableType.RESIZE_TOP_RIGHT;
						}
						else if (DraggableType.RESIZE_BOTTOM_RIGHT.getPointRect(bounds, POINT_SIZE).contains(p)) {
							draggableType = DraggableType.RESIZE_BOTTOM_RIGHT;
						}
						else if (DraggableType.RESIZE_BOTTOM_LEFT.getPointRect(bounds, POINT_SIZE).contains(p)) {
							draggableType = DraggableType.RESIZE_BOTTOM_LEFT;
						}
						else if (DraggableType.RESIZE_TOP.getPointRect(bounds, POINT_SIZE).contains(p)) {
							draggableType = DraggableType.RESIZE_TOP;
						}
						else if (DraggableType.RESIZE_BOTTOM.getPointRect(bounds, POINT_SIZE).contains(p)) {
							draggableType = DraggableType.RESIZE_BOTTOM;
						}
						else if (DraggableType.RESIZE_LEFT.getPointRect(bounds, POINT_SIZE).contains(p)) {
							draggableType = DraggableType.RESIZE_LEFT;
						}
						else if (DraggableType.RESIZE_RIGHT.getPointRect(bounds, POINT_SIZE).contains(p)) {
							draggableType = DraggableType.RESIZE_RIGHT;
						}
						else {
							processNormalMouseClick(e);
						}
					}
					else {
						processNormalMouseClick(e);
					}
				}
				else {
					processNormalMouseClick(e);
				}
			}
			
			// if we don't do this, it won't generate undo history
			if (!isArtificialMoving) {
				mouseX = e.getX();
				mouseY = e.getY();
				lastClickMouseX = mouseX;
				lastClickMouseY = mouseY;
			}
		}
	}
	
	private void processNormalMouseClick(MouseEvent e) {
		WinComponent component = null;
		for (WinComponent mainWindow : layoutWindow.getChildren()) {
			component = mainWindow.getComponentInCoords(new Point(e.getX() - offsetX, e.getY() - offsetY));
			if (component != null) break;
		}
		if (component != null) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				setActiveComponent((SPUIComponent) component);
			}
			// we must set the draggable type even if it's right mouse click
			draggableType = DraggableType.COMPONENT;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (isPreview) {
			if (hoveredComponent != null) {
				hoveredComponent.setState(hoveredComponent.getState() & ~ActionableComponent.STATE_CLICK);
				if (hoveredComponent instanceof WinButton && ((WinButton) hoveredComponent).isSelectable()) {
					int state = hoveredComponent.getState();
					if ((state & ActionableComponent.STATE_SELECTED) == ActionableComponent.STATE_SELECTED) {
						hoveredComponent.setState(hoveredComponent.getState() & ~ActionableComponent.STATE_SELECTED);
					}
					else {
						List<SPUIComponent> groupComponents = new ArrayList<SPUIComponent>();
						final int buttonGroup = ((WinButton) hoveredComponent).getButtonGroup();
						
						if (buttonGroup != 0) {
							// This is not accurate, it doesn't really work like this
//							mainWindow.getComponents(groupComponents, new SPUIComponentFilter() {
//								@Override
//								public boolean accept(SPUIComponent component) {
//									if (component instanceof WinButton && ((WinButton) component).getButtonGroup().equals(buttonGroup)) {
//										String str = ((WinButton) component).getButtonGroup();
//										if (str != null && str.equals(buttonGroup)) {
//											return true;
//										}
//										return false;
//									}
//									else {
//										return false;
//									}
//								}
//							});
//							for (SPUIComponent comp : groupComponents) {
//								((WinButton) comp).setState(((WinButton) comp).getState() & ~WinButton.STATE_SELECTED);
//							}
						}
						
						hoveredComponent.setState(hoveredComponent.getState() | ActionableComponent.STATE_SELECTED);
						
					}
				}
			}
		}
		else {
			if (editor != null && activeComponent instanceof WinComponent) {
				if (mouseX - lastClickMouseX != 0 || mouseY - lastClickMouseY != 0) {
					editor.addCommandAction(new WinComponentAction((WinComponent) activeComponent, draggableType, mouseX - lastClickMouseX, mouseY - lastClickMouseY));
				}
			}
			draggableType = null;
			// fix previous workaround
			if (isArtificialMoving) {
				lastClickMouseX = mouseX;
				lastClickMouseY = mouseY;
			}
			isArtificialMoving = false;
		}
		
		this.requestFocusInWindow();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!isPreview) {
			if (SwingUtilities.isMiddleMouseButton(e)) {
				offsetX += e.getX() - mouseX;
				offsetY += e.getY() - mouseY;
				repaint();
			}
			else if (SwingUtilities.isRightMouseButton(e)) {
				if (activeComponent != null && draggableType != null && 
						activeComponent instanceof WinComponent && ((WinComponent) activeComponent).isMoveable()) {
					draggableType.process((WinComponent) activeComponent, e.getX() - mouseX, e.getY() - mouseY);
				}
			}
			mouseX = e.getX();
			mouseY = e.getY();
		}
		else {
			if (hoveredComponent != null && 
					hoveredComponent instanceof WinComponent && ((WinComponent) hoveredComponent).isMoveable()) {
				
				((WinComponent) hoveredComponent).translate(e.getX() - mouseX, e.getY() - mouseY);
				mouseX = e.getX();
				mouseY = e.getY();
			}
		}
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (isPreview) {
			// Some WinButtons contain windows, but they must get enabled anyways
			ActionableComponent comp = null;
			for (WinComponent mainWindow : layoutWindow.getChildren()) {
				comp = mainWindow.getComponentInCoords(new Point(e.getX() - offsetX, e.getY() - offsetY), ActionableComponent.class);
				if (comp != null) break;
			}
			
			if (comp != hoveredComponent && hoveredComponent != null) {
				hoveredComponent.setState(hoveredComponent.getState() & ~ActionableComponent.STATE_HOVER);
			}
			if (comp != null) {
				comp.setState(comp.getState() | ActionableComponent.STATE_HOVER);
				hoveredComponent = comp;
			} else {
				hoveredComponent = null;
			}
		}
		else {
			if (activeComponent != null && activeComponent instanceof WinComponent) {
				if (isArtificialMoving) {
					draggableType.process((WinComponent) activeComponent, e.getX() - mouseX, e.getY() - mouseY);
				}
				else {
					Rectangle bounds = ((WinComponent) activeComponent).getRealBounds();
					Point p = e.getPoint();
					p.x -= offsetX;
					p.y -= offsetY;
					
					if (DraggableType.RESIZE_TOP_LEFT.getPointRect(bounds, POINT_SIZE).contains(p)) {
						setCursor(DraggableType.RESIZE_TOP_LEFT.getCursor());
					}
					else if (DraggableType.RESIZE_TOP_RIGHT.getPointRect(bounds, POINT_SIZE).contains(p)) {
						setCursor(DraggableType.RESIZE_TOP_RIGHT.getCursor());
					}
					else if (DraggableType.RESIZE_BOTTOM_RIGHT.getPointRect(bounds, POINT_SIZE).contains(p)) {
						setCursor(DraggableType.RESIZE_BOTTOM_RIGHT.getCursor());
					}
					else if (DraggableType.RESIZE_BOTTOM_LEFT.getPointRect(bounds, POINT_SIZE).contains(p)) {
						setCursor(DraggableType.RESIZE_BOTTOM_LEFT.getCursor());
					}
					else if (DraggableType.RESIZE_TOP.getPointRect(bounds, POINT_SIZE).contains(p)) {
						setCursor(DraggableType.RESIZE_TOP.getCursor());
					}
					else if (DraggableType.RESIZE_BOTTOM.getPointRect(bounds, POINT_SIZE).contains(p)) {
						setCursor(DraggableType.RESIZE_BOTTOM.getCursor());
					}
					else if (DraggableType.RESIZE_LEFT.getPointRect(bounds, POINT_SIZE).contains(p)) {
						setCursor(DraggableType.RESIZE_LEFT.getCursor());
					}
					else if (DraggableType.RESIZE_RIGHT.getPointRect(bounds, POINT_SIZE).contains(p)) {
						setCursor(DraggableType.RESIZE_RIGHT.getCursor());
					}
					else {
						setCursor(Cursor.getDefaultCursor());
					}
				}
			}
		}
		mouseX = e.getX();
		mouseY = e.getY();
	}
	
	@Override
	public void keyPressed(KeyEvent arg0) {
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if (!isArtificialMoving && 
				activeComponent != null && 
				activeComponent instanceof WinComponent && 
				arg0.getKeyCode() == KeyEvent.VK_G) {
			
			draggableType = DraggableType.COMPONENT;
			lastClickMouseX = mouseX;
			lastClickMouseY = mouseY;
			isArtificialMoving = true;
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}
	
	public boolean getShowInvisibleComponents() {
		return showInvisibleComponents;
	}
	
	public void setShowInvisibleComponents(boolean value) {
		this.showInvisibleComponents = value;
		repaint();
	}
	
	public SPUIEditor getEditor() {
		return editor;
	}
	
	/**
	 * Tells whether this viewer represents a preview or not. In previews, elements can't be selected or modified, and they are shown in the same way they would be in-game.
	 * For example, disabled elements will show the disabled icons, buttons will show the images according to their state, etc
	 * @return
	 */
	public boolean isPreview() {
		return isPreview;
	}
	
	public void setIsPreview(boolean isPreview) {
		this.isPreview = isPreview;
	}

	public boolean isUseLocaleFiles() {
		return useLocaleFiles;
	}

	public void setUseLocaleFiles(boolean useLocaleFiles) {
		this.useLocaleFiles = useLocaleFiles;
		repaint();
	}
	
	public String getString(LocalizedText text) {
		if (text == null) {
			return null;
		}
		if (useLocaleFiles && text.tableID != -1) {
			
			try {
				LocaleFile locale = ResourceLoader.getLocaleFile(text.tableID);
				if (locale != null) {
					return locale.get(text.instanceID);
				}
				
			} catch (NumberFormatException | IOException e) {
				e.printStackTrace();
			}
		}
		
		return text.getString();
	}

	public boolean isMarkUsingComponents() {
		return markUsingComponents;
	}

	public void setMarkUsingComponents(boolean markUsingComponents) {
		this.markUsingComponents = markUsingComponents;
		if (markUsingComponents) {
			fillUsingComponents();
		}
		repaint();
	}
	
	
}
