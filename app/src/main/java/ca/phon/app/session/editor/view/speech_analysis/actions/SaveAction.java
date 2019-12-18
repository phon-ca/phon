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
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.SessionMediaModel;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.speech_analysis.SpeechAnalysisEditorView;
import ca.phon.media.ExportSegment;
import ca.phon.media.LongSound;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogListener;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class SaveAction extends SpeechAnalysisEditorViewAction {

	private static final long serialVersionUID = -8249184983712169161L;

	private final static String CMD_NAME = "Save...";
	
	private final static String SHORT_DESC = "Save segment/selection";
	
	private final static ImageIcon ICON =
			IconManager.getInstance().getIcon("actions/filesave", IconSize.SMALL);
	
	public SaveAction(SessionEditor editor, SpeechAnalysisEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(SMALL_ICON, ICON);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setParentWindow(getEditor());
		props.setFileFilter(FileFilter.wavFilter);
		props.setTitle("Save segment");
		props.setPrompt("Save");
		props.setCanCreateDirectories(true);
		props.setRunAsync(true);
		props.setListener(saveListener);
		
		NativeDialogs.showSaveDialog(props);
	}
	
	private void exportSegment(File file) throws IOException {
		SessionMediaModel mediaModel = getEditor().getMediaModel();
		if(!mediaModel.isSessionAudioAvailable()) return;
		
		LongSound sharedSound = mediaModel.getSharedSessionAudio();
		if(sharedSound == null) return;
		
		ExportSegment exportSegment = sharedSound.getExtension(ExportSegment.class);
		if(exportSegment == null) return;
		
		float startTime = 0.0f;
		float endTime = 0.0f;
		
		if(getView().getSelectionInterval() != null) {
			startTime = getView().getSelectionInterval().getStartMarker().getTime();
			endTime = getView().getSelectionInterval().getEndMarker().getTime();
		} else if(getView().getCurrentRecordInterval() != null) {
			startTime = getView().getCurrentRecordInterval().getStartMarker().getTime();
			endTime = getView().getCurrentRecordInterval().getEndMarker().getTime();
		}
		
		exportSegment.exportSegment(file, startTime, endTime);
	}

	private final NativeDialogListener saveListener = new NativeDialogListener() {
		
		@Override
		public void nativeDialogEvent(NativeDialogEvent event) {
			if(event.getDialogResult() == NativeDialogEvent.OK_OPTION) {
				try {
					exportSegment(new File(event.getDialogData().toString()));
				} catch (IOException e) {
					LogUtil.severe(e);
					Toolkit.getDefaultToolkit().beep();
				}
			}
		}
		
	};
	
}
