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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.worker.PhonTask;
import vlc4j.VLCException;
import vlc4j.VLCInstance;
import vlc4j.VLCMedia;
import vlc4j.VLCMediaPlayer;
import vlc4j.event.VLCMediaPlayerAdapter;
import vlc4j.event.VLCMediaPlayerEvent;

/**
 * Uses vlc4j to export a media file into a wav file.
 * 
 */
public class VLCWavExporter extends PhonTask {
	
	private final static Logger LOGGER = Logger.getLogger(VLCWavExporter.class.getName());

	/** The output file */
	private String outputFile;

	/** Media file */
	private String mediaFile;

	private int bitrate = 128;

	private VLCInstance vlcInstance;

	/** Media player used for snapshot */
	private VLCMediaPlayer mediaPlayer;

	/** Media */
	private VLCMedia media;

	private final static String ENCODE_OPTIONS =
			":sout=#transcode{acodec=s16l,ab=$BITRATE}:standard{mux=wav,dst=$DESTINATION,access=file}";

	private static final String[] vlcOpts =
	{
		"-I", "dummy",	// Don't use an interface
		"--ignore-config", // Don't use VLC's default config
		"--no-osd", // No on screen display
		"--no-media-library", // we don't need the media library
	};

	/**
	 * Constructor
	 */
	public VLCWavExporter(String mediaFile, String outputFile) {
		this(mediaFile, outputFile, 128);
	}

	public VLCWavExporter(String mediaFile, String outputFile, int bitrate) {
		super("Export as wav");
		this.outputFile = outputFile;
		this.mediaFile = mediaFile;
		this.bitrate = bitrate;
	}

	@Override
	public void performTask() {
		super.setStatus(TaskStatus.RUNNING);
		try {
			doExport();
		} catch (VLCException ex) {
			LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
		}

//		super.setStatus(TaskStatus.FINISHED);
	}

	/**
	 * Asynchronous export
	 */
	public void doExport() throws VLCException {
		VLCInstance instance = VLCInstance.getInstance();
		instance.init(vlcOpts);

		VLCMediaPlayer player = instance.newMediaPlayer();
		
		VLCMedia media = instance.newFromPath(mediaFile);
		
		media.addOption("no-sout-video");
		media.addOption("sout=#transcode{acodec=s16l,ab=192}:standard{mux=wav,dst=" +outputFile+",access=file}");

		player.setMedia(media);
		player.addMediaPlayerListener(new ExportListener());
		player.play();
	}

	private void fixHeaderDataSize() throws IOException {
		File f = new File(outputFile);

		FileChannel rwChannel =
				new RandomAccessFile(f, "rw").getChannel();
		ByteBuffer rwBuffer =
				rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, rwChannel.size());

		// we need to fix SubChunk2Size in the wave header
		// SubChunk2Size = SubChunkSize - 36
		// SubChunkSize is contained in bytes 4-8 and bytes
		// need to be reversed
		byte buf[] = new byte[4];

		// write chunksize
		rwBuffer.position(4);
		int chunkSize =
				(int)rwChannel.size() - 8;
		buf[0] = (byte)((chunkSize & 0x000000FF) >> 0);
		buf[1] = (byte)((chunkSize & 0x0000FF00) >> 8);
		buf[2] = (byte)((chunkSize & 0x00FF0000) >> 16);
		buf[3] = (byte)((chunkSize & 0xFF000000) >> 24);
		rwBuffer.put(buf);

		rwBuffer.position(40);
		int dataChunkSize = chunkSize - 36;
		buf[0] = (byte)((dataChunkSize & 0x000000FF) >> 0);
		buf[1] = (byte)((dataChunkSize & 0x0000FF00) >> 8);
		buf[2] = (byte)((dataChunkSize & 0x00FF0000) >> 16);
		buf[3] = (byte)((dataChunkSize & 0xFF000000) >> 24);
		rwBuffer.put(buf);

		rwChannel.close();
	}

	private class ExportListener extends VLCMediaPlayerAdapter {

		@Override
		public void onPositionChanged(VLCMediaPlayerEvent vlcmpe) {
			try {
				VLCMediaPlayer player = vlcmpe.getSource();

				float pos = player.getPosition();

				// use position as percentage done
				VLCWavExporter.this.setProperty(PhonTask.PROGRESS_PROP, pos);

			} catch (VLCException ex) {
				LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			}
		}
		
		@Override
		public void onEndReached(VLCMediaPlayerEvent vlcmpe) {
			try {
				// fix header data in wav file
				fixHeaderDataSize();
				VLCWavExporter.this.setProperty(PhonTask.PROGRESS_PROP, 1.0f);
				VLCWavExporter.this.setStatus(TaskStatus.FINISHED);
			} catch (IOException ex) {
				LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			}
		}
		
	}

	public void cleanup() {
		try {
			if(mediaPlayer != null) {
				mediaPlayer.free();
			}
			if(vlcInstance != null) {
				vlcInstance.free();
			}
		} catch (VLCException ex) {
			LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
		}
	}

}
