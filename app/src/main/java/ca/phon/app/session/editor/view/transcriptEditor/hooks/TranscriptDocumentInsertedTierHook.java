package ca.phon.app.session.editor.view.transcriptEditor.hooks;

import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import java.util.List;

public interface TranscriptDocumentInsertedTierHook {
    public List<DefaultStyledDocument.ElementSpec> insertedTier(MutableAttributeSet attrs);
}
