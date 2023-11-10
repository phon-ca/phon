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
package ca.phon.app.session.editor.autotranscribe;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.actions.SessionEditorAction;
import ca.phon.app.session.editor.autotranscribe.AutoTranscriber;
import ca.phon.app.session.editor.autotranscribe.AutoTranscriptionDialog;
import ca.phon.app.session.editor.view.ipa_lookup.*;
import ca.phon.app.session.editor.view.ipa_lookup.actions.IPALookupViewAction;
import ca.phon.ipadictionary.*;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.session.Transcriber;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.util.Language;
import ca.phon.worker.*;

import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;

/**
 * Action for auto-transcribing a {@link Session} using the
 * current {@link IPADictionary}.
 */
public class AutoTranscribeCommand extends HookableAction {
	
	private final static String CMD_NAME = "Auto-transcribe IPA tiers...";

	private final static String DESC = "Automatically transcribe IPA tiers in session";

	private final Project project;

	private final Session session;

	private final EditorEventManager eventManager;

	private final UndoableEditSupport undoSupport;

	private final Transcriber transcriber;

	/**
	 * Constructor
	 *
	 * @param project may be null, if present additional filter options may be available
	 * @param session
	 * @param editorEventManager
	 */
	public AutoTranscribeCommand(Project project, Session session, EditorEventManager editorEventManager,
								 UndoableEditSupport undoSupport, Transcriber transcriber) {
		super();

		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, DESC);

		this.project = project;
		this.session = session;
		this.eventManager = editorEventManager;
		this.undoSupport = undoSupport;
		this.transcriber = transcriber;
	}

	private Language getDictionaryLang(Session session) {
		Language retVal = IPADictionaryLibrary.getInstance().getDefaultLanguage();
		if(!session.getLanguages().isEmpty()) {
			final Language primaryLanguage = session.getLanguages().get(0);
			if(IPADictionaryLibrary.getInstance().availableLanguages().contains(primaryLanguage)) {
				retVal = primaryLanguage;
			} else {
				if(primaryLanguage.getUserIDs().length > 0) {
					// attempt to find dictionary for root language
					Language rootLang = Language.parseLanguage(primaryLanguage.getPrimaryLanguage().getId());
					if(IPADictionaryLibrary.getInstance().availableLanguages().contains(rootLang)) {
						retVal = rootLang;
					}
				}
			}
		}
		return retVal;
	}

	public Project getProject() {
		return project;
	}

	public Session getSession() {
		return session;
	}

	public EditorEventManager getEventManager() {
		return eventManager;
	}

	public UndoableEditSupport getUndoSupport() {
		return undoSupport;
	}

	public Transcriber getTranscriber() {
		return transcriber;
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final AutoTranscriptionDialog autoTranscribeDialog =
				new AutoTranscriptionDialog(getProject(), getSession(), getDictionaryLang(session));
		autoTranscribeDialog.setModal(true);
		
		autoTranscribeDialog.pack();
		autoTranscribeDialog.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
		autoTranscribeDialog.setVisible(true);
		
		if(!autoTranscribeDialog.wasCanceled()) {
			// perform auto transcription
			final PhonTask task = new PhonTask() {
				
				@Override
				public void performTask() {
					setStatus(TaskStatus.RUNNING);
					setProperty(PhonTask.PROGRESS_PROP, -1f);

					Optional<IPADictionary> dict =
							IPADictionaryLibrary.getInstance().dictionariesForLanguage(autoTranscribeDialog.getForm().getDictionaryLanguage()).stream().findAny();

					final AutoTranscriber transcriber = new AutoTranscriber(getSession(), getEventManager());
					transcriber.setDictionary(dict.orElse(IPADictionaryLibrary.getInstance().defaultDictionary()));
					transcriber.setOverwrite(autoTranscribeDialog.getForm().isOverwrite());
					transcriber.setSetIPAActual(autoTranscribeDialog.getForm().isSetIPAActual());
					transcriber.setSetIPATarget(autoTranscribeDialog.getForm().isSetIPATarget());
					transcriber.setRecordFilter(autoTranscribeDialog.getForm().getRecordFilter());
					transcriber.setSyllabifier(autoTranscribeDialog.getForm().getSyllabifier());
					transcriber.setTranscriber(getTranscriber());
					
					final UndoableEdit edit = transcriber.transcribeSession(getSession());
					getUndoSupport().postEdit(edit);

//					final EditorEvent<EditorEventType.RecordChangedData> ee =
//							new EditorEvent<>(EditorEventType.RecordRefresh, autoTranscribeDialog,
//									new EditorEventType.RecordChangedData(sessionEditor.currentRecord(),
//											sessionEditor.getSession().getRecordElementIndex(sessionEditor.getCurrentRecordIndex()),
//											sessionEditor.getCurrentRecordIndex()));
//					sessionEditor.getEventManager().queueEvent(ee);
					
					setStatus(TaskStatus.FINISHED);
				}
			};
//			getLookupView().getEditor().getStatusBar().watchTask(task);
			
			PhonWorker worker = PhonWorker.createWorker();
			worker.invokeLater(task);
			worker.setFinishWhenQueueEmpty(true);
			worker.start();
		}
	}
	
}
