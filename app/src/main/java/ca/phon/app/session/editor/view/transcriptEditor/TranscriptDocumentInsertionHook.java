package ca.phon.app.session.editor.view.transcriptEditor;

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

    public List<DefaultStyledDocument.ElementSpec> startSession() {
        return EMPTY_LIST;
    }
    public List<DefaultStyledDocument.ElementSpec> endSession() {
        return EMPTY_LIST;
    }

    public List<DefaultStyledDocument.ElementSpec> startHeader() {
        return EMPTY_LIST;
    }
    public List<DefaultStyledDocument.ElementSpec> endHeader() {
        return EMPTY_LIST;
    }

    public List<DefaultStyledDocument.ElementSpec> startHeaderLine() {
        return EMPTY_LIST;
    }
    public List<DefaultStyledDocument.ElementSpec> endHeaderLine() {
        return EMPTY_LIST;
    }

    public List<DefaultStyledDocument.ElementSpec> startTranscript() {
        return EMPTY_LIST;
    }
    public List<DefaultStyledDocument.ElementSpec> endTranscript() {
        return EMPTY_LIST;
    }

    public List<DefaultStyledDocument.ElementSpec> startComment() {
        return EMPTY_LIST;
    }
    public List<DefaultStyledDocument.ElementSpec> endComment() {
        return EMPTY_LIST;
    }

    public List<DefaultStyledDocument.ElementSpec> startGem() {
        return EMPTY_LIST;
    }
    public List<DefaultStyledDocument.ElementSpec> endGem() {
        return EMPTY_LIST;
    }

    public List<DefaultStyledDocument.ElementSpec> startRecord() {
        return EMPTY_LIST;
    }
    public List<DefaultStyledDocument.ElementSpec> endRecord() {
        return EMPTY_LIST;
    }

    public List<DefaultStyledDocument.ElementSpec> startRecordHeader() {
        return EMPTY_LIST;
    }
    public List<DefaultStyledDocument.ElementSpec> endRecordHeader() {
        return EMPTY_LIST;
    }

    public List<DefaultStyledDocument.ElementSpec> beginTier() {
        return EMPTY_LIST;
    }
    public List<DefaultStyledDocument.ElementSpec> endTier(MutableAttributeSet attrs) {
        return EMPTY_LIST;
    }
}
