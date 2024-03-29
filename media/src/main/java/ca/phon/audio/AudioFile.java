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
import java.math.*;
import java.nio.*;
import java.nio.channels.FileChannel.MapMode;

/**
 * An audio file. For a list of supported file types see {@link AudioFileType}.
 * For a list of supported encodings see {@link AudioFileEncoding}.
 * 
 * This class encapsulates a memory mapped audio file and should be closed when
 * no longer needed.  Use {@link AudioIO#openAudioFile(File)} to create a new
 * AudioFile object from a file on disk.
 * 
 */
public final class AudioFile implements AutoCloseable, Closeable {

	private File file;

	private RandomAccessFile raf;	
	private MappedByteBuffer mappedBuffer;

	private long dataOffset = -1;
	
	private int numberOfChannels = -1;
	
	private double sampleRate = 0.0;
	
	private long numberOfSamples = 0;

	private AudioFileType audioFileType;

	private AudioFileEncoding audioFileEncoding;
	
	AudioFile(File file, AudioFileInfo info) throws IOException {
		this(file, info.getFileType(), info.getEncoding(), info.getNumberOfChannels(),
				info.getSampleRate(), info.getNumberOfSamples(), info.getDataOffset());
	}

	/**
	 * Constructor
	 */
	AudioFile(File file, AudioFileType fileType, AudioFileEncoding encoding, int numberOfChannels, 
			double sampleRate, long numberOfSamples, long dataOffset) throws IOException {
		super();

		this.file = file;
		this.audioFileType = fileType;
		this.audioFileEncoding = encoding;
		this.numberOfChannels = numberOfChannels;
		this.sampleRate = sampleRate;
		this.numberOfSamples = numberOfSamples;
		this.dataOffset = dataOffset;
		
		raf = new RandomAccessFile(file, "r");
		mappedBuffer = raf.getChannel().map(MapMode.READ_ONLY, dataOffset, raf.getChannel().size() - dataOffset);
	}
	
	@Override
	public void close() throws IOException {
		raf.close();
	}
	
	public File getFile() {
		return file;
	}

	public int getNumberOfChannels() {
		return numberOfChannels;
	}

	public long getDataOffset() {
		return this.dataOffset;
	}
	
	public double getSampleRate() {
		return sampleRate;
	}

	public long getNumberOfSamples() {
		return numberOfSamples;
	}

	public AudioFileType getAudioFileType() {
		return audioFileType;
	}

	public AudioFileEncoding getAudioFileEncoding() {
		return audioFileEncoding;
	}
	
	public int sampleIndexForTime(float time) {
		final double frameRate = getSampleRate();
		final int sampleIdx = (int)Math.round(frameRate * time);
		return sampleIdx;
	}
	
	public float timeForSampleIndex(long sample) {
		final double frameRate = getSampleRate();
		final float time = 
				BigDecimal.valueOf(sample / frameRate).setScale(3, RoundingMode.HALF_UP).floatValue();
		return time;
	}
	
	public float getLength() {
		return (float)(getNumberOfSamples() / getSampleRate());
	}
	
	public int getFrameSize() {
		return (getNumberOfChannels() * getAudioFileEncoding().getBytesPerSample());
	}
	
	public synchronized void seekToTime(float time) {
		long sample = sampleIndexForTime(time);
		seekToSample(sample);
	}
	
	public synchronized void seekToSample(long sample) {
		long byteIdx = sample * getFrameSize();
		mappedBuffer.position((int)byteIdx);
	}
	
	/**
	 * Read buffer[0].length samples
	 * from buffer.length channels starting
	 * from the current position
	 * 
	 * @param buffer
	 */
	public synchronized int readSamples(double[][] buffer) throws IOException {
		if(buffer.length == 0) // no samples to read
			return 0;
		if(buffer[0] == null)
			throw new IOException(new NullPointerException("buffer[0]"));
		return readSamples(buffer, 0, buffer[0].length);
	}
	
	/**
	 * Read length samples from buffer.length channels
	 * Data will be inserted into buffer[channel] starting at offset
	 * 
	 * @param buffer
	 * @param offset
	 * @param length
	 * 
	 * @returns number of samples read
	 */
	public synchronized int readSamples(double[][] buffer, int offset, int numSamples) throws IOException {
		int samplesRead = 0;
		int frameSize = getFrameSize();
		
		byte[] frameData = new byte[frameSize];
		double[] channelSamples = new double[getNumberOfChannels()];
		for(int isamp = 0; isamp < numSamples; isamp++) {
			mappedBuffer.get(frameData);
			try {
				AudioIO.decodeFrame(frameData, 0, getAudioFileEncoding(), getNumberOfChannels(), channelSamples, 0);
			} catch (BufferUnderflowException | UnsupportedFormatException e) {
				throw new IOException(e);
			}
			for(int ichan = 0; ichan < buffer.length; ichan++) {
				buffer[ichan][isamp + offset] = channelSamples[ichan];
			}
			++samplesRead;
		}
		
		return samplesRead;
	}

}
