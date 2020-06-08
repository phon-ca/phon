package ca.phon.media;

import ca.phon.media.sampled.Channel;

public interface Sound {
	
	/**
	 * Number of channels in audio file.
	 * @return number of channels
	 */
	public int numberOfChannels();
	
	public float startTime();
	
	public float endTime();
	
	/**
	 * Length of audio in seconds
	 * 
	 * @return
	 */
	public float length();
	
	/**
	 * Get min/max values for specified time range
	 * 
	 * @param channel
	 * @param startTime
	 * @param endTime
	 * 
	 * 
	 * @return min/max values for specified range
	 */
	public double[][] getWindowExtrema(float startTime, float endTime);

}
