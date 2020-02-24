/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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

package ca.phon.media.export;

import java.awt.Desktop;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import ca.phon.media.exceptions.PhonMediaException;
import ca.phon.util.PrefHelper;
import ca.phon.worker.PhonTask;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener;

/**
 * Export video and/or audio with the option
 * to transcode the output.  Also able to
 * perform a segment extraction.
 *
 */
public class VLCMediaExporter extends PhonTask {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(VLCMediaExporter.class.getName());
	
	public static enum Preset {
		H264_HIGH("h.264 + mp3 (high)", ".mp4",
				"#transcode{vcodec=h264,venc=x264{cfr=16},scale=1,acodec=mp4a,aenc=aac{strict=-2},ab=128,channels=2,samplerate=44100}"),
		H264_LOW("h.264 + mp3 (low)", ".mp4",
				"#transcode{vcodec=h264,venc=x264{cfr=40},scale=1,acodec=mp3,ab=96,channels=2,samplerate=44100}"),
		WAV("Audio only (wav)", ".wav",
				"#transcode{acodec=s16l,ab=128,channels=2}", ":no-sout-video");
		
		private String presetName;
		
		private String extension;
		
		private String soutOptions;
		
		private String[] mediaOptions;
		
		private Preset(String title, String extension, String soutOptions, String ... mediaOptions) {
			this.presetName = title;
			this.extension = extension;
			this.soutOptions = soutOptions;
			this.mediaOptions = mediaOptions;
		}
		
		public String getPresetName() {
			return this.presetName;
		}
		
		public String getExtension() {
			return this.extension;
		}
		
		public String getSoutOptions() {
			return this.soutOptions;
		}
		
		public String[] getMediaOptions() {
			return this.mediaOptions;
		}
	};

	/**
	 * Encode video
	 */
	private boolean includeVideo = true;

	/**
	 * Encode audio
	 *
	 */
	private boolean includeAudio = true;

	/**
	 * Start time in seconds
	 */
	private float startTime = -1.0f; 
	
	/**
	 * Stop time in seconds
	 */
	private float stopTime = -1.0f;

	/**
	 * Input file
	 */
	private File inputFile;

	/**
	 * Output file
	 */
	private File outputFile;
	
	private List<String> mediaOpts = null;
	
	private final static String PRESET_PROP = VLCMediaExporter.class.getSimpleName() + ".defaultPreset";
	private final static int DEFAULT_PRESET = 0;
	private Preset preset = null;
	
	private MediaPlayer player;
	
	private CountDownLatch latch = new CountDownLatch(1);
	
	/**
	 * Constructor
	 *
	 */
	public VLCMediaExporter() {
		super("Media export");
	}
	
	public VLCMediaExporter(String inputFile, String outputFile) {
		this(inputFile, outputFile,  Preset.values()[PrefHelper.getInt(PRESET_PROP, DEFAULT_PRESET)]);
	}

	/**
	 * Constructor
	 */
	public VLCMediaExporter(String inputFile, String outputFile, Preset preset) {
		this(new File(inputFile), new File(outputFile), preset);
	}

	public VLCMediaExporter(File inputFile, File outputFile) {
		this(inputFile, outputFile, Preset.values()[PrefHelper.getInt(PRESET_PROP, DEFAULT_PRESET)]);
	}
	
	public VLCMediaExporter(File inputFile, File outputFile, Preset preset) {
		super("Media export");
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.preset = preset;
	}

	@Override
	public void performTask() {
		super.setStatus(TaskStatus.RUNNING);
		
		try {
			doExport();
			setStatus(TaskStatus.FINISHED);
		} catch (PhonMediaException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
			super.err = e;
			setStatus(TaskStatus.ERROR);
		}
	}
	
	@Override
	public void shutdown() {
		if(player != null) {
			player.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
				
				@Override
				public void stopped(MediaPlayer mediaPlayer) {
					try {
						Desktop.getDesktop().moveToTrash(outputFile);
					} catch (UnsupportedOperationException ex) {
						LOGGER.error(ex.getLocalizedMessage(), ex);
					}
				}
				
			});
			player.controls().stop();
		}
		latch.countDown();
		super.shutdown();
	}
	
	protected void doExport() throws PhonMediaException {
		final MediaPlayerFactory factory = new MediaPlayerFactory();
		player = factory.mediaPlayers().newMediaPlayer();
		
		List<String> mediaOpts = new ArrayList<>();
		final NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(3);
		
		if(isPartialExtract()) {
			mediaOpts.add("start-time=" + nf.format(startTime));
			mediaOpts.add("stop-time=" + nf.format(stopTime));
		}
		mediaOpts.addAll(getMediaOptions());
		
		player.media().prepare(inputFile.getAbsolutePath(), mediaOpts.toArray(new String[0]));
		player.events().addMediaPlayerEventListener(playerEventListener);
		boolean started = player.controls().start();

		try {
			if(!started)
				throw new InterruptedException("Unable to start player");
			
			latch.await();
		} catch (InterruptedException e) {
			throw new PhonMediaException(e);
		} finally {
			factory.release();
		}
	}
	
	public List<String> getMediaOptions() {
		if(mediaOpts == null) {
			mediaOpts = buildMediaOptions();
		}
		return mediaOpts;
	}
	
	public void setMediaOptions(List<String> mediaOpts) {
		this.mediaOpts = mediaOpts;
	}

	private List<String> buildMediaOptions() {
		List<String> retVal = new ArrayList<>();

		if(!includeVideo) {
			retVal.add(":no-sout-video");
		}

		if(!includeAudio) {
			retVal.add(":no-sout-audio");
		}
		
		final StringBuffer buffer = new StringBuffer();
		buffer.append(":sout=");
		if(preset != null) {
			if(preset.getMediaOptions() != null)
				retVal.addAll(Arrays.asList(preset.getMediaOptions()));
			buffer.append(preset.getSoutOptions());
			buffer.append(":file{dst=\"").append(outputFile.getAbsolutePath()).append("\"}");
		} else {
			buffer.append("#duplicate{dst=\"").append(outputFile.getAbsolutePath()).append("\"}");
		}
		retVal.add(buffer.toString());
		
		return retVal;
	}
	
	public Preset getPreset() {
		return this.preset;
	}

	public void setPreset(Preset preset) {
		this.preset = preset;
		this.mediaOpts = null;
	}
	
	public boolean isPartialExtract() {
		return startTime > 0
				|| stopTime > 0;
	}

	public boolean isIncludeAudio() {
		return includeAudio;
	}

	public void setIncludeAudio(boolean includeAudio) {
		this.includeAudio = includeAudio;
	}

	public boolean isIncludeVideo() {
		return includeVideo;
	}

	public void setIncludeVideo(boolean includeVideo) {
		this.includeVideo = includeVideo;
	}

	public File getInputFile() {
		return inputFile;
	}

	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
		this.mediaOpts = null;
	}

	public float getMediaStopTime() {
		return stopTime;
	}

	public void setMediaStopTime(float stopTime) {
		this.stopTime = stopTime;
	}

	public float getMediaStartTime() {
		return startTime;
	}

	public void setMediaStartTime(float startTime) {
		this.startTime = startTime;
	}

	/**
	 * Media export listener
	 */
	private MediaPlayerEventListener playerEventListener = new MediaPlayerEventAdapter() {

		@Override
		public void finished(MediaPlayer mediaPlayer) {
			latch.countDown();
			setProperty(PROGRESS_PROP, 1.0f);
		}

		@Override
		public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
			setProperty(PROGRESS_PROP, newPosition);
		}

		@Override
		public void error(MediaPlayer mediaPlayer) {
			latch.countDown();
		}

	};
	
}
