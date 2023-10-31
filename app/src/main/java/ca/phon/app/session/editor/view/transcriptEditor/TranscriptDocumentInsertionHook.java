package ca.phon.app.session.editor.view.transcriptEditor;

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
public interface TranscriptDocumentInsertionHook {

    /**
     * Hook into batch string insertions before they are performed.
     *
     * @param buffer
     * @param attrs
     * @return list of additional element specs to add (if any)
     */
    public List<DefaultStyledDocument.ElementSpec> batchInsertString(StringBuilder buffer, MutableAttributeSet attrs);

}
