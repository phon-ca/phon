package ca.phon.app.session.editor.view.session_information.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.ParticipantUndoableEdit;
import ca.phon.app.session.editor.view.session_information.SessionInfoEditorView;
import ca.phon.session.Participant;
import ca.phon.session.Participants;
import ca.phon.session.Record;
import ca.phon.session.SessionFactory;
import ca.phon.ui.participant.ParticipantEditor;

public class EditParticipantAction extends SessionInfoAction {

	private static final long serialVersionUID = 552410881946559812L;

	private final Participant participant;
	
	public EditParticipantAction(SessionEditor editor,
			SessionInfoEditorView view, Participant participant) {
		super(editor, view);
		this.participant = participant;
		
		putValue(NAME, "Edit " + participant.getName() + "...");
		
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final SessionFactory factory = SessionFactory.newFactory();
		final Participant part = factory.createParticipant();
		Participants.copyParticipantInfo(participant, part);
		
		boolean canceled = ParticipantEditor.editParticipant(getEditor(), part, 
				getEditor().getDataModel().getSession().getDate(),
				getEditor().getDataModel().getSession().getParticipants().otherParticipants(participant));
		
		if(!canceled) {
			if(!participant.getId().equals(part.getId())) {
				// XXX we need to ensure that every record is loaded 
				// so that participant information changes when id is modified
				for(Record r:getEditor().getSession().getRecords()) {
					r.getSpeaker();
				}
			}
			final ParticipantUndoableEdit edit = new ParticipantUndoableEdit(getEditor(), participant, part);
			getEditor().getUndoSupport().postEdit(edit);
			
			final EditorEvent ee = new EditorEvent(EditorEventType.RECORD_REFRESH_EVT);
			getEditor().getEventManager().queueEvent(ee);
		}
	}

}
