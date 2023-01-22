package sporemodder.extras.spuieditor;

import java.awt.BasicStroke;
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
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import sporemodder.extras.spuieditor.ComponentValueAction.ComponentValueListener;
import sporemodder.extras.spuieditor.components.Image;

public class UVEditorImagePanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
	
	private static final int GRID_SIZE = 20;
	private static final Color GRID_COLOR1 = Color.white;
	private static final Color GRID_COLOR2 = Color.lightGray;
	
	private static final float SCALING_FACTOR = 4f;
	private static final float LINE_WIDTH = 1.0f;
	private static final int POINT_SIZE = 4;
	private static final Color POINT_COLOR = Color.red;
	
	private UVEditor editor;
	
	private float zoomLevel = 1.0f;
	
	private DraggableType selectedType;
	
	private int mouseX;
	private int mouseY;
	private int lastClickMouseX;
	private int lastClickMouseY;
	
	private boolean isArtificialMoving;
	
	private final float[] oldUVCoords = new float[4];
	
	public UVEditorImagePanel(UVEditor editor) {
		super();
		
		this.editor = editor;
		
		setBackground(Color.gray);
		setFocusable(true);
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
	}

	public void setZoomLevel(float zoomLevel) {
		this.zoomLevel = zoomLevel;
		
		revalidate();  // update parent scrollPane
		repaint();
	}
	
	public float getZoomLevel() {
		return zoomLevel;
	}
	
	@Override
	public Dimension getPreferredSize() {
		BufferedImage image = editor.getImage().getBufferedImage();
		if (image == null) {
			return super.getPreferredSize();
		}
        int w = (int)(zoomLevel * image.getWidth());
        int h = (int)(zoomLevel * image.getHeight());
        return new Dimension(w, h);
    }
	
	private Rectangle getImageRect(Image image) {
        // 0, 0 -> upper left corner
		BufferedImage bi = image.getBufferedImage();
 		Dimension panelSize = getSize();
 		Rectangle rect = new Rectangle();
 		
 		rect.width = Math.round(bi.getWidth() * zoomLevel);
 		rect.height = Math.round(bi.getHeight() * zoomLevel);
 		rect.x = (panelSize.width - rect.width) / 2;
 		rect.y = (panelSize.height - rect.height) / 2;
 		
 		return rect;
	}
	
	private Rectangle getUVBounds(float[] uvCoords, Rectangle imageRect, boolean snapToPixels) {
		
		float[] newUVCoords = null;
		if (snapToPixels) {
			
			newUVCoords = new float[4];
			
			editor.getImage().snapToPixels(newUVCoords);
		}
		else {
			newUVCoords = uvCoords;
		}
		
		Rectangle bounds = new Rectangle();
 		bounds.x = Math.round(imageRect.x + newUVCoords[0] * imageRect.width);
 		bounds.y = Math.round(imageRect.y + newUVCoords[1] * imageRect.height);
 		bounds.width = Math.round((newUVCoords[2] - newUVCoords[0]) * imageRect.width);
 		bounds.height = Math.round((newUVCoords[3] - newUVCoords[1]) * imageRect.height);
 		return bounds;
	}
	
	private void setUVCoordsFromBounds(Rectangle bounds, Rectangle imageRect) {
		float[] uvCoords = editor.getImage().getUVCoords();
		uvCoords[0] = (bounds.x - imageRect.x) / (float) imageRect.width;
		uvCoords[1] = (bounds.y - imageRect.y) / (float) imageRect.height;
		uvCoords[2] = bounds.width / (float) imageRect.width + uvCoords[0];
		uvCoords[3] = bounds.height / (float) imageRect.height + uvCoords[1];
	}

	@Override
	public void paintComponent( Graphics g ) {
        super.paintComponent(g);
        
        Image image = editor.getImage();
        
        if (image == null) {
        	return;
        }
        
        // Draw image on the center
        Rectangle imageRect = getImageRect(image);
        
        Graphics2D g2d = (Graphics2D)g;
        
        drawBackgroundGrid(g2d, imageRect);
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
 		g2d.drawImage(image.getBufferedImage(), imageRect.x, imageRect.y, imageRect.width, imageRect.height, this);
 		
 		Rectangle bounds = getUVBounds(image.getUVCoords(), imageRect, editor.isSnapToPixels());
 		
 		int pointRadius = Math.round(POINT_SIZE + ((zoomLevel - 1) / SCALING_FACTOR));
		int lineWidth = Math.round(LINE_WIDTH + ((zoomLevel - 1) / SCALING_FACTOR));
		
		g2d.setStroke(new BasicStroke(lineWidth));
 		
 		g2d.setColor(POINT_COLOR);
		g2d.draw(bounds);
		
		g2d.fill(DraggableType.RESIZE_TOP_LEFT.getPointRect(bounds, pointRadius));
		g2d.fill(DraggableType.RESIZE_TOP_RIGHT.getPointRect(bounds, pointRadius));
		g2d.fill(DraggableType.RESIZE_BOTTOM_RIGHT.getPointRect(bounds, pointRadius));
		g2d.fill(DraggableType.RESIZE_BOTTOM_LEFT.getPointRect(bounds, pointRadius));
		g2d.fill(DraggableType.RESIZE_TOP.getPointRect(bounds, pointRadius));
		g2d.fill(DraggableType.RESIZE_BOTTOM.getPointRect(bounds, pointRadius));
		g2d.fill(DraggableType.RESIZE_LEFT.getPointRect(bounds, pointRadius));
		g2d.fill(DraggableType.RESIZE_RIGHT.getPointRect(bounds, pointRadius));
        
    }
	
	private void drawBackgroundGrid(Graphics2D g2, Rectangle imageBounds) {
		int limitX = imageBounds.x + imageBounds.width;
		int limitY = imageBounds.y + imageBounds.height;
		
		g2.setColor(GRID_COLOR1);
		
		for (int i = imageBounds.x; i < limitX; i += GRID_SIZE * 2) {
			for (int j = imageBounds.y; j < limitY; j += GRID_SIZE * 2) {
				g2.fillRect(i, j, 
						i + GRID_SIZE > limitX ? limitX - i : GRID_SIZE,
								j + GRID_SIZE > limitY ? limitY - j : GRID_SIZE);
			}
		}
		for (int i = imageBounds.x + GRID_SIZE; i < limitX; i += GRID_SIZE * 2) {
			for (int j = imageBounds.y + GRID_SIZE; j < limitY; j += GRID_SIZE * 2) {
				g2.fillRect(i, j, 
						i + GRID_SIZE > limitX ? limitX - i : GRID_SIZE,
								j + GRID_SIZE > limitY ? limitY - j : GRID_SIZE);
			}
		}
		
		g2.setColor(GRID_COLOR2);
		
		for (int i = imageBounds.x + GRID_SIZE; i < limitX; i += GRID_SIZE * 2) {
			for (int j = imageBounds.y; j < limitY; j += GRID_SIZE * 2) {
				g2.fillRect(i, j, 
						i + GRID_SIZE > limitX ? limitX - i : GRID_SIZE,
								j + GRID_SIZE > limitY ? limitY - j : GRID_SIZE);
			}
		}
		for (int i = imageBounds.x; i < limitX; i += GRID_SIZE * 2) {
			for (int j = imageBounds.y + GRID_SIZE; j < limitY; j += GRID_SIZE * 2) {
				g2.fillRect(i, j, 
						i + GRID_SIZE > limitX ? limitX - i : GRID_SIZE,
								j + GRID_SIZE > limitY ? limitY - j : GRID_SIZE);
			}
		}
	}
	
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		requestFocusInWindow();
		if (selectedType == null) {
			System.arraycopy(editor.getImage().getUVCoords(), 0, oldUVCoords, 0, oldUVCoords.length);

			// we want it to use the real coordinates here
			Rectangle bounds = getUVBounds(editor.getImage().getUVCoords(), getImageRect(editor.getImage()), false);
			Point p = e.getPoint();
			int pointRadius = Math.round(POINT_SIZE + ((zoomLevel - 1) / SCALING_FACTOR));
			
			if (DraggableType.RESIZE_TOP_LEFT.getPointRect(bounds, pointRadius).contains(p)) {
				selectedType = DraggableType.RESIZE_TOP_LEFT;
			}
			else if (DraggableType.RESIZE_TOP_RIGHT.getPointRect(bounds, pointRadius).contains(p)) {
				selectedType = DraggableType.RESIZE_TOP_RIGHT;
			}
			else if (DraggableType.RESIZE_BOTTOM_RIGHT.getPointRect(bounds, pointRadius).contains(p)) {
				selectedType = DraggableType.RESIZE_BOTTOM_RIGHT;
			}
			else if (DraggableType.RESIZE_BOTTOM_LEFT.getPointRect(bounds, pointRadius).contains(p)) {
				selectedType = DraggableType.RESIZE_BOTTOM_LEFT;
			}
			else if (DraggableType.RESIZE_TOP.getPointRect(bounds, pointRadius).contains(p)) {
				selectedType = DraggableType.RESIZE_TOP;
			}
			else if (DraggableType.RESIZE_BOTTOM.getPointRect(bounds, pointRadius).contains(p)) {
				selectedType = DraggableType.RESIZE_BOTTOM;
			}
			else if (DraggableType.RESIZE_LEFT.getPointRect(bounds, pointRadius).contains(p)) {
				selectedType = DraggableType.RESIZE_LEFT;
			}
			else if (DraggableType.RESIZE_RIGHT.getPointRect(bounds, pointRadius).contains(p)) {
				selectedType = DraggableType.RESIZE_RIGHT;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (mouseX - lastClickMouseX != 0 || mouseY - lastClickMouseY != 0) {
			float[] lastUVCoords = new float[4];
			System.arraycopy(oldUVCoords, 0, lastUVCoords, 0, lastUVCoords.length);
			
			float[] newUVCoords = new float[4];
			System.arraycopy(editor.getImage().getUVCoords(), 0, newUVCoords, 0, newUVCoords.length);
			
			
			editor.addCommandAction(new ComponentValueAction<float[]>(lastUVCoords, newUVCoords, new ComponentValueListener<float[]>() {
				@Override
				public void valueChanged(float[] value) {
					float[] uvCoords = editor.getImage().getUVCoords();
					uvCoords[0] = value[0];
					uvCoords[1] = value[1];
					uvCoords[2] = value[2];
					uvCoords[3] = value[3];
					editor.updateCoordinates();
					editor.updateDisplay();
				}
			}));
		}
		
		selectedType = null;
		// fix previous workaround
		if (isArtificialMoving) {
			lastClickMouseX = mouseX;
			lastClickMouseY = mouseY;
		}
		isArtificialMoving = false;
		
		editor.updateDisplay();
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if (selectedType != null) {
			Rectangle imageRect = getImageRect(editor.getImage());
			// we want it to use the real coordinates here
			Rectangle bounds = getUVBounds(editor.getImage().getUVCoords(), imageRect, false);
			selectedType.process(bounds, e.getX() - mouseX, e.getY() - mouseY);
			setUVCoordsFromBounds(bounds, imageRect);
			
			editor.updateDisplay();
			editor.updateCoordinates();
		}
		mouseX = e.getX();
		mouseY = e.getY();
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (isArtificialMoving) {
			Rectangle imageRect = getImageRect(editor.getImage());
			// we want it to use the real coordinates here
			Rectangle bounds = getUVBounds(editor.getImage().getUVCoords(), imageRect, false);
			selectedType.process(bounds, e.getX() - mouseX, e.getY() - mouseY);
			setUVCoordsFromBounds(bounds, imageRect);
			
			editor.updateDisplay();
			editor.updateCoordinates();
		}
		else {
			Rectangle bounds = getUVBounds(editor.getImage().getUVCoords(), getImageRect(editor.getImage()), true);
			Point p = e.getPoint();
			int pointRadius = Math.round(POINT_SIZE + ((zoomLevel - 1) / SCALING_FACTOR));
			
			if (DraggableType.RESIZE_TOP_LEFT.getPointRect(bounds, pointRadius).contains(p)) {
				setCursor(DraggableType.RESIZE_TOP_LEFT.getCursor());
			}
			else if (DraggableType.RESIZE_TOP_RIGHT.getPointRect(bounds, pointRadius).contains(p)) {
				setCursor(DraggableType.RESIZE_TOP_RIGHT.getCursor());
			}
			else if (DraggableType.RESIZE_BOTTOM_RIGHT.getPointRect(bounds, pointRadius).contains(p)) {
				setCursor(DraggableType.RESIZE_BOTTOM_RIGHT.getCursor());
			}
			else if (DraggableType.RESIZE_BOTTOM_LEFT.getPointRect(bounds, pointRadius).contains(p)) {
				setCursor(DraggableType.RESIZE_BOTTOM_LEFT.getCursor());
			}
			else if (DraggableType.RESIZE_TOP.getPointRect(bounds, pointRadius).contains(p)) {
				setCursor(DraggableType.RESIZE_TOP.getCursor());
			}
			else if (DraggableType.RESIZE_BOTTOM.getPointRect(bounds, pointRadius).contains(p)) {
				setCursor(DraggableType.RESIZE_BOTTOM.getCursor());
			}
			else if (DraggableType.RESIZE_LEFT.getPointRect(bounds, pointRadius).contains(p)) {
				setCursor(DraggableType.RESIZE_LEFT.getCursor());
			}
			else if (DraggableType.RESIZE_RIGHT.getPointRect(bounds, pointRadius).contains(p)) {
				setCursor(DraggableType.RESIZE_RIGHT.getCursor());
			}
			else {
				setCursor(Cursor.getDefaultCursor());
			}
		}
		
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if (!isArtificialMoving &&
				arg0.getKeyCode() == KeyEvent.VK_G) {
			
			selectedType = DraggableType.COMPONENT;
			lastClickMouseX = mouseX;
			lastClickMouseY = mouseY;
			isArtificialMoving = true;
			System.arraycopy(editor.getImage().getUVCoords(), 0, oldUVCoords, 0, oldUVCoords.length);
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
