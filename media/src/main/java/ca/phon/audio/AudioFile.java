package ca.phon.audio;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.BufferUnderflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.logging.log4j.core.time.PreciseClock;

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
	
	// file as memory mapped buffer
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
	public synchronized void readSamples(double[][] buffer) throws IOException {
		if(buffer.length == 0) // no samples to read
			return;
		if(buffer[0] == null)
			throw new IOException(new NullPointerException("buffer[0]"));
		readSamples(buffer, 0, buffer[0].length);
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
