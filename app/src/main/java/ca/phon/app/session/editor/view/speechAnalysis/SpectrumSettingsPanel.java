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

import ca.phon.ui.decorations.DialogHeader;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for editing settings for Spectrumt tier of Phon's speech
 * analysis view.
 * 
 * 
 */
public class SpectrumSettingsPanel extends JPanel {

	private static final long serialVersionUID = -7778566797207084580L;

	private JTabbedPane tabPanel;
	
	private SpectrogramSettingsPanel spectrogramSettingsPanel;
	
	private FormantSettingsPanel formantSettingsPanel;
	
	private PitchSettingsPanel pitchSettingsPanel;
	
	private IntensitySettingsPanel intensitySettingsPanel;
	
	public SpectrumSettingsPanel() {
		super();
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		final DialogHeader header = new DialogHeader("Spectrum Settigns", "Edit settings for Spectrum tier.");
		add(header, BorderLayout.NORTH);
		
		tabPanel = new JTabbedPane();
		add(tabPanel, BorderLayout.CENTER);
		
		spectrogramSettingsPanel = new SpectrogramSettingsPanel();
		tabPanel.add("Spectrogram", spectrogramSettingsPanel);
		
		formantSettingsPanel = new FormantSettingsPanel();
		tabPanel.add("Formant", formantSettingsPanel);
		
		pitchSettingsPanel = new PitchSettingsPanel();
		tabPanel.add("Pitch", pitchSettingsPanel);
		
		intensitySettingsPanel = new IntensitySettingsPanel();
		tabPanel.add("Intensity", intensitySettingsPanel);
	}
	
	public void showSpectrogramSettings() {
		tabPanel.setSelectedComponent(spectrogramSettingsPanel);
	}
	
	public void loadSpectrogramSettings(SpectrogramSettings spectrogramSettings) {
		spectrogramSettingsPanel.loadSettings(spectrogramSettings);
	}
	
	public void showFormantSettings() {
		tabPanel.setSelectedComponent(formantSettingsPanel);
	}
	
	public void loadFormantSettings(FormantSettings formantSettings) {
		formantSettingsPanel.loadSettings(formantSettings);
	}
	
	public void showPitchSettings() {
		tabPanel.setSelectedComponent(pitchSettingsPanel);
	}
	
	public void loadPitchSettings(PitchSettings pitchSettings) {
		pitchSettingsPanel.loadSettings(pitchSettings);
	}
	
	public void showIntensitySettings() {
		tabPanel.setSelectedComponent(intensitySettingsPanel);
	}
	
	public void loadIntensitySettings(IntensitySettings intensitySettings) {
		intensitySettingsPanel.loadSettings(intensitySettings);
	}
	
}
