package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.log.LogUtil;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.MediaTimeFormatStyle;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.InternalMedia;
import ca.phon.orthography.Orthography;
import ca.phon.orthography.mor.Grasp;
import ca.phon.orthography.mor.GraspTierData;
import ca.phon.orthography.mor.Mor;
import ca.phon.orthography.mor.MorTierData;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.format.MediaSegmentFormatter;
import ca.phon.session.tierdata.*;
import ca.phon.syllable.SyllabificationInfo;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.ui.FontFormatter;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TranscriptDocument extends DefaultStyledDocument {
    private Session session;
    private final SessionFactory sessionFactory;
    private boolean singleRecordView = false;
    private int singleRecordIndex = 0;
    private static final char[] EOL_ARRAY = { '\n' };
    private ArrayList<ElementSpec> batch;
    private boolean syllabificationVisible = false;
    private boolean syllabificationIsComponent = false;
    private boolean alignmentVisible = false;
    private boolean alignmentIsComponent = false;
    public int labelColumnWidth = 20;
    private float lineSpacing = 0.2f;
    private TierViewItem alignmentParent = null;
    private final Map<String, Tier<?>> headerTierMap;
    private boolean bypassDocumentFilter = false;

    public TranscriptDocument() {
        super(new TranscriptStyleContext());
        sessionFactory = SessionFactory.newFactory();
        setDocumentFilter(new TranscriptDocumentFilter());
        batch = new ArrayList<>();

        headerTierMap = new HashMap<>();
        headerTierMap.put("tiers", sessionFactory.createTier("Tiers", TierData.class));
        headerTierMap.put("participants", sessionFactory.createTier("Participants", TierData.class));
        headerTierMap.put("languages", sessionFactory.createTier("Languages", Languages.class));
        headerTierMap.put("media", sessionFactory.createTier("Media", TierData.class));
    }


    // region Getters and Setters

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
        try {
            populate();
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    public boolean getSingleRecordView() {
        return singleRecordView;
    }

    public void setSingleRecordView(boolean singleRecordView) {
        if (this.singleRecordView == singleRecordView) return;
        this.singleRecordView = singleRecordView;
        reload();
    }

    public int getSingleRecordIndex() {
        return singleRecordIndex;
    }

    public void setSingleRecordIndex(int singleRecordIndex) {
        this.singleRecordIndex = singleRecordIndex;
        if (singleRecordView) {
            reload();
        }
    }

    public boolean isSyllabificationVisible() {
        return syllabificationVisible;
    }

    public void setSyllabificationVisible(boolean syllabificationVisible) {
        this.syllabificationVisible = syllabificationVisible;
        reload();
    }

    public boolean isSyllabificationComponent() {
        return syllabificationIsComponent;
    }

    public void setSyllabificationIsComponent(boolean syllabificationIsComponent) {
        this.syllabificationIsComponent = syllabificationIsComponent;
        if (syllabificationVisible) {
            reload();
        }
    }

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
        reload();
    }

    public boolean isAlignmentComponent() {
        return alignmentIsComponent;
    }

    public void setAlignmentIsComponent(boolean alignmentIsComponent) {
        this.alignmentIsComponent = alignmentIsComponent;
        if (alignmentVisible) {
            reload();
        }
    }

    public int getLabelColumnWidth() {
        return labelColumnWidth;
    }

    public void setLabelColumnWidth(int labelColumnWidth) {
        this.labelColumnWidth = labelColumnWidth;
    }

    public float getLineSpacing() {
        return lineSpacing;
    }

    public void setLineSpacing(float lineSpacing) {
        this.lineSpacing = lineSpacing;
    }

    public TierViewItem getAlignmentParent() {
        return alignmentParent;
    }

    public void setAlignmentParent(TierViewItem alignmentParent) {
        this.alignmentParent = alignmentParent;
    }

    public Map<String, Tier<?>> getHeaderTierMap() {
        return headerTierMap;
    }

    // endregion Getters and Setters

    // region Attribute Getters

    private SimpleAttributeSet getRecordAttributes(int recordIndex) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        Record record = session.getRecord(recordIndex);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD, record);

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE, TranscriptStyleConstants.ATTR_KEY_RECORD);

        return retVal;
    }

    private SimpleAttributeSet getTierAttributes(Tier<?> tier) {
        return getTierAttributes(tier, null);
    }
    private SimpleAttributeSet getTierAttributes(Tier<?> tier, TierViewItem item) {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_TIER, tier);
        String fontString = "default";
        if (item != null) {
            fontString = item.getTierFont();
        }
        if ("default".equalsIgnoreCase(fontString)) {
            fontString = new FontFormatter().format(FontPreferences.getTierFont());
        }

        var font = Font.decode(fontString);

        StyleConstants.setFontFamily(retVal, font.getFamily());
        StyleConstants.setFontSize(retVal, font.getSize() + (int) PrefHelper.getUserPreferences().getFloat(TranscriptView.FONT_SIZE_DELTA_PROP, 0));
        StyleConstants.setBold(retVal, font.isBold());
        StyleConstants.setItalic(retVal, font.isItalic());

        return retVal;
    }

    private SimpleAttributeSet getSyllabificationAttributes() {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_COMPONENT_FACTORY, new SyllabificationComponentFactory());
        return retVal;
    }

    private SimpleAttributeSet getAlignmentAttributes() {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_COMPONENT_FACTORY, new AlignmentComponentFactory());

        return retVal;
    }

    private SimpleAttributeSet getIPAWordAttributes(Tier<IPATranscript> tier) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_TIER, tier);

        StyleConstants.setForeground(retVal, UIManager.getColor(TranscriptEditorUIProps.IPA_WORD));

        return retVal;
    }

    private SimpleAttributeSet getIPAPauseAttributes(Tier<IPATranscript> tier) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_TIER, tier);

        StyleConstants.setForeground(retVal, UIManager.getColor(TranscriptEditorUIProps.IPA_PAUSE));

        return retVal;
    }

    private SimpleAttributeSet getSegmentTimeAttributes(MediaSegment segment) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_MEDIA_SEGMENT, segment);

        return retVal;
    }

    private SimpleAttributeSet getSegmentDashAttributes(MediaSegment segment) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_MEDIA_SEGMENT, segment);

        StyleConstants.setForeground(retVal, UIManager.getColor(TranscriptEditorUIProps.SEGMENT_DASH));

        return retVal;
    }

    private SimpleAttributeSet getTierStringAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        StyleConstants.setForeground(retVal, Color.black);

        return retVal;
    }

    private SimpleAttributeSet getTierCommentAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        StyleConstants.setForeground(retVal, UIManager.getColor(TranscriptEditorUIProps.TIER_COMMENT));

        return retVal;
    }

    private SimpleAttributeSet getTierInternalMediaAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        StyleConstants.setForeground(retVal, UIManager.getColor(TranscriptEditorUIProps.INTERNAL_MEDIA));

        return retVal;
    }

    private SimpleAttributeSet getTierLinkAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        return retVal;
    }

    private SimpleAttributeSet getTierLabelAttributes(Tier<?> tier) {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL, true);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_TRAVERSABLE, true);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_TIER, tier);

        retVal.addAttributes(getMonospaceFontAttributes());

        return retVal;
    }

    private SimpleAttributeSet getSeparatorAttributes() {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL, true);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_TRAVERSABLE, true);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_SEPARATOR, true);

        retVal.addAttributes(getMonospaceFontAttributes());

        return retVal;
    }

    private SimpleAttributeSet getCommentAttributes(Comment comment) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE, TranscriptStyleConstants.ATTR_KEY_COMMENT);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_COMMENT, comment);

        return retVal;
    }

    private SimpleAttributeSet getCommentLabelAttributes(Comment comment) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttributes(getCommentAttributes(comment));
        retVal.addAttributes(getMonospaceFontAttributes());

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL, true);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_TRAVERSABLE, true);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);

        return retVal;
    }

    private SimpleAttributeSet getGemAttributes(Gem gem) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE, TranscriptStyleConstants.ATTR_KEY_GEM);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_GEM, gem);

        return retVal;
    }

    private SimpleAttributeSet getGemLabelAttributes(Gem gem) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttributes(getGemAttributes(gem));
        retVal.addAttributes(getMonospaceFontAttributes());

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL, true);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_TRAVERSABLE, true);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);

        return retVal;
    }

    private SimpleAttributeSet getLabelAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttributes(getMonospaceFontAttributes());

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL, true);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_TRAVERSABLE, true);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);

        return retVal;
    }

    private SimpleAttributeSet getStandardFontAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        Font font = FontPreferences.getTierFont();
        StyleConstants.setFontFamily(retVal, font.getFamily());
        StyleConstants.setFontSize(retVal, 14);
        StyleConstants.setBold(retVal, font.isBold());
        StyleConstants.setItalic(retVal, font.isItalic());

        return retVal;
    }

    private SimpleAttributeSet getMonospaceFontAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        Font font = FontPreferences.getMonospaceFont();
        StyleConstants.setFontFamily(retVal, font.getFamily());
        StyleConstants.setFontSize(retVal, 14);
        StyleConstants.setBold(retVal, font.isBold());
        StyleConstants.setItalic(retVal, font.isItalic());

        return retVal;
    }

    private SimpleAttributeSet getTiersHeaderAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);

        return retVal;
    }

    private SimpleAttributeSet getParticipantsHeaderAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);

        return retVal;
    }

    // endregion Attribute Getters

    // region Batching

    public void appendBatchString(String str, AttributeSet a) {
        a = a.copyAttributes();
        char[] chars = str.toCharArray();
        batch.add(new ElementSpec(a, ElementSpec.ContentType, chars, 0, str.length()));
    }

    public void appendBatchLineFeed(AttributeSet a) {
        // Add a spec with the linefeed characters
        batch.add(new ElementSpec(a, ElementSpec.ContentType, EOL_ARRAY, 0, 1));

        appendBatchEndStart();
    }

    public void appendBatchEndStart() {
        batch.add(new ElementSpec(null, ElementSpec.EndTagType));
        batch.add(new ElementSpec(null, ElementSpec.StartTagType));
    }

    public void processBatchUpdates(int offs) throws BadLocationException {
        // As with insertBatchString, this could be synchronized if
        // there was a chance multiple threads would be in here.
        ElementSpec[] inserts = new ElementSpec[batch.size()];
        batch.toArray(inserts);

        // Process all the inserts in bulk
        super.insert(offs, inserts);

        // Empty batch the list
        batch.clear();

        setGlobalParagraphAttributes();
    }

    // endregion Batching

    // region Add Comment / Gem

    public void addComment(Comment comment, int transcriptElementIndex) {
        int offset = -1;

        if (transcriptElementIndex == 0) {
            var elementAfterComment = session.getTranscript().getElementAt(1);
            if (elementAfterComment.isRecord()) {
                offset = getRecordStart(session.getRecordPosition(elementAfterComment.asRecord()));
            }
            else if (elementAfterComment.isComment()) {
                offset = getCommentStart(elementAfterComment.asComment());
            }
            else if (elementAfterComment.isGem()) {
                offset = getGemStart(elementAfterComment.asGem());
            }
            else {
                throw new RuntimeException("Invalid transcript element");
            }

            offset -= labelColumnWidth + 2;
        }
        else {
            var elementBeforeComment = session.getTranscript().getElementAt(transcriptElementIndex-1);
            if (elementBeforeComment.isRecord()) {
                offset = getRecordEnd(session.getRecordPosition(elementBeforeComment.asRecord()));
            }
            else if (elementBeforeComment.isComment()) {
                offset = getCommentEnd(elementBeforeComment.asComment());
            }
            else if (elementBeforeComment.isGem()) {
                offset = getGemEnd(elementBeforeComment.asGem());
            }
            else {
                throw new RuntimeException("Invalid transcript element");
            }
        }

        try {
            appendBatchEndStart();
            var attrs = writeComment(comment);
            appendBatchLineFeed(attrs);
            processBatchUpdates(offset);
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    public void addGem(Gem gem, int transcriptElementIndex) {
        int offset = -1;

        if (transcriptElementIndex == 0) {
            var elementAfterComment = session.getTranscript().getElementAt(1);
            if (elementAfterComment.isRecord()) {
                offset = getRecordStart(session.getRecordPosition(elementAfterComment.asRecord()));
            }
            else if (elementAfterComment.isComment()) {
                offset = getCommentStart(elementAfterComment.asComment());
            }
            else if (elementAfterComment.isGem()) {
                offset = getGemStart(elementAfterComment.asGem());
            }
            else {
                throw new RuntimeException("Invalid transcript element");
            }

            offset -= labelColumnWidth + 2;
        }
        else {
            var elementBeforeComment = session.getTranscript().getElementAt(transcriptElementIndex-1);
            if (elementBeforeComment.isRecord()) {
                offset = getRecordEnd(session.getRecordPosition(elementBeforeComment.asRecord()));
            }
            else if (elementBeforeComment.isComment()) {
                offset = getCommentEnd(elementBeforeComment.asComment());
            }
            else if (elementBeforeComment.isGem()) {
                offset = getGemEnd(elementBeforeComment.asGem());
            }
            else {
                throw new RuntimeException("Invalid transcript element");
            }
        }

        try {
            appendBatchEndStart();
            var attrs = writeGem(gem);
            appendBatchLineFeed(attrs);
            processBatchUpdates(offset);
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    // endregion Add Comment / Gem

    public void deleteTranscriptElement(Transcript.Element elem) {
        int labelLength = labelColumnWidth + 2;
        try {
            int startOffset = -1;
            int endOffset = -1;

            if (elem.isComment()) {
                startOffset = getCommentStart(elem.asComment());
                endOffset = getCommentEnd(elem.asComment());
            }
            else if (elem.isGem()) {
                startOffset = getGemStart(elem.asGem());
                endOffset = getGemEnd(elem.asGem());
            }
            else if (elem.isRecord()) {
                return;
//                Transcript transcript = session.getTranscript();
//                int recordIndex = transcript.getRecordPosition(elem.asRecord());
//                int recordElementIndex = transcript.getRecordElementIndex(recordIndex);
//                deleteRecord(recordIndex, recordElementIndex);
//                return;
            }

            startOffset -= labelLength;

            if (startOffset < 0 || endOffset < 0) return;

            bypassDocumentFilter = true;
            remove(startOffset, endOffset - startOffset);
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    public void changeCommentType(Comment comment) {
        int start = getCommentStart(comment);
        int end = getCommentEnd(comment);

        start -= labelColumnWidth + 2;

        try {
            bypassDocumentFilter = true;
            remove(start, end - start);
            appendBatchEndStart();
            var attrs = writeComment(comment);
            appendBatchLineFeed(attrs);
            processBatchUpdates(start);
            setGlobalParagraphAttributes();
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    public void changeGemType(Gem gem) {
        int start = getGemStart(gem);
        int end = getGemEnd(gem);

        start -= labelColumnWidth + 2;

        try {
            bypassDocumentFilter = true;
            remove(start, end - start);
            appendBatchEndStart();
            var attrs = writeGem(gem);
            appendBatchLineFeed(attrs);
            processBatchUpdates(start);
            setGlobalParagraphAttributes();
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    // region Write Transcript Element

    private SimpleAttributeSet writeRecord(
            Record record,
            Transcript transcript,
            List<TierViewItem> tierView
    ) {
        int recordIndex = transcript.getRecordPosition(record);
        SimpleAttributeSet recordAttrs = getRecordAttributes(recordIndex);

        SimpleAttributeSet sepAttrs = getSeparatorAttributes();
        sepAttrs.addAttributes(recordAttrs);

        appendBatchString(formatLabelText(record.getSpeaker().toString()) + "  ", sepAttrs);

        MediaSegment segment = record.getMediaSegment();

        sepAttrs.addAttributes(getStandardFontAttributes());
        formatSegment(segment, sepAttrs);
        appendBatchLineFeed(sepAttrs);

        SimpleAttributeSet tierAttrs = null;

        List<TierViewItem> visibleTierView = tierView.stream().filter(item -> item.isVisible()).toList();

        for (int i = 0; i < visibleTierView.size(); i++) {
            TierViewItem item = visibleTierView.get(i);
            Tier<?> tier = record.getTier(item.getTierName());
            if (tier == null) {
                var tdOpt = session.getUserTiers().stream().filter(td -> td.getName().equals(item.getTierName())).findAny();
                if (tdOpt.isEmpty()) {
                    continue;
                }
                tier = sessionFactory.createTier(tdOpt.get());
            }

            tierAttrs = insertTier(recordIndex, tier, item, recordAttrs);

            if (i < visibleTierView.size() - 1) {
                appendBatchLineFeed(tierAttrs);
            }
        }

        return (tierAttrs != null) ? tierAttrs : sepAttrs;
    }

    private SimpleAttributeSet writeComment(Comment comment) {

        Tier<TierData> commentTier = sessionFactory.createTier("commentTier", TierData.class);
        commentTier.setValue(comment.getValue() == null ? new TierData() : comment.getValue());

        SimpleAttributeSet commentAttrs = getCommentAttributes(comment);
        commentAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_COMMENT_TIER, commentTier);
        commentAttrs.addAttributes(getStandardFontAttributes());

        SimpleAttributeSet labelAttrs = getCommentLabelAttributes(comment);
        String labelText = comment.getType().getLabel();
        if (labelText.length() < labelColumnWidth) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < (labelColumnWidth - labelText.length()); i++) {
                builder.append(' ');
            }
            appendBatchString(builder.toString(), labelAttrs);
        }
        else {
            labelText = formatLabelText(labelText);
        }

        TierData tierData = commentTier.getValue();

        labelAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE, true);
        appendBatchString(labelText, labelAttrs);

        labelAttrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE);
        appendBatchString(": ", labelAttrs);

        for (int i = 0; i < tierData.length(); i++) {
            TierElement userTierElement = tierData.elementAt(i);
            String text = null;
            SimpleAttributeSet attrs;
            if (userTierElement instanceof TierString tierString) {
                // Text
                text = tierString.text();
                attrs = getTierStringAttributes();
            } else if (userTierElement instanceof TierComment userTierComment) {
                // Comment
                text = userTierComment.toString();
                attrs = getTierCommentAttributes();
            } else if (userTierElement instanceof TierInternalMedia internalMedia) {
                // Internal media
                attrs = getTierInternalMediaAttributes();
                formatInternalMedia(internalMedia.getInternalMedia(), attrs);
            }
            else if (userTierElement instanceof TierLink link) {
                // Link
                text = link.toString();
                attrs = getTierLinkAttributes();
            }
            else {
                throw new RuntimeException("Invalid type");
            }

            attrs.addAttributes(commentAttrs);

            if (text != null) appendBatchString(text, attrs);

            if (i < tierData.length() - 1) {
                appendBatchString(" ", attrs);
            }
        }

        return commentAttrs;
    }

    private SimpleAttributeSet writeGem(Gem gem) {
        String text = gem.getLabel();

        SimpleAttributeSet gemAttrs = getGemAttributes(gem);
        gemAttrs.addAttributes(getStandardFontAttributes());

        SimpleAttributeSet labelAttrs = getGemLabelAttributes(gem);
        String labelText = gem.getType().toString() + " Gem";
        if (labelText.length() < labelColumnWidth) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < (labelColumnWidth - labelText.length()); i++) {
                builder.append(' ');
            }
            appendBatchString(builder.toString(), labelAttrs);
        }
        else {
            labelText = formatLabelText(labelText);
        }

        labelAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE, true);
        appendBatchString(labelText, labelAttrs);

        labelAttrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE);
        appendBatchString(": ", labelAttrs);

        appendBatchString(text, gemAttrs);

        return gemAttrs;
    }

    private SimpleAttributeSet writeGeneric(String label, Tier<?> tier) {
        return writeGeneric(label, tier, null);
    }
    private SimpleAttributeSet writeGeneric(String label, Tier<?> tier, AttributeSet additionalAttributes) {

        SimpleAttributeSet attrs = new SimpleAttributeSet();
        attrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE, TranscriptStyleConstants.ATTR_KEY_GENERIC);
        attrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_GENERIC, tier);
        attrs.addAttributes(getStandardFontAttributes());
        if (additionalAttributes != null) attrs.addAttributes(additionalAttributes);

        SimpleAttributeSet labelAttrs = new SimpleAttributeSet(attrs);
        labelAttrs.addAttributes(getLabelAttributes());
        String labelText = label;
        if (labelText.length() < labelColumnWidth) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < (labelColumnWidth - labelText.length()); i++) {
                builder.append(' ');
            }
            appendBatchString(builder.toString(), labelAttrs);
        }
        else {
            labelText = formatLabelText(labelText);
        }

        labelAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE, true);
        appendBatchString(labelText, labelAttrs);

        labelAttrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE);
        appendBatchString(": ", labelAttrs);

        if (tier.isUnvalidated()) {
            appendBatchString(tier.getUnvalidatedValue().toString(), attrs);
        }
        else {
            appendBatchString(tier.toString(), attrs);
        }

        return attrs;
    }

    // endregion Write Transcript Element


    // region Get Record/Tier Start/End

    public int getRecordStart(int recordIndex) {
        return getRecordStart(recordIndex, false);
    }

    public int getRecordStart(int recordIndex, boolean includeSeparator) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            AttributeSet attrs = elem.getElement(0).getAttributes();
            Record currentRecord = (Record) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
            if (currentRecord == null) continue;
            int currentRecordIndex = session.getRecordPosition(currentRecord);
            var tier = attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
            if ((tier != null || includeSeparator) && recordIndex == currentRecordIndex) {
                return elem.getStartOffset();
            }
        }

        return -1;
    }

    public int getRecordStart(Record record) {
        return getRecordStart(record, false);
    }

    public int getRecordStart(Record record, boolean includeSeparator) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            AttributeSet attrs = elem.getElement(0).getAttributes();
            Record currentRecord = (Record) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
            if (currentRecord == null) continue;
            var tier = attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
            if ((tier != null || includeSeparator) && currentRecord == record) {
                return elem.getStartOffset();
            }
        }

        return -1;
    }

    public int getRecordStart(Tier<?> tier) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            Record record = (Record) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
            // If correct record
            if (record != null) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> currentTier = (Tier<?>)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                    // If correct tier
                    if (currentTier != null && currentTier == tier) {
                        return elem.getStartOffset();
                    }
                }
            }
        }

        return -1;
    }

    public int getRecordEnd(int recordIndex) {
        Element root = getDefaultRootElement();

        int retVal = -1;

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            AttributeSet attrs = elem.getElement(0).getAttributes();
            Record currentRecord = (Record) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
            if (currentRecord == null) continue;
            int currentRecordIndex = session.getRecordPosition(currentRecord);
            if (recordIndex == currentRecordIndex) {
                retVal = Math.max(retVal, elem.getEndOffset());
            }
        }

        return retVal;
    }

    public int getRecordEnd(Record record) {
        Element root = getDefaultRootElement();

        int retVal = -1;

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            AttributeSet attrs = elem.getElement(0).getAttributes();
            Record currentRecord = (Record) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
            if (currentRecord == null) continue;
            if (record == currentRecord) {
                retVal = Math.max(retVal, elem.getEndOffset());
            }
        }

        return retVal;
    }

    public int getRecordEnd(Tier<?> tier) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            Record currentRecord = (Record) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
            // If correct record index
            if (currentRecord != null) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    var currentTier = innerElem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                    // If correct tier
                    if (currentTier != null && currentTier == tier) {
                        return getRecordEnd(tier);
                    }
                }
            }
        }

        return -1;
    }

    public int getTierStart(int recordIndex, String tierName) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            Record currentRecord = (Record) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
            if (currentRecord == null) continue;
            int currentRecordIndex = session.getRecordPosition(currentRecord);
            // If correct record index
            if (currentRecordIndex == recordIndex) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> tier = (Tier<?>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                    Boolean isLabel = (Boolean) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL);
                    // If correct tier name
                    if (isLabel == null && tier != null && tier.getName().equals(tierName)) {
                        return innerElem.getStartOffset();
                    }
                }
            }
        }

        return -1;
    }

    public int getTierStart(Tier<?> tier) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() == 0) continue;
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
            // If correct record index
            if (transcriptElementType != null && transcriptElementType.equals(TranscriptStyleConstants.ATTR_KEY_RECORD)) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> currentTier = (Tier<?>)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                    Boolean isLabel = (Boolean)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL);
                    // If correct tier
                    if (isLabel == null && currentTier != null && currentTier == tier) {
                        return innerElem.getStartOffset();
                    }
                }
            }
        }

        return -1;
    }

    public int getTierEnd(int recordIndex, String tierName) {
        Element root = getDefaultRootElement();

        int retVal = -1;

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            Record currentRecord = (Record) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
            if (currentRecord == null) continue;
            int currentRecordIndex = session.getRecordPosition(currentRecord);
            // If correct record index
            if ((currentRecordIndex) == recordIndex) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> tier = (Tier<?>)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                    Boolean isLabel = (Boolean)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL);
                    if (isLabel == null && tier != null && tier.getName().equals(tierName)) {
                        retVal = Math.max(retVal, innerElem.getEndOffset());
                    }
                }
            }
        }

        return retVal;
    }

    public int getTierEnd(Tier<?> tier) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            Record currentRecord = (Record) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
            // If correct record index
            if (currentRecord != null) {
                int currentRecordIndex = session.getRecordPosition(currentRecord);
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    Tier<?> currentTier = (Tier<?>)innerElem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                    // If correct tier
                    if (currentTier != null && currentTier == tier) {
                        return getTierEnd(currentRecordIndex, tier.getName());
                    }
                }
            }
        }

        return -1;
    }

    public int getCommentStart(Comment comment) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() == 0) continue;
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
            // If transcript element type is comment
            if (transcriptElementType != null && transcriptElementType.equals(TranscriptStyleConstants.ATTR_KEY_COMMENT)) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Comment currentComment = (Comment)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_COMMENT);
                    Boolean isLabel = (Boolean)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL);
                    // If correct tier
                    if (isLabel == null && currentComment != null && currentComment == comment) {
                        return innerElem.getStartOffset();
                    }
                }
            }
        }

        return -1;
    }

    public int getCommentEnd(Comment comment) {
        Element root = getDefaultRootElement();

        int retVal = -1;

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
            // If transcript element type is comment
            if (transcriptElementType != null && transcriptElementType.equals(TranscriptStyleConstants.ATTR_KEY_COMMENT)) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Comment currentComment = (Comment)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_COMMENT);
                    Boolean isLabel = (Boolean)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL);
                    // If correct comment
                    if (isLabel == null && currentComment != null && currentComment == comment) {
                        retVal = Math.max(retVal, innerElem.getEndOffset());
                    }
                }
            }
        }

        return retVal;
    }

    public int getGemStart(Gem gem) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() == 0) continue;
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
            // If transcript element type is gem
            if (transcriptElementType != null && transcriptElementType.equals(TranscriptStyleConstants.ATTR_KEY_GEM)) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Gem currentGem = (Gem)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_GEM);
                    Boolean isLabel = (Boolean)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL);
                    // If correct tier
                    if (isLabel == null && currentGem != null && currentGem == gem) {
                        return innerElem.getStartOffset();
                    }
                }
            }
        }

        return -1;
    }

    public int getGemEnd(Gem gem) {
        Element root = getDefaultRootElement();

        int retVal = -1;

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
            // If transcript element type is gem
            if (transcriptElementType != null && transcriptElementType.equals(TranscriptStyleConstants.ATTR_KEY_GEM)) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Gem currentGem = (Gem) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_GEM);
                    Boolean isLabel = (Boolean)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL);
                    // If correct tier name
                    if (isLabel == null && currentGem != null && currentGem == gem) {
                        retVal = Math.max(retVal, innerElem.getEndOffset());
                    }
                }
            }
        }

        return retVal;
    }

    public int getGenericStart(Tier<?> genericTier) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() == 0) continue;
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
            // If transcript element type is tierData
            if (transcriptElementType != null && transcriptElementType.equals(TranscriptStyleConstants.ATTR_KEY_GENERIC)) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> currentGenericTier = (Tier<?>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_GENERIC);
                    Boolean isLabel = (Boolean)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL);
                    // If correct tierData
                    if (isLabel == null && currentGenericTier != null && currentGenericTier == genericTier) {
                        return innerElem.getStartOffset();
                    }
                }
            }
        }

        return -1;
    }

    public int getGenericEnd(Tier<?> genericTier) {
        int retVal = -1;

        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
            // If transcript element type is tierData
            if (transcriptElementType != null && transcriptElementType.equals(TranscriptStyleConstants.ATTR_KEY_GENERIC)) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> currentGenericTier = (Tier<?>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_GENERIC);
                    Boolean isLabel = (Boolean)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL);
                    // If correct tierData
                    if (isLabel == null && currentGenericTier != null && currentGenericTier.toString().equals(genericTier.toString())) {
                        retVal = Math.max(retVal, innerElem.getEndOffset());
                    }
                }
            }
        }

        return retVal;
    }

    public Tuple<Integer, Integer> getSegmentBounds(MediaSegment segment, int includedPos) {
        Element root = getDefaultRootElement();

        int indexInSegment = -1;

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
            // If correct record index
            if (transcriptElementType != null && transcriptElementType.equals(TranscriptStyleConstants.ATTR_KEY_RECORD)) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    MediaSegment elemSegment = (MediaSegment) innerElem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_MEDIA_SEGMENT);
                    boolean includedPosInElem = includedPos < innerElem.getEndOffset() && includedPos >= innerElem.getStartOffset();
                    if (elemSegment != null && elemSegment == segment && includedPosInElem) {
                        indexInSegment = innerElem.getStartOffset();
                        i = root.getElementCount();;
                        j = elem.getElementCount();
                    }
                }
            }
        }

        Tuple<Integer, Integer> retVal = new Tuple<>(-1, -1);

        if (indexInSegment == -1) return retVal;

        int segmentStart = indexInSegment;

        AttributeSet attrs = getCharacterElement(segmentStart).getAttributes();
        while (attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_MEDIA_SEGMENT) != null) {
            segmentStart--;
            attrs = getCharacterElement(segmentStart).getAttributes();
        }
        retVal.setObj1(segmentStart + 1);

        int segmentEnd = indexInSegment;
        attrs = getCharacterElement(segmentEnd).getAttributes();
        while (attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_MEDIA_SEGMENT) != null) {
            segmentEnd++;
            attrs = getCharacterElement(segmentEnd).getAttributes();
        }
        retVal.setObj2(segmentEnd - 1);

        return retVal;
    }

    // endregion Get Record/Tier Start/End


    // region Tier View Changes

    public void moveTier(List<TierViewItem> movedTiers) {

        movedTiers = movedTiers.stream().filter(item -> item.isVisible()).toList();
        if (movedTiers.size() < 1) return;

        boolean alignmentParentMoved = false;
        List<TierViewItem> movedTiersNewOrder = new ArrayList<>();
        for (TierViewItem item : session.getTierView()) {
            if (movedTiers.contains(item)) {
                movedTiersNewOrder.add(item);

                String tierName = item.getTierName();
                // If one of the syllabification tiers are moved remove the syllabification
                if ((tierName.equals("IPA Target") || tierName.equals("IPA Actual")) && syllabificationVisible) {
                    try {
                        removeSyllabification(tierName);
                    }
                    catch (BadLocationException e) {
                        LogUtil.severe(e);
                    }
                }
                // If the tier with alignment is hidden remove the alignment and set a flag to add it back correctly
                if (alignmentVisible && alignmentParent != null && tierName.equals(alignmentParent.getTierName())) {
                    try {
                        removeAlignment();
                    }
                    catch (BadLocationException e) {
                        LogUtil.severe(e);
                    }
                }
            }
        }

        // Reload the contents of the editor
        if (singleRecordView) {
            moveTierInRecord(singleRecordIndex, movedTiers, movedTiersNewOrder);
        }
        else {
            int recordCount = session.getRecordCount();
            for (int i = 0; i < recordCount; i++) {
                moveTierInRecord(i, movedTiers, movedTiersNewOrder);
            }
        }

        updateTiersHeader();
    }

    private void moveTierInRecord(int recordIndex, List<TierViewItem> movedTiers, List<TierViewItem> movedTiersNewOrder) {
        int labelLength = labelColumnWidth + 2;

        SimpleAttributeSet recordAttrs = getRecordAttributes(recordIndex);
        Record record = session.getRecord(recordIndex);

        movedTiers = movedTiers.stream().filter(item -> record.hasTier(item.getTierName())).toList();
        if (movedTiers.size() < 2) return;

        int offset = -1;
        for (TierViewItem item : movedTiers) {
            try {
                String tierName = item.getTierName();

                int tierStartOffset = getTierStart(recordIndex, tierName) - labelLength;
                int tierEndOffset = getTierEnd(recordIndex, tierName);

                bypassDocumentFilter = true;
                remove(tierStartOffset, tierEndOffset - tierStartOffset);

                if (offset == -1) {
                    offset = tierStartOffset;
                }
            }
            catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        }



        try {
            appendBatchEndStart();
            for (TierViewItem item : movedTiersNewOrder) {
                var tier = record.getTier(item.getTierName());
                var attrs = insertTier(recordIndex, tier, item, recordAttrs);
                appendBatchLineFeed(attrs);
            }
            processBatchUpdates(offset);
            setGlobalParagraphAttributes();
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    public void deleteTier(List<String> deletedTiers) {
        if (singleRecordView) {
            deleteTierFromRecord(singleRecordIndex, deletedTiers);
        }
        else {
            int recordCount = session.getRecordCount();
            for (int i = 0; i < recordCount; i++) {
                deleteTierFromRecord(i, deletedTiers);
            }
        }

        updateTiersHeader();
    }

    private void deleteTierFromRecord(int recordIndex, List<String> deletedTiers) {
        int labelLength = labelColumnWidth + 2;
        try {
            for (String tierName : deletedTiers) {
                int tierStartOffset = getTierStart(recordIndex, tierName) - labelLength;
                int tierEndOffset = getTierEnd(recordIndex, tierName);

                if (tierStartOffset < 0 || tierEndOffset < 0) continue;

                bypassDocumentFilter = true;
                remove(tierStartOffset, tierEndOffset - tierStartOffset);
            }
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    public void addTier(List<TierViewItem> addedTiers) {
        if (singleRecordView) {
            addTierToRecord(singleRecordIndex, addedTiers);
        }
        else {
            int recordCount = session.getRecordCount();
            for (int i = 0; i < recordCount; i++) {
                addTierToRecord(i, addedTiers);
            }
        }

        if (alignmentVisible && alignmentParent != null) {
            TierViewItem calculatedAlignmentParent = calculateAlignmentParent();
            if (!calculatedAlignmentParent.getTierName().equals(alignmentParent.getTierName())) {
                System.out.println("Current alignment parent: " + alignmentParent.getTierName());
                System.out.println("Calculated alignment parent: " + calculatedAlignmentParent.getTierName());
                try {
                    alignmentParent = calculatedAlignmentParent;
                    removeAlignment();
                    appendAlignmentToParent();
                }
                catch (BadLocationException e) {
                    LogUtil.severe(e);
                }
            }
        }

        updateTiersHeader();
    }

    private void addTierToRecord(int recordIndex, List<TierViewItem> addedTiers) {
        int offset = getRecordEnd(recordIndex);
        Record record = session.getRecord(recordIndex);
        try {
            appendBatchEndStart();
            for (TierViewItem item : addedTiers) {
                var tier = record.getTier(item.getTierName());
                var attrs = insertTier(recordIndex, tier, item, getRecordAttributes(recordIndex));
                appendBatchLineFeed(attrs);
            }
            processBatchUpdates(offset);
            setGlobalParagraphAttributes();
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    public void hideTier(List<String> hiddenTiers) {
        // Hide the tiers
        if (singleRecordView) {
            hideTierInRecord(singleRecordIndex, hiddenTiers);
        }
        else {
            int recordCount = session.getRecordCount();
            for (int i = 0; i < recordCount; i++) {
                hideTierInRecord(i, hiddenTiers);
            }
        }

        boolean alignmentParentRemoved = false;
        for (String tierName : hiddenTiers) {
            // If a tier with syllabification enabled was hidden remove the syllabification
            if ((tierName.equals("IPA Target") || tierName.equals("IPA Actual")) && syllabificationVisible) {
                try {
                    removeSyllabification(tierName);
                }
                catch (BadLocationException e) {
                    LogUtil.severe(e);
                }
            }
            // If the tier with alignment is hidden remove the alignment and set a flag to add it back correctly
            if (alignmentVisible && alignmentParent != null && tierName.equals(alignmentParent.getTierName())) {
                try {
                    removeAlignment();
                    alignmentParentRemoved = true;
                }
                catch (BadLocationException e) {
                    LogUtil.severe(e);
                }
            }
        }

        // If needed, add the alignment back
        if (alignmentParentRemoved) {
            alignmentParent = calculateAlignmentParent();
            System.out.println("Alignment parent removed");
            System.out.println("Appending it to: " + alignmentParent.getTierName());
            appendAlignmentToParent();
        }

        updateTiersHeader();
    }

    private void hideTierInRecord(int recordIndex, List<String> hiddenTiers) {
        try {
            int labelLength = labelColumnWidth + 2;
            Record record = session.getRecord(recordIndex);
            for (String tierName : hiddenTiers) {
                if (!record.hasTier(tierName)) continue;

                int tierStartOffset = getTierStart(recordIndex, tierName) - labelLength;
                int tierEndOffset = getTierEnd(recordIndex, tierName);
                bypassDocumentFilter = true;
                remove(tierStartOffset, tierEndOffset - tierStartOffset);
            }
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }

    }

    public void showTier(List<TierViewItem> shownTiers, List<TierViewItem> newTierView) {
        if (singleRecordView) {
            showTierInRecord(singleRecordIndex, shownTiers, newTierView);
        }
        else {
            int recordCount = session.getRecordCount();
            for (int i = 0; i < recordCount; i++) {
                showTierInRecord(i, shownTiers, newTierView);
            }
        }

        if (alignmentVisible && alignmentParent != null) {
            TierViewItem calculatedAlignmentParent = calculateAlignmentParent();
            if (!calculatedAlignmentParent.getTierName().equals(alignmentParent.getTierName())) {
                alignmentParent = calculatedAlignmentParent;
                System.out.println("Alignment parent added");
                System.out.println("Appending it to: " + alignmentParent.getTierName());
                try {
                    removeAlignment();
                    appendAlignmentToParent();
                }
                catch (BadLocationException e) {
                    LogUtil.severe(e);
                }
            }
        }

        updateTiersHeader();
    }

    private void showTierInRecord(int recordIndex, List<TierViewItem> shownTiers, List<TierViewItem> newTierView) {
        SimpleAttributeSet recordAttrs = getRecordAttributes(recordIndex);
        Record record = session.getRecord(recordIndex);
        int offset = getRecordStart(recordIndex);
        try {
            for (TierViewItem item : shownTiers) {
                if (!record.hasTier(item.getTierName())) continue;

                TierViewItem itemBeforeShownItem = null;
                for (TierViewItem newItem : newTierView) {
                    if (newItem.equals(item)) {
                        break;
                    }
                    if (newItem.isVisible() && record.hasTier(newItem.getTierName())) {
                        itemBeforeShownItem = newItem;
                    }
                }

                if (itemBeforeShownItem != null) {
                    if (syllabificationVisible) {
                        if (itemBeforeShownItem.getTierName().equals("IPA Target")) {
                            offset = getTierEnd(recordIndex, SystemTierType.TargetSyllables.getName());
                        }
                        else if (itemBeforeShownItem.getTierName().equals("IPA Actual")) {
                            offset = getTierEnd(recordIndex, SystemTierType.ActualSyllables.getName());
                        }
                        else {
                            offset = getTierEnd(recordIndex, itemBeforeShownItem.getTierName());
                        }
                    }
                    else {
                        offset = getTierEnd(recordIndex, itemBeforeShownItem.getTierName());
                    }
                }

                appendBatchEndStart();
                var tier = record.getTier(item.getTierName());
                var attrs = insertTier(recordIndex, tier, item, recordAttrs);
                appendBatchLineFeed(attrs);
                processBatchUpdates(offset);
                setGlobalParagraphAttributes();
            }
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    public void tierFontChanged(List<TierViewItem> changedTiers) {
        if (singleRecordView) {
            tierFontChangedInRecord(singleRecordIndex, changedTiers);
        }
        else {
            int recordCount = session.getRecordCount();
            for (int i = 0; i < recordCount; i++) {
                tierFontChangedInRecord(i, changedTiers);
            }
        }
    }

    private void tierFontChangedInRecord(int recordIndex, List<TierViewItem> changedTiers) {
        int labelLength = labelColumnWidth + 2;

        SimpleAttributeSet recordAttrs = getRecordAttributes(recordIndex);
        Record record = session.getRecord(recordIndex);
        try {
            for (TierViewItem item : changedTiers) {
                String tierName = item.getTierName();
                if (!record.hasTier(tierName)) continue;

                int tierStartOffset = getTierStart(recordIndex, tierName) - labelLength;
                int tierEndOffset = getTierEnd(recordIndex, tierName);

                bypassDocumentFilter = true;
                remove(tierStartOffset, tierEndOffset - tierStartOffset);

                appendBatchEndStart();
                var tier = record.getTier(item.getTierName());
                var attrs = insertTier(recordIndex, tier, item, recordAttrs);
                appendBatchLineFeed(attrs);
                processBatchUpdates(tierStartOffset);
                setGlobalParagraphAttributes();
            }
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    public void tierNameChanged(List<TierViewItem> oldTiers, List<TierViewItem> newTiers) {
        if (singleRecordView) {
            tierNameChangedInRecord(singleRecordIndex, oldTiers, newTiers);
        }
        else {
            int recordCount = session.getRecordCount();
            for (int i = 0; i < recordCount; i++) {
                tierNameChangedInRecord(i, oldTiers, newTiers);
            }
        }

        updateTiersHeader();
    }

    private void tierNameChangedInRecord(int recordIndex, List<TierViewItem> oldTiers, List<TierViewItem> newTiers) {
        try {
            int labelLength = labelColumnWidth + 2;

            SimpleAttributeSet recordAttrs = getRecordAttributes(recordIndex);
            Record record = session.getRecord(recordIndex);
            for (int i = 0; i < oldTiers.size(); i++) {
                TierViewItem newTier = newTiers.get(i);
                if (!record.hasTier(newTier.getTierName())) continue;
                String oldTierName = oldTiers.get(i).getTierName();

                int tierStartOffset = getTierStart(recordIndex, oldTierName) - labelLength;
                int tierEndOffset = getTierEnd(recordIndex, oldTierName);

                bypassDocumentFilter = true;
                remove(tierStartOffset, tierEndOffset - tierStartOffset);

                appendBatchEndStart();
                var tier = record.getTier(newTier.getTierName());
                var attrs = insertTier(recordIndex, tier, newTier, recordAttrs);
                appendBatchLineFeed(attrs);
                processBatchUpdates(tierStartOffset);
                setGlobalParagraphAttributes();
            }
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    // endregion Tier View Changes


    // region Record Changes

    public void addRecord(Record addedRecord) {
        try {
            appendBatchEndStart();
            AttributeSet attrs = writeRecord(addedRecord, session.getTranscript(), session.getTierView());
            appendBatchLineFeed(attrs);
            processBatchUpdates(getLength());
            setGlobalParagraphAttributes();
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    public void deleteRecord(Record removedRecord) {
        try {
            int start = getRecordStart(removedRecord, true);
            int end = getRecordEnd(removedRecord);

            bypassDocumentFilter = true;
            remove(start, end - start);
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    public void moveRecord(int oldRecordIndex, int newRecordIndex, int oldElementIndex, int newElementIndex) {
        try {
            Transcript transcript = session.getTranscript();
            var tierView = session.getTierView();
            AttributeSet newLineAttrs;

            int start = getRecordStart(Math.min(oldRecordIndex, newRecordIndex));
            int end = getRecordEnd(Math.max(oldRecordIndex, newRecordIndex));

            bypassDocumentFilter = true;
            remove(start, end - start);

            appendBatchEndStart();

            int startElementIndex = Math.min(oldElementIndex, newElementIndex);
            int endElementIndex = Math.max(oldElementIndex, newElementIndex);

            for (int i = startElementIndex; i < endElementIndex + 1; i++) {
                Transcript.Element elem = transcript.getElementAt(i);
                if (elem.isRecord()) {
                    newLineAttrs = writeRecord(elem.asRecord(), transcript, tierView);
                }
                else if (elem.isComment()) {
                    newLineAttrs = writeComment(elem.asComment());
                }
                else {
                    newLineAttrs = writeGem(elem.asGem());
                }

                appendBatchLineFeed(newLineAttrs);
            }

            processBatchUpdates(start);
            setGlobalParagraphAttributes();
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    public void changeSpeaker(Record record) {
        try {
            int recordIndex = session.getRecordPosition(record);
            int start = getRecordStart(recordIndex, true);

            var tierView = session.getTierView();
            String firstVisibleTierName = tierView
                .stream()
                .filter(item -> item.isVisible())
                .findFirst()
                .get()
                .getTierName();
            int end = getTierStart(recordIndex, firstVisibleTierName) - (labelColumnWidth + 2);

            bypassDocumentFilter = true;
            remove(start, end - start);

            SimpleAttributeSet sepAttrs = getSeparatorAttributes();
            sepAttrs.addAttributes(getRecordAttributes(recordIndex));

            appendBatchEndStart();

            appendBatchString(formatLabelText(record.getSpeaker().toString()) + "  ", sepAttrs);

            MediaSegment segment = record.getMediaSegment();

            sepAttrs.addAttributes(getStandardFontAttributes());
            formatSegment(segment, sepAttrs);

            processBatchUpdates(start);
            setGlobalParagraphAttributes();
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    public void onTierDataChanged(Tier<?> tier) {
        if (tier.getName().equals(SystemTierType.TargetSyllables.getName()) || tier.getName().equals(SystemTierType.ActualSyllables.getName())) return;

        System.out.println(tier.getDeclaredType().getName());

        try {
            int start = getTierStart(tier);
            int recordIndex = getRecordIndex(start);
            start -= labelColumnWidth + 2;
            int end = getTierEnd(tier);

            bypassDocumentFilter = true;
            remove(start, end - start);

            var tierView = session.getTierView();
            TierViewItem tierViewItem =  tierView.stream().filter(item -> item.getTierName().equals(tier.getName())).findFirst().orElse(null);

            appendBatchEndStart();

            SimpleAttributeSet attrs;
            if (tier.getDeclaredType().equals(PhoneAlignment.class)) {
                Record record = session.getRecord(getRecordIndex(start));
                attrs = appendAlignmentToTierInRecord(record.getTier(getAlignmentParent().getTierName()), record);
            }
            else {
                attrs = insertTier(recordIndex, tier, tierViewItem, getRecordAttributes(recordIndex));
            }


            appendBatchLineFeed(attrs);

            processBatchUpdates(start);
            setGlobalParagraphAttributes();
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    // endregion Record Changes


    public TierViewItem calculateAlignmentParent() {
        List<TierViewItem> visibleTierView = session.getTierView().stream().filter(item -> item.isVisible()).toList();

        var retVal = visibleTierView.stream().filter(item -> item.getTierName().equals("IPA Actual")).findFirst();
        if (retVal.isPresent()) return retVal.get();

        retVal = visibleTierView.stream().filter(item -> item.getTierName().equals("IPA Target")).findFirst();
        if (retVal.isPresent()) return retVal.get();

        return visibleTierView.get(visibleTierView.size()-1);
    }

    private void appendAlignmentToParent() {
        try {
            String appendingTierName = alignmentParent.getTierName();

            System.out.println("Appending tier name: " + appendingTierName);

            if (syllabificationVisible) {
                if (appendingTierName.equals("IPA Target")) {
                    appendingTierName = SystemTierType.TargetSyllables.getName();;
                }
                else if (appendingTierName.equals("IPA Actual")) {
                    appendingTierName = SystemTierType.ActualSyllables.getName();
                }
            }

            if (singleRecordView) {
                System.out.println("Appending tier name: " + appendingTierName);
                appendBatchEndStart();
                Record record = session.getRecord(singleRecordIndex);
                var attrs = appendAlignmentToTierInRecord(record.getTier(appendingTierName), record);
                appendBatchLineFeed(attrs);
                System.out.println("Appending tier name: " + appendingTierName);
                int tierEnd = getTierEnd(singleRecordIndex, appendingTierName);
                System.out.println("Process batch at: " + tierEnd);
                processBatchUpdates(tierEnd);
            }
            else {
                int recordCount = session.getRecordCount();
                for (int i = 0; i < recordCount; i++) {
                    appendBatchEndStart();
                    Record record = session.getRecord(i);
                    var attrs = appendAlignmentToTierInRecord(record.getTier(appendingTierName), record);
                    appendBatchLineFeed(attrs);
                    processBatchUpdates(getTierEnd(i, appendingTierName));
                }
            }
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    private SimpleAttributeSet appendAlignmentToTierInRecord(Tier<?> tier, Record record) {
        // Get the alignment tier
        Tier<PhoneAlignment> alignmentTier = record.getPhoneAlignmentTier();
        // Set up the tier attributes for the dummy tier
        var tierAttrs = getTierAttributes(tier);
        tierAttrs.addAttributes(getTierAttributes(alignmentTier));
        // Set up the attributes for its label
        SimpleAttributeSet alignmentLabelAttrs = getTierLabelAttributes(alignmentTier);
        // Set up record attributes
        SimpleAttributeSet recordAttrs = getRecordAttributes(session.getRecordPosition(record));
        alignmentLabelAttrs.addAttributes(recordAttrs);
        tierAttrs.addAttributes(recordAttrs);
        // Get the string for the label
        String alignmentLabelText = formatLabelText("Alignment");
        // Add the label
        appendBatchString(alignmentLabelText + ": ", alignmentLabelAttrs);
        // Get the string version of the alignment
        String alignmentContent = alignmentTier.getValue().toString();
        // Add component factory if needed
        if (alignmentIsComponent) {
            tierAttrs.addAttributes(getAlignmentAttributes());
        }
        appendBatchString(alignmentContent, tierAttrs);

        return tierAttrs;
    }

    private void removeAlignment() throws BadLocationException {
        Element root = getDefaultRootElement();
        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            for (int j = 0; j < elem.getElementCount(); j++) {
                Element innerElem = elem.getElement(j);
                var attrs = innerElem.getAttributes();
                Tier<?> tier = (Tier<?>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                if (tier != null) {
                    boolean correctTier = tier.getName().equals("Alignment");
                    if (correctTier) {
                        int tierStartOffset = getTierStart(tier) - labelColumnWidth - 2;
                        int tierEndOffset = getTierEnd(tier);
                        if (tierStartOffset >= 0 && tierEndOffset >= 0) {
                            System.out.println("Removed alignment ---------------------------");
                            bypassDocumentFilter = true;
                            remove(tierStartOffset, tierEndOffset - tierStartOffset);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void removeSyllabification(String tierName) throws BadLocationException {
        Element root = getDefaultRootElement();
        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            for (int j = 0; j < elem.getElementCount(); j++) {
                Element innerElem = elem.getElement(j);
                var attrs = innerElem.getAttributes();
                Tier<?> tier = (Tier<?>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                if (tier != null) {
                    boolean correctTier;
                    if (tierName.equals("IPA Target")) {
                        correctTier = tier.getName().equals(SystemTierType.TargetSyllables.getName());
                    } else {
                        correctTier = tier.getName().equals(SystemTierType.ActualSyllables.getName());
                    }
                    if (correctTier) {
                        int tierStartOffset = getTierStart(tier) - labelColumnWidth - 2;
                        int tierEndOffset = getTierEnd(tier);
                        if (tierStartOffset >= 0 && tierEndOffset >= 0) {
                            bypassDocumentFilter = true;
                            remove(tierStartOffset, tierEndOffset - tierStartOffset);
                        }
                    }
                }
            }
        }
    }

    public Character getCharAtPos(int pos) {
        try {
            return getText(pos, 1).charAt(0);
        }
        catch (BadLocationException e) {
            LogUtil.warning(e);
            return null;
        }
    }

    private String formatLabelText(String labelText) {
        int labelTextLen = labelText.length();
        if (labelTextLen < labelColumnWidth) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < (labelColumnWidth - labelTextLen); i++) {
                builder.append(' ');
            }
            builder.append(labelText);
            return builder.toString();
        }
        else if (labelTextLen > labelColumnWidth) {
            String remaining = labelText.substring(labelTextLen - labelColumnWidth + 3, labelTextLen);
            return "..." + remaining;
        }
        return labelText;
    }

    public void reload() {
        try {
            bypassDocumentFilter = true;
            remove(0, getLength());
            populate();
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    public int getRecordIndex(int offset) {
        AttributeSet attributes = getCharacterElement(offset).getAttributes();
        Record record = (Record) attributes.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
        if (record == null) return -1;
        return session.getRecordPosition(record);
    }

    public int getRecordElementIndex(int offset) {
        AttributeSet attributes = getCharacterElement(offset).getAttributes();
        Record record = (Record) attributes.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
        if (record == null) return -1;
        return session.getRecordElementIndex(record);
    }

    public Tier<?> getTier(int offset) {
        AttributeSet attributes = getCharacterElement(offset).getAttributes();
        Tier<?> tier = (Tier<?>) attributes.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
        if (tier == null) {
            return null;
        }
        return tier;
    }

    private void formatSegment(MediaSegment segment, AttributeSet additionalAttrs) {
        formatSegment(segment, additionalAttrs, MediaTimeFormatStyle.PADDED_MINUTES_AND_SECONDS);
    }

    private void formatSegment(MediaSegment segment, AttributeSet additionalAttrs, MediaTimeFormatStyle style) {

        var formatter = new MediaSegmentFormatter(style);
        String value = formatter.format(segment);

        var segmentTimeAttrs = getSegmentTimeAttributes(segment);
        var segmentDashAttrs = getSegmentDashAttributes(segment);
        segmentTimeAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);
        segmentDashAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);
        if (additionalAttrs != null) {
            segmentTimeAttrs.addAttributes(additionalAttrs);
            segmentDashAttrs.addAttributes(additionalAttrs);
        }

        appendBatchString("•", segmentDashAttrs);

        appendBatchString(value, segmentTimeAttrs);

//
//        appendBatchString(start, segmentTimeAttrs);
//
//        if (!segment.isPoint()) {
//            appendBatchString("-", segmentDashAttrs);
//
//            String end = MediaTimeFormatter.timeToString(segment.getEndValue(), style);
//
//            appendBatchString(end, segmentTimeAttrs);
//        }
//
        appendBatchString("•", segmentDashAttrs);
    }

    public void formatInternalMedia(InternalMedia internalMedia, AttributeSet additionalAttrs) {
        MediaSegment segment = sessionFactory.createMediaSegment();
        segment.setSegment(internalMedia.getStartTime(), internalMedia.getEndTime(), MediaUnit.Second);
        formatSegment(segment, additionalAttrs, MediaTimeFormatStyle.MINUTES_AND_SECONDS);
    }

    private void formatSyllabification(IPATranscript ipaTranscript, AttributeSet additionalAttrs) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        if (additionalAttrs != null) attrs.addAttributes(additionalAttrs);
        attrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_SYLLABIFICATION, true);

        Set<SyllableConstituentType> hiddenConstituent = new HashSet<>();
        hiddenConstituent.add(SyllableConstituentType.SYLLABLESTRESSMARKER);
        hiddenConstituent.add(SyllableConstituentType.UNKNOWN);
        hiddenConstituent.add(SyllableConstituentType.WORDBOUNDARYMARKER);

        for (IPAElement p : ipaTranscript) {
            attrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_PHONE, p);
            attrs.removeAttribute(StyleConstants.Foreground);
            attrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_TRAVERSABLE, true);
            attrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);
            if (p.getScType().equals(SyllableConstituentType.UNKNOWN)) {
                StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.IPA_PAUSE));
            }
            appendBatchString(p.toString(), attrs);
            final SyllabificationInfo sInfo = p.getExtension(SyllabificationInfo.class);
            if (hiddenConstituent.contains(sInfo.getConstituentType())) continue;
            appendBatchString(":", attrs);
            attrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_TRAVERSABLE);
            attrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE);
            if(sInfo.getConstituentType() == SyllableConstituentType.NUCLEUS && sInfo.isDiphthongMember()) {
                StyleConstants.setForeground(attrs, Color.RED);
                appendBatchString("D", attrs);
            }
            else {
                StyleConstants.setForeground(attrs, sInfo.getConstituentType().getColor());
                appendBatchString(String.valueOf(sInfo.getConstituentType().getIdChar()), attrs);
            }
        }
        attrs.removeAttribute(StyleConstants.Foreground);
    }

    private void setGlobalParagraphAttributes() {
        SimpleAttributeSet paragraphAttrs = new SimpleAttributeSet();
        StyleConstants.setLineSpacing(paragraphAttrs, getLineSpacing());
        StyleConstants.setForeground(paragraphAttrs, UIManager.getColor(TranscriptEditorUIProps.FOREGROUND));

        // Soft wrap
        Font font = FontPreferences.getMonospaceFont().deriveFont(14.0f);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < labelColumnWidth + 2; i++) {
            builder.append(" ");
        }
        FontMetrics fm = new JLabel().getFontMetrics(font);
        int indent = fm.stringWidth(builder.toString());
        int rightInset =  fm.stringWidth(" ");
        StyleConstants.setLeftIndent(paragraphAttrs, indent);
        StyleConstants.setRightIndent(paragraphAttrs, rightInset);
        StyleConstants.setFirstLineIndent(paragraphAttrs, -indent);

        setParagraphAttributes(0, getLength(), paragraphAttrs, false);
    }

    private SimpleAttributeSet insertTier(int recordIndex, Tier<?> tier, TierViewItem tierViewItem, AttributeSet recordAttrs) {
        String tierName = tier.getName();
        Record record = session.getRecord(recordIndex);

        SimpleAttributeSet tierAttrs = getTierAttributes(tier, tierViewItem);
        if (recordAttrs != null) {
            tierAttrs.addAttributes(recordAttrs);
        }

        SimpleAttributeSet labelAttrs = getTierLabelAttributes(tier);
        if (recordAttrs != null) {
            labelAttrs.addAttributes(recordAttrs);
        }

        String labelText = tierName;
        if (labelText.length() < labelColumnWidth) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < (labelColumnWidth - labelText.length()); i++) {
                builder.append(' ');
            }
            appendBatchString(builder.toString(), labelAttrs);
        }
        else {
            labelText = formatLabelText(labelText);
        }

        labelAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE, true);
        appendBatchString(labelText, labelAttrs);

        labelAttrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE);
        appendBatchString(": ", labelAttrs);

        Class<?> tierType = tier.getDeclaredType();

        if(!tier.hasValue()) {
            appendBatchString("", tierAttrs);
        }
        else if (tier.isUnvalidated()) {
            appendBatchString(tier.getUnvalidatedValue().getValue(), tierAttrs);
        }
        else {
            if (tierType.equals(IPATranscript.class)) {
                Tier<IPATranscript> ipaTier = (Tier<IPATranscript>)tier;
                List<IPATranscript> words = (ipaTier).getValue().words();
                for (int i = 0; i < words.size(); i++) {
                    var word = words.get(i);
                    SimpleAttributeSet attrs;
                    if (word.matches("\\P")) {
                        // Pause
                        attrs = getIPAPauseAttributes(ipaTier);
                    } else {
                        // Word
                        attrs = getIPAWordAttributes(ipaTier);
                    }
                    attrs.addAttributes(tierAttrs);
                    String content = word.toString();
                    appendBatchString(content, attrs);

                    if (i < words.size() - 1) {
                        appendBatchString(" ", tierAttrs);
                    }
                }
                if (tierName.equals("IPA Target") && syllabificationVisible) {
                    // Add a newline at the end of the regular tier content
                    appendBatchLineFeed(tierAttrs);
                    // Create a dummy tier for the syllabification
                    IPATranscript ipaTarget = ipaTier.getValue();
                    Tier<IPATranscript> syllableTier = sessionFactory.createTier(SystemTierType.TargetSyllables.getName(), IPATranscript.class);
                    syllableTier.setValue(ipaTarget);
                    // Set up the tier attributes for the dummy tier
                    tierAttrs = new SimpleAttributeSet(tierAttrs);
                    tierAttrs.addAttributes(getTierAttributes(syllableTier));
                    // Set up the attributes for its label
                    SimpleAttributeSet syllabificationLabelAttrs = getTierLabelAttributes(syllableTier);
                    if (recordAttrs != null) {
                        syllabificationLabelAttrs.addAttributes(recordAttrs);
                    }
                    // Get the string for the label
                    String syllabificationLabelText = formatLabelText("Syllabification");
                    // Add the label
                    appendBatchString(syllabificationLabelText + ": ", syllabificationLabelAttrs);
                    // Add component factory if needed
                    if (syllabificationIsComponent) {
                        tierAttrs.addAttributes(getSyllabificationAttributes());
                    }
                    // Append the content
                    formatSyllabification(syllableTier.getValue(), tierAttrs);
                }
                else if (tierName.equals("IPA Actual") && syllabificationVisible) {
                    // Add a newline at the end of the regular tier content
                    appendBatchLineFeed(tierAttrs);
                    // Create a dummy tier for the syllabification
                    IPATranscript ipaActual = ipaTier.getValue();
                    Tier<IPATranscript> syllableTier = sessionFactory.createTier(SystemTierType.ActualSyllables.getName(), IPATranscript.class);
                    syllableTier.setValue(ipaActual);
                    // Set up the tier attributes for the dummy tier
                    tierAttrs = new SimpleAttributeSet(tierAttrs);
                    tierAttrs.addAttributes(getTierAttributes(syllableTier));
                    // Set up the attributes for its label
                    SimpleAttributeSet syllabificationLabelAttrs = getTierLabelAttributes(syllableTier);
                    if (recordAttrs != null) {
                        syllabificationLabelAttrs.addAttributes(recordAttrs);
                    }
                    // Get the string for the label
                    String syllabificationLabelText = formatLabelText("Syllabification");
                    // Add the label
                    appendBatchString(syllabificationLabelText + ": ", syllabificationLabelAttrs);
                    // Add component factory if needed
                    if (syllabificationIsComponent) {
                        tierAttrs.addAttributes(getSyllabificationAttributes());
                    }
                    // Append the content
                    formatSyllabification(syllableTier.getValue(), tierAttrs);
                }
            }
            else if (tierType.equals(MediaSegment.class)) {
                MediaSegment segment = record.getMediaSegment();
                formatSegment(segment, tierAttrs);
            }
            else if (tierType.equals(Orthography.class)) {
                Tier<Orthography> orthographyTier = (Tier<Orthography>) tier;
                orthographyTier.getValue().accept(new TranscriptOrthographyVisitors.KeywordVisitor(this, tierAttrs));
                //appendBatchString(tierContent, tierAttrs);
            }
            else if (tierType.equals(MorTierData.class)) {
                Tier<MorTierData> morTier = (Tier<MorTierData>) tier;
                MorTierData mors = morTier.getValue();

                for (int i = 0; i < mors.size(); i++) {
                    Mor mor = mors.get(i);
                    appendBatchString(mor.toString(), tierAttrs);
                    if (i < mors.size() - 1) {
                        appendBatchString(" ", tierAttrs);
                    }
                }
            }
            else if (tierType.equals(GraspTierData.class)) {
                Tier<GraspTierData> graspTier = (Tier<GraspTierData>) tier;
                GraspTierData grasps = graspTier.getValue();

                for (int i = 0; i < grasps.size(); i++) {
                    Grasp grasp = grasps.get(i);
                    appendBatchString(grasp.toString(), tierAttrs);
                    if (i < grasps.size() - 1) {
                        appendBatchString(" ", tierAttrs);
                    }
                }
            }
            else if (tierType.equals(TierData.class))  {
                Tier<TierData> userTier = (Tier<TierData>) tier;
                TierData tierData = userTier.getValue();
                if (tierData != null) {
                    for (int i = 0; i < tierData.length(); i++) {
                        TierElement elem = tierData.elementAt(i);
                        String text = null;
                        SimpleAttributeSet attrs;
                        if (elem instanceof TierString tierString) {
                            text = tierString.text();
                            attrs = getTierStringAttributes();
                        }
                        else if (elem instanceof TierComment comment) {
                            text = comment.toString();
                            attrs = getTierCommentAttributes();
                        }
                        else if (elem instanceof TierInternalMedia internalMedia) {
                            attrs = getTierInternalMediaAttributes();
                            formatInternalMedia(internalMedia.getInternalMedia(), attrs);
                        }
                        else if (elem instanceof TierLink link) {
                            text = link.toString();
                            attrs = getTierLinkAttributes();
                        }
                        else {
                            throw new RuntimeException("Invalid type");
                        }

                        attrs.addAttributes(tierAttrs);

                        if (text != null) appendBatchString(text, attrs);

                        if (i < tierData.length() - 1) {
                            appendBatchString(" ", tierAttrs);
                        }
                    }
                }
            }
        }

        if (alignmentVisible && alignmentParent != null && tierName.equals(alignmentParent.getTierName())) {
            tierAttrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_COMPONENT_FACTORY);
            // Add a newline at the end of the regular tier content
            appendBatchLineFeed(tierAttrs);
            // Append the alignment
            tierAttrs = appendAlignmentToTierInRecord(tier, record);
        }

        tierAttrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_COMPONENT_FACTORY);
        return tierAttrs;
    }

    public void setTierItemViewLocked(String tierName, boolean locked) {
        var currentTierVIew = session.getTierView();
        List<TierViewItem> newTierView = new ArrayList<>();
        for (TierViewItem oldItem : currentTierVIew) {
            if (oldItem.getTierName().equals(tierName)) {
                final TierViewItem newItem = sessionFactory.createTierViewItem(
                    oldItem.getTierName(),
                    oldItem.isVisible(),
                    oldItem.getTierFont(),
                    locked
                );
                newTierView.add(newItem);
            }
            else {
                newTierView.add(oldItem);
            }
        }
        session.setTierView(newTierView);
    }

    public int getOffsetInContent(int pos) {
        Element elem = getCharacterElement(pos);
        String transcriptElementType = (String) elem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
        if (transcriptElementType == null) return -1;

        switch (transcriptElementType) {
            case TranscriptStyleConstants.ATTR_KEY_RECORD -> {
                Tier<?> tier = (Tier<?>) elem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                if (tier == null) return -1;
                int recordStartPos = getTierStart(tier);
                int delta = pos - recordStartPos;
                return delta;
            }
            case TranscriptStyleConstants.ATTR_KEY_COMMENT -> {
                Comment comment = (Comment) elem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_COMMENT);
                if (comment == null) return -1;
                int commentStartPos = getCommentStart(comment);
                int delta = pos - commentStartPos;
                return delta;
            }
            case TranscriptStyleConstants.ATTR_KEY_GEM -> {
                Gem gem = (Gem) elem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_GEM);
                if (gem == null) return -1;
                return pos - getGemStart(gem);
            }
            case TranscriptStyleConstants.ATTR_KEY_GENERIC -> {
                Tier<?> genericTier = (Tier<?>) elem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_GENERIC);
                if (genericTier == null) return -1;
                return pos - getGenericStart(genericTier);
            }
            default -> {
                return -1;
            }
        }
    }
    public AttributeSet updateTiersHeader() {
        return updateTiersHeader(false);
    }
    public SimpleAttributeSet updateTiersHeader(boolean partOfBatch) {
        Tier<TierData> tiersTier = (Tier<TierData>) headerTierMap.get("tiers");

        int start = getGenericStart(tiersTier);
        int end = getGenericEnd(tiersTier);

        try {
            if (start != -1 && end != -1) {
                start -= labelColumnWidth + 2;
                bypassDocumentFilter = true;
                remove(start, end - start);
            }

            List<TierViewItem> visibleTierView = session
                .getTierView()
                .stream()
                .filter(item -> item.isVisible())
                .toList();
            StringJoiner joiner = new StringJoiner(", ");
            for (TierViewItem item : visibleTierView) {
                joiner.add(item.getTierName());
                boolean isIPATier = session
                    .getTiers()
                    .stream()
                    .filter(td -> td.getName().equals(item.getTierName()))
                    .anyMatch(td -> td.getDeclaredType().equals(IPATranscript.class));
                if (syllabificationVisible && isIPATier) {
                    joiner.add(item.getTierName() + " Syllabification");
                }
                if (alignmentVisible && alignmentParent == item) {
                    joiner.add("Alignment");
                }
            }
            tiersTier.setText(joiner.toString());

            appendBatchEndStart();
            var newlineAttrs = writeGeneric("Tiers", tiersTier, getTiersHeaderAttributes());
            if (partOfBatch) {
                return newlineAttrs;
            }
            appendBatchLineFeed(newlineAttrs);
            processBatchUpdates(start);
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
        return null;
    }

    private void populate() throws BadLocationException {
        Transcript transcript = session.getTranscript();
        var tierView = session.getTierView();

        SimpleAttributeSet newLineAttrs;

//        // Add date line if present
//        var sessionDate = session.getDate();
//        if (sessionDate != null) {
//            Tier<LocalDate> dateTier = sessionFactory.createTier("Date", LocalDate.class);
//            dateTier.setValue(sessionDate);
//            newLineAttrs = writeGeneric("Date", dateTier);
//            appendBatchLineFeed(newLineAttrs);
//        }

        // Add media line if present
        var sessionMedia = session.getMediaLocation();
        if (sessionMedia != null) {
            Tier<TierData> mediaTier = (Tier<TierData>) headerTierMap.get("media");
            mediaTier.setText(sessionMedia);
            newLineAttrs = writeGeneric("Media", mediaTier);
            appendBatchLineFeed(newLineAttrs);
        }

        // Add languages line if present
        var sessionLanguages = session.getLanguages();
        if (sessionLanguages != null && !sessionLanguages.isEmpty()) {
            Tier<Languages> languagesTier = (Tier<Languages>) headerTierMap.get("languages");
            languagesTier.setFormatter(new Formatter<>() {
                @Override
                public String format(Languages obj) {
                    return obj
                        .languageList()
                        .stream()
                        .map(Language::toString)
                        .collect(Collectors.joining(" "));
                }

                @Override
                public Languages parse(String text) throws ParseException {
                    List<Language> languageList = new ArrayList<>();

                    String[] languageStrings = text.split(" ");
                    for (String languageString : languageStrings) {
                        LanguageEntry languageEntry = LanguageParser.getInstance().getEntryById(languageString);
                        if (languageEntry == null) throw new ParseException(text, text.indexOf(languageString));

                        languageList.add(Language.parseLanguage(languageString));
                    }

                    return new Languages(languageList);
                }
            });
            languagesTier.setValue(new Languages(sessionLanguages));
            newLineAttrs = writeGeneric("Languages", languagesTier);
            appendBatchLineFeed(newLineAttrs);
        }

        // Add Tiers header
        newLineAttrs = updateTiersHeader(true);
        appendBatchLineFeed(newLineAttrs);

        // Add Participants header
        Tier<TierData> participantsTier = (Tier<TierData>) headerTierMap.get("participants");
        var participants = session.getParticipants();
        StringJoiner participantsJoiner = new StringJoiner(", ");
        for (Participant participant : participants) {
            if (participant.getName() != null) {
                participantsJoiner.add(participant.getName() + " (" + participant.getId() + ")");
            }
            else {
                participantsJoiner.add(participant.getId());
            }
        }
        participantsTier.setText(participantsJoiner.toString());
        newLineAttrs = writeGeneric("Participants", participantsTier, getParticipantsHeaderAttributes());
        appendBatchLineFeed(newLineAttrs);


        // Single record
        if (singleRecordView) {
            Record record = session.getRecord(singleRecordIndex);
            int recordTranscriptElementIndex = transcript.getElementIndex(record);

            int includedElementIndex;
            if (singleRecordIndex == 0) {
                includedElementIndex = 0;
            }
            else {
                includedElementIndex = transcript.getElementIndex(transcript.getRecord(singleRecordIndex-1)) + 1;
            }
            while (includedElementIndex < recordTranscriptElementIndex) {
                Transcript.Element previousElement = transcript.getElementAt(includedElementIndex);
                if (previousElement.isRecord()) {
                    break;
                }

                if (previousElement.isComment()) {
                    newLineAttrs = writeComment(previousElement.asComment());
                }
                else {
                    newLineAttrs = writeGem(previousElement.asGem());
                }

                includedElementIndex++;

                appendBatchLineFeed(newLineAttrs);
            }

            newLineAttrs = writeRecord(record, transcript, tierView);

            int nextElementIndex = recordTranscriptElementIndex + 1;
            int transcriptElementCount = transcript.getNumberOfElements();
            while (nextElementIndex < transcriptElementCount) {
                Transcript.Element nextElement = transcript.getElementAt(nextElementIndex);
                appendBatchLineFeed(newLineAttrs);
                if (nextElement.isRecord()) {
                    break;
                }

                if (nextElement.isComment()) {
                    newLineAttrs = writeComment(nextElement.asComment());
                }
                else {
                    newLineAttrs = writeGem(nextElement.asGem());
                }

                nextElementIndex++;
            }
        }
        // All records
        else {
            for (int i = 0; i < transcript.getNumberOfElements(); i++) {
                Transcript.Element elem = transcript.getElementAt(i);
                if (elem.isRecord()) {
                    newLineAttrs = writeRecord(elem.asRecord(), transcript, tierView);
                }
                else if (elem.isComment()) {
                    newLineAttrs = writeComment(elem.asComment());
                }
                else {
                    newLineAttrs = writeGem(elem.asGem());
                }

                appendBatchLineFeed(newLineAttrs);
            }
        }

        processBatchUpdates(0);
        setGlobalParagraphAttributes();
    }

    private class TranscriptDocumentFilter extends DocumentFilter {
        Set<Character> syllabificationChars;

        public TranscriptDocumentFilter() {
            syllabificationChars = new HashSet<>();
            for (SyllableConstituentType type : SyllableConstituentType.values()) {
                syllabificationChars.add(type.getIdChar());
            }
            syllabificationChars.add('D');
            syllabificationChars.add('H');
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet _attrs) throws BadLocationException {

            // For some reason attrs gets the attributes from the previous character, so this fixes that
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            attrs.addAttributes(getCharacterElement(offset).getAttributes());

            if (attrs != null) {
                // Labels and stuff
                if (attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE) != null) return;

                // Locked tiers
                Tier<?> tier = (Tier<?>)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                if (tier != null) {
                    String tierName = tier.getName();
                    var tierViewItem = session
                        .getTierView()
                        .stream()
                        .filter(item -> item.getTierName().equals(tierName))
                        .findFirst();
                    if (tierViewItem.isPresent() && tierViewItem.get().isTierLocked()) {
                        return;
                    }

                    // Syllabification tiers
                    if (attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_SYLLABIFICATION) != null) {
                        if (text == null || text.length() == 0) return;
                        final String textUpper = text.toUpperCase();
                        char c = textUpper.charAt(0);
                        if (syllabificationChars.contains(c)) {
                            SyllableConstituentType type = Arrays
                                .stream(SyllableConstituentType.values())
                                .filter(item -> item.getIdChar() == textUpper.charAt(0))
                                .findFirst()
                                .orElse(null);
                            if (type == null) {
                                List<IPAElement> syllabificationTranscript = ((Tier<IPATranscript>) tier).getValue().toList();
                                IPAElement phone = (IPAElement) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_PHONE);

                                IPAElement otherNucleus = null;
                                for (int i = 0; i < syllabificationTranscript.size(); i++) {
                                    IPAElement p = syllabificationTranscript.get(i);
                                    if (!p.equals(phone)) continue;
                                    if (!p.getScType().equals(SyllableConstituentType.NUCLEUS)) return;
                                    if (i < syllabificationTranscript.size() - 1) {
                                        IPAElement nextP = syllabificationTranscript.get(i + 1);
                                        if (nextP.getScType().equals(SyllableConstituentType.NUCLEUS)) {
                                            otherNucleus = nextP;
                                            break;
                                        }
                                    }
                                    if (i > 0) {
                                        IPAElement prevP = syllabificationTranscript.get(i - 1);
                                        if (prevP.getScType().equals(SyllableConstituentType.NUCLEUS)) {
                                            otherNucleus = prevP;
                                            break;
                                        }
                                    }
                                }

                                if (otherNucleus == null) return;

                                final SyllabificationInfo sInfo = phone.getExtension(SyllabificationInfo.class);
                                final SyllabificationInfo otherSInfo = otherNucleus.getExtension(SyllabificationInfo.class);

                                if (c == 'D' && !sInfo.isDiphthongMember()) {
                                    sInfo.setDiphthongMember(true);
                                    otherSInfo.setDiphthongMember(true);

                                    int start = getTierStart(tier);
                                    int end = getTierEnd(tier);

                                    for (int i = start; i < end; i++) {
                                        var charAttrs = getCharacterElement(i).getAttributes();
                                        IPAElement charPhone = (IPAElement) charAttrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_PHONE);
                                        if (charAttrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE) == null && charPhone != null && (charPhone.equals(phone) || charPhone.equals(otherNucleus))) {
                                            SimpleAttributeSet newCharAttrs = new SimpleAttributeSet();
                                            newCharAttrs.addAttributes(charAttrs);
                                            StyleConstants.setForeground(newCharAttrs, Color.RED);
                                            super.replace(fb, i, 1, "D", attrs);
                                        }
                                    }
                                }
                                else if (c == 'H' && sInfo.isDiphthongMember()) {
                                    sInfo.setDiphthongMember(false);
                                    otherSInfo.setDiphthongMember(false);

                                    int start = getTierStart(tier);
                                    int end = getTierEnd(tier);

                                    for (int i = start; i < end; i++) {
                                        var charAttrs = getCharacterElement(i).getAttributes();
                                        IPAElement charPhone = (IPAElement) charAttrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_PHONE);
                                        if (charAttrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE) == null && charPhone != null && (charPhone.equals(phone) || charPhone.equals(otherNucleus))) {
                                            SimpleAttributeSet newCharAttrs = new SimpleAttributeSet();
                                            newCharAttrs.addAttributes(charAttrs);
                                            StyleConstants.setForeground(newCharAttrs, SyllableConstituentType.NUCLEUS.getColor());
                                            super.replace(fb, i, 1, "N", attrs);
                                        }
                                    }
                                }
                                return;
                            }
                            else {
                                StyleConstants.setForeground(attrs, type.getColor());
                                text = textUpper;
                            }
                        }
                    }
                }
            }
            super.replace(fb, offset, length, text, attrs);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {

            if (!bypassDocumentFilter) {
                var attrs = getCharacterElement(offset).getAttributes();
                if (attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE) != null) return;
                if (attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_SYLLABIFICATION) != null) return;
            }

            bypassDocumentFilter = false;
            super.remove(fb, offset, length);
        }
    }

    public record Languages(List<Language> languageList) {}
}