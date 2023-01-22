package sporemodder.utilities.performance;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProfilerData {
	
	private static final float COLOR_HUE_INCREMENT = 0.15f;
	private static final float COLOR_HUE = 0;
	private static final float COLOR_BRIGHTNESS = 0.5f;
	private static final float COLOR_SATURATION = 0.7f;
	
	private Profiler profiler;
	
	private final List<String> profileNames = new ArrayList<String>();
	private final List<Long> profileTimes = new ArrayList<Long>();
	private final List<Float> profileProportions = new ArrayList<Float>();
	
	private final List<Color> profileColors = new ArrayList<Color>();
	
	public ProfilerData(Profiler profiler) {
		this.profiler = profiler;
	}

	public Profiler getProfiler() {
		return profiler;
	}
	
	public int getProfilesCount() {
		return profileNames.size();
	}

	public void setProfiler(Profiler profiler) {
		this.profiler = profiler;
		reset();
	}

	public List<String> getProfileNames() {
		return profileNames;
	}
	
	public String getProfileName(int index) {
		return profileNames.get(index);
	}

	public List<Long> getProfileTimes() {
		return profileTimes;
	}
	
	public long getProfileTime(int index) {
		return profileTimes.get(index);
	}

	public List<Float> getProfileProportions() {
		return profileProportions;
	}
	
	public float getProfileProportion(int index) {
		return profileProportions.get(index);
	}

	public List<Color> getProfileColors() {
		return profileColors;
	}
	
	public Color getProfileColor(int index) {
		return profileColors.get(index);
	}
	
	public HashMap<String, Color> getColorLegend() {
		HashMap<String, Color> result = new HashMap<String, Color>();
		int len = getProfilesCount();
		
		for (int i = 0; i < len; i++) {
			result.put(getProfileName(i), getProfileColor(i));
		}
		
		return result;
	}
	
	public void setProfiles(String[] profiles) {
		
		reset();
		
		float h = COLOR_HUE;
		
		long totalTime = 0;
		
		for (int i = 0; i < profiles.length; i++) {
			
			long time = profiler.getTotalTime(profiles[i]);
			
			profileNames.add(profiles[i]);
			profileTimes.add(time);
			profileColors.add(Color.getHSBColor(h, COLOR_SATURATION, COLOR_BRIGHTNESS));
			
			totalTime += time;
			h += COLOR_HUE_INCREMENT;
		}
		
		
		for (int i = 0; i < profiles.length; i++) {
			
			long time = profileTimes.get(i);
			float proportion = time / (float) totalTime;
			
			profileProportions.add(proportion);
			
		}
	}
	
	private void reset() {
		profileNames.clear();
		profileTimes.clear();
		profileProportions.clear();
		profileColors.clear();
	}
	
}
