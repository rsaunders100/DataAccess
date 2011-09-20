package com.mig.dataaccess.example;

import java.util.Calendar;
import java.util.Date;



/**
 * Use to check check if a threshold has expired since a last event.
 *  e.g. 5 mins has expired since the last time some data has been downloaded.
 *  
 *  This isnt actually used any more, but i left this in because its usefull.
 * 
 * @author rob
 *
 */
public class SimpleTimerThreshold {
	
	private int _timeoutThresholdSeconds;
	private Date _timeoutDate;
	
	// Private to hide no- paramerter constructor
	@SuppressWarnings("unused")
	private SimpleTimerThreshold() {}
	
	
	/**
	 * Creates a new timer, 
	 * call restartTimer() 
	 * then later check if the amout of time has elapsed
	 * by calling hasTimerPassedThreshold()
	 * 
	 * @param timeThresholdSeconds
	 * 			How long the timer should be.
	 */
	public SimpleTimerThreshold(int timeThresholdSeconds) {
		_timeoutThresholdSeconds = timeThresholdSeconds;
	}
	
	/**
	 *  Call this to start / reset the timer
	 */
	public void restartTimer() {
		
		// Get a refrence to the Calendar singleton
		Calendar calendar = Calendar.getInstance();
		
		// Record the current time
		calendar.setTime(new Date());
		
		// Add on X seconds
		calendar.add(Calendar.SECOND, _timeoutThresholdSeconds);
		
		// Save the expiry time
		_timeoutDate = calendar.getTime();
		
		//Log.v(TAG, "TimeOutDate:" +_timeOutDate.toString()); 
	}
	
	
	/**
	 *  Determins if the given amout of time has expired 
	 *  since the timer was last started / reset.
	 */
	public boolean hasTimerPassedThreshold() {
		
		if (_timeoutDate == null) return false;
		
		// Get the current time
		Date now = new Date();
		
		//Log.v(TAG, "Now:" + now.toString());
		//Log.v(TAG, "TimeOutDate:" + _timeOutDate.toString());
		
		// Compare it to the timeout date
		return (now.after(_timeoutDate));
	}
}

