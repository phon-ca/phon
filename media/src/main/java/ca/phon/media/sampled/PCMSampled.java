package ca.phon.media.sampled;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFormat.Encoding;

public class PCMSampled implements Sampled {
	
	/**
	 * Audio format
	 */
	private AudioFileFormat audioFileFormat;
	
	/**
	 * Byte buffer
	 */
	private ByteBuffer byteBuffer;
	
	private float startTime = 0.0f;
	
	public PCMSampled(File file) {
		super();
		
		try {
			mapFile(file);
		} catch (Exception e) {
			e.printStackTrace();
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

		final RandomAccessFile raf = new RandomAccessFile(file, "r");
		final FileChannel fc = raf.getChannel();
		final long len = raf.length() - offset;
		MappedByteBuffer byteBuffer = fc.map(MapMode.READ_ONLY, offset, len);
		if(!byteBuffer.isLoaded()) {
			byteBuffer.load();
		}
		this.byteBuffer = byteBuffer;
	}
	
	public PCMSampled(AudioFileFormat format, ByteBuffer buffer) {
		this.audioFileFormat = format;
		this.byteBuffer = buffer;
	}

	public AudioFileFormat getAudioFileFormat() {
		return this.audioFileFormat;
	}
	
	public ByteBuffer getByteBuffer() {
		return this.byteBuffer;
	}
	
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
	
	protected int get8bitSample(int byteOffset) {
		final byte low = byteBuffer.get(byteOffset);
		final int sample = (low & 0x00ff);
		return sample;
	}
	
	protected int get12bitSample(int byteOffset) {
		byte sampleBytes[] = new byte[2];
		byteBuffer.position(byteOffset);
		byteBuffer.get(sampleBytes, 0, 2);
		
		byte low = sampleBytes[0];
		byte high = sampleBytes[1];

		int sample = ((high << 8) & 0x0F00) + (low & 0x00FF);
		return sample;
	}
	
	protected int get16bitSample(int byteOffset) {
		byte sampleBytes[] = new byte[2];
		byteBuffer.position(byteOffset);
		byteBuffer.get(sampleBytes, 0, 2);
		
		byte low = sampleBytes[0];
		byte high = sampleBytes[1];

		int sample = (high << 8) + (low & 0x00ff);
		return sample;
	}

	protected int get24bitSample(int byteOffset) {
		byte sampleBytes[] = new byte[3];
		byteBuffer.position(byteOffset);
		byteBuffer.get(sampleBytes, 0, 3);
		
		byte low = sampleBytes[0];
		byte mid = sampleBytes[1];
		byte high = sampleBytes[2];

		int sample = (high << 16) + (mid << 8) + (low & 0x00ff);
		return sample;
	}
	
	protected int get32bitSample(int byteOffset) {
		byte sampleBytes[] = new byte[4];
		byteBuffer.position(byteOffset);
		byteBuffer.get(sampleBytes, 0, 4);
		
		byte low = sampleBytes[0];
		byte lowmid = sampleBytes[1];
		byte midhigh = sampleBytes[2];
		byte high = sampleBytes[3];

		int sample = ((high << 24) & 0xFF000000)
				+ ((midhigh << 16) & 0x00FF0000)
				+ ((lowmid << 8) & 0x0000FF00) + (low & 0x000000FF);
		return sample;
	}
	
	protected int byteOffsetForFrame(long frame) {
		return (int)(frame * getAudioFileFormat().getFormat().getFrameSize());
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
		
		int offset = byteOffsetForFrame(startIdx);
		int byteLength = (int)((endIdx-startIdx) * getAudioFileFormat().getFormat().getFrameSize());
		byte[] buffer = new byte[byteLength];
		byteBuffer.position(offset);
		byteBuffer.get(buffer, 0, byteLength);
		
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
		
		for(long i = firstSample; i <= lastSample && i < getNumberOfSamples(); i++) {
			final double value = valueForSample(channel, i);
			extrema[0] = (i == firstSample ? value :
					Math.min(extrema[0], value));
			extrema[1] = (i == firstSample ? value: 
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
