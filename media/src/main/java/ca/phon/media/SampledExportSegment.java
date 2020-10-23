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

/**
 * Save a segment of sampled audio to given file using fileType and encoding
 * provided during construction. If an encoding which is not supported
 * is selected for the given fileType it is adjusted to default 16bit
 * encodings for the given fileType.
 */
public class SampledExportSegment extends ExportSegment {

	private final static AudioFileEncoding DEFAULT_WAV_ENCODING = AudioFileEncoding.LINEAR_16_LITTLE_ENDIAN;
	private final static AudioFileEncoding DEFAULT_AIFF_ENCODING = AudioFileEncoding.LINEAR_16_BIG_ENDIAN;
	
	private Sampled samples;
	
	private AudioFileType fileType;
	
	private AudioFileEncoding encoding;
	
	public SampledExportSegment(Sampled samples, AudioFileType type, AudioFileEncoding encoding) {
		super();
		
		this.samples = samples;
		this.fileType = type;
		this.encoding = checkEncoding(encoding);
	}
	
	private AudioFileEncoding checkEncoding(AudioFileEncoding encoding) {
		switch(encoding) {
		// use default if any of these
		case ALAW:
		case MULAW:
		case EXTENDED:
			if(fileType == AudioFileType.WAV) {
				encoding = DEFAULT_WAV_ENCODING;
			} else if(fileType == AudioFileType.AIFC || fileType == AudioFileType.AIFF) {
				encoding = DEFAULT_AIFF_ENCODING;
			}
			break;
			
		default:
			break;
		}
		
		return encoding;
	}
	
	@Override
	public void exportSegment(File file, float startTime, float endTime) throws IOException {
		try {
			AudioIO.writeSamplesToFile(samples, samples.sampleForTime(startTime),
					samples.sampleForTime(endTime) - samples.sampleForTime(startTime),
					fileType, encoding, file);
		} catch (AudioIOException e) {
			throw new IOException(e);
		}
	}

	@Override
	public AudioFileType getFileType() {
		return fileType;
	}

	@Override
	public AudioFileEncoding getEncoding() {
		return encoding;
	}	

}
