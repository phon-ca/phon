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
