package sporemodder.utilities.performance;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;

public class ProfilerGraph extends JPanel implements MouseMotionListener, MouseListener {
	
	private static final int RADIUS = 400;
	
	private final int defaultInitialDelay = ToolTipManager.sharedInstance().getInitialDelay();

	protected final ProfilerData data;
	
	protected final List<Shape> shapes = new ArrayList<Shape>();
	
	private Shape currentShape;
	
	
	public ProfilerGraph(Profiler profiler) {
		super();
		this.data = new ProfilerData(profiler);
		
		addMouseMotionListener(this);
		addMouseListener(this);
	}
	
	public ProfilerGraph(ProfilerData data) {
		super();
		this.data = data;
		
		addMouseMotionListener(this);
		addMouseListener(this);
	}

	public ProfilerData getData() {
		return data;
	}
	
	public Profiler getProfiler() {
		return data.getProfiler();
	}

	public void setProfiler(Profiler profiler) {
		data.setProfiler(profiler);
		reset();
	}

	public List<String> getProfileNames() {
		return data.getProfileNames();
	}

	public HashMap<String, Color> getColorLegend() {
		return data.getColorLegend();
	}

	private void reset() {
		shapes.clear();
	}
	
	public void setProfiles(String[] profiles) {
		reset();

		data.setProfiles(profiles);
	}
	
	public void updateShapes() {
		updateShapes(RADIUS, RADIUS);
	}
	
	protected void updateShapes(int totalWidth, int totalHeight) {
		// data to create the shapes
        int radius = RADIUS;

        int x = (totalWidth - radius) / 2;
        int y = (totalHeight - radius) / 2;
        
        float startAt = 0;
        
        shapes.clear();
        
        int len = data.getProfilesCount();
		
		for (int i = 0; i < len; i++) {
			
			float proportion = data.getProfileProportion(i);
			
			// Create shape
			float extent = 360f * proportion;
			shapes.add(new Arc2D.Double(x, y, radius, radius, startAt, extent, Arc2D.PIE));
			
			startAt += extent;
			
		}
	}
	
	protected Dimension getDefaultPreferredSize() {
		return super.getPreferredSize();
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(RADIUS, RADIUS);
	}
	
	protected String getTimeString(long time) {
		return time + " ms (" + String.format("%.3f", time / 1000f) + " s)";
	}
	
	// gets index of the shape
	protected String generateTooltipText(int index) {
		
		String name = data.getProfileName(index);
		long time = data.getProfileTime(index);
		int executionCount = data.getProfiler().getExecutionCount(name);
		
		return generateTooltipText(name, data.getProfileProportion(index), time, executionCount);
	}
	
	protected String generateTooltipText(String name, float proportion, long time, int executionCount) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("<html>");
		sb.append(name);
		sb.append(" (");
		sb.append(String.format("%.3f", proportion * 100));
		sb.append("%)");
		sb.append("<br>");
		sb.append("Average: ");
		sb.append(getTimeString(time));
		sb.append("<br>");
		sb.append("Executed ");
		sb.append(executionCount);
		sb.append(" time");
		if (executionCount != 1) {
			sb.append("s");
		}
		sb.append("</html>");
		
		return sb.toString();
	}
	
	protected String generateAverageTooltipText(String name, long time, int executionCount) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("<html>");
		sb.append(name);
		sb.append("<br>");
		sb.append("Average: ");
		sb.append(getTimeString(time));
		sb.append("<br>");
		sb.append("Executed ");
		sb.append(executionCount);
		sb.append(" time");
		if (executionCount != 1) {
			sb.append("s");
		}
		sb.append("</html>");
		
		return sb.toString();
	}
	
	protected void paint(Graphics2D g2d) {
		for (int i = 0; i < shapes.size(); i++) {
			
			g2d.setColor(data.getProfileColor(i));
			g2d.fill(shapes.get(i));
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		updateShapes(getWidth(), getHeight());
		
		Graphics2D g2d = (Graphics2D) g.create();
		
		g2d.setRenderingHint(
			    RenderingHints.KEY_ANTIALIASING,
			    RenderingHints.VALUE_ANTIALIAS_ON);
		
		paint(g2d);
	}
	

	@Override
	public void mouseDragged(MouseEvent ev) {
	}
	

	@Override
	public void mouseMoved(MouseEvent ev) {
		
		Shape hoveredShape = null;
		int index = 0;
		
		for (Shape shape : shapes) {
			if (shape.contains(ev.getX(), ev.getY())) {
				hoveredShape = shape;
				break;
			}
			index++;
		}
		
		if (hoveredShape != currentShape) {
			
			if (hoveredShape != null) {
				setToolTipText(generateTooltipText(index));
				
			}
			else {
				setToolTipText(null);
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		ToolTipManager.sharedInstance().setInitialDelay(100);
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		ToolTipManager.sharedInstance().setInitialDelay(defaultInitialDelay);
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

}

