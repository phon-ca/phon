/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
