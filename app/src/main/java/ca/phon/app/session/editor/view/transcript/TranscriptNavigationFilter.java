package ca.phon.app.session.editor.view.transcript;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.session.*;
import ca.phon.session.Record;

import javax.swing.text.*;

/**
 * The default navigation filter for the transcript editor
 */
public class TranscriptNavigationFilter extends NavigationFilter {

    private final TranscriptEditor editor;

    /**
     * Constructor
     *
     * @param editor a reference to the transcript editor
     */
    public TranscriptNavigationFilter(TranscriptEditor editor) {
        this.editor = editor;
    }

    @Override
    public void setDot(FilterBypass fb, int dot, Position.Bias bias) {
        TranscriptDocument doc = editor.getTranscriptDocument();
        if (doc.getLength() == 0) {
            fb.setDot(dot, bias);
        }
        if(doc.isBypassDocumentFilter()) {
            fb.setDot(dot, bias);
            LogUtil.info("Bypassing document filter");
            return;
        }

        Element elem = doc.getCharacterElement(dot);
        AttributeSet attrs = elem.getAttributes();
        if (attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_TRAVERSABLE) != null) {
            LogUtil.info("Not traversable");
            return;
        }

        if (doc.getLength() == dot) return;

        if (!editor.isCaretMoveFromUpDown()) editor.setUpDownOffset(-1);
        editor.setCaretMoveFromUpDown(false);
        fb.setDot(dot, bias);

        TranscriptEditor.TranscriptLocationChangeData transcriptLocationChangeData = new TranscriptEditor.TranscriptLocationChangeData(
                editor.getTranscriptEditorCaret().getPreviousLocation(),
                editor.getTranscriptEditorCaret().getCurrentLocation()
        );
        if(editor.getTranscriptEditorCaret().isFreezeCaret()) {
            LogUtil.info("Not sending editor event - freeze caret is enabled");
            return;
        }
        if(transcriptLocationChangeData.newLoc().equals(transcriptLocationChangeData.oldLoc())) {
            LogUtil.info("Not sending editor event - new location is the same as old location");
            return;
        }

        final EditorEvent<TranscriptEditor.TranscriptLocationChangeData> e = new EditorEvent<>(
                TranscriptEditor.transcriptLocationChanged,
                editor,
                transcriptLocationChangeData
        );
        editor.getEventManager().queueEvent(e);
    }

    @Override
    public void moveDot(FilterBypass fb, int dot, Position.Bias bias) {
        TranscriptDocument doc = editor.getTranscriptDocument();

        if (editor.getTranscriptDocument().getLength() == dot) return;

        AttributeSet attrs = doc.getCharacterElement(editor.getCaretPosition()).getAttributes();
        String elementType = (String) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);

        if (elementType != null) {
            int start = -1;
            int end = -1;

            switch (elementType) {
                case TranscriptStyleConstants.ATTR_KEY_RECORD, TranscriptStyleConstants.ATTR_KEY_BLIND_TRANSCRIPTION -> {
                    Record record = TranscriptStyleConstants.getRecord(attrs);
                    if(record == null) return;
                    int recordIndex = editor.getSession().getRecordIndex(record);
                    if(recordIndex < 0) return;
                    Tier<?> tier = (Tier<?>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                    if (tier != null) {
                        final TranscriptDocument.StartEnd tierStartEnd = doc.getTierContentStartEnd(recordIndex, tier.getName());
                        start = tierStartEnd.start();
                        end = tierStartEnd.end();
                    }
                }
                case TranscriptStyleConstants.ATTR_KEY_COMMENT -> {
                    Comment comment = (Comment) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_COMMENT);
                    if (comment != null) {
                        start = doc.getCommentContentStart(comment);
                        end = doc.getCommentEnd(comment);
                    }
                }
                case TranscriptStyleConstants.ATTR_KEY_GEM -> {
                    Gem gem = (Gem) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_GEM);
                    if (gem != null) {
                        start = doc.getGemContentStart(gem);
                        end = doc.getGemEnd(gem);
                    }
                }
                case TranscriptStyleConstants.ATTR_KEY_GENERIC_TIER -> {
                    Tier<?> generic = (Tier<?>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_GENERIC_TIER);
                    if (generic != null) {
                        start = doc.getGenericContentStart(generic);
                        end = doc.getGenericEnd(generic);
                    }
                }
            }

            if (start != -1 && end != -1) {
                dot = Math.min(Math.max(dot, start), end);
            }
        }

        fb.moveDot(dot, bias);
    }
}
