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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.VerticalLayout;

import vlc4j.VLCException;
import vlc4j.VLCInstance;
import vlc4j.VLCMedia;
import vlc4j.VLCMediaPlayer;
import vlc4j.event.VLCMediaPlayerAdapter;
import vlc4j.event.VLCMediaPlayerEvent;
import vlc4j.event.VLCMediaPlayerListener;
import ca.phon.gui.CommonModuleFrame;
import ca.phon.gui.action.PhonActionEvent;
import ca.phon.gui.action.PhonUIAction;
import ca.phon.gui.components.JRangeSlider;
import ca.phon.gui.recordeditor.SegmentField;
import ca.phon.system.logger.PhonLogger;
import ca.phon.util.FileFilter;
import ca.phon.util.NativeDialogs;
import ca.phon.util.PhonUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Media player using vlc4j (including media playback controls.)
 */
public class PhonMediaPlayer extends JPanel {

	private final static int SLIDER_MAX = 10000;

	private final static int VOL_MAX = 200;

	private final static int PLAYER_WIDTH = 320;

	private final static int PLAYER_HEIGHT = 240;

	/** UI  components */
	/* Canvas for the player to draw on */
	private PhonPlayerCanvas playerCanvas;

	/* Play/pause button */
	private JButton playPauseBtn;

	/* Position slider */
	private JSlider positionSlider;

	/* Volume button */
	private JButton volumeBtn;

	/* Volume slider */
	private JSlider volumeSlider;

	/* Menu button */
	private JButton menuBtn;

	/* Container for media controls */
	private JPanel mediaControlPanel;

	/* Auto-hide controls (default:true) */
	private boolean autoHideControls = true;

	/* VLC instance */
	private VLCInstance vlcInstance;

	/* Media player */
	private VLCMediaPlayer mediaPlayer;

	/* Media player listener */
	private MediaPlayerListener mediaListener;

	/* Icons */
	private ImageIcon playIcn;
	private ImageIcon pauseIcn;
	private ImageIcon volMuteIcn;
	private ImageIcon volIcn;
	private ImageIcon menuIcn;
	
	private boolean reloadMedia = true;

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

		playerCanvas = new PhonPlayerCanvas();
		playerCanvas.setMessage("Click play to load media");
		playerCanvas.setBackground(Color.black);
//		playerCanvas.setPreferredSize(new Dimension(PLAYER_WIDTH, PLAYER_HEIGHT));

		// create media panel
		mediaControlPanel = getMediaControlPanel();
		
		add(playerCanvas, BorderLayout.CENTER);
		add(mediaControlPanel, BorderLayout.SOUTH);

		addMediaMenuFilter(new MediaMenuFilter());
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
			positionSlider = getPositionSlider();
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

	public JSlider getPositionSlider() {
		JSlider retVal = positionSlider;
		if(retVal == null) {
			retVal = new JSlider();
			retVal.setPaintLabels(false);
			retVal.setPaintTicks(false);
			retVal.setOrientation(SwingConstants.HORIZONTAL);
			retVal.setMaximum(SLIDER_MAX);
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

			retVal.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent ce) {
					JSlider slider = (JSlider)ce.getSource();
					if(getMediaPlayer() == null) return;
//					if(slider.getValueIsAdjusting()) {
						try {
							getMediaPlayer().setVolume(slider.getValue());
						} catch (VLCException ex) {
							PhonLogger.severe(ex.toString());
						}
//					}
				}
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
	
	/*
	 * Stored media player listeners
	 */
	private final List<VLCMediaPlayerListener> _mediaListeners =
		Collections.synchronizedList(new ArrayList<VLCMediaPlayerListener>());
	/**
	 * Add a new media player listener to the
	 * VLC media player.  Listeners added using
	 * this method will be automatically added to
	 * new players created by this object.
	 * 
	 * @param listener
	 */
	public void addMediaPlayerListener(VLCMediaPlayerListener listener) {
		if(!_mediaListeners.contains(listener))
			_mediaListeners.add(listener);
		if(getMediaPlayer() != null)
			getMediaPlayer().addMediaPlayerListener(listener);
	}
	
	/**
	 * Return the underlying VLC media player
	 * object.
	 * 
	 * @return the current VLC media player - this
	 * value can change during playback (e.g., looping)
	 */
	public VLCMediaPlayer getMediaPlayer() {
		return this.mediaPlayer;
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
	}

	public PhonPlayerCanvas getCanvas() {
		return this.playerCanvas;
	}
	
	/**
	 * Load media from the given location
	 *
	 * @param location
	 */
	private static final String[] defaultOptions = 
    {   
        "-I", "dummy",  // Don't use an interface
        "--ignore-config", // Don't use VLC's default config
        "--no-osd", // No on screen display
        "--no-media-library", // we don't need the media library
        "--no-plugins-cache",
	"--no-xlib"
//        "--file-caching", "0" // don't cache for local files
//        "--loop"
//        "--play-and-pause" // pause, don't stop, at eof
        			
    }; 
	public void loadMedia(String loc, boolean useLastPosition) {
		if(vlcInstance == null) {
			try {
				vlcInstance = VLCInstance.getInstance();
				
				List<String> opts = new ArrayList<String>();
				for(String defOpt:defaultOptions)
					opts.add(defOpt);
				
				if(!PhonUtilities.isLinux()) {
					
					// see if the embedded version of VLC is installed
					File embeddedVLCPlugins = 
						( PhonUtilities.isMacOs() 
								? new File("lib/jni/vlclib/plugins")
								: new File("lib/jni/plugins") );
					if(embeddedVLCPlugins.exists() && embeddedVLCPlugins.isDirectory()) {
						String pluginLoc = 
							"--plugin-path=" +
				        	( PhonUtilities.isMacOs() 
				        			? (new File("lib/jni/vlclib/plugins")).getAbsolutePath()
				        			: (new File("lib/jni/plugins")).getAbsolutePath() );
						opts.add(pluginLoc);
					}
				}
				
				vlcInstance.init(opts.toArray(new String[0]));
				
			} catch (VLCException e) {
				getCanvas().setMessage(e.getMessage());
				PhonLogger.severe(e.toString());
				return;
			}
		}

		if(mediaListener == null)
			mediaListener = new MediaPlayerListener();

		long startTime = 0L;
		String oldMediaPath = "";

		if(useLastPosition && mediaPlayer != null) {
			try {
				for(VLCMediaPlayerListener listener:mediaPlayer.getMediaPlayerLisetners())
					mediaPlayer.removeMediaPlayerListener(listener);
				
				segmentListener = null;
				
				startTime = mediaPlayer.getTime();
				mediaPlayer.stop();
				if(mediaPlayer.getMedia() != null) {
					VLCMedia m = mediaPlayer.getMedia();
					oldMediaPath = m.getMRL();
				//	mediaPlayer.setMedia(null);
					m.free();
				}
				mediaPlayer.free();
			} catch (VLCException ex) {
				getCanvas().setMessage(ex.getMessage());
				PhonLogger.severe(ex.toString());
			}
		}

		try {
			mediaPlayer = vlcInstance.newMediaPlayer();
			mediaPlayer.addMediaPlayerListener(mediaListener);
		} catch (VLCException ex) {
			getCanvas().setMessage(ex.getMessage());
			PhonLogger.severe(ex.toString());
		}
//		if(playerCanvas != null) {
//			super.remove(playerCanvas);
//			playerCanvas = new PhonPlayerCanvas();
//			super.add(playerCanvas, BorderLayout.CENTER);
		try {
			mediaPlayer.setVideoOut(playerCanvas);
		} catch (VLCException ex) {
			getCanvas().setMessage(ex.getMessage());
			PhonLogger.severe(ex.toString());
		}
//		}

		try {
			final File f = new File(loc);
			final URI uri = f.toURI();
			final String asciiURI = uri.toASCIIString();
			final String uriLoc = asciiURI.replaceFirst("file\\:", "file://");
//			String uriLoc = 
//				"file://" + (new File(loc)).toURI().getPath();
			VLCMedia media = vlcInstance.newFromLocation(uriLoc);
			media.addOption(":play-and-pause");
			
//			media.addOption(":delay-time=-0.5");
			String newMediaPath = media.getMRL();
			
			mediaPlayer.setMedia(media);
			mediaPlayer.play();

			// re-set time if loading the same file
			if(useLastPosition && newMediaPath.equals(oldMediaPath))
				mediaPlayer.setTime(startTime);

			mediaFile = loc;
		} catch (VLCException e) {
			getCanvas().setMessage(e.getMessage());
			PhonLogger.severe(e.toString());
		} 
		
		for(VLCMediaPlayerListener listener:_mediaListeners) {
			mediaPlayer.addMediaPlayerListener(listener);
		}
	}

	/* UI actions */
	public void toggleVolumeMute(PhonActionEvent pae) {
		VLCMediaPlayer player = getMediaPlayer();
		if(player != null) {
			try {
				boolean isMuted = !player.isMuted();
				player.toggleMute();

				if(isMuted) {
					getVolumeButton().getAction().putValue(Action.SMALL_ICON, volMuteIcn);
				} else {
					getVolumeButton().getAction().putValue(Action.SMALL_ICON, volIcn);
				}
				getVolumeButton().repaint();

				getVolumeSlider().setEnabled(!isMuted);
			} catch (VLCException e) {
				e.printStackTrace();;
			}
		}
	}

	public void onVolumeBtn(PhonActionEvent pae) {
		VLCMediaPlayer player = getMediaPlayer();
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
			try {
				if(!mediaPlayer.isMuted())
					volumeSlider.setValue(mediaPlayer.getVolume());
				else
					volumeSlider.setEnabled(false);
			} catch (VLCException ex) {
				PhonLogger.severe(ex.toString());
			}
			

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
		VLCMediaPlayer player = getMediaPlayer();
		if(player != null) {
			try {
				if(player.isPlaying()) {
					player.pause();
				} else {
					player.play();
				}
			} catch (VLCException e) {
				e.printStackTrace();
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
		if(isShowing() && mediaFile != null) {
			loadMedia(mediaFile, true);
		}
	}

	public void onTakeSnapshot(PhonActionEvent pae) {
		VLCMediaPlayer player = getMediaPlayer();
		if(player != null
				&& player.getMedia() != null) {
			try{
				boolean isPlaying = player.isPlaying();

				// if we are playing, pause while we
				// bring up the save dialog
				if(isPlaying) {
					player.pause();
				}

				FileFilter[] filters = new FileFilter[1];
				filters[0] = new FileFilter("PNG files (*.png)", "*.png");
				String saveTo =
						NativeDialogs.showSaveFileDialogBlocking(
							CommonModuleFrame.getCurrentFrame(),
							System.getProperty("user.home") + File.separator + "Desktop",
							"png", filters, "Save snapshot");

				if(isPlaying) {
					// start player again
					player.play();
				}

				if(saveTo != null) {
					player.takeSnapshot(saveTo);
				}

			} catch (VLCException ex) {
				PhonLogger.warning(ex.toString());
			}

		}
		
	}

	public void onToggleVideo(PhonActionEvent pae) {
//		boolean isCollapsed =
//				playerPane.isCollapsed();
//		System.out.println(isCollapsed);
//		playerPane.setCollapsed(!isCollapsed);
//		playerPane..
//		boolean isCanvasVisible = playerCanvas.isVisible();
//		playerCanvas.setVisible(!isCanvasVisible);
//		revalidate();
	}

	private SegmentListener segmentListener;
	/**
	 * Playback given segment
	 *
	 * @param startTime in ms
	 * @param length in ms
	 */
	public void playSegment(long startTime, long length) {
		VLCMediaPlayer player = getMediaPlayer();
		
		if(player != null
				&& player.getMedia() != null) {
			long endTime = startTime + length;
			

			if(segmentListener == null) {
				segmentListener = new SegmentListener(endTime);
				player.addMediaPlayerListener(segmentListener);
			} else {
				segmentListener.setStopTime(endTime);
			}

//			SegmentListener listener = new SegmentListener(endTime);
			try {
				player.setTime(startTime);
//				player.addMediaPlayerListener(listener);

				if(!player.isPlaying())
					player.play();
			} catch (VLCException ex) {
				PhonLogger.warning(ex.toString());
			}
		}
	}
	
	public boolean isMediaNeedsReload() {
		return reloadMedia;
	}

	public void setMediaNeedsReload(boolean b) {
		reloadMedia = b;
		if(this.mediaFile == null)
			playerCanvas.setMessage("Media not found");
		else
			playerCanvas.setMessage("Click play...");
		playerCanvas.repaint();
	}

	/**
	 *
	 */


	/**
	 * Listener for user changes to the position slider
	 *
	 */
	private class PositionListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent ce) {
			if(getPositionSlider().getValueIsAdjusting()) {
				int sliderPos = getPositionSlider().getValue();
				float pos = (float)sliderPos / SLIDER_MAX;
				if(pos < 1.0f) {
					try {
						VLCMediaPlayer mediaPlayer = getMediaPlayer();
						if(mediaPlayer != null)
							mediaPlayer.setPosition(pos);
					} catch (VLCException ex) {
						PhonLogger.warning(ex.toString());
					}
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

//			menu.add(getLoadMediaItem());
//			menu.addSeparator();
//			menu.add(getToggleVideoItem());
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

		private JMenuItem getToggleVideoItem() {
			PhonUIAction toggleVideoAct =
					new PhonUIAction(PhonMediaPlayer.this, "onToggleVideo");
			toggleVideoAct.putValue(Action.NAME, "Toggle video");
			toggleVideoAct.putValue(Action.SHORT_DESCRIPTION, "Display/hide video panel");

			JMenuItem retVal = new JMenuItem(toggleVideoAct);
			return retVal;
		}
	}

	/**
	 * Listener for segment playback.
	 */
	private class SegmentListener extends VLCMediaPlayerAdapter {

		private volatile long stopTime = -1L;
		private Lock stopTimeMutex = new ReentrantLock();

		public SegmentListener(long stopTime) {
			stopTimeMutex.lock();
			this.stopTime = stopTime;
			stopTimeMutex.unlock();
		}

		@Override
		public void onPaused(VLCMediaPlayerEvent vlcmpe) {
			// remove ourselves from the listener list
//			VLCMediaPlayer player = vlcmpe.getSource();
//			player.removeMediaPlayerListener(this);
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
		public void onTimeChanged(VLCMediaPlayerEvent vlcmpe) {
			VLCMediaPlayer player = vlcmpe.getSource();
			stopTimeMutex.lock();
			try {
				long time = player.getTime();

				if(stopTime >= 0 && time >= stopTime) {
					player.pause();
				}
			} catch (VLCException ex) {
				PhonLogger.warning(ex.toString());
			} finally {
				stopTimeMutex.unlock();
			}
		}

	}

	/**
	 * Media player listener
	 */
	private class MediaPlayerListener extends VLCMediaPlayerAdapter {

		@Override
		public void onOpening(VLCMediaPlayerEvent arg0) {
			playerCanvas.setMessage("Opening...");
			playerCanvas.repaint();
		}

		@Override
		public void onBuffering(VLCMediaPlayerEvent arg0) {
			playerCanvas.setMessage("Buffering....");
			playerCanvas.repaint();
		}

		@Override
		public void onPlaying(VLCMediaPlayerEvent vlcmpe) {
			playerCanvas.setMessage("");
			// change icon on playPause btn
			getPlayPauseButton().getAction().putValue(Action.SMALL_ICON, pauseIcn);
		}

		@Override
		public void onPaused(VLCMediaPlayerEvent vlcmpe) {
			getPlayPauseButton().getAction().putValue(Action.SMALL_ICON, playIcn);
		}

		@Override
		public void onPositionChanged(VLCMediaPlayerEvent vlcmpe) {
			try {
				// update slider location
				VLCMediaPlayer player = vlcmpe.getSource();
				float pos = player.getPosition();
				int sliderLoc = Math.round(SLIDER_MAX * pos);
				if(!getPositionSlider().getValueIsAdjusting())
					getPositionSlider().setValue(sliderLoc);
			} catch (VLCException ex) {
				PhonLogger.warning(ex.toString());
			}
		}

		@Override
		public void onStopped(VLCMediaPlayerEvent arg0) {
			
		}

		@Override
		public void onEndReached(VLCMediaPlayerEvent arg0) {
			if(isShowing() && mediaFile != null) {
//				try {
//					mediaPlayer.free();
//				} catch (VLCException e) {
//					e.printStackTrace();
//					PhonLogger.warning(e.getMessage());
//				}
					mediaPlayer = null;
				// media player
				loadMedia(mediaFile, false);
			}
		}

		
	}
	
}
