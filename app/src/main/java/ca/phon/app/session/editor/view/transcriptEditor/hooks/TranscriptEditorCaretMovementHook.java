package ca.phon.app.session.editor.view.transcriptEditor.hooks;

import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import java.util.List;

/**
 * Hook for controlling document insertions before they are preformed.
 * Attribute sets may be modified and string contents may be altered by
 * modifying the StringBuffer. The batchInsertString method may return
 * a list of additional element specs which should be added after the
 * provided data.
 */
public interface TranscriptEditorCaretMovementHook {

    /**
     * Hook into batch string insertions before they are performed.
     *
     * @param dot
     * @return list of additional element specs to add (if any)
     */
    public void caretMoved(int dot, AttributeSet characterAttributes);

}
