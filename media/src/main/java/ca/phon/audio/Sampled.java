/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.audio;

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
	public int getNumberOfSamples();
	
	/**
	 * Get sample rate.
	 * 
	 * @return sample rate (samples/second)
	 */
	public float getSampleRate();
	
	/**
	 * Get value for sample
	 * 
	 * @param channel
	 * @param sample index
	 */
	public double valueForSample(int channel, int sample);
	
	/**
	 * Convert a time value to a sample index
	 * 
	 * @param time (in seconds)
	 * 
	 * @return sample index or -1 if time is outside Sampled data range
	 */
	public int sampleForTime(float time);
	
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
	 * End time in seconds
	 * 
	 * @return end time in seconds
	 */
	public float getEndTime();
	
	/**
	 * Get end time
	 * 
	 * @return end time in seconds
	 */
	
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
	public double maximumValue(int channel, int firstSample, int lastSample);
	
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
	public double minimumValue(int channel, int firstSample, int lastSample);
	
	public int loadSampleData(double[][] buffer, int offset, int firstSample, int numSamples);
	
	public double[][] getWindowExtrema(int firstSample, int lastSample);
	
	public double[][] getWindowExtrema(float startTime, float endTime);
	
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
	public double[] getWindowExtrema(int channel, int firstSample, int lastSample);
	
	/**
	 * Get min/max values for specified window
	 * 
	 * @param channel
	 * @param firstSample
	 * @param ladSample
	 * @param extrema
	 */
	public void getWindowExtrema(int channel, int firstSample, int lastSample, double[] extrema);
	
	
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
	public void getWindowExtrema(int channel, float startTime, float endTime, double[] extrema);
		
}
