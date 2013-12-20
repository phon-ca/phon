/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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

package ca.phon.media;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

import vlc4j.VLCException;
import vlc4j.VLCInstance;
import vlc4j.VLCMediaPlayer;
import vlc4j.event.VLCMediaPlayerAdapter;
import vlc4j.event.VLCMediaPlayerEvent;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTaskListener;

/**
 * Export video and/or audio with the option
 * to transcode the output.  Also able to
 * perform a segment extraction.
 *
 */
public class VLCMediaExporter extends PhonTask {
	
	private final static Logger LOGGER = Logger.getLogger(VLCMediaExporter.class.getName());

	private boolean transcode = false;

	/**
	 * Encode video
	 */
	private boolean includeVideo = true;

	private String videoCodec;

	private int videoBitrate;

	/**
	 * Encode audio
	 *
	 */
	private boolean includeAudio = true;

	private String audioCodec;

	private int audioBitrate;

	/**
	 * Partial extract
	 */
	private long startTime; //ms
	
	private long runTime; //ms

	/**
	 * Container format
	 */
	private String mux;

	/**
	 * Input file
	 */
	private File inputFile;

	/**
	 * Output file
	 */
	private File outputFile;

	/**
	 * VLC instance
	 */
	private VLCInstance vlcInstance;

	private static final String[] vlcOpts =
	{
		"-I", "dummy",	// Don't use an interface
		"--ignore-config", // Don't use VLC's default config
		"--no-osd", // No on screen display
		"--no-media-library", // we don't need the media library
	};

	/**
	 * Constructor
	 *
	 */
	public VLCMediaExporter() {
		super("Media export");
	}

	/**
	 * Constructor
	 */
	public VLCMediaExporter(String inputFile, String outputFile) {
		this(new File(inputFile), new File(outputFile));
	}

	public VLCMediaExporter(File inputFile, File outputFile) {
		super("Media export");
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}

	@Override
	public void performTask() {
		super.setStatus(TaskStatus.RUNNING);

		try {
			doExport();
		} catch (VLCException ex) {
			LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			super.err = ex;
			super.setStatus(TaskStatus.ERROR);
		}
		
	}
	
	private void doExport() throws VLCException {
		try {
			List<String> cmd = new ArrayList<String>();
			cmd.add(getVLCBinaryPath());
			cmd.addAll(getProgramArguments());

			ProcessBuilder processBuilder = new ProcessBuilder(cmd);
			processBuilder.redirectErrorStream(true);
//			System.out.println(processBuilder.command().toString());
			Process p = processBuilder.start();

			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while((line = in.readLine()) != null) {
				LOGGER.info("[vlc] " + line);
			}
			in.close();

			System.exit(0);
		} catch (IOException ex) {
			LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			throw new VLCException(ex.toString());
		}

	}

	private String getVLCBinaryPath() {
		String retVal =
				"/Applications/VLC.app/Contents/MacOS/VLC";
		return retVal;
	}

	public List<String> getProgramArguments() {
		List<String> retVal = new ArrayList<String>();

		retVal.add("-vvv");
		retVal.add("-I"); retVal.add("dummy");
//		retVal.add("--no-osd");
//		retVal.add("--ignore-config");
//		retVal.add("--no-media-library");
		
		if(isPartialExtract()) {
			retVal.add("--stop-time=" +
					((startTime+runTime)/1000.0f));
		}

		retVal.add(inputFile.getAbsolutePath());

		if(isPartialExtract()) {
			retVal.add("--start-time=" +
					(startTime/1000.0f));
		}

		if(!includeVideo) {
			retVal.add("--no-sout-video");
		}

		if(!includeAudio) {
			retVal.add("--no-sout-audio");
		}

		if(includeVideo) {
			retVal.add("--deinterlace");
		}

		String soutOpts = "--sout=#";
		if(transcode) {
			// setup transcode module
			soutOpts += "transcode{";
			if(includeVideo) {
				soutOpts += "vcodec=" + videoCodec;
				soutOpts += ",vb=" + videoBitrate;
			}

			if(includeAudio) {
				soutOpts += (includeVideo ? "," : "") + "acodec=" + audioCodec;
				soutOpts += ",ab=" + audioBitrate;
			}
			soutOpts += "}:";
			
			soutOpts += "standard{";
			soutOpts += "mux=" + mux + ",";
			soutOpts += "dst='" + outputFile.getAbsolutePath() + "'";
			soutOpts += ",access=file}";
		} else {
			// setup standard module
			soutOpts += "standard{";
			soutOpts += "dst='" + outputFile.getAbsolutePath() + "'";
			soutOpts += ",mux=mp4";
			soutOpts += ",access=file}";
		}
		retVal.add(soutOpts);

		retVal.add("vlc://quit");

		System.out.println(retVal);
		
		return retVal;
	}

	public boolean isPartialExtract() {
		return startTime >= 0L
				&& runTime > 0L;
	}

	public int getAudioBitrate() {
		return audioBitrate;
	}

	public void setAudioBitrate(int audioBitrate) {
		this.audioBitrate = audioBitrate;
	}

	public String getAudioCodec() {
		return audioCodec;
	}

	public void setAudioCodec(String audioCodec) {
		this.audioCodec = audioCodec;
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

	public String getMux() {
		return mux;
	}

	public void setMux(String mux) {
		this.mux = mux;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	@Override
	public long getRunTime() {
		return runTime;
	}

	public void setRunTime(long runTime) {
		this.runTime = runTime;
	}

	@Override
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public boolean isTranscode() {
		return transcode;
	}

	public void setTranscode(boolean transcode) {
		this.transcode = transcode;
	}

	public int getVideoBitrate() {
		return videoBitrate;
	}

	public void setVideoBitrate(int videoBitrate) {
		this.videoBitrate = videoBitrate;
	}

	public String getVideoCodec() {
		return videoCodec;
	}

	public void setVideoCodec(String videoCodec) {
		this.videoCodec = videoCodec;
	}

	public void cleanup() {
		try {
			if(vlcInstance != null) {
				vlcInstance.free();
			}
		} catch (VLCException ex) {
			LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
		}
	}

	/**
	 * Media export listener
	 */
	private class MediaExportListener extends VLCMediaPlayerAdapter {

		@Override
		public void onEndReached(VLCMediaPlayerEvent vlcmpe) {
			setProperty(PhonTask.PROGRESS_PROP, 1.0f);
			setStatus(TaskStatus.FINISHED);
		}

		@Override
		public void onPositionChanged(VLCMediaPlayerEvent vlcmpe) {
			try {
				VLCMediaPlayer player = vlcmpe.getSource();

				float pos = player.getPosition();

				// use position as percentage done
				setProperty(PhonTask.PROGRESS_PROP, pos);
			} catch (VLCException ex) {
				LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			}
		}

	}

	public static void  main(String[] args) throws Exception {
		JFrame f = new JFrame("Test");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final JProgressBar progressBar = new JProgressBar();
		progressBar.setMaximum(100);
		progressBar.setValue(0);

		f.getContentPane().add(progressBar);

		f.pack();
		f.setVisible(true);

		String mediaFile =
				"/Users/ghedlund/Movies/MyMovie.mp4";
		String outputFile =
				"/Users/ghedlund/Desktop/test.mpg";

		final VLCMediaExporter exporter = new VLCMediaExporter(mediaFile, outputFile);
		exporter.setStartTime(10254L);
		exporter.setRunTime(3000L);
		exporter.setTranscode(true);
		exporter.setIncludeVideo(true);
		exporter.setVideoCodec("mp2v");
		exporter.setVideoBitrate(1024);
		exporter.setIncludeAudio(true);
		exporter.setAudioCodec("mpga");
		exporter.setAudioBitrate(192);
		exporter.setMux("ps");
		
		exporter.addTaskListener(new PhonTaskListener() {

			@Override
			public void statusChanged(PhonTask task, TaskStatus oldStatus, TaskStatus newStatus) {
			}

			@Override
			public void propertyChanged(PhonTask task, String property, Object oldValue, Object newValue) {
				if(property.equals(PhonTask.PROGRESS_PROP)) {
					Float percentDone = (Float)newValue;

					int sliderPos = Math.round(progressBar.getMaximum() * percentDone);
					progressBar.setValue(sliderPos);

					if(progressBar.getValue() == progressBar.getMaximum()) {
						exporter.cleanup();
						System.exit(0);
					}
				}
			}

		});

		exporter.doExport();
	}
}
