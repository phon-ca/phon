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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class PCMSampled implements Sampled {
	
	private final static Logger LOGGER = Logger.getLogger(PCMSampled.class.getName());
	
	/**
	 * Audio format
	 */
	private AudioFileFormat audioFileFormat;
	
	/**
	 * Random access file
	 */
	private RandomAccessFile raf;
	
	private int dataOffset = -1;
	
	private long length = 0L;
	
	private float startTime = 0.0f;
	
	public PCMSampled(File file) throws IOException {
		super();
		
		try {
			mapFile(file);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	private void mapFile(File file) throws IOException, UnsupportedAudioFileException {
		// load audio file format
		audioFileFormat = AudioSystem.getAudioFileFormat(file);
		
		// find the data offset position in the given wav file
		final InputStream is = new FileInputStream(file);
		final byte[] buffer = new byte[4];
		int offset = 0;
		String txt = "";
		while (!txt.equals("data")) {
			offset += is.read(buffer, 0, 4);
			txt = new String(buffer);
		}
		is.close();

		raf = new RandomAccessFile(file, "r");
		dataOffset = offset+4;
		length = raf.length() - offset;
	}
	
//	public PCMSampled(AudioFileFormat format, RandomAccessFile raf) {
//		this.audioFileFormat = format;
//		this.raf = raf;
//	}

	public AudioFileFormat getAudioFileFormat() {
		return this.audioFileFormat;
	}
	
//	public ByteBuffer getByteBuffer() {
//		return this.byteBuffer;
//	}
	
	@Override
	public int getNumberOfChannels() {
		return getAudioFileFormat().getFormat().getChannels();
	}

	@Override
	public long getNumberOfSamples() {
		return getAudioFileFormat().getFrameLength();
	}

	@Override
	public float getSampleRate() {
		return getAudioFileFormat().getFormat().getSampleRate();
	}
	
	@Override
	public int getSampleSize() {
		return getAudioFileFormat().getFormat().getSampleSizeInBits();
	}
	
	@Override
	public boolean isSigned() {
		return getAudioFileFormat().getFormat().getEncoding() == Encoding.PCM_SIGNED;
	}

	@Override
	public double valueForSample(int channel, long sample) {
		final int bytesPerSample = getAudioFileFormat().getFormat().getFrameSize() / 
				getAudioFileFormat().getFormat().getChannels();
		
		int byteIndex = byteOffsetForFrame(sample);
		byteIndex += channel * bytesPerSample;
		
		return getSample(byteIndex);
	}

	@Override
	public long sampleForTime(float time) {
		final AudioFileFormat fileFormat = getAudioFileFormat();
		final float frameRate = fileFormat.getFormat().getFrameRate();
		final long sampleIdx = Math.round(frameRate * time);
		return sampleIdx;
	}

	@Override
	public double valueForTime(int channel, float time) {
		final long sampleIndex = sampleForTime(time);
		return valueForSample(channel, sampleIndex);
	}
	
	protected int getSample(int byteOffset) {
		int retVal = 0;
		
		final AudioFormat format = getAudioFileFormat().getFormat();
		if(format.getEncoding() != Encoding.PCM_SIGNED &&
				format.getEncoding() != Encoding.PCM_UNSIGNED)
			throw new IllegalStateException("Unknown format");
		
		switch(format.getSampleSizeInBits()) {
		case 8:
			retVal = get8bitSample(byteOffset);
			break;
			
		case 12:
			retVal = get12bitSample(byteOffset);
			break;
			
		case 16:
			retVal = get16bitSample(byteOffset);
			break;
			
		case 24:
			retVal = get24bitSample(byteOffset);
			break;
			
		case 32:
			retVal = get32bitSample(byteOffset);
			break;
			
		default:
			break;
		}
		
		return retVal;
	}
	
	protected int toSample(int idx, byte[] frames) {
		int retVal = 0;
		
		final AudioFormat format = getAudioFileFormat().getFormat();
		if(format.getEncoding() != Encoding.PCM_SIGNED &&
				format.getEncoding() != Encoding.PCM_UNSIGNED)
			throw new IllegalStateException("Unknown format");
		
		switch(format.getSampleSizeInBits()) {
		case 8:
			retVal = to8bitSample(idx, frames);
			break;
			
		case 12:
			retVal = to12bitSample(idx, frames);
			break;
			
		case 16:
			retVal = to16bitSample(idx, frames);
			break;
			
		case 24:
			retVal = to24bitSample(idx, frames);
			break;
			
		case 32:
			retVal = to32bitSample(idx, frames);
			break;
			
		default:
			break;
		}
		
		return retVal;
	}
	
	protected byte[] getBytes(int byteOffset, int len) {
		byte[] retVal = new byte[len];
		
		try {
			raf.seek(byteOffset);
			raf.read(retVal, 0, len);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
		return retVal;
	}
	
	protected int get8bitSample(int byteOffset) {
		final byte[] frame = getBytes(byteOffset, 1);
		return to8bitSample(frame);
	}
	
	protected int to8bitSample(byte[] frame) {
		return to8bitSample(0, frame);
	}
	
	protected int to8bitSample(int idx, byte[] frames) {
		return (frames[idx] & 0xff);
	}
	
	protected int get12bitSample(int byteOffset) {
		byte sampleBytes[] = getBytes(byteOffset, 2);
		return to12bitSample(sampleBytes);
	}
	
	protected int to12bitSample(byte[] frame) {
		return to12bitSample(0, frame);
	}
	
	protected int to12bitSample(int idx, byte[] frames) {
		byte low = frames[idx];
		byte high = frames[idx+1];

		int sample = ((high << 8) & 0x0F00) + (low & 0x00FF);
		return sample;
	}
	
	protected int get16bitSample(int byteOffset) {
		byte sampleBytes[] = getBytes(byteOffset, 2);
		return to16bitSamle(sampleBytes);
	}
	
	protected int to16bitSamle(byte[] frame) {
		return to16bitSample(0, frame);
	}
	
	protected int to16bitSample(int idx, byte[] frames) {
		byte low = frames[idx];
		byte high = frames[idx+1];

		int sample = (high << 8) + (low & 0x00ff);
		return sample;
	}

	protected int get24bitSample(int byteOffset) {
		byte sampleBytes[] = getBytes(byteOffset, 3);
		return to24bitSample(sampleBytes);
	}
	
	protected int to24bitSample(byte[] frame) {
		return to24bitSample(0, frame);
	}
	
	protected int to24bitSample(int idx, byte[] frames) {
		byte low = frames[idx];
		byte mid = frames[idx+1];
		byte high = frames[idx+2];

		int sample = (high << 16) + (mid << 8) + (low & 0x00ff);
		return sample;
	}
	
	protected int get32bitSample(int byteOffset) {
		byte sampleBytes[] = getBytes(byteOffset, 4);
		return to32bitSample(sampleBytes);
	}
	
	protected int to32bitSample(byte[] frame) {
		return to32bitSample(0, frame);
	}
	
	protected int to32bitSample(int idx, byte[] frames) {
		byte low = frames[idx];
		byte lowmid = frames[idx+1];
		byte midhigh = frames[idx+2];
		byte high = frames[idx+3];

		int sample = ((high << 24) & 0xFF000000)
				+ ((midhigh << 16) & 0x00FF0000)
				+ ((lowmid << 8) & 0x0000FF00) + (low & 0x000000FF);
		return sample;
	}
	
	protected int byteOffsetForFrame(long frame) {
		return dataOffset + (int)(frame * getAudioFileFormat().getFormat().getFrameSize());
	}
	
	@Override
	public float getStartTime() {
		return startTime;
	}

	@Override
	public float getLength() {
		final AudioFileFormat fileFormat = getAudioFileFormat();
		final float frameRate = fileFormat.getFormat().getFrameRate();
		return (fileFormat.getFrameLength() / frameRate);
	}

	@Override
	public double maximumValue(int channel, long firstSample, long lastSample) {
		double retVal = Double.MIN_VALUE;
		
		for(long i = firstSample; i <= lastSample && i < getNumberOfSamples(); i++) {
			retVal =
					Math.max(retVal, valueForSample(channel, i));
		}
		
		return retVal;
	}

	@Override
	public double maximumValue(int channel, float startTime, float endTime) {
		long startIdx = sampleForTime(startTime);
		long endIdx = sampleForTime(endTime);
		if(endIdx < startIdx) {
			long t = startIdx;
			startIdx = endIdx;
			endIdx = t;
		}
		return maximumValue(channel, startIdx, endIdx);
	}

	@Override
	public double minimumValue(int channel, long firstSample, long lastSample) {
		double retVal = Double.MAX_VALUE;
		
		for(long i = firstSample; i <= lastSample && i < getNumberOfSamples(); i++) {
			retVal =
					Math.min(retVal, valueForSample(channel, i));
		}
		
		return retVal;
	}

	@Override
	public double[] getWindowExtrema(int channel, long firstSample,
			long lastSample) {
		double[] retVal = new double[2];
		getWindowExtrema(channel, firstSample, lastSample, retVal);
		return retVal;
	}

	@Override
	public double[] getWindowExtrema(int channel, float startTime, float endTime) {
		double[] retVal = new double[2];
		getWindowExtrema(channel, startTime, endTime, retVal);
		return retVal;
	}
	
	@Override
	public byte[] getBytes(float startTime, float endTime) {
		long startIdx = sampleForTime(startTime);
		long endIdx = sampleForTime(endTime);
		
		return getBytes(startIdx, endIdx);
	}
	
	public byte[] getBytes(long firstSample, long lastSample) {
		int offset = byteOffsetForFrame(firstSample);
		int byteLength = (int)((lastSample-firstSample) * getAudioFileFormat().getFormat().getFrameSize());
		byte[] buffer = new byte[byteLength];
		
		try {
			raf.seek(offset);
			raf.read(buffer, 0, byteLength);
		} catch(IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
		return buffer;
	}

	@Override
	public void getWindowExtrema(int channel, long firstSample,
			long lastSample, double[] extrema) {
		if(extrema == null || extrema.length != 2) {
			throw new IllegalArgumentException("extrema must be an array of doubles with length 2");
		}
		checkChannelBounds(channel);
		checkSampleBounds(firstSample, lastSample);
		
		byte[] frames = getBytes(firstSample, lastSample);
		int frameSize = getAudioFileFormat().getFormat().getFrameSize();
		int numFrames = frames.length / frameSize;
		final int bytesPerSample = getAudioFileFormat().getFormat().getFrameSize() / 
				getAudioFileFormat().getFormat().getChannels();
		
		for(int i = 0; i < numFrames; i++) {
			final double value = toSample(i*frameSize+channel*bytesPerSample, frames);
			extrema[0] = (i == 0 ? value :
					Math.min(extrema[0], value));
			extrema[1] = (i == 0 ? value: 
					Math.max(extrema[1], value));
		}
	}
	
	@Override
	public void getWindowExtrema(int channel, float startTime, float endTime,
			double[] extrema) {
		checkTimeBounds(startTime, endTime);
		long startIdx = sampleForTime(startTime);
		long endIdx = sampleForTime(endTime);
		getWindowExtrema(channel, startIdx, endIdx, extrema);
	}
	
	private void checkChannelBounds(int channel) {
		if(channel < 0 || channel >= getNumberOfChannels()) {
			throw new ArrayIndexOutOfBoundsException(channel);
		}
	}
	
	private void checkSampleBounds(long firstSample, long lastSample) {
		if(firstSample < 0 || firstSample > getNumberOfSamples()) {
			throw new ArrayIndexOutOfBoundsException((int)firstSample);
		}
		if(lastSample < firstSample || lastSample > getNumberOfSamples()) {
			throw new ArrayIndexOutOfBoundsException((int)lastSample);
		}
	}
	
	private void checkTimeBounds(float startTime, float endTime) {
		long startIdx = sampleForTime(startTime);
		long endIdx = sampleForTime(endTime);
		checkSampleBounds(startIdx, endIdx);
	}

}
