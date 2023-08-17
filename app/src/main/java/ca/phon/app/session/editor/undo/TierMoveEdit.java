package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.TierViewItem;

import java.util.ArrayList;
import java.util.List;

public class TierMoveEdit extends SessionEditorUndoableEdit {

    private final TierViewItem tierViewItem;

    private final int oldViewIndex;

    private final int newViewIndex;

    public TierMoveEdit(SessionEditor editor, TierViewItem tierViewItem, int newViewIndex) {
        super(editor);
        this.tierViewItem = tierViewItem;
        this.oldViewIndex = editor.getSession().getTierView().indexOf(tierViewItem);
        this.newViewIndex = newViewIndex;
    }

    @Override
    public String getPresentationName() {
        return "Move tier";
    }

    @Override
    public void undo() {
        final List<TierViewItem> oldView = new ArrayList<>(getEditor().getSession().getTierView());
        final List<TierViewItem> newView = new ArrayList<>(oldView);
        newView.remove(tierViewItem);
        newView.add(oldViewIndex, tierViewItem);
        getEditor().getSession().setTierView(newView);
        final EditorEvent<EditorEventType.TierViewChangedData> ee =
                new EditorEvent<>(EditorEventType.TierViewChanged, getSource(),
                        new EditorEventType.TierViewChangedData(newView, oldView,
                                EditorEventType.TierViewChangeType.MOVE_TIER,
                                List.of(tierViewItem.getTierName()), List.of(newViewIndex, oldViewIndex)));
        getEditor().getEventManager().queueEvent(ee);
    }

    @Override
    public void doIt() {
        final List<TierViewItem> oldView = new ArrayList<>(getEditor().getSession().getTierView());
        final List<TierViewItem> newView = new ArrayList<>(oldView);
        newView.remove(tierViewItem);
        newView.add(newViewIndex, tierViewItem);
        getEditor().getSession().setTierView(newView);
        final EditorEvent<EditorEventType.TierViewChangedData> ee =
                new EditorEvent<>(EditorEventType.TierViewChanged, getSource(),
                        new EditorEventType.TierViewChangedData(newView, oldView,
                                EditorEventType.TierViewChangeType.MOVE_TIER,
                                List.of(tierViewItem.getTierName()), List.of(oldViewIndex, newViewIndex)));
        getEditor().getEventManager().queueEvent(ee);
    }

}
