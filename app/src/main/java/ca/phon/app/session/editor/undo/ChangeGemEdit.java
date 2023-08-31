package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.session.Gem;
import ca.phon.session.Session;

import javax.swing.undo.CannotUndoException;

public class ChangeGemEdit extends SessionUndoableEdit {

    private final Gem gem;
    private final int elementIndex;
    private final String oldLabel;
    private final String newLabel;

    public ChangeGemEdit(Session session, EditorEventManager editorEventManager, Gem gem, String newLabel) {
        super(session, editorEventManager);
        this.gem = gem;
        this.elementIndex = session.getTranscript().getElementIndex(gem);
        this.oldLabel = gem.getLabel();
        this.newLabel = newLabel;
    }

    @Override
    public String getPresentationName() {
        return "change gem";
    }

    @Override
    public void undo() throws CannotUndoException {
        this.gem.setLabel(this.oldLabel);

        final EditorEvent<EditorEventType.GemChangedData> ee =
                new EditorEvent<>(EditorEventType.GemChanged, getSource(),
                        new EditorEventType.GemChangedData(gem, elementIndex, newLabel, oldLabel));
        getEditorEventManager().queueEvent(ee);
    }

    @Override
    public void doIt() {
        this.gem.setLabel(this.newLabel);

        final EditorEvent<EditorEventType.GemChangedData> ee =
                new EditorEvent<>(EditorEventType.GemChanged, getSource(),
                        new EditorEventType.GemChangedData(gem, elementIndex, oldLabel, newLabel));
        getEditorEventManager().queueEvent(ee);
    }
}
