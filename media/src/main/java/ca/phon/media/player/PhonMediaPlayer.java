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

package ca.phon.media.player;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.logging.log4j.LogManager;

import com.sun.jna.Memory;

import ca.phon.media.VLCHelper;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

/**
 * Media player using vlc4j (including media playback controls.)
 */
public class PhonMediaPlayer extends JPanel {
	
	private static final long serialVersionUID = -5365398623998749265L;
	
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(PhonMediaPlayer.class.getName());

	private final static int VOL_MAX = 200;

	/** UI  components */
	/* Play/pause button */
	private JButton playPauseBtn;
	
	private JButton replayBtn;

	/* Position slider */
	private TimeSlider positionSlider;

	/* Volume button */
	private JButton volumeBtn;

	/* Volume slider */
	private JSlider volumeSlider;

	/* Menu button */
	private JButton menuBtn;

	/* Container for media controls */
	private JPanel mediaControlPanel;

	/* Media player */
	private MediaPlayerFactory mediaPlayerFactory;
	private EmbeddedMediaPlayer mediaPlayer;
	private PhonPlayerComponent mediaPlayerCanvas;
	//private DirectMediaPlayerComponent mediaPlayerComponent;
	
//	private Timer mediaTimer;

	/* Media player listener */
	private final MediaPlayerListener mediaListener = new MediaPlayerListener();
	
	/* Icons */
	private ImageIcon playIcn;
	private ImageIcon replayIcn;
	private ImageIcon pauseIcn;
	private ImageIcon volMuteIcn;
	private ImageIcon volIcn;
	private ImageIcon menuIcn;
	
	/** Menu filters */
	private List<IMediaMenuFilter> menuFilters =
			new ArrayList<IMediaMenuFilter>();

	/** Media file */
	private String mediaFile = null;

	/** Volume popup frame */
	private JFrame volumePopup;
	
	/**
	 * Constructor
	 */
	public PhonMediaPlayer() {
		super();

		loadIcons();
		init();
	}
	
	public void cleanup() {
		if(mediaPlayerFactory != null) {
			mediaPlayerFactory.release();
			mediaPlayerFactory = null;
		}
		// clean up player
		if(mediaPlayer != null) {
			if(mediaPlayer.status().isPlaying()) {
				mediaPlayer.controls().stop();
			}
			
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}
	
	/*
	 * Load icons
	 */
	private void loadIcons() {
		IconManager icnMgr = IconManager.getInstance();
		IconSize icnSize = IconSize.SMALL;
		playIcn = icnMgr.getIcon("actions/media-playback-start", icnSize);
		pauseIcn = icnMgr.getIcon("actions/media-playback-pause", icnSize);
		volMuteIcn = icnMgr.getIcon("status/audio-volume-muted", icnSize);
		volIcn = icnMgr.getIcon("status/audio-volume-high", icnSize);
		menuIcn = icnMgr.getIcon("misc/layer_lowerlayer", icnSize);
		replayIcn = icnMgr.getIcon("actions/media-replay-30", icnSize);
	}

	/*
	 * Init display
	 */
	private void init() {
		setLayout(new BorderLayout());
		
		// create media panel
		mediaControlPanel = getMediaControlPanel();
		
		add(mediaControlPanel, BorderLayout.SOUTH);

		addMediaMenuFilter(new MediaMenuFilter());
		
		mediaPlayerCanvas = new PhonPlayerComponent();
		add(mediaPlayerCanvas, BorderLayout.CENTER);
	}
	
	public void addMediaMenuFilter(IMediaMenuFilter filter) {
		if(!menuFilters.contains(filter))
			menuFilters.add(filter);
	}

	public void removeMediaMenuFilter(IMediaMenuFilter filter) {
		menuFilters.remove(filter);
	}

	public List<IMediaMenuFilter> getMediaMenuFilters() {
		return this.menuFilters;
	}

	public JPanel getMediaControlPanel() {
		JPanel retVal = mediaControlPanel;
		if(retVal == null) {
			retVal = new JPanel();

			playPauseBtn = getPlayPauseButton();
			playPauseBtn.setEnabled(false);
			replayBtn = getReplayButton();
			replayBtn.setEnabled(false);
			positionSlider = getPositionSlider();
			positionSlider.setEnabled(false);
			positionSlider.setUI(new TimeSliderUI());
			volumeBtn = getVolumeButton();
			menuBtn = getMenuButton();
			
			// setup layout
			final GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridheight = 1;
			gbc.gridwidth = 1;
			gbc.insets = new Insets(0, 0, 0, 0);
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.fill = GridBagConstraints.NONE;
			retVal.setLayout(new GridBagLayout());
			
			retVal.add(playPauseBtn, gbc);
			++gbc.gridx;
			retVal.add(replayBtn, gbc);
			++gbc.gridx;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			retVal.add(positionSlider, gbc);
			gbc.weightx = 0.0;
			gbc.fill = GridBagConstraints.NONE;
			++gbc.gridx;
			retVal.add(volumeBtn, gbc);
			++gbc.gridx;
			retVal.add(menuBtn, gbc);
		}
		return retVal;
	}

	public JButton getPlayPauseButton() {
		JButton retVal = playPauseBtn;
		if(retVal == null) {
			PhonUIAction playPauseAct =
					new PhonUIAction(this, "onPlayPause");
			playPauseAct.putValue(Action.SMALL_ICON, playIcn);
			retVal = new JButton();
			retVal.setAction(playPauseAct);
			playPauseBtn = retVal;
		}
		return retVal;
	}
	
	public JButton getReplayButton() {
		JButton retVal = replayBtn;
		if(retVal == null) {
			PhonUIAction replayAct = 
					new PhonUIAction(this, "onReplay30");
			replayAct.putValue(Action.SMALL_ICON, replayIcn);
			retVal = new JButton();
			retVal.setAction(replayAct);
			replayBtn = retVal;
		}
		return retVal;
	}

	public TimeSlider getPositionSlider() {
		TimeSlider retVal = positionSlider;
		if(retVal == null) {
			retVal = new TimeSlider();
			retVal.setValue(0);
			retVal.setPaintLabels(false);
			retVal.setPaintTicks(false);
			retVal.setOrientation(SwingConstants.HORIZONTAL);
			retVal.addChangeListener(new PositionListener());
			positionSlider = retVal;
		}
		return retVal;
	}

	public JButton getVolumeButton() {
		JButton retVal = volumeBtn;
		if(retVal == null) {
			PhonUIAction toggleMuteAct =
					new PhonUIAction(this, "onVolumeBtn");
			toggleMuteAct.putValue(Action.SMALL_ICON, volIcn);
			retVal = new JButton();
			retVal.setAction(toggleMuteAct);
			volumeBtn = retVal;
		}
		return retVal;
	}

	public JSlider getVolumeSlider() {
		JSlider retVal = volumeSlider;
		if(retVal == null) {
			retVal = new JSlider();
			retVal.setOrientation(SwingConstants.VERTICAL);
			retVal.setMaximum(VOL_MAX);

			retVal.addChangeListener((ChangeEvent ce) -> {
					JSlider slider = (JSlider)ce.getSource();
					final MediaPlayer player = getMediaPlayer();
					if(player == null) return;
					player.audio().setVolume(slider.getValue());
				});
			volumeSlider = retVal;
		}
		return retVal;
	}

	public JButton getMenuButton() {
		JButton retVal = menuBtn;
		if(retVal == null) {
			PhonUIAction showMenuAct =
					new PhonUIAction(this, "showMediaMenu");
			showMenuAct.putValue(Action.SMALL_ICON, menuIcn);
			retVal = new JButton();
			retVal.setAction(showMenuAct);
			menuBtn = retVal;
		}
		return retVal;
	}
	
	/**
	 * Return the underlying VLC media player
	 * object.
	 * 
	 * @return the current VLC media player - this
	 * value can change during playback (e.g., looping)
	 */
	private MediaPlayer getMediaPlayer() {
		return mediaPlayer;
	}

	/**
	 * Return the current media file handled by the 
	 * player.
	 * 
	 * @return the curent media file path/location
	 */
	public String getMediaFile() {
		return this.mediaFile;
	}

	/**
	 * Sets media location but does not load media
	 * @param mediaFile
	 */
	public void setMediaFile(String mediaFile) {
		this.mediaFile = mediaFile;
		loadMedia();
	}
	
	public void loadMedia(String loc) {
		setMediaFile(loc);
	}
	
	public void loadMedia() {
		// cleanup any existing vlc objects
		cleanup();
		
		getPositionSlider().setValue(0);
		getPositionSlider().setMaximum(0);
		if(mediaFile == null) {
			playPauseBtn.setEnabled(false);
			replayBtn.setEnabled(false);
			positionSlider.setEnabled(false);
			mediaPlayerCanvas.setBufferedImage(null);
			mediaPlayerCanvas.repaint();
		} else if(VLCHelper.isLoaded()) {
			mediaPlayerFactory = new MediaPlayerFactory("--no-metadata-network-access");
			mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
			if(mediaPlayer == null) return;
			
			playPauseBtn.setEnabled(true);
			replayBtn.setEnabled(true);
			positionSlider.setEnabled(true);
			
			mediaPlayer.media().prepare(getMediaFile(), ":play-and-pause", ":no-video-title-show");
			mediaPlayer.videoSurface().set(
					mediaPlayerFactory.videoSurfaces().newVideoSurface( new BufferFormatCallback(), new MediaPlayerRenderCallback(), true));
			
			mediaPlayer.events().addMediaPlayerEventListener(loadListener);
			mediaPlayer.controls().play();
		}
	}
	
	/*
	 * Listener used when loading media to setup position slider maximum.
	 */
	private final MediaPlayerEventAdapter loadListener = new MediaPlayerEventAdapter() {

		@Override
		public void playing(final MediaPlayer mediaPlayer) {
			mediaPlayer.submit( () -> {
				mediaPlayer.controls().pause();
				mediaPlayer.events().removeMediaPlayerEventListener(loadListener);
				
				final int sliderMax = (int)mediaPlayer.status().length();
				SwingUtilities.invokeLater( () -> getPositionSlider().setMaximum(sliderMax) );
				
				mediaPlayer.events().addMediaPlayerEventListener(mediaListener);
				for(MediaPlayerEventListener listener:cachedListeners) {
					mediaPlayer.events().addMediaPlayerEventListener(listener);
				}
			});
		}
		
	};

	/* UI actions */
	public void toggleVolumeMute(PhonActionEvent pae) {
		final MediaPlayer player = getMediaPlayer();
		if(player != null) {
			boolean isMuted = !player.audio().isMute();
			player.audio().setMute(isMuted);

			if(isMuted) {
				getVolumeButton().getAction().putValue(Action.SMALL_ICON, volMuteIcn);
			} else {
				getVolumeButton().getAction().putValue(Action.SMALL_ICON, volIcn);
			}
			getVolumeButton().repaint();

			getVolumeSlider().setEnabled(!isMuted);
		}
	}

	public void onVolumeBtn(PhonActionEvent pae) {
		final MediaPlayer player = getMediaPlayer();
		if(player == null) return;

		// show volume popup
		if(volumePopup == null) {
			volumePopup = new JFrame();
			volumePopup.setUndecorated(true);
			volumePopup.addWindowFocusListener(new WindowFocusListener() {

				@Override
				public void windowGainedFocus(WindowEvent we) {

				}

				@Override
				public void windowLostFocus(WindowEvent we) {
					if(volumePopup != null) {
						volumePopup.setVisible(false);
						volumePopup = null;
					}
				}
			});

			// create slider and mute button
			JSlider volumeSlider = getVolumeSlider();
			volumeSlider.setPreferredSize(new Dimension(
					volumeSlider.getPreferredSize().width, 100));
			if(!player.audio().isMute())
				volumeSlider.setValue(player.audio().volume());
			else
				volumeSlider.setEnabled(false);
			

			PhonUIAction muteAct = new PhonUIAction(this, "toggleVolumeMute");
			muteAct.putValue(Action.SMALL_ICON, volMuteIcn);
			muteAct.putValue(Action.SHORT_DESCRIPTION, "Toggle mute");

			JButton muteBtn = new JButton(muteAct);
			muteBtn.setBorder(null);

			JPanel panel = new JPanel(new BorderLayout());
			panel.add(volumeSlider, BorderLayout.CENTER);
			panel.add(muteBtn, BorderLayout.SOUTH);

			volumePopup.add(panel);

			Point p = getVolumeButton().getLocation();
			SwingUtilities.convertPointToScreen(p, getVolumeButton().getParent());
			Rectangle windowBounds = new Rectangle(
					p.x,
					p.y - volumePopup.getPreferredSize().height,
					volumePopup.getPreferredSize().width,
					volumePopup.getPreferredSize().height);
			volumePopup.setBounds(windowBounds);
			volumePopup.setVisible(true);
		}
	}

	public void onPlayPause(PhonActionEvent pae) {
		final MediaPlayer player = getMediaPlayer();
		if(player != null) {
			if(player.status().isPlaying()) {
				player.controls().pause();
			} else {
				player.controls().play();
			}
		}
	}
	
	public void onReplay30(PhonActionEvent pae) {
		final MediaPlayer player = getMediaPlayer();
		if(player != null) {
			long currentPos = player.status().time();
			long newPos = Math.max(0, currentPos - (30 * 1000));
			player.controls().setTime(newPos);
		}
	}

	public void showMediaMenu(PhonActionEvent pae) {
		JPopupMenu menu = createMediaContextMenu();
		menu.show(menuBtn, 0, menuBtn.getHeight());
	}

	/**
	 * Create the media menu using current menu
	 * filters.
	 */
	private JPopupMenu createMediaContextMenu() {
		JPopupMenu retVal = new JPopupMenu();

		for(IMediaMenuFilter menuFilter:menuFilters) {
			retVal = menuFilter.makeMenuChanges(retVal);
		}

		return retVal;
	}

	/* Menu actions */
	public void onReloadMedia(PhonActionEvent pae) {
		loadMedia();
	}

	public void onTakeSnapshot(PhonActionEvent pae) {
		final MediaPlayer player = getMediaPlayer();
		if(player != null) {
			boolean isPlaying = player.status().isPlaying();

			// if we are playing, pause while we
			// bring up the save dialog
			if(isPlaying) {
				player.controls().pause();
			}

			final SaveDialogProperties props = new SaveDialogProperties();
			props.setRunAsync(false);
			props.setParentWindow(null);
			props.setFileFilter(new FileFilter("PNG files (*.png)", "png"));
			props.setCanCreateDirectories(true);
			props.setInitialFolder(System.getProperty("user.home") + File.separator + "Desktop");
			props.setPrompt("Save snapshot");
			
			String saveTo = NativeDialogs.showSaveDialog(props);

			if(isPlaying) {
				// start player again
				player.controls().play();
			}

			if(saveTo != null) {
				player.snapshots().save(new File(saveTo));
			}
		}
		
	}
	
	private SegmentListener segmentListener;
	/**
	 * Playback given segment
	 *
	 * @param startTime in ms
	 * @param length in ms
	 */
	public void playSegment(long startTime, long length) {
		final MediaPlayer player = getMediaPlayer();
		
		if(player != null) {
			long endTime = startTime + length;
			if(segmentListener != null)
				player.events().removeMediaPlayerEventListener(segmentListener);
			
			if(!player.status().isPlaying())
				player.controls().play();
			player.controls().setTime(startTime);
			
			if(segmentListener == null) {
				segmentListener = new SegmentListener(endTime);
			} else {
				segmentListener.setStopTime(endTime);
			}
			player.events().addMediaPlayerEventListener(segmentListener);
		}
	}

	/**
	 * Listener for user changes to the position slider
	 *
	 */
	private class PositionListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent ce) {
			if(getPositionSlider().getValueIsAdjusting()) {
				final MediaPlayer mediaPlayer = getMediaPlayer();
				if(mediaPlayer == null) return;
				int sliderPos = getPositionSlider().getValue();
				float pos = (float)sliderPos / getPositionSlider().getMaximum();
				if(pos < 1.0f) {
					mediaPlayer.controls().setPosition(pos);
				}
			}
		}

	}

	/**
	 * Media menu filter
	 */
	private class MediaMenuFilter implements IMediaMenuFilter {

		@Override
		public JPopupMenu makeMenuChanges(JPopupMenu menu) {
			JPopupMenu retVal = menu;

			menu.add(getTakeSnapshotItem());

			return retVal;
		}

		private JMenuItem getLoadMediaItem() {
			PhonUIAction loadMediaAct =
					new PhonUIAction(PhonMediaPlayer.this, "onReloadMedia");
			loadMediaAct.putValue(Action.NAME, "Load media");
			loadMediaAct.putValue(Action.SHORT_DESCRIPTION, "Loads/reloads media for session");

			JMenuItem retVal = new JMenuItem(loadMediaAct);
			return retVal;
		}

		private JMenuItem getTakeSnapshotItem() {
			PhonUIAction takeSnapshotAct =
					new PhonUIAction(PhonMediaPlayer.this, "onTakeSnapshot");
			takeSnapshotAct.putValue(Action.NAME, "Take snapshot");
			takeSnapshotAct.putValue(Action.SHORT_DESCRIPTION, "Take snapshot of video");

			JMenuItem retVal = new JMenuItem(takeSnapshotAct);
			return retVal;
		}

	}

	/**
	 * Listener for segment playback.
	 */
	private class SegmentListener extends MediaPlayerEventAdapter {

		private volatile long stopTime = -1L;
		private Lock stopTimeMutex = new ReentrantLock();

		public SegmentListener(long stopTime) {
			stopTimeMutex.lock();
			this.stopTime = stopTime;
			stopTimeMutex.unlock();
		}
		
		@Override
		public void paused(MediaPlayer mediaPlayer) {
			super.paused(mediaPlayer);
			setStopTime(-1L);
		}
		
		public long getStopTime() {
			long retVal = 0L;
			stopTimeMutex.lock();
			retVal = this.stopTime;
			stopTimeMutex.unlock();
			return retVal;
		}
		
		public void setStopTime(long stopTime) {
			stopTimeMutex.lock();
			this.stopTime = stopTime;
			stopTimeMutex.unlock();
		}
		
		@Override
		public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
			super.timeChanged(mediaPlayer, newTime);
			stopTimeMutex.lock();
			if(stopTime >= 0 && newTime >= stopTime) {
				getMediaPlayer().controls().pause();
			}
			stopTimeMutex.unlock();
		}
		
	}
	
	private class MediaPlayerRenderCallback implements RenderCallback {

		@Override
		public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
			if(nativeBuffers.length < 1) return;
			
			ByteBuffer nativeBuffer = nativeBuffers[0];
			final Dimension videoDimension = getMediaPlayer().video().videoDimension();
			if(videoDimension != null) {
				int w = (int)videoDimension.getWidth();
				int h = (int)videoDimension.getHeight();
				
				BufferedImage img = mediaPlayerCanvas.getBufferedImage(w, h);
				if(img != null) {
					int[] rgbBuffer = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
			        nativeBuffer.asIntBuffer().get(rgbBuffer, 0, h * w);
				}
			
				mediaPlayerCanvas.repaint((long)(1/30.0f * 1000.0f));
			}			
		}
		
	}
	
	private final class BufferFormatCallback extends BufferFormatCallbackAdapter {

        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            return new RV32BufferFormat(sourceWidth, sourceHeight);
        }

    }
	
	/**
	 * Media player listener
	 */
	private class MediaPlayerListener extends MediaPlayerEventAdapter {
		
		@Override
		public void buffering(MediaPlayer mediaPlayer, float newCache) {
			super.buffering(mediaPlayer, newCache);
			
//			if(newCache >= 100.0f) {
//				startRenderTimer();
//			}
			
			if(PrefHelper.getBoolean("phon.debug", false)) {
				final String logMsg = String.format("Buffering %s: %.2f%% complete", 
						mediaPlayer.media().info().mrl(), newCache);
				LOGGER.trace(logMsg);
			}
		}

		@Override
		public void playing(MediaPlayer mediaPlayer) {
			super.playing(mediaPlayer);
//			startRenderTimer();
			getPlayPauseButton().getAction().putValue(Action.SMALL_ICON, pauseIcn);
		}
		
		@Override
		public void paused(MediaPlayer mediaPlayer) {
			super.paused(mediaPlayer);
//			stopRenderTimer();
			getPlayPauseButton().getAction().putValue(Action.SMALL_ICON, playIcn);
		}
		
		@Override
		public void stopped(MediaPlayer mediaPlayer) {
//			stopRenderTimer();
			getPlayPauseButton().getAction().putValue(Action.SMALL_ICON, playIcn);
		}
		
		@Override
		public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
			super.positionChanged(mediaPlayer, newPosition);
			int sliderLoc = Math.round(getPositionSlider().getMaximum() * newPosition);
			if(!getPositionSlider().getValueIsAdjusting())
				getPositionSlider().setValue(sliderLoc);
		}
		
	}

	/*
	 * Media player delegate methods
	 */
	public long getLength() {
		return (getMediaPlayer() != null ? getMediaPlayer().status().length() : 0L);
	}

	public float getPosition() {
		return (getMediaPlayer() != null ? getMediaPlayer().status().position() : 0.0f);
	}

	public float getRate() {
		return (getMediaPlayer() != null ? getMediaPlayer().status().rate() : 0.0f);
	}

	public long getTime() {
		return (getMediaPlayer() != null ? getMediaPlayer().status().time() : 0L);
	}

	public int getVolume() {
		return (getMediaPlayer() != null ? getMediaPlayer().audio().volume() : 0);
	}

	public boolean isMuted() {
		return (getMediaPlayer() != null ? getMediaPlayer().audio().isMute() : false);
	}

	public boolean isPlaying() {
		return (getMediaPlayer() != null ? getMediaPlayer().status().isPlaying() : false);
	}

	public void pause() {
		if(getMediaPlayer() != null)
			getMediaPlayer().controls().pause();
	}

	public void play() {
		if(getMediaPlayer() != null)
			getMediaPlayer().controls().play();
	}
	
	public boolean willPlay() {
		return (getMediaPlayer() != null ? getMediaPlayer().status().isPlayable() : false);
	}

	public void setMuted(boolean arg0) {
		if(getMediaPlayer() != null)
			getMediaPlayer().audio().setMute(arg0);
	}

	public void setPosition(float arg0) {
		if(getMediaPlayer() != null)
			getMediaPlayer().controls().setPosition(arg0);
		updateSliderPosition();
	}

	public boolean setRate(float arg0) {
		if(getMediaPlayer() != null) {
			getMediaPlayer().controls().setRate(arg0);
			return true;
		} else
			return false;
	}

	public void setTime(long arg0) {
		if(getMediaPlayer() != null)
			getMediaPlayer().controls().setTime(arg0);
		updateSliderPosition();
	}

	public void stop() {
//		if(mediaTimer != null) {
//			mediaTimer.stop();
//			mediaTimer = null;
//		}
		if(getMediaPlayer() != null)
			getMediaPlayer().controls().stop();
	}

	public void toggleMute() {
		if(getMediaPlayer() != null)
			getMediaPlayer().audio().setMute(!getMediaPlayer().audio().isMute());
	}
	
	/**
	 * Update the slider position based on the current time value
	 */
	private void updateSliderPosition() {
		final JSlider slider = getPositionSlider();
		if(getMediaPlayer() == null || slider == null || slider.getValueIsAdjusting()) return;
		
		final float pos = getMediaPlayer().status().position();
		final int sliderPos = Math.round(slider.getMaximum() * pos);
		slider.setValue(sliderPos);
	}
	
	private final List<MediaPlayerEventListener> cachedListeners = 
			new ArrayList<MediaPlayerEventListener>();
	public void addMediaPlayerListener(MediaPlayerEventListener listener) {
		if(getMediaPlayer() != null) {
			getMediaPlayer().events().addMediaPlayerEventListener(listener);
		}
		cachedListeners.add(listener);
	}
	
	public void removeMediaPlayerListener(MediaPlayerEventListener listener) {
		if(getMediaPlayer() != null) {
			getMediaPlayer().events().removeMediaPlayerEventListener(listener);
		}
		cachedListeners.remove(listener);
	}
	
}
