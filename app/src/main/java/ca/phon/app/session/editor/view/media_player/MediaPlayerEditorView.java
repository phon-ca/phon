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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.text.ParseException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.text.MaskFormatter;

import org.apache.logging.log4j.LogManager;

import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.DockPosition;
import ca.phon.app.session.editor.EditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.media_player.actions.GoToAction;
import ca.phon.app.session.editor.view.media_player.actions.GoToEndOfSegmentedAction;
import ca.phon.app.session.editor.view.media_player.actions.PlayAdjacencySequenceAction;
import ca.phon.app.session.editor.view.media_player.actions.PlayCustomSegmentAction;
import ca.phon.app.session.editor.view.media_player.actions.PlaySegmentAction;
import ca.phon.app.session.editor.view.media_player.actions.PlaySpeechTurnAction;
import ca.phon.app.session.editor.view.media_player.actions.TakeSnapshotAction;
import ca.phon.app.session.editor.view.media_player.actions.ToggleAdjustVideoAction;
import ca.phon.media.VLCHelper;
import ca.phon.media.player.IMediaMenuFilter;
import ca.phon.media.player.PhonMediaPlayer;
import ca.phon.media.util.MediaLocator;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.SegmentCalculator;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
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

	private PhonMediaPlayer mediaPlayer;

	public MediaPlayerEditorView(SessionEditor editor) {
		super(editor);

		init();
	}

	private void init() {
		setLayout(new BorderLayout());
		mediaPlayer = new PhonMediaPlayer();
		mediaPlayer.addMediaMenuFilter(new MediaMenuFilter());
		add(mediaPlayer, BorderLayout.CENTER);

		setupEditorActions();

		// load media if available
		final String mediaFilePath = getMediaFilePath();
		if(mediaFilePath != null)
			mediaPlayer.setMediaFile(mediaFilePath);
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

	public void onPlayCustomSegment(PhonActionEvent pae) {
		final PlayCustomSegmentDialog dialog = new PlayCustomSegmentDialog(getEditor(), mediaPlayer);
		dialog.setSize(new Dimension(300, 320));
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}

	public void onLoadMedia(EditorEvent ee) {
		loadMedia();
	}

	public void reloadMedia() {
		loadMedia();
	}

	private void loadMedia() {
		final Session t = getEditor().getSession();
		final File mediaFile =
				MediaLocator.findMediaFile(getEditor().getProject(), t);
		if(mediaFile != null) {
			// first check to make sure VLC was found, if not issue a message
			// and return
			if(!VLCHelper.checkNativeLibrary(true)) return;

			mediaPlayer.loadMedia(mediaFile.getAbsolutePath());
		}
//		else {
//			mediaPlayer.getCanvas().setMessage("Media not found");
//			mediaPlayer.getCanvas().repaint();
//		}
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
		if(mediaPlayer.getMediaFile() != null) {
			mediaPlayer.stop();
		}
		String mediaRef = getEditor().getSession().getMediaLocation();
		File mediaFile =
				MediaLocator.findMediaFile(mediaRef, getEditor().getProject(), getEditor().getSession().getCorpus());
		if(mediaFile != null)
			mediaPlayer.setMediaFile(mediaFile.getAbsolutePath());
		else
			mediaPlayer.setMediaFile(null);
		mediaPlayer.loadMedia();
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

	// called when the docking window containing this component is closed
	@Override
	public void onClose() {
		if(mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
		}
	}

	/**
	 *  Menu actions
	 */
//	public void onExportMedia(PhonActionEvent pae) {
//		String mediaFilePath = getMediaFilePath();
//		if(mediaFilePath != null) {
//			File f = new File(mediaFilePath);
//			String name = f.getName();
//			int extDotIdx = name.lastIndexOf(".");
//			String ext = "";
//			if(extDotIdx > 0) {
//				ext = name.substring(extDotIdx);
//			}
//
//			File projFile = new File(getEditor().getProject().getLocation());
//			File resFile = new File(projFile, "__res");
//			File mediaResFile = new File(resFile, "media");
//			File segmentFile = new File(mediaResFile, "segments");
//			if(!segmentFile.exists()) {
//				segmentFile.mkdirs();
//			}
//
//			File outputFile = new File(segmentFile,
//					getEditor().getSession().getName() + "_" + getEditor().getSession().getCorpus() + "_" + (getEditor().getCurrentRecordIndex()+1) + ext);
//						int fIdx = 0;
//			while(outputFile.exists()) {
//				outputFile = new File(segmentFile,
//						getEditor().getSession().getName() + "_" + getEditor().getSession().getCorpus() + "_" + (getEditor().getCurrentRecordIndex()+1) +
//						"(" + (++fIdx) + ")" + ext);
//			}
//
//			final VLCMediaExporter exporter =
//					new VLCMediaExporter(new File(mediaFilePath), outputFile, Preset.H264_HIGH);
//			getEditor().getStatusBar().watchTask(exporter);
//			PhonWorker.getInstance().invokeLater(exporter);
//		}
//	}

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

	/**
	 * Play the media from the beginning of the current record's
	 * segment to the end of the contiguous section for the
	 * current speaker.
	 */
	public void onPlaySpeakerSegment(PhonActionEvent pae) {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		final Record utt = editor.currentRecord();

		final Participant speaker = utt.getSpeaker();
		if(speaker == null) return;

		Record firstRecord = utt;
		Record lastRecord = utt;

		// if contiguous
		if(pae.getData() != null && pae.getData() instanceof Boolean && ((Boolean)pae.getData()).booleanValue()) {
			for(int rIdx = editor.getCurrentRecordIndex()-1; rIdx >= 0; rIdx--) {
				if(session.getRecord(rIdx).getSpeaker() == speaker) {
					firstRecord = session.getRecord(rIdx);
				} else {
					break;
				}
			}

			for(int rIdx = editor.getCurrentRecordIndex()+1; rIdx < session.getRecordCount(); rIdx++) {
				if(session.getRecord(rIdx).getSpeaker() == speaker) {
					lastRecord = session.getRecord(rIdx);
				} else {
					break;
				}
			}
		}

		final MediaSegment firstSegment = firstRecord.getSegment().getGroup(0);
		final MediaSegment lastSegment = lastRecord.getSegment().getGroup(0);

		if(firstSegment != null && lastSegment != null) {
			final long startTime = (long)firstSegment.getStartValue();
			final long endTime = (long)lastSegment.getEndValue();

			mediaPlayer.playSegment(startTime, (endTime-startTime));
		}

	}

	public void onPlayConvPeriod() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		final MediaSegment segment =
				SegmentCalculator.conversationPeriod(session, editor.getCurrentRecordIndex());

		final long len = (long)(segment.getEndValue() - segment.getStartValue());
		if(len > 0)
			mediaPlayer.playSegment((long)segment.getStartValue(), len);
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

	/**
	 * Media player menu filter
	 */
	private class MediaMenuFilter implements IMediaMenuFilter {

		@Override
		public JPopupMenu makeMenuChanges(JPopupMenu menu) {
			JPopupMenu retVal = menu;

			setupPlaytoItems(menu);

			menu.addSeparator();

			setupGotoItems(menu);

			return retVal;
		}

		private void setupPlaytoItems(JPopupMenu menu) {
			final JMenuItem playCustomItem = new JMenuItem(new PlayCustomSegmentAction(getEditor(), MediaPlayerEditorView.this));
			menu.add(playCustomItem);

			final JMenuItem playSegmentItem = new JMenuItem(new PlaySegmentAction(getEditor(), MediaPlayerEditorView.this));
			menu.add(playSegmentItem);

			final JMenuItem playContiguousItem = new JMenuItem(new PlaySpeechTurnAction(getEditor(), MediaPlayerEditorView.this));
			menu.add(playContiguousItem);

//			final JMenuItem playConvPeriodItem = new JMenuItem(new PlayAdjacencySequenceAction(getEditor(), MediaPlayerEditorView.this));
//			menu.add(playConvPeriodItem);
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

	@Override
	public ImageIcon getIcon() {
		return IconManager.getInstance().getIcon("apps/vlc", IconSize.SMALL);
	}

	@Override
	public JMenu getMenu() {
		final JMenu menu = new JMenu();

		menu.add(new TakeSnapshotAction(getEditor(), this));
		menu.addSeparator();
		menu.add(new PlayCustomSegmentAction(getEditor(), this));
		menu.add(new PlaySegmentAction(getEditor(), this));
		menu.add(new PlaySpeechTurnAction(getEditor(), this));
		menu.add(new PlayAdjacencySequenceAction(getEditor(), this));
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

}