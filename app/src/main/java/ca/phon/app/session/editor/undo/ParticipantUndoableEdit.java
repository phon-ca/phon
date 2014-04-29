package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Participant;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;

/**
 * {@link Session} edits involving particpants.
 */
public class ParticipantUndoableEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = -5312599132320247077L;

	/**
	 * Participant involved in the edit
	 */
	private Participant participant;
	
	private Participant template;
	
	private Participant oldVals;
	
	public ParticipantUndoableEdit(SessionEditor editor, Participant participant, Participant template) {
		super(editor);
		this.participant = participant;
		this.template = template;
	}
	
	public Participant getParticipant() {
		return this.participant;
	}
	
	@Override
	public void undo() {
		copyParticipantInfo(oldVals, participant);

		final EditorEvent ee = new EditorEvent(EditorEventType.PARTICIPANT_CHANGED, getSource(), participant);
		getEditor().getEventManager().queueEvent(ee);
	}

	@Override
	public void doIt() {
		final SessionFactory factory = SessionFactory.newFactory();
		final Participant p = factory.createParticipant();
		copyParticipantInfo(participant, p);
		oldVals = p;
		
		copyParticipantInfo(template, participant);
		
		final EditorEvent ee = new EditorEvent(EditorEventType.PARTICIPANT_CHANGED, getSource(), participant);
		getEditor().getEventManager().queueEvent(ee);
	}
	
	private void copyParticipantInfo(Participant src, Participant dest) {
		dest.setBirthDate(src.getBirthDate());
		dest.setEducation(src.getEducation());
		dest.setGroup(src.getGroup());
		dest.setLanguage(src.getLanguage());
		dest.setName(src.getName());
		dest.setRole(src.getRole());
		dest.setSES(src.getSES());
		dest.setSex(src.getSex());
	}
}
