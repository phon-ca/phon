package ca.phon.app.session.editor.view.ipa_lookup.actions;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.undo.UndoableEdit;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.ipa_lookup.AutoTranscriber;
import ca.phon.app.session.editor.view.ipa_lookup.AutoTranscriptionDialog;
import ca.phon.app.session.editor.view.ipa_lookup.AutoTranscriptionForm;
import ca.phon.app.session.editor.view.ipa_lookup.IPALookupView;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.session.Session;
import ca.phon.worker.PhonWorker;

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
			final Runnable task = new Runnable() {
				
				@Override
				public void run() {
					final AutoTranscriber transcriber = new AutoTranscriber();
					transcriber.setDictionary(getLookupView().getLookupContext().getDictionary());
					transcriber.setOverwrite(autoTranscribeDialog.getForm().isOverwrite());
					transcriber.setSetIPAActual(autoTranscribeDialog.getForm().isSetIPAActual());
					transcriber.setSetIPATarget(autoTranscribeDialog.getForm().isSetIPATarget());
					transcriber.setRecordFilter(autoTranscribeDialog.getForm().getRecordFilter());
					transcriber.setSyllabifier(autoTranscribeDialog.getForm().getSyllabifier());
					transcriber.setTranscriber(getLookupView().getEditor().getDataModel().getTranscriber());
					final UndoableEdit edit = transcriber.transcribeSession(sessionEditor.getSession());
					sessionEditor.getUndoSupport().postEdit(edit);
				}
			};
			PhonWorker.getInstance().invokeLater(task);
		}
	}
	
}
