package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.session.Comment;
import ca.phon.session.CommentType;
import ca.phon.session.Session;

import javax.swing.undo.CannotUndoException;

public class ChangeCommentTypeEdit extends SessionUndoableEdit {

    private final Comment comment;
    private final int elementIndex;
    private final CommentType oldType;
    private final CommentType newType;

    public ChangeCommentTypeEdit(Session session, EditorEventManager editorEventManager, Comment comment, CommentType commentType) {
        super(session, editorEventManager);
        this.comment = comment;
        this.elementIndex = session.getTranscript().getElementIndex(comment);
        this.oldType = comment.getType();
        this.newType = commentType;
    }

    @Override
    public String getPresentationName() {
        return "change comment type";
    }

    @Override
    public void undo() throws CannotUndoException {
        comment.setType(this.oldType);

        final EditorEvent<EditorEventType.CommentTypeChangedData> ee =
                new EditorEvent<>(EditorEventType.CommenTypeChanged, getSource(),
                        new EditorEventType.CommentTypeChangedData(comment, elementIndex, newType, oldType));
        getEditorEventManager().queueEvent(ee);
    }

    @Override
    public void doIt() {
        comment.setType(this.newType);

        final EditorEvent<EditorEventType.CommentTypeChangedData> ee =
                new EditorEvent<>(EditorEventType.CommenTypeChanged, getSource(),
                        new EditorEventType.CommentTypeChangedData(comment, elementIndex, oldType, newType));
        getEditorEventManager().queueEvent(ee);
    }

}
