package ca.phon.app.session.editor.view.transcript.extensions;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.view.transcript.*;
import ca.phon.session.PhoneAlignment;
import ca.phon.session.Record;
import ca.phon.session.Tier;
import ca.phon.session.TierViewItem;
import com.kitfox.svg.A;

import javax.swing.text.*;
import java.util.ArrayList;
import java.util.List;

/**
 * An extension that provides phone alignment support to the {@link TranscriptEditor}
 * */
public class AlignmentExtension implements TranscriptEditorExtension {
    private TranscriptEditor editor;
    private TranscriptDocument doc;

    /* Document property stuff */

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

        // TODO: make this stuff happen in the right order

        doc.addInsertionHook(new DefaultInsertionHook() {
            @Override
            public List<DefaultStyledDocument.ElementSpec> endTier(MutableAttributeSet attrs) {
                Tier<?> tier = (Tier<?>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                TierViewItem alignmentParent = getAlignmentParent();

                if (tier != null && isAlignmentVisible() && alignmentParent != null && tier.getName().equals(alignmentParent.getTierName())) {
                    Record record = (Record) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
                    return getFormattedAlignment(record.getTier(alignmentParent.getTierName()), record);
                }

                return new ArrayList<>();
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

        editor.getEventManager().registerActionForEvent(EditorEventType.TierViewChanged, (event) -> {
            doc.putDocumentProperty(ALIGNMENT_PARENT, calculateAlignmentParent());
        }, EditorEventManager.RunOn.AWTEventDispatchThread);
    }

    /**
     * Runs when tier data gets changed.
     * If the alignment tier is the one that gets changed, update it in the doc.
     *
     * @param editorEvent the event that changed the tier data
     * */
    private void onTierDataChanged(EditorEvent<EditorEventType.TierChangeData> editorEvent) {

        Tier<?> tier = editorEvent.data().tier();

        if (!tier.getDeclaredType().equals(PhoneAlignment.class) || !isAlignmentVisible()) return;

//        try {
//            int start = doc.getTierStart(tier);
//            Record record = doc.getRecord(tier);
//            int end = doc.getTierEnd(tier);
//
//            doc.setBypassDocumentFilter(true);
//            doc.remove(start, end - start);
//
//            List<DefaultStyledDocument.ElementSpec> insertions = new ArrayList<>();
//            insertions.addAll(TranscriptBatchBuilder.getBatchEndStart());
//
//            insertions.addAll(getFormattedAlignment(record.getTier(getAlignmentParent().getTierName()), record));
//
//            var attrs = insertions.get(insertions.size() - 1).getAttributes();
//            insertions.addAll(TranscriptBatchBuilder.getBatchEndLineFeed(attrs, null));
//
//            TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder();
//            batchBuilder.appendAll(insertions);
//            doc.processBatchUpdates(start, batchBuilder.getBatch());
//        }
//        catch (BadLocationException e) {
//            LogUtil.severe(e);
//        }
    }

    /**
     * Gets a list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} that contains the data for the
     * properly formatted alignment tier line
     *
     * @param parentTier a reference to the parent tier
     * @param record a reference to the record containing the alignment tier
     * @return the list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} data
     * */
    public List<DefaultStyledDocument.ElementSpec> getFormattedAlignment(Tier<?> parentTier, Record record) {
        final TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder(doc);
        // Get the alignment tier
        Tier<PhoneAlignment> alignmentTier = record.getPhoneAlignmentTier();
        TranscriptStyleContext transcriptStyleContext = doc.getTranscriptStyleContext();

        // Set up the tier attributes for the dummy tier
        var tierAttrs = new SimpleAttributeSet();
        TranscriptStyleConstants.setNotEditable(tierAttrs, true);
        TranscriptStyleConstants.setRecord(tierAttrs, record);

        batchBuilder.appendTierLabel(editor.getSession(), record, alignmentTier, alignmentTier.getName(), null, doc.isChatTierNamesShown(), tierAttrs);

        // Get the string version of the alignment
        // Add component factory if needed
        if (isAlignmentComponent()) {
            tierAttrs.addAttributes(transcriptStyleContext.getAlignmentAttributes());
        }
        batchBuilder.appendTierContent(record, alignmentTier, tierAttrs);

        return batchBuilder.getBatch();
    }

    /**
     * Calculates which tier the alignment tier line should be parented to
     *
     * @return the {@link TierViewItem} associated with the calculated parent tier
     * */
    public TierViewItem calculateAlignmentParent() {
        List<TierViewItem> visibleTierView = editor.getSession().getTierView().stream().filter(TierViewItem::isVisible).toList();

        var retVal = visibleTierView.stream().filter(item -> item.getTierName().equals("IPA Actual")).findFirst();
        if (retVal.isPresent()) return retVal.get();

        retVal = visibleTierView.stream().filter(item -> item.getTierName().equals("IPA Target")).findFirst();
        return retVal.orElseGet(() -> visibleTierView.get(visibleTierView.size() - 1));
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
