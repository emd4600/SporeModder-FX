package sporemodder.utilities;

public class Timer {

	// Found in http://www.javaworld.com/article/2076241/build-ci-sdlc/tweak-your-io-performance-for-faster-runtime.html
	// A simple "stopwatch" class with millisecond accuracy
	protected long startTime, endTime;
	
	public void start()   {  startTime = System.currentTimeMillis();       }
	public void stop()    {  endTime   = System.currentTimeMillis();       }
	public long getTime() {  return endTime - startTime;                   }
	
	public void reset() {
		startTime = 0;
		endTime = 0;
	}

	
	public static class NanoTimer extends Timer {
		public void start()   {  startTime = System.nanoTime();       }
		public void stop()    {  endTime   = System.nanoTime();       }
	}
}
