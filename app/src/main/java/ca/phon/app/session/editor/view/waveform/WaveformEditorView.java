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
package ca.phon.app.session.editor.view.waveform;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.DockPosition;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.waveform.actions.GenerateAction;
import ca.phon.app.session.editor.view.waveform.actions.PlayAction;
import ca.phon.app.session.editor.view.waveform.actions.RefreshAction;
import ca.phon.app.session.editor.view.waveform.actions.SaveAction;
import ca.phon.app.session.editor.view.waveform.actions.ShowMoreAction;
import ca.phon.media.exceptions.PhonMediaException;
import ca.phon.media.exportwizard.MediaExportWizard;
import ca.phon.media.exportwizard.MediaExportWizardProp;
import ca.phon.media.util.MediaLocator;
import ca.phon.media.wavdisplay.WavDisplay;
import ca.phon.media.wavdisplay.WavHelper;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;
import ca.phon.session.MediaSegment;
import ca.phon.session.MediaUnit;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.ui.toast.Toast;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonTask;

/**
 * Displays wavform and associated commands.
 *
 */
public class WaveformEditorView extends EditorView {
	
	private static final long serialVersionUID = -1680881691504590317L;

	private static final Logger LOGGER = Logger
			.getLogger(WaveformEditorView.class.getName());

	public static final String VIEW_TITLE = "Speech Analysis";

	private JPanel contentPane;
	
	/**
	 * Wav display
	 */
	private WavDisplay wavDisplay;
	
	/* Toolbar and buttons */
	private JToolBar toolbar;
	
	private JButton playButton;
	private JButton refreshButton;
	
	private JButton exportButton;
//	private JButton quickExportButton;
	
	private JButton showMoreButton;
	
	private JButton generateButton;
	
	private JLabel msgLabel;
	
	private JComponent sizer;
	
	private final List<WaveformTier> pluginTiers = 
			Collections.synchronizedList(new ArrayList<WaveformTier>());
	
	private class HideStatusComponentTask extends PhonTask {
		
		CommonModuleFrame frame;
		
		public HideStatusComponentTask(CommonModuleFrame cmf) {
			super();
			
			this.frame = cmf;
		}
		
		@Override
		public void performTask() {
			try {
				Thread.sleep(2000);
			} catch (Exception e) {}
			frame.hideStatusComponent();
		}
		
	}
	
	public WaveformEditorView(SessionEditor editor) {
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
		final List<IPluginExtensionPoint<WaveformTier>> extraTiers = 
				pluginManager.getExtensionPoints(WaveformTier.class);
		for(IPluginExtensionPoint<WaveformTier> extraTier:extraTiers) {
			try {
				// ANYTHING can happen during plug-in object creation, 
				// try to catch exceptions which the plug-in lets through
				pluginTiers.add(extraTier.getFactory().createObject(this));
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}
	
	public List<WaveformTier> getPluginTiers() {
		return Collections.unmodifiableList(this.pluginTiers);
	}
	
	private void init() {
		setLayout(new BorderLayout());
		setupToolbar();
		setupInputMap();
		
		msgLabel = new JLabel();
		msgLabel.setBackground(Color.yellow);
		msgLabel.setVisible(false);
		msgLabel.setOpaque(true);
		msgLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		msgLabel.addMouseListener(new MouseInputAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(msgLabel.getText().startsWith("Audio file not found")) {
					msgLabel.setVisible(false);
					generateAudioFile();
				}
			}
			
		});
		add(msgLabel, BorderLayout.SOUTH);
		wavDisplay = new WavDisplay();
		
		wavDisplay.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getPropertyName().equals(WavDisplay._SEGMENT_VALUE_PROP_)) {
					long newSegStart =
						wavDisplay.get_timeBar().getSegStart();
					long newSegEnd = 
						newSegStart + wavDisplay.get_timeBar().getSegLength();
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
			}
			
		});
		
		contentPane = new JPanel(new VerticalLayout());
		
		contentPane.add(wavDisplay);
		
		sizer = new JSeparator(SwingConstants.HORIZONTAL);
		
		sizer.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
		sizer.setPreferredSize(new Dimension(0, 5));
		sizer.addMouseMotionListener(new MouseAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				final Dimension currentSize = wavDisplay.getSize();
				final Dimension prefSize = wavDisplay.getPreferredSize();
				prefSize.height = currentSize.height + e.getY();
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
		
		add(new JScrollPane(contentPane), BorderLayout.CENTER);

		setupActions();
		setupEditorActions();
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
		for(WaveformTier tier:pluginTiers) {
			final JComponent comp = tier.getTierComponent();
			retVal.add(comp);
		}
		return retVal;
	}
	
	public WaveformViewCalculator getCalculator() {
		return new WaveformEditorViewCalculator(wavDisplay.get_timeBar());
	}
	
	@Override
	public String getName() {
		return VIEW_TITLE;
	}
	
	public JToolBar getToolbar() {
		return this.toolbar;
	}
	
	private void setupInputMap() {
		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
				| InputEvent.SHIFT_MASK);
		this.registerKeyboardAction(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					wavDisplay.play();
				} catch (PhonMediaException e1) {
					CommonModuleFrame currentFrame = CommonModuleFrame.getCurrentFrame();
					currentFrame.showErrorMessage(e1.getMessage());
				}
			}
			
		}, ks, JComponent.WHEN_IN_FOCUSED_WINDOW);
	}
	
	private void setupToolbar() {
		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		
		// play button
		final PlayAction playAct = new PlayAction(getEditor(), this);
		playButton = new JButton(playAct);
		playButton.setFocusable(false);
		
		final RefreshAction refreshAct = new RefreshAction(getEditor(), this);
		refreshButton = new JButton(refreshAct);
		refreshButton.setFocusable(false);
		
		final SaveAction exportAct = new SaveAction(getEditor(), this);
		exportButton = new JButton(exportAct);
		exportButton.setFocusable(false);
		
		final ShowMoreAction showMoreAct = new ShowMoreAction(getEditor(), this);
		showMoreButton = new JButton(showMoreAct);
		
		final GenerateAction generateAct = new GenerateAction(getEditor(), this);
		generateButton = new JButton(generateAct);
		
		toolbar.add(playButton);
		toolbar.addSeparator();
		toolbar.add(refreshButton);
		toolbar.add(showMoreButton);
		toolbar.addSeparator();
		toolbar.add(exportButton);
		toolbar.add(generateButton);
		
		add(toolbar, BorderLayout.NORTH);
	}
	
	/* toolbar actions */
	public void play() {
		try {
			wavDisplay.play();
		} catch (PhonMediaException e) {
			final Toast toast = ToastFactory.makeToast(e.getLocalizedMessage());
			toast.start(playButton);
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
	public void onExport() {
//		getModel().queueBackgroundTask(new GenerateWaveSampleTask());
		// get audio segment/selection
		final WavHelper audioInfo = wavDisplay.getSegmentInfo();
		if(audioInfo == null) return;
		
		// setup __res/media/segments if it does not exist
		final File projFile = new File(getEditor().getProject().getLocation());
		final File resFile = new File(projFile, "__res");
		final File mediaResFile = new File(resFile, "media");
		final File segmentFile = new File(mediaResFile, "segments");
		if(!segmentFile.exists()) {
			segmentFile.mkdirs();
		}
		
		// setup segment path
		if(getEditor().currentRecord() == null) return;
		
		File selectedFile = new File(segmentFile, getEditor().getSession().getName() + "_" + getEditor().getSession().getCorpus() + "_" + (getEditor().getCurrentRecordIndex()+1) + ".wav");
		final String segPath = selectedFile.getName();
		
		// show save as.. dialog
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setRunAsync(false);
		props.setCanCreateDirectories(true);
		props.setFileFilter(FileFilter.wavFilter);
		props.setInitialFolder(segmentFile.getAbsolutePath());
		props.setInitialFile(segPath);
		props.setTitle("Save audio segment");
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		
		final String saveAs = NativeDialogs.showSaveDialog(props);
		
		if(saveAs != null) {
			try {
				audioInfo.saveToFile(saveAs);
			} catch (IOException e) {
				final Toast toast = ToastFactory.makeToast(e.getLocalizedMessage());
				toast.start(exportButton);
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}
	
	public void showMore() {
		long curStart = wavDisplay.get_timeBar().getStartMs();
		long curEnd = wavDisplay.get_timeBar().getEndMs();
		
		long newStart = Math.max(0, curStart-1000);
		long newEnd =  curEnd + 1000;
		
		wavDisplay.load(newStart, (newEnd-newStart));
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
				
				// show ffmpeg export wizard
				HashMap<MediaExportWizardProp, Object>
						wizardProps = new HashMap<MediaExportWizardProp, Object>();
				wizardProps.put(MediaExportWizardProp.INPUT_FILE, movFile.getAbsolutePath());
				wizardProps.put(MediaExportWizardProp.OUTPUT_FILE, resFile.getAbsolutePath());
				wizardProps.put(MediaExportWizardProp.ALLOW_PARTIAL_EXTRACT, Boolean.FALSE);
				wizardProps.put(MediaExportWizardProp.ENCODE_VIDEO, Boolean.FALSE);
				wizardProps.put(MediaExportWizardProp.AUDIO_CODEC, "wav");
				wizardProps.put(MediaExportWizardProp.OTHER_ARGS, "-async 1");

				MediaExportWizard wizard = new MediaExportWizard(wizardProps);
				wizard.setSize(500, 550);
				wizard.setLocationByPlatform(true);
				wizard.setVisible(true);

				wizard.addWindowListener(new WindowAdapter() {

					@Override
					public void windowDeactivated(WindowEvent we) {
						File audioFile = getAudioFile();
						if(audioFile != null) {
							wavDisplay.loadFile(audioFile);
							update();
						} else {
							msgLabel.setVisible(true);
						}
					}

				});
			}
		}
	}
	
	public void shutdown() {
		wavDisplay.shutdown();
	}
	
	public WavDisplay getWavDisplay() {
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
	
	private final static long CLIP_EXTENSION_MIN = 100L;
	private final static long CLIP_EXTENSION_MAX = 500L;
	
	public void update() {
		Record utt = getEditor().currentRecord();
		if(utt != null) {

			File audioFile = getAudioFile();
			
			if(audioFile != null && audioFile.exists()) {
				if(!audioFile.equals(wavDisplay.getFile())) {
					wavDisplay.loadFile(audioFile);
				}
				
				final MediaSegment segment = utt.getSegment().getGroup(0);
				
				double length = (segment.getEndValue() - segment.getStartValue());
				long preferredClipExtension = (long)Math.ceil(length * 0.1);
				if(preferredClipExtension < CLIP_EXTENSION_MIN)
					preferredClipExtension = CLIP_EXTENSION_MIN;
				if(preferredClipExtension > CLIP_EXTENSION_MAX)
					preferredClipExtension = CLIP_EXTENSION_MAX;
				
				long clipStart = Math.round(segment.getStartValue() - preferredClipExtension);
				
				long displayStart = 
					Math.max(0,
							clipStart);
				long segLength = 
					Math.round(segment.getEndValue() - segment.getStartValue());
				long displayLength = 
					segLength + (2*preferredClipExtension);
				
				wavDisplay.get_timeBar().setSegStart((int)segment.getStartValue());
				wavDisplay.get_timeBar().setSegLength(segLength);
				wavDisplay.get_timeBar().setStartMs(displayStart);
				wavDisplay.get_timeBar().setEndMs(displayStart+displayLength);
				wavDisplay.load(displayStart, displayLength);
				
				if(msgLabel.isVisible()) {
					msgLabel.setVisible(false);
				}
			} else {
				wavDisplay.clear();
				msgLabel.setVisible(true);
				msgLabel.setText("Audio file not found.  Click here to create.");
			}
		} else {
			wavDisplay.clear();
		}
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
			
			wavDisplay.set_selectionStart(-1);
			wavDisplay.set_selectionEnd(-1);
		}
		lastRecord = getEditor().getCurrentRecordIndex();
	}

	@RunOnEDT
	public void onSessionMediaChanged(EditorEvent ee) {
		if(!isVisible() || !getEditor().getViewModel().isShowing(VIEW_TITLE)) return;
		
		update();
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
	
	/**
	 * Task for exporting selection to a wav file.
	 * 
	 */
	private class GenerateWaveSampleTask extends PhonTask {

		@Override
		public void performTask() {
			
			WavHelper audioInfo = wavDisplay.getSegmentInfo();
			if(audioInfo == null) return;
			
			final CommonModuleFrame currentFrame = CommonModuleFrame.getCurrentFrame();
			currentFrame.showStatusMessage("Saving audio segment...");
			
			if(getEditor().currentRecord() == null) return;
			
			File projFile = new File(getEditor().getProject().getLocation());
			File resFile = new File(projFile, "__res");
			File mediaResFile = new File(resFile, "media");
			File segmentFile = new File(mediaResFile, "segments");
			if(!segmentFile.exists()) {
				segmentFile.mkdirs();
			}
			String segmentPath = segmentFile.getAbsolutePath();
			
//			final PathExpander pe = new PathExpander();
			final String mediaLocation = getEditor().getSession().getMediaLocation();
//					pe.expandPath(getEditor().getSession().getMediaLocation());
			
			File mediaFile = 
				MediaLocator.findMediaFile(mediaLocation, getEditor().getProject(), getEditor().getSession().getCorpus());
			if(mediaFile == null)
				mediaFile = new File(getEditor().getSession().getMediaLocation());
			if(mediaFile != null && mediaFile.exists()) {
				final MediaSegment m = getEditor().currentRecord().getSegment().getGroup(0);
				long segStart = Math.round(m.getStartValue());
				long segLen = Math.round(m.getEndValue() - m.getStartValue());
				
				if(segLen > 0) {
					FileFilter[] filters = new FileFilter[1];
					filters[0] = FileFilter.wavFilter;
					String selectedFile = 
						NativeDialogs.showSaveFileDialogBlocking(CommonModuleFrame.getCurrentFrame(), 
								segmentPath, ".wav", filters, "Save segment");
					if(selectedFile != null) {
						try {
							audioInfo.saveToFile(selectedFile);
							
//							currentFrame.showStatusMessage("Saved segment as '" + 
//									selectedFile + "'");
//							getModel().queueBackgroundTask(new HideStatusComponentTask(currentFrame));
						} catch (IOException e) {
//							currentFrame.showErrorMessage("Could not export audio segment.");
						}
					}  else {
						currentFrame.hideStatusComponent(); 
					}
				} // if segLen
			} // if mediafile
		}
	}
	
	private class AutoGenerateWaveSampleTask extends PhonTask {

		@Override
		public void performTask() {
			
			WavHelper audioInfo = wavDisplay.getSegmentInfo();
			if(audioInfo == null) return;
			
			CommonModuleFrame currentFrame = CommonModuleFrame.getCurrentFrame();
			currentFrame.showStatusMessage("Saving audio segment...");
			
			File projFile = new File(getEditor().getProject().getLocation());
			File resFile = new File(projFile, "__res");
			File mediaResFile = new File(resFile, "media");
			File segmentFile = new File(mediaResFile, "segments");
			if(!segmentFile.exists()) {
				segmentFile.mkdirs();
			}
			String segmentPath = segmentFile.getAbsolutePath();
			
			if(getEditor().currentRecord() == null) return;
			
//			final PathExpander pe = new PathExpander();
			final String mediaLocation = getEditor().getSession().getMediaLocation();
			
			File mediaFile = 
				MediaLocator.findMediaFile(mediaLocation, getEditor().getProject(), getEditor().getSession().getCorpus());
			if(mediaFile == null)
				mediaFile = new File(getEditor().getSession().getMediaLocation());
			if(mediaFile != null && mediaFile.exists()) {
				final MediaSegment m = getEditor().currentRecord().getSegment().getGroup(0);
				long segStart = Math.round(m.getStartValue());
				long segLen = Math.round((long)m.getEndValue() - segStart);
				
				if(segLen > 0) {
					FileFilter[] filters = new FileFilter[1];
					filters[0] = FileFilter.wavFilter;
					
					File selectedFile = new File(segmentFile, getEditor().getSession().getName() + "_" + getEditor().getSession().getCorpus() + "_" + (getEditor().getCurrentRecordIndex()+1) + ".wav");
				
					int fIdx = 0;
					while(selectedFile.exists()) {
						selectedFile = new File(segmentFile,
								getEditor().getSession().getName() + "_" + getEditor().getSession().getCorpus() + "_" + (getEditor().getCurrentRecordIndex()+1) + 
								"(" + (++fIdx) + ").wav");
					}
					String segPath = selectedFile.getAbsolutePath();
					try {
						audioInfo.saveToFile(selectedFile);
//						currentFrame.showStatusMessage("Saved segment as '" + 
//								segPath + "'");
//						getModel().queueBackgroundTask(new HideStatusComponentTask(currentFrame));
					} catch (IOException e) {
//						currentFrame.showErrorMessage("Could not export audio segment.");
					}
				} // if segLen
			} // if mediafile
		}
	}

	@Override
	public ImageIcon getIcon() {
		return IconManager.getInstance().getIcon("misc/oscilloscope", IconSize.SMALL);
	}

	@Override
	public JMenu getMenu() {
		final JMenu retVal = new JMenu();
		
		retVal.add(new PlayAction(getEditor(), this));
		retVal.addSeparator();
		retVal.add(new RefreshAction(getEditor(), this));
		retVal.add(new ShowMoreAction(getEditor(), this));
		retVal.addSeparator();
		retVal.add(new SaveAction(getEditor(), this));
		retVal.add(new GenerateAction(getEditor(), this));
		
		for(WaveformTier tier:pluginTiers) {
			tier.addMenuItems(retVal);
		}
		
		return retVal;	
	}

	@Override
	public DockPosition getPreferredDockPosition() {
		return DockPosition.CENTER;
	}
	
	
}
