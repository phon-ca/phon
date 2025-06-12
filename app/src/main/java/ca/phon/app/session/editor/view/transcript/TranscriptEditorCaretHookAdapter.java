package ca.phon.app.session.editor.view.transcript;

/**
 * Adapter class for {@link TranscriptEditorCaretHook}.
 */
public class TranscriptEditorCaretHookAdapter implements TranscriptEditorCaretHook {

    @Override
    public boolean beforeSetDot(int oldDot, int newDot) {
        return true;
    }

    @Override
    public void afterSetDot(int oldDot, int newDot) {
        // No action by default
    }

    @Override
    public void caretFrozen(boolean frozen) {
        // No action by default
    }

    @Override
    public boolean beforeMoveCaret(int dot, int moveTo) {
        // No action by default
        return false;
    }

    @Override
    public void afterMoveCaret(int dot, int moveTo) {
        // No action by default
    }

}
