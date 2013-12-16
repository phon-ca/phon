package ca.phon.app.session.editor.undo;

import java.lang.ref.WeakReference;

import javax.swing.undo.CannotUndoException;

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Participant;
import ca.phon.session.Record;

public class ChangeSpeakerEdit extends SessionEditorUndoableEdit {

	private final Record record;
	
	private final Participant speaker;
	
	private final Participant oldSpeaker;
	
	public ChangeSpeakerEdit(SessionEditor editor, Record record, Participant speaker) {
		super(editor);
		this.record = record;
		oldSpeaker = record.getSpeaker();
		this.speaker = speaker;
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo change record speaker";
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo change record speaker";
	}

	@Override
	public void undo() throws CannotUndoException {
		record.setSpeaker(oldSpeaker);
		
		queueEvent(EditorEventType.RECORD_CHANGED_EVT, getEditor().getUndoSupport(), record);
	}

	@Override
	public void doIt() {
		record.setSpeaker(speaker);
		
		queueEvent(EditorEventType.RECORD_CHANGED_EVT, getSource(), record);
	}

}
