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
package ca.phon.app.session.editor.view.ipa_lookup.actions;

import java.awt.event.*;

import javax.swing.undo.*;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.view.ipa_lookup.*;
import ca.phon.ipadictionary.*;
import ca.phon.session.*;
import ca.phon.worker.*;

/**
 * Action for auto-transcribing a {@link Session} using the
 * current {@link IPADictionary}.
 */
public class AutoTranscribeCommand extends IPALookupViewAction {
	
	private static final long serialVersionUID = 106655469525258379L;

	private final static String CMD_NAME = "Auto-transcribe Session";
	
	// TODO icon?
	
	// TODO keystroke?

	public AutoTranscribeCommand(IPALookupView view) {
		super(view);
		
		putValue(NAME, CMD_NAME);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final SessionEditor sessionEditor = getLookupView().getEditor();
		final AutoTranscriptionDialog autoTranscribeDialog = 
				new AutoTranscriptionDialog(sessionEditor.getProject(), sessionEditor.getSession());
		autoTranscribeDialog.setModal(true);
		
		autoTranscribeDialog.pack();
		autoTranscribeDialog.setLocationRelativeTo(sessionEditor);
		autoTranscribeDialog.setVisible(true);
		
		if(!autoTranscribeDialog.wasCanceled()) {
			// perform auto transcription
			final PhonTask task = new PhonTask() {
				
				@Override
				public void performTask() {
					setStatus(TaskStatus.RUNNING);
					setProperty(PhonTask.PROGRESS_PROP, -1f);
					
					final AutoTranscriber transcriber = new AutoTranscriber(sessionEditor);
					transcriber.setDictionary(getLookupView().getLookupContext().getDictionary());
					transcriber.setOverwrite(autoTranscribeDialog.getForm().isOverwrite());
					transcriber.setSetIPAActual(autoTranscribeDialog.getForm().isSetIPAActual());
					transcriber.setSetIPATarget(autoTranscribeDialog.getForm().isSetIPATarget());
					transcriber.setRecordFilter(autoTranscribeDialog.getForm().getRecordFilter());
					transcriber.setSyllabifier(autoTranscribeDialog.getForm().getSyllabifier());
					transcriber.setTranscriber(getLookupView().getEditor().getDataModel().getTranscriber());
					
					final UndoableEdit edit = transcriber.transcribeSession(sessionEditor.getSession());
					sessionEditor.getUndoSupport().postEdit(edit);
					
					final EditorEvent ee = new EditorEvent(EditorEventType.RECORD_REFRESH_EVT);
					sessionEditor.getEventManager().queueEvent(ee);
					
					setStatus(TaskStatus.FINISHED);
				}
			};
			getLookupView().getEditor().getStatusBar().watchTask(task); 
			
			PhonWorker worker = PhonWorker.createWorker();
			worker.invokeLater(task);
			worker.setFinishWhenQueueEmpty(true);
			worker.start();
		}
	}
	
}
