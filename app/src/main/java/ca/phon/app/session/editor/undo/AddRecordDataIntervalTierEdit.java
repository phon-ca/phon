package ca.phon.app.session.editor.undo;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Session;
import ca.phon.session.TierDescription;

public class AddRecordDataIntervalTierEdit extends SessionUndoableEdit {

    private final String tierName;

    public AddRecordDataIntervalTierEdit(SessionEditor editor, String tierName) {
        this(editor.getSession(), editor.getEventManager(), tierName);
    }

    public AddRecordDataIntervalTierEdit(Session session, EditorEventManager editorEventManager, String tierName) {
        super(session, editorEventManager);
        this.tierName = tierName;
    }

    @Override
    public String getPresentationName() {
        return "Add record data interval tier: " + tierName;
    }

    @Override
    public void doIt() {
        final TierDescription td = getSession().getTier(tierName);
        if(td == null) {
            LogUtil.warning("Tier '" + tierName + "' not found in session");
            return;
        }

        getSession().getTimeline().addRecordIntervalTier(tierName);

        final EditorEvent<EditorEventType.RecordDataIntervalTierAddData> ee =
                new EditorEvent<>(EditorEventType.RecordDataIntervalTierAdd, getSource(), new EditorEventType.RecordDataIntervalTierAddData(tierName));
        getEditorEventManager().queueEvent(ee);
    }

    @Override
    public void undo() {
        if(getSession().getTimeline().removeRecordIntervalTier(tierName)) {
            final EditorEvent<EditorEventType.RecordDataIntervalTierRemoveData> ee =
                    new EditorEvent<>(EditorEventType.RecordDataIntervalTierRemove, getSource(), new EditorEventType.RecordDataIntervalTierRemoveData(tierName));
            getEditorEventManager().queueEvent(ee);
        }
    }
}
