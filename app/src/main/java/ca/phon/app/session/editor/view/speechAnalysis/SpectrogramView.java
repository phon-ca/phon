/*
 * Copyright (C) 2012-2018 Gregory Hedlund
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
package ca.phon.app.session.editor.view.speechAnalysis;

import ca.hedlund.jpraat.binding.fon.*;
import ca.hedlund.jpraat.binding.stat.Table;
import ca.hedlund.jpraat.binding.sys.Interpreter;
import ca.hedlund.jpraat.binding.sys.MelderFile;
import ca.hedlund.jpraat.exceptions.PraatException;
import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.BufferWindow;
import ca.phon.app.log.LogBuffer;
import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.view.speechAnalysis.painters.FormantPainter;
import ca.phon.app.session.editor.view.speechAnalysis.painters.IntensityPainter;
import ca.phon.app.session.editor.view.speechAnalysis.painters.PitchSpecklePainter;
import ca.phon.app.session.editor.view.speechAnalysis.painters.SpectrogramPainter;
import ca.phon.media.TimeComponent;
import ca.phon.media.TimeComponentUI;
import ca.phon.media.TimeUIModel;
import ca.phon.media.TimeUIModel.Interval;
import ca.phon.media.TimeUIModel.Marker;
import ca.phon.project.Project;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.FlatButton;
import ca.phon.ui.IconStrip;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTask.TaskStatus;
import ca.phon.worker.PhonTaskListener;
import ca.phon.worker.PhonWorker;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Adds a spectrogram tier to the waveform editor view.
 */
public class SpectrogramView extends SpeechAnalysisTier {

	private SpectrogramPanel spectrogramPanel;

	/**
	 * Max analysis length in seconds.
	 */
	public final static String MAX_ANALYSIS_LENGTH_PROP = SpectrogramView.class.getName() + ".maxAnalysisLength";
	public final static double DEFAULT_MAX_ANALYSIS_LENGTH = 10.0;
	private double maxAnalysisLength = PrefHelper.getDouble(MAX_ANALYSIS_LENGTH_PROP, DEFAULT_MAX_ANALYSIS_LENGTH);

	private final ErrorBanner maxAnalysisMessage = new ErrorBanner();

	/*
	 * Spectrogram
	 */
	public final static String SHOW_SPECTROGRAM_PROP = SpectrogramView.class.getName() + ".showSpectrogram";
	public final static Boolean DEFAULT_SHOW_SPECTROGRAM = Boolean.TRUE;
	private boolean showSpectrogram = PrefHelper.getBoolean(SHOW_SPECTROGRAM_PROP, DEFAULT_SHOW_SPECTROGRAM);

	private SpectrogramSettings spectrogramSettings = new SpectrogramSettings();

	private final SpectrogramPainter spectrogramPainter = new SpectrogramPainter(spectrogramSettings);


	/*
	 * Spectral moments
	 */
	private SpectralMomentsSettings spectralMomentsSettings = new SpectralMomentsSettings();

	private final AtomicReference<Spectrogram> spectrogramRef = new AtomicReference<>();

    private FormantSettings formantSettings = new FormantSettings();

	public final static String SHOW_FORMANTS_PROP = SpectrogramView.class.getName() + ".showFormants";
	private boolean showFormants =
			PrefHelper.getBoolean(SHOW_FORMANTS_PROP, false);

	private final AtomicReference<Formant> formantRef = new AtomicReference<>();

	private FormantPainter formantPainter = new FormantPainter();

	/*
	 * Pitch
	 */
	private final AtomicReference<Pitch> pitchRef = new AtomicReference<>();

	private PitchSettings pitchSettings = new PitchSettings();

	private PitchSpecklePainter pitchPainter = new PitchSpecklePainter();

	public final static String SHOW_PITCH_PROP = SpectrogramView.class.getName() + ".showPitch";
	private boolean showPitch =
			PrefHelper.getBoolean(SHOW_PITCH_PROP, false);

	private final AtomicReference<Intensity> intensityRef = new AtomicReference<>();

	/*
	 * Intensity
	 */
	private IntensitySettings intensitySettings = new IntensitySettings();

	private IntensityPainter intensityPainter = new IntensityPainter();

	public final static String SHOW_INTENSITY_PROP = SpectrogramView.class.getName() + ".showIntensity";
	private boolean showIntensity =
			PrefHelper.getBoolean(SHOW_INTENSITY_PROP, false);

	/*
	 * UI
	 */
	private transient Point currentPoint = null;

	private final static String DISPLAY_HEIGHT = "SpectrogramView.displayHeight";
	private JComponent sizer;
	private int displayHeight = PrefHelper.getInt(DISPLAY_HEIGHT, -1);

	private transient volatile double lastStartTime = 0.0;
	private transient volatile double lastEndTime = 0.0;
	
	private boolean forceLoadSpectrogram = false;

	public SpectrogramView(SpeechAnalysisEditorView p) {
		super(p);
		setVisible(showSpectrogram);

		init();

		setupEditorEvents();
		setupToolbar();

		installKeyStrokes(p);
	}

	private void init() {
		spectrogramPanel = new SpectrogramPanel();
		spectrogramPanel.setBackground(Color.white);
		spectrogramPanel.setOpaque(true);
		if(displayHeight < 0) {
			displayHeight = (int)Math.ceil(spectrogramSettings.getMaxFrequency()  / 40);
		}
		Dimension prefSize = spectrogramPanel.getPreferredSize();
		prefSize.height = displayHeight;
		spectrogramPanel.setPreferredSize(prefSize);
		
		setLayout(new BorderLayout());
		add(spectrogramPanel, BorderLayout.CENTER);
		
		SpeechAnalysisTierDivider divider = new SpeechAnalysisTierDivider(spectrogramPanel);
		add(divider, BorderLayout.SOUTH);
		
		spectrogramPanel.addMouseListener(getParentView().getContextMenuAdapter());
		spectrogramPanel.addMouseListener(getParentView().getCursorAndSelectionAdapter());
		spectrogramPanel.addMouseMotionListener(getParentView().getCursorAndSelectionAdapter());
		spectrogramPanel.addMouseListener(pointListener);

		spectrogramPanel.setDefaultCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

		final PhonUIAction<Void> forceUpdateAct = PhonUIAction.runnable(this::onForceLoadSpectrogram);
		forceUpdateAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Force load spectrogram");

		maxAnalysisMessage.setTopLabelText("<html><b>Spectrogram Not Loaded</b></htmlL>");
		maxAnalysisMessage.setBottomLabelText(
				String.format("<html>Record segment exceeds max analysis length of %.1fs. Click this message to force loading.</html>", maxAnalysisLength));
		maxAnalysisMessage.addAction(forceUpdateAct);
		maxAnalysisMessage.setDefaultAction(forceUpdateAct);
		maxAnalysisMessage.setVisible(false);
		
		getParentView().getCursorMarker().addPropertyChangeListener("time", (e) -> {
			spectrogramPanel.repaint();
		});

		getParentView().getErrorPane().add(maxAnalysisMessage);
	}

	@Override
	public void addNotify() {
		super.addNotify();
		update();
	}

	private void setupEditorEvents() {
		getParentView().getEditor().getEventManager().registerActionForEvent(SpeechAnalysisEditorView.TimeModelUpdated, this::onTimeModelChanged);
		
		getParentView().getEditor().getEventManager().registerActionForEvent(SessionMediaModel.SessionAudioAvailable, this::onSessionAudioAvailable);
		getParentView().getEditor().getEventManager().registerActionForEvent(EditorEventType.SessionMediaChanged, this::onMediaChanged);

		getParentView().getEditor().getEventManager().registerActionForEvent(EditorEventType.TierChange, this::onSegmentChanged);
		
		getParentView().getEditor().getEventManager().registerActionForEvent(EditorEventType.EditorClosing, this::onEditorClosing, EditorEventManager.RunOn.AWTEventDispatchThread);
	}

	private void setupToolbar() {
        JPopupMenu spectrumMenu = new JPopupMenu();
        spectrumMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                spectrumMenu.removeAll();
                addMenuItems(new MenuBuilder(spectrumMenu), true);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {

            }
        });

        final PhonUIAction<Void> phonUIAction = PhonUIAction.eventConsumer((phonActionEvent) -> {
            final JButton source = (JButton) phonActionEvent.getActionEvent().getSource();
            spectrumMenu.show(source, 0, source.getHeight());
        });
        phonUIAction.putValue(PhonUIAction.NAME, "Spectrum");
        phonUIAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show spectrum menu");
        phonUIAction.putValue(FlatButton.ICON_FONT_NAME_PROP, IconManager.GoogleMaterialDesignIconsFontName);
        phonUIAction.putValue(FlatButton.ICON_NAME_PROP, "arrow_drop_down");
        phonUIAction.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
        final JButton spectrumMenuButton = new FlatButton(phonUIAction);
        getParentView().getToolbar().add(spectrumMenuButton, IconStrip.IconStripPosition.LEFT);

//
//		menuAct.putValue(Action.NAME, "Spectrum");
//		menuAct.putValue(Action.SHORT_DESCRIPTION, "Show spectrum menu");
//		menuAct.putValue(Action.SMALL_ICON, IconManager.getInstance().getIcon("praat/spectrogram", IconSize.SMALL));
//		menuAct.putValue(DropDownButton.ARROW_ICON_GAP, 2);
//		menuAct.putValue(DropDownButton.ARROW_ICON_POSITION, SwingConstants.BOTTOM);
//		menuAct.putValue(DropDownButton.BUTTON_POPUP, spectrumMenu);
//
//		DropDownButton menuBtn = new DropDownButton(menuAct);
//		menuBtn.setOnlyPopup(true);
//		menuBtn.getButtonPopup().addPropertyChangeListener(ButtonPopup.POPUP_VISIBLE, (e) -> {
//			if((boolean)e.getNewValue()) {
//				spectrumMenu.removeAll();
//				addMenuItems(new MenuBuilder(spectrumMenu), true);
//			}
//		});
//		getParentView().getToolbar().add(menuBtn, IconStrip.IconStripPosition.LEFT);
	}

	private void installKeyStrokes(SpeechAnalysisEditorView p) {
		final InputMap inputMap = p.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		final ActionMap actionMap = p.getActionMap();

		final String toggleSpectrogramId = "onToggleSpectrogram";
		final PhonUIAction<Void> toggleSpectrogramAct = PhonUIAction.runnable(this::onToggleSpectrogram);
		actionMap.put(toggleSpectrogramId, toggleSpectrogramAct);
		final KeyStroke toggleSpectrogramKs = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.SHIFT_MASK);
		inputMap.put(toggleSpectrogramKs, toggleSpectrogramId);

		final String toggleFormantsId = "onToggleFormants";
		final PhonUIAction<Void> toggleFormantsAct = PhonUIAction.runnable(this::onToggleFormants);
		actionMap.put(toggleFormantsId, toggleFormantsAct);
		final KeyStroke toggleFormantsKs = KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.SHIFT_MASK);
		inputMap.put(toggleFormantsKs, toggleFormantsId);

		final String listDurationId = "listDuration";
		final PhonUIAction<Void> listDurationAct = PhonUIAction.runnable(this::listDuration);
		actionMap.put(listDurationId, listDurationAct);
		final KeyStroke listDurationKs = KeyStroke.getKeyStroke(KeyEvent.VK_D, 0);
		inputMap.put(listDurationKs, listDurationId);

		final String listFormantsId = "listFormants";
		final PhonUIAction<Void> listFormantsAct = PhonUIAction.runnable(this::listFormants);
		actionMap.put(listFormantsId, listFormantsAct);
		final KeyStroke listFormantsKs = KeyStroke.getKeyStroke(KeyEvent.VK_F, 0);
		inputMap.put(listFormantsKs, listFormantsId);

		final String togglePitchId = "onTogglePitch";
		final PhonUIAction<Void> togglePitcAct = PhonUIAction.runnable(this::onTogglePitch);
		actionMap.put(togglePitchId, togglePitcAct);
		final KeyStroke togglePitchKs = KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.SHIFT_MASK);
		inputMap.put(togglePitchKs, togglePitchId);

		final String listPitchId = "listPitch";
		final PhonUIAction<Void> listPitchAct = PhonUIAction.runnable(this::listPitch);
		actionMap.put(listPitchId, listPitchAct);
		final KeyStroke listPitchKs = KeyStroke.getKeyStroke(KeyEvent.VK_P, 0);
		inputMap.put(listPitchKs, listPitchId);

		final String toggleIntensityId = "onToggleIntensity";
		final PhonUIAction<Void> toggleIntensityAct = PhonUIAction.runnable(this::onToggleIntensity);
		actionMap.put(toggleIntensityId, toggleIntensityAct);
		final KeyStroke toggleIntensityKs = KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.SHIFT_MASK);
		inputMap.put(toggleIntensityKs, toggleIntensityId);

		final String listIntensityId = "listIntensity";
		final PhonUIAction<Void> listIntensityAct = PhonUIAction.runnable(this::listIntensity);
		actionMap.put(listIntensityId, listIntensityAct);
		final KeyStroke listIntensityKs = KeyStroke.getKeyStroke(KeyEvent.VK_I, 0);
		inputMap.put(listIntensityKs, listIntensityId);

		p.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputMap);
		p.setActionMap(actionMap);
	}

	@Override
	public boolean shouldShow() {
		return PrefHelper.getUserPreferences().getBoolean(SHOW_SPECTROGRAM_PROP, super.shouldShow());
	}
	
	public void onForceLoadSpectrogram() {
		forceLoadSpectrogram = true;
		update(true);
	}
	
	public void onToggleSpectrogram() {
		showSpectrogram = !showSpectrogram;
		PrefHelper.getUserPreferences().putBoolean(SHOW_SPECTROGRAM_PROP, showSpectrogram);
		SpectrogramView.this.setVisible(showSpectrogram);
		if(showSpectrogram) update();
	}

	public void onEditSettings() {
		final AtomicBoolean wasCanceled = new AtomicBoolean(false);
		final PhonTask onEDT = new PhonTask() {

			@Override
			public void performTask() {
				setStatus(TaskStatus.RUNNING);

				final JDialog dialog = new JDialog(CommonModuleFrame.getCurrentFrame());
				dialog.setModal(true);

				dialog.setLayout(new BorderLayout());

				final DialogHeader header = new DialogHeader("Spectrogram settings", "Edit spectrogram settings");
				dialog.add(header, BorderLayout.NORTH);

				final Project project = getParentView().getEditor().getProject();
				final Session session = getParentView().getEditor().getSession();

				SpectrogramSettings spectrogramSettings = SpectrogramView.this.spectrogramSettings;
				final Record currentRecord = getParentView().getEditor().currentRecord();
				if(currentRecord != null && currentRecord.getSpeaker() != Participant.UNKNOWN) {
					spectrogramSettings = SpectrogramSettings.getSettingsForSessionParticipant(
							getParentView().getEditor().getProject(),
							getParentView().getEditor().getSession(),
							currentRecord.getSpeaker()
					);
				}

				final SpectrogramSettingsPanel settingsPanel = new SpectrogramSettingsPanel(project, session, currentRecord != null ? currentRecord.getSpeaker() : Participant.UNKNOWN);
				settingsPanel.loadSettings(spectrogramSettings);
				settingsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				dialog.add(settingsPanel, BorderLayout.CENTER);

				final JButton okButton = new JButton("Ok");
				okButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						wasCanceled.getAndSet(false);
						dialog.setVisible(false);
					}

				});
				final JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						wasCanceled.getAndSet(true);
						dialog.setVisible(false);
					}

				});

				dialog.getRootPane().setDefaultButton(okButton);
				okButton.requestFocusInWindow();

				dialog.add(ButtonBarBuilder.buildOkCancelBar(okButton, cancelButton),
						BorderLayout.SOUTH);

				dialog.pack();
				dialog.setLocationRelativeTo(getParentView());
				dialog.setVisible(true);

				if(!wasCanceled.get()) {
					SpectrogramView.this.spectrogramSettings = settingsPanel.getSettings();
					spectrogramPainter.setRepaintBuffer(true);
					formantPainter.setMaxFrequency(SpectrogramView.this.spectrogramSettings.getMaxFrequency());
					formantPainter.setRepaintBuffer(true);
					spectrogramPainter.setSettings(SpectrogramView.this.spectrogramSettings);
				}

				setStatus(TaskStatus.FINISHED);
			}
		};
		onEDT.addTaskListener(new PhonTaskListener() {

			@Override
			public void statusChanged(PhonTask task, TaskStatus oldStatus,
					TaskStatus newStatus) {
				if(newStatus != TaskStatus.RUNNING) {
					if(!wasCanceled.get()) {
						final PhonWorker worker = PhonWorker.createWorker();
						worker.invokeLater(spectrogramLoader);
						worker.invokeLater( () -> SwingUtilities.invokeLater(updateTask) );
						worker.setFinishWhenQueueEmpty(true);
						worker.start();
					}
				}
			}

			@Override
			public void propertyChanged(PhonTask task, String property,
					Object oldValue, Object newValue) {
			}
		});
		if(SwingUtilities.isEventDispatchThread())
			onEDT.run();
		else
			SwingUtilities.invokeLater(onEDT);
	}

	public void onToggleFormants() {
		showFormants = !showFormants;
		PrefHelper.getUserPreferences().putBoolean(SHOW_FORMANTS_PROP, showFormants);

		final PhonWorker worker = PhonWorker.createWorker();
		worker.setFinishWhenQueueEmpty(true);
		if(showFormants) {
			formantPainter.setMaxFrequency(spectrogramSettings.getMaxFrequency());
			worker.invokeLater(formantLoader);
		}
		worker.invokeLater( () -> SwingUtilities.invokeLater(updateTask) );
		worker.start();
	}

	public void onEditFormantSettings() {
		final JDialog dialog = new JDialog(CommonModuleFrame.getCurrentFrame());
		dialog.setModal(true);

		dialog.setLayout(new BorderLayout());

		final DialogHeader header = new DialogHeader("Formant settings", "Edit formant settings");
		dialog.add(header, BorderLayout.NORTH);

        final Project project = getParentView().getEditor().getProject();
        final Session session = getParentView().getEditor().getSession();

        FormantSettings formantSettings = this.formantSettings;
        final Record currentRecord = getParentView().getEditor().currentRecord();
        if(currentRecord != null && currentRecord.getSpeaker() != Participant.UNKNOWN) {
            formantSettings = FormantSettings.getSettingsForSessionParticipant(
                    getParentView().getEditor().getProject(),
                    getParentView().getEditor().getSession(),
                    currentRecord.getSpeaker()
            );
        }

		final FormantSettingsPanel settingsPanel = new FormantSettingsPanel(project, session, currentRecord != null ? currentRecord.getSpeaker() : Participant.UNKNOWN);
		settingsPanel.loadSettings(formantSettings);
		settingsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		dialog.add(settingsPanel, BorderLayout.CENTER);

		final AtomicBoolean wasCanceled = new AtomicBoolean(false);
		final JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				wasCanceled.getAndSet(false);
				dialog.setVisible(false);
			}

		});
		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				wasCanceled.getAndSet(true);
				dialog.setVisible(false);
			}
		});

		dialog.getRootPane().setDefaultButton(okButton);
		okButton.requestFocusInWindow();

		dialog.add(ButtonBarBuilder.buildOkCancelBar(okButton, cancelButton),
				BorderLayout.SOUTH);

		dialog.pack();
		dialog.setLocationRelativeTo(getParentView());
		dialog.setVisible(true);

		// ... wait, it's modal

		if(!wasCanceled.get()) {
			this.formantSettings = settingsPanel.getSettings();
			formantPainter.setSettings(this.formantSettings);
			formantPainter.setMaxFrequency(spectrogramSettings.getMaxFrequency());
			formantPainter.setRepaintBuffer(true);

			final PhonWorker worker = PhonWorker.createWorker();
			worker.setFinishWhenQueueEmpty(true);
			worker.invokeLater(formantLoader);
			worker.invokeLater( () -> SwingUtilities.invokeLater(updateTask) );
			worker.start();
		}
	}

	public void onTogglePitch() {
		showPitch = !showPitch;
		PrefHelper.getUserPreferences().putBoolean(SHOW_PITCH_PROP, showPitch);

		final PhonWorker worker = PhonWorker.createWorker();
		worker.setFinishWhenQueueEmpty(true);

		if(showPitch) {
			worker.invokeLater(pitchLoader);
		}
		worker.invokeLater( () -> SwingUtilities.invokeLater(updateTask) );
		worker.start();
	}

	public void onEditPitchSettings() {
		final JDialog dialog = new JDialog(CommonModuleFrame.getCurrentFrame());
		dialog.setModal(true);

		dialog.setLayout(new BorderLayout());

		final DialogHeader header = new DialogHeader("Pitch settings", "Edit pitch settings");
		dialog.add(header, BorderLayout.NORTH);

		final Project project = getParentView().getEditor().getProject();
		final Session session = getParentView().getEditor().getSession();

		PitchSettings pitchSettings = this.pitchSettings;
		final Record currentRecord = getParentView().getEditor().currentRecord();
		if(currentRecord != null && currentRecord.getSpeaker() != Participant.UNKNOWN) {
			pitchSettings = PitchSettings.getSettingsForSessionParticipant(
					getParentView().getEditor().getProject(),
					getParentView().getEditor().getSession(),
					currentRecord.getSpeaker()
			);
		}

		final PitchSettingsPanel settingsPanel = new PitchSettingsPanel(project, session, currentRecord != null ? currentRecord.getSpeaker() : Participant.UNKNOWN);
		settingsPanel.loadSettings(pitchSettings);
		settingsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		dialog.add(settingsPanel, BorderLayout.CENTER);

		final AtomicBoolean wasCanceled = new AtomicBoolean(false);
		final JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				wasCanceled.getAndSet(false);
				dialog.setVisible(false);
			}

		});
		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				wasCanceled.getAndSet(true);
				dialog.setVisible(false);
			}
		});

		dialog.getRootPane().setDefaultButton(okButton);
		okButton.requestFocusInWindow();

		dialog.add(ButtonBarBuilder.buildOkCancelBar(okButton, cancelButton),
				BorderLayout.SOUTH);

		dialog.pack();
		dialog.setLocationRelativeTo(getParentView());
		dialog.setVisible(true);

		// ... wait, it's modal

		if(!wasCanceled.get()) {
			this.pitchSettings = settingsPanel.getSettings();
			pitchPainter.setSettings(this.pitchSettings);
			pitchPainter.setRepaintBuffer(true);

			final PhonWorker worker = PhonWorker.createWorker();
			worker.setFinishWhenQueueEmpty(true);
			worker.invokeLater(pitchLoader);
			worker.invokeLater( () -> SwingUtilities.invokeLater(updateTask) );
			worker.start();
		}
	}

	public void onEditSpectralMomentsSettings() {
		final JDialog dialog = new JDialog(CommonModuleFrame.getCurrentFrame());
		dialog.setModal(true);

		dialog.setLayout(new BorderLayout());

		final DialogHeader header = new DialogHeader("Spectral Moments settings", "Edit spectral moments settings");
		dialog.add(header, BorderLayout.NORTH);

		final Project project = getParentView().getEditor().getProject();
		final Session session = getParentView().getEditor().getSession();

		SpectralMomentsSettings spectralMomentsSettings = this.spectralMomentsSettings;
		final Record currentRecord = getParentView().getEditor().currentRecord();
		if(currentRecord != null && currentRecord.getSpeaker() != Participant.UNKNOWN) {
			spectralMomentsSettings = SpectralMomentsSettings.getSettingsForSessionParticipant(
					getParentView().getEditor().getProject(),
					getParentView().getEditor().getSession(),
					currentRecord.getSpeaker()
			);
		}

		final SpectralMomentsSettingsPanel settingsPanel = new SpectralMomentsSettingsPanel(project, session, currentRecord != null ? currentRecord.getSpeaker() : Participant.UNKNOWN);
		settingsPanel.loadSettings(spectralMomentsSettings);
		settingsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		dialog.add(settingsPanel, BorderLayout.CENTER);

		final AtomicBoolean wasCanceled = new AtomicBoolean(false);
		final JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				wasCanceled.getAndSet(false);
				dialog.setVisible(false);
			}

		});
		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				wasCanceled.getAndSet(true);
				dialog.setVisible(false);
			}
		});

		dialog.getRootPane().setDefaultButton(okButton);
		okButton.requestFocusInWindow();

		dialog.add(ButtonBarBuilder.buildOkCancelBar(okButton, cancelButton),
				BorderLayout.SOUTH);

		dialog.pack();
		dialog.setLocationRelativeTo(getParentView());
		dialog.setVisible(true);

		// ... wait, it's modal

		if(!wasCanceled.get()) {
			spectralMomentsSettings = settingsPanel.getSettings();
		}
	}

	public void onToggleIntensity() {
		showIntensity = !showIntensity;
		PrefHelper.getUserPreferences().putBoolean(SHOW_INTENSITY_PROP, showIntensity);

		final PhonWorker worker = PhonWorker.createWorker();
		worker.setFinishWhenQueueEmpty(true);
		if(showIntensity) {
			worker.invokeLater(intensityLoader);
		}
		worker.invokeLater( () -> SwingUtilities.invokeLater(updateTask) );
		worker.start();
	}

	public void onEditIntensitySettings() {
		final JDialog dialog = new JDialog(CommonModuleFrame.getCurrentFrame());
		dialog.setModal(true);

		dialog.setLayout(new BorderLayout());

		final DialogHeader header = new DialogHeader("Intensity settings", "Edit intensity settings");
		dialog.add(header, BorderLayout.NORTH);

		final Project project = getParentView().getEditor().getProject();
		final Session session = getParentView().getEditor().getSession();

		IntensitySettings intensitySettings = this.intensitySettings;
		final Record currentRecord = getParentView().getEditor().currentRecord();
		if(currentRecord != null && currentRecord.getSpeaker() != Participant.UNKNOWN) {
			intensitySettings = IntensitySettings.getSettingsForSessionParticipant(
					getParentView().getEditor().getProject(),
					getParentView().getEditor().getSession(),
					currentRecord.getSpeaker()
			);
		}

		final IntensitySettingsPanel settingsPanel = new IntensitySettingsPanel(project, session, currentRecord != null ? currentRecord.getSpeaker() : Participant.UNKNOWN);
		settingsPanel.loadSettings(intensitySettings);
		settingsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		dialog.add(settingsPanel, BorderLayout.CENTER);

		final AtomicBoolean wasCanceled = new AtomicBoolean(false);
		final JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				wasCanceled.getAndSet(false);
				dialog.setVisible(false);
			}

		});
		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				wasCanceled.getAndSet(true);
				dialog.setVisible(false);
			}
		});

		dialog.getRootPane().setDefaultButton(okButton);
		okButton.requestFocusInWindow();

		dialog.add(ButtonBarBuilder.buildOkCancelBar(okButton, cancelButton),
				BorderLayout.SOUTH);

		dialog.pack();
		dialog.setLocationRelativeTo(getParentView());
		dialog.setVisible(true);

		// ... wait, it's modal

		if(!wasCanceled.get()) {
			this.intensitySettings = settingsPanel.getSettings();
			intensityPainter.setRepaintBuffer(true);

			final PhonWorker worker = PhonWorker.createWorker();
			worker.setFinishWhenQueueEmpty(true);
			worker.invokeLater(intensityLoader);
			worker.invokeLater( () -> SwingUtilities.invokeLater(updateTask) );
			worker.start();
		}
	}

	@SuppressWarnings("resource")
	public void listPitch() {
		final Pitch pitch = (pitchRef.get() != null ? pitchRef.get() : loadPitch());
		if(pitch == null) return;

		Interval interval = getParentView().getSelectionInterval() != null ? getParentView().getSelectionInterval() : getParentView().getCurrentRecordInterval();
		if(interval == null) return;
		
		float startTime = interval.getStartMarker().getTime();
		float endTime = interval.getEndMarker().getTime();
		float length = endTime - startTime;
		if(length <= 0.0f) return;

		final NumberFormat format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(6);

		final BufferWindow bw = BufferWindow.getBufferWindow();
		bw.showWindow();
		final BufferPanel bufferPanel = bw.createBuffer("Pitch (" +
				format.format(startTime) + "-" + format.format(endTime) + ")");
		final LogBuffer buffer = bufferPanel.getLogBuffer();

		final AtomicReference<Long> ixminPtr = new AtomicReference<Long>();
		final AtomicReference<Long> ixmaxPtr = new AtomicReference<Long>();

		pitch.getWindowSamples(startTime, endTime, ixminPtr, ixmaxPtr);

		final int xmin = ixminPtr.get().intValue();
		final int xmax = ixmaxPtr.get().intValue();

		// print header
		try {
			final PrintWriter out =
					new PrintWriter(new OutputStreamWriter(buffer.getStdOutStream(), "UTF-8"));
			out.flush();
			out.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_BUSY);
			out.flush();
			final StringBuilder sb = new StringBuilder();
			final char qc = '\"';
			final char sc = ',';
			sb.append(qc).append("Time(s)").append(qc);
			sb.append(sc).append(qc).append("F0(");
			final String unitText = pitch.getUnitText(Pitch.LEVEL_FREQUENCY,
					pitchSettings.getUnits().ordinal(), Function.UNIT_TEXT_SHORT);
			sb.append(unitText).append(')').append(qc);
			out.println(sb.toString());
			sb.setLength(0);

			for(int i = xmin; i <= xmax; i++) {
				double t = pitch.indexToX(i);
				double f0 = pitch.getValueAtSample(i, Pitch.LEVEL_FREQUENCY, pitchSettings.getUnits().ordinal());
				f0 = pitch.convertToNonlogarithmic(f0, Pitch.LEVEL_FREQUENCY, pitchSettings.getUnits().ordinal());
				sb.append(qc).append(format.format(t)).append(qc);
				sb.append(sc).append(qc).append(format.format(f0)).append(qc);
				out.println(sb.toString());
				sb.setLength(0);
			}

			out.flush();
			out.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.STOP_BUSY);
			out.flush();
			out.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_TABLE_CODE);
			out.flush();
			out.close();
			
		} catch(IOException e) {
			LogUtil.warning(e);
		}
		// delete pitch if necessary
		if(pitch != pitchRef.get()) {
			try {
				pitch.close();
			} catch (Exception e) {
				LogUtil.severe(e);
			}
		}
	}

	public void listDuration() {
		Interval interval = getParentView().getSelectionInterval() != null ? getParentView().getSelectionInterval() : getParentView().getCurrentRecordInterval();
		if(interval == null) return;
		
		float startTime = interval.getStartMarker().getTime();
		float endTime = interval.getEndMarker().getTime();
		float length = endTime - startTime;
		if(length <= 0.0f) return;

		final NumberFormat format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(6);

		final BufferWindow bw = BufferWindow.getBufferWindow();
		bw.showWindow();
		final BufferPanel bufferPanel = bw.createBuffer("Duration (" +
				format.format(startTime) + "-" + format.format(endTime) + ")");
		final LogBuffer buffer = bufferPanel.getLogBuffer();

		try {
			final PrintWriter out =
					new PrintWriter(new OutputStreamWriter(buffer.getStdOutStream(), "UTF-8"));
			out.flush();
			out.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_BUSY);
			out.flush();
			final char qc = '\"';
			final char sc = ',';

			final StringBuilder sb = new StringBuilder();
			sb.append(qc).append("Start Time(s)").append(qc).append(sc);
			sb.append(qc).append("End Time(s)").append(qc).append(sc);
			sb.append(qc).append("Duration(s)").append(qc).append(sc);
			sb.append("\n");

			sb.append(qc).append(format.format(startTime)).append(qc).append(sc);
			sb.append(qc).append(format.format(endTime)).append(qc).append(sc);
			sb.append(qc).append(format.format(endTime-startTime)).append(qc).append(sc);
			sb.append("\n");

			out.write(sb.toString());

			out.flush();
			out.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.STOP_BUSY);
			out.flush();
			out.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_TABLE_CODE);
			out.flush();
			out.close();
		} catch (UnsupportedEncodingException e) {
			LogUtil.warning(e);
		}
	}

	public void listFormants() {
		Interval interval = getParentView().getSelectionInterval() != null ? getParentView().getSelectionInterval() : getParentView().getCurrentRecordInterval();
		if(interval == null) return;
		
		float startTime = interval.getStartMarker().getTime();
		float endTime = interval.getEndMarker().getTime();
		float length = endTime - startTime;
		if(length <= 0.0f) return;

		final NumberFormat format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(6);

		final Formant formants = (formantRef.get() != null ? formantRef.get() : loadFormants());
		try {
			if(formants == null)
				throw new PraatException("No formant information loaded");
			try(Table formantTable = formants.downto_Table(false, true, 6,
					formantSettings.isIncludeIntensity(), 6, formantSettings.isIncludeNumFormants(), 6, formantSettings.isIncludeBandwidths())) {
				final BufferWindow bw = BufferWindow.getBufferWindow();
				bw.showWindow();
				final BufferPanel bufferPanel = bw.createBuffer("Formants (" +
						format.format(startTime) + "-" + format.format(endTime) + ")");
				final LogBuffer buffer = bufferPanel.getLogBuffer();
				
				final PrintWriter out =
						new PrintWriter(new OutputStreamWriter(buffer.getStdOutStream(), "UTF-8"));
				out.flush();
				out.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_BUSY);
				out.flush();
				final char qc = '\"';
				final char sc = ',';
				
				final StringBuilder sb = new StringBuilder();
				for(int col = 1; col <= formantTable.getNcol(); col++) {
					if(col > 1) sb.append(sc);
					sb.append(qc);
					sb.append(formantTable.getColStr(col));
					sb.append(qc);
				}
				out.println(sb.toString());
				
				for(int row = 1; row <= formantTable.getNrow(); row++) {
					sb.setLength(0);
					
					final double time = formantTable.getNumericValue(row, 1);
					
					if(time > endTime) break;
					
					if(time >= startTime) {
						for(int col = 1; col <= formantTable.getNcol(); col++) {
							if(col > 1) sb.append(sc);
							sb.append(qc);
							sb.append(format.format(formantTable.getNumericValue(row, col)));
							sb.append(qc);
						}
						out.println(sb.toString());
					}
				}
				
				out.flush();
				out.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.STOP_BUSY);
				out.flush();
				out.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_TABLE_CODE);
				out.flush();
				out.close();
			}
			
			// delete formants if necessary
			if(formants != formantRef.get()) {
				formants.close();
			}
		} catch (Exception pe) {
			LogUtil.warning(pe);
			return;
		}
	}

	@SuppressWarnings("resource")
	public void listIntensity() {
		final Intensity intensity = (intensityRef.get() != null ? intensityRef.get() : loadIntensity());
		if(intensity == null) return;

		Interval interval = getParentView().getSelectionInterval() != null ? getParentView().getSelectionInterval() : getParentView().getCurrentRecordInterval();
		if(interval == null) return;
		
		float startTime = interval.getStartMarker().getTime();
		float endTime = interval.getEndMarker().getTime();
		float length = endTime - startTime;
		if(length <= 0.0f) return;

		final NumberFormat format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(6);

		final AtomicReference<Long> ixminRef = new AtomicReference<Long>();
		final AtomicReference<Long> ixmaxRef = new AtomicReference<Long>();

		intensity.getWindowSamples(startTime, endTime, ixminRef, ixmaxRef);

		final int ixmin = ixminRef.get().intValue();
		final int ixmax = ixmaxRef.get().intValue();

		final BufferWindow bw = BufferWindow.getBufferWindow();
		bw.showWindow();
		final BufferPanel bufferPanel = bw.createBuffer("Intensity (" +
				format.format(startTime) + "-" + format.format(endTime) + ")");
		final LogBuffer buffer = bufferPanel.getLogBuffer();

		try {
			final PrintWriter out =
					new PrintWriter(new OutputStreamWriter(buffer.getStdOutStream(), "UTF-8"));
			out.flush();
			out.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_BUSY);
			out.flush();
			final char qc = '\"';
			final char sc = ',';

			final StringBuilder sb = new StringBuilder();
			sb.append(qc).append("Time(s)").append(qc).append(sc);
			sb.append(qc).append("Intensity(dB)").append(qc);
			out.println(sb.toString());
			sb.setLength(0);

			for(int i = ixmin; i < ixmax; i++) {
				final double time = intensity.indexToX(i);
				final double val = intensity.getValueAtSample(i, 1, Intensity.UNITS_DB);

				sb.append(qc).append(format.format(time)).append(qc).append(sc);
				sb.append(qc).append(format.format(val)).append(qc);
				out.println(sb.toString());
				sb.setLength(0);
			}

			out.flush();
			out.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.STOP_BUSY);
			out.flush();
			out.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_TABLE_CODE);
			out.flush();
			out.close();
		} catch (IOException e) {
			LogUtil.warning(e);
		}
		
		// delete intensity if necessary
		if(intensity != intensityRef.get()) {
			try {
				intensity.close();
			} catch (Exception e) {
				LogUtil.severe(e);
			}
		}
	}

	public void listSpectralMoments() {
		try(final Spectrum spectrum = loadSpectrumForSpectralMoments()) {
			Interval interval = getParentView().getSelectionInterval() != null ? getParentView().getSelectionInterval() : getParentView().getCurrentRecordInterval();
			if(interval == null) return;
			
			float startTime = interval.getStartMarker().getTime();
			float endTime = interval.getEndMarker().getTime();
			float length = endTime - startTime;
			if(length <= 0.0f) return;
	
			final NumberFormat format = NumberFormat.getNumberInstance();
			format.setMaximumFractionDigits(6);
	
			final BufferWindow bw = BufferWindow.getBufferWindow();
			bw.showWindow();
			final BufferPanel bufferPanel = bw.createBuffer("Spectral Moments (" +
					format.format(startTime) + "-" + format.format(endTime) + ")");
			final LogBuffer buffer = bufferPanel.getLogBuffer();

			final PrintWriter out =
					new PrintWriter(new OutputStreamWriter(buffer.getStdOutStream(), "UTF-8"));
			out.flush();
			out.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_BUSY);
			out.flush();
			final char qc = '\"';
			final char sc = ',';

			final StringBuilder sb = new StringBuilder();
			sb.append(qc).append("Start Time(s)").append(qc).append(sc);
			sb.append(qc).append("End Time(s)").append(qc).append(sc);
			sb.append(qc).append("Center of Gravity").append(qc).append(sc);
			sb.append(qc).append("Standard Deviation").append(qc).append(sc);
			sb.append(qc).append("Kurtosis").append(qc).append(sc);
			sb.append(qc).append("Skewness").append(qc).append(sc);
			out.println(sb.toString());
			sb.setLength(0);

			sb.append(qc).append(format.format(startTime)).append(qc).append(sc);
			sb.append(qc).append(format.format(endTime)).append(qc).append(sc);
			sb.append(qc).append(format.format(spectrum.getCentreOfGravity(2))).append(qc).append(sc);
			sb.append(qc).append(format.format(spectrum.getStandardDeviation(2))).append(qc).append(sc);
			sb.append(qc).append(format.format(spectrum.getKurtosis(2))).append(qc).append(sc);
			sb.append(qc).append(format.format(spectrum.getSkewness(2))).append(qc).append(sc);
			out.println(sb.toString());
			sb.setLength(0);

			out.flush();
			out.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.STOP_BUSY);
			out.flush();
			out.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_TABLE_CODE);
			out.flush();
			out.close();
		} catch (Exception e) {
			LogUtil.warning(e);
		}
	}
	
	public void listPulses() {
		final PointProcess pulses = loadPulses();
		if(pulses == null) return;

		Interval interval = getParentView().getSelectionInterval() != null ? getParentView().getSelectionInterval() : getParentView().getCurrentRecordInterval();
		if(interval == null) return;
		
		float startTime = interval.getStartMarker().getTime();
		float endTime = interval.getEndMarker().getTime();
		float length = endTime - startTime;
		if(length <= 0.0f) return;

		final NumberFormat format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(6);

		final BufferWindow bw = BufferWindow.getBufferWindow();
		bw.showWindow();
		final BufferPanel bufferPanel = bw.createBuffer("Pulses (" +
				format.format(startTime) + "-" + format.format(endTime) + ")");
		final LogBuffer buffer = bufferPanel.getLogBuffer();

		long i1 = pulses.getHighIndex(startTime);
		long i2 = pulses.getLowIndex(endTime);
		
		// print header
		try {
			final PrintWriter out =
					new PrintWriter(new OutputStreamWriter(buffer.getStdOutStream(), "UTF-8"));
			out.flush();
			out.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_BUSY);
			out.flush();
			final StringBuilder sb = new StringBuilder();
			final char qc = '\"';
			sb.append(qc).append("Time(s)").append(qc);
			out.println(sb.toString());
			sb.setLength(0);

			for(long i = i1; i <= i2; i++) {
				double t = pulses.getValueAtIndex(i);
				
				sb.append(qc).append(format.format(t)).append(qc);
				out.println(sb.toString());
				sb.setLength(0);
			}

			out.flush();
			out.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.STOP_BUSY);
			out.flush();
			out.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_TABLE_CODE);
			out.flush();
			out.close();
			
		} catch(IOException e) {
			LogUtil.warning(e);
		}
		try {
			pulses.close();
		} catch (Exception e) {
			LogUtil.severe(e);
		}
	}

	private MediaSegment getSegment() {
		Record currentRecord = getParentView().getEditor().currentRecord();
		if(currentRecord == null) return null;
		return getParentView().getEditor().currentRecord().getMediaSegment();
	}

	public File getAudioFile() {
		SessionMediaModel mediaModel = getParentView().getEditor().getMediaModel();
		if(mediaModel.isSessionAudioAvailable()) {
			return mediaModel.getSessionAudioFile();
		}
		return null;
	}
	
	/**
	 * @return
	 */
	private Spectrogram loadSpectrogram() {
		final MediaSegment segment = getSegment();
		if(segment == null || segment.getEndValue() - segment.getStartValue() <= 0.0f) {
			return null;
		}
		final File audioFile = getAudioFile();
		if(audioFile == null) return null;

		final double xmin = (double)segment.getStartTime();
		final double xmax = (double)segment.getEndTime();

		SpectrogramSettings spectrogramSettings = this.spectrogramSettings;
		final Record currentRecord = getParentView().getEditor().currentRecord();
		if(currentRecord != null && currentRecord.getSpeaker() != Participant.UNKNOWN) {
			spectrogramSettings = SpectrogramSettings.getSettingsForSessionParticipant(
					getParentView().getEditor().getProject(),
					getParentView().getEditor().getSession(),
					currentRecord.getSpeaker()
			);
		}

		Spectrogram spectrogram = null;
		try (final LongSound longSound = LongSound.open(MelderFile.fromPath(getAudioFile().getAbsolutePath()))) {
			try(final Sound part = longSound.extractPart(xmin, xmax, true)) {
				spectrogram = part.to_Spectrogram(
					spectrogramSettings.getWindowLength(), spectrogramSettings.getMaxFrequency(),
					spectrogramSettings.getTimeStep(), spectrogramSettings.getFrequencyStep(),
					spectrogramSettings.getWindowShape(), 8.0, 8.0);
			}
		} catch (Exception e) {
			LogUtil.warning(e);
		}
		return spectrogram;
	}

	private Pitch loadPitch() {
		final MediaSegment segment = getSegment();
		if(segment == null || segment.getEndValue() - segment.getStartValue() <= 0.0f) {
			return null;
		}
		final File audioFile = getAudioFile();
		if(audioFile == null) return null;
		
		final double xmin = (double)segment.getStartTime();
		final double xmax = (double)segment.getEndTime();

		PitchSettings pitchSettings = this.pitchSettings;
		final Record currentRecord = getParentView().getEditor().currentRecord();
		if(currentRecord != null && currentRecord.getSpeaker() != Participant.UNKNOWN) {
			pitchSettings = PitchSettings.getSettingsForSessionParticipant(
					getParentView().getEditor().getProject(),
					getParentView().getEditor().getSession(),
					currentRecord.getSpeaker()
			);
		}

		Pitch pitch = null;
		try (final LongSound longSound = LongSound.open(MelderFile.fromPath(getAudioFile().getAbsolutePath()))) {
			try(final Sound part = longSound.extractPart(xmin, xmax, true)) {
				if(pitchSettings.isAutoCorrelate()) {
					pitch = part.to_Pitch_ac(pitchSettings.getTimeStep(), pitchSettings.getRangeStart(), 3.0,
						pitchSettings.getMaxCandidates(), (pitchSettings.isVeryAccurate() ? 1 : 0), pitchSettings.getSilenceThreshold(),
						pitchSettings.getVoicingThreshold(), pitchSettings.getOctaveCost(),
						pitchSettings.getOctaveJumpCost(), pitchSettings.getVoicedUnvoicedCost(), pitchSettings.getRangeEnd());
				} else {
					pitch = part.to_Pitch_cc(pitchSettings.getTimeStep(), pitchSettings.getRangeStart(), 3.0,
						pitchSettings.getMaxCandidates(), (pitchSettings.isVeryAccurate() ? 1 : 0), pitchSettings.getSilenceThreshold(),
						pitchSettings.getVoicingThreshold(), pitchSettings.getOctaveCost(),
						pitchSettings.getOctaveJumpCost(), pitchSettings.getVoicedUnvoicedCost(), pitchSettings.getRangeEnd());
				}
			}
		} catch (Exception pe) {
			LogUtil.warning(pe);
		}
		return pitch;
	}

	private Formant loadFormants() {
		final MediaSegment segment = getSegment();
		if(segment == null || segment.getEndValue() - segment.getStartValue() <= 0.0f) {
			return null;
		}
		final File audioFile = getAudioFile();
		if(audioFile == null) return null;
		
		final double xmin = (double)segment.getStartTime();
		final double xmax = (double)segment.getEndTime();

        FormantSettings formantSettings = this.formantSettings;
        final Record currentRecord = getParentView().getEditor().currentRecord();
        if(currentRecord != null && currentRecord.getSpeaker() != Participant.UNKNOWN) {
            formantSettings = FormantSettings.getSettingsForSessionParticipant(
                    getParentView().getEditor().getProject(),
                    getParentView().getEditor().getSession(),
                    currentRecord.getSpeaker()
            );
        }

		Formant formants = null;
		try (final LongSound longSound = LongSound.open(MelderFile.fromPath(getAudioFile().getAbsolutePath()))) {
			try(final Sound part = longSound.extractPart(xmin, xmax, true)) {
				formants =
						part.to_Formant_burg(formantSettings.getTimeStep(), formantSettings.getNumFormants(),
								formantSettings.getMaxFrequency(), formantSettings.getWindowLength(), formantSettings.getPreEmphasis());
			}
		} catch (Exception pe) {
			LogUtil.warning(pe);
		}
		return formants;
	}

	private Intensity loadIntensity() {
		final MediaSegment segment = getSegment();
		if(segment == null || segment.getEndValue() - segment.getStartValue() <= 0.0f) {
			return null;
		}
		final File audioFile = getAudioFile();
		if(audioFile == null) return null;

		final double xmin = (double)segment.getStartTime();
		final double xmax = (double)segment.getEndTime();
		
		IntensitySettings intensitySettings = this.intensitySettings;
		final Record currentRecord = getParentView().getEditor().currentRecord();
		if(currentRecord != null && currentRecord.getSpeaker() != Participant.UNKNOWN) {
			intensitySettings = IntensitySettings.getSettingsForSessionParticipant(
					getParentView().getEditor().getProject(),
					getParentView().getEditor().getSession(),
					currentRecord.getSpeaker()
			);
		}

		Intensity intensity = null;
		try (final LongSound longSound = LongSound.open(MelderFile.fromPath(getAudioFile().getAbsolutePath()))) {
			try(final Sound part = longSound.extractPart(xmin, xmax, true)) {
				intensity =
						part.to_Intensity(pitchSettings.getRangeStart(),
								0.0,
								intensitySettings.getSubtractMean());
			}
		} catch (Exception pe) {
			LogUtil.warning(pe);
		}
		return intensity;
	}

	private Spectrum loadSpectrumForSpectralMoments() {
		Interval interval = getParentView().getSelectionInterval() != null ? getParentView().getSelectionInterval() : getParentView().getCurrentRecordInterval();
		if(interval == null) return null;
		
		final File audioFile = getAudioFile();
		if(audioFile == null) return null;

		SpectralMomentsSettings spectralMomentsSettings = this.spectralMomentsSettings;
		final Record currentRecord = getParentView().getEditor().currentRecord();
		if(currentRecord != null && currentRecord.getSpeaker() != Participant.UNKNOWN) {
			spectralMomentsSettings = SpectralMomentsSettings.getSettingsForSessionParticipant(
					getParentView().getEditor().getProject(),
					getParentView().getEditor().getSession(),
					currentRecord.getSpeaker()
			);
		}

		Spectrum spectrum = null;
		try (final LongSound longSound = LongSound.open(MelderFile.fromPath(getAudioFile().getAbsolutePath()))) {
			
			float xmin = interval.getStartMarker().getTime();
			float xmax = interval.getEndMarker().getTime();
			
			try(final Sound part = longSound.extractPart(xmin, xmax, true)) {
				try(final Sound shapedPart = part.extractPart(xmin, xmax, spectralMomentsSettings.getWindowShape(), 2, true)) {
					spectrum = shapedPart.to_Spectrum(true);
					spectrum.passHannBand(spectralMomentsSettings.getFilterStart(), spectralMomentsSettings.getFilterEnd(), spectralMomentsSettings.getFilterSmoothing());
		
					if(spectralMomentsSettings.isUsePreemphasis()) {
						final String formula =
								String.format("if x >= %d then self*x else self fi",
										(Double.valueOf(spectralMomentsSettings.getPreempFrom())).intValue());
						spectrum.formula(formula, Interpreter.create(), null);
					}
				}
			}
		} catch (Exception pe) {
			LogUtil.warning(pe);
		}

		return spectrum;
	}
	
	private PointProcess loadPulses() {
		final MediaSegment segment = getSegment();
		if(segment == null || segment.getEndValue() - segment.getStartValue() <= 0.0f) {
			return null;
		}
		final File audioFile = getAudioFile();
		if(audioFile == null) return null;
		
		final double xmin = (double)segment.getStartTime();
		final double xmax = (double)segment.getEndTime();

		PitchSettings pitchSettings = this.pitchSettings;
		final Record currentRecord = getParentView().getEditor().currentRecord();
		if(currentRecord != null && currentRecord.getSpeaker() != Participant.UNKNOWN) {
			pitchSettings = PitchSettings.getSettingsForSessionParticipant(
					getParentView().getEditor().getProject(),
					getParentView().getEditor().getSession(),
					currentRecord.getSpeaker()
			);
		}

		PointProcess pulses = null;
		try (final LongSound longSound = LongSound.open(MelderFile.fromPath(getAudioFile().getAbsolutePath()))) {
			try(final Sound part = longSound.extractPart(xmin, xmax, true)) {
				Pitch pitch = null;
				if(pitchSettings.isAutoCorrelate()) {
					pitch = part.to_Pitch_ac(pitchSettings.getTimeStep(), pitchSettings.getRangeStart(), 3.0,
						pitchSettings.getMaxCandidates(), (pitchSettings.isVeryAccurate() ? 1 : 0), pitchSettings.getSilenceThreshold(),
						pitchSettings.getVoicingThreshold(), pitchSettings.getOctaveCost(),
						pitchSettings.getOctaveJumpCost(), pitchSettings.getVoicedUnvoicedCost(), pitchSettings.getRangeEnd());
				} else {
					pitch = part.to_Pitch_cc(pitchSettings.getTimeStep(), pitchSettings.getRangeStart(), 3.0,
						pitchSettings.getMaxCandidates(), (pitchSettings.isVeryAccurate() ? 1 : 0), pitchSettings.getSilenceThreshold(),
						pitchSettings.getVoicingThreshold(), pitchSettings.getOctaveCost(),
						pitchSettings.getOctaveJumpCost(), pitchSettings.getVoicedUnvoicedCost(), pitchSettings.getRangeEnd());
				}
				
				pulses = pitch.to_PointProcess_cc(part);
				pitch.close();
			}
		} catch (Exception pe) {
			LogUtil.warning(pe);
		}
		return pulses;
	}

	private void onSessionAudioAvailable(EditorEvent<SessionMediaModel> ee) {
		if(!shouldShow() || !getParentView().getEditor().getViewModel().isShowingInStack(SpeechAnalysisEditorView.VIEW_NAME)) return;
		update(true);
	}

	private void onMediaChanged(EditorEvent<EditorEventType.SessionMediaChangedData> ee) {
		if(!shouldShow() || !getParentView().getEditor().getViewModel().isShowingInStack(SpeechAnalysisEditorView.VIEW_NAME)) return;
		update(true);
	}
	
	private void onTimeModelChanged(EditorEvent<TimeUIModel> ee) {
		if(!shouldShow() || !getParentView().getEditor().getViewModel().isShowingInStack(SpeechAnalysisEditorView.VIEW_NAME)) return;
		update();
	}

	private void onSegmentChanged(EditorEvent<EditorEventType.TierChangeData> ee) {
		if(ee.data().valueAdjusting()) return;
		if(!shouldShow() || !getParentView().getEditor().getViewModel().isShowingInStack(SpeechAnalysisEditorView.VIEW_NAME)) return;
		if(ee.data().tier().getName().equals(SystemTierType.Segment.getName())) {
			update();
		}
	}
	
	private void onEditorClosing(EditorEvent<Void> ee) {
		// cleanup any loaded data
		cleanup();
	}

	/**
	 * Generic load data class with reentrant lock
	 *
	 * @param <T>
	 */
	private class LoadData<T> extends PhonTask {

		private final ReentrantLock updateLock = new ReentrantLock();

		private AtomicReference<T> ref;

		private Supplier<T> supplier;

		public LoadData(AtomicReference<T> ref, Supplier<T> supplier) {
			super();

			this.ref = ref;
			this.supplier = supplier;
		}

		@Override
		public void performTask() {
			super.setStatus(TaskStatus.RUNNING);

			final MediaSegment segment = getSegment();
			if(segment == null) {
				if(updateLock.isLocked())
					updateLock.unlock();
				return;
			}
			
			updateLock.lock();

			try {
				final T data = supplier.get();
				ref.set(data);
			} catch (Exception e) {
				LogUtil.warning(e);
				super.err = e;
				super.setStatus(TaskStatus.ERROR);
			}

			updateLock.unlock();

			super.setStatus(TaskStatus.FINISHED);
		}

	}

	private final LoadData<Spectrogram> spectrogramLoader = new LoadData<>(spectrogramRef, this::loadSpectrogram);
	private final LoadData<Formant> formantLoader = new LoadData<>(formantRef, this::loadFormants);
	private final LoadData<Pitch> pitchLoader = new LoadData<>(pitchRef, this::loadPitch);
	private final LoadData<Intensity> intensityLoader = new LoadData<>(intensityRef, this::loadIntensity);

	/**
	 * Task used to update display.
	 */
	private final Runnable updateTask = new Runnable() {

		@Override
		public void run() {
			revalidate();
			repaint();
		}

	};

	private void cleanup() {
		if(spectrogramRef.get() != null) {
			try {
				spectrogramRef.get().forget();
			} catch (PraatException e) {
				LogUtil.severe(e);
			}
		}
		spectrogramRef.set(null);
		spectrogramPainter.setRepaintBuffer(true);

		if(formantRef.get() != null) {
			try {
				formantRef.get().forget();
			} catch (PraatException e) {
				LogUtil.severe(e);
			}
		}
		formantRef.set(null);
		formantPainter.setRepaintBuffer(true);
		
		if(pitchRef.get() != null) {
			try {
				pitchRef.get().forget();
			} catch (PraatException e) {
				LogUtil.severe(e);
			}
		}
		pitchRef.set(null);
		pitchPainter.setRepaintBuffer(true);

		if(intensityRef.get() != null) {
			try {
				intensityRef.get().forget();
			} catch (PraatException e) {
				LogUtil.severe(e);
			}
		}
		intensityRef.set(null);
		intensityPainter.setRepaintBuffer(true);
	}
	
	/**
	 * Set all values to null and reset painters.
	 *
	 */
	private void clearDisplay() {
		cleanup();

		if(SwingUtilities.isEventDispatchThread())
			updateTask.run();
		else
			SwingUtilities.invokeLater(updateTask);
	}

	public void update() {
		update(false);
	}

	public void update(boolean force) {
		if(getParentView().getEditor().currentRecord() == null) return;
		
		if(!force && !shouldShow()) return;
		
		if(!getParentView().getEditor().getMediaModel().isSessionAudioAvailable()) return;
		
		final MediaSegment segment = getSegment();
		if(segment == null) {
			clearDisplay();
			return;
		}

		// check analysis length
		final double startTime = segment.getStartTime();
		final double endTime = segment.getEndTime();
		final double len = endTime - startTime;
		final boolean sameSegment =
				(lastStartTime == startTime && lastEndTime == endTime);

		if(len <= 0.0) return;

		if(len > maxAnalysisLength && !forceLoadSpectrogram && !sameSegment) {
			lastStartTime = -1;
			lastEndTime = -1;

			clearDisplay();
			SwingUtilities.invokeLater( () -> maxAnalysisMessage.setVisible(true) );
			return;
		}

		if(sameSegment && !force) {
			// don't re-load data, return
			return;
		}
		forceLoadSpectrogram = false;

		SwingUtilities.invokeLater( () -> maxAnalysisMessage.setVisible(false) );

		final PhonWorker worker = PhonWorker.createWorker();
		worker.setName(SpectrogramView.class.getName()+".worker");

		SpectrogramSettings spectrogramSettings = this.spectrogramSettings;
		final Record currentRecord = getParentView().getEditor().currentRecord();
		if(currentRecord != null && currentRecord.getSpeaker() != Participant.UNKNOWN) {
			spectrogramSettings = SpectrogramSettings.getSettingsForSessionParticipant(
					getParentView().getEditor().getProject(),
					getParentView().getEditor().getSession(),
					currentRecord.getSpeaker()
			);
		}
		spectrogramPainter.setRepaintBuffer(true);
		spectrogramPainter.setSettings(spectrogramSettings);
		worker.invokeLater(spectrogramLoader);

		if(showFormants) {
			formantPainter.setRepaintBuffer(true);
			formantPainter.setSettings(formantSettings);
			formantPainter.setMaxFrequency(spectrogramSettings.getMaxFrequency());
			worker.invokeLater(formantLoader);
		}

		if(showPitch) {
			PitchSettings pitchSettings = this.pitchSettings;
			if(currentRecord != null && currentRecord.getSpeaker() != Participant.UNKNOWN) {
				pitchSettings = PitchSettings.getSettingsForSessionParticipant(
						getParentView().getEditor().getProject(),
						getParentView().getEditor().getSession(),
						currentRecord.getSpeaker()
				);
			}
			pitchPainter.setRepaintBuffer(true);
			pitchPainter.setSettings(pitchSettings);
			worker.invokeLater(pitchLoader);
		}

		if(showIntensity) {
			IntensitySettings intensitySettings = this.intensitySettings;
			if(currentRecord != null && currentRecord.getSpeaker() != Participant.UNKNOWN) {
				intensitySettings = IntensitySettings.getSettingsForSessionParticipant(
						getParentView().getEditor().getProject(),
						getParentView().getEditor().getSession(),
						currentRecord.getSpeaker()
				);
			}
			intensityPainter.setRepaintBuffer(true);
			intensityPainter.setSettings(intensitySettings);
			worker.invokeLater(intensityLoader);
		}

		worker.invokeLater( () -> { lastStartTime = startTime; lastEndTime = endTime; } );
		worker.invokeLater( () -> SwingUtilities.invokeLater(updateTask) );

		worker.setFinishWhenQueueEmpty(true);
		worker.start();
	}

	private class SpectrogramPanel extends TimeComponent {

		private static final long serialVersionUID = 7940163213370438304L;

		public SpectrogramPanel() {
			super(SpectrogramView.this.getTimeModel());
			setFocusable(true);
			
			setUI(new TimeComponentUI());
		}

		@Override
		public void paintComponent(Graphics g) {
			final Graphics2D g2 = (Graphics2D)g;

			g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, 
					RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
					RenderingHints.VALUE_STROKE_PURE);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			if(isOpaque()) {
				g2.setColor(getBackground());
				g2.fill(g2.getClipBounds());
			}
			
			spectrogramLoader.updateLock.lock();
			Spectrogram spectrogram = spectrogramRef.get();
			if(spectrogram == null) {
				spectrogramLoader.updateLock.unlock();
				return;
			}
						
			final TimeUIModel timeModel = getTimeModel();
			final int height = getHeight();
			
			final double segX1 = timeModel.xForTime((float)spectrogram.getXMin());
			final double segX2 = timeModel.xForTime((float)spectrogram.getXMax());

			final Rectangle2D contentRect = new Rectangle2D.Double(
					segX1, 0, segX2-segX1, height);

			if((int)contentRect.getWidth() <= 0
					|| (int)contentRect.getHeight() <= 0) {
				return;
			}

			spectrogramPainter.paint(spectrogram, g2, contentRect);

			if(showFormants && formantRef.get() != null) {
				formantLoader.updateLock.lock();
				formantPainter.paint(formantRef.get(), g2, contentRect);
				formantLoader.updateLock.unlock();
			}

			if(showPitch && pitchRef.get() != null) {
				pitchLoader.updateLock.lock();
				pitchPainter.paint(pitchRef.get(), g2, contentRect);
				pitchLoader.updateLock.unlock();
			}

			if(showIntensity && intensityRef.get() != null) {
				intensityLoader.updateLock.lock();
				intensityPainter.paint(intensityRef.get(), g2, contentRect);
				intensityLoader.updateLock.unlock();
			}

			final Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
			final NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(2);
			nf.setGroupingUsed(false);

			Marker cursorMarker = getParentView().getCursorMarker();
			double cursorX = (cursorMarker != null ? getTimeModel().xForTime(cursorMarker.getTime()) : -1.0);
			if(cursorMarker != null && cursorX >= 0.0 && contentRect.contains(cursorX, contentRect.getY())) {
				final Line2D line = new Line2D.Double(cursorX, contentRect.getY(),
						cursorX, contentRect.getY() + contentRect.getHeight());

				g2.setStroke(dashed);
				g2.setColor(Color.WHITE);
				g2.draw(line);

				if(showFormants && formantRef.get() != null) {
					final Formant formants = formantRef.get();

					int x = (int)(getVisibleRect().x);
					int y = (int)(contentRect.getCenterY() - ((g2.getFontMetrics().getHeight() * formantSettings.getNumFormants()) / 2.0));
					for(int i = formantSettings.getNumFormants(); i > 0; i--) {
						final double fVal = formants.getValueAtTime(i, cursorMarker.getTime(), kFormant_unit.HERTZ);

						final String formantStr = (!Double.isNaN(fVal) ? String.format("F%d: %.2f Hz", i, fVal) : String.format("F%d:", i));
						final Rectangle2D bounds = g2.getFontMetrics().getStringBounds(formantStr, g2);

						y += (int)Math.ceil(bounds.getHeight());
						g2.setColor(Color.red);
						g2.drawString(formantStr, x, y);
					}

					if(formantSettings.isIncludeBandwidths()) {
						y += 10;

						for(int i = formantSettings.getNumFormants(); i > 0; i--) {
							final double bVal = formants.getBandwidthAtTime(i, cursorMarker.getTime(), kFormant_unit.HERTZ);

							final String formantStr = String.format("B%d: %.2f", i, bVal);
							final Rectangle2D bounds = g2.getFontMetrics().getStringBounds(formantStr, g2);

						 y += (int)Math.ceil(bounds.getHeight());
							g2.setColor(Color.red);
							g2.drawString(formantStr, x, y);
						}
					}
				}

				if(showPitch && pitchRef.get() != null && getParentView().getSelectionInterval() == null) {
					final Pitch pitch = pitchRef.get();
					// get pitch at current x
					double pitchVal = pitch.getValueAtX(cursorMarker.getTime(), Pitch.LEVEL_FREQUENCY,
							pitchSettings.getUnits().ordinal(), true);
					if(!Double.isInfinite(pitchVal) && !Double.isNaN(pitchVal)) {
						final double hzPerPixel =
								(pitchSettings.getRangeEnd() - pitchSettings.getRangeStart()) / getHeight();
						final double yPos =
								getHeight() - ((pitchVal - pitchSettings.getRangeStart()) / hzPerPixel);
						pitchVal = pitch.convertStandardToSpecialUnit(pitchVal, Pitch.LEVEL_FREQUENCY,
								pitchSettings.getUnits().ordinal());
						final String pitchUnitStr =
								pitch.getUnitText(Pitch.LEVEL_FREQUENCY, pitchSettings.getUnits().ordinal(),
										Function.UNIT_TEXT_SHORT).toString();

						final String pitchStr =
								nf.format(pitchVal) + " " + pitchUnitStr;

						g2.setColor(Color.blue);
						g2.drawString(pitchStr, (float)contentRect.getX() + (float)contentRect.getWidth(), (float)yPos);
					}
				}

				if(showIntensity && intensityRef.get() != null && getParentView().getSelectionInterval() == null) {
					final Intensity intensity = intensityRef.get();
					double intensityVal = intensity.getValueAtX(cursorMarker.getTime(), 1, Intensity.UNITS_DB, true);

					if(!Double.isInfinite(intensityVal) && !Double.isNaN(intensityVal)) {
						final double dbPerPixel =
								(intensitySettings.getViewRangeMax() - intensitySettings.getViewRangeMin()) / getHeight();
						final double yPos =
								getHeight() - ((intensityVal - intensitySettings.getViewRangeMin()) / dbPerPixel);
						final String intensityUnitStr = "dB";
						final String intensityStr =
								nf.format(intensityVal) + " " + intensityUnitStr;
						final Rectangle2D intensityRect =
								g2.getFontMetrics().getStringBounds(intensityStr, g2);
						final double intensityX = (contentRect.getX() + contentRect.getWidth()) -
								intensityRect.getWidth();

						g2.setColor(Color.yellow);
						g2.drawString(intensityStr, (float)intensityX, (float)yPos);
					}
				}
			}

			Interval selectionInterval = getParentView().getSelectionInterval();
			if(selectionInterval != null) {
				float startTime = selectionInterval.getStartMarker().getTime();
				double x1 = timeModel.xForTime(startTime);
				float endTime = selectionInterval.getEndMarker().getTime();
				double x2 = timeModel.xForTime(endTime);
				
				final Line2D line =  new Line2D.Double(x1, contentRect.getY(),
						x1, contentRect.getY() + contentRect.getHeight());

				g2.setStroke(dashed);
				g2.setColor(Color.white);
				g2.draw(line);

				line.setLine(x2, contentRect.getY(), x2, contentRect.getY() + contentRect.getHeight());
				g2.draw(line);

				Rectangle2D selRect = new Rectangle2D.Double(x1, contentRect.getY(), x2-x1,
								contentRect.getHeight());

				g2.setColor(selectionInterval.getColor());
				g2.fill(selRect);

				if(showPitch && pitchRef.get() != null) {
					final Pitch pitch = pitchRef.get();
					// draw avg pitch
					double pitchVal = pitch.getMean(startTime, endTime, Pitch.LEVEL_FREQUENCY,
							pitchSettings.getUnits().ordinal(), true);
					if(!Double.isInfinite(pitchVal) && !Double.isNaN(pitchVal)) {
						final double hzPerPixel =
								(pitchSettings.getRangeEnd() - pitchSettings.getRangeStart()) / getHeight();
						final double yPos =
								getHeight() - ((pitchVal - pitchSettings.getRangeStart()) / hzPerPixel);
						pitchVal = pitch.convertStandardToSpecialUnit(pitchVal, Pitch.LEVEL_FREQUENCY,
								pitchSettings.getUnits().ordinal());
						final String pitchUnitStr =
								pitch.getUnitText(Pitch.LEVEL_FREQUENCY, pitchSettings.getUnits().ordinal(),
										Function.UNIT_TEXT_SHORT).toString();

						final String pitchStr =
								nf.format(pitchVal) + " " + pitchUnitStr;
						g2.setColor(Color.blue);

						g2.drawString(pitchStr, (float)contentRect.getX()+(float)contentRect.getWidth(), (float)yPos);
					}
				}

				if(showIntensity && intensityRef.get() != null) {
					final Intensity intensity = intensityRef.get();
					double intensityVal = 0.0;
					try {
						intensityVal = intensity.getAverage(startTime, endTime, intensitySettings.getAveraging());
					} catch (PraatException pe) {
						LogUtil.warning(pe);
					}
					if(!Double.isInfinite(intensityVal) && !Double.isNaN(intensityVal)) {
						final double dbPerPixel =
								(intensitySettings.getViewRangeMax() - intensitySettings.getViewRangeMin()) / getHeight();
						final double yPos =
								getHeight() - ((intensityVal - intensitySettings.getViewRangeMin()) / dbPerPixel);
						final String intensityUnitStr = "dB";
						final String intensityStr =
								nf.format(intensityVal) + " " + intensityUnitStr;
						final Rectangle2D intensityRect =
								g2.getFontMetrics().getStringBounds(intensityStr, g2);
						final double intensityX = (contentRect.getX()+contentRect.getWidth()) - intensityRect.getWidth();

						g2.setColor(Color.yellow);
						g2.drawString(intensityStr, (float)intensityX, (float)yPos);
					}
				}
			}

			if(currentPoint != null) {
				// draw dotted lines intersecting our point
				g2.setStroke(dashed);
				g2.setColor(Color.white);
				g2.drawLine((int)contentRect.getX(), currentPoint.y,
						(int)contentRect.getX()+(int)contentRect.getWidth(), currentPoint.y );

				// draw frequency at point
				if(spectrogram != null) {
					// convert y to a frequency bin
					final int bin = (int)Math.round(
							((contentRect.getHeight()-currentPoint.y)/contentRect.getHeight()) * spectrogram.getNy() );
					final float freq = bin * (float)spectrogram.getDy();

					final String freqTxt = nf.format(freq) + " Hz";

					final FontMetrics fm = g2.getFontMetrics();
					final Rectangle2D bounds = fm.getStringBounds(freqTxt, g2);

					g2.setColor(Color.red);
					g2.drawString(freqTxt,
							(float)(contentRect.getX() - bounds.getWidth()),
							(float)(currentPoint.y + (bounds.getHeight()/4)));

				}
			}

			final Rectangle2D leftInsetRect = new Rectangle2D.Double(
					contentRect.getX() - 100.0, contentRect.getY(),
					100.0, contentRect.getHeight());
			final Rectangle2D rightInsetRect = new Rectangle2D.Double(
					contentRect.getX()+contentRect.getWidth(), contentRect.getY(),
					100.0, contentRect.getHeight());
			if(spectrogram != null) {
				spectrogramLoader.updateLock.lock();
				spectrogramPainter.paintGarnish(spectrogram, g2, leftInsetRect, SwingConstants.LEFT);
				spectrogramLoader.updateLock.unlock();
			}

			if(showPitch && pitchRef.get() != null) {
				pitchLoader.updateLock.lock();
				pitchPainter.paintGarnish(pitchRef.get(), g2, rightInsetRect, SwingConstants.RIGHT);
				pitchLoader.updateLock.unlock();
			}

			if(showIntensity && intensityRef.get() != null) {
				intensityLoader.updateLock.lock();
				intensityPainter.paintGarnish(intensityRef.get(), g2, rightInsetRect, SwingConstants.RIGHT);
				intensityLoader.updateLock.unlock();
			}
			spectrogramLoader.updateLock.unlock();
		
			for(var i:getTimeModel().getIntervals()) {
				getUI().paintInterval(g2, i, false);
			}
			
			for(var marker:getTimeModel().getMarkers()) {
				getUI().paintMarker(g2, marker);
			}
		
		}
		
		
	}

	@Override
	public void onRefresh() {
		update(true);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if(!visible && maxAnalysisMessage.isVisible())
			maxAnalysisMessage.setVisible(false);
	}
	
	public void addMenuItems(MenuBuilder builder, boolean isContextMenu) {
		final PhonUIAction<Void> toggleAct = PhonUIAction.runnable(this::onToggleSpectrogram);
		toggleAct.setRunInBackground(true);
		toggleAct.putValue(PhonUIAction.NAME, "Show Spectrogram");
		toggleAct.putValue(PhonUIAction.SELECTED_KEY, shouldShow());
		if(isContextMenu)
			toggleAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.SHIFT_DOWN_MASK));
		builder.addItem(".", new JCheckBoxMenuItem(toggleAct));

		final PhonUIAction<Void> settingsAct = PhonUIAction.runnable(this::onEditSettings);
		settingsAct.putValue(PhonUIAction.NAME, "Spectrogram settings...");
		settingsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Edit spectrogram settings...");
		builder.addItem(".", settingsAct);

		builder.addSeparator(".", "s1");

		final PhonUIAction<Void> durationAct = PhonUIAction.runnable(this::listDuration);
		durationAct.putValue(PhonUIAction.NAME, "Get duration...");
		durationAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Get duration for segment/selection");
		if(isContextMenu)
			durationAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, 0));
		builder.addItem(".", durationAct);

		builder.addSeparator(".", "s2");

		// formants
		final PhonUIAction<Void> toggleFormants = PhonUIAction.runnable(this::onToggleFormants);
		toggleFormants.putValue(PhonUIAction.NAME, "Show Formants");
		toggleFormants.putValue(PhonUIAction.SELECTED_KEY, showFormants);
		if(isContextMenu)
			toggleFormants.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.SHIFT_DOWN_MASK));
		builder.addItem(".", new JCheckBoxMenuItem(toggleFormants));

		final PhonUIAction<Void> formantSettingsAct = PhonUIAction.runnable(this::onEditFormantSettings);
		formantSettingsAct.putValue(PhonUIAction.NAME, "Formant settings...");
		formantSettingsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Edit formant settings...");
		builder.addItem(".", formantSettingsAct);

		final PhonUIAction<Void> listFormantsAct = PhonUIAction.runnable(this::listFormants);
		listFormantsAct.putValue(PhonUIAction.NAME, "Formant listing");
		listFormantsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "List formants for segment/selection");
		if(isContextMenu)
			listFormantsAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, 0));
		builder.addItem(".", listFormantsAct);

		builder.addSeparator(".", "s3");

		final PhonUIAction<Void> togglePitchAct = PhonUIAction.runnable(this::onTogglePitch);
		togglePitchAct.putValue(PhonUIAction.NAME, "Show Pitch");
		togglePitchAct.putValue(PhonUIAction.SELECTED_KEY, showPitch);
		if(isContextMenu)
			togglePitchAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.SHIFT_DOWN_MASK));
		builder.addItem(".", new JCheckBoxMenuItem(togglePitchAct));

		final PhonUIAction<Void> pitchSettingsAct = PhonUIAction.runnable(this::onEditPitchSettings);
		pitchSettingsAct.putValue(PhonUIAction.NAME, "Pitch settings...");
		pitchSettingsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Edit pitch settings...");
		builder.addItem(".", pitchSettingsAct);

		final PhonUIAction<Void> listPitchAct = PhonUIAction.runnable(this::listPitch);
		listPitchAct.putValue(PhonUIAction.NAME, "Pitch listing");
		listPitchAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "List pitch for segment/selection");
		if(isContextMenu)
			listPitchAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, 0));
		builder.addItem(".", listPitchAct);
		
		final PhonUIAction<Void> listPulsesAct = PhonUIAction.runnable(this::listPulses);
		listPulsesAct.putValue(PhonUIAction.NAME, "Pulse listing");
		builder.addItem(".", listPulsesAct);

		builder.addSeparator(".", "s4");

		final PhonUIAction<Void> toggleIntensityAct = PhonUIAction.runnable(this::onToggleIntensity);
		toggleIntensityAct.putValue(PhonUIAction.NAME, "Show Intensity");
		toggleIntensityAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show intensity");
		if(isContextMenu)
			toggleIntensityAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.SHIFT_DOWN_MASK));
		final JCheckBoxMenuItem toggleIntensityItem = new JCheckBoxMenuItem(toggleIntensityAct);
		toggleIntensityItem.setSelected(showIntensity);
		builder.addItem(".", toggleIntensityItem);

		final PhonUIAction<Void> intensitySettingsAct = PhonUIAction.runnable(this::onEditIntensitySettings);
		intensitySettingsAct.putValue(PhonUIAction.NAME, "Intensity settings...");
		intensitySettingsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Edit intensity settings...");
		builder.addItem(".", intensitySettingsAct);

		final PhonUIAction<Void> listIntensityAct = PhonUIAction.runnable(this::listIntensity);
		listIntensityAct.putValue(PhonUIAction.NAME, "Intensity listing");
		listIntensityAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "List intensity for segment/selection");
		if(isContextMenu)
			listIntensityAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, 0));
		builder.addItem(".", listIntensityAct);

		builder.addSeparator(".", "s5");

		final PhonUIAction<Void> spectralMomentsSettingsAct = PhonUIAction.runnable(this::onEditSpectralMomentsSettings);
		spectralMomentsSettingsAct.putValue(PhonUIAction.NAME, "Spectral Moments settings...");
		spectralMomentsSettingsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Edit spectral moments settings...");
		builder.addItem(".", spectralMomentsSettingsAct);

		final PhonUIAction<Void> listSpectralMomentsAct = PhonUIAction.runnable(this::listSpectralMoments);
		listSpectralMomentsAct.putValue(PhonUIAction.NAME, "Spectral Moments listing...");
		listSpectralMomentsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "List spectral moments for segment/selection");
		builder.addItem(".", listSpectralMomentsAct);
	}

	private final MouseInputAdapter pointListener = new MouseInputAdapter() {

		@Override
		public void mousePressed(MouseEvent me) {
			if(me.getButton() == MouseEvent.BUTTON1) {
				currentPoint = me.getPoint();
				repaint();
			}
		}

	};

}
