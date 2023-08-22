package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.session.Session;
import ca.phon.session.Transcript;

public class MoveTranscriptElementEdit extends SessionUndoableEdit {

    private final Transcript.Element element;
    private int oldElementIndex = -1;
    private int oldRecordIndex = -1;
    private final int newElementIndex;

    public MoveTranscriptElementEdit(Session session, EditorEventManager editorEventManager,
                                     Transcript.Element element, int newElementIndex) {
        super(session, editorEventManager);
        this.element = element;
        this.newElementIndex = newElementIndex;
    }

    private void fireMoveEvents(int oldElementIndex, int newElementIndex) {
        final EditorEvent<EditorEventType.ElementMovedData> elementMovedEvt =
                new EditorEvent<>(EditorEventType.ElementMoved, getSource(),
                        new EditorEventType.ElementMovedData(this.element, oldElementIndex, newElementIndex));
        getEditorEventManager().queueEvent(elementMovedEvt);

        if(this.element.isComment()) {
            final EditorEvent<EditorEventType.CommentMovedData> commentMovedEvt =
                    new EditorEvent<>(EditorEventType.CommentMoved, getSource(),
                            new EditorEventType.CommentMovedData(this.element.asComment(), oldElementIndex, newElementIndex));
            getEditorEventManager().queueEvent(commentMovedEvt);
        } else if(this.element.isGem()) {
            final EditorEvent<EditorEventType.GemMovedData> gemMovedEvt =
                    new EditorEvent<>(EditorEventType.GemMoved, getSource(),
                            new EditorEventType.GemMovedData(this.element.asGem(), oldElementIndex, newElementIndex));
            getEditorEventManager().queueEvent(gemMovedEvt);
        } else if(this.element.isRecord()) {
            final int recordIndex = getSession().getRecordPosition(this.element.asRecord());
            final EditorEvent<EditorEventType.RecordMovedData> recordMovedEvt =
                    new EditorEvent<>(EditorEventType.RecordMoved, getSource(),
                            new EditorEventType.RecordMovedData(this.element.asRecord(), this.oldElementIndex, this.oldRecordIndex, this.newElementIndex, recordIndex));
            getEditorEventManager().queueEvent(recordMovedEvt);
        }
    }

    @Override
    public void doIt() {
        this.oldElementIndex = getSession().getTranscript().getElementIndex(this.element);
        if(this.element.isRecord())
            oldRecordIndex = getSession().getTranscript().getRecordPosition(this.element.asRecord());
        getSession().getTranscript().removeElement(this.oldElementIndex);
        int addIndex = newElementIndex > oldElementIndex ? newElementIndex - 1 : newElementIndex;
        getSession().getTranscript().addElement(addIndex, this.element);
        fireMoveEvents(this.oldElementIndex, this.newElementIndex);
    }

}
