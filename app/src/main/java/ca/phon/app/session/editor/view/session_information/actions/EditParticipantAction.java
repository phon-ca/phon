package ca.phon.app.session.editor.view.session_information.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.AddParticipantEdit;
import ca.phon.app.session.editor.undo.ParticipantUndoableEdit;
import ca.phon.app.session.editor.view.session_information.SessionInfoEditorView;
import ca.phon.session.Participant;
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
	public void actionPerformed(ActionEvent e) {
		final SessionFactory factory = SessionFactory.newFactory();
		final Participant part = factory.createParticipant();
		copyParticipantInfo(participant, part);
		boolean canceled = ParticipantEditor.editParticipant(getEditor(), part, 
				getEditor().getDataModel().getSession().getDate());
		
		if(!canceled) {
			final ParticipantUndoableEdit edit = new ParticipantUndoableEdit(getEditor(), participant, part);
			getEditor().getUndoSupport().postEdit(edit);
		}
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
