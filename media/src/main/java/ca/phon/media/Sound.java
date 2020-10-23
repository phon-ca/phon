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
package ca.phon.media;

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
