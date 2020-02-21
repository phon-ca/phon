
package ca.phon.app.session.editor.view.timeline;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.derby.impl.sql.compile.SetSchemaNode;
import org.apache.tools.ant.taskdefs.condition.IsSet;
import org.jdesktop.swingx.VerticalLayout;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.EditorViewAdapter;
import ca.phon.app.session.SessionMediaModel;
import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.ErrorBanner;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.AssignMediaAction;
import ca.phon.app.session.editor.actions.GenerateSessionAudioAction;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.app.session.editor.view.speech_analysis.SpeechAnalysisEditorView;
import ca.phon.app.session.editor.view.timeline.actions.SplitRecordAction;
import ca.phon.app.session.editor.view.timeline.actions.ZoomAction;
import ca.phon.media.LongSound;
import ca.phon.media.TimeUIModel;
import ca.phon.media.Timebar;
import ca.phon.media.TimebarMarkerModel.Interval;
import ca.phon.media.export.VLCWavExporter;
import ca.phon.media.util.MediaLocator;
import ca.phon.plugin.PluginManager;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SystemTierType;
import ca.phon.ui.DropDownButton;
import ca.phon.ui.HidablePanel;
import ca.phon.ui.PhonTaskButton;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTaskListener;
import ca.phon.worker.PhonWorker;
import ca.phon.worker.PhonTask.TaskStatus;
import groovy.swing.factory.GlueFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

public final class TimelineView extends EditorView {

	private static final long serialVersionUID = 8442995100291417078L;
	
	static {
		TimelineViewColors.install();
	}

	public static final String VIEW_TITLE = "Timeline";
	
	public static final String VIEW_ICON = "docking-frames/timeline";
	
	private final static String PLABACK_FPS = "TimelineView.playbackFps";
	private final float DEFAULT_PLAYBACK_FPS = 30.0f;
	private float playbackMarkerFps = PrefHelper.getFloat(PLABACK_FPS, DEFAULT_PLAYBACK_FPS);
	
	private JToolBar toolbar;
	
	private JButton zoomOutButton;
	
	private JButton zoomInButton;
	
	private JButton segmentationButton;
	
	private DropDownButton speakerButton;
	private JPopupMenu speakerMenu;
	
	private DropDownButton tierVisiblityButton;
	private JPopupMenu tierVisibilityMenu;
	
	private JScrollPane tierScrollPane;
	private TierPanel tierPanel;
	
	private JPanel errorPanel;
	private ErrorBanner messageButton = new ErrorBanner();
	private PhonTaskButton generateButton = null;
	
	/**
	 * Default {@link TimeUIModel} which should be
	 * used by most tier components
	 */
	private TimeUIModel timeModel;
	
	private Timebar timebar;
	
	private TimelineWaveformTier wavTier;
	
	private TimelineRecordTier recordGrid;
	
	private PlaybackMarkerSyncListener playbackMarkerSyncListener = new PlaybackMarkerSyncListener();
	
	public TimelineView(SessionEditor editor) {
		super(editor);
		addEditorViewListener(editorViewListener);
		
		init();
		update();
	}
	
	private void init() {
		toolbar = setupToolbar();

		// the shared time model
		timeModel = new TimeUIModel();
		
		timeModel.setPixelsPerSecond(100.0f);
		timeModel.setStartTime(0.0f);
		timeModel.setEndTime(0.0f);
		timeModel.addPropertyChangeListener("endTime", (e) -> {
			timebar.revalidate();
			wavTier.revalidate();
			recordGrid.revalidate();
		});
		
		timebar = new Timebar(timeModel);
		timebar.setOpaque(true);
		timebar.setBackground(Color.WHITE);
		timebar.addMouseListener(new MouseInputAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1 &&
						e.getClickCount() == 1) {
					// goto position in media
					MediaPlayerEditorView mediaPlayerView = 
							(MediaPlayerEditorView)getEditor().getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
					if(mediaPlayerView != null) {
						float time = getTimeModel().timeAtX(e.getX());
						long timeMS = (long)(time * 1000.0f);
						
						mediaPlayerView.getPlayer().setTime(timeMS);
					}
				}
			}
			
		});
		
		tierPanel = new TierPanel(new GridBagLayout());
		tierScrollPane = new JScrollPane(tierPanel);
		tierScrollPane.setColumnHeaderView(timebar);
		
		// Order here matters - for the purpose of
		// editor events the record tier object must be created before the
		// wav tier
		recordGrid = new TimelineRecordTier(this);
		recordGrid.getRecordGrid().addMouseListener(contextMenuListener);
		
		wavTier = new TimelineWaveformTier(this);
		wavTier.getPreferredSize();
		wavTier.getWaveformDisplay().addMouseListener(contextMenuListener);
		
		addTier(wavTier);
		addTier(recordGrid);
		
		for(var extPt:PluginManager.getInstance().getExtensionPoints(TimelineTier.class)) {
			var tier = extPt.getFactory().createObject(this);
			addTier(tier);
		}
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridx = 0;
		gbc.gridy = tierIdx++;
		tierPanel.add(Box.createVerticalGlue(), gbc);

		setLayout(new BorderLayout());
		add(toolbar, BorderLayout.NORTH);
		
		add(tierScrollPane, BorderLayout.CENTER);
		
		errorPanel = new JPanel(new VerticalLayout());
		errorPanel.add(messageButton);
		
		final GenerateSessionAudioAction generateAct = getEditor().getMediaModel().getGenerateSessionAudioAction();
		generateAct.putValue(PhonUIAction.LARGE_ICON_KEY, generateAct.getValue(PhonUIAction.SMALL_ICON));
		generateAct.addTaskListener(new PhonTaskListener() {
			
			@Override
			public void statusChanged(PhonTask task, TaskStatus oldStatus, TaskStatus newStatus) {
				if(TaskStatus.RUNNING == newStatus) {
					final VLCWavExporter wavExportTask = (VLCWavExporter)task;
					generateButton = new PhonTaskButton(wavExportTask);
					generateButton.getTopLabel().setFont(generateButton.getTopLabel().getFont().deriveFont(Font.BOLD));
					generateButton.getBusyLabel().setBusy(true);
					generateButton.setTopLabelText("Export audio - 0%");
					generateButton.setBottomLabelText(wavExportTask.getOutputFile().getAbsolutePath());
					
					messageButton.setVisible(false);
					errorPanel.add(generateButton);
				} else {
					if(generateButton != null) {
						errorPanel.remove(generateButton);
					}
					if(TaskStatus.FINISHED != newStatus) {
						messageButton.setVisible(true);
					}
				}
			}
			
			@Override
			public void propertyChanged(PhonTask task, String property, Object oldValue, Object newValue) {
				if(PhonTask.PROGRESS_PROP.contentEquals(property)) {
					generateButton.setTopLabelText(String.format("Export audio - %d%%", (int)Math.round(100.0*(float)newValue)));
				}
			}
		});

		add(errorPanel, BorderLayout.SOUTH);
		
		SessionMediaModel mediaModel = getEditor().getMediaModel();
		if(mediaModel.isSessionMediaAvailable()) {
			MediaPlayerEditorView mediaPlayerView = 
					(MediaPlayerEditorView)getEditor().getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
			if(mediaPlayerView.getPlayer().getMediaFile() != null) {
				mediaPlayerView.getPlayer().addMediaPlayerListener(playbackMarkerSyncListener);
			}
		}
	}
	
	public JToolBar getToolbar() {
		return this.toolbar;
	}
	
	public JComponent getTierPanel() {
		return this.tierPanel;
	}
	
	public TimelineWaveformTier getWaveformTier() {
		return this.wavTier;
	}
	
	public TimelineRecordTier getRecordTier() {
		return this.recordGrid;
	}
	
	private JToolBar setupToolbar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		
		PhonUIAction segmentationAction = new PhonUIAction(this, "toggleSegmentation");
		segmentationAction.putValue(PhonUIAction.NAME, "Start Segmentation");
		segmentationAction.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL));
		segmentationButton = new JButton(segmentationAction);
		
		speakerMenu = new JPopupMenu();
		speakerMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				speakerMenu.removeAll();
				recordGrid.setupSpeakerMenu(new MenuBuilder(speakerMenu));
			}
			
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
			
		});
		
		final PhonUIAction speakerVisibilityAct = new PhonUIAction(this, null);
		speakerVisibilityAct.putValue(PhonUIAction.NAME, "Participants");
		speakerVisibilityAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show participant visibility menu");
		speakerVisibilityAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("apps/system-users", IconSize.SMALL));
		speakerVisibilityAct.putValue(DropDownButton.BUTTON_POPUP, speakerMenu);
		speakerVisibilityAct.putValue(DropDownButton.ARROW_ICON_POSITION, SwingUtilities.BOTTOM);
		speakerVisibilityAct.putValue(DropDownButton.ARROW_ICON_GAP, 2);
		
		speakerButton = new DropDownButton(speakerVisibilityAct);
		speakerButton.setOnlyPopup(true);
		
		tierVisibilityMenu = new JPopupMenu();
		tierVisibilityMenu.addPopupMenuListener(new PopupMenuListener() {
			
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				tierVisibilityMenu.removeAll();
				recordGrid.setupTierMenu(new MenuBuilder(tierVisibilityMenu));
			}
			
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
			
		});
		
		final PhonUIAction tierVisibilityAct = new PhonUIAction(this, null);
		tierVisibilityAct.putValue(PhonUIAction.NAME, "Tiers");
		tierVisibilityAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show tier visibility menu");
		tierVisibilityAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("misc/record", IconSize.SMALL));
		tierVisibilityAct.putValue(DropDownButton.BUTTON_POPUP, tierVisibilityMenu);
		tierVisibilityAct.putValue(DropDownButton.ARROW_ICON_POSITION, SwingConstants.BOTTOM);
		tierVisibilityAct.putValue(DropDownButton.ARROW_ICON_GAP, 2);
		
		tierVisiblityButton = new DropDownButton(tierVisibilityAct);
		tierVisiblityButton.setOnlyPopup(true);
		
		zoomOutButton = new JButton(new ZoomAction(this, false));
		zoomInButton = new JButton(new ZoomAction(this, true));
		
//		SplitRecordAction splitRecordAct = new SplitRecordAction(this);
//		JButton splitRecordButton = new JButton(splitRecordAct);
		
		toolbar.add(segmentationButton);
		toolbar.addSeparator();
		toolbar.add(speakerButton);
		toolbar.add(tierVisiblityButton);
//		toolbar.add(splitRecordButton);
		toolbar.addSeparator();
		toolbar.add(zoomInButton);
		toolbar.add(zoomOutButton);
		
		return toolbar;
	}
	
	private int tierIdx = 0;
	private void addTier(TimelineTier tier) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = tierIdx++;
		
		tierPanel.add(tier, gbc);
	}
	
	private void update() {
		final SessionMediaModel mediaModel = getEditor().getMediaModel();
		if(mediaModel.isSessionAudioAvailable()) {
			loadSessionAudio();
			wavTier.setVisible(true);
			messageButton.setVisible(false);
		} else {
			wavTier.setVisible(false);
			getTimeModel().clearIntervals();
			getTimeModel().clearMarkers();
			
			messageButton.clearActions();

			// display option to generate audio file if there is session media available
			if(mediaModel.isSessionMediaAvailable()) {
				// show generate audio message
				messageButton.setTopLabelText("<html><b>Session audio file not available</b></html>");
				messageButton.setBottomLabelText("<html>Click here to generate audio (.wav) file from session media.</html>");

				messageButton.setDefaultAction(mediaModel.getGenerateSessionAudioAction());
				messageButton.addAction(mediaModel.getGenerateSessionAudioAction());
			} else {
				// no media, tell user to setup media in Session Information
				final AssignMediaAction browseForMediaAct = new AssignMediaAction(getEditor());
				browseForMediaAct.putValue(AssignMediaAction.LARGE_ICON_KEY, browseForMediaAct.getValue(AssignMediaAction.SMALL_ICON));

				messageButton.setDefaultAction(browseForMediaAct);
				messageButton.addAction(browseForMediaAct);

				messageButton.setTopLabelText("<html><b>Session media not available</b></html>");
				messageButton.setBottomLabelText("<html>Click here to assign media file to session.</html>");
			}
			messageButton.setVisible(true);
			messageButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
		setupTimeModel();
	}

	private float getMaxRecordTime() {
		float retVal = 0.0f;
		
		for(Record r:getEditor().getSession().getRecords()) {
			MediaSegment segment = r.getSegment().getGroup(0);
			if(segment != null) {
				float segEnd = (float)(segment.getEndValue() / 1000.0f);
				retVal = Math.max(segEnd, retVal);
			}
		}
		
		return retVal;
	}
	
	private void setupTimeModel() {
		float endTime = getMaxRecordTime();
		
		final SessionMediaModel mediaModel = getEditor().getMediaModel();
		if(mediaModel.isSessionMediaAvailable()) {
			if(mediaModel.isSessionAudioAvailable()) {
				endTime = Math.max(endTime, wavTier.getWaveformDisplay().getLongSound().length());
				timeModel.setMediaEndTime(wavTier.getWaveformDisplay().getLongSound().length());
			} else {
				// check if media is loaded in player, if so use time from player
				MediaPlayerEditorView mediaPlayerView = 
						(MediaPlayerEditorView)getEditor().getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
				if(mediaPlayerView.getPlayer().getMediaFile() != null) {
					endTime = Math.max(endTime, mediaPlayerView.getPlayer().getLength() / 1000.0f);
					timeModel.setMediaEndTime(mediaPlayerView.getPlayer().getLength()/1000.0f);
				}
			}
		}
		
		timeModel.setEndTime(endTime);
	}
	
	private void loadSessionAudio() {
		final SessionMediaModel mediaModel = getEditor().getMediaModel();
		try {
			LongSound ls = mediaModel.getSharedSessionAudio();
			//timeModel.setEndTime(ls.length());
			wavTier.getWaveformDisplay().setEndTime(ls.length());
			wavTier.getWaveformDisplay().setLongSound(ls);
		} catch (IOException e) {
			LogUtil.severe(e);
		}
	}
	
	public TimeUIModel getTimeModel() {
		return this.timeModel;
	}
	
	public void scrollToTime(float time) {
		var x = getTimeModel().xForTime(time);
		var rect = tierPanel.getVisibleRect();
		rect.x = (int)x;
		tierPanel.scrollRectToVisible(rect);
	}
	
	public void scrollToRecord(Record r) {
		MediaSegment seg = r.getSegment().getGroup(0);
		float time = seg.getStartValue() / 1000.0f;
		float endTime = seg.getEndValue() / 1000.0f;
		float paddingTime = 100.0f / getTimeModel().getPixelsPerSecond();
		
		float windowStart = Math.max(0.0f, time - paddingTime);
		float windowEnd = Math.min(endTime + paddingTime, getTimeModel().getEndTime());
		float windowLen = windowEnd - windowStart;
		
		float viewStart = getWindowStart();
		float viewEnd = getWindowEnd();
		float viewLen = viewEnd - viewStart;
		
		if(windowLen <= viewLen) {
			if( (windowStart >= viewStart && windowEnd > viewEnd) 
					|| (windowStart > viewEnd && windowEnd > viewEnd) ) {
				scrollToTime(windowEnd - viewLen);
			} else if( (windowStart < viewStart && windowEnd < viewStart)
					|| (windowStart < viewStart && windowEnd >= viewStart) ) {
				scrollToTime(windowStart);
			}
		} else {
			int viewWidth = getVisibleRect().width;
			float newPxPerS = viewWidth / windowLen;
			
			getTimeModel().setPixelsPerSecond(newPxPerS);
			scrollToTime(windowStart);
		}
		
	}
	
	public void scrollRectToVisible(Rectangle rect) {
		tierPanel.scrollRectToVisible(rect);
	}
	
	public float getWindowStart() {
		return getTimeModel().timeAtX(tierPanel.getVisibleRect().getX());
	}
	
	public float getWindowEnd() {
		return getTimeModel().timeAtX(tierPanel.getVisibleRect().getMaxX());
	}

	public double getWindowStartX() {
		return tierPanel.getVisibleRect().getX();
	}
	
	public double getWindowEndX() {
		return tierPanel.getVisibleRect().getMaxX();
	}
	
	public float getWindowLength() {
		return getWindowEnd() - getWindowStart();
	}
	
	/* Editor actions */
	
	private final DelegateEditorAction onMediaChangedAct = new DelegateEditorAction(this, "onMediaChanged");
	
	private final DelegateEditorAction onSessionAudioAvailableAct = new DelegateEditorAction(this, "onSessionAudioAvailable");
	
	private final DelegateEditorAction onRecordChangeAct = new DelegateEditorAction(this, "onRecordChanged");
	
	private final DelegateEditorAction onSegmentationStarted = new DelegateEditorAction(this, "onSegmentationStarted");
	
	private final DelegateEditorAction onSegmentationEnded = new DelegateEditorAction(this, "onSegmentationEnded");
	
	private final DelegateEditorAction onRecordAddedAct = new DelegateEditorAction(this, "onRecordAdded");
	
	private final DelegateEditorAction onRecordDeletedAct = new DelegateEditorAction(this, "onRecordDeleted");
	
	private final DelegateEditorAction onTierChangedAct = new DelegateEditorAction(this, "onTierChanged");
	
	private final DelegateEditorAction onMediaLoadedAct = new DelegateEditorAction(this, "onMediaLoaded");
	
	private void registerEditorEvents() {
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_CHANGED_EVT, onRecordChangeAct);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SESSION_MEDIA_CHANGED, onMediaChangedAct);
		getEditor().getEventManager().registerActionForEvent(SessionMediaModel.SESSION_AUDIO_AVAILABLE, onSessionAudioAvailableAct);
		
		getEditor().getEventManager().registerActionForEvent(SegmentationHandler.EDITOR_SEGMENTATION_START, onSegmentationStarted);
		getEditor().getEventManager().registerActionForEvent(SegmentationHandler.EDITOR_SEGMENTATION_END, onSegmentationEnded);
		
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_ADDED_EVT, onRecordAddedAct);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_DELETED_EVT, onRecordDeletedAct);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.TIER_CHANGED_EVT, onTierChangedAct);
		
		getEditor().getEventManager().registerActionForEvent(MediaPlayerEditorView.MEDIA_LOADED_EVENT, onMediaLoadedAct);
	}
	
	private void deregisterEditorEvents() {
		getEditor().getEventManager().removeActionForEvent(EditorEventType.RECORD_CHANGED_EVT, onRecordChangeAct);
		getEditor().getEventManager().removeActionForEvent(EditorEventType.SESSION_MEDIA_CHANGED, onMediaChangedAct);
		getEditor().getEventManager().removeActionForEvent(SessionMediaModel.SESSION_AUDIO_AVAILABLE, onSessionAudioAvailableAct);
		
		getEditor().getEventManager().removeActionForEvent(SegmentationHandler.EDITOR_SEGMENTATION_START, onSegmentationStarted);
		getEditor().getEventManager().removeActionForEvent(SegmentationHandler.EDITOR_SEGMENTATION_END, onSegmentationEnded);
		
		getEditor().getEventManager().removeActionForEvent(EditorEventType.RECORD_ADDED_EVT, onRecordAddedAct);
		getEditor().getEventManager().removeActionForEvent(EditorEventType.RECORD_DELETED_EVT, onRecordDeletedAct);
		getEditor().getEventManager().removeActionForEvent(EditorEventType.TIER_CHANGED_EVT, onTierChangedAct);
		
		getEditor().getEventManager().removeActionForEvent(MediaPlayerEditorView.MEDIA_LOADED_EVENT, onMediaLoadedAct);
	}
	
	@RunOnEDT
	public void onMediaLoaded(EditorEvent ee) {
		setupTimeModel();
		MediaPlayerEditorView mediaPlayerView = 
				(MediaPlayerEditorView)getEditor().getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
		if(mediaPlayerView.getPlayer().getMediaFile() != null) {
			mediaPlayerView.getPlayer().addMediaPlayerListener(playbackMarkerSyncListener);
		}
	}
	
	@RunOnEDT
	public void onMediaChanged(EditorEvent ee) {
		update();
	}
	
	@RunOnEDT
	public void onRecordChanged(EditorEvent ee) {
		Record r = getEditor().currentRecord();
		
		if(getEditor().getViewModel().isShowing(MediaPlayerEditorView.VIEW_TITLE)) {
			MediaPlayerEditorView mediaPlayerView = (MediaPlayerEditorView)getEditor().getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
			// don't adjust scroll position while playing
			if(mediaPlayerView.getPlayer().isPlaying())
				return;
		}
		
		if(r != null) {
			scrollToRecord(r);
		}
	}
	
	@RunOnEDT
	public void onSessionAudioAvailable(EditorEvent ee) {
		update();
	}
	
	@RunOnEDT
	public void onSegmentationStarted(EditorEvent ee) {
		segmentationButton.setText("Stop Segmentation");
		segmentationButton.setIcon(IconManager.getInstance().getIcon("actions/media-playback-stop", IconSize.SMALL));
		
		if(playbackMarkerSyncListener.playbackMarker != null)
			timeModel.removeMarker(playbackMarkerSyncListener.playbackMarker);
	}
	
	@RunOnEDT
	public void onSegmentationEnded(EditorEvent ee) {
		segmentationButton.setText("Start Segmentation");
		segmentationButton.setIcon(IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL));
		
		getEditor().putExtension(SegmentationHandler.class, null);

		repaint();
	}
	
	@RunOnEDT
	public void onRecordAdded(EditorEvent ee) {
		setupTimeModel();
		repaint();
	}
	
	@RunOnEDT
	public void onRecordDeleted(EditorEvent ee) {
		setupTimeModel();
		repaint();
	}
	
	@RunOnEDT
	public void onTierChanged(EditorEvent ee) {
		String tierName = ee.getEventData().toString();
		if(SystemTierType.Segment.getName().equals(tierName)) {
			setupTimeModel();
		}
	}
	
	public void toggleSegmentation() {
		SegmentationHandler handler = getEditor().getExtension(SegmentationHandler.class);
		if(handler != null) {
			handler.stopSegmentation();
		} else {
			SessionMediaModel mediaModel = getEditor().getMediaModel();
			if(!mediaModel.isSessionMediaAvailable()) {
				getEditor().showMessageDialog("Unable to start segmentation",
						"Please assign session media file before starting segmentation.",
						MessageDialogProperties.okOptions);
				return;
			}
			
			SegmentationDialog startDialog = new SegmentationDialog(getEditor());
			startDialog.setModal(true);
			startDialog.pack();
			startDialog.setLocationRelativeTo(getEditor());
			startDialog.setVisible(true);
			
			if(!startDialog.wasCanceled()) {
				handler = new SegmentationHandler(getEditor());
				handler.setMediaStart(startDialog.getMediaStart());
				handler.setParticipantForMediaStart(startDialog.getSelectedParticipant());
				handler.setSegmentationMode(startDialog.getSegmentationMode());
				handler.getWindow().setBackwardWindowLengthMs(startDialog.getWindowLength());

				// check for speech analysis view
				if(getEditor().getViewModel().isShowing(SpeechAnalysisEditorView.VIEW_TITLE)) {
					int result = getEditor().showMessageDialog("Segmentation Performance", "The Speech Analysis view may cause performance issues with segmentation.", new String[] {
							"Close Speech Analysis and continue", "Continue without closing", "Cancel" });
					switch(result) {
					case 0:
						getEditor().getViewModel().getCloseAction(SpeechAnalysisEditorView.VIEW_TITLE).actionPerformed(new ActionEvent(this, -1, "close"));
						break;
						
					case 1:
						break;
						
					case 2:
					default:
						return;
					}
				}
				
				getEditor().putExtension(SegmentationHandler.class, handler);
				handler.startSegmentation();
			}
		}
	}
	
	@Override
	public JMenu getMenu() {
		JMenu menu = new JMenu();
		
		MenuBuilder builder = new MenuBuilder(menu);

		// setup media actions
		SessionMediaModel mediaModel = getEditor().getMediaModel();
		if(mediaModel.isSessionMediaAvailable()) {
			if(!mediaModel.isSessionAudioAvailable()) {
				// Add generate audio action
				GenerateSessionAudioAction genAudioAct = new GenerateSessionAudioAction(getEditor());
				builder.addItem(".", genAudioAct);
			} else {
				wavTier.setupContextMenu(builder, false);
			}
		} else {
			// Add browse for media action
			builder.addItem(".", new AssignMediaAction(getEditor()));
		}
		
		builder.addSeparator(".", "record_grid");
		
		recordGrid.setupContextMenu(builder, false);
		
		builder.addSeparator(".", "segmentation");
		
		PhonUIAction segmentationAction = new PhonUIAction(this, "toggleSegmentation");
		if(getEditor().getExtension(SegmentationHandler.class) != null) {
			segmentationAction.putValue(PhonUIAction.NAME, "Stop Segmentation");
			segmentationAction.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/media-playback-stop", IconSize.SMALL));
		} else {
			segmentationAction.putValue(PhonUIAction.NAME, "Start Segmentation");
			segmentationAction.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL));
		}
		builder.addItem(".", segmentationAction);
		
		builder.addSeparator(".", "visiblity");

		JMenu participantMenu = builder.addMenu(".", "Participants");
		participantMenu.setIcon(IconManager.getInstance().getIcon("apps/system-users", IconSize.SMALL));
		recordGrid.setupSpeakerMenu(new MenuBuilder(participantMenu));

		JMenu tierMenu = builder.addMenu(".", "Tiers");
		tierMenu.setIcon(IconManager.getInstance().getIcon("misc/record", IconSize.SMALL));
		recordGrid.setupTierMenu(new MenuBuilder(tierMenu));
		
		builder.addSeparator(".", "zoom");
		
		builder.addItem(".", new ZoomAction(this, true));
		builder.addItem(".", new ZoomAction(this, false));
		
		return menu;
	}

	private void showContextMenu(MouseEvent me) {
		JPopupMenu contextMenu = new JPopupMenu();
		
		MenuBuilder builder = new MenuBuilder(contextMenu);
		
		if(me.getComponent() == recordGrid.getRecordGrid()) {
			recordGrid.setupContextMenu(builder, true);
//			builder.addSeparator(".", "wav_actions");
//			wavTier.setupContextMenu(me, builder);
		} else {
			wavTier.setupContextMenu(builder, true);
//			builder.addSeparator(".", "record_actions");
//			recordGrid.setupContextMenu(me, builder);
		}
		
		builder.addSeparator(".", "segmentation");
		
		PhonUIAction segmentationAction = new PhonUIAction(this, "toggleSegmentation");
		if(getEditor().getExtension(SegmentationHandler.class) != null) {
			segmentationAction.putValue(PhonUIAction.NAME, "Stop Segmentation");
			segmentationAction.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/media-playback-stop", IconSize.SMALL));
		} else {
			segmentationAction.putValue(PhonUIAction.NAME, "Start Segmentation");
			segmentationAction.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL));
		}
		builder.addItem(".", segmentationAction);
		
		builder.addSeparator(".", "visiblity");

		JMenu participantMenu = builder.addMenu(".", "Participants");
		participantMenu.setIcon(IconManager.getInstance().getIcon("apps/system-users", IconSize.SMALL));
		recordGrid.setupSpeakerMenu(new MenuBuilder(participantMenu));

		JMenu tierMenu = builder.addMenu(".", "Tiers");
		tierMenu.setIcon(IconManager.getInstance().getIcon("misc/record", IconSize.SMALL));
		recordGrid.setupTierMenu(new MenuBuilder(tierMenu));
		
		builder.addSeparator(".", "zoom");
		
		builder.addItem(".", new ZoomAction(this, true));
		builder.addItem(".", new ZoomAction(this, false));
		
		contextMenu.show(me.getComponent(), me.getX(), me.getY());
	}
		
	@Override
	public String getName() {
		return VIEW_TITLE;
	}

	@Override
	public ImageIcon getIcon() {
		return IconManager.getInstance().getIcon(VIEW_ICON, IconSize.SMALL);
	}
	
	public void repaint(long tn, float startTime, float endTime) {
		timebar.repaint(tn, startTime, endTime);
		getWaveformTier().getWaveformDisplay().repaint(tn, startTime, endTime);
		getRecordTier().getRecordGrid().repaint(tn, startTime, endTime);
	}
	
	private final EditorViewAdapter editorViewListener = new EditorViewAdapter() {

		@Override
		public void onOpened(EditorView view) {
			registerEditorEvents();
			
			// scroll to current record position
			SwingUtilities.invokeLater(() -> {
				Record r = getEditor().currentRecord();
				if(r != null) {
					scrollToRecord(r);
//					MediaSegment seg = r.getSegment().getGroup(0);
//					if(seg != null && seg.getEndValue() - seg.getStartValue() > 0) {
//						
//						Rectangle2D segRect = new Rectangle2D.Double(
//								timeModel.xForTime(seg.getStartValue()/1000.0f), 0,
//								timeModel.xForTime(seg.getEndValue()/1000.0f) - timeModel.xForTime(seg.getStartValue()/1000.0f), 1);
//						if(!segRect.intersects(recordGrid.getVisibleRect())) {
//							scrollToTime(seg.getStartValue() / 1000.0f);
//						}
//					}
				}
			});
		}

		@Override
		public void onClosed(EditorView view) {
			deregisterEditorEvents();
		}
		
	};
	
	private class PlaybackMarkerSyncListener extends MediaPlayerEventAdapter {

		private TimeUIModel.Marker playbackMarker;
		
		private Timer playbackTimer;
		
		private PlaybackMarkerTask playbackMarkerTask;
		
		@Override
		public void playing(MediaPlayer mediaPlayer) {
			if(playbackMarker == null && getEditor().getExtension(SegmentationHandler.class) == null) {
				float currentTime = (float)TimeUIModel.roundTime(mediaPlayer.status().time() / 1000.0f);
				
				playbackMarker = timeModel.addMarker(currentTime, Color.darkGray);
				playbackMarker.setOwner(wavTier.getWaveformDisplay());
				
				playbackMarkerTask = new PlaybackMarkerTask(playbackMarker);
				playbackMarkerTask.mediaSyncTime = mediaPlayer.status().time();
				playbackMarkerTask.startTime = System.currentTimeMillis();
			}
		}

		@Override
		public void paused(MediaPlayer mediaPlayer) {
			if(playbackMarker != null)
				SwingUtilities.invokeLater( () -> {
					timeModel.removeMarker(playbackMarker);
					playbackMarker = null;
					playbackTimer = null;
					
					if(playbackMarkerTask != null) {
						playbackMarkerTask.cancel();
						playbackMarkerTask = null;
					}
				});
		}
		
		@Override
		public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
			if(playbackMarkerTask != null) {
				// sync playback time
				playbackMarkerTask.mediaSyncTime = newTime;
				playbackMarkerTask.startTime = System.currentTimeMillis();
				if(playbackTimer == null) {
					playbackTimer = new Timer(true);
					playbackTimer.schedule(playbackMarkerTask, 0L, (long)(1/playbackMarkerFps * 1000.0f));
				}
			}
		}
		
	}
	
	private class PlaybackMarkerTask extends TimerTask {

		volatile long startTime;
		
		volatile long mediaSyncTime = 0L;
		
		TimeUIModel.Marker playbackMarker;
		
		public PlaybackMarkerTask(TimeUIModel.Marker playbackMarker) {
			super();
			this.playbackMarker = playbackMarker;
		}
		
		@Override
		public void run() {
			if(playbackMarker != null) {
				long currentTime = System.currentTimeMillis();
				long newTime = mediaSyncTime + (currentTime - startTime);
				
				playbackMarker.setTime((float)TimeUIModel.roundTime(newTime / 1000.0f));
			} else {
				cancel();
			}
		}
		
	}

	private class SeparatorMouseListener extends MouseInputAdapter {
		
		private TimelineTier tier;
		
		private boolean valueAdjusting = false;
		
		public SeparatorMouseListener(TimelineTier tier) {
			super();
			
			this.tier = tier;
		}
		
		public TimelineTier getTier() {
			return this.tier;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			valueAdjusting = true;
			((JComponent)e.getSource()).firePropertyChange("valueAdjusting", false, valueAdjusting);
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			valueAdjusting = false;
			((JComponent)e.getSource()).firePropertyChange("valueAdjusting", true, valueAdjusting);
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			Dimension currentSize = tier.getSize();
			Dimension prefSize = tier.getPreferredSize();

			prefSize.height = currentSize.height + e.getY();
			if(prefSize.height < 0) prefSize.height = 0;
			
			tier.setPreferredSize(prefSize);
			tierPanel.revalidate();
		}
		
	}
	
	private MouseListener contextMenuListener = new MouseAdapter() {

		@Override
		public void mousePressed(MouseEvent e) {
			if(e.isPopupTrigger()) {
				showContextMenu(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if(e.isPopupTrigger()) {
				showContextMenu(e);
			}
		}
		
	};
	
	private class TierPanel extends JPanel implements Scrollable {
		
		public TierPanel() {
			super();
			setOpaque(false);
		}

		public TierPanel(boolean isDoubleBuffered) {
			super(isDoubleBuffered);
		}

		public TierPanel(LayoutManager layout, boolean isDoubleBuffered) {
			super(layout, isDoubleBuffered);
		}

		public TierPanel(LayoutManager layout) {
			super(layout);
		}

		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 10;
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return (int)visibleRect.getWidth();
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			return false;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			return false;
		}
		
	}
	
}
