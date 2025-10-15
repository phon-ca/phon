package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.session.Session;

/**
 * Undo edit for adding an interval tier to a session.
 *
 */
public class AddIntervalTierEdit extends SessionUndoableEdit {

    private final String tierName;


    public AddIntervalTierEdit(Session session, EditorEventManager editorEventManager, String tierName) {
        super(session, editorEventManager);
        this.tierName = tierName;
    }

    @Override
    public void doIt() {
        getSession().getTimeline().addTier(tierName);

        final EditorEvent<EditorEventType.TimelineTierAddData> ee =
                new EditorEvent<>(EditorEventType.TimelineTierAdd, getSource(), new EditorEventType.TimelineTierAddData(tierName));
        getEditorEventManager().queueEvent(ee);
    }

    @Override
    public void undo() {
        getSession().getTimeline().removeTier(tierName);

        final EditorEvent<EditorEventType.TimelineTierRemoveData> ee =
                new EditorEvent<>(EditorEventType.TimelineTierRemove, getSource(), new EditorEventType.TimelineTierRemoveData(tierName));
        getEditorEventManager().queueEvent(ee);
    }

    @Override
    public String getPresentationName() {
        return "Add interval tier: " + tierName;
    }

}
