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

import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;

import ca.phon.app.media.TimeComponent;
import ca.phon.app.media.TimeUIModel;

/**
 * Extension point for waveform view tiers.
 *
 */
public abstract class SpeechAnalysisTier extends TimeComponent {
	
	private SpeechAnalysisEditorView speechAnalysisView;
	
	private JPanel contentPane;
	
	public SpeechAnalysisTier(SpeechAnalysisEditorView parentView) {
		super(parentView.getTimeModel());
		this.speechAnalysisView = parentView;
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		contentPane = new JPanel();
		add(contentPane, BorderLayout.CENTER);
	}
	
	public SpeechAnalysisEditorView getParentView() {
		return this.speechAnalysisView;
	}
	
	public JPanel getContentPane() {
		return this.contentPane;
	}
	
	public void setContentPane(JPanel contentPane) {
		var oldVal = this.contentPane;
		remove(this.contentPane);
		this.contentPane = contentPane;
		if(contentPane != null)
			add(contentPane, BorderLayout.CENTER);
		revalidate();
		super.firePropertyChange("contentPane", oldVal, contentPane);
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
	
}
