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

import ca.phon.app.media.TimeUIModel;
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
	
	private final static float MIN_PXPERS = 1.0f;
	
	private final static float MAX_PXPERS = 3200.0f;

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
		TimeUIModel timeModel = getView().getTimeModel();
		
		float pxPerS = timeModel.getPixelsPerSecond();
		float zoomAmount = -1.0f * pxPerS * this.zoomAmount;
		if(zoomIn) {
			zoomAmount *= -1.0f;
		}
		pxPerS += zoomAmount;
		
		pxPerS = Math.max(MIN_PXPERS, Math.min(pxPerS, MAX_PXPERS));
		
		// beep to indicate we are at a limit
		if(pxPerS == MIN_PXPERS || pxPerS == MAX_PXPERS)
			Toolkit.getDefaultToolkit().beep();
		
		timeModel.setPixelsPerSecond(pxPerS);
	}

}
