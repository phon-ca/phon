/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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
package ca.phon.app.session.editor.view.speech_analysis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
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
import java.util.logging.Level;
import java.util.logging.Logger;

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
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.swingx.VerticalLayout;

import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.DockPosition;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.session_information.SessionInfoEditorView;
import ca.phon.app.session.editor.view.speech_analysis.actions.GenerateAction;
import ca.phon.app.session.editor.view.speech_analysis.actions.PlayAction;
import ca.phon.app.session.editor.view.speech_analysis.actions.ResetAction;
import ca.phon.app.session.editor.view.speech_analysis.actions.SaveAction;
import ca.phon.app.session.editor.view.speech_analysis.actions.StopAction;
import ca.phon.app.session.editor.view.speech_analysis.actions.ZoomAction;
import ca.phon.media.export.VLCWavExporter;
import ca.phon.media.sampled.PCMSampled;
import ca.phon.media.sampled.PCMSegmentView;
import ca.phon.media.sampled.Sampled;
import ca.phon.media.sampled.actions.SelectMixerAction;
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

	private static final Logger LOGGER = Logger
			.getLogger(SpeechAnalysisEditorView.class.getName());

	public static final String VIEW_TITLE = "Speech Analysis";
	
	public static final int MAX_TIER_HEIGHT = Integer.MAX_VALUE;

	private JPanel contentPane;
	
	/**
	 * Wav display
	 */
	private PCMSegmentView wavDisplay;
	
	/* Toolbar and buttons */
	private JToolBar toolbar;
	
	private JButton playButton;
	private JButton refreshButton;
	
	private JButton exportButton;
	
	private JButton showMoreButton;
	private JButton zoomOutButton;
	
	private JButton generateButton;
	
	private JPanel btmPanel;
	
	private HidablePanel messageButton = new HidablePanel("SpeechAnalysisView.noAudio");
	
	private final static String WAV_DISPLAY_HEIGHT = "SpeechAnalysisView.wavDisplayHeight";
	private final int DEFAULT_WAV_DISPLAY_HEIGHT = 100;
	private JComponent sizer;
	private int wavDisplayHeight = 
			PrefHelper.getInt(WAV_DISPLAY_HEIGHT, DEFAULT_WAV_DISPLAY_HEIGHT);
	
	private JScrollBar horizontalScroller;
	private volatile boolean isDragging = false;
	
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
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}
	
	public List<SpeechAnalysisTier> getPluginTiers() {
		return Collections.unmodifiableList(this.pluginTiers);
	}
	
	private void init() {
		setLayout(new BorderLayout());
		setupToolbar();
		setupInputMap();
		
		horizontalScroller = new JScrollBar(SwingConstants.HORIZONTAL);
		horizontalScroller.addMouseListener(new MouseInputAdapter() {

			@Override
			public void mousePressed(MouseEvent arg0) {
				isDragging = true;
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				isDragging = false;
			}
			
			
			
		});
		horizontalScroller.addAdjustmentListener( (e) -> {
			if(!e.getValueIsAdjusting() || isDragging) {
				int val = e.getValue();
				float time = val / 1000.0f;
				getWavDisplay().setWindowStart(time);
			}
		});
			
		btmPanel = new JPanel(new VerticalLayout());
		btmPanel.removeAll();
		
		btmPanel.add(messageButton);
		btmPanel.add(horizontalScroller);
		add(btmPanel, BorderLayout.SOUTH);
		
		wavDisplay = new PCMSegmentView();
		wavDisplay.setFocusable(true);
		wavDisplay.setBackground(Color.white);
		wavDisplay.setOpaque(true);
		wavDisplay.setFont(FontPreferences.getMonospaceFont());
		wavDisplay.addPropertyChangeListener(PCMSegmentView.SEGMENT_LENGTH_PROP, segmentListener);
		wavDisplay.addPropertyChangeListener(PCMSegmentView.WINDOW_LENGTH_PROP, (e) -> { setupTimeScrollbar(); });
		wavDisplay.addPropertyChangeListener(PCMSegmentView.PLAYING_PROP, (e) -> {
			if(wavDisplay.isPlaying()) {
				// setup stop button
				playButton.setAction(new StopAction(getEditor(), SpeechAnalysisEditorView.this));
			} else {
				playButton.setAction(new PlayAction(getEditor(), SpeechAnalysisEditorView.this));
			}
		});
		
		Dimension prefSize = wavDisplay.getPreferredSize();
			
		if(wavDisplayHeight < 0) wavDisplayHeight = prefSize.height;
		
		prefSize.height = wavDisplayHeight;
		if(prefSize.height < 0) prefSize.height = 0;
		if(prefSize.height > MAX_TIER_HEIGHT) prefSize.height = MAX_TIER_HEIGHT;
		
		wavDisplay.setPreferredSize(prefSize);
		
		contentPane = new JPanel(new VerticalLayout());
		
		contentPane.add(wavDisplay);
		
		sizer = new SpeechAnalysisDivider();
		sizer.addMouseMotionListener(new MouseAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				Dimension currentSize = wavDisplay.getSize();
				Dimension prefSize = wavDisplay.getPreferredSize();
				
				prefSize.height = currentSize.height + e.getY();
				if(prefSize.height < 0) prefSize.height = 0;
				if(prefSize.height > MAX_TIER_HEIGHT) prefSize.height = MAX_TIER_HEIGHT;
				wavDisplayHeight = prefSize.height;
				
				PrefHelper.getUserPreferences().putInt(WAV_DISPLAY_HEIGHT, wavDisplayHeight);
				
				wavDisplay.setPreferredSize(prefSize);
				contentPane.invalidate();
				contentPane.revalidate();
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
		});
		contentPane.add(sizer);
		
		// add plug-in tiers
		final JPanel tierPanel = initPlugins();
		contentPane.add(tierPanel);
		
		final JScrollPane scroller = new JScrollPane(contentPane);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scroller, BorderLayout.CENTER);

		setupActions();
		setupEditorActions();
		setupTimeScrollbar();
	}
	
	private void setupTimeScrollbar() {
		final Sampled samples = getWavDisplay().getSampled();
		if(samples != null) {
			float length = samples.getLength();
			final int numTicks = 
					(length - getWavDisplay().getWindowLength() <= 0 ? 1 : (int)Math.ceil( (length - getWavDisplay().getWindowLength()) * 1000));
			
			Runnable onEDT = () -> {
				horizontalScroller.setEnabled(true);
				horizontalScroller.setValueIsAdjusting(true);
				horizontalScroller.setMinimum(0);			
				horizontalScroller.setMaximum(numTicks);
				horizontalScroller.setUnitIncrement(100);
				horizontalScroller.setBlockIncrement(1000);
				horizontalScroller.setValue((int)Math.floor(getWavDisplay().getWindowStart() * 1000));
				horizontalScroller.setValueIsAdjusting(false);
			};
			if(SwingUtilities.isEventDispatchThread())
				onEDT.run();
			else
				SwingUtilities.invokeLater(onEDT);
		} else {
			horizontalScroller.setEnabled(false);
		}
	}
	
	private void setupActions() {
		final ActionMap am = getActionMap();
		final InputMap im = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		
		final String playId = "play";
		final Action playAct = new PlayAction(getEditor(), this);
		final KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
		am.put(playId, playAct);
		im.put(ks, playId);
		
		setActionMap(am);
		setInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, im);
	}
	
	private JPanel initPlugins() {
		loadPlugins();
		final JPanel retVal = new JPanel(new VerticalLayout());
		for(SpeechAnalysisTier tier:pluginTiers) {
			final JComponent comp = tier.getTierComponent();
			retVal.add(comp);
		}
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
//		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
//				| InputEvent.SHIFT_MASK);
//		this.registerKeyboardAction(new AbstractAction() {
//
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				wavDisplay.play();
//			}
//			
//		}, ks, JComponent.WHEN_IN_FOCUSED_WINDOW);
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
		
		final GenerateAction generateAct = new GenerateAction(getEditor(), this);
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
		wavDisplay.play();
	}
	
	public void onExport() {
	}
	
	public void showMore() {
		
	}
	
	/**
	 * Generate audio file for wav display.
	 * 
	 * The file will be placed in <proj_root>/__res/media/
	 */
	public void generateAudioFile() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		File movFile = MediaLocator.findMediaFile(
				session.getMediaLocation(), editor.getProject(), session.getCorpus());
		if(movFile == null) {
//			final PathExpander pe = new PathExpander();
			final String expandedPath = session.getMediaLocation();
//					pe.expandPath(session.getMediaLocation());
			movFile = new File(expandedPath);
		}
		if(movFile != null && movFile.exists()) {
			int lastDot = movFile.getName().lastIndexOf(".");
			if(lastDot > 0) {
				String movExt = movFile.getName().substring(lastDot);
				if(movExt.equals(".wav")) {
					// already a wav, do nothing!
					final MessageDialogProperties props = new MessageDialogProperties();
					props.setParentWindow(getEditor());
					props.setTitle("Generate Wav");
					props.setHeader("Failed to generate wav");
					props.setMessage("Source file is already in wav format.");
					props.setRunAsync(false);
					props.setOptions(MessageDialogProperties.okOptions);
					NativeDialogs.showMessageDialog(props);
					return;
				}
				String audioFileName = 
					movFile.getName().substring(0, movFile.getName().lastIndexOf(".")) + 
						".wav";
				File parentFile = movFile.getParentFile();
				File resFile = new File(parentFile, audioFileName);
				
				if(resFile.exists()) {
					// ask to overwrite
					final MessageDialogProperties props = new MessageDialogProperties();
					props.setParentWindow(getEditor());
					props.setTitle("Generate Wav");
					props.setHeader("Overwrite file?");
					props.setMessage("Wav file already exists, overwrite?");
					props.setRunAsync(false);
					props.setOptions(MessageDialogProperties.yesNoOptions);
					int retVal = NativeDialogs.showMessageDialog(props);
					if(retVal != 0) return;
				}
				
				final VLCWavExporter exporter = new VLCWavExporter(movFile, resFile);
				final PhonTaskButton taskBtn = new PhonTaskButton(exporter);
				taskBtn.setTopLabelText("Media export - 0%");
				exporter.addTaskListener(new PhonTaskListener() {
					
					@Override
					public void statusChanged(PhonTask task, TaskStatus oldStatus,
							TaskStatus newStatus) {
						if(newStatus == TaskStatus.FINISHED) {
							File audioFile = getAudioFile();
							if(audioFile != null) {
								try {
									final PCMSampled sampled = new PCMSampled(audioFile);
									wavDisplay.setSampled(sampled);
									(new ResetAction(getEditor(), SpeechAnalysisEditorView.this)).actionPerformed(new ActionEvent(this, -1, "reset"));
								} catch (IOException e) {
									LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
									ToastFactory.makeToast(e.getLocalizedMessage()).start(getToolbar());
								}
							}
						}
						if(newStatus != TaskStatus.RUNNING) {
							btmPanel.remove(taskBtn);
							btmPanel.revalidate();
						}
					}
					
					@Override
					public void propertyChanged(PhonTask task, String property,
							Object oldValue, Object newValue) {
						if(PhonTask.PROGRESS_PROP.equals(property)) {
							taskBtn.setTopLabelText("Media export - " + 
									(int)Math.round(100.0*(float)newValue) + "%");
						}
					}
					
				});
				
				messageButton.setVisible(false);
				
				btmPanel.add(taskBtn, null, 0);
				
				getEditor().getStatusBar().watchTask(exporter);
				PhonWorker.getInstance().invokeLater(exporter);
			}
		}
	}
	
	public void shutdown() {
	}
	
	public PCMSegmentView getWavDisplay() {
		return this.wavDisplay;
	}
	
	/**
	 * Get the location of the audio file.
	 * 
	 */
	public File getAudioFile() {
		File selectedMedia = 
				MediaLocator.findMediaFile(getEditor().getProject(), getEditor().getSession());
		if(selectedMedia == null) return null;
		File audioFile = null;
		
		int lastDot = selectedMedia.getName().lastIndexOf('.');
		String mediaName = selectedMedia.getName();
		if(lastDot >= 0) {
			mediaName = mediaName.substring(0, lastDot);
		}
		if(!selectedMedia.isAbsolute()) selectedMedia = 
			MediaLocator.findMediaFile(getEditor().getSession().getMediaLocation(), getEditor().getProject(), getEditor().getSession().getCorpus());
		
		if(selectedMedia != null) {
			File parentFile = selectedMedia.getParentFile();
			audioFile = new File(parentFile, mediaName + ".wav");
			
			if(!audioFile.exists()) {
				audioFile = null;
			}
		}
		return audioFile;
	}
	
	private final static long CLIP_EXTENSION_MIN = 500L;
	private final static long CLIP_EXTENSION_MAX = 1000L;
	
	public void update() {
		Record utt = getEditor().currentRecord();
		
		wavDisplay.setValuesAdusting(true);
		if(wavDisplay.getSampled() == null) {
			final File audioFile = getAudioFile();
			if(audioFile != null) {
				try {
					final PCMSampled sampled = new PCMSampled(audioFile);
					wavDisplay.setSampled(sampled);
//					msgLabel.setVisible(false);
				} catch (IOException e) {
					ToastFactory.makeToast(e.getLocalizedMessage()).start(getToolbar());
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			} else {
//				msgLabel.setVisible(true);
			}
		}
		
		if(utt != null && wavDisplay.getSampled() != null) {
			final MediaSegment segment = utt.getSegment().getGroup(0);
			
			double length = (segment.getEndValue() - segment.getStartValue());
			long preferredClipExtension = (long)Math.ceil(length * 0.4);
			if(preferredClipExtension < CLIP_EXTENSION_MIN)
				preferredClipExtension = CLIP_EXTENSION_MIN;
			if(preferredClipExtension > CLIP_EXTENSION_MAX)
				preferredClipExtension = CLIP_EXTENSION_MAX;
			
			float clipStart =
					(Math.round(segment.getStartValue() - preferredClipExtension)) / 1000.0f;
			float displayStart = 
				Math.max(wavDisplay.getSampled().getStartTime(),
						clipStart);
			float segStart = 
					segment.getStartValue() / 1000.0f;
			float segLength = 
				(segment.getEndValue() - segment.getStartValue()) / 1000.0f;
			float displayLength = 
				segLength + ((2*preferredClipExtension) / 1000.0f);
			if(segLength == 0.0f) {
				// using a default display length of 3.0 seconds (or length of sampled)
				displayLength = Math.min(3.0f, wavDisplay.getSampled().getLength());
			}
			
			if((displayStart + displayLength) > wavDisplay.getSampled().getLength()) {
				displayStart = wavDisplay.getSampled().getLength() - displayLength;
				
				if(displayStart < wavDisplay.getSampled().getStartTime()) {
					displayStart = wavDisplay.getSampled().getStartTime();
					displayLength = wavDisplay.getSampled().getLength();
				}
			}
			
			wavDisplay.setWindowStart(displayStart);
			wavDisplay.setWindowLength(displayLength);
			wavDisplay.setSegmentStart(segStart);
			wavDisplay.setSegmentLength(segLength);
			
			messageButton.setVisible(false);
		} else {
			messageButton.setTopLabelText("<html><b>Unable to open audio file</b></html>");
			messageButton.clearActions();
			
			// display option to generate audio file if there is session media available
			final Session session = getEditor().getSession();
			
			final File mediaFile = MediaLocator.findMediaFile(getEditor().getProject(), session);
			if(mediaFile != null && mediaFile.exists()) {
				// show generate audio message
				final GenerateAction generateAct = new GenerateAction(getEditor(), this);
				generateAct.putValue(GenerateAction.LARGE_ICON_KEY, generateAct.getValue(PhonUIAction.SMALL_ICON));
				
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
		wavDisplay.setValuesAdusting(false);
		wavDisplay.repaint();
	}

	/** Editor events */
	private int lastRecord = -1;
	
	@RunOnEDT
	public void onSessionChanged(EditorEvent ee) {
		update();
	}

	@RunOnEDT
	public void onRecordChanged(EditorEvent ee) {
		if(!isVisible() || !getEditor().getViewModel().isShowing(VIEW_TITLE)) return;
		
		if(lastRecord != getEditor().getCurrentRecordIndex()) {
			update();
			
			wavDisplay.setSelectionStart(0.0f);
			wavDisplay.setSelectionLength(0.0f);
		}
		lastRecord = getEditor().getCurrentRecordIndex();
	}

	@RunOnEDT
	public void onSessionMediaChanged(EditorEvent ee) {
		if(!isVisible() || !getEditor().getViewModel().isShowing(VIEW_TITLE)) return;
		wavDisplay.setSampled(null);
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
		
		if(getWavDisplay().isPlaying()) {
			retVal.add(new StopAction(getEditor(), this));
		} else {
			retVal.add(new PlayAction(getEditor(), this));
			final JCheckBoxMenuItem loopItem = new JCheckBoxMenuItem(new ToggleLoop(getWavDisplay()));
			retVal.add(loopItem);
		
			// output device selection
			final JMenu mixerMenu = new JMenu("Output Device");
			final Info[] mixers = AudioSystem.getMixerInfo();
			for(Info mixerInfo:mixers) {
				// if we have no source lines, we can't use this device
				if(AudioSystem.getMixer(mixerInfo).getSourceLineInfo().length == 0) continue;
				final SelectMixerAction mixerAct = new SelectMixerAction(getWavDisplay(), mixerInfo);
				mixerAct.putValue(SelectMixerAction.SELECTED_KEY,
						getWavDisplay().getMixerInfo() == mixerInfo);
				mixerMenu.add(new JCheckBoxMenuItem(mixerAct));
			}
			retVal.add(mixerMenu);
		}
		retVal.addSeparator();
		retVal.add(new ResetAction(getEditor(), this));
		retVal.add(new ZoomAction(getEditor(), this));
		retVal.add(new ZoomAction(getEditor(), this, false));
		retVal.addSeparator();
		retVal.add(new SaveAction(getEditor(), this));
		retVal.add(new GenerateAction(getEditor(), this));
		
		for(SpeechAnalysisTier tier:pluginTiers) {
			tier.addMenuItems(retVal);
		}
		
		return retVal;	
	}

	@Override
	public DockPosition getPreferredDockPosition() {
		return DockPosition.CENTER;
	}
	
	private final PropertyChangeListener segmentListener = new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			// don't update while adjusting values
			if(getWavDisplay().isValuesAdjusting()) return;
			
			final float segmentStart = getWavDisplay().getSegmentStart();
			final float segmentEnd = getWavDisplay().getSegmentStart() + getWavDisplay().getSegmentLength();
			
			final long newSegStart = Math.round(segmentStart * 1000.0f);
			final long newSegEnd = Math.round(segmentEnd * 1000.0f);
			
			final SessionFactory factory = SessionFactory.newFactory();
			final MediaSegment newSegment = factory.createMediaSegment();
			newSegment.setStartValue(newSegStart);
			newSegment.setEndValue(newSegEnd);
			newSegment.setUnitType(MediaUnit.Millisecond);
			
			final SessionEditor editor = getEditor();
			final Record record = editor.currentRecord();
			final Tier<MediaSegment> segmentTier = record.getSegment();
			
			final TierEdit<MediaSegment> segmentEdit = 
					new TierEdit<MediaSegment>(editor, segmentTier, 0, newSegment);
			segmentEdit.setFireHardChangeOnUndo(true);
			editor.getUndoSupport().postEdit(segmentEdit);
		}
		
	};
	
}
