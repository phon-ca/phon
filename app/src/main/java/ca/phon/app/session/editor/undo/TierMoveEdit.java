package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Session;
import ca.phon.session.TierViewItem;

import java.util.ArrayList;
import java.util.List;

public class TierMoveEdit extends SessionUndoableEdit {

    private final TierViewItem tierViewItem;

    private final int oldViewIndex;

    private final int newViewIndex;

    public TierMoveEdit(SessionEditor editor, TierViewItem tierViewItem, int newViewIndex) {
        this(editor.getSession(), editor.getEventManager(), tierViewItem, newViewIndex);
    }

    public TierMoveEdit(Session session, EditorEventManager editorEventManager, TierViewItem tierViewItem, int newViewIndex) {
        super(session, editorEventManager);
        this.tierViewItem = tierViewItem;
        this.oldViewIndex = getSession().getTierView().indexOf(tierViewItem);
        this.newViewIndex = newViewIndex;
    }

    @Override
    public String getPresentationName() {
        return "Move tier";
    }

    @Override
    public void undo() {
        final List<TierViewItem> oldView = new ArrayList<>(getSession().getTierView());
        final List<TierViewItem> newView = new ArrayList<>(oldView);
        newView.remove(tierViewItem);
        newView.add(oldViewIndex, tierViewItem);
        getSession().setTierView(newView);
        final EditorEvent<EditorEventType.TierViewChangedData> ee =
                new EditorEvent<>(EditorEventType.TierViewChanged, getSource(),
                        new EditorEventType.TierViewChangedData(oldView, newView,
                                EditorEventType.TierViewChangeType.MOVE_TIER,
                                List.of(tierViewItem.getTierName()), List.of(newViewIndex, oldViewIndex)));
        getEditorEventManager().queueEvent(ee);
    }

    @Override
    public void doIt() {
        final List<TierViewItem> oldView = new ArrayList<>(getSession().getTierView());
        final List<TierViewItem> newView = new ArrayList<>(oldView);
        newView.remove(tierViewItem);
        newView.add(newViewIndex, tierViewItem);
        getSession().setTierView(newView);
        final EditorEvent<EditorEventType.TierViewChangedData> ee =
                new EditorEvent<>(EditorEventType.TierViewChanged, getSource(),
                        new EditorEventType.TierViewChangedData(oldView, newView,
                                EditorEventType.TierViewChangeType.MOVE_TIER,
                                List.of(tierViewItem.getTierName()), List.of(oldViewIndex, newViewIndex)));
        getEditorEventManager().queueEvent(ee);
    }

}
