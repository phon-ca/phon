package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.session.IntervalTier;
import ca.phon.session.Session;

/**
 * Undo edit for adding an interval tier to a session.
 *
 */
public class AddIntervalTierEdit extends SessionUndoableEdit {

    private IntervalTier tier;

    public AddIntervalTierEdit(Session session, EditorEventManager editorEventManager, IntervalTier tier) {
        super(session, editorEventManager);
        this.tier = tier;
    }

    @Override
    public void doIt() {
        getSession().getTimeline().addTier(tier);

        final EditorEvent<EditorEventType.TimelineTierAddData> ee =
                new EditorEvent<>(EditorEventType.TimelineTierAdd, getSource(), new EditorEventType.TimelineTierAddData(tier.getName()));
        getEditorEventManager().queueEvent(ee);
    }

    @Override
    public void undo() {
        getSession().getTimeline().removeTier(tier);

        final EditorEvent<EditorEventType.TimelineTierRemoveData> ee =
                new EditorEvent<>(EditorEventType.TimelineTierRemove, getSource(), new EditorEventType.TimelineTierRemoveData(tier.getName()));
        getEditorEventManager().queueEvent(ee);
    }

    @Override
    public String getPresentationName() {
        return "Add interval tier: " + tier.getName();
    }

}
