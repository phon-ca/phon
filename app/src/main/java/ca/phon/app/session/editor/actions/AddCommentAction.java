package ca.phon.app.session.editor.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Comment;
import ca.phon.session.SessionFactory;

import java.awt.event.ActionEvent;

public class AddCommentAction extends SessionEditorAction {

    private final static String TXT = "Add comment";

    private final static String DESC = "Add comment to session transcript";

    private final Comment comment;

    private final int elementIndex;

    /**
     * Add a new generic empty comment to the end of the transcript
     *
     * @param editor
     */
    public AddCommentAction(SessionEditor editor) {
        this(editor, SessionFactory.newFactory().createComment());
    }

    public AddCommentAction(SessionEditor editor, Comment comment) {
        this(editor, comment, editor.getSession().getTranscript().getNumberOfElements());
    }

    public AddCommentAction(SessionEditor editor, Comment comment, int elementIndex) {
        super(editor);
        this.comment = comment;
        this.elementIndex = elementIndex;
    }

    @Override
    public void hookableActionPerformed(ActionEvent ae) {

    }

}
