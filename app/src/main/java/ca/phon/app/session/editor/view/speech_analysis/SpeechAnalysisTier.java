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

import ca.phon.media.TimeComponent;

import javax.swing.*;

/**
 * Extension point for waveform view tiers.
 *
 */
public abstract class SpeechAnalysisTier extends TimeComponent {
	
	private SpeechAnalysisEditorView speechAnalysisView;
	
	public SpeechAnalysisTier(SpeechAnalysisEditorView parentView) {
		super(parentView.getTimeModel());
		this.speechAnalysisView = parentView;
	}
	
	public SpeechAnalysisEditorView getParentView() {
		return this.speechAnalysisView;
	}
	
	/**
	 * Add custom commands to the editor view menu.
	 * 
	 * @param menu
	 * @param includeAccelerators
	 */
	public abstract void addMenuItems(JMenu menuEle, boolean includeAccelerators);
	
	/**
	 * Called on the Refresh action for the tier.
	 * 
	 */
	public abstract void onRefresh();
	
	/**
	 * Should this tier be displayed?
	 * 
	 * @return <code>true</code> if tier should be visible, <code>false</code> otherwise
	 */
	public boolean shouldShow() {
		return true;
	}
	
}
