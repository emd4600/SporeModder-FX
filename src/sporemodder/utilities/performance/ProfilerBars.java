package sporemodder.utilities.performance;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class ProfilerBars extends ProfilerGraph {
	
	private static final int SEPARATOR_SIZE = 2;
	private static final int BAR_HEIGHT = 15;
	private static final int AVERAGE_BAR_HEIGHT = 25;
	private static final int GAP_BETWEEN_PROFILERS = 4;
	private static final int GAP_BETWEEN_PROFILES = 8;
	
	private static final Color AVERAGE_COLOR_1 = new Color(0x73B4C9);
	private static final Color AVERAGE_COLOR_2 = new Color(0x37778C);
	
	protected final ProfilerData otherData;
	
	protected String name;
	protected String otherName;
	protected String averageProfileName;
	
	public ProfilerBars(Profiler profiler, Profiler otherProfiler, String name, String otherName) {
		super(profiler);
		this.otherData = new ProfilerData(otherProfiler);
		
		this.name = name;
		this.otherName = otherName;
	}
	
	public ProfilerBars(ProfilerData data, ProfilerData otherData, String name, String otherName) {
		super(data);
		this.otherData = otherData;
		
		this.name = name;
		this.otherName = otherName;
	}
	
	@Override
	public void setProfiles(String[] profiles) {
		super.setProfiles(profiles);
		
		otherData.setProfiles(profiles);
	}
	
	public void setAverageProfileName(String name) {
		this.averageProfileName = name;
	}
	
	@Override
	public Dimension getPreferredSize() {
		int nShapes = shapes.size();
		
		if (nShapes == 0) {
			return getDefaultPreferredSize();
		}
		
		int width = 0;
		for (Shape shape : shapes) {
			Rectangle bounds = shape.getBounds();
			if (bounds.getWidth() > width) {
				width = (int) bounds.getWidth();
			}
		}
		
		int height = nShapes * BAR_HEIGHT + nShapes * GAP_BETWEEN_PROFILES + (nShapes / 2) * GAP_BETWEEN_PROFILERS;
		
		if (averageProfileName != null) {
			height += 2 * AVERAGE_BAR_HEIGHT +GAP_BETWEEN_PROFILERS + 2 * GAP_BETWEEN_PROFILES + SEPARATOR_SIZE;
		}
		
		return new Dimension(width, height);
	}
	

	@Override
	protected void paint(Graphics2D g2d) {
		int count = data.getProfilesCount();
		
		for (int i = 0; i < count; i++) {
			
			g2d.setColor(data.getProfileColor(i));
			g2d.fill(shapes.get(i * 2));
			
			g2d.setColor(otherData.getProfileColor(i));
			g2d.fill(shapes.get(i * 2 + 1));
		}
		
		if (averageProfileName != null) {
			
			Shape shape1 = shapes.get(count*2);
			Shape shape2 = shapes.get(count*2 + 1);
			
			Rectangle bounds1 = shape1.getBounds();
			Rectangle bounds2 = shape2.getBounds();
			
			g2d.setPaint(new GradientPaint(
					0, bounds1.y, AVERAGE_COLOR_1, 
					0, bounds1.y + bounds1.height, AVERAGE_COLOR_2));
			
			g2d.fill(shape1);
			
			
			g2d.setPaint(new GradientPaint(
					0, bounds2.y, AVERAGE_COLOR_1, 
					0, bounds2.y + bounds2.height, AVERAGE_COLOR_2));
			
			g2d.fill(shape2);
			
			
			g2d.setPaint(null);
			
			g2d.setColor(Color.black);
			g2d.fillRect(0, AVERAGE_BAR_HEIGHT * 2 + GAP_BETWEEN_PROFILES + GAP_BETWEEN_PROFILERS, getWidth(), SEPARATOR_SIZE);
		}
	}
	
	@Override
	protected void updateShapes(int totalWidth, int totalHeight) {

		shapes.clear();
		
		// first, get longest time
		long longestTime = 0;
		int count = data.getProfilesCount();
		
		for (int i = 0; i < count; i++) {
			long time = data.getProfileTime(i);
			if (time > longestTime) {
				longestTime = time;
			}
			
			time = otherData.getProfileTime(i);
			if (time > longestTime) {
				longestTime = time;
			}
		}
		
		if (averageProfileName != null) {
			long time = data.getProfiler().getTime(averageProfileName);
			if (time > longestTime) {
				longestTime = time;
			}
			
			time = otherData.getProfiler().getTime(averageProfileName);
			if (time > longestTime) {
				longestTime = time;
			}
		}
		
		
		double maxBarWidth = totalWidth;
		
		double x = 0;
		double y = 0;
		
		// to store average times, they must go at the end of the list
		List<Shape> tempShapes = null;
		
		if (averageProfileName != null) {
			tempShapes = new ArrayList<Shape>();
			
			tempShapes.add(new Rectangle2D.Double(x, y, 
					(data.getProfiler().getTime(averageProfileName) / (double)longestTime) * maxBarWidth, AVERAGE_BAR_HEIGHT));
			y += AVERAGE_BAR_HEIGHT + GAP_BETWEEN_PROFILERS;
			
			tempShapes.add(new Rectangle2D.Double(x, y, 
					(otherData.getProfiler().getTime(averageProfileName) / (double)longestTime) * maxBarWidth, AVERAGE_BAR_HEIGHT));
			y += AVERAGE_BAR_HEIGHT + GAP_BETWEEN_PROFILES + SEPARATOR_SIZE + GAP_BETWEEN_PROFILES;
		}
		
		for (int i = 0; i < count; i++) {
			
			shapes.add(new Rectangle2D.Double(x, y, (data.getProfileTime(i) / (double)longestTime) * maxBarWidth, BAR_HEIGHT));
			y += BAR_HEIGHT + GAP_BETWEEN_PROFILERS;
			
			shapes.add(new Rectangle2D.Double(x, y, (otherData.getProfileTime(i) / (double)longestTime) * maxBarWidth, BAR_HEIGHT));
			y += BAR_HEIGHT + GAP_BETWEEN_PROFILES;
		}
		
		if (tempShapes != null) {
			shapes.addAll(tempShapes);
		}
	}
	
	@Override
	protected String generateTooltipText(int index) {
		
		int count = data.getProfilesCount();
		
		ProfilerData realData = index % 2 == 0 ? data : otherData;
		
		index = index / 2;
		
		if (averageProfileName != null && index >= count) {
			
			long time = realData.getProfiler().getTime(averageProfileName);
			int executionCount = realData.getProfiler().getExecutionCount(averageProfileName);
			
			return generateAverageTooltipText(realData == data ? "OLD METHOD" : "NEW METHOD", time, executionCount);
		}
		else {
			
			String name = realData.getProfileName(index);
			long time = realData.getProfileTime(index);
			int executionCount = realData.getProfiler().getExecutionCount(name);
			
			name = (realData == data ? this.name : this.otherName) + ": " + name;
			
			return generateTooltipText(name, realData.getProfileProportion(index), time, executionCount);
		}
	}
}
