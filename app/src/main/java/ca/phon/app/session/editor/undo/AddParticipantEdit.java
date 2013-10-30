package ca.phon.app.session.editor.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Participant;
import ca.phon.session.Session;

/**
 * Undo-able edit for adding participants in a {@link Session}.
 *
 */
public class AddParticipantEdit extends ParticipantUndoableEdit {

	private static final long serialVersionUID = 2633079803945025310L;
	
	/**
	 * Constructor
	 */
	public AddParticipantEdit(SessionEditor editor, Participant participant) {
		super(editor, participant);
	}

	@Override
	public boolean canRedo() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		boolean retVal = true;
		for(int i = 0; i < session.getParticipantCount(); i++) {
			final Participant p = session.getParticipant(i);
			if(getParticipant() == p) {
				retVal = false;
				break;
			}
		}
		return retVal;
	}

	@Override
	public boolean canUndo() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		boolean retVal = false;
		for(int i = 0; i < session.getParticipantCount(); i++) {
			final Participant p = session.getParticipant(i);
			if(getParticipant() == p) {
				retVal = true;
				break;
			}
		}
		return retVal;
	}
	
	@Override
	public String getRedoPresentationName() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Redo add participant '");
		builder.append(getParticipant().getName());
		return builder.toString();
	}

	@Override
	public String getUndoPresentationName() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Undo add participant '");
		builder.append(getParticipant().getName());
		return builder.toString();
	}

	@Override
	public boolean isSignificant() {
		return true;
	}
	
	@Override
	public void undo() throws CannotUndoException {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		session.removeParticipant(getParticipant());
		
		queueEvent(EditorEventType.PARTICIPANT_REMOVED, getEditor().getUndoSupport(), getParticipant());
	}

	@Override
	public void doIt() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		session.addParticipant(getParticipant());
		
		queueEvent(EditorEventType.PARTICIPANT_ADDED, getSource(), getParticipant());
	}
	
}
