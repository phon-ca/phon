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

import ca.phon.media.WaveformDisplay;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.PrefHelper;

import javax.swing.*;
import java.awt.*;

public class SpeechAnalysisWaveformTier extends SpeechAnalysisTier {
	
	private final static String WAV_DISPLAY_HEIGHT = "SpeechAnalysisView.wavDisplayHeight";
	private final int DEFAULT_WAV_DISPLAY_HEIGHT = 100;
	private int wavDisplayHeight =
			PrefHelper.getInt(WAV_DISPLAY_HEIGHT, DEFAULT_WAV_DISPLAY_HEIGHT);
	
	private WaveformDisplay wavDisplay;
	
	public SpeechAnalysisWaveformTier(SpeechAnalysisEditorView parentView) {
		super(parentView);
		
		init();
		setupActionMap();
	}
	
	private void init() {
		wavDisplay = new WaveformDisplay(getTimeModel());
		wavDisplay.setPreferredChannelHeight(50);
		wavDisplay.setTrackViewportHeight(true);
		wavDisplay.setFocusable(true);
		wavDisplay.setBackground(Color.white);
		wavDisplay.setOpaque(true);
		wavDisplay.setFont(FontPreferences.getMonospaceFont());
		
		wavDisplay.addMouseListener(getParentView().getContextMenuAdapter());
		wavDisplay.addMouseListener(getParentView().getCursorAndSelectionAdapter());
		wavDisplay.addMouseMotionListener(getParentView().getCursorAndSelectionAdapter());
		
		setLayout(new BorderLayout());
		add(wavDisplay, BorderLayout.CENTER);
	
		final JSeparator separator =  new SpeechAnalysisTierDivider(wavDisplay);
		add(separator, BorderLayout.SOUTH);
	}
	
	private void setupActionMap() {
		final InputMap inputMap = new InputMap();
		final ActionMap actionMap = wavDisplay.getActionMap();
		
		wavDisplay.setActionMap(actionMap);
		wavDisplay.setInputMap(WHEN_FOCUSED, inputMap);
	}
	
	public WaveformDisplay getWaveformDisplay() {
		return this.wavDisplay;
	}

	@Override
	public void addMenuItems(JMenu menuEle, boolean includeAccelerators) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRefresh() {
	}
	
}
