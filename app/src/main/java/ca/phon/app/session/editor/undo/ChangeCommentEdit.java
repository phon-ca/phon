package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.session.Comment;
import ca.phon.session.Session;
import ca.phon.session.tierdata.TierData;

import javax.swing.undo.CannotUndoException;

public class ChangeCommentEdit extends SessionUndoableEdit {

    private final Comment comment;
    private final int elementIndex;
    private final TierData oldValue;
    private final TierData newValue;

    public ChangeCommentEdit(Session session, EditorEventManager editorEventManager, Comment comment, TierData newValue) {
        super(session, editorEventManager);
        this.comment = comment;
        this.elementIndex = session.getTranscript().getElementIndex(comment);
        this.oldValue = comment.getValue();
        this.newValue = newValue;
    }

    @Override
    public String getPresentationName() {
        return "change comment";
    }

    @Override
    public void undo() throws CannotUndoException {
        this.comment.setValue(oldValue);

        final EditorEvent<EditorEventType.CommentChangedData> ee =
                new EditorEvent<>(EditorEventType.CommentChanged, getSource(),
                        new EditorEventType.CommentChangedData(comment, elementIndex, newValue, oldValue));
        getEditorEventManager().queueEvent(ee);
    }

    @Override
    public void doIt() {
        comment.setValue(newValue);

        final EditorEvent<EditorEventType.CommentChangedData> ee =
                new EditorEvent<>(EditorEventType.CommentChanged, getSource(),
                        new EditorEventType.CommentChangedData(comment, elementIndex, oldValue, newValue));
        getEditorEventManager().queueEvent(ee);
    }

}
