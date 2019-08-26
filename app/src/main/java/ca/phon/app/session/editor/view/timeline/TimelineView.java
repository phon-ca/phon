package ca.phon.app.session.editor.view.timeline;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.derby.impl.sql.compile.SetSchemaNode;
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

public final class TimelineView extends EditorView {

	private static final long serialVersionUID = 8442995100291417078L;
	
	public static final String VIEW_TITLE = "Timeline";
	
	/**
	 * Values for the zoom bar
	 */
	public static final float zoomValues[] = { 25.0f, 50.0f, 100.0f, 200.0f, 400.0f, 800.0f, 1600.0f };
	
	private static final int defaultZoomIdx = 3;
	
	private JToolBar toolbar;
	
	private JSlider zoomSlider;
	
	private JButton segmentationButton;
	
	private DropDownButton speakerVisibilityButton;
	private JPopupMenu speakerVisibilityMenu;
	
	private DropDownButton tierVisiblityButton;
	private JPopupMenu tierVisibilityMenu;
	
	private JPanel tierPanel;
	
	/**
	 * Default {@link TimeUIModel} which should be
	 * used by most tier components
	 */
	private TimeUIModel timeModel;
	
	private TimelineWaveformTier wavTier;
	
	private TimelineRecordTier recordGrid;
	
	public TimelineView(SessionEditor editor) {
		super(editor);
		
		init();
		registerEditorEvents();
		update();
	}
	
	private void init() {
		toolbar = setupToolbar();

		// the shared time model
		timeModel = new TimeUIModel();
		
		timeModel.addPropertyChangeListener((e) -> {
			if(e.getPropertyName().equals("pixelsPerSecond")) {
				int zoomIdx = Arrays.binarySearch(zoomValues, (float)e.getNewValue());
				if(zoomIdx >= 0)
					zoomSlider.setValue(zoomIdx);
				tierPanel.revalidate();
			}
		});
		timeModel.setPixelsPerSecond(100.0f);
		timeModel.setStartTime(0.0f);
		timeModel.setEndTime(0.0f);	
		
		tierPanel = new JPanel(new GridBagLayout());
		JScrollPane scroller = new JScrollPane(tierPanel);
		
		// XXX Order here matters - for the purpose of
		// editor events the record tier object must be created before the
		// wav tier
		recordGrid = new TimelineRecordTier(this);
		
		wavTier = new TimelineWaveformTier(this);
		wavTier.getPreferredSize();
		
		addTier(wavTier);
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
		
		speakerVisibilityMenu = new JPopupMenu();
		speakerVisibilityMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				speakerVisibilityMenu.removeAll();
				recordGrid.setupSpeakerMenu(new MenuBuilder(speakerVisibilityMenu));
			}
			
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
			
		});
		
		final PhonUIAction speakerVisibilityAct = new PhonUIAction(this, null);
		speakerVisibilityAct.putValue(PhonUIAction.NAME, "Speakers");
		speakerVisibilityAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show speaker visibility menu");
		speakerVisibilityAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("apps/system-users", IconSize.SMALL));
		speakerVisibilityAct.putValue(DropDownButton.BUTTON_POPUP, speakerVisibilityMenu);
		speakerVisibilityAct.putValue(DropDownButton.ARROW_ICON_POSITION, SwingUtilities.BOTTOM);
		speakerVisibilityAct.putValue(DropDownButton.ARROW_ICON_GAP, 2);
		
		speakerVisibilityButton = new DropDownButton(speakerVisibilityAct);
		speakerVisibilityButton.setOnlyPopup(true);
		
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
		tierVisibilityAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("blank", IconSize.SMALL));
		tierVisibilityAct.putValue(DropDownButton.BUTTON_POPUP, tierVisibilityMenu);
		tierVisibilityAct.putValue(DropDownButton.ARROW_ICON_POSITION, SwingConstants.BOTTOM);
		tierVisibilityAct.putValue(DropDownButton.ARROW_ICON_GAP, 2);
		
		tierVisiblityButton = new DropDownButton(tierVisibilityAct);
		tierVisiblityButton.setOnlyPopup(true);
		
//		SplitRecordAction splitRecordAct = new SplitRecordAction(this);
//		JButton splitRecordButton = new JButton(splitRecordAct);
		
		zoomSlider = new JSlider(SwingConstants.HORIZONTAL, 0, zoomValues.length-1, defaultZoomIdx);
		zoomSlider.setPaintLabels(false);
		zoomSlider.setPaintTicks(true);
		zoomSlider.setPaintTrack(true);
		zoomSlider.setSnapToTicks(true);
		zoomSlider.putClientProperty("JComponent.sizeVariant", "small");
		
		zoomSlider.addChangeListener( (e) -> {
			getTimeModel().setPixelsPerSecond(zoomValues[zoomSlider.getValue()]);
		});
		
		toolbar.add(segmentationButton);
		toolbar.add(speakerVisibilityButton);
		toolbar.add(tierVisiblityButton);
//		toolbar.add(splitRecordButton);
		toolbar.add(zoomSlider);
		
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
		
		if(tier.isResizeable()) {
			final JSeparator separator =  new JSeparator(SwingConstants.HORIZONTAL);
			separator.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
			separator.addMouseMotionListener(new SeparatorMouseListener(tier));
			
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
		}
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
		if(mediaModel.isSessionMediaAvailable() && !mediaModel.isSessionAudioAvailable()) {
			// check if media is loaded in player, if so use time from player
			MediaPlayerEditorView mediaPlayerView = 
					(MediaPlayerEditorView)getEditor().getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
			if(mediaPlayerView.getPlayer().getMediaFile() != null) {
				endTime = Math.max(endTime, mediaPlayerView.getPlayer().getLength() / 1000.0f);
			}
		} else {
			endTime = Math.max(endTime, wavTier.getWaveformDisplay().getLongSound().length());
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
	
	/* Editor actions */
	
	private final DelegateEditorAction onMediaChangedAct = new DelegateEditorAction(this, "onMediaChanged");
	
	private final DelegateEditorAction onRecordChangeAct = new DelegateEditorAction(this, "onRecordChanged");
	
	private final DelegateEditorAction onSegmentationStarted = new DelegateEditorAction(this, "onSegmentationStarted");
	
	private final DelegateEditorAction onSegmentationEnded = new DelegateEditorAction(this, "onSegmentationEnded");
	
	private final DelegateEditorAction onRecordAddedAct = new DelegateEditorAction(this, "onRecordAdded");
	
	private final DelegateEditorAction onRecordDeletedAct = new DelegateEditorAction(this, "onRecordDeleted");
	
	private final DelegateEditorAction onTierChangedAct = new DelegateEditorAction(this, "onTierChanged");
	
	private void registerEditorEvents() {
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SESSION_MEDIA_CHANGED, onMediaChangedAct);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_CHANGED_EVT, onRecordChangeAct);
		
		getEditor().getEventManager().registerActionForEvent(SegmentationHandler.EDITOR_SEGMENTATION_START, onSegmentationStarted);
		getEditor().getEventManager().registerActionForEvent(SegmentationHandler.EDITOR_SEGMENTATION_END, onSegmentationEnded);
		
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_ADDED_EVT, onRecordAddedAct);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_DELETED_EVT, onRecordDeletedAct);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.TIER_CHANGED_EVT, onTierChangedAct);
	}
	
	private void deregisterEditorEvents() {
		getEditor().getEventManager().removeActionForEvent(EditorEventType.SESSION_MEDIA_CHANGED, onMediaChangedAct);
		getEditor().getEventManager().removeActionForEvent(EditorEventType.RECORD_CHANGED_EVT, onRecordChangeAct);
		
		getEditor().getEventManager().removeActionForEvent(SegmentationHandler.EDITOR_SEGMENTATION_START, onSegmentationStarted);
		getEditor().getEventManager().removeActionForEvent(SegmentationHandler.EDITOR_SEGMENTATION_END, onSegmentationEnded);
		
		getEditor().getEventManager().removeActionForEvent(EditorEventType.RECORD_ADDED_EVT, onRecordAddedAct);
		getEditor().getEventManager().removeActionForEvent(EditorEventType.RECORD_DELETED_EVT, onRecordDeletedAct);
		getEditor().getEventManager().removeActionForEvent(EditorEventType.TIER_CHANGED_EVT, onTierChangedAct);
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
			MediaSegment seg = r.getSegment().getGroup(0);
			float time = seg.getStartValue() / 1000.0f;
			
			var x = getTimeModel().xForTime(time);
			if(!tierPanel.getVisibleRect().contains(x, 0)) {
				scrollToTime(time);
			}
		}
	}
	
	@RunOnEDT
	public void onSegmentationStarted(EditorEvent ee) {
		segmentationButton.setText("Stop Segmentation");
		segmentationButton.setIcon(IconManager.getInstance().getIcon("actions/media-playback-stop", IconSize.SMALL));
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
			SegmentationStartDialog startDialog = new SegmentationStartDialog(getEditor());
			startDialog.setModal(true);
			startDialog.pack();
			startDialog.setLocationRelativeTo(this);
			startDialog.setVisible(true);
			
			if(!startDialog.wasCanceled()) {
				handler = new SegmentationHandler(getEditor());
				handler.setMediaStart(startDialog.getMediaStart());
				handler.setSegmentationMode(startDialog.getSegmentationMode());
				handler.getWindow().setBackwardWindowLengthMs(startDialog.getWindowLength());
				
				getEditor().putExtension(SegmentationHandler.class, handler);
				
				handler.startSegmentation();
			}
		}
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
	public JMenu getMenu() {
		JMenu menu = new JMenu();
		
		menu.add(new ZoomAction(this, 1));
		menu.add(new ZoomAction(this, -1));
		
		menu.addSeparator();
		
		MenuBuilder builder = new MenuBuilder(menu);
		recordGrid.setupContextMenu(builder);
		
		return menu;
	}
	
	@Override
	public void onClose() {
		super.onClose();
		
		deregisterEditorEvents();
		wavTier.onClose();
		recordGrid.onClose();
	}

	private class SeparatorMouseListener extends MouseInputAdapter {
		
		private TimelineTier tier;
		
		public SeparatorMouseListener(TimelineTier tier) {
			super();
			
			this.tier = tier;
		}
		
		public TimelineTier getTier() {
			return this.tier;
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			Dimension currentSize = wavTier.getSize();
			Dimension prefSize = wavTier.getPreferredSize();

			prefSize.height = currentSize.height + e.getY();
			if(prefSize.height < 0) prefSize.height = 0;
			
			tier.setPreferredSize(prefSize);
			tierPanel.revalidate();
		}
		
	}
	
}
