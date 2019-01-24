/****************************************************************************
* Copyright (C) 2019 Eric Mor
*
* This file is part of SporeModder FX.
*
* SporeModder FX is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
****************************************************************************/
package sporemodder.view;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/**
 * Taken from https://stackoverflow.com/questions/16925612/how-to-resize-component-with-mouse-drag-in-javafx#16925941
 * @author Andy Till
 *
 */
public class DragResizer {
	
	public static enum DragSide {LEFT, RIGHT, TOP, BOTTOM};
	
	/**
     * The margin around the control that a user can click in to start resizing
     * the region.
     */
    private static final int RESIZE_MARGIN = 5;

    private final Region region;

    private double x;
    private double y;

    private boolean initMinWidth;

    private boolean dragging;
    
    private final Set<DragSide> dragSides = new HashSet<DragSide>();
    private DragSide currentDragZone = null;

    private DragResizer(Region aRegion, DragSide ... dragSides) {
        region = aRegion;
        
        this.dragSides.addAll(Arrays.asList(dragSides));
    }

    public static void makeResizable(Region region, DragSide ... dragSides) {
        final DragResizer resizer = new DragResizer(region, dragSides);

        region.setOnMousePressed(resizer::mousePressed);
        region.setOnMouseDragged(resizer::mouseDragged);
        region.setOnMouseMoved(resizer::mouseOver);
        region.setOnMouseReleased(resizer::mouseReleased);
        region.setOnMouseExited(resizer::mouseExited);
    }

    protected void mouseReleased(MouseEvent event) {
        dragging = false;
        region.setCursor(Cursor.DEFAULT);
    }

    protected void mouseOver(MouseEvent event) {
    	if (dragging) return;
    	
    	if (event.getSource() == region) {
    		DragSide side = getDraggableZone(event);
    		if (side == null) {
    			region.setCursor(Cursor.DEFAULT);
    		}
    		else {
    		switch (side) {
				case BOTTOM:
					region.setCursor(Cursor.S_RESIZE);
					break;
				case LEFT:
					region.setCursor(Cursor.W_RESIZE);
					break;
				case RIGHT:
					region.setCursor(Cursor.E_RESIZE);
					break;
				case TOP:
					region.setCursor(Cursor.N_RESIZE);
					break;
				default:
					region.setCursor(Cursor.DEFAULT);
	    		}
    		}
    	}
        else {
            region.setCursor(Cursor.DEFAULT);
        }
    }
    
    protected void mouseExited(MouseEvent event) {
    	region.setCursor(Cursor.DEFAULT);
    }

//    protected boolean isInDraggableZone(MouseEvent event) {
//        return event.getX() > (region.getWidth() - RESIZE_MARGIN);
//    }
    
    protected DragSide getDraggableZone(MouseEvent event) {
    	if (dragSides.contains(DragSide.RIGHT) && 
    			event.getX() > (region.getWidth() - RESIZE_MARGIN) &&
    			event.getX() < (region.getWidth() + RESIZE_MARGIN)) {
    		
    		return DragSide.RIGHT;
    	}
    	else if (dragSides.contains(DragSide.LEFT) && 
    			event.getX() > (-RESIZE_MARGIN) &&
    			event.getX() < (+RESIZE_MARGIN)) {

    		return DragSide.LEFT;
    	}
    	if (dragSides.contains(DragSide.BOTTOM) && 
    			event.getY() > (region.getHeight() - RESIZE_MARGIN) &&
    			event.getY() < (region.getHeight() + RESIZE_MARGIN)) {
    		
    		return DragSide.BOTTOM;
    	}
    	else if (dragSides.contains(DragSide.TOP) && 
    			event.getY() > (-RESIZE_MARGIN) &&
    			event.getY() < (+RESIZE_MARGIN)) {

    		return DragSide.TOP;
    	}
    	else {
    		return null;
    	}
    }

    protected void mouseDragged(MouseEvent event) {
        if(!dragging) {
            return;
        }
        
        if (currentDragZone == DragSide.RIGHT) {
        	double mousex = event.getScreenX();
            region.setMinWidth(region.getMinWidth() + (mousex - x));
            x = mousex;
        }
        else if (currentDragZone == DragSide.LEFT) {
        	double mousex = event.getScreenX();
            region.setMinWidth(region.getMinWidth() - (mousex - x));
            x = mousex;
        }
        else if (currentDragZone == DragSide.BOTTOM) {
        	double mousey = event.getScreenY();
            region.setMinHeight(region.getMinHeight() + (mousey - y));
            y = mousey;
        }
        else if (currentDragZone == DragSide.TOP) {
        	double mousey = event.getScreenY();
            region.setMinHeight(region.getMinHeight() - (mousey - y));
            y = mousey;
        }
    }

    protected void mousePressed(MouseEvent event) {

    	currentDragZone = getDraggableZone(event);
    	
        // ignore clicks outside of the draggable margin
        if(currentDragZone == null) {
            return;
        }

        dragging = true;

        // make sure that the minimum height is set to the current height once,
        // setting a min height that is smaller than the current height will
        // have no effect
        if (!initMinWidth) {
            if (dragSides.contains(DragSide.LEFT) || dragSides.contains(DragSide.RIGHT)) {
            	region.setMinWidth(region.getWidth());
            }
            if (dragSides.contains(DragSide.TOP) || dragSides.contains(DragSide.BOTTOM)) {
            	region.setMinHeight(region.getHeight());
            }
            initMinWidth = true;
        }

        x = event.getScreenX();
        y = event.getScreenY();
    }
}
