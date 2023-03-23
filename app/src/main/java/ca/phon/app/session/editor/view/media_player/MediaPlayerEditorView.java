/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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

package ca.phon.app.session.editor.view.media_player;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.EditorViewAdapter;
import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.actions.AssignMediaAction;
import ca.phon.app.session.editor.undo.MediaLocationEdit;
import ca.phon.app.session.editor.view.media_player.actions.*;
import ca.phon.media.*;
import ca.phon.media.player.*;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.ui.action.*;
import ca.phon.ui.dnd.FileTransferHandler;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.util.*;
import ca.phon.util.icons.*;
import org.apache.logging.log4j.LogManager;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.*;
import java.text.ParseException;

/**
 * Panel for embedded media player for editor.
 *
 */
public class MediaPlayerEditorView extends EditorView {

	public static final String VIEW_TITLE = "Media Player";
	
	/** 
	 * Custom editor event signaled when media loaded 
	 * Event data: media file (as string)
	 */
	public static final EditorEventType<MediaPlayerEditorView> MediaLoaded = new EditorEventType<>("_media_loaded_", MediaPlayerEditorView.class);

	public static final EditorEventType<MediaPlayerEditorView> MediaUnloaded = new EditorEventType<>("_media_unloaded_", MediaPlayerEditorView.class);

	private PhonMediaPlayer mediaPlayer;
	
	private JPanel errorPanel;
	private ErrorBanner messageButton = new ErrorBanner();
	
	public MediaPlayerEditorView(SessionEditor editor) {
		super(editor);

		init();
		addEditorViewListener(editorViewListener);
		editor.getMediaModel().addPropertyChangeListener((evt) -> {
			switch(evt.getPropertyName()) {
				case "playbackRate":
					if(mediaPlayer != null) {
						mediaPlayer.setRate(editor.getMediaModel().getPlaybackRate());
					}
					break;

			default:
				break;
			}
		});
	}

	private void init() {
		setLayout(new BorderLayout());
		mediaPlayer = new PhonMediaPlayer(getEditor().getMediaModel().getVolumeModel());
		mediaPlayer.addMediaMenuFilter(new MediaMenuFilter());
		mediaPlayer.addPropertyChangeListener("mediaLoaded", (e) -> {
			if((boolean)e.getNewValue()) {
				EditorEvent<MediaPlayerEditorView> ee = new EditorEvent<>(MediaLoaded, this, this);
				getEditor().getEventManager().queueEvent(ee);
			} else {
				EditorEvent<MediaPlayerEditorView> ee = new EditorEvent(MediaUnloaded, this, this);
				getEditor().getEventManager().queueEvent(ee);
			}
		});
		mediaPlayer.getMediaPlayerCanvas().setTransferHandler(new FileSelectionTransferHandler());
		
		add(mediaPlayer, BorderLayout.CENTER);
		
		final AssignMediaAction browseForMediaAct = new AssignMediaAction(getEditor());
		browseForMediaAct.putValue(AssignMediaAction.LARGE_ICON_KEY, browseForMediaAct.getValue(AssignMediaAction.SMALL_ICON));
		
		messageButton.setDefaultAction(browseForMediaAct);
		messageButton.addAction(browseForMediaAct);

		messageButton.setTopLabelText("<html><b>Session media not available</b></html>");
		messageButton.setBottomLabelText("<html>Click here to assign media file to session.</html>");
		messageButton.setVisible(!getEditor().getMediaModel().isSessionMediaAvailable());
		
		errorPanel = new JPanel(new VerticalLayout());
		errorPanel.add(messageButton);

		add(errorPanel, BorderLayout.SOUTH);

		setupEditorActions();

		// load media if available
		final String mediaFilePath = getMediaFilePath();
		if(mediaFilePath != null)
			SwingUtilities.invokeLater( this::loadMedia );
	}

	@Override
	public String getName() {
		return VIEW_TITLE;
	}

	public PhonMediaPlayer getPlayer() {
		return this.mediaPlayer;
	}

	/**
	 * Return the media file path or null if not found
	 */
	private String getMediaFilePath() {
		final Session t = getEditor().getSession();
		File mediaFile =
				MediaLocator.findMediaFile(getEditor().getProject(), t);
		if(mediaFile != null) {
			return mediaFile.getAbsolutePath();
		}
		return null;
	}

	/**
	 * Called when we need to refresh the media player.
	 * Media players need to be refreshed when the
	 * PlayerCanvas they are listening to becomes invalid.
	 * @param pae
	 */
	public void onLoadMedia(PhonActionEvent pae) {
		loadMedia();
	}

	public void onLoadMedia(EditorEvent ee) {
		loadMedia();
	}

	public void onSessionChanged(EditorEvent<Session> ee) {
		loadMedia();
	}

	public void reloadMedia() {
		loadMedia();
	}

	private void loadMedia() {
		final SessionMediaModel mediaModel = getEditor().getMediaModel();
		if(mediaModel.isSessionMediaAvailable()) {
			// first check to make sure VLC was found, if not issue a message
			// and return
			if(!VLCHelper.checkNativeLibrary(true)) return;

			final File mediaFile = mediaModel.getSessionMediaFile();
			mediaPlayer.loadMedia(mediaFile.getAbsolutePath());
			mediaPlayer.setRate(getEditor().getMediaModel().getPlaybackRate());
			
			messageButton.setVisible(false);
		} else {
			mediaPlayer.setMediaFile(null);
			messageButton.setVisible(true);
		}
	}

	private void setupEditorActions() {
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SessionMediaChanged, this::onMediaChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SessionChanged, this::onSessionChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RecordChanged, this::onRecordChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SegmentPlayback, this::doSegmentPlayback, EditorEventManager.RunOn.AWTEventDispatchThread);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.EditorClosing, this::doCleanup, EditorEventManager.RunOn.AWTEventDispatchThread);
	}

	/** Editor actions */
	private void onMediaChanged(EditorEvent<EditorEventType.SessionMediaChangedData> ee) {
		loadMedia();
	}

	private void onRecordChanged(EditorEvent<EditorEventType.RecordChangedData> ee) {
		if(!isAdjustVideo()) return;
		final Record utt = getEditor().currentRecord();
		if(utt == null) return;

		final MediaSegment media = utt.getMediaSegment();

		// check for necessary vars
		if(media == null) return;
		if(!mediaPlayer.willPlay()) return;

		// don't set position if player is playing
		if(mediaPlayer.isPlaying()) return;

		mediaPlayer.setTime((long)media.getStartValue());
	}

	private void doSegmentPlayback(EditorEvent<MediaSegment> ee) {
		MediaSegment segment = ee.data();

		long startTime = (long)segment.getStartValue();
		long length = (long)(segment.getStartValue() - segment.getEndValue());

		mediaPlayer.playSegment(startTime, length);
	}

	private void doCleanup(EditorEvent ee) {
		mediaPlayer.cleanup();
	}

	// popup frame for time selection
	private JFrame timeSelectionPopup = null;

	public void onMenuSelectGoto(PhonActionEvent pae)
		throws ParseException {
		// display a popup window with
		// a formatted text field to select media time

		String defValue = "000:00.000";
		Record utt = getEditor().currentRecord();
		if(utt.getMediaSegment() != null) {
			long startTime = (long)utt.getMediaSegment().getStartValue();
			defValue = MsFormatter.msToDisplayString(startTime);
		}

		MaskFormatter formatter = new MaskFormatter("###:##.###");
		formatter.setPlaceholderCharacter('0');
		JFormattedTextField formattedTextField =
				new JFormattedTextField(formatter);
		formattedTextField.setText(defValue);
		formattedTextField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {
				String timeStr =
						((JFormattedTextField)ae.getSource()).getText();
				long msVal;
				try {
					msVal = MsFormatter.displayStringToMs(timeStr);
					mediaPlayer.setTime(msVal);
					if(timeSelectionPopup != null) {
						timeSelectionPopup.dispose();
						timeSelectionPopup = null;
					}
				} catch (ParseException e) {
					LogUtil.warning(e);
				}
			}

		});

		JButton menuBtn = mediaPlayer.getMenuButton();

		if(timeSelectionPopup == null) {
			timeSelectionPopup = new JFrame();
			timeSelectionPopup.setUndecorated(true);
			timeSelectionPopup.addWindowFocusListener(new WindowFocusListener() {

				@Override
				public void windowGainedFocus(WindowEvent we) {
				}

				@Override
				public void windowLostFocus(WindowEvent we) {
					if(timeSelectionPopup != null) {
						timeSelectionPopup.setVisible(false);
						timeSelectionPopup = null;
					}
				}
			});

			timeSelectionPopup.add(formattedTextField);

			Point p = menuBtn.getLocation();
			SwingUtilities.convertPointToScreen(p, menuBtn.getParent());
			// setup bounds
			Rectangle windowBounds = new Rectangle(
					p.x,
					p.y - menuBtn.getHeight(),
					timeSelectionPopup.getPreferredSize().width,
					timeSelectionPopup.getPreferredSize().height);
			timeSelectionPopup.setBounds(windowBounds);
			timeSelectionPopup.setVisible(true);
		}
	}

	public void onMenuGoto(PhonActionEvent pae) {
		final SessionFactory factory = SessionFactory.newFactory();
		MediaSegment lastSegment = factory.createMediaSegment();
		lastSegment.setStartValue(0.0f);
		lastSegment.setEndValue(0.0f);

		final Session t = getEditor().getSession();

		if(pae.getData() == null) {
			for(int uttIdx = t.getRecordCount()-1; uttIdx >= 0; uttIdx--) {
				final Record utt = t.getRecord(uttIdx);
				if(utt.getMediaSegment().getEndValue() > lastSegment.getEndValue())
					lastSegment = utt.getMediaSegment();
			}
		} else {
			final Participant p = (Participant)pae.getData();
			for(int uttIdx = t.getRecordCount()-1; uttIdx >= 0; uttIdx--) {
				final Record utt = t.getRecord(uttIdx);
				if(utt.getSpeaker() != null
						&& utt.getSpeaker().getId() == p.getId()) {
					lastSegment = utt.getMediaSegment();
					break;
				}
			}
		}

		mediaPlayer.setTime((long) lastSegment.getEndValue());
	}

	private final static String ADJUST_VIDEO = MediaPlayerEditorView.class.getName() + ".adjustVideo";
	/**
	 * Toggle the option to trun on/off moving
	 * video with the current record (while
	 * paused.)
	 */
	public void onToggleAdjustVideo() {
		final Boolean isAdjustVideo = isAdjustVideo();
		PrefHelper.getUserPreferences().putBoolean(ADJUST_VIDEO, !isAdjustVideo);
	}

	/**
	 * Should the media position move with the
	 * current record.
	 */
	public boolean isAdjustVideo() {
		final Boolean isAdjustVideo = PrefHelper.getBoolean(ADJUST_VIDEO, Boolean.TRUE);
		return isAdjustVideo;
	}

	@Override
	public ImageIcon getIcon() {
		return IconManager.getInstance().getIcon("apps/vlc", IconSize.SMALL);
	}

	@Override
	public JMenu getMenu() {
		final JMenu menu = new JMenu();
	
		menu.add(new TakeSnapshotAction(getEditor(), this));
		menu.addSeparator();
		final ToggleAdjustVideoAction adjustVideoAct = new ToggleAdjustVideoAction(getEditor(), MediaPlayerEditorView.this);
		adjustVideoAct.putValue(PhonUIAction.SELECTED_KEY, isAdjustVideo());
		JCheckBoxMenuItem adjustVideoItem = new JCheckBoxMenuItem(adjustVideoAct);
		menu.add(adjustVideoItem);
		menu.addSeparator();
		menu.add(new GoToAction(getEditor(), this));
		menu.add(new GoToEndOfSegmentedAction(getEditor(), this));
	
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		// for each participant
		for(int i = 0; i < session.getParticipantCount(); i++) {
			final Participant p = session.getParticipant(i);
			final GoToEndOfSegmentedAction gotoPartSegmentAct =
					new GoToEndOfSegmentedAction(getEditor(), this, p);
			menu.add(gotoPartSegmentAct);
		}
	
		return menu;
	}

	@Override
	public DockPosition getPreferredDockPosition() {
		return DockPosition.WEST;
	}

	private final EditorViewAdapter editorViewListener = new EditorViewAdapter() {

		@Override
		public void onClosed(EditorView view) {
			if(mediaPlayer != null && mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
			}
		}
		
	};
	
	/**
	 * Media player menu filter
	 */
	private class MediaMenuFilter implements IMediaMenuFilter {

		@Override
		public JPopupMenu makeMenuChanges(JPopupMenu menu) {
			JPopupMenu retVal = menu;

			menu.addSeparator();

			setupGotoItems(menu);

			return retVal;
		}

		private void setupGotoItems(JPopupMenu menu) {
			final ToggleAdjustVideoAction adjustVideoAct = new ToggleAdjustVideoAction(getEditor(), MediaPlayerEditorView.this);
			adjustVideoAct.putValue(PhonUIAction.SELECTED_KEY, isAdjustVideo());
			JCheckBoxMenuItem adjustVideoItem = new JCheckBoxMenuItem(adjustVideoAct);
			menu.add(adjustVideoItem);

			final GoToAction gotoSelectAct = new GoToAction(getEditor(), MediaPlayerEditorView.this);
			JMenuItem gotoSelectItem = new JMenuItem(gotoSelectAct);
			menu.add(gotoSelectItem);

			final GoToEndOfSegmentedAction gotoLastSegmentAct = new GoToEndOfSegmentedAction(getEditor(), MediaPlayerEditorView.this);
			menu.add(gotoLastSegmentAct);

			final SessionEditor editor = getEditor();
			final Session session = editor.getSession();

			// for each participant
			for(int i = 0; i < session.getParticipantCount(); i++) {
				final Participant p = session.getParticipant(i);
				final GoToEndOfSegmentedAction gotoPartSegmentAct =
						new GoToEndOfSegmentedAction(getEditor(), MediaPlayerEditorView.this, p);
				menu.add(gotoPartSegmentAct);
			}
		}

	}

	private class FileSelectionTransferHandler extends FileTransferHandler {

		private static final long serialVersionUID = 6799990443658389742L;

		@Override
		public boolean importData(JComponent comp, Transferable transferable) {
			File file = null;
			try {
				file = getFile(transferable);
			} catch (IOException e) {
				return false;
			}
			
			if(file != null && FileFilter.mediaFilter.accept(file)) {
				MediaLocationEdit mediaEdit = new MediaLocationEdit(getEditor(), file.getAbsolutePath());
				getEditor().getUndoSupport().postEdit(mediaEdit);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public File getFile(Transferable transferable) throws IOException {
			File retVal = super.getFile(transferable);
			final FileFilter filter = FileFilter.mediaFilter;
			if(filter != null && !filter.accept(retVal)) {
				retVal = null;
			}
			return retVal;
		}

		@Override
		protected Transferable createTransferable(JComponent c) {
			return super.createTransferable(c);
		}
		
	}
}
