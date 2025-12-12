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

import ca.hedlund.jpraat.binding.fon.kSound_to_Spectrogram_windowShape;
import ca.phon.project.Project;
import ca.phon.session.Participant;
import ca.phon.session.Session;
import ca.phon.ui.action.PhonUIAction;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

/**
 * Panel for modifying spectrogram settings.
 *
 */
public class SpectrogramSettingsPanel extends JPanel {

	private static final long serialVersionUID = 5101268187463658850L;

	private JFormattedTextField windowLengthField;
	
	private JFormattedTextField maxFreqField;
	
	private JFormattedTextField timeStepField;
	
	private JFormattedTextField freqStepField;
	
	private JComboBox<kSound_to_Spectrogram_windowShape> windowShapeBox;
	
	private JFormattedTextField preEmphasisField;
	
	private JFormattedTextField dynamicRangeField;
	
	private JFormattedTextField dynamicCompressionField;
	
	private JButton saveDefaultsButton;
	
	private JButton loadDefaultsButton;
	
	private JButton loadStandardsButton;

	private Project project;

	private Session session;

	private Participant participant;

	public SpectrogramSettingsPanel() {
		this(null, null, null, new SpectrogramSettings());
	}

	public SpectrogramSettingsPanel(Project project, Session session, Participant participant) {
		this(project, session, participant, SpectrogramSettings.getSettingsForSessionParticipant(project, session, participant));
	}

	public SpectrogramSettingsPanel(SpectrogramSettings settings) {
		this(null, null, null, settings);
	}

	public SpectrogramSettingsPanel(Project project, Session session, Participant participant, SpectrogramSettings settings) {
		super();
		this.project = project;
		this.session = session;
		this.participant = participant;
		init();
		loadSettings(settings);
	}
	
	private void init() {
		final FormLayout layout = new FormLayout(
				"right:pref, 3dlu, fill:pref:grow, pref",
				"pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu");
		final JPanel formPanel = new JPanel(layout);
		final CellConstraints cc = new CellConstraints();
		
		final NumberFormat numberFormat = NumberFormat.getNumberInstance();
		
		windowLengthField = new JFormattedTextField(numberFormat);
		formPanel.add(new JLabel("Window length:"), cc.xy(1, 1));
		formPanel.add(windowLengthField, cc.xy(3, 1));
		formPanel.add(new JLabel("(s)"), cc.xy(4, 1));
		
		maxFreqField = new JFormattedTextField(numberFormat);
		formPanel.add(new JLabel("Max frequency:"), cc.xy(1, 3));
		formPanel.add(maxFreqField, cc.xy(3, 3));
		formPanel.add(new JLabel("(Hz)"), cc.xy(4, 3));
		
		timeStepField = new JFormattedTextField(numberFormat);
		formPanel.add(new JLabel("Time step:"), cc.xy(1, 5));
		formPanel.add(timeStepField, cc.xy(3, 5));
		formPanel.add(new JLabel("(s)"), cc.xy(4, 5));
		
		freqStepField = new JFormattedTextField(numberFormat);
		formPanel.add(new JLabel("Frequency step:"), cc.xy(1, 7));
		formPanel.add(freqStepField, cc.xy(3, 7));
		formPanel.add(new JLabel("(Hz)"), cc.xy(4, 7));
		
		windowShapeBox = new JComboBox<>(kSound_to_Spectrogram_windowShape.values());
		formPanel.add(new JLabel("Window shape:"), cc.xy(1, 9));
		formPanel.add(windowShapeBox, cc.xy(3, 9));
		
		preEmphasisField = new JFormattedTextField(numberFormat);
		formPanel.add(new JLabel("Pre-emphasis:"), cc.xy(1, 11));
		formPanel.add(preEmphasisField, cc.xy(3, 11));
		formPanel.add(new JLabel("(dB/oct)"), cc.xy(4, 11));
		
		dynamicRangeField = new JFormattedTextField(numberFormat);
		formPanel.add(new JLabel("Dynamic range:"), cc.xy(1, 13));
		formPanel.add(dynamicRangeField, cc.xy(3, 13));
		formPanel.add(new JLabel("(Hz)"), cc.xy(4, 13));
		
		dynamicCompressionField = new JFormattedTextField(numberFormat);
		formPanel.add(new JLabel("Dynamic compression:"), cc.xy(1,15));
		formPanel.add(dynamicCompressionField, cc.xy(3, 15));
		formPanel.add(new JLabel("(dB)"), cc.xy(4, 15));
		
		final JPanel btnPanel = new JPanel(new VerticalLayout());
		
		final PhonUIAction<Void> loadDefaultsAct = PhonUIAction.runnable(this::loadDefaults);
		loadDefaultsAct.putValue(PhonUIAction.NAME, "Load defaults");
		loadDefaultsButton = new JButton(loadDefaultsAct);
		btnPanel.add(loadDefaultsButton);
		
		final PhonUIAction<Void> loadStandardsAct = PhonUIAction.runnable(this::loadStandards);
		loadStandardsAct.putValue(PhonUIAction.NAME, "Load standards");
		loadStandardsButton = new JButton(loadStandardsAct);
		btnPanel.add(loadStandardsButton);
		
		final PhonUIAction<Void> saveDefaultsAct = PhonUIAction.runnable(this::saveSettingsAsDefaults);
		saveDefaultsAct.putValue(PhonUIAction.NAME, "Save as defaults");
		saveDefaultsButton = new JButton(saveDefaultsAct);
		btnPanel.add(saveDefaultsButton);

		if(project != null && session != null && participant != null && participant != Participant.UNKNOWN) {
			final PhonUIAction<Void> saveParticipantDefaultsAct = PhonUIAction.runnable(this::saveSettingsForParticipant);
			saveParticipantDefaultsAct.putValue(PhonUIAction.NAME, "Save settings for " + participant);
			final JButton saveParticipantDefaultsButton = new JButton(saveParticipantDefaultsAct);
			btnPanel.add(saveParticipantDefaultsButton);
		}

		setLayout(new BorderLayout());
		add(formPanel, BorderLayout.CENTER);
		add(btnPanel, BorderLayout.EAST);
	}
	
	public void loadSettings(SpectrogramSettings settings) {
		windowLengthField.setValue(settings.getWindowLength());
		maxFreqField.setValue(settings.getMaxFrequency());
		timeStepField.setValue(settings.getTimeStep());
		freqStepField.setValue(settings.getFrequencyStep());
		windowShapeBox.setSelectedItem(settings.getWindowShape());
		preEmphasisField.setValue(settings.getPreEmphasis());
		dynamicRangeField.setValue(settings.getDynamicRange());
		dynamicCompressionField.setValue(settings.getDynamicCompression());
	}
	
	public SpectrogramSettings getSettings() {
		final SpectrogramSettings retVal = new SpectrogramSettings();
		
		final double windowLength = ((Number)windowLengthField.getValue()).doubleValue();
		retVal.setWindowLength(windowLength);
		
		final double maxFreq = ((Number)maxFreqField.getValue()).doubleValue();
		retVal.setMaxFrequency(maxFreq);
		
		final double timeStep = ((Number)timeStepField.getValue()).doubleValue();
		retVal.setTimeStep(timeStep);
		
		final double freqStep = ((Number)freqStepField.getValue()).doubleValue();
		retVal.setFrequencyStep(freqStep);
		
		final kSound_to_Spectrogram_windowShape windowShape = 
				(kSound_to_Spectrogram_windowShape)windowShapeBox.getSelectedItem();
		retVal.setWindowShape(windowShape);
		
		final double preEmphasis = ((Number)preEmphasisField.getValue()).doubleValue();
		retVal.setPreEmphasis(preEmphasis);
		
		final double dynamicRange = ((Number)dynamicRangeField.getValue()).doubleValue();
		retVal.setDynamicRange(dynamicRange);
		
		final double dynamicCompression = ((Number)dynamicCompressionField.getValue()).doubleValue();
		retVal.setDynamicCompression(dynamicCompression);
		
		return retVal;
	}
	
	public void saveSettingsAsDefaults() {
		getSettings().saveAsDefaults();
	}
	
	public void loadStandards() {
		final SpectrogramSettings settings = new SpectrogramSettings();
		settings.loadStandards();
		loadSettings(settings);
	}
	
	public void loadDefaults() {
		loadSettings(new SpectrogramSettings());
	}

	public void saveSettingsForParticipant() {
		if(project != null && session != null && participant != null && participant != Participant.UNKNOWN) {
			final SpectrogramSettings settings = getSettings();
			SpectrogramSettings.saveForSessionParticipant(project, session, participant, settings);
		}
	}

}
