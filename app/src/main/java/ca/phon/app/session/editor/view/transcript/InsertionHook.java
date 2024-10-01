package ca.phon.app.session.editor.view.transcript;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import java.util.List;

public interface InsertionHook {
    List<DefaultStyledDocument.ElementSpec> batchInsertString(StringBuilder buffer, MutableAttributeSet attrs);

    List<DefaultStyledDocument.ElementSpec> startSession();

    List<DefaultStyledDocument.ElementSpec> endSession();

    List<DefaultStyledDocument.ElementSpec> startHeader();

    List<DefaultStyledDocument.ElementSpec> endHeader();

    List<DefaultStyledDocument.ElementSpec> startHeaderLine();

    List<DefaultStyledDocument.ElementSpec> endHeaderLine();

    List<DefaultStyledDocument.ElementSpec> startTranscript();

    List<DefaultStyledDocument.ElementSpec> endTranscript();

    List<DefaultStyledDocument.ElementSpec> startComment();

    List<DefaultStyledDocument.ElementSpec> endComment();

    List<DefaultStyledDocument.ElementSpec> startGem();

    List<DefaultStyledDocument.ElementSpec> endGem();

    List<DefaultStyledDocument.ElementSpec> startRecord();

    List<DefaultStyledDocument.ElementSpec> endRecord();

    List<DefaultStyledDocument.ElementSpec> startRecordHeader();

    List<DefaultStyledDocument.ElementSpec> endRecordHeader();

    List<DefaultStyledDocument.ElementSpec> startTier();

    List<DefaultStyledDocument.ElementSpec> endTier(MutableAttributeSet attrs);

    // called when a tier has been removed from the document
    void tierRemoved(TranscriptDocument doc, String tierName);
}
