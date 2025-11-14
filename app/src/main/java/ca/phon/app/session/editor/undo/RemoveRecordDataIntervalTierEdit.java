package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.session.Session;

public class RemoveRecordDataIntervalTierEdit extends AddRecordDataIntervalTierEdit {

    public RemoveRecordDataIntervalTierEdit(Session session, EditorEventManager eventManager, String tierName) {
        super(session, eventManager, tierName);
    }

    @Override
    public void undo() {
        super.doIt();
    }

    @Override
    public void doIt() {
        super.undo();
    }

}
