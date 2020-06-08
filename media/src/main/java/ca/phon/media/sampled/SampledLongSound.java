package ca.phon.media.sampled;

import java.io.File;
import java.io.IOException;

import ca.phon.audio.AudioFile;
import ca.phon.audio.AudioFiles;
import ca.phon.audio.InvalidHeaderException;
import ca.phon.audio.UnsupportedFormatException;
import ca.phon.media.ExportSegment;
import ca.phon.media.LongSound;
import ca.phon.media.PlaySegment;
import ca.phon.media.Sound;

public class SampledLongSound extends LongSound {
	
	private Sampled sampled;
	
	private File file;
	
	public SampledLongSound(File file) throws IOException {
		super(file);
		this.file = file;
		
		AudioFile audioFile;
		try {
			audioFile = AudioFiles.openAudioFile(file);
			this.sampled = new AudioFileSampled(audioFile);
		} catch (UnsupportedFormatException | InvalidHeaderException e) {
			throw new IOException(e);
		}
		
//		this.sampled = new PCMSampled(file);
		
//		putExtension(PlaySegment.class, new SampledPlaySegment(sampled));
//		putExtension(ExportSegment.class, new SampledExportSegment(sampled));
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
