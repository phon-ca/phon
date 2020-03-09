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

package ca.phon.app.session.editor.view.media_player;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.text.MaskFormatter;

import org.apache.logging.log4j.LogManager;
import org.jdesktop.swingx.VerticalLayout;

import ca.phon.app.session.EditorViewAdapter;
import ca.phon.app.session.SessionMediaModel;
import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.DockPosition;
import ca.phon.app.session.editor.EditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.ErrorBanner;
import ca.phon.app.session.editor.PlayCustomSegmentDialog;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.AssignMediaAction;
import ca.phon.app.session.editor.actions.PlayAdjacencySequenceAction;
import ca.phon.app.session.editor.actions.PlayCustomSegmentAction;
import ca.phon.app.session.editor.actions.PlaySegmentAction;
import ca.phon.app.session.editor.actions.PlaySpeechTurnAction;
import ca.phon.app.session.editor.undo.MediaLocationEdit;
import ca.phon.app.session.editor.view.media_player.actions.GoToAction;
import ca.phon.app.session.editor.view.media_player.actions.GoToEndOfSegmentedAction;
import ca.phon.app.session.editor.view.media_player.actions.TakeSnapshotAction;
import ca.phon.app.session.editor.view.media_player.actions.ToggleAdjustVideoAction;
import ca.phon.media.VLCHelper;
import ca.phon.media.player.IMediaMenuFilter;
import ca.phon.media.player.PhonMediaPlayer;
import ca.phon.media.util.MediaLocator;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.position.SegmentCalculator;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.dnd.FileTransferHandler;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.util.MsFormatter;
import ca.phon.util.PrefHelper;
import ca.phon.util.Tuple;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Panel for embedded media player for editor.
 *
 */
public class MediaPlayerEditorView extends EditorView {

	private final static org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(MediaPlayerEditorView.class.getName());

	public static final String VIEW_TITLE = "Media Player";
	
	/** 
	 * Custom editor event signaled when media loaded 
	 * Event data: media file (as string)
	 */
	public static final String MEDIA_LOADED_EVENT = "_media_loaded_";
	
	public static final String MEDIA_UNLOADED_EVENT = "_media_unloaded_";

	private PhonMediaPlayer mediaPlayer;
	
	private JPanel errorPanel;
	private ErrorBanner messageButton = new ErrorBanner();
	
	public MediaPlayerEditorView(SessionEditor editor) {
		super(editor);

		init();
		addEditorViewListener(editorViewListener);
	}

	private void init() {
		setLayout(new BorderLayout());
		mediaPlayer = new PhonMediaPlayer();
		mediaPlayer.addMediaMenuFilter(new MediaMenuFilter());
		mediaPlayer.addPropertyChangeListener("mediaLoaded", (e) -> {
			if((boolean)e.getNewValue()) {
				EditorEvent ee = new EditorEvent(MEDIA_LOADED_EVENT, MediaPlayerEditorView.this, mediaPlayer.getMediaFile());
				getEditor().getEventManager().queueEvent(ee);
			} else {
				EditorEvent ee = new EditorEvent(MEDIA_UNLOADED_EVENT, MediaPlayerEditorView.this);
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
	 * @throws VLCException
	 */

	public void onLoadMedia(PhonActionEvent pae) {
		loadMedia();
	}

	public void onLoadMedia(EditorEvent ee) {
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
			
			messageButton.setVisible(false);
		} else {
			mediaPlayer.setMediaFile(null);
			messageButton.setVisible(true);
		}
	}

	private void setupEditorActions() {
		final EditorAction mediaChangedAct =
				new DelegateEditorAction(this, "onMediaChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SESSION_MEDIA_CHANGED,
				mediaChangedAct);

		final EditorAction recordChangedAct =
				new DelegateEditorAction(this, "onRecordChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_CHANGED_EVT, recordChangedAct);

		final EditorAction segmentPlaybackAct =
				new DelegateEditorAction(this, "doSegmentPlayback");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SEGMENT_PLAYBACK_EVENT, segmentPlaybackAct);

		final EditorAction edtiorClosingAct =
				new DelegateEditorAction(this, "doCleanup");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.EDITOR_CLOSING, edtiorClosingAct);
	}

	/** Editor actions */
	public void onMediaChanged(EditorEvent ee) {
		loadMedia();
	}

	public void onRecordChanged(EditorEvent ee) {
		if(!isAdjustVideo()) return;
		final Record utt = getEditor().currentRecord();
		if(utt == null) return;

		final MediaSegment media = utt.getSegment().getGroup(0);

		// check for necessary vars
		if(media == null) return;
		if(!mediaPlayer.willPlay()) return;

		// don't set position if player is playing
		if(mediaPlayer.isPlaying()) return;

		mediaPlayer.setTime((long)media.getStartValue());
	}

	public void doSegmentPlayback(EditorEvent ee) {
		Tuple<Integer, Integer> segment =
				(Tuple<Integer,Integer>)ee.getEventData();

		long startTime = segment.getObj1();
		long length = segment.getObj2();

		mediaPlayer.playSegment(startTime, length);
	}

	@RunOnEDT
	public void doCleanup(EditorEvent ee) {
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
		if(utt.getSegment().getGroup(0) != null) {
			long startTime = (long)utt.getSegment().getGroup(0).getStartValue();
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
					LOGGER.error( e.getLocalizedMessage(), e);
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
				if(utt.getSegment().numberOfGroups() == 0) continue;

				if(utt.getSegment().getGroup(0).getEndValue() > lastSegment.getEndValue())
					lastSegment = utt.getSegment().getGroup(0);
			}
		} else {
			final Participant p = (Participant)pae.getData();
			for(int uttIdx = t.getRecordCount()-1; uttIdx >= 0; uttIdx--) {
				final Record utt = t.getRecord(uttIdx);
				if(utt.getSpeaker() != null
						&& utt.getSpeaker().getId() == p.getId()) {
					lastSegment = utt.getSegment().getGroup(0);
					break;
				}
			}
		}

		mediaPlayer.setTime((long) lastSegment.getEndValue());
	}

	public void onMenuPlayto(PhonActionEvent pae) {

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
