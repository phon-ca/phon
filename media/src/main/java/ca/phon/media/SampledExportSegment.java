package ca.phon.media;

import java.io.File;
import java.io.IOException;

import ca.phon.audio.AudioFileEncoding;
import ca.phon.audio.AudioFileType;
import ca.phon.audio.AudioIO;
import ca.phon.audio.AudioIOException;
import ca.phon.audio.Sampled;

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
