package sporemodder.utilities.performance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Profiler {

	private final HashMap<String, List<Long>> timesMap = new HashMap<String, List<Long>>();
	private final HashMap<String, Long> currentTimesMap = new HashMap<String, Long>();
	
	public void addProfiler(Profiler profiler) {
		for (String name : profiler.timesMap.keySet()) {
			
			addTimes(name, profiler.timesMap.get(name));
		}
	}
	
	// check if this profile already exists; if so, create a new list; otherwise, add to the existing one
	public void addTime(String name, long time) {
		List<Long> list = timesMap.get(name);
		if (list == null) {
			list = new ArrayList<Long>();
			timesMap.put(name, list);
		}
		list.add(time);
	}
	
	// check if this profile already exists; if so, create a new list; otherwise, add to the existing one
	private void addTimes(String name, List<Long> times) {
		List<Long> list = timesMap.get(name);
		if (list == null) {
			list = new ArrayList<Long>();
			timesMap.put(name, list);
		}
		list.addAll(times);
	}
	
	/**
	 * Starts measuring the time, storing the results in a profile with the given name.
	 * The results won't be added until endMeasure(name) is called.
	 * You can use these methods with the same name multiple times to get the average time.
	 * @param name The name of the profile to measure.
	 */
	public void startMeasure(String name) {
		currentTimesMap.put(name, System.currentTimeMillis());
	}
	
	/**
	 * Ends a measure previously started with startMeasure(name). 
	 * The result will be stored in a profile with the given name. That profile contains
	 * all the times previously measured for that name. You can use getTime(name) to get the average time.
	 * @param name The name of the profile to end the measure.
	 * @return The time measured. This is not the average.
	 */
	public long endMeasure(String name) {
		long endTime = System.currentTimeMillis();
		Long startTime = currentTimesMap.get(name);
		
		if (startTime == null) {
			throw new IllegalArgumentException(name + " is not a section in this profiler.");
		}
		
		long resultTime = endTime - startTime;
		
		addTime(name, resultTime);
		
		return resultTime;
	}
	
	public long getTotalTime() {
		long totalTime = 0;
		
		for (String name : timesMap.keySet()) {
			
			totalTime += getTotalTime(name);
		}
		
		return totalTime;
	}
	
	public long getTotalTime(String name) {
		List<Long> times = timesMap.get(name);
		
		if (times == null) {
			throw new IllegalArgumentException(name + " is not a section in this profiler.");
		}
		
		long sum = 0;
		
		for (long time : times) {
			sum += time;
		}
		
		return sum;
	}
	
	public int getExecutionCount(String name) {
		List<Long> times = timesMap.get(name);
		
		if (times == null) {
			throw new IllegalArgumentException(name + " is not a section in this profiler.");
		}
		
		return times.size();
	}
	
	public long getTime(String name) {
		List<Long> times = timesMap.get(name);
		
		if (times == null) {
			throw new IllegalArgumentException(name + " is not a section in this profiler.");
		}
		
		int count = times.size();
		
		long sum = 0;
		
		for (long time : times) {
			sum += time;
		}
		
		return Math.round(sum / (double) count);
	}
}
