package ca.phon.media;

import java.io.File;
import java.io.IOException;

import ca.phon.audio.AudioFile;
import ca.phon.audio.AudioFileEncoding;
import ca.phon.audio.AudioFileSampled;
import ca.phon.audio.AudioFileType;
import ca.phon.audio.AudioIO;
import ca.phon.audio.AudioIOException;
import ca.phon.audio.Sampled;

public class SampledExportSegment extends ExportSegment {

	private Sampled samples;
	
	private AudioFileType fileType;
	
	private AudioFileEncoding encoding;
	
	public SampledExportSegment(Sampled samples, AudioFileType type, AudioFileEncoding encoding) {
		super();
		
		this.samples = samples;
		this.fileType = fileType;
		this.encoding = encoding;
	}
	
	@Override
	public void exportSegment(File file, float startTime, float endTime) throws IOException {
		try {
			AudioIO.writeSamplesToFile(samples, samples.sampleForTime(startTime),
					samples.sampleForTime(endTime) - samples.sampleForTime(startTime),
					AudioFileEncoding.LINEAR_16_LITTLE_ENDIAN, file);
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
