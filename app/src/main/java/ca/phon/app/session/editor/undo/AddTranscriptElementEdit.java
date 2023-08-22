package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.session.Session;
import ca.phon.session.Transcript;

import javax.swing.undo.CannotUndoException;

public class AddTranscriptElementEdit extends SessionUndoableEdit {

    private final Transcript.Element element;

    private final int elementIndex;

    public AddTranscriptElementEdit(Session session, EditorEventManager editorEventManager, Transcript.Element element, int elementIndex) {
        super(session, editorEventManager);
        this.element = element;
        this.elementIndex = elementIndex;
    }

    @Override
    public String getPresentationName() {
        String elementType = element.isComment() ? "comment" : element.isGem() ? "gem" : "record";
        return "add " + elementType;
    }

    @Override
    public void undo() throws CannotUndoException {
        getSession().getTranscript().removeElement(this.elementIndex);
        fireUndoEvents();
    }

    private void fireUndoEvents() {
        final EditorEvent<EditorEventType.ElementDeletedData> elementDeletedEvt =
                new EditorEvent<>(EditorEventType.ElementDeleted, getSource(),
                        new EditorEventType.ElementDeletedData(this.element, this.elementIndex));
        getEditorEventManager().queueEvent(elementDeletedEvt);

        if(this.element.isComment()) {
            final EditorEvent<EditorEventType.CommentDeletedData> commentDeletedEvt =
                    new EditorEvent<>(EditorEventType.CommentDeleted, getSource(),
                            new EditorEventType.CommentDeletedData(this.element.asComment(), this.elementIndex));
            getEditorEventManager().queueEvent(commentDeletedEvt);
        } else if(this.element.isGem()) {
            final EditorEvent<EditorEventType.GemDeletedData> gemDeletedEvt =
                    new EditorEvent<>(EditorEventType.GemDeleted, getSource(),
                            new EditorEventType.GemDeletedData(this.element.asGem(), this.elementIndex));
        } else if(this.element.isRecord()) {
            final EditorEvent<EditorEventType.RecordAddedData> recordAddedEvt =
                    new EditorEvent<>(EditorEventType.RecordAdded, getSource(),
                            new EditorEventType.RecordAddedData(this.element.asRecord(), this.elementIndex, getSession().getRecordPosition(this.element.asRecord())));
            getEditorEventManager().queueEvent(recordAddedEvt);
        }
    }

    private void fireEvents() {
        final EditorEvent<EditorEventType.ElementAddedData> elementAddedEvt =
                new EditorEvent<>(EditorEventType.ElementAdded, getSource(),
                        new EditorEventType.ElementAddedData(this.element, this.elementIndex));
        getEditorEventManager().queueEvent(elementAddedEvt);

        if(this.element.isComment()) {
            final EditorEvent<EditorEventType.CommentAddedData> commentAddedEvt =
                    new EditorEvent<>(EditorEventType.CommentAdded, getSource(),
                            new EditorEventType.CommentAddedData(this.element.asComment(), this.elementIndex));
            getEditorEventManager().queueEvent(commentAddedEvt);
        } else if(this.element.isGem()) {
            final EditorEvent<EditorEventType.GemAddedData> gemAddedEvt =
                    new EditorEvent<>(EditorEventType.GemAdded, getSource(),
                            new EditorEventType.GemAddedData(this.element.asGem(), this.elementIndex));
        } else if(this.element.isRecord()) {
            final EditorEvent<EditorEventType.RecordAddedData> recordAddedEvt =
                    new EditorEvent<>(EditorEventType.RecordAdded, getSource(),
                            new EditorEventType.RecordAddedData(this.element.asRecord(), this.elementIndex, getSession().getRecordPosition(this.element.asRecord())));
            getEditorEventManager().queueEvent(recordAddedEvt);
        }
    }

    @Override
    public void doIt() {
        getSession().getTranscript().addElement(elementIndex, this.element);
        fireEvents();
    }

}
