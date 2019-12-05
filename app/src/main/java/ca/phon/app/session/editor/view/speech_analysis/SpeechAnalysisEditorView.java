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
package ca.phon.app.session.editor.view.speech_analysis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer.Info;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.apache.logging.log4j.LogManager;
import org.jdesktop.swingx.VerticalLayout;

import ca.phon.app.log.LogUtil;
import ca.phon.app.media.TimeUIModel;
import ca.phon.app.media.TimeUIModel.Interval;
import ca.phon.app.media.Timebar;
import ca.phon.app.media.WaveformDisplay;
import ca.phon.app.session.EditorViewAdapter;
import ca.phon.app.session.SessionMediaModel;
import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.DockPosition;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.GenerateSessionAudioAction;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.session_information.SessionInfoEditorView;
import ca.phon.app.session.editor.view.speech_analysis.actions.NewRecordAction;
import ca.phon.app.session.editor.view.speech_analysis.actions.PlayAction;
import ca.phon.app.session.editor.view.speech_analysis.actions.ResetAction;
import ca.phon.app.session.editor.view.speech_analysis.actions.SaveAction;
import ca.phon.app.session.editor.view.speech_analysis.actions.StopAction;
import ca.phon.app.session.editor.view.speech_analysis.actions.ZoomAction;
import ca.phon.app.session.editor.view.timeline.TimelineTier;
import ca.phon.media.LongSound;
import ca.phon.media.sampled.PCMSampled;
import ca.phon.media.sampled.PCMSegmentView;
import ca.phon.media.sampled.Sampled;
import ca.phon.media.sampled.actions.SelectMixerAction;
import ca.phon.media.sampled.actions.SelectSegmentAction;
import ca.phon.media.sampled.actions.ToggleLoop;
import ca.phon.media.util.MediaLocator;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;
import ca.phon.session.MediaSegment;
import ca.phon.session.MediaUnit;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.ui.HidablePanel;
import ca.phon.ui.PhonTaskButton;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTask.TaskStatus;
import ca.phon.worker.PhonTaskListener;
import ca.phon.worker.PhonWorker;

/**
 * Displays wavform and associated commands.
 *
 */
public class SpeechAnalysisEditorView extends EditorView {

	private static final long serialVersionUID = -1680881691504590317L;

	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SpeechAnalysisEditorView.class.getName());

	public static final String VIEW_TITLE = "Speech Analysis";

	public static final int MAX_TIER_HEIGHT = Integer.MAX_VALUE;

	private TierPanel tierPane;

	/**
	 * Time model for the view
	 */
	private TimeUIModel timeModel;

	/**
	 * Wav display
	 */
	private SpeechAnalysisWaveformTier waveformTier;
	
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

	private JButton playButton;
	private JButton refreshButton;

	private JButton exportButton;

	private JButton showMoreButton;
	private JButton zoomOutButton;

	private JButton generateButton;

	private JPanel btmPanel;

	private JPanel errorPanel;
	private HidablePanel messageButton = new HidablePanel("SpeechAnalysisView.noAudio");

	private final static String WAV_DISPLAY_HEIGHT = "SpeechAnalysisView.wavDisplayHeight";
	private final int DEFAULT_WAV_DISPLAY_HEIGHT = 100;
	private JComponent sizer;
	private int wavDisplayHeight =
			PrefHelper.getInt(WAV_DISPLAY_HEIGHT, DEFAULT_WAV_DISPLAY_HEIGHT);

//	private JScrollBar horizontalScroller;
//	private volatile boolean isDragging = false;

	private final List<SpeechAnalysisTier> pluginTiers =
			Collections.synchronizedList(new ArrayList<SpeechAnalysisTier>());

	public SpeechAnalysisEditorView(SessionEditor editor) {
		super(editor);

		init();
		update();
	}

	private void setupEditorActions() {
		final DelegateEditorAction sessionChangedAct =
			new DelegateEditorAction(this, "onSessionChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.EDITOR_FINISHED_LOADING, sessionChangedAct);

		final DelegateEditorAction recordChangedAct =
				new DelegateEditorAction(this, "onRecordChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_CHANGED_EVT, recordChangedAct);

		final DelegateEditorAction recordRefershAct =
				new DelegateEditorAction(this, "onRecordRefresh");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_REFRESH_EVT, recordRefershAct);

		final DelegateEditorAction sessionMediaChangedAct =
				new DelegateEditorAction(this, "onSessionMediaChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SESSION_MEDIA_CHANGED, sessionMediaChangedAct);

		final DelegateEditorAction segmentChangedAct =
				new DelegateEditorAction(this, "onMediaSegmentChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.TIER_CHANGED_EVT, segmentChangedAct);
		
		final DelegateEditorAction onSessionAudioAvailableAct = 
				new DelegateEditorAction(this, "onSessionAudioAvailable");
		getEditor().getEventManager().registerActionForEvent(SessionMediaModel.SESSION_AUDIO_AVAILABLE, onSessionAudioAvailableAct);
	}

	private void loadPlugins() {
		pluginTiers.clear();

		final PluginManager pluginManager = PluginManager.getInstance();
		final List<IPluginExtensionPoint<SpeechAnalysisTier>> extraTiers =
				pluginManager.getExtensionPoints(SpeechAnalysisTier.class);
		for(IPluginExtensionPoint<SpeechAnalysisTier> extraTier:extraTiers) {
			try {
				// ANYTHING can happen during plug-in object creation,
				// try to catch exceptions which the plug-in lets through
				pluginTiers.add(extraTier.getFactory().createObject(this));
			} catch (Exception e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
	}

	public List<SpeechAnalysisTier> getPluginTiers() {
		return Collections.unmodifiableList(this.pluginTiers);
	}

	private void init() {
		setLayout(new BorderLayout());
		setupToolbar();
		
		btmPanel = new JPanel(new VerticalLayout());
		btmPanel.removeAll();

		errorPanel = new JPanel(new VerticalLayout());
		errorPanel.add(messageButton);

		btmPanel.add(errorPanel);
		add(btmPanel, BorderLayout.SOUTH);

		timeModel = new TimeUIModel();
		waveformTier = new SpeechAnalysisWaveformTier(this);
//		wavDisplay.addMouseListener(contenxtMenuHandler);

		Timebar timebar = new Timebar(timeModel);
		timebar.setBackground(Color.white);
		timebar.setOpaque(true);

		tierPane = new TierPanel(new GridBagLayout());
		addTier(timebar);
		addTier(waveformTier);
		
//		// add plug-in tiers
//		final JPanel tierPanel = initPlugins();
//		contentPane.add(tierPanel);

		final JScrollPane scroller = new JScrollPane(tierPane);
//		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scroller, BorderLayout.CENTER);

		setupInputMap();
		setupEditorActions();
		setupTimeScrollbar();
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

	private void setupTimeScrollbar() {
//		final Sampled samples = getWavDisplay().getSampled();
//		if(samples != null) {
//			float length = samples.getLength();
//			final int numTicks =
//					(length - getWavDisplay().getWindowLength() <= 0 ? 1 : (int)Math.ceil( (length - getWavDisplay().getWindowLength()) * 1000));
//
//			Runnable onEDT = () -> {
//				horizontalScroller.setEnabled(true);
//				horizontalScroller.setValueIsAdjusting(true);
//				horizontalScroller.setMinimum(0);
//				horizontalScroller.setMaximum(numTicks);
//				horizontalScroller.setUnitIncrement(100);
//				horizontalScroller.setBlockIncrement(1000);
//				horizontalScroller.setValue((int)Math.floor(getWavDisplay().getWindowStart() * 1000));
//				horizontalScroller.setValueIsAdjusting(false);
//			};
//			if(SwingUtilities.isEventDispatchThread())
//				onEDT.run();
//			else
//				SwingUtilities.invokeLater(onEDT);
//		} else {
//			horizontalScroller.setEnabled(false);
//		}
	}

	private JPanel initPlugins() {
		loadPlugins();
		final JPanel retVal = new JPanel(new VerticalLayout());
//		for(SpeechAnalysisTier tier:pluginTiers) {
//			final JComponent comp = tier.getTierComponent();
//			comp.addMouseListener(contenxtMenuHandler);
//			retVal.add(comp);
//		}
		return retVal;
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

//		final String selectId = "select";
//		final SelectSegmentAction selectAct = new SelectSegmentAction(wavDisplay);
//		final KeyStroke selectKs = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
//		am.put(selectId, selectAct);
//		im.put(selectKs, selectId);

		final String playId = "play";
		final Action playAct = new PlayAction(getEditor(), this);
		final KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
		am.put(playId, playAct);
		im.put(ks, playId);

		setActionMap(am);
		setInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, im);
	}

	private void setupToolbar() {
		toolbar = new JToolBar();
		toolbar.setFloatable(false);

		// play button
		final PlayAction playAct = new PlayAction(getEditor(), this);
		playButton = new JButton(playAct);
		playButton.setFocusable(false);

		final ResetAction refreshAct = new ResetAction(getEditor(), this);
		refreshButton = new JButton(refreshAct);
		refreshButton.setFocusable(false);

		final ZoomAction showMoreAct = new ZoomAction(getEditor(), this);
		showMoreButton = new JButton(showMoreAct);
		showMoreButton.setFocusable(false);

		final ZoomAction zoomOutAct = new ZoomAction(getEditor(), this, false);
		zoomOutButton = new JButton(zoomOutAct);
		zoomOutButton.setFocusable(false);

		final SaveAction exportAct = new SaveAction(getEditor(), this);
		exportButton = new JButton(exportAct);
		exportButton.setFocusable(false);

		final GenerateSessionAudioAction generateAct = new GenerateSessionAudioAction(getEditor());
		generateButton = new JButton(generateAct);
		generateButton.setFocusable(false);

		toolbar.add(playButton);
		toolbar.addSeparator();
		toolbar.add(refreshButton);
		toolbar.add(showMoreButton);
		toolbar.add(zoomOutButton);
		toolbar.addSeparator();
		toolbar.add(exportButton);
		toolbar.add(generateButton);

		add(toolbar, BorderLayout.NORTH);
	}

	/* toolbar actions */
	public void play() {
//		wavDisplay.play();
	}

	public void onExport() {
	}

	public void showMore() {

	}

	public void shutdown() {
	}

	public TimeUIModel getTimeModel() {
		return this.timeModel;
	}
	
	public SpeechAnalysisWaveformTier getWaveformTier() {
		return this.waveformTier;
	}

	private final static long CLIP_EXTENSION_MIN = 500L;
	private final static long CLIP_EXTENSION_MAX = 1000L;

	public void update() {
		update(false);
	}
	
	public void update(boolean force) {
		Record utt = getEditor().currentRecord();
		if(utt == null) return;

		SessionMediaModel mediaModel = getEditor().getMediaModel();
		
		try {
			LongSound ls = mediaModel.getSharedSessionAudio();
			if(waveformTier.getWaveformDisplay().getLongSound() != ls) {
				timeModel.setStartTime(0.0f);
				timeModel.setEndTime(ls.length());
				timeModel.setPixelsPerSecond(100.0f);
				waveformTier.getWaveformDisplay().setLongSound(ls);
			}
		} catch (IOException e) {
			LogUtil.severe(e);
		}
		
		if(currentRecordInterval != null) {
			timeModel.removeInterval(currentRecordInterval);
			currentRecordInterval = null;
		}
		
		if(selectionInterval != null) {
			timeModel.removeInterval(selectionInterval);
			selectionInterval = null;
		}

		if(utt != null && waveformTier.getWaveformDisplay().getLongSound() != null) {
			final MediaSegment segment = utt.getSegment().getGroup(0);

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
			
			if(segLength == 0.0f) {
				// using a default display length of 3.0 seconds (or length of sampled)
				displayLength = Math.min(3.0f, waveformTier.getWaveformDisplay().getLongSound().length());
			} else if(segLength > 60.0f && !force) {
				messageButton.setTopLabelText("<html><b>Segment not loaded</b></html>");
				messageButton.setBottomLabelText("Segment length exceeds 60 seconds, click this message to force loading.");
				messageButton.clearActions();
				
				final PhonUIAction forceUpdateAct = new PhonUIAction(this, "update", true);
				messageButton.setDefaultAction(forceUpdateAct);
				
				waveformTier.getWaveformDisplay().setVisible(false);
				messageButton.setVisible(true);
			} else {
				if((displayStart + displayLength) > waveformTier.getWaveformDisplay().getLongSound().length()) {
					displayStart = waveformTier.getWaveformDisplay().getLongSound().length() - displayLength;
	
					if(displayStart < 0.0f) {
						displayStart = 0.0f;
						displayLength = waveformTier.getWaveformDisplay().getLongSound().length();
					}
				}

				int displayWidth = getVisibleRect().width;
				float pxPerS = displayWidth / displayLength;
				
				timeModel.setPixelsPerSecond(pxPerS);
				currentRecordInterval = timeModel.addInterval(segStart, segStart+segLength);
				scrollToTime(displayStart);
				
				waveformTier.getWaveformDisplay().setVisible(true);
				messageButton.setVisible(false);
			}
		} else {
			messageButton.setTopLabelText("<html><b>Unable to open audio file</b></html>");
			messageButton.clearActions();

			// display option to generate audio file if there is session media available
			final Session session = getEditor().getSession();

			final File mediaFile = MediaLocator.findMediaFile(getEditor().getProject(), session);
			if(mediaFile != null && mediaFile.exists()) {
				// show generate audio message
				final GenerateSessionAudioAction generateAct = new GenerateSessionAudioAction(getEditor());
				generateAct.putValue(PhonUIAction.LARGE_ICON_KEY, generateAct.getValue(PhonUIAction.SMALL_ICON));

				messageButton.setTopLabelText("<html><b>No wav file available for Speech Analysis view</b></html>");
				messageButton.setBottomLabelText("<html>Click here to extract wav from Session media.</html>");

				messageButton.setDefaultAction(generateAct);
				messageButton.addAction(generateAct);
			} else {
				// no media, tell user to setup media in Session Information
				final PhonUIAction showSessionInformationAct = new PhonUIAction(getEditor().getViewModel(),
						"showView", SessionInfoEditorView.VIEW_TITLE);
				showSessionInformationAct.putValue(PhonUIAction.NAME, "Show Session Information view");
				showSessionInformationAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show Session Information view");
				showSessionInformationAct.putValue(PhonUIAction.LARGE_ICON_KEY, IconManager.getInstance().getIcon("apps/system-users", IconSize.SMALL));

				messageButton.setDefaultAction(showSessionInformationAct);
				messageButton.addAction(showSessionInformationAct);

				messageButton.setTopLabelText("<html><b>Media file not found</b></html>");
				messageButton.setBottomLabelText("<html>Click here to set media in Session Information.</html>");
			}
			messageButton.setVisible(true);
		}

		setupTimeScrollbar();
		revalidate();
		
		waveformTier.getWaveformDisplay().repaint();
	}
	
	public void scrollToTime(float time) {
		var x = getTimeModel().xForTime(time);
		var rect = tierPane.getVisibleRect();
		rect.x = (int)x;
		tierPane.scrollRectToVisible(rect);
	}
	
	/** Editor events */
	private int lastRecord = -1;

	@RunOnEDT
	public void onSessionChanged(EditorEvent ee) {
		update(true);
		lastRecord = getEditor().getCurrentRecordIndex();
	}

	@RunOnEDT
	public void onRecordChanged(EditorEvent ee) {
		if(!isVisible() || !getEditor().getViewModel().isShowing(VIEW_TITLE)
				|| !getEditor().getViewModel().isShowingInStack(VIEW_TITLE)) return;

		updateAndChangedRecord();
	}

	private void updateAndChangedRecord() {
		if(lastRecord != getEditor().getCurrentRecordIndex()) {
			update();
		}
		lastRecord = getEditor().getCurrentRecordIndex();
	}
	
	@RunOnEDT
	public void onSessionMediaChanged(EditorEvent ee) {
		if(!isVisible() || !getEditor().getViewModel().isShowing(VIEW_TITLE)) return;
//		wavDisplay.setSampled(null);
		(new ResetAction(getEditor(), this)).actionPerformed(new ActionEvent(ee.getSource(), -1, ee.getEventName()));
	}
	
	@RunOnEDT
	public void onSessionAudioAvailable(EditorEvent ee) {
		if(!isVisible() || !getEditor().getViewModel().isShowing(VIEW_TITLE)) return;
//		wavDisplay.setSampled(null);
		(new ResetAction(getEditor(), this)).actionPerformed(new ActionEvent(ee.getSource(), -1, ee.getEventName()));
	}

	@RunOnEDT
	public void onMediaSegmentChanged(EditorEvent ee) {
		if(ee.getEventData() != null && ee.getEventData().toString().equals(SystemTierType.Segment.getName()))
			update();
	}

	@RunOnEDT
	public void onRecordRefresh(EditorEvent ee) {
		if(!isVisible() || !getEditor().getViewModel().isShowing(VIEW_TITLE)) return;
		update();
	}

	@Override
	public ImageIcon getIcon() {
		return IconManager.getInstance().getIcon("misc/oscilloscope", IconSize.SMALL);
	}

	@Override
	public JMenu getMenu() {
		final JMenu retVal = new JMenu();

//		if(getWavDisplay().isPlaying()) {
//			retVal.add(new StopAction(getEditor(), this));
//		} else {
//			retVal.add(new PlayAction(getEditor(), this));
//			final JCheckBoxMenuItem loopItem = new JCheckBoxMenuItem(new ToggleLoop(getWavDisplay()));
//			retVal.add(loopItem);
//
//			// output device selection
//			final JMenu mixerMenu = new JMenu("Output Device");
//			final Info[] mixers = AudioSystem.getMixerInfo();
//			for(Info mixerInfo:mixers) {
//				// if we have no source lines, we can't use this device
//				if(AudioSystem.getMixer(mixerInfo).getSourceLineInfo().length == 0) continue;
//				final SelectMixerAction mixerAct = new SelectMixerAction(getWavDisplay(), mixerInfo);
//				mixerAct.putValue(SelectMixerAction.SELECTED_KEY,
//						getWavDisplay().getMixerInfo() == mixerInfo);
//				mixerMenu.add(new JCheckBoxMenuItem(mixerAct));
//			}
//			retVal.add(mixerMenu);
//		}
//		retVal.addSeparator();
		retVal.add(new ResetAction(getEditor(), this));
		retVal.add(new ZoomAction(getEditor(), this));
		retVal.add(new ZoomAction(getEditor(), this, false));
		retVal.addSeparator();
		retVal.add(new SaveAction(getEditor(), this));
		retVal.add(new GenerateSessionAudioAction(getEditor()));

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
	
	private final EditorViewAdapter editorViewListener = new EditorViewAdapter() {

		@Override
		public void onFocused(EditorView view) {
			updateAndChangedRecord();
		}
		
	};

//	private final PropertyChangeListener segmentListener = new PropertyChangeListener() {
//
//		@Override
//		public void propertyChange(PropertyChangeEvent evt) {
//			// don't update while adjusting values
//			if(getWavDisplay().isValuesAdjusting()) return;
//
//			final float segmentStart = getWavDisplay().getSegmentStart();
//			final float segmentEnd = getWavDisplay().getSegmentStart() + getWavDisplay().getSegmentLength();
//
//			final long newSegStart = Math.round(segmentStart * 1000.0f);
//			final long newSegEnd = Math.round(segmentEnd * 1000.0f);
//
//			final SessionFactory factory = SessionFactory.newFactory();
//			final MediaSegment newSegment = factory.createMediaSegment();
//			newSegment.setStartValue(newSegStart);
//			newSegment.setEndValue(newSegEnd);
//			newSegment.setUnitType(MediaUnit.Millisecond);
//
//			final SessionEditor editor = getEditor();
//			final Record record = editor.currentRecord();
//			final Tier<MediaSegment> segmentTier = record.getSegment();
//
//			final TierEdit<MediaSegment> segmentEdit =
//					new TierEdit<MediaSegment>(editor, segmentTier, 0, newSegment);
//			segmentEdit.setFireHardChangeOnUndo(true);
//			editor.getUndoSupport().postEdit(segmentEdit);
//		}
//
//	};

	private final MouseInputAdapter contenxtMenuHandler = new MouseInputAdapter() {

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

	private JMenu createContextMenu() {
		final JMenu menu = new JMenu();
//
//		final SelectSegmentAction selectSegAct = new SelectSegmentAction(wavDisplay);
//		selectSegAct.putValue(Action.NAME, "Set segment for current record");
//		selectSegAct.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
//		menu.add(new JMenuItem(selectSegAct));
//
//		if(wavDisplay.hasSelection()) {
//			final NewRecordAction newRecordAct = new NewRecordAction(getEditor(), this);
//			menu.add(newRecordAct);
//		}
//
//		menu.addSeparator();
//
//		wavDisplay.getUI().addContextMenuItems(menu);

		for(SpeechAnalysisTier tier:getPluginTiers()) {
			tier.addMenuItems(menu, true);
		}

		return menu;
	}

	private void showContextMenu(MouseEvent e) {
		final JMenu menu = createContextMenu();

		menu.getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
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
