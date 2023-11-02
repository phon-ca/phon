package ca.phon.app.session.editor.view.transcriptEditor.extensions;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptDocument;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptDocumentInsertionHook;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptEditor;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptStyleConstants;
import ca.phon.session.PhoneAlignment;
import ca.phon.session.Record;
import ca.phon.session.Tier;
import ca.phon.session.TierViewItem;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import java.util.ArrayList;
import java.util.List;

public class AlignmentExtension implements TranscriptEditorExtension {
    private TranscriptEditor editor;
    private TranscriptDocument doc;

    private boolean alignmentVisible = false;
    private boolean alignmentIsComponent = false;
    private TierViewItem alignmentParent = null;

    @Override
    public void install(TranscriptEditor editor) {
        this.editor = editor;
        doc = editor.getTranscriptDocument();

        doc.addInsertionHook(new TranscriptDocumentInsertionHook() {
            @Override
            public List<DefaultStyledDocument.ElementSpec> endTier(MutableAttributeSet attrs) {
                List<DefaultStyledDocument.ElementSpec> retVal = new ArrayList<>();

                Tier<?> tier = (Tier<?>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);

                TierViewItem alignmentParent = getAlignmentParent();
                if (tier != null && isAlignmentVisible() && alignmentParent != null && tier.getName().equals(alignmentParent.getTierName())) {
                    retVal.addAll(doc.getBatchEndLineFeed(attrs));

                    Record record = (Record) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
                    retVal.addAll(getFormattedAlignment(tier, record));
                }

                return retVal;
            }
        });

        EditorEventManager eventManager = editor.getEventManager();
        eventManager.registerActionForEvent(EditorEventType.TierChange, this::onTierDataChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
    }

    private void onTierDataChanged(EditorEvent<EditorEventType.TierChangeData> editorEvent) {

        Tier<?> tier = editorEvent.data().tier();

        try {
            int start = doc.getTierStart(tier);
            Record record = doc.getRecord(tier);
            start -= doc.getLabelColumnWidth() + 2;
            int end = doc.getTierEnd(tier);

            doc.setBypassDocumentFilter(true);
            doc.remove(start, end - start);

            List<DefaultStyledDocument.ElementSpec> insertions = new ArrayList<>();
            insertions.addAll(doc.getBatchEndStart());

            insertions.addAll(getFormattedAlignment(record.getTier(getAlignmentParent().getTierName()), record));

            var attrs = insertions.get(insertions.size() - 1).getAttributes();
            insertions.addAll(doc.getBatchEndLineFeed(attrs));

            doc.getBatch().addAll(insertions);
            doc.processBatchUpdates(start);
            doc.setGlobalParagraphAttributes();
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    public List<DefaultStyledDocument.ElementSpec> getFormattedAlignment(Tier<?> tier, Record record) {
        List<DefaultStyledDocument.ElementSpec> retVal = new ArrayList<>();

        // Get the alignment tier
        Tier<PhoneAlignment> alignmentTier = record.getPhoneAlignmentTier();
        // Set up the tier attributes for the dummy tier
        var tierAttrs = doc.getTierAttributes(tier);
        tierAttrs.addAttributes(doc.getTierAttributes(alignmentTier));
        // Set up the attributes for its label
        SimpleAttributeSet alignmentLabelAttrs = doc.getTierLabelAttributes(alignmentTier);
        // Set up record attributes
        SimpleAttributeSet recordAttrs = doc.getRecordAttributes(editor.getSession().getRecordPosition(record));
        alignmentLabelAttrs.addAttributes(recordAttrs);
        tierAttrs.addAttributes(recordAttrs);
        // Get the string for the label
        String alignmentLabelText = doc.formatLabelText("Alignment");
        // Add the label
        retVal.add(doc.getBatchString(alignmentLabelText + ": ", alignmentLabelAttrs));
        // Get the string version of the alignment
        String alignmentContent = alignmentTier.getValue().toString();
        // Add component factory if needed
        if (isAlignmentComponent()) {
            tierAttrs.addAttributes(doc.getAlignmentAttributes());
        }
        retVal.add(doc.getBatchString(alignmentContent, tierAttrs));

        return retVal;
    }
    private void appendFormattedAlignment(Tier<?> tier, Record record) {
        doc.getBatch().addAll(getFormattedAlignment(tier, record));
    }

    public TierViewItem calculateAlignmentParent() {
        List<TierViewItem> visibleTierView = editor.getSession().getTierView().stream().filter(item -> item.isVisible()).toList();

        var retVal = visibleTierView.stream().filter(item -> item.getTierName().equals("IPA Actual")).findFirst();
        if (retVal.isPresent()) return retVal.get();

        retVal = visibleTierView.stream().filter(item -> item.getTierName().equals("IPA Target")).findFirst();
        if (retVal.isPresent()) return retVal.get();

        return visibleTierView.get(visibleTierView.size()-1);
    }

    // region Getters and Setters

    public boolean isAlignmentVisible() {
        return alignmentVisible;
    }

    public void setAlignmentVisible(boolean alignmentVisible) {
        this.alignmentVisible = alignmentVisible;
        if (alignmentVisible) {
            alignmentParent = calculateAlignmentParent();
        }
        else {
            alignmentParent = null;
        }
        doc.reload();
    }

    public boolean isAlignmentComponent() {
        return alignmentIsComponent;
    }

    public void setAlignmentIsComponent(boolean alignmentIsComponent) {
        this.alignmentIsComponent = alignmentIsComponent;
        if (alignmentVisible) {
            doc.reload();
        }
    }

    public TierViewItem getAlignmentParent() {
        return alignmentParent;
    }

    public void setAlignmentParent(TierViewItem alignmentParent) {
        this.alignmentParent = alignmentParent;
    }

    // endregion Getters and Setters
}
