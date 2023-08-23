package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.session.Session;

import javax.swing.undo.CannotUndoException;

public class TierBlindEdit extends SessionUndoableEdit {

    private final String tierName;

    private final boolean blind;

    public TierBlindEdit(Session session, EditorEventManager editorEventManager, String tierName, boolean blind) {
        super(session, editorEventManager);
        this.tierName = tierName;
        this.blind = blind;
    }

    @Override
    public void undo() throws CannotUndoException {
        // TODO
    }

    @Override
    public void doIt() {
        // TODO
    }

}
