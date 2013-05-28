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

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import ca.phon.worker.PhonTask;

import vlc4j.VLCException;
import vlc4j.VLCInstance;
import vlc4j.VLCMedia;
import vlc4j.VLCMediaPlayer;
import vlc4j.event.VLCMediaPlayerEvent;
import vlc4j.event.VLCMediaPlayerEventType;
import vlc4j.event.VLCMediaPlayerListener;

/**
 * Media thumbnailer using vlc4j.
 */
public class VLCThumbnailer extends PhonTask {

	/** The output file */
	private String outputFile;

	/** Media file */
	private String mediaFile;

	/** location in file to take snapshot (between 0.0 and 1.0) */
	private float snapshotPos = 0.1f;

	private VLCInstance vlcInstance;

	/** Media player used for snapshot */
	private VLCMediaPlayer mediaPlayer;

	/** Media */
	private VLCMedia media;

	public VLCThumbnailer(String mediaFile, String outputFile) {
		this.mediaFile = mediaFile;
		this.outputFile = outputFile;
	}

	@Override
	public void performTask() {
		if(media != null || mediaPlayer != null) {
			try {
				cleanup();
			} catch (VLCException ex) {
				PhonLogger.warning(ex.toString());
			}
		}

		// starup VLC if necessary
		try {
			vlcInstance = VLCInstance.getInstance();
			vlcInstance.init();
		} catch (VLCException ex) {
			PhonLogger.severe(ex.toString());
			return;
		}

		if(mediaFile == null)
			throw new IllegalArgumentException("No media file given.");
		if(outputFile == null)
			throw new IllegalArgumentException("No output file given.");

		super.setStatus(TaskStatus.RUNNING);
		// load media and setup player
		try {
			media = vlcInstance.newFromPath(mediaFile);

			media.addOption(":vout=dummy");
			media.addOption(":aout=dummy");

			
			mediaPlayer = vlcInstance.newMediaPlayer();
			mediaPlayer.setMedia(media);
			mediaPlayer.addMediaPlayerListener(new MediaSnapshotListener());
			mediaPlayer.play();

		} catch (VLCException ex) {
			PhonLogger.severe(ex.toString());
		}
	}

	private class MediaSnapshotListener implements VLCMediaPlayerListener {

		private volatile boolean snapshotTaken = false;

		@Override
		public void mediaPlayerEvent(VLCMediaPlayerEvent vlcmpe) {
			System.out.println(vlcmpe.getType());
			if(vlcmpe.getType() == VLCMediaPlayerEventType.PLAYING) {
				try {
					// media has loaded, set position
					mediaPlayer.setPosition(snapshotPos);
				} catch (VLCException ex) {
					PhonLogger.severe(ex.toString());
					setStatus(TaskStatus.ERROR);
				}
			} else if(vlcmpe.getType() == VLCMediaPlayerEventType.POSITION_CHANGED) {
				try {
					if (!snapshotTaken && mediaPlayer.getPosition() >= snapshotPos) {
						mediaPlayer.pause();
						snapshotTaken = true;
						mediaPlayer.takeSnapshot(outputFile);
					}
				} catch (VLCException ex) {
					PhonLogger.severe(ex.toString());
				}
			} else if(vlcmpe.getType() == VLCMediaPlayerEventType.SNAPSHOT_TAKEN) {
//				try {
////					mediaPlayer.stop();
//
////					cleanup();
//				} catch (VLCException ex) {
//					PhonLogger.warning(ex.toString());
//				}
//				setStatus(TaskStatus.FINISHED);
				try {
					VLCThumbnailer.this.firePropertyChange("BLAH", Boolean.TRUE, Boolean.FALSE);
//					mediaPlayer.removeMediaPlayerListener(this);
					mediaPlayer.stop();
					mediaPlayer.free();
					vlcInstance.free();
					//				setStatus(TaskStatus.FINISHED);
				} catch (VLCException ex) {
					PhonLogger.severe(ex.toString());
				}
			}
		}
	}

	private void cleanup() throws VLCException {
//		if(media != null) {
//			media.free();
//			media = null;
//		}
		if(mediaPlayer != null) {
			mediaPlayer.free();
			mediaPlayer = null;
		}
	}

	public static void main(String[] args) throws Exception {
		JFrame f= new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.pack();
		f.setVisible(true);
		
		VLCThumbnailer thumbnailer =
				new VLCThumbnailer("/Users/ghedlund/Movies/Avaledan_XT-002-web.mov", "/Users/ghedlund/Desktop/irock.png");
		thumbnailer.addTaskListener(new PhonTaskListener() {

			@Override
			public void statusChanged(PhonTask task, TaskStatus oldStatus, TaskStatus newStatus) {
//				if(newStatus == TaskStatus.FINISHED) {
//					try {
//						Image tstImg =
//								ImageIO.read(new File("/Users/ghedlund/Desktop/irock.png"));
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//					System.exit(0);
//				}
			}

			@Override
			public void propertyChanged(PhonTask task, String property, Object oldValue, Object newValue) {
				System.out.println(property);
				try {
						Image tstImg =
								ImageIO.read(new File("/Users/ghedlund/Desktop/irock.png"));
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.exit(0);
			}
		});

		thumbnailer.performTask();
	}
}
