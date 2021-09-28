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

import java.io.*;

import ca.phon.audio.*;

public class SampledLongSound extends LongSound {
	
	private Sampled sampled;
	
	private File file;
	
	public SampledLongSound(File file) throws IOException {
		super(file);
		this.file = file;
		
		AudioFile audioFile;
		try {
			audioFile = AudioIO.openAudioFile(file);
			this.sampled = new AudioFileSampled(audioFile);
		} catch (UnsupportedFormatException | InvalidHeaderException e) {
			throw new IOException(e);
		}
		
		putExtension(PlaySegment.class, new SampledPlaySegment(sampled));
		putExtension(ExportSegment.class, new SampledExportSegment(sampled, audioFile.getAudioFileType(), audioFile.getAudioFileEncoding()));
	}

	@Override
	public void close() throws IOException {
		sampled.close();
	}

	public Sampled getSampled() {
		return this.sampled;
	}
	
	@Override
	public int numberOfChannels() {
		return sampled.getNumberOfChannels();
	}

	@Override
	public float length() {
		return sampled.getLength();
	}

	@Override
	public synchronized Sound extractPart(float startTime, float endTime) {
		return new SampledSound(startTime, endTime);
	}

	private class SampledSound implements Sound {
		
		private float startTime;
		
		private float endTime;
		
		public SampledSound(float startTime, float endTime) {
			this.startTime = startTime;
			this.endTime = endTime;
		}

		@Override
		public int numberOfChannels() {
			return sampled.getNumberOfChannels();
		}

		@Override
		public float startTime() {
			return this.startTime;
		}

		@Override
		public float endTime() {
			return this.endTime;
		}

		@Override
		public float length() {
			return endTime - startTime;
		}

		@Override
		public double[][] getWindowExtrema(float startTime, float endTime) {
			return sampled.getWindowExtrema(startTime, endTime);
		}
		
	}
	
}
