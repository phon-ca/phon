package ca.phon.app.session.editor.view.transcript;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.Tier;
import ca.phon.session.TierViewItem;
import ca.phon.ui.menu.MenuBuilder;

public interface TierLabelMenuHandler {
    public void addMenuItems(MenuBuilder builder, Session session, EditorEventManager eventManager, SessionEditUndoSupport undoSupport, Tier<?> tier, Record record);
    public void addMenuItems(MenuBuilder builder, Session session, EditorEventManager eventManager, SessionEditUndoSupport undoSupport, TierViewItem item);
}
