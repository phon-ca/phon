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

/**
 * Container for audio file information.
 *
 */
public class AudioFileInfo {
	
	private AudioFileType fileType;
	
	private AudioFileEncoding encoding;
	
	private float sampleRate;

	private int numberOfChannels;
	
	private long dataOffset = 0L;
	
	private long dataChunkSize = 0xffffffff;
	
	private long numberOfSamples = 0L;

	public AudioFileType getFileType() {
		return fileType;
	}

	void setFileType(AudioFileType fileType) {
		this.fileType = fileType;
	}

	public AudioFileEncoding getEncoding() {
		return encoding;
	}

	void setEncoding(AudioFileEncoding encoding) {
		this.encoding = encoding;
	}

	public float getSampleRate() {
		return sampleRate;
	}

	void setSampleRate(float sampleRate) {
		this.sampleRate = sampleRate;
	}

	public int getNumberOfChannels() {
		return numberOfChannels;
	}

	void setNumberOfChannels(int numberOfChannels) {
		this.numberOfChannels = numberOfChannels;
	}

	public long getDataOffset() {
		return dataOffset;
	}

	void setDataOffset(long dataOffset) {
		this.dataOffset = dataOffset;
	}
	
	public long getDataChunkSize() {
		return this.dataChunkSize;
	}
	
	public void setDataChunkSize(long dataChunkSize) {
		 this.dataChunkSize = dataChunkSize;
	}

	public long getNumberOfSamples() {
		return numberOfSamples;
	}

	void setNumberOfSamples(long numberOfSamples) {
		this.numberOfSamples = numberOfSamples;
	}
	
}
