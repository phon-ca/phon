package ca.phon.app.session.editor.view.timeline;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import ca.phon.app.media.TimeUIModel;
import ca.phon.app.session.SessionMediaModel;
import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.BrowseForMediaAction;
import ca.phon.app.session.editor.actions.GenerateSessionAudioAction;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.app.session.editor.view.timeline.actions.SplitRecordAction;
import ca.phon.app.session.editor.view.timeline.actions.ZoomAction;
import ca.phon.media.LongSound;
import ca.phon.media.util.MediaLocator;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.ui.DropDownButton;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonWorker;
import groovy.swing.factory.GlueFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

public final class TimelineView extends EditorView {

	private static final long serialVersionUID = 8442995100291417078L;
	
	public static final String VIEW_TITLE = "Timeline";
	
	/**
	 * Values for the zoom bar
	 */
	public static final float zoomValues[] = { 3.125f,  6.25f, 12.5f, 25.0f, 50.0f, 100.0f, 200.0f, 400.0f, 800.0f, 1600.0f };
	
	private static final int defaultZoomIdx = 5;
	
	private JToolBar toolbar;
	
	private JButton zoomOutButton;
	
	private JButton zoomInButton;
	
	private JButton segmentationButton;
	
	private DropDownButton speakerButton;
	private JPopupMenu speakerMenu;
	
	private DropDownButton tierVisiblityButton;
	private JPopupMenu tierVisibilityMenu;
	
	private TierPanel tierPanel;
	
	/**
	 * Default {@link TimeUIModel} which should be
	 * used by most tier components
	 */
	private TimeUIModel timeModel;
	
	private TimelineWaveformTier wavTier;
	
	private TimelineRecordTier recordGrid;
	
	private PlaybackMediaListener playbackMediaListener = new PlaybackMediaListener();
	
	public TimelineView(SessionEditor editor) {
		super(editor);
		
		init();
		update();
	}
	
	private void init() {
		toolbar = setupToolbar();

		// the shared time model
		timeModel = new TimeUIModel();
		
		timeModel.addPropertyChangeListener((e) -> {
			if(e.getPropertyName().equals("pixelsPerSecond")) {
				int zoomIdx = Arrays.binarySearch(zoomValues, (float)e.getNewValue());
				if(zoomIdx == 0) {
					zoomOutButton.setEnabled(false);
				} else if (zoomIdx == zoomValues.length - 1) {
					zoomInButton.setEnabled(false);
				} else {
					zoomInButton.setEnabled(true);
					zoomOutButton.setEnabled(true);
				}
			}
		});
		timeModel.setPixelsPerSecond(zoomValues[defaultZoomIdx]);
		timeModel.setStartTime(0.0f);
		timeModel.setEndTime(0.0f);	
		
		tierPanel = new TierPanel(new GridBagLayout());
		JScrollPane scroller = new JScrollPane(tierPanel);
		
		// Order here matters - for the purpose of
		// editor events the record tier object must be created before the
		// wav tier
		recordGrid = new TimelineRecordTier(this);
		recordGrid.getRecordGrid().addMouseListener(contextMenuListener);
		
		wavTier = new TimelineWaveformTier(this);
		wavTier.getPreferredSize();
		wavTier.getWaveformDisplay().addMouseListener(contextMenuListener);
		
		JSeparator wavSep = addTier(wavTier);
		wavSep.addPropertyChangeListener("valueAdjusting", (e) -> {
			if((boolean)e.getNewValue() == Boolean.FALSE) {
				wavTier.getWaveformDisplay().getUI().updateCache();
			}
		});
		
		addTier(recordGrid);
		
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
		
		add(scroller, BorderLayout.CENTER);
		
		SessionMediaModel mediaModel = getEditor().getMediaModel();
		if(mediaModel.isSessionMediaAvailable()) {
			MediaPlayerEditorView mediaPlayerView = 
					(MediaPlayerEditorView)getEditor().getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
			if(mediaPlayerView.getPlayer().getMediaFile() != null) {
				mediaPlayerView.getPlayer().addMediaPlayerListener(playbackMediaListener);
			}
		}
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
		
		zoomOutButton = new JButton(new ZoomAction(this, -1));
		zoomInButton = new JButton(new ZoomAction(this, 1));
		
//		SplitRecordAction splitRecordAct = new SplitRecordAction(this);
//		JButton splitRecordButton = new JButton(splitRecordAct);
		
		toolbar.add(segmentationButton);
		toolbar.addSeparator();
		toolbar.add(speakerButton);
		toolbar.add(tierVisiblityButton);
//		toolbar.add(splitRecordButton);
		toolbar.addSeparator();
		toolbar.add(zoomOutButton);
		toolbar.add(zoomInButton);
		
		return toolbar;
	}
	
	private int tierIdx = 0;
	private JSeparator addTier(TimelineTier tier) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = tierIdx++;
		
		tierPanel.add(tier, gbc);
		
		if(tier.isResizeable()) {
			final JSeparator separator =  new JSeparator(SwingConstants.HORIZONTAL);
			separator.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
			
			SeparatorMouseListener listener = new SeparatorMouseListener(tier);
			separator.addMouseMotionListener(listener);
			separator.addMouseListener(listener);
			
			gbc.gridy = tierIdx++;
			
			tierPanel.add(separator, gbc);
			tier.addComponentListener(new ComponentListener() {
				
				@Override
				public void componentShown(ComponentEvent e) {
					separator.setVisible(true);
				}
				
				@Override
				public void componentResized(ComponentEvent e) {
					
				}
				
				@Override
				public void componentMoved(ComponentEvent e) {
					
				}
				
				@Override
				public void componentHidden(ComponentEvent e) {
					separator.setVisible(false);
				}
				
			});
			return separator;
		}
		return null;
	}
	
	private void update() {
		final SessionMediaModel mediaModel = getEditor().getMediaModel();
		if(mediaModel.isSessionAudioAvailable()) {
			loadSessionAudio();
			wavTier.setVisible(true);
			recordGrid.getTimebar().setVisible(false);
		} else {
			wavTier.setVisible(false);
			recordGrid.getTimebar().setVisible(true);
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
	
	public void scrollRectToVisible(Rectangle rect) {
		tierPanel.scrollRectToVisible(rect);
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
	
	private void registerEditorEvents() {
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_CHANGED_EVT, onRecordChangeAct);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SESSION_MEDIA_CHANGED, onMediaChangedAct);
		getEditor().getEventManager().registerActionForEvent(SessionMediaModel.SESSION_AUDIO_AVAILABLE, onSessionAudioAvailableAct);
		
		getEditor().getEventManager().registerActionForEvent(SegmentationHandler.EDITOR_SEGMENTATION_START, onSegmentationStarted);
		getEditor().getEventManager().registerActionForEvent(SegmentationHandler.EDITOR_SEGMENTATION_END, onSegmentationEnded);
		
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_ADDED_EVT, onRecordAddedAct);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_DELETED_EVT, onRecordDeletedAct);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.TIER_CHANGED_EVT, onTierChangedAct);
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
	}
	
	@RunOnEDT
	public void onMediaChanged(EditorEvent ee) {
		update();
		SessionMediaModel mediaModel = getEditor().getMediaModel();
		if(mediaModel.isSessionMediaAvailable()) {
			MediaPlayerEditorView mediaPlayerView = 
					(MediaPlayerEditorView)getEditor().getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
			if(mediaPlayerView.getPlayer().getMediaFile() != null) {
				mediaPlayerView.getPlayer().addMediaPlayerListener(playbackMediaListener);
			}
		}
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
			MediaSegment seg = r.getSegment().getGroup(0);
			float time = seg.getStartValue() / 1000.0f;
			
			var x = getTimeModel().xForTime(time);
			if(!tierPanel.getVisibleRect().contains(x, 0)) {
				scrollToTime(time);
			}
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
		
		if(playbackMediaListener.playbackMarker != null)
			timeModel.removeMarker(playbackMediaListener.playbackMarker);
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
				wavTier.setupContextMenu(null, builder);
			}
		} else {
			// Add browse for media action
			builder.addItem(".", new BrowseForMediaAction(getEditor()));
		}
		
		builder.addSeparator(".", "record_grid");
		
		recordGrid.setupContextMenu(null, builder);
		
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
		
		builder.addItem(".", new ZoomAction(this, -1));
		builder.addItem(".", new ZoomAction(this, 1));
		
		return menu;
	}

	private void showContextMenu(MouseEvent me) {
		JPopupMenu contextMenu = new JPopupMenu();
		
		MenuBuilder builder = new MenuBuilder(contextMenu);
		
		if(me.getComponent() == recordGrid.getRecordGrid()) {
			recordGrid.setupContextMenu(me, builder);
			builder.addSeparator(".", "wav_actions");
			wavTier.setupContextMenu(me, builder);
		} else {
			wavTier.setupContextMenu(me, builder);
			builder.addSeparator(".", "record_actions");
			recordGrid.setupContextMenu(me, builder);
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
		
		builder.addItem(".", new ZoomAction(this, -1));
		builder.addItem(".", new ZoomAction(this, 1));
		
		contextMenu.show(me.getComponent(), me.getX(), me.getY());
	}
		
	@Override
	public String getName() {
		return VIEW_TITLE;
	}

	@Override
	public ImageIcon getIcon() {
		return IconManager.getInstance().getIcon("misc/table", IconSize.SMALL);
	}
	
	@Override
	public void onOpen() {
		super.onOpen();
		
		registerEditorEvents();
		
		// scroll to current record position
		SwingUtilities.invokeLater(() -> {
			Record r = getEditor().currentRecord();
			if(r != null) {
				MediaSegment seg = r.getSegment().getGroup(0);
				if(seg != null && seg.getEndValue() - seg.getStartValue() > 0) {
					
					Rectangle2D segRect = new Rectangle2D.Double(
							timeModel.xForTime(seg.getStartValue()/1000.0f), 0,
							timeModel.xForTime(seg.getEndValue()/1000.0f) - timeModel.xForTime(seg.getStartValue()/1000.0f), 1);
					if(!segRect.intersects(recordGrid.getVisibleRect())) {
						scrollToTime(seg.getStartValue() / 1000.0f);
					}
				}
			}
		});
	}
	
	@Override
	public void onClose() {
		super.onClose();
		
		deregisterEditorEvents();
		/* XXX Fix this
		wavTier.onClose();
		recordGrid.onClose();
		*/
	}
	
	private class PlaybackMediaListener extends MediaPlayerEventAdapter {

		private TimeUIModel.Marker playbackMarker;
		
		private PlaybackMarkerTask playbackMarkerTask;
		
		@Override
		public void playing(MediaPlayer mediaPlayer) {
			if(playbackMarker == null && getEditor().getExtension(SegmentationHandler.class) == null) {
				float currentTime = mediaPlayer.status().time() / 1000.0f;
				
				playbackMarker = timeModel.addMarker(currentTime, Color.darkGray);
				playbackMarker.setRepaintOnTimeChange(false);
				
				playbackMarkerTask = new PlaybackMarkerTask(playbackMarker);
				playbackMarkerTask.mediaStartTime = mediaPlayer.status().time();
				playbackMarkerTask.systemStartTime = System.currentTimeMillis();
				
				Timer timer = new Timer(true);
				timer.schedule(playbackMarkerTask, 0L, (long)(1/30.0f * 1000.0f));
			}
		}

		@Override
		public void paused(MediaPlayer mediaPlayer) {
			timeModel.removeMarker(playbackMarker);
			playbackMarker = null;
			
			if(playbackMarkerTask != null) {
				playbackMarkerTask.cancel();
				playbackMarkerTask = null;
			}
		}
		
		@Override
		public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
			// sync time
			if(playbackMarkerTask != null) {
				playbackMarkerTask.mediaStartTime = newTime;
				playbackMarkerTask.systemStartTime = System.currentTimeMillis();
			}
		}
		
	}
	
	private class PlaybackMarkerTask extends TimerTask {

		volatile long systemStartTime;
		
		volatile long mediaStartTime = 0L;
		
		TimeUIModel.Marker playbackMarker;
		
		public PlaybackMarkerTask(TimeUIModel.Marker playbackMarker) {
			super();
			this.playbackMarker = playbackMarker;
		}
		
		@Override
		public void run() {
			if(playbackMarker != null) {
				long currentTime = System.currentTimeMillis();
				long newTime = mediaStartTime + (currentTime - systemStartTime);
				playbackMarker.setTime(newTime / 1000.0f);		
				
				repaint((long)(1/30.0f * 1000.0f));
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
			return 100;
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
