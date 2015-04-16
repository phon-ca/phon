/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import ca.phon.util.OSInfo;
import ca.phon.worker.PhonTask;

/**
 * Uses the redistributed copy of ffmpeg-static
 * to extract segments of a given media file.
 */
public class FFMpegMediaExporter extends PhonTask {
	
	private final static Logger LOGGER = Logger.getLogger(FFMpegMediaExporter.class.getName());

	/* ffmpeg options */
	private String inputFile;

	private String outputFile;

	private long startTime = -1L;

	private long duration = -1L;

	private boolean includeVideo = true;

	private String videoCodec = "copy";

	private boolean includeAudio = true;

	private String audioCodec = "copy";

	private String additionalArgs = "";

	/**
	 * Constructor
	 */
	public FFMpegMediaExporter() {
		super("ffmpeg");
	}

	public String getOtherArgs() {
		return this.additionalArgs;
	}

	public void setOtherArgs(String args) {
		this.additionalArgs = args;

	}
	
	public boolean isIncludeVideo() {
		return this.includeVideo;
	}

	public void setIncludeVideo(boolean v) {
		this.includeVideo = v;
	}

	public String getVideoCodec() {
		return this.videoCodec;
	}

	public void setVideoCodec(String codec) {
		this.videoCodec = codec;
	}

	public boolean isIncludeAudio() {
		return this.includeAudio;
	}

	public void setIncludeAudio(boolean v) {
		this.includeAudio = v;
	}

	public String getAudioCodec() {
		return this.audioCodec;
	}

	public void setAudioCodec(String audioCodec) {
		this.audioCodec = audioCodec;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	@Override
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	private String getFFMpegBinary() {
		String retVal = null;
		
		if(OSInfo.isMacOs()) {
			retVal = "data/bin/macos/ffmpeg-static";
		} else if(OSInfo.isWindows()) {
			retVal = "data/bin/windows/ffmpeg-static.exe";
		} else if(OSInfo.isNix()) {
			// use default install location for ubuntu 10.10
			retVal = "/usr/bin/ffmpeg";
		}
		
		return retVal;
	}

	private List<String> getCmdLine() {
		List<String> cmdLine =
				new ArrayList<String>();
		cmdLine.add(getFFMpegBinary());

		// setup options
		cmdLine.add("-i"); cmdLine.add(inputFile);

		if(startTime >= 0L) {
			cmdLine.add("-ss"); cmdLine.add(""+(startTime/1000.0f));
		}
		if(duration > 0L) {
			cmdLine.add("-t"); cmdLine.add(""+(duration/1000.0f));
		}
		if(isIncludeAudio()) {
			String ac = getAudioCodec();
			if(ac.equalsIgnoreCase("wav")
					|| ac.equalsIgnoreCase("raw")) {
				ac = "pcm_s16le";
			}

			cmdLine.add("-acodec"); cmdLine.add(ac);

		} else {
			cmdLine.add("-an");
		}

		if(isIncludeVideo()) {
			cmdLine.add("-vcodec"); cmdLine.add(getAudioCodec());
		} else {
			cmdLine.add("-vn");
		}

		if(StringUtils.strip(additionalArgs).length() > 0) {
			BufferedReader argsReader =
					new BufferedReader(new InputStreamReader(new ByteArrayInputStream(additionalArgs.getBytes())));
			String line = null;
			try {
				while ((line = argsReader.readLine()) != null) {
					if(StringUtils.strip(line).length() == 0 ||
						line.matches("^#.*")) {
						continue;
					}
					// allow for a single pair of arguments to be added
					// for example -ac 1
					// otherwise it's one argument per line
					int spaceIdx = line.indexOf(" ");
					if(spaceIdx > 0) {
						String arg1 = line.substring(0, spaceIdx);
						String arg2 = line.substring(spaceIdx+1);
						cmdLine.add(arg1); cmdLine.add(arg2);
					} else
						cmdLine.add(StringUtils.strip(line));
				}

				argsReader.close();
			} catch (IOException ex) {
				LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			}
		}

		cmdLine.add("-y");
		cmdLine.add(outputFile);

		return cmdLine;
	}

	@Override
	public void performTask() {
		super.setStatus(TaskStatus.RUNNING);

		// check options
		if(inputFile != null) {
			File f = new File(inputFile);
			if(!f.exists()) {
				IOException ex = new IOException("File not found");
				super.err = ex;
				super.setStatus(TaskStatus.ERROR);
				return;
			}
		} else {
			IllegalArgumentException ex = new IllegalArgumentException("Input file cannot be null");
			super.err = ex;
			super.setStatus(TaskStatus.ERROR);
			return;
		}

		if(outputFile != null) {
			File f = new File(outputFile);
			File pf = f.getParentFile();

			if(!pf.exists()) {
				// try to create directories
				if(!pf.mkdirs()) {
					IOException ex = new IOException("Could not create output directory");
					super.err = ex;
					super.setStatus(TaskStatus.ERROR);
					return;
				}
			} else {
				if(!pf.isDirectory()) {
					IOException ex = new IOException(pf.getAbsolutePath() + " exists but is not a directory");
					super.err = ex;
					super.setStatus(TaskStatus.ERROR);
					return;
				}
			}
		} else {
			IllegalArgumentException ex = new IllegalArgumentException("Ouput file cannot be null");
			super.err = ex;
			super.setStatus(TaskStatus.ERROR);
			return;
		}

		List<String> cmdLine = getCmdLine();

		// setup process builder
		ProcessBuilder processBuilder =
				new ProcessBuilder(cmdLine);
		processBuilder.redirectErrorStream(true);
		try {
			// create process
			Process process = processBuilder.start();

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			String line = null;
			while((line = reader.readLine()) != null) {
				LOGGER.info(line);
			}
			reader.close();

			super.setStatus(TaskStatus.FINISHED);
		} catch (IOException ex) {
			LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			super.err = ex;
			super.setStatus(TaskStatus.ERROR);
		}

	}
	
}
