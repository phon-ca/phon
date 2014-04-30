package ca.phon.app.session.editor.view.session_information.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.CompoundEdit;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.ChangeSpeakerEdit;
import ca.phon.app.session.editor.undo.RemoveParticipantEdit;
import ca.phon.app.session.editor.view.session_information.SessionInfoEditorView;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;

public class DeleteParticipantAction extends SessionInfoAction {
	
	private static final long serialVersionUID = -1353836110650283444L;

	private final Participant participant;

	public DeleteParticipantAction(SessionEditor editor,
			SessionInfoEditorView view, Participant participant) {
		super(editor, view);
		this.participant = participant;
		
		putValue(NAME, "Delete " + participant.getName());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		final CompoundEdit edit = new CompoundEdit();
		final RemoveParticipantEdit removePartEdit = new RemoveParticipantEdit(editor, participant);
		removePartEdit.doIt();
		edit.addEdit(removePartEdit);
		
		for(Record r:session.getRecords()) {
			if(r.getSpeaker() == participant) {
				final ChangeSpeakerEdit chSpeakerEdit = new ChangeSpeakerEdit(editor, r, null);
				chSpeakerEdit.doIt();
				edit.addEdit(chSpeakerEdit);
			}
		}
		edit.end();
		
		editor.getUndoSupport().postEdit(edit);
	}

}
