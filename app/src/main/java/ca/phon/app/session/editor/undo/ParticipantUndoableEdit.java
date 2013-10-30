package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Participant;
import ca.phon.session.Session;

/**
 * {@link Session} edits involving particpants.
 */
public abstract class ParticipantUndoableEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = -5312599132320247077L;

	/**
	 * Participant involved in the edit
	 */
	private Participant participant;
	
	public ParticipantUndoableEdit(SessionEditor editor, Participant participant) {
		super(editor);
		this.participant = participant;
	}
	
	public Participant getParticipant() {
		return this.participant;
	}
	
}
