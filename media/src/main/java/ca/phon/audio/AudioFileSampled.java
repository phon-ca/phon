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

import java.io.*;

public class AudioFileSampled implements Sampled {

	private final AudioFile audioFile;
	
	public AudioFileSampled(AudioFile audioFile) {
		this.audioFile = audioFile;
	}
		
	@Override
	public int getNumberOfChannels() {
		return audioFile.getNumberOfChannels();
	}

	@Override
	public int getNumberOfSamples() {
		return (int)audioFile.getNumberOfSamples();
	}

	@Override
	public float getSampleRate() {
		return (float)audioFile.getSampleRate();
	}

	@Override
	public double valueForSample(int channel, int sample) {
		double[][] buffer = new double[getNumberOfChannels()][];
		for(int i = 0; i < getNumberOfChannels(); i++) {
			buffer[i] = new double[1];
		}
		try {
			audioFile.seekToSample(sample);
			audioFile.readSamples(buffer, 0, 1);
			
			return buffer[channel][0];
		} catch (IOException e) {
			return 0.0;
		}
	}

	@Override
	public int sampleForTime(float time) {
		return audioFile.sampleIndexForTime(time);
	}

	@Override
	public double valueForTime(int channel, float time) {
		return valueForSample(channel, sampleForTime(time));
	}

	@Override
	public float getStartTime() {
		return 0.0f;
	}

	@Override
	public float getEndTime() {
		return (float)(getNumberOfSamples() / getSampleRate());
	}

	@Override
	public float getLength() {
		return getEndTime() - getStartTime();
	}

	@Override
	public double maximumValue(int channel, int firstSample, int lastSample) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double maximumValue(int channel, float startTime, float endTime) {
		double[] extrema = getWindowExtrema(channel, startTime, endTime);
		return extrema[1];
	}

	@Override
	public double minimumValue(int channel, int firstSample, int lastSample) {
		return 0.0;
	}

	@Override
	public double[] getWindowExtrema(int channel, int firstSample, int lastSample) {
		double[] retVal = new double[2];
		getWindowExtrema(channel, firstSample, lastSample, retVal);
		return retVal;
	}

	@Override
	public void getWindowExtrema(int channel, int firstSample, int lastSample, double[] extrema) {
		int numSamples = (int)(lastSample - firstSample);
		if(numSamples < 0) throw new ArrayIndexOutOfBoundsException();
		
		double[][] data = new double[getNumberOfChannels()][];
		for(int i = 0; i < getNumberOfChannels(); i++) {
			data[i] = new double[numSamples];
		}
		
		try {
			audioFile.seekToSample(firstSample);
			audioFile.readSamples(data, 0, numSamples);
			
			for(int i = 0; i < numSamples; i++) {
				extrema[0] = Math.min(extrema[0], data[channel][i]);
				extrema[1] = Math.max(extrema[1], data[channel][i]);
			}
		} catch (IOException e) {
		}
	}

	public double[][] getWindowExtrema(float firstTime, float endTime) {
		return getWindowExtrema(sampleForTime(firstTime), sampleForTime(endTime));
	}
	
	@Override
	public double[][] getWindowExtrema(int firstSample, int lastSample) {
		int numSamples = (int)(lastSample - firstSample);
		double[][] retVal = new double[getNumberOfChannels()][];
		for(int i = 0; i < getNumberOfChannels(); i++) {
			retVal[i] = new double[2];
		}
		
		double[][] data = new double[getNumberOfChannels()][];
		for(int i = 0; i < getNumberOfChannels(); i++) {
			data[i] = new double[numSamples];
		}
		
		loadSampleData(data, 0, firstSample, numSamples);
		for(int isamp = 0; isamp < numSamples; isamp++) {
			for(int ichan = 0; ichan < getNumberOfChannels(); ichan++) {
				retVal[ichan][0] = Math.min(retVal[ichan][0], data[ichan][isamp]);
				retVal[ichan][1] = Math.max(retVal[ichan][1], data[ichan][isamp]);
			}
		}
		
		return retVal;
	}
	
	@Override
	public double[] getWindowExtrema(int channel, float startTime, float endTime) {
		return getWindowExtrema(channel, sampleForTime(startTime), sampleForTime(endTime));
	}

	@Override
	public void getWindowExtrema(int channel, float startTime, float endTime, double[] extrema) {
		getWindowExtrema(channel, sampleForTime(startTime), sampleForTime(endTime), extrema);
	}

	@Override
	public int loadSampleData(double[][] buffer, int offset, int firstSample, int numSamples)  {
		try {
			audioFile.seekToSample(firstSample);
			return audioFile.readSamples(buffer, offset, numSamples);
		} catch (IOException e) {
			return 0;
		}
	}

}
