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
import java.nio.BufferUnderflowException;

public final class AudioFileSampled extends AbstractSampled {

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
	public void close() throws IOException {
		audioFile.close();
	}

	@Override
	public int loadSampleData(double[][] buffer, int offset, int firstSample, int numSamples)  {
		try {
			audioFile.seekToSample(firstSample);
			return audioFile.readSamples(buffer, offset, numSamples);
		} catch (IOException | BufferUnderflowException e) {
			return 0;
		}
	}

}
