package ca.phon.media.sampled;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import ca.phon.audio.Sampled;
import ca.phon.media.ExportSegment;

public class SampledExportSegment extends ExportSegment {
	
	private final Sampled sampled;
	
	public SampledExportSegment(Sampled sampled) {
		super();
		
		this.sampled = sampled;
	}
	
	public Sampled getSampled() {
		return this.sampled;
	}

	public AudioFormat getAudioFormat() {
		final AudioFormat format = new AudioFormat(getSampled().getSampleRate(), 
				getSampled().getSampleSize(), getSampled().getNumberOfChannels(), 
				getSampled().isSigned(), false);
		return format;
	}
	
	@Override
	public void exportSegment(File file, float startTime, float endTime) throws IOException {
		final byte[] bytes = getSampled().getBytes(startTime, endTime);
		final ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
		final long len = bytes.length;
		final AudioFormat format = getAudioFormat();
		
		final AudioInputStream aio = new AudioInputStream(bin, format, (len/format.getFrameSize()));
		
		setFile(file);
		setExporting(true);
		AudioSystem.write(aio, Type.WAVE, file);
		setExporting(false);
	}

}
