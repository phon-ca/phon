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

package ca.phon.media.player;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

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

import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.jna.Memory;

/**
 * Media player using vlc4j (including media playback controls.)
 */
public class PhonMediaPlayer extends JPanel {
	
	private static final long serialVersionUID = -5365398623998749265L;
	
	private static final Logger LOGGER = Logger
			.getLogger(PhonMediaPlayer.class.getName());

	private final static int VOL_MAX = 200;

	/** UI  components */
	/* Play/pause button */
	private JButton playPauseBtn;

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
	private PhonPlayerComponent mediaPlayerCanvas;
	private DirectMediaPlayerComponent mediaPlayerComponent;
	
	private Timer mediaTimer;

	/* Media player listener */
	private final MediaPlayerListener mediaListener = new MediaPlayerListener();
	
	/* Icons */
	private ImageIcon playIcn;
	private ImageIcon pauseIcn;
	private ImageIcon volMuteIcn;
	private ImageIcon volIcn;
	private ImageIcon menuIcn;
	
//	private boolean reloadMedia = true;

	/** Menu filters */
	private List<IMediaMenuFilter> menuFilters =
			new ArrayList<IMediaMenuFilter>();

	/** Media file */
	private String mediaFile = null;

	/** Volume popup frame */
	private JFrame volumePopup;
	
	/**
	 * Last playback location
	 */
	private long lastLocation = 0L;
	
	/**
	 * Was player playing
	 */
	private boolean wasPlaying = false;

	/**
	 * Constructor
	 */
	public PhonMediaPlayer() {
		super();

		loadIcons();
		init();
	}
	
	public void cleanup() {
		// clean up player
		if(mediaPlayerComponent != null) {
			wasPlaying = mediaPlayerComponent.getMediaPlayer().isPlaying();
			lastLocation = mediaPlayerComponent.getMediaPlayer().getTime();
			
			mediaPlayerComponent.getMediaPlayer().stop();
			
			mediaPlayerComponent.release();
			mediaPlayerComponent = null;
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
		
//		loadMedia();
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
			positionSlider = getPositionSlider();
			positionSlider.setEnabled(false);
			positionSlider.setUI(new TimeSliderUI());
			volumeBtn = getVolumeButton();
			menuBtn = getMenuButton();

//			volumeSlider = new JSlider();
//			volumeSlider.setOrientation(JSlider.VERTICAL);
//			volumeSlider.setPreferredSize(new Dimension(volumeBtn.getPreferredSize().width, 100));
			
			// setup layout
			FormLayout layout = new FormLayout(
					"pref, fill:pref:grow, pref, pref",
					"pref");
			CellConstraints cc = new CellConstraints();

			retVal.setLayout(layout);
			retVal.add(playPauseBtn, cc.xy(1,1));
			retVal.add(positionSlider, cc.xy(2,1));
//			retVal.add(volumePane, cc.xy(3, 1));
			retVal.add(volumeBtn, cc.xy(3,1));
			retVal.add(menuBtn, cc.xy(4,1));
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
					player.setVolume(slider.getValue());
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
		return (mediaPlayerComponent != null ? mediaPlayerComponent.getMediaPlayer() : null);
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
		wasPlaying = false;
		lastLocation = 0L;
		loadMedia();
	}
	
	public void loadMedia(String loc) {
		setMediaFile(loc);
		loadMedia();
	}
	
	public synchronized void loadMedia() {
		if(mediaPlayerComponent != null) {
			final MediaPlayer mediaPlayer = getMediaPlayer();
			if(mediaPlayer != null) {
				if(mediaPlayer.isPlaying()) {
					mediaPlayer.pause();
				}
				if(mediaPlayer.isPlayable()) {
					mediaPlayer.stop();
				}
				mediaPlayer.release();
				mediaPlayerComponent.release();
			}
		} 
		mediaPlayerComponent = new DirectMediaPlayerComponent((int sourceWidth, int sourceHeight) -> {
			return new RV32BufferFormat(sourceWidth, sourceHeight);
		});
		
		final MediaPlayer mediaPlayer = getMediaPlayer();
		if(mediaPlayer == null) return;
		
		positionSlider.setValue(0);
		if(mediaFile == null) {
			playPauseBtn.setEnabled(false);
			positionSlider.setEnabled(false);
			mediaPlayerCanvas.setBufferedImage(null);
			mediaPlayerCanvas.repaint();
		} else {
			playPauseBtn.setEnabled(true);
			positionSlider.setEnabled(true);
			mediaPlayer.prepareMedia(getMediaFile(), ":play-and-pause");
			
			mediaPlayer.addMediaPlayerEventListener(loadListener);
			mediaPlayer.play();
		}
	}
	
	private final MediaPlayerEventAdapter loadListener = new MediaPlayerEventAdapter() {

		@Override
		public void playing(final MediaPlayer mediaPlayer) {
			mediaPlayer.pause();
			mediaPlayer.removeMediaPlayerEventListener(this);
			
			SwingUtilities.invokeLater( () -> {
					getPositionSlider().setMaximum((int)mediaPlayer.getLength());
				});
			
			mediaPlayer.addMediaPlayerEventListener(mediaListener);
			for(MediaPlayerEventListener listener:cachedListenerrs) {
				mediaPlayer.addMediaPlayerEventListener(listener);
			}
		}
		
	};

	/* UI actions */
	public void toggleVolumeMute(PhonActionEvent pae) {
		final MediaPlayer player = getMediaPlayer();
		if(player != null) {
			boolean isMuted = !player.isMute();
			player.mute(isMuted);

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
			if(!player.isMute())
				volumeSlider.setValue(player.getVolume());
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
			if(player.isPlaying()) {
				player.pause();
			} else {
				player.play();
			}
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
			boolean isPlaying = player.isPlaying();

			// if we are playing, pause while we
			// bring up the save dialog
			if(isPlaying) {
				player.pause();
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
				player.play();
			}

			if(saveTo != null) {
				player.saveSnapshot(new File(saveTo));
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
				player.removeMediaPlayerEventListener(segmentListener);
			
			if(!player.isPlaying())
				player.play();
			player.setTime(startTime);
			
			if(segmentListener == null) {
				segmentListener = new SegmentListener(endTime);
			} else {
				segmentListener.setStopTime(endTime);
			}
			player.addMediaPlayerEventListener(segmentListener);
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
					mediaPlayer.setPosition(pos);
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
				getMediaPlayer().pause();
			}
			stopTimeMutex.unlock();
		}
		
	}
	
	/**
	 * Update frame using a timer.
	 */
	public void updateFrame() {
		if(getMediaPlayer() == null) return;
		Timer renderTimer = new Timer(Math.round(1000.0f/getMediaPlayer().getFps()), (ActionEvent ae) -> {
			renderFrame();
		});
		renderTimer.setRepeats(false);
		renderTimer.start();
	}
	
	protected synchronized void renderFrame() {
		if(mediaPlayerComponent == null || mediaPlayerComponent.getMediaPlayer() == null) return;
		Memory[] nativeBuffers = mediaPlayerComponent.getMediaPlayer().lock();
		if(nativeBuffers != null && nativeBuffers.length > 0) {
			Memory nativeBuffer = nativeBuffers[0];
			if(nativeBuffer != null) {
				
				final Dimension videoDimension = mediaPlayerComponent.getMediaPlayer().getVideoDimension();
				if(videoDimension != null) {
					int w = (int)videoDimension.getWidth();
					int h = (int)videoDimension.getHeight();
					
					BufferedImage img = mediaPlayerCanvas.getBufferedImage(w, h);
					if(img != null) {
						int[] rgbBuffer = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
				        nativeBuffer.getByteBuffer(0L, nativeBuffer.size()).asIntBuffer().get(rgbBuffer, 0, h * w);
					}
				}
				
		        mediaPlayerComponent.getMediaPlayer().unlock();

				mediaPlayerCanvas.repaint(Math.round(1000.0f / getMediaPlayer().getFps()));
			}
		} else {
	        mediaPlayerComponent.getMediaPlayer().unlock();
	        
			mediaPlayerCanvas.setBufferedImage(null);
			mediaPlayerCanvas.repaint();
		}
	}

	protected void startRenderTimer() {
		final MediaPlayer mediaPlayer = getMediaPlayer();
		if(mediaTimer == null && mediaPlayer.getVideoTrackCount() > 0) {
			mediaTimer = new Timer(0, (ActionEvent e) -> {
					renderFrame();
				});
			mediaTimer.setDelay(Math.round(1000.0f/mediaPlayer.getFps()));
			mediaTimer.setRepeats(true);
			mediaTimer.start();
		}
	}
	
	protected void stopRenderTimer() {
		if(mediaTimer != null) {
			mediaTimer.stop();
			mediaTimer = null;
		}
	}
	
	/**
	 * Media player listener
	 */
	private class MediaPlayerListener extends MediaPlayerEventAdapter {
		
		@Override
		public void buffering(MediaPlayer mediaPlayer, float newCache) {
			super.buffering(mediaPlayer, newCache);
			
			if(newCache >= 100.0f) {
				startRenderTimer();
			}
			
			if(PrefHelper.getBoolean("phon.debug", false)) {
				final String logMsg = String.format("Buffering %s: %.2f%% complete", 
						mediaPlayer.mrl(), newCache);
				LOGGER.info(logMsg);
			}
		}

		@Override
		public void playing(MediaPlayer mediaPlayer) {
			super.playing(mediaPlayer);
			startRenderTimer();
			getPlayPauseButton().getAction().putValue(Action.SMALL_ICON, pauseIcn);
		}
		
		@Override
		public void paused(MediaPlayer mediaPlayer) {
			super.paused(mediaPlayer);
			stopRenderTimer();
			getPlayPauseButton().getAction().putValue(Action.SMALL_ICON, playIcn);
		}
		
		@Override
		public void stopped(MediaPlayer mediaPlayer) {
			stopRenderTimer();
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
		return (getMediaPlayer() != null ? getMediaPlayer().getLength() : 0L);
	}

	public float getPosition() {
		return (getMediaPlayer() != null ? getMediaPlayer().getPosition() : 0.0f);
	}

	public float getRate() {
		return (getMediaPlayer() != null ? getMediaPlayer().getRate() : 0.0f);
	}

	public long getTime() {
		return (getMediaPlayer() != null ? getMediaPlayer().getTime() : 0L);
	}

	public int getVolume() {
		return (getMediaPlayer() != null ? getMediaPlayer().getVolume() : 0);
	}

	public boolean isMuted() {
		return (getMediaPlayer() != null ? getMediaPlayer().isMute() : false);
	}

	public boolean isPlaying() {
		return (getMediaPlayer() != null ? getMediaPlayer().isPlaying() : false);
	}

	public void pause() {
		if(getMediaPlayer() != null)
			getMediaPlayer().pause();
	}

	public void play() {
		if(getMediaPlayer() != null)
			getMediaPlayer().play();
	}
	
	public boolean willPlay() {
		return (getMediaPlayer() != null ? getMediaPlayer().isPlayable() : false);
	}

	public void setMuted(boolean arg0) {
		if(getMediaPlayer() != null)
			getMediaPlayer().mute(arg0);
	}

	public void setPosition(float arg0) {
		if(getMediaPlayer() != null)
			getMediaPlayer().setPosition(arg0);
		updateSliderPosition();
	}

	public boolean setRate(float arg0) {
		if(getMediaPlayer() != null) {
			getMediaPlayer().setRate(arg0);
			return true;
		} else
			return false;
	}

	public void setTime(long arg0) {
		if(getMediaPlayer() != null)
			getMediaPlayer().setTime(arg0);
		updateSliderPosition();
	}

	public void stop() {
		if(mediaTimer != null) {
			mediaTimer.stop();
			mediaTimer = null;
		}
		if(getMediaPlayer() != null)
			getMediaPlayer().stop();
	}

	public void toggleMute() {
		if(getMediaPlayer() != null)
			getMediaPlayer().mute(!getMediaPlayer().isMute());
	}
	
	/**
	 * Update the slider position based on the current time value
	 */
	private void updateSliderPosition() {
		final JSlider slider = getPositionSlider();
		if(getMediaPlayer() == null || slider == null || slider.getValueIsAdjusting()) return;
		
		final float pos = getMediaPlayer().getPosition();
		final int sliderPos = Math.round(slider.getMaximum() * pos);
		slider.setValue(sliderPos);
	}
	
	private final List<MediaPlayerEventListener> cachedListenerrs = 
			new ArrayList<MediaPlayerEventListener>();
	public void addMediaPlayerListener(MediaPlayerEventListener listener) {
		if(getMediaPlayer() != null) {
			getMediaPlayer().addMediaPlayerEventListener(listener);
		}
		cachedListenerrs.add(listener);
	}
	
}
