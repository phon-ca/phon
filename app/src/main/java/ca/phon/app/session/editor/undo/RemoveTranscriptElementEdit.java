package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.session.Session;
import ca.phon.session.Transcript;

import javax.swing.undo.CannotUndoException;

public class RemoveTranscriptElementEdit extends AddTranscriptElementEdit {

    public RemoveTranscriptElementEdit(Session session, EditorEventManager editorEventManager, Transcript.Element element, int elementIndex) {
        super(session, editorEventManager, element, elementIndex);
    }

    @Override
    public void undo() throws CannotUndoException {
        super.doIt();
    }

    @Override
    public void doIt() {
        super.undo();
    }

}
