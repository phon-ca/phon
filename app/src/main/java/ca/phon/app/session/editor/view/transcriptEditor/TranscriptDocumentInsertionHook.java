package ca.phon.app.session.editor.view.transcriptEditor.hooks;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Hook for controlling document insertions before they are preformed.
 * Attribute sets may be modified and string contents may be altered by
 * modifying the StringBuffer. The batchInsertString method may return
 * a list of additional element specs which should be added after the
 * provided data.
 */
public abstract class TranscriptDocumentInsertionHook {
    private final List<DefaultStyledDocument.ElementSpec> EMPTY_LIST = new ArrayList<>();

    /**
     * Hook into batch string insertions before they are performed.
     *
     * @param buffer
     * @param attrs
     * @return list of additional element specs to add (if any)
     */
    public List<DefaultStyledDocument.ElementSpec> batchInsertString(StringBuilder buffer, MutableAttributeSet attrs) {
        return EMPTY_LIST;
    }

    public List<DefaultStyledDocument.ElementSpec> endTier(MutableAttributeSet attrs) {
        return EMPTY_LIST;
    }
}
