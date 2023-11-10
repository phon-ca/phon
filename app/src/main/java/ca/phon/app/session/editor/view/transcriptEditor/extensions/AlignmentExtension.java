package ca.phon.app.session.editor.view.transcriptEditor.extensions;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.view.transcriptEditor.*;
import ca.phon.session.PhoneAlignment;
import ca.phon.session.Record;
import ca.phon.session.Tier;
import ca.phon.session.TierViewItem;

import javax.swing.text.*;
import java.util.ArrayList;
import java.util.List;

public class AlignmentExtension implements TranscriptEditorExtension {
    private TranscriptEditor editor;
    private TranscriptDocument doc;

    public final static String ALIGNMENT_IS_VISIBLE = "isAlignmentVisible";
    public final static boolean ALIGNMENT_IS_VISIBLE_DEFAULT = false;
    public final static String ALIGNMENT_IS_COMPONENT = "isAlignmentComponent";
    public final static boolean ALIGNMENT_IS_COMPONENT_DEFAULT = false;
    public final static String ALIGNMENT_PARENT = "alignmentParent";
    public final static TierViewItem ALIGNMENT_PARENT_DEFAULT = null;

    @Override
    public void install(TranscriptEditor editor) {
        this.editor = editor;
        doc = editor.getTranscriptDocument();

        doc.addInsertionHook(new DefaultInsertionHook() {
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

        doc.addDocumentPropertyChangeListener(ALIGNMENT_IS_VISIBLE, evt -> {
            if (isAlignmentVisible()) {
                doc.putDocumentProperty(ALIGNMENT_PARENT, calculateAlignmentParent());
            }
            else {
                doc.putDocumentProperty(ALIGNMENT_PARENT, ALIGNMENT_PARENT_DEFAULT);
            }
            doc.reload();
        });
        doc.addDocumentPropertyChangeListener(ALIGNMENT_IS_COMPONENT, evt -> {
            if (isAlignmentVisible()) {
                doc.reload();
            }
        });
    }

    private void onTierDataChanged(EditorEvent<EditorEventType.TierChangeData> editorEvent) {

        Tier<?> tier = editorEvent.data().tier();

        if (!tier.getDeclaredType().equals(PhoneAlignment.class) || !isAlignmentVisible()) return;

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
        return (boolean) doc.getDocumentPropertyOrDefault(ALIGNMENT_IS_VISIBLE, ALIGNMENT_IS_VISIBLE_DEFAULT);
    }

    public boolean isAlignmentComponent() {
        return (boolean) doc.getDocumentPropertyOrDefault(ALIGNMENT_IS_COMPONENT, ALIGNMENT_IS_COMPONENT_DEFAULT);
    }

    public TierViewItem getAlignmentParent() {
        return (TierViewItem) doc.getDocumentPropertyOrDefault(ALIGNMENT_PARENT, ALIGNMENT_PARENT_DEFAULT);
    }

    // endregion Getters and Setters
}