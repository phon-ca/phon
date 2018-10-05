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
package ca.phon.app.session.editor.view.speech_analysis.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.speech_analysis.SpeechAnalysisEditorView;
import ca.phon.media.sampled.PCMSegmentView;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class ZoomAction extends SpeechAnalysisEditorViewAction {

	private static final long serialVersionUID = 5911882184540602671L;
	
	private final static String CMD_NAME_ZOOMIN = "Zoom in";
	
	private final static String CMD_NAME_ZOOMOUT = "Zoom out";
	
	private final static ImageIcon ZOOMIN_ICON = 
			IconManager.getInstance().getIcon("actions/zoom-in-3", IconSize.SMALL);
	
	private final static ImageIcon ZOOMOUT_ICON =
			IconManager.getInstance().getIcon("actions/zoom-out-3", IconSize.SMALL);
	
	private final static String ZOOM_AMOUNT_PROP = 
			SpeechAnalysisEditorView.class.getName() + ".zoomAmount";
	private final static float DEFAULT_ZOOM_AMOUNT = 0.3f;
	private float zoomAmount = PrefHelper.getFloat(ZOOM_AMOUNT_PROP, DEFAULT_ZOOM_AMOUNT);
	
	private boolean zoomIn = true;

	public ZoomAction(SessionEditor editor, SpeechAnalysisEditorView view) {
		this(editor, view, true);
	}
	
	public ZoomAction(SessionEditor editor, SpeechAnalysisEditorView view, boolean zoomIn) {
		super(editor, view);
		
		this.zoomIn = zoomIn;
		putValue(NAME, (zoomIn ? CMD_NAME_ZOOMIN : CMD_NAME_ZOOMOUT));
		putValue(SMALL_ICON, (zoomIn ? ZOOMIN_ICON : ZOOMOUT_ICON));
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final PCMSegmentView wavDisplay = getView().getWavDisplay();
		float start = wavDisplay.getWindowStart();
		float len = wavDisplay.getWindowLength();
		
		if(len <= 1.0f && zoomIn) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		
		float end = start + len;
		float endTime = wavDisplay.getSampled().getStartTime() + wavDisplay.getSampled().getLength();
		
		float zoomAmount = this.zoomAmount;
		if(zoomIn) {
			zoomAmount *= -1.0f;
		}
		
		start -= zoomAmount;
		end += zoomAmount;
		
		start = Math.min(Math.max(wavDisplay.getSampled().getStartTime(), start), 
				endTime);
		end = Math.min(endTime, Math.max(wavDisplay.getSampled().getStartTime(), end));
		len = end - start;
		
		if(len < 0.1f && (endTime - start) >= 0.1f) {
			len = 0.1f;
		}
		
		if(len >= 0.1f && len < wavDisplay.getSampled().getLength()) {
			// adjust values
			wavDisplay.setWindowStart(start);
			wavDisplay.setWindowLength(len);
		} else {
			// beep
			Toolkit.getDefaultToolkit().beep();
		}
	}

}
