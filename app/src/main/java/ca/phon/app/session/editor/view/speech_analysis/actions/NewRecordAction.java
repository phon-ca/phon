package ca.phon.app.session.editor.view.speech_analysis.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.AddRecordEdit;
import ca.phon.app.session.editor.view.speech_analysis.SpeechAnalysisEditorView;
import ca.phon.session.MediaSegment;
import ca.phon.session.Record;
import ca.phon.session.SessionFactory;

public class NewRecordAction extends SpeechAnalysisEditorViewAction {

	private static final long serialVersionUID = 1773651812539774012L;

	private final static String TXT = "New record from selection";
	
	private final static String DESC = "Create a new record after current using selection as segment time";
	
	public NewRecordAction(SessionEditor editor, SpeechAnalysisEditorView view) {
		super(editor, view);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		if(!getView().getWavDisplay().hasSelection()) return;
		
		final Record currentRecord = getEditor().currentRecord();
		
		final float startTime = getView().getWavDisplay().getSelectionStart();
		final float endTime = startTime + getView().getWavDisplay().getSelectionLength();
		
		final SessionFactory factory = SessionFactory.newFactory();
		final Record record = factory.createRecord();
		record.setSpeaker(currentRecord.getSpeaker());
		record.addGroup();
		
		final MediaSegment segment = factory.createMediaSegment();
		segment.setStartValue(startTime*1000.0f);
		segment.setEndValue(endTime*1000.0f);
		
		record.getSegment().setGroup(0, segment);
		
		final AddRecordEdit edit = new AddRecordEdit(getEditor(), record, getEditor().getCurrentRecordIndex()+1);
		getEditor().getUndoSupport().postEdit(edit);
	}

}
