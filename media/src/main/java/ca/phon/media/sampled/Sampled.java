/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
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
 */
package ca.phon.media.sampled;

public interface Sampled {
	
	/**
	 * Get the number of channels.
	 * 
	 * @return number of channels
	 */
	public int getNumberOfChannels();
	
	/**
	 * Return the number of samples
	 * 
	 * @return number of samples
	 */
	public long getNumberOfSamples();
	
	/**
	 * Get sample rate.
	 * 
	 * @return sample rate (samples/second)
	 */
	public float getSampleRate();
	
	/**
	 * Get sample size (in bits)
	 * 
	 * @return sample size
	 */
	public int getSampleSize();
	
	/**
	 * Is data signed or unsigned
	 * 
	 * @return is data signed
	 */
	public boolean isSigned();
	
	/**
	 * Get value for sample
	 * 
	 * @param channel
	 * @param sample index
	 */
	public double valueForSample(int channel, long sample);
	
	/**
	 * Convert a time value to a sample index
	 * 
	 * @param time (in seconds)
	 * 
	 * @return sample index or -1 if time is outside Sampled data range
	 */
	public long sampleForTime(float time);
	
	/**
	 * Get value for specified time
	 * 
	 * @param time
	 * 
	 * @return value at specified time
	 */
	public double valueForTime(int channel, float time);
	
	/**
	 * Get start time
	 * 
	 * @return start time in seconds
	 */
	public float getStartTime();
	
	/**
	 * Get length in seconds
	 * 
	 * @return length in seconds
	 */
	public float getLength();
	
	/**
	 * Get the maximum value for the specified sample range
	 * 
	 * @param channel
	 * @param firstSample
	 * @param lastSample
	 * 
	 * @return maximum value for specified range
	 */
	public double maximumValue(int channel, long firstSample, long lastSample);
	
	/**
	 * Get the maximum value for the specified time range
	 * 
	 * @param channel
	 * @param startTime
	 * @param endTime
	 * 
	 * @return maximum value for specified range
	 */
	public double maximumValue(int channel, float startTime, float endTime);
	
	/**
	 * Get the minimum value for the specified sample range
	 * 
	 * @param channel
	 * @param firstSample
	 * @param lastSample
	 * 
	 * @return minimum value for specified range
	 */
	public double minimumValue(int channel, long firstSample, long lastSample);
	
	/**
	 * Get the miminum and maximum values for the specified sample range
	 * 
	 * 
	 * @param channel
	 * @param firstSample
	 * @param lastSample
	 * 
	 * @return min/max values for range
	 */
	public double[] getWindowExtrema(int channel, long firstSample, long lastSample);
	
	/**
	 * Get min/max values for specified window
	 * 
	 * @param channel
	 * @param firstSample
	 * @param ladSample
	 * @param extrema
	 */
	public void getWindowExtrema(int channel, long firstSample, long lastSample, double[] extrema);
	
	
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
	public double[] getWindowExtrema(int channel, float startTime, float endTime);
	
	/**
	 * Get min/max values for specified time range
	 * 
	 * @param channel
	 * @param startTime
	 * @param endTime
	 * @param extrema an array of double[2] where the
	 *  data will be stored
	 */
	public void getWindowExtrema(int channle, float startTime, float endTime, double[] extrema);
	
	/**
	 * Return the raw byte data for the given time range
	 * 
	 * @param startTime
	 * @param length
	 * 
	 * @return raw bytes
	 */
	public byte[] getBytes(float startTime, float endTime);
	
	/**
	 * Get 16-bit samples.
	 * 
	 */
	public byte[] get16BitSample(float startTime, float endTime);
	
}
