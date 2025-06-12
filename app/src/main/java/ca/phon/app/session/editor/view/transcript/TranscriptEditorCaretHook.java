package ca.phon.app.session.editor.view.transcript;

/**
 * Actions which may be performed before the caret is moved and may
 * prevent the caret from moving.
 */
public interface TranscriptEditorCaretHook {

    /**
     * Called before the caret is moved.
     *
     * @param oldDot the old caret position
     * @param newDot the new caret position to move to
     * @return true if the caret should be moved, false otherwise
     */
    public boolean beforeSetDot(int oldDot, int newDot);

    /**
     * Called after the caret is moved.
     *
     * @param oldDot the old caret position
     * @param newDot the new caret position
     * @param editor the transcript editor
     */
    public void afterSetDot(int oldDot, int newDot);

    /**
     * Called when the caret is frozen or unfrozen.
     *
     * @param frozen true if the caret is frozen, false if unfrozen
     */
    public void caretFrozen(boolean frozen);

    /**
     * Called when the caret is moved to a new location.
     *
     * @param dot the previous location of the caret
     * @param moveTo the move location of the caret
     *
     * @return true if the caret should be moved, false otherwise
     */
    public boolean beforeMoveCaret(int dot, int moveTo);

    /**
     * Called when the caret is moved to a new location.
     *
     * @param dot the previous location of the caret
     * @param moveTo the move location of the caret
     */
    public void afterMoveCaret(int dot, int moveTo);

}
