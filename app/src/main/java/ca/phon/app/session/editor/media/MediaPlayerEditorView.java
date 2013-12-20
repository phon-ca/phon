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

package ca.phon.app.session.editor.media;

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
import java.util.HashMap;

import javax.swing.Action;
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

import vlc4j.VLCError;
import vlc4j.VLCException;
import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.DockPosition;
import ca.phon.app.session.editor.EditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.media.VLCHelper;
import ca.phon.media.exportwizard.MediaExportWizard;
import ca.phon.media.exportwizard.MediaExportWizardProp;
import ca.phon.media.player.IMediaMenuFilter;
import ca.phon.media.player.PhonMediaPlayer;
import ca.phon.media.util.MediaLocator;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.SegmentCalculator;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.Tier;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.MsFormatter;
import ca.phon.util.PathExpander;
import ca.phon.util.PrefHelper;
import ca.phon.util.Tuple;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Panel for embedded media player for editor.
 *
 */
public class MediaPlayerEditorView extends EditorView {

	static final String VIEW_TITLE = "Media Player";

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

	public void onLoadMedia(PhonActionEvent pae)
		throws VLCException {
		loadMedia();
	}
	
	public void onPlayCustomSegment(PhonActionEvent pae) {
		final PlayCustomSegmentDialog dialog = new PlayCustomSegmentDialog(getEditor(), mediaPlayer);
		dialog.setSize(new Dimension(300, 320));
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}

	public void onLoadMedia(EditorEvent ee)
		throws VLCException {
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
		} else {
			mediaPlayer.getCanvas().setMessage("Media not found");
			mediaPlayer.getCanvas().repaint();
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
	public void onMediaChanged(EditorEvent ee)
		throws VLCException {
		if(mediaPlayer.getMediaFile() != null) {
			mediaPlayer.stop();
		}
		final PathExpander pe = new PathExpander();
		String mediaRef = pe.expandPath(getEditor().getSession().getMediaLocation());
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
		final MediaSegment media = utt.getSegment().getGroup(0);
		
		// check for necessary vars
		if(media == null) return;
		try {
			if(!mediaPlayer.willPlay()) return;
		} catch (VLCException e) {
			VLCError.logAndClear(e);
			return;
		}
		
		// don't set position if player is playing
		try {
			if(mediaPlayer.isPlaying()) return;
		} catch (VLCException e) {
			VLCError.logAndClear(e);
			return;
		}
		
		try {
			mediaPlayer.setTime((long)media.getStartValue());
		} catch (VLCException e) {
			VLCError.logAndClear(e);
		}
	}

	public void doSegmentPlayback(EditorEvent ee)
		throws VLCException {
		Tuple<Integer, Integer> segment =
				(Tuple<Integer,Integer>)ee.getEventData();

		long startTime = segment.getObj1();
		long length = segment.getObj2();

		mediaPlayer.playSegment(startTime, length);
	}

	public void doCleanup(EditorEvent ee) {
		try {
			mediaPlayer.stop();
		} catch (VLCException e) {
			VLCError.logAndClear(e);
		}
	}
	
	// called when the docking window containing this component is closed
	public void onClose() {
		try {
			if(mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
			}
		} catch (VLCException ex) {
			VLCError.logAndClear(ex);
		}
		
	}
	
	// called when the media player needs to pause and the media be reloaded
	public void onWindowViewChanged() {
		try {
			if(mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
			}
//			mediaPlayer.setMediaNeedsReload(true);
		} catch (VLCException ex) {
			VLCError.logAndClear(ex);
		}
	}

	/**
	 *  Menu actions
	 */
	public void onExportMedia(PhonActionEvent pae) {
		// show the export wizard
		// setup export for segment if one is found for the current record
		HashMap<MediaExportWizardProp, Object> wizardProps =
				new HashMap<MediaExportWizardProp, Object>();
		String mediaFilePath = getMediaFilePath();
		if(mediaFilePath != null) {
			wizardProps.put(MediaExportWizardProp.INPUT_FILE, mediaFilePath);
			
			File f = new File(mediaFilePath);
			String name = f.getName();
			int extDotIdx = name.lastIndexOf(".");
			String ext = "";
			if(extDotIdx > 0) {
				ext = name.substring(extDotIdx);
			}
			
			File projFile = new File(getEditor().getProject().getLocation());
			File resFile = new File(projFile, "__res");
			File mediaResFile = new File(resFile, "media");
			File segmentFile = new File(mediaResFile, "segments");
			if(!segmentFile.exists()) {
				segmentFile.mkdirs();
			}
			String segmentPath = segmentFile.getAbsolutePath();
			
			File outputFile = new File(segmentFile, 
					getEditor().getSession().getName() + "_" + getEditor().getSession().getCorpus() + "_" + (getEditor().getCurrentRecordIndex()+1) + ext);
			
//			String outputFilePath = 
//					System.getProperty("user.home")
//					+ File.separator + 
//					"Desktop" + File.separator +
//					"phon_export" + ext;
//			File outputFile = new File(outputFilePath);
			int fIdx = 0;
			while(outputFile.exists()) {
//				outputFilePath = 
//						System.getProperty("user.home")
//					+ File.separator + 
//					"Desktop" + File.separator +
//					"phon_export" + (++fIdx) + ext;
//				outputFile = new File(outputFilePath);
				outputFile = new File(segmentFile, 
						getEditor().getSession().getName() + "_" + getEditor().getSession().getCorpus() + "_" + (getEditor().getCurrentRecordIndex()+1) +
						"(" + (++fIdx) + ")" + ext);
			}
			String outputFilePath = outputFile.getAbsolutePath();
			wizardProps.put(MediaExportWizardProp.OUTPUT_FILE, outputFilePath);
		}
		
//		wizardProps.put(MediaExportWizardProp.OUTPUT_FILE, "/Users/ghedlund/Desktop/test.mov");
		wizardProps.put(MediaExportWizardProp.ALLOW_PARTIAL_EXTRACT, Boolean.TRUE);

		final Record utt = getEditor().currentRecord();
		final Tier<MediaSegment> segmentTier = utt.getSegment();
		final MediaSegment recordMedia = segmentTier.getGroup(0);
		if(recordMedia != null) {
			long startTime = (long)recordMedia.getStartValue();
			long endTime = (long)recordMedia.getEndValue();
			long dur = endTime - startTime;

			if(dur > 0L) {
				wizardProps.put(MediaExportWizardProp.IS_PARTICAL_EXTRACT, Boolean.TRUE);
				wizardProps.put(MediaExportWizardProp.PARTIAL_EXTRACT_SEGMENT_START, startTime);
				wizardProps.put(MediaExportWizardProp.PARTIAL_EXTRACT_SEGMENT_DURATION, dur);
			}
		}

		MediaExportWizard wizard = new MediaExportWizard(wizardProps);
		wizard.setSize(500, 550);
//		wizard.centerWindow();
		wizard.setLocationByPlatform(true);
		wizard.setVisible(true);
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
				try {
					long msVal = MsFormatter.displayStringToMs(timeStr);
					mediaPlayer.setTime(msVal);
				} catch (VLCException ex) {
					VLCError.logAndClear(ex);
				} catch (ParseException e) {
				}
				if(timeSelectionPopup != null) {
					timeSelectionPopup.dispose();
					timeSelectionPopup = null;
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
				lastSegment = utt.getSegment().getGroup(0);
				break;
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

		try {
			mediaPlayer.setTime((long) lastSegment.getEndValue());
		} catch (VLCException ex) {
			VLCError.logAndClear(ex);
		}
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
		final MediaSegment media = utt.getSegment().getGroup(0);
		
		if(media == null) return;
		
		final long startTime = (long)media.getStartValue();
		long endTime = (long)media.getEndValue();
		
		final boolean contiguous = Boolean.valueOf("" + pae.getData());
		
		if(contiguous) {
			final Participant speaker = utt.getSpeaker();
			
			for(int i = editor.getCurrentRecordIndex(); i < session.getRecordCount(); i++) {
				final Record u = session.getRecord(i);
				if(u.getSpeaker() == null && speaker != null) break;
				if(u.getSpeaker() != null)
					if(!u.getSpeaker().getId().equals(speaker.getId())) break;
				final MediaSegment m = u.getSegment().getGroup(0);
				if(m == null) break;
				endTime = (long)m.getEndValue();
			}
		}
		
		mediaPlayer.playSegment(startTime, (endTime-startTime));
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

			menu.add(getMediaExportItem());

			menu.addSeparator();
			
			setupPlaytoItems(menu);
			
			menu.addSeparator();
			
			setupGotoItems(menu);

			return retVal;
		}

		private JMenuItem getMediaExportItem() {
			PhonUIAction mediaExportAct =
					new PhonUIAction(MediaPlayerEditorView.this, "onExportMedia");
			mediaExportAct.putValue(Action.NAME, "Export media...");
			mediaExportAct.putValue(Action.SHORT_DESCRIPTION, "Export media");

			JMenuItem retVal = new JMenuItem(mediaExportAct);
			return retVal;
		}
		
		private void setupPlaytoItems(JPopupMenu menu) {
			PhonUIAction playCustomAct = new PhonUIAction(MediaPlayerEditorView.this, "onPlayCustomSegment");
			playCustomAct.putValue(PhonUIAction.NAME, "Play custom segment");
			JMenuItem playCustomItem = new JMenuItem(playCustomAct);
			menu.add(playCustomItem);
			
			PhonUIAction playSegmentAct = new PhonUIAction(MediaPlayerEditorView.this, "onPlaySpeakerSegment", false);
			playSegmentAct.putValue(PhonUIAction.NAME, "Play current segment");
			playSegmentAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Play segment for current record");
			JMenuItem playSegmentItem = new JMenuItem(playSegmentAct);
			menu.add(playSegmentItem);
			
			PhonUIAction playContiguousAct = new PhonUIAction(MediaPlayerEditorView.this, "onPlaySpeakerSegment", true);
			playContiguousAct.putValue(PhonUIAction.NAME, "Play current speech turn");
			playContiguousAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Play contiguous segments for current speaker");
			JMenuItem playContiguousItem = new JMenuItem(playContiguousAct);
			menu.add(playContiguousItem);
			
			PhonUIAction playConvPeriodAct = new PhonUIAction(MediaPlayerEditorView.this, "onPlayConvPeriod");
			playConvPeriodAct.putValue(PhonUIAction.NAME, "Play adjacency sequence");
			playConvPeriodAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Play a sequence of speaker turns");
			JMenuItem playConvPeriodItem = new JMenuItem(playConvPeriodAct);
			menu.add(playConvPeriodItem);
		}

		private void setupGotoItems(JPopupMenu menu) {
			PhonUIAction adjustVideoAct = 
					new PhonUIAction(MediaPlayerEditorView.this, "onToggleAdjustVideo");
			adjustVideoAct.putValue(PhonUIAction.NAME, "Move media position with record");
			adjustVideoAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move media to beginning of each record's segment");
			adjustVideoAct.putValue(PhonUIAction.SELECTED_KEY, isAdjustVideo());
			JCheckBoxMenuItem adjustVideoItem = new JCheckBoxMenuItem(adjustVideoAct);
			menu.add(adjustVideoItem); 
			
			PhonUIAction gotoSelectAct =
					new PhonUIAction(MediaPlayerEditorView.this, "onMenuSelectGoto");
			gotoSelectAct.putValue(Action.NAME, "Go to...");
			gotoSelectAct.putValue(Action.SHORT_DESCRIPTION, "Go to a specific time");

			JMenuItem gotoSelectItem = new JMenuItem(gotoSelectAct);
			menu.add(gotoSelectItem);

			PhonUIAction gotoLastSegmentAct =
					new PhonUIAction(MediaPlayerEditorView.this, "onMenuGoto");
			gotoLastSegmentAct.putValue(Action.NAME, "Go to end of segmented media");
			menu.add(gotoLastSegmentAct);
			
			final SessionEditor editor = getEditor();
			final Session session = editor.getSession();

			// for each participant
			for(int i = 0; i < session.getParticipantCount(); i++) {
				final Participant p = session.getParticipant(i);
				String msg =
						"Go to end of last segment for " +
							(p.getName() == null ? p.getId() : p.getName());
				PhonUIAction gotoPartSegmentAct =
						new PhonUIAction(MediaPlayerEditorView.this, "onMenuGoto", p);
				gotoPartSegmentAct.putValue(Action.NAME, msg);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DockPosition getPreferredDockPosition() {
		return DockPosition.WEST;
	}
	
}
