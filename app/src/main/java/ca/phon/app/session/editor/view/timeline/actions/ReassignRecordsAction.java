package ca.phon.app.session.editor.view.timeline.actions;

import ca.phon.app.session.editor.undo.ChangeSpeakerEdit;
import ca.phon.app.session.editor.view.timeline.TimelineView;
import ca.phon.session.*;

import java.awt.event.ActionEvent;

public class ReassignRecordsAction extends TimelineAction {
	
	private Participant fromSpeaker;
	
	private Participant toSpeaker;

	public ReassignRecordsAction(TimelineView view, Participant fromSpeaker, Participant toSpeaker) {
		super(view);
		
		this.fromSpeaker = fromSpeaker;
		this.toSpeaker = toSpeaker;
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {		
		getView().getEditor().getUndoSupport().beginUpdate();
		Session session = getView().getEditor().getSession();
		session.getRecords().forEach( (r) -> {
			if(r.getSpeaker() == fromSpeaker) {
				ChangeSpeakerEdit edit = new ChangeSpeakerEdit(getView().getEditor(), r, toSpeaker);
				getView().getEditor().getUndoSupport().postEdit(edit);
			}
		});
		getView().getEditor().getUndoSupport().endUpdate();
	}
	
}
