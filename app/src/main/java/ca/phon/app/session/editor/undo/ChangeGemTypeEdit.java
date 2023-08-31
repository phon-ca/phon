package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.session.Gem;
import ca.phon.session.GemType;
import ca.phon.session.Session;

import javax.swing.undo.CannotUndoException;

public class ChangeGemTypeEdit extends SessionUndoableEdit {

    private final Gem gem;
    private final int elementIndex;
    private final GemType oldType;
    private final GemType newType;

    public ChangeGemTypeEdit(Session session, EditorEventManager editorEventManager, Gem gem, GemType newType) {
        super(session, editorEventManager);
        this.gem = gem;
        this.elementIndex = session.getTranscript().getElementIndex(gem);
        this.oldType = this.gem.getType();
        this.newType = newType;
    }

    @Override
    public String getPresentationName() {
        return "change gem type";
    }

    @Override
    public void undo() throws CannotUndoException {
        this.gem.setType(this.oldType);

        final EditorEvent<EditorEventType.GemTypeChangedData> ee =
                new EditorEvent<>(EditorEventType.GemTypeChanged, getSource(),
                        new EditorEventType.GemTypeChangedData(gem, elementIndex, newType, oldType));
        getEditorEventManager().queueEvent(ee);
    }

    @Override
    public void doIt() {
        this.gem.setType(this.newType);

        final EditorEvent<EditorEventType.GemTypeChangedData> ee =
                new EditorEvent<>(EditorEventType.GemTypeChanged, getSource(),
                        new EditorEventType.GemTypeChangedData(gem, elementIndex, oldType, newType));
        getEditorEventManager().queueEvent(ee);
    }

}
