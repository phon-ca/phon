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
package ca.phon.app.session.editor.view.speech_analysis;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.EditorViewAdapter;
import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.actions.*;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.speech_analysis.actions.NewRecordAction;
import ca.phon.app.session.editor.view.speech_analysis.actions.*;
import ca.phon.media.*;
import ca.phon.media.TimeUIModel.*;
import ca.phon.media.export.VLCWavExporter;
import ca.phon.plugin.*;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.icons.*;
import ca.phon.worker.*;
import ca.phon.worker.PhonTask.TaskStatus;
import org.apache.commons.logging.Log;
import org.apache.logging.log4j.LogManager;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 * Displays wavform and associated commands.
 *
 */
public class SpeechAnalysisEditorView extends EditorView {

	/** Editor event sent when time model has been updated */
	public final static EditorEventType<TimeUIModel> TimeModelUpdated =
			new EditorEventType<>("_SPEECH_ANALYSIS_TIME_MODEL_UPDATE_", TimeUIModel.class);

	public static final String VIEW_TITLE = "Speech Analysis";

	public static final int MAX_TIER_HEIGHT = Integer.MAX_VALUE;

	private final static long CLIP_EXTENSION_MIN = 500L;

	private final static long CLIP_EXTENSION_MAX = 1000L;

	private TierPanel tierPane;

	static {
		SpeechAnalysisViewColors.install();
	}
	
	/**
	 * Time model for the view
	 */
	private TimeUIModel timeModel;

	/**
	 * Wav display
	 */
	private SpeechAnalysisWaveformTier waveformTier;
	
	/**
	 * Cursor
	 */
	private Marker cursorMarker = new Marker(-1.0f, UIManager.getColor(SpeechAnalysisViewColors.CURSOR_MARKER_COLOR));
	
	/**
	 * Playback marker
	 */
	private Marker playbackMarker;
	
	/**
	 * Current record interval
	 */
	private Interval currentRecordInterval;
	
	/**
	 * Current selection interval
	 */
	private Interval selectionInterval;

	/* Toolbar and buttons */
	private JToolBar toolbar;

	private DropDownButton playButton;
	private DropDownButton exportButton;

	private JButton refreshButton;
	private JButton showMoreButton;
	private JButton zoomOutButton;

	private JPanel errorPanel;
	private ErrorBanner messageButton = new ErrorBanner();
	private PhonTaskButton generateButton = null;

	private VolumeSlider volumeSlider;

	private final List<SpeechAnalysisTier> pluginTiers =
			Collections.synchronizedList(new ArrayList<SpeechAnalysisTier>());

	public SpeechAnalysisEditorView(SessionEditor editor) {
		super(editor);
		
		
		init();
		update();
		
		editor.getMediaModel().getSegmentPlayback().addPropertyChangeListener(playbackListener);
		addEditorViewListener(editorViewListener);
	}

	private void loadPlugins() {
		pluginTiers.clear();

		final PluginManager pluginManager = PluginManager.getInstance();
		final List<IPluginExtensionPoint<SpeechAnalysisTier>> extraTiers =
				pluginManager.getExtensionPoints(SpeechAnalysisTier.class);
		for(IPluginExtensionPoint<SpeechAnalysisTier> extraTier:extraTiers) {
			try {
				SpeechAnalysisTier tier = extraTier.getFactory().createObject(this);
				addTier(tier);
				pluginTiers.add(tier);
			} catch (Exception e) {
				LogUtil.warning(e);
			}
		}
	}

	public List<SpeechAnalysisTier> getPluginTiers() {
		return Collections.unmodifiableList(this.pluginTiers);
	}

	private void init() {
		setLayout(new BorderLayout());
		toolbar = setupToolbar();

		volumeSlider = new VolumeSlider(getEditor().getMediaModel().getVolumeModel());
		volumeSlider.setFocusable(false);
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(toolbar, BorderLayout.CENTER);
		topPanel.add(volumeSlider, BorderLayout.EAST);
		add(topPanel, BorderLayout.NORTH);

		errorPanel = new JPanel(new VerticalLayout());
		errorPanel.add(messageButton);

		add(errorPanel, BorderLayout.SOUTH);

		timeModel = new TimeUIModel();
		timeModel.addPropertyChangeListener( (e) -> {
			if(e.getPropertyName().equals("pixelsPerSecond")) {
				revalidate();
				repaint();
			}
		});
		waveformTier = new SpeechAnalysisWaveformTier(this);

		Timebar timebar = new Timebar(timeModel);
		timebar.setBackground(Color.white);
		timebar.setOpaque(true);
		
		timebar.addMouseListener(contextMenuAdapter);
		timebar.addMouseListener(cursorAndSelectionAdapter);
		timebar.addMouseMotionListener(cursorAndSelectionAdapter);

		tierPane = new TierPanel(new GridBagLayout());
		addTier(waveformTier);
		
		loadPlugins();
		
		final JScrollPane scroller = new JScrollPane(tierPane);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroller.setColumnHeaderView(timebar);
		add(scroller, BorderLayout.CENTER);
		
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

		setupInputMap();
		setupEditorActions();
	}
	
	private int tierIdx = 0;
	private void addTier(JComponent tierComp) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = tierIdx++;
		
		tierPane.add(tierComp, gbc);
	}
	
	@Override
	public String getName() {
		return VIEW_TITLE;
	}

	public JToolBar getToolbar() {
		return this.toolbar;
	}

	private void setupInputMap() {
		final ActionMap am = getActionMap();
		final InputMap im = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		
		final String escapeId = "escape";
		final PhonUIAction<Void> escapeAct = PhonUIAction.eventConsumer(this::onEscape);
		final KeyStroke escapeKs = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		am.put(escapeId, escapeAct);
		im.put(escapeKs, escapeId);
		
		final String selectId = "select";
		final PhonUIAction<Void> selectAct = PhonUIAction.eventConsumer(this::onEnter);
		final KeyStroke selectKs = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		am.put(selectId, selectAct);
		im.put(selectKs, selectId);

		final String playId = "play";
		final Action playAct = new PlayAction(getEditor(), this);
		final KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
		am.put(playId, playAct);
		im.put(ks, playId);

		setActionMap(am);
		setInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, im);
	}
	
	public void onEscape(PhonActionEvent<Void> pae) {
		if(isPlaying()) {
			stopPlaying();
		} else if(selectionInterval != null) {
			clearSelection();
		}
	}
	
	public void onEnter(PhonActionEvent<Void> pae) {
		if(selectionInterval != null) {
			// use selection as new segment for current record
			Record r = getEditor().currentRecord();
			if(r == null) return;
			
			MediaSegment seg = SessionFactory.newFactory().createMediaSegment();
			seg.setStartValue(selectionInterval.getStartMarker().getTime() * 1000.0f);
			seg.setEndValue(selectionInterval.getEndMarker().getTime() * 1000.0f);
			
			clearSelection();
			
			TierEdit<MediaSegment> segEdit = new TierEdit<MediaSegment>(getEditor(), r.getSegment(), 0, seg);
			segEdit.setFireHardChangeOnUndo(true);
			getEditor().getUndoSupport().postEdit(segEdit);
		}
	}

	private JToolBar setupToolbar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		final JPopupMenu playMenu = new JPopupMenu();
		playMenu.addPopupMenuListener(new PopupMenuListener() {
			
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				playMenu.removeAll();
				setupPlaybackMenu(new MenuBuilder(playMenu));
			}
			
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
			
		});
		
		final PhonUIAction<Void> playAct = PhonUIAction.runnable(this::playPause);
		playAct.putValue(PhonUIAction.NAME, "Play segment");
		playAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Play selection/segment");
		playAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL));
		playAct.putValue(DropDownButton.BUTTON_POPUP, playMenu);
		playButton = new DropDownButton(playAct);
		playButton.setFocusable(false);
		playButton.setEnabled(false);

		final JPopupMenu saveMenu = new JPopupMenu();
		saveMenu.addPopupMenuListener(new PopupMenuListener() {
			
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				saveMenu.removeAll();
				setupExportMenu(new MenuBuilder(saveMenu));
			}
			
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				
			}
			
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				
			}
			
		});
		
		final PhonUIAction<Void> exportAct = PhonUIAction.runnable(this::onExportSelectionOrSegment);
		exportAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/document-save-as", IconSize.SMALL));
		exportAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Export selection/segment (audio only)");
		exportAct.putValue(PhonUIAction.NAME, "Export segment...");
		exportAct.putValue(DropDownButton.BUTTON_POPUP, saveMenu);
		
		exportButton = new DropDownButton(exportAct);
		exportButton.setFocusable(false);
		exportButton.setEnabled(false);
		
		final ResetAction refreshAct = new ResetAction(getEditor(), this);
		refreshButton = new JButton(refreshAct);
		refreshButton.setFocusable(false);

		final ZoomAction showMoreAct = new ZoomAction(getEditor(), this);
		showMoreButton = new JButton(showMoreAct);
		showMoreButton.setFocusable(false);

		final ZoomAction zoomOutAct = new ZoomAction(getEditor(), this, false);
		zoomOutButton = new JButton(zoomOutAct);
		zoomOutButton.setFocusable(false);

		toolbar.add(playButton);
		toolbar.add(exportButton);
		toolbar.addSeparator();
		toolbar.add(refreshButton);
		toolbar.add(showMoreButton);
		toolbar.add(zoomOutButton);

		return toolbar;
	}
	
	private void setupPlaybackMenu(MenuBuilder builder) {
		if(isPlaying()) {
			final PhonUIAction<Void> stopAct = PhonUIAction.runnable(SpeechAnalysisEditorView.this::stopPlaying);
			stopAct.putValue(PhonUIAction.NAME, "Stop");
			stopAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Stop playback");
			stopAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/media-playback-stop", IconSize.SMALL));
			
			builder.addItem(".", stopAct);
			builder.addSeparator(".", "stop");
		}
		
		final PhonUIAction<Void> playSelectionAct = PhonUIAction.runnable(SpeechAnalysisEditorView.this::playSelection);
		playSelectionAct.putValue(PhonUIAction.NAME, "Play selection");
		playSelectionAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Play current selection");
		playSelectionAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL));
		final JMenuItem playSelectionItem = new JMenuItem(playSelectionAct);
		playSelectionItem.setEnabled( selectionInterval != null );
		
		final PhonUIAction<Void> playSegmentAct = PhonUIAction.runnable(SpeechAnalysisEditorView.this::playSegment);
		playSegmentAct.putValue(PhonUIAction.NAME, "Play record segment");
		playSegmentAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Play current record segment");
		playSegmentAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL));
		final JMenuItem playSegmentItem = new JMenuItem(playSegmentAct);
		playSegmentItem.setEnabled( currentRecordInterval != null );
		
		builder.addItem(".", playSelectionItem);
		builder.addItem(".", playSegmentItem);
	}
	
	private void setupExportMenu(MenuBuilder builder) {
		final PhonUIAction<Void> exportSelectionAct = PhonUIAction.runnable(this::exportSelection);
		exportSelectionAct.putValue(PhonUIAction.NAME, "Export selection...");
		exportSelectionAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Export selection (audio only)");
		exportSelectionAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/document-save-as", IconSize.SMALL));
		
		final PhonUIAction<Void> exportSegmentAct = PhonUIAction.runnable(this::exportSegment);
		exportSegmentAct.putValue(PhonUIAction.NAME, "Export record segment...");
		exportSegmentAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Export record segment (audio only)");
		exportSegmentAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/document-save-as", IconSize.SMALL));
		
		builder.addItem(".", exportSelectionAct).setEnabled(selectionInterval != null);
		builder.addItem(".", exportSegmentAct).setEnabled(currentRecordInterval != null);
	}
	
	public void onExportSelectionOrSegment() {
		if(selectionInterval != null) {
			exportSelection();
		} else {
			exportSegment();
		}
	}
	
	public void exportSelection() {
		if(selectionInterval != null)
			exportInterval(selectionInterval);
	}
	
	public void exportSegment() {
		if(currentRecordInterval != null)
			exportInterval(currentRecordInterval);
	}

	public void exportInterval(Interval interval) {
		exportInterval(interval.getStartMarker().getTime(), interval.getEndMarker().getTime());
	}
	
	public void exportInterval(float startTime, float endTime) {
		ExportSegmentAction exportAct = new ExportSegmentAction(getEditor(), startTime, endTime);
		exportAct.actionPerformed(new ActionEvent(this, -1, "export"));
	}
	
	/* toolbar actions */
	public void playPause() {
		if(isPlaying()) {
			stopPlaying();
		} else if(selectionInterval != null) {
			playSelection();
		} else if(currentRecordInterval != null) {
			playSegment();
		}
	}
	
	public void playSelection() {
		if(selectionInterval != null)
			playInterval(selectionInterval);
	}
	
	public void playSegment() {
		if(currentRecordInterval != null)
			playInterval(currentRecordInterval);
	}
	
	private void playInterval(Interval interval) {
		playInterval(interval.getStartMarker().getTime(), interval.getEndMarker().getTime());
	}
	
	private void playInterval(float startTime, float endTime) {
		SessionMediaModel mediaModel = getEditor().getMediaModel();
		mediaModel.getSegmentPlayback().playSegment(startTime, endTime);
	}
	
	public boolean isPlaying() {
		SessionMediaModel mediaModel = getEditor().getMediaModel();
		return mediaModel.getSegmentPlayback().isPlaying();
	}
	
	public void stopPlaying() {
		SessionMediaModel mediaModel = getEditor().getMediaModel();
		mediaModel.getSegmentPlayback().stopPlaying();
	}
	
	public TimeUIModel getTimeModel() {
		return this.timeModel;
	}

	/* Cursor */
	
	public Marker getCursorMarker() {
		return this.cursorMarker;
	}
	
	/* Selection */
	/**
	 * Clear current selection interval (if any)
	 */
	public void clearSelection() {
		if(selectionInterval != null) {
			timeModel.removeInterval(selectionInterval);
			selectionInterval = null;
		}
	}
	
	/**
	 * Set selection interval
	 * 
	 * @param startTime
	 * @param endTime
	 * @return selection interval
	 */
	public Interval setSelection(float startTime, float endTime) {
		if(selectionInterval != null)
			clearSelection();
		
		selectionInterval = timeModel.addInterval(startTime, endTime);
		selectionInterval.setColor(UIManager.getColor(SpeechAnalysisViewColors.SELECTED_INTERVAL_BACKGROUND));
		selectionInterval.getStartMarker().setColor(UIManager.getColor(SpeechAnalysisViewColors.SELECTED_INTERVAL_MARKER_COLOR));
		selectionInterval.getEndMarker().setColor(UIManager.getColor(SpeechAnalysisViewColors.SELECTED_INTERVAL_MARKER_COLOR));
		
		return selectionInterval;
	}
	
	/**
	 * Selection interval for the view.  
	 * 
	 * @return selection interval or <code>null</code> if no
	 * selection is present
	 */
	public Interval getSelectionInterval() {
		return this.selectionInterval;
	}
	
	/**
	 * Interval for current current record. Changes to this interval
	 * will be reflected in the media segment tier for the current
	 * editor record (if any.)
	 * 
	 * @return current record interval or <code>null</code> if no
	 * record is currently loaded
	 */
	public Interval getCurrentRecordInterval() {
		return this.currentRecordInterval;
	}
	
	/* 
	 * Listeners for tiers. Tiers should add these listeners
	 * to inner lightweight components as applicable
	 */
	public MouseAdapter getCursorAndSelectionAdapter() {
		return this.cursorAndSelectionAdapter;
	}
	
	public MouseAdapter getContextMenuAdapter() {
		return this.contextMenuAdapter;
	}
	
	/**
	 * Default tier for the view
	 * 
	 * @return waveform tier
 	 */
	public SpeechAnalysisWaveformTier getWaveformTier() {
		return this.waveformTier;
	}
	
	/**
	 * Return the start time of the visible rect
	 * 
	 * @return
	 */
	public float getWindowStart() {
		return getTimeModel().timeAtX(tierPane.getVisibleRect().getX());
	}
	
	public float getWindowEnd() {
		return getTimeModel().timeAtX(tierPane.getVisibleRect().getMaxX());
	}

	public double getWindowStartX() {
		return tierPane.getVisibleRect().getX();
	}
	
	public double getWindowEndX() {
		return tierPane.getVisibleRect().getMaxX();
	}
	
	public float getWindowLength() {
		return getWindowEnd() - getWindowStart();
	}

	private void setupTimeModel() {
		float startTime = 0.0f;
		float pxPerS = 100.0f;
		float endTime = 0.0f;
		float scrollTo = 0.0f;
		
		if(currentRecordInterval != null)
			timeModel.removeInterval(currentRecordInterval);
		if(selectionInterval != null)
			clearSelection();
		
		Record r = getEditor().currentRecord();
		if(r != null) {
			final MediaSegment segment = r.getSegment().getGroup(0);

			double length = (segment.getEndValue() - segment.getStartValue());
			long preferredClipExtension = (long)Math.ceil(length * 0.4);
			if(preferredClipExtension < CLIP_EXTENSION_MIN)
				preferredClipExtension = CLIP_EXTENSION_MIN;
			if(preferredClipExtension > CLIP_EXTENSION_MAX)
				preferredClipExtension = CLIP_EXTENSION_MAX;

			float clipStart = (Math.round(segment.getStartValue() - preferredClipExtension)) / 1000.0f;
			float displayStart = Math.max(0.0f, clipStart);
			float segStart = segment.getStartValue() / 1000.0f;
			float segLength = (segment.getEndValue() - segment.getStartValue()) / 1000.0f;
			float displayLength = segLength + ((2*preferredClipExtension) / 1000.0f);
			
			if(waveformTier.getWaveformDisplay().getLongSound() != null) {
				if((displayStart + displayLength) > waveformTier.getWaveformDisplay().getLongSound().length()) {
					displayStart = waveformTier.getWaveformDisplay().getLongSound().length() - displayLength;
	
					if(displayStart < 0.0f) {
						displayStart = 0.0f;
						displayLength = waveformTier.getWaveformDisplay().getLongSound().length();
					}
				}
			}

			int displayWidth = getVisibleRect().width;
			if(displayWidth > 0)
				pxPerS = displayWidth / displayLength;
			
			currentRecordInterval = timeModel.addInterval(segStart, segStart+segLength);
			currentRecordInterval.setColor(UIManager.getColor(SpeechAnalysisViewColors.INTERVAL_BACKGROUND));
			currentRecordInterval.getStartMarker().setColor(UIManager.getColor(SpeechAnalysisViewColors.INTERVAL_MARKER_COLOR));
			currentRecordInterval.getEndMarker().setColor(UIManager.getColor(SpeechAnalysisViewColors.INTERVAL_MARKER_COLOR));
			currentRecordInterval.setRepaintEntireInterval(true);
			currentRecordInterval.addPropertyChangeListener(new RecordIntervalListener());
			scrollTo = displayStart;
		}
		
		if(waveformTier.getWaveformDisplay().getLongSound() != null) {
			endTime = waveformTier.getWaveformDisplay().getLongSound().length();
		}
		
		timeModel.setStartTime(startTime);
		timeModel.setEndTime(endTime);
		timeModel.setPixelsPerSecond(pxPerS);

		final float scrollToTime = scrollTo;
		SwingUtilities.invokeLater(() -> scrollToTime(scrollToTime));

		getEditor().getEventManager().queueEvent(new EditorEvent<>(TimeModelUpdated, this, timeModel));
	}

	public void scrollToRecord(Record r) {
		MediaSegment seg = r.getSegment().getGroup(0);
		float time = seg.getStartValue() / 1000.0f;
		float endTime = seg.getEndValue() / 1000.0f;
		float windowLen = endTime - time;

		float viewStart = getWindowStart();
		float viewEnd = getWindowEnd();
		float viewLen = viewEnd - viewStart;

		// entire segment can be viewed
		float delta = viewLen - windowLen;
		float newViewStart = Math.max(0, time - (delta/2.0f));

		if(newViewStart + viewLen > getTimeModel().getEndTime()) {
			newViewStart = getTimeModel().getEndTime() - viewLen;
		}

		scrollToTime(newViewStart);
	}

	public void update() {
		SessionMediaModel mediaModel = getEditor().getMediaModel();
		if(mediaModel.isSessionAudioAvailable()) {
			setTiersVisible(true);
			try {
				LongSound ls = mediaModel.getSharedSessionAudio();
				if(waveformTier.getWaveformDisplay().getLongSound() != ls) {
					waveformTier.getWaveformDisplay().setEndTime(ls.length());
					waveformTier.getWaveformDisplay().setLongSound(ls);
					
					PlaySegment playSeg = ls.getExtension(PlaySegment.class);
					playButton.setEnabled( playSeg != null );
					
					ExportSegment exportSeg = ls.getExtension(ExportSegment.class);
					exportButton.setEnabled( exportSeg != null );
				}
				messageButton.setVisible(false);
			} catch (IOException e) {
				LogUtil.severe(e);
				setTiersVisible(false);
			}
		} else {
			getTimeModel().clearIntervals();
			getTimeModel().clearMarkers();
			waveformTier.getWaveformDisplay().setLongSound(null);
			
			messageButton.clearActions();
			setTiersVisible(false);

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
			revalidate();
			messageButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
		setupTimeModel();
	}
	
	private void setTiersVisible(boolean visible) {
		waveformTier.setVisible(visible);
		for(SpeechAnalysisTier tier:pluginTiers) {
			tier.setVisible(tier.shouldShow() && visible);
			if(tier.shouldShow() && visible) {
				tier.revalidate();
				tier.repaint();
			}
		}
	}
	
	public void scrollToTime(float time) {
		var x = getTimeModel().xForTime(time);
		var rect = tierPane.getVisibleRect();
		rect.x = (int)x;
		tierPane.scrollRectToVisible(rect);
	}
	
	/** Editor events */
	private void setupEditorActions() {
		getEditor().getEventManager().registerActionForEvent(EditorEventType.EditorFinishedLoading, this::onSessionLoaded, EditorEventManager.RunOn.AWTEventDispatchThread);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SessionChanged, this::onSessionChanged, EditorEventManager.RunOn.AWTEventDispatchThread);

		getEditor().getEventManager().registerActionForEvent(SessionMediaModel.SessionAudioAvailable, this::onSessionAudioAvailable, EditorEventManager.RunOn.AWTEventDispatchThread);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SessionMediaChanged, this::onSessionMediaChanged, EditorEventManager.RunOn.AWTEventDispatchThread);

		getEditor().getEventManager().registerActionForEvent(EditorEventType.RecordChanged, this::onRecordChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RecordRefresh, this::onRecordRefresh, EditorEventManager.RunOn.AWTEventDispatchThread);

		getEditor().getEventManager().registerActionForEvent(EditorEventType.TierChanged, this::onMediaSegmentChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
	}

	private void onSessionChanged(EditorEvent ee) {
		onRecordChanged(ee);
	}

	private void onSessionLoaded(EditorEvent<Void> ee) {
		// time model will be at default settings when initialized
		setupTimeModel();
	}
	
	private void onRecordChanged(EditorEvent<EditorEventType.RecordChangedData> ee) {
		if(!isVisible() || !getEditor().getViewModel().isShowing(VIEW_TITLE)
				|| !getEditor().getViewModel().isShowingInStack(VIEW_TITLE)) return;
		setupTimeModel();
	}

	private void onSessionMediaChanged(EditorEvent<EditorEventType.SessionMediaChangedData> ee) {
		update();
	}
	
	private void onSessionAudioAvailable(EditorEvent<SessionMediaModel> ee) {
		update();
	}

	private void onMediaSegmentChanged(EditorEvent<EditorEventType.TierChangeData> ee) {
		if(ee.data().tier().getName().equals(SystemTierType.Segment.getName())
				&& getEditor().getViewModel().isShowingInStack(VIEW_TITLE))
			setupTimeModel();
	}

	private void onRecordRefresh(EditorEvent<EditorEventType.RecordChangedData> ee) {
		if(!isVisible() || !getEditor().getViewModel().isShowing(VIEW_TITLE)) return;
		update();
	}

	@Override
	public ImageIcon getIcon() {
		return IconManager.getInstance().getIcon("docking-frames/waveform", IconSize.SMALL);
	}

	@Override
	public JMenu getMenu() {
		final JMenu retVal = new JMenu();
		MenuBuilder builder = new MenuBuilder(retVal);
		
		if(selectionInterval != null) {
			final PhonUIAction<Void> selectAct = PhonUIAction.eventConsumer(this::onEnter);
			selectAct.putValue(PhonUIAction.NAME, "Set segment");
			selectAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Select segment for current record");
			builder.addItem(".", selectAct);
			
			builder.addItem(".", new NewRecordAction(getEditor(), this));
			builder.addSeparator(".", "selection");
		}

		SessionMediaModel mediaModel = getEditor().getMediaModel();
		if(mediaModel.isSessionAudioAvailable()) {
			setupPlaybackMenu(builder);
			builder.addSeparator(".", "playback");
			builder.addSeparator(".", "export");
		} else {
			if(mediaModel.isSessionMediaAvailable()) {
				builder.addItem(".", new GenerateSessionAudioAction(getEditor()));
				builder.addSeparator(".", "generate");
			}
		}
		
		retVal.add(new ResetAction(getEditor(), this));
		retVal.add(new ZoomAction(getEditor(), this));
		retVal.add(new ZoomAction(getEditor(), this, false));
		
		for(SpeechAnalysisTier tier:pluginTiers) {
			tier.addMenuItems(retVal, false);
		}

		return retVal;
	}

	@Override
	public DockPosition getPreferredDockPosition() {
		return DockPosition.CENTER;
	}

	public JPanel getErrorPane() {
		return this.errorPanel;
	}
	

	private JMenu createContextMenu() {
		final JMenu menu = new JMenu();
		MenuBuilder builder = new MenuBuilder(menu);
		
		if(selectionInterval != null) {
			final PhonUIAction<Void> selectAct = PhonUIAction.eventConsumer(this::onEnter);
			selectAct.putValue(PhonUIAction.NAME, "Assign segment to record");
			selectAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Assign selected segment to current record");
			builder.addItem(".", selectAct);
			
			builder.addItem(".", new NewRecordAction(getEditor(), this));
			builder.addSeparator(".", "selection");
		}

		SessionMediaModel mediaModel = getEditor().getMediaModel();
		if(mediaModel.isSessionAudioAvailable()) {
			setupPlaybackMenu(builder);
			builder.addSeparator(".", "playback");
			builder.addSeparator(".", "export");
		} else {
			builder.addItem(".", mediaModel.getGenerateSessionAudioAction());
			builder.addSeparator(".", "generate");
		}
		
		menu.add(new ResetAction(getEditor(), this));
		menu.add(new ZoomAction(getEditor(), this));
		menu.add(new ZoomAction(getEditor(), this, false));

		for(SpeechAnalysisTier tier:getPluginTiers()) {
			tier.addMenuItems(menu, true);
		}

		return menu;
	}

	private void showContextMenu(MouseEvent e) {
		final JMenu menu = createContextMenu();
		menu.getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
	}
	
	private final EditorViewAdapter editorViewListener = new EditorViewAdapter() {
		
		@Override
		public void onFocused(EditorView view) {
			setupTimeModel();
			for(SpeechAnalysisTier tier:getPluginTiers())
				tier.onRefresh();
		}
		
	};
	
	private PropertyChangeListener playbackListener = new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			SegmentPlayback segmentPlayback = (SegmentPlayback)evt.getSource();
			if(SegmentPlayback.PLAYBACK_PROP.equals(evt.getPropertyName())) {
				if(segmentPlayback.isPlaying()) {
					playbackMarker = timeModel.addMarker(segmentPlayback.getTime(), UIManager.getColor(SpeechAnalysisViewColors.PLAYBACK_MARKER_COLOR));
					playbackMarker.setOwner(waveformTier.getWaveformDisplay());
					playbackMarker.setDraggable(false);
					
					playButton.setIcon(IconManager.getInstance().getIcon("actions/media-playback-stop", IconSize.SMALL));
					playButton.setText("Stop playback");
					
				} else {
					if(playbackMarker != null)
						timeModel.removeMarker(playbackMarker);
					playbackMarker = null;
					
					playButton.setIcon(IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL));
					playButton.setText("Play segment");
				}
			} else if(SegmentPlayback.TIME_PROP.equals(evt.getPropertyName())) {
				if(playbackMarker != null) {
					playbackMarker.setTime((float)evt.getNewValue());
				}
			}
		}
		
	};
	
	private final MouseAdapter contextMenuAdapter = new MouseAdapter() {
		
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
	
	private float initialSelectionTime = -1.0f;
	private final MouseAdapter cursorAndSelectionAdapter = new MouseAdapter() {

		@Override
		public void mousePressed(MouseEvent e) {
			TimeComponent tc = ((TimeComponent)e.getSource());
			if(tc.isFocusable() && tc.getUI().getCurrentlyDraggedInterval() == null)
				tc.requestFocusInWindow();
			
			if(cursorMarker != null && e.getButton() == MouseEvent.BUTTON1) {
				timeModel.removeMarker(cursorMarker);
				cursorMarker.setTime(-1.0f);
			}
			
			if(tc.getUI().getCurrentlyDraggedMarker() != null) {
				initialSelectionTime = -1.0f;
				return;
			}
			
			if(e.isPopupTrigger()) {
				return;
			}
			
			if(e.getButton() == MouseEvent.BUTTON1) {
				if(selectionInterval != null) {
					clearSelection();
				}
				
				initialSelectionTime = timeModel.timeAtX(e.getX());
			}
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			TimeComponent tc = ((TimeComponent)e.getSource());
			if(initialSelectionTime > 0) {
				float currentTime = getTimeModel().timeAtX(e.getX());
				float diff = currentTime - initialSelectionTime;
				if(selectionInterval == null) {
					float intervalStartTime, intervalEndTime = 0.0f;
					if(diff > 0) {
						intervalStartTime = initialSelectionTime;
						intervalEndTime = currentTime; 
						
					} else {
						intervalStartTime = currentTime;
						intervalEndTime = initialSelectionTime;
					}
					
					selectionInterval = setSelection(intervalStartTime, intervalEndTime);
					selectionInterval.addPropertyChangeListener("valueAdjusting", (evt) -> {
						if(tc.isFocusable() && Boolean.parseBoolean(evt.getNewValue().toString())) {
							tc.requestFocusInWindow();
						}
					});
					
					if(diff > 0) {
						tc.getUI().beginDrag(selectionInterval, selectionInterval.getEndMarker());
					} else {
						tc.getUI().beginDrag(selectionInterval, selectionInterval.getStartMarker());
					}
				}
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if(e.isPopupTrigger()) {
				return;
			}
			
			if(((JComponent)e.getSource()).getVisibleRect().contains(e.getPoint())) {
				mouseEntered(e);
			}
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {
			if(cursorMarker != null) {
				timeModel.removeMarker(cursorMarker);
			}
			cursorMarker.setTime(timeModel.timeAtX(e.getX()));
//			cursorMarker = timeModel.addMarker(timeModel.timeAtX(x), UIManager.getColor(SpeechAnalysisViewColors.CURSOR_MARKER_COLOR));
			cursorMarker.setDraggable(false);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			if(cursorMarker != null) {
				timeModel.removeMarker(cursorMarker);
				cursorMarker.setTime(-1.0f);
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			if(cursorMarker != null) {
				cursorMarker.setTime(timeModel.timeAtX(e.getX()));
			}
		}
		
	};

	private class RecordIntervalListener implements PropertyChangeListener {

		private boolean isFirstChange = true;

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			Record r = getEditor().currentRecord();
			if(r == null) return;
			
			MediaSegment segment = r.getSegment().getGroup(0);
			final SessionFactory factory = SessionFactory.newFactory();

			if(evt.getPropertyName().equals("valueAdjusting")) {
				if(isFocusable()) {
					requestFocusInWindow();
				}
								
				if((boolean)evt.getNewValue()) {
					isFirstChange = true;
					getEditor().getUndoSupport().beginUpdate();
				} else {
					getEditor().getUndoSupport().endUpdate();
					final EditorEventType.TierChangeData data = new EditorEventType.TierChangeData(r.getSegment(), 0, r.getSegment().getGroup(0), r.getSegment().getGroup(0));
					getEditor().getEventManager().queueEvent(new EditorEvent(EditorEventType.TierChanged, SpeechAnalysisEditorView.this, data));
				}
			} else if(evt.getPropertyName().endsWith("time")) {
				MediaSegment newSegment = factory.createMediaSegment();
				newSegment.setStartValue(segment.getStartValue());
				newSegment.setEndValue(segment.getEndValue());
				
				if(evt.getPropertyName().startsWith("startMarker")) {
					newSegment.setStartValue((float)evt.getNewValue() * 1000.0f);
				} else if(evt.getPropertyName().startsWith("endMarker")) {
					newSegment.setEndValue((float)evt.getNewValue() * 1000.0f);
				}
				
				TierEdit<MediaSegment> segmentEdit = new TierEdit<MediaSegment>(getEditor(), r.getSegment(), 0, newSegment);
				getEditor().getUndoSupport().postEdit(segmentEdit);
				segmentEdit.setFireHardChangeOnUndo(isFirstChange);
				isFirstChange = false;
			}
		}

	}
	
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
