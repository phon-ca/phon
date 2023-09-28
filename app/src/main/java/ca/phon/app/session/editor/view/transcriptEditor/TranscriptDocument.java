package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.log.LogUtil;
import ca.phon.formatter.MediaTimeFormatter;
import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.tierdata.*;
import ca.phon.ui.FontFormatter;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.Tuple;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;

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
    private int labelColumnWidth = 20;
    private float lineSpacing = 0.2f;

    public TranscriptDocument() {
        super(new TranscriptStyleContext());
        sessionFactory = SessionFactory.newFactory();
        setDocumentFilter(new TranscriptDocumentFilter());
        batch = new ArrayList<>();
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

    // endregion Getters and Setters


    // region Attribute Getters

    private SimpleAttributeSet getRecordAttributes(int recordIndex) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        int recordElementIndex = session.getRecordElementIndex(recordIndex);
        retVal.addAttribute("recordIndex", recordIndex);
        retVal.addAttribute("recordElementIndex", recordElementIndex);
        retVal.addAttribute("elementType", "record");

        return retVal;
    }

    private SimpleAttributeSet getTierAttributes(Tier<?> tier) {
        return getTierAttributes(tier, null);
    }
    private SimpleAttributeSet getTierAttributes(Tier<?> tier, TierViewItem item) {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute("tier", tier);
        String fontString = "default";
        if (item != null) {
            fontString = item.getTierFont();
        }
        if ("default".equalsIgnoreCase(fontString)) {
            fontString = new FontFormatter().format(FontPreferences.getTierFont());
        }

        var font = Font.decode(fontString);

        StyleConstants.setFontFamily(retVal, font.getFamily());
        StyleConstants.setFontSize(retVal, font.getSize());
        StyleConstants.setBold(retVal, font.isBold());
        StyleConstants.setItalic(retVal, font.isItalic());

        return retVal;
    }

    private SimpleAttributeSet getSyllabificationAttributes() {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute("componentFactory", new SyllabificationComponentFactory());
        return retVal;
    }

    private SimpleAttributeSet getAlignmentAttributes() {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttribute("componentFactory", new AlignmentComponentFactory());

        return retVal;
    }

    private SimpleAttributeSet getIPAWordAttributes(Tier<IPATranscript> tier) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute("tier", tier);

        StyleConstants.setForeground(retVal, UIManager.getColor(TranscriptEditorUIProps.IPA_WORD));

        return retVal;
    }

    private SimpleAttributeSet getIPAPauseAttributes(Tier<IPATranscript> tier) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute("tier", tier);

        StyleConstants.setForeground(retVal, UIManager.getColor(TranscriptEditorUIProps.IPA_PAUSE));

        return retVal;
    }

    private SimpleAttributeSet getSegmentTimeAttributes(MediaSegment segment) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute("mediaSegment", segment);

        return retVal;
    }

    private SimpleAttributeSet getSegmentDashAttributes(MediaSegment segment) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute("mediaSegment", segment);

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

        StyleConstants.setForeground(retVal, UIManager.getColor(TranscriptEditorUIProps.DEFAULT_INTERNAL_MEDIA));

        return retVal;
    }

    private SimpleAttributeSet getTierLabelAttributes(Tier<?> tier) {
        return getTierLabelAttributes(tier, null);
    }
    private SimpleAttributeSet getTierLabelAttributes(Tier<?> tier, TierViewItem tierViewItem) {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();

        if (tierViewItem == null || tierViewItem.isTierLocked()) {
            retVal.addAttribute("locked", true);
        }
        retVal.addAttribute("label", true);
        retVal.addAttribute("notEditable", true);
        retVal.addAttribute("tier", tier);

        retVal.addAttributes(getMonospaceFontAttributes());

        return retVal;
    }

    private SimpleAttributeSet getSeparatorAttributes() {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttribute("notEditable", true);
        retVal.addAttribute("label", true);
        retVal.addAttribute("sep", true);

        retVal.addAttributes(getMonospaceFontAttributes());

        return retVal;
    }

    private SimpleAttributeSet getCommentAttributes(Comment comment) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttribute("elementType", "comment");
        retVal.addAttribute("comment", comment);

        return retVal;
    }

    private SimpleAttributeSet getCommentLabelAttributes(Comment comment) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttributes(getCommentAttributes(comment));
        retVal.addAttributes(getMonospaceFontAttributes());

        retVal.addAttribute("label", true);
        retVal.addAttribute("notEditable", true);

        return retVal;
    }

    private SimpleAttributeSet getGemAttributes(Gem gem) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttribute("elementType", "gem");
        retVal.addAttribute("gem", gem);

        return retVal;
    }

    private SimpleAttributeSet getGemLabelAttributes(Gem gem) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttributes(getGemAttributes(gem));
        retVal.addAttributes(getMonospaceFontAttributes());

        retVal.addAttribute("label", true);
        retVal.addAttribute("notEditable", true);

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

        ElementSpec last = batch.get(batch.size() - 1);

        // Empty batch the list
        batch.clear();

        System.out.println(getCharAtPos(getLength() - 1));
        System.out.println(getLength());
    }

    // endregion Batching


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

            tierAttrs = insertTier(recordIndex, item, recordAttrs);

            if (i < visibleTierView.size() - 1) {
                appendBatchLineFeed(tierAttrs);
            }
        }

        return (tierAttrs != null) ? tierAttrs : sepAttrs;
    }

    private SimpleAttributeSet writeComment(Comment comment) {

        SimpleAttributeSet commentAttrs = getCommentAttributes(comment);
        commentAttrs.addAttributes(getStandardFontAttributes());

        TierData tierData = comment.getValue();

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

        labelAttrs.addAttribute("clickable", true);
        appendBatchString(labelText, labelAttrs);

        labelAttrs.removeAttribute("clickable");
        appendBatchString(": ", labelAttrs);

        for (int i = 0; i < tierData.length(); i++) {
            TierElement userTierElement = tierData.elementAt(i);
            String text;
            SimpleAttributeSet attrs;
            if (userTierElement instanceof TierString tierString) {
                // Text
                text = tierString.text();
                attrs = getTierStringAttributes();
            } else if (userTierElement instanceof TierComment userTierComment) {
                // Comment
                text = userTierComment.toString();
                attrs = getTierCommentAttributes();
            } else {
                // Internal media
                TierInternalMedia internalMedia = (TierInternalMedia) userTierElement;
                text = internalMedia.toString();
                attrs = getTierInternalMediaAttributes();
            }

            attrs.addAttributes(commentAttrs);

            appendBatchString(text, attrs);

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
        String labelText = gem.getType().toString();
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

        labelAttrs.addAttribute("clickable", true);
        appendBatchString(labelText, labelAttrs);

        labelAttrs.removeAttribute("clickable");
        appendBatchString(": ", labelAttrs);

        appendBatchString(text, gemAttrs);

        return gemAttrs;
    }

    // endregion Write Transcript Element


    // region Get Record/Tier Start/End

    public int getRecordStart(int recordIndex) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            AttributeSet attrs = elem.getElement(0).getAttributes();
            var currentRecordIndex = attrs.getAttribute("recordIndex");
            var tier = attrs.getAttribute("tier");
            if (tier != null && currentRecordIndex != null && recordIndex == (int)currentRecordIndex) {
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
            Integer currentRecordIndex = (Integer) elem.getElement(0).getAttributes().getAttribute("recordIndex");
            // If correct record index
            if (currentRecordIndex != null) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> currentTier = (Tier<?>)attrs.getAttribute("tier");
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
            var currentRecordIndex = attrs.getAttribute("recordIndex");
            if (currentRecordIndex != null && recordIndex == (int)currentRecordIndex) {
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
            var currentRecordIndex = elem.getElement(0).getAttributes().getAttribute("recordIndex");
            // If correct record index
            if (currentRecordIndex != null) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    var currentTier = innerElem.getAttributes().getAttribute("tier");
                    // If correct tier
                    if (currentTier != null && currentTier == tier) {
                        return getRecordEnd((int)currentRecordIndex);
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
            Integer currentRecordIndex = (Integer) elem.getElement(0).getAttributes().getAttribute("recordIndex");
            // If correct record index
            if (currentRecordIndex != null && currentRecordIndex == recordIndex) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> tier = (Tier<?>) attrs.getAttribute("tier");
                    Boolean isLabel = (Boolean) attrs.getAttribute("label");
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
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute("elementType");
            // If correct record index
            if (transcriptElementType != null && transcriptElementType.equals("record")) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> currentTier = (Tier<?>)attrs.getAttribute("tier");
                    Boolean isLabel = (Boolean)attrs.getAttribute("label");
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
            var currentRecordIndex = elem.getElement(0).getAttributes().getAttribute("recordIndex");
            // If correct record index
            if (currentRecordIndex != null && ((int)currentRecordIndex) == recordIndex) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> tier = (Tier<?>)attrs.getAttribute("tier");
                    Boolean isLabel = (Boolean)attrs.getAttribute("label");
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
            Integer currentRecordIndex = (Integer) elem.getElement(0).getAttributes().getAttribute("recordIndex");
            // If correct record index
            if (currentRecordIndex != null) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    Tier<?> currentTier = (Tier<?>)innerElem.getAttributes().getAttribute("tier");
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
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute("elementType");
            // If transcript element type is comment
            if (transcriptElementType != null && transcriptElementType.equals("comment")) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Comment currentComment = (Comment)attrs.getAttribute("comment");
                    Boolean isLabel = (Boolean)attrs.getAttribute("label");
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
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute("elementType");
            // If transcript element type is comment
            if (transcriptElementType != null && transcriptElementType.equals("comment")) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Comment currentComment = (Comment)attrs.getAttribute("comment");
                    Boolean isLabel = (Boolean)attrs.getAttribute("label");
                    // If correct tier name
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
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute("elementType");
            // If transcript element type is gem
            if (transcriptElementType != null && transcriptElementType.equals("gem")) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Gem currentGem = (Gem)attrs.getAttribute("gem");
                    Boolean isLabel = (Boolean)attrs.getAttribute("label");
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
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute("elementType");
            // If transcript element type is gem
            if (transcriptElementType != null && transcriptElementType.equals("gem")) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Gem currentGem = (Gem) attrs.getAttribute("gem");
                    Boolean isLabel = (Boolean)attrs.getAttribute("label");
                    // If correct tier name
                    if (isLabel == null && currentGem != null && currentGem == gem) {
                        retVal = Math.max(retVal, innerElem.getEndOffset());
                    }
                }
            }
        }

        return retVal;
    }

    public Tuple<Integer, Integer> getSegmentBounds(MediaSegment segment, Element includedElem) {
        Element root = getDefaultRootElement();

        int indexInSegment = -1;

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute("elementType");
            // If correct record index
            if (transcriptElementType != null && transcriptElementType.equals("record")) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    MediaSegment elemSegment = (MediaSegment) innerElem.getAttributes().getAttribute("mediaSegment");
                    if (elemSegment != null && elemSegment == segment && innerElem == includedElem) {
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
        while (attrs.getAttribute("mediaSegment") != null) {
            segmentStart--;
            attrs = getCharacterElement(segmentStart).getAttributes();
        }
        retVal.setObj1(segmentStart + 1);

        int segmentEnd = indexInSegment;
        attrs = getCharacterElement(segmentEnd).getAttributes();
        while (attrs.getAttribute("mediaSegment") != null) {
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

        List<TierViewItem> movedTiersNewOrder = new ArrayList<>();
        for (TierViewItem item : session.getTierView()) {
            if (movedTiers.contains(item)) {
                movedTiersNewOrder.add(item);
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

                if (record.getTier(tierName).getValue().toString().strip().equals("")) {
                    tierStartOffset += 1;
                }

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
            int offsetBeforeInsert = offset;
            for (TierViewItem item : movedTiersNewOrder) {
                insertTier(recordIndex, item, recordAttrs);
            }
            processBatchUpdates(offsetBeforeInsert);
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
    }

    private void deleteTierFromRecord(int recordIndex, List<String> deletedTiers) {
        int labelLength = labelColumnWidth + 2;
        try {
            for (String tierName : deletedTiers) {
                int tierStartOffset = getTierStart(recordIndex, tierName) - labelLength;
                int tierEndOffset = getTierEnd(recordIndex, tierName);

                if (tierStartOffset < 0 || tierEndOffset < 0) continue;

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
    }

    private void addTierToRecord(int recordIndex, List<TierViewItem> addedTiers) {
        int offset = getRecordEnd(recordIndex);
        try {
            appendBatchEndStart();
            for (TierViewItem item : addedTiers) {
                insertTier(recordIndex, item, getRecordAttributes(recordIndex));
            }
            processBatchUpdates(offset);
            setGlobalParagraphAttributes();
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    public void hideTier(List<String> hiddenTiers) {
        if (singleRecordView) {
            hideTierInRecord(singleRecordIndex, hiddenTiers);
        }
        else {
            int recordCount = session.getRecordCount();
            for (int i = 0; i < recordCount; i++) {
                hideTierInRecord(i, hiddenTiers);
            }
        }

        for (String tierName : hiddenTiers) {
            if ((tierName.equals("IPA Target") || tierName.equals("IPA Actual")) && syllabificationVisible) {
                try {
                    Element root = getDefaultRootElement();
                    for (int i = 0; i < root.getElementCount(); i++) {
                        Element elem = root.getElement(i);
                        for (int j = 0; j < elem.getElementCount(); j++) {
                            Element innerElem = elem.getElement(j);
                            var attrs = innerElem.getAttributes();
                            Tier<?> tier = (Tier<?>) attrs.getAttribute("tier");
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
                                    remove(tierStartOffset, tierEndOffset - tierStartOffset);
                                }
                            }

                        }
                    }
                }
                catch (BadLocationException e) {
                    LogUtil.severe(e);
                }
            }
        }
    }

    private void hideTierInRecord(int recordIndex, List<String> hiddenTiers) {
        try {
            int labelLength = labelColumnWidth + 2;
            Record record = session.getRecord(recordIndex);
            for (String tierName : hiddenTiers) {
                if (!record.hasTier(tierName)) continue;

                int tierStartOffset = getTierStart(recordIndex, tierName) - labelLength;
                int tierEndOffset = getTierEnd(recordIndex, tierName);
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
                    offset = getTierEnd(recordIndex, itemBeforeShownItem.getTierName());
                }

                int offsetBeforeInsert = offset;
                appendBatchEndStart();
                insertTier(recordIndex, item, recordAttrs);
                processBatchUpdates(offsetBeforeInsert);
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
                tierFontChangedInRecord(singleRecordIndex, changedTiers);
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

                remove(tierStartOffset, tierEndOffset - tierStartOffset);

                appendBatchEndStart();
                insertTier(recordIndex, item, recordAttrs);
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

                remove(tierStartOffset, tierEndOffset - tierStartOffset);

                appendBatchEndStart();
                insertTier(recordIndex, newTier, recordAttrs);
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

    public void deleteRecord(int removedRecordIndex, int removedRecordElementIndex) {
        try {
            Transcript transcript = session.getTranscript();
            var tierView = session.getTierView();
            AttributeSet newLineAttrs;

            int start = getRecordStart(removedRecordIndex);

            remove(start, getLength() - start);

            appendBatchEndStart();

            for (int i = removedRecordElementIndex; i < transcript.getNumberOfElements(); i++) {
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

    public void moveRecord(int oldRecordIndex, int newRecordIndex, int oldElementIndex, int newElementIndex) {
        try {
            Transcript transcript = session.getTranscript();
            var tierView = session.getTierView();
            AttributeSet newLineAttrs;

            int start = getRecordStart(Math.min(oldRecordIndex, newRecordIndex));
            int end = getRecordEnd(Math.max(oldRecordIndex, newRecordIndex));

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
            int start = getRecordStart(recordIndex);

            var tierView = session.getTierView();
            String firstVisibleTierName = tierView
                .stream()
                .filter(item -> item.isVisible())
                .findFirst()
                .get()
                .getTierName();
            int end = getTierStart(recordIndex, firstVisibleTierName) - (labelColumnWidth + 2);

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
        try {
            int start = getTierStart(tier);
            int recordIndex = getRecordIndex(start);
            start -= (labelColumnWidth + 2);
            int end = getTierEnd(tier);

            remove(start, end - start);

            var tierView = session.getTierView();
            TierViewItem tierViewItem =  tierView.stream().filter(item -> item.getTierName().equals(tier.getName())).findFirst().get();

            appendBatchEndStart();

            SimpleAttributeSet attrs = insertTier(recordIndex, tierViewItem, getRecordAttributes(recordIndex));

            appendBatchLineFeed(attrs);

            processBatchUpdates(start);
            setGlobalParagraphAttributes();
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    // endregion Record Changes


    public TierViewItem getAlignmentTierView() {
        List<TierViewItem> visibleTierView = session.getTierView().stream().filter(item -> item.isVisible()).toList();

        var retVal = visibleTierView.stream().filter(item -> item.getTierName().equals("IPA Actual")).findFirst();
        if (retVal.isPresent()) return retVal.get();

        retVal = visibleTierView.stream().filter(item -> item.getTierName().equals("IPA Target")).findFirst();
        if (retVal.isPresent()) return retVal.get();

        return visibleTierView.get(visibleTierView.size()-1);
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
            remove(0, getLength());
            populate();
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    public int getRecordIndex(int offset) {
        AttributeSet attributes = getCharacterElement(offset).getAttributes();
        var index = attributes.getAttribute("recordIndex");
        if (index == null) {
            return -1;
        }
        return (int)index;
    }

    public int getRecordElementIndex(int offset) {
        AttributeSet attributes = getCharacterElement(offset).getAttributes();
        var index = attributes.getAttribute("recordElementIndex");
        if (index == null) {
            return -1;
        }
        return (int)index;
    }

    public Tier<?> getTier(int offset) {
        AttributeSet attributes = getCharacterElement(offset).getAttributes();
        Tier<?> tier = (Tier<?>) attributes.getAttribute("tier");
        if (tier == null) {
            return null;
        }
        return tier;
    }

    private void formatSegment(MediaSegment segment, AttributeSet additionalAttrs) {

        String start = MediaTimeFormatter.msToPaddedMinutesAndSeconds(segment.getStartValue());

        var segmentTimeAttrs = getSegmentTimeAttributes(segment);
        var segmentDashAttrs = getSegmentDashAttributes(segment);
        segmentTimeAttrs.addAttribute("notEditable", true);
        segmentDashAttrs.addAttribute("notEditable", true);
        if (additionalAttrs != null) {
            segmentTimeAttrs.addAttributes(additionalAttrs);
            segmentDashAttrs.addAttributes(additionalAttrs);
        }

        appendBatchString("•", segmentDashAttrs);

        appendBatchString(start, segmentTimeAttrs);

        appendBatchString("-", segmentDashAttrs);

        String end = MediaTimeFormatter.msToPaddedMinutesAndSeconds(segment.getEndValue());

        appendBatchString(end, segmentTimeAttrs);

        appendBatchString("•", segmentDashAttrs);
    }

    private void setGlobalParagraphAttributes() {
        SimpleAttributeSet paragraphAttrs = new SimpleAttributeSet();
        StyleConstants.setLineSpacing(paragraphAttrs, getLineSpacing());
        StyleConstants.setForeground(paragraphAttrs, UIManager.getColor(TranscriptEditorUIProps.FOREGROUND));
        setParagraphAttributes(0, getLength(), paragraphAttrs, false);
    }

    private SimpleAttributeSet insertTier(int recordIndex, TierViewItem tierViewItem, AttributeSet recordAttrs) {
        String tierName = tierViewItem.getTierName();
        Record record = session.getRecord(recordIndex);
        Tier<?> tier = record.getTier(tierName);
        if (tier == null) {
            Optional<TierDescription> td = session
                .getUserTiers()
                .stream()
                .filter(item -> item.getName().equals(tierName))
                .findFirst();
            tier = sessionFactory.createTier(td.get());
        }

        SimpleAttributeSet tierAttrs = getTierAttributes(tier, tierViewItem);
        if (recordAttrs != null) {
            tierAttrs.addAttributes(recordAttrs);
        }

        SimpleAttributeSet labelAttrs = getTierLabelAttributes(tier, tierViewItem);
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

        labelAttrs.addAttribute("clickable", true);
        appendBatchString(labelText, labelAttrs);

        labelAttrs.removeAttribute("clickable");
        appendBatchString(": ", labelAttrs);

        if (tierName.equals("IPA Target")) {
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
            if (syllabificationVisible) {
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
                // Get the string version of the syllabification
                String ipaTargetContent = ipaTarget.toString(true);
                // Add component factory if needed
                if (syllabificationIsComponent) {
                    tierAttrs.addAttributes(getSyllabificationAttributes());
                }
                // Append the content
                appendBatchString(ipaTargetContent, tierAttrs);
            }
        }
        else if (tierName.equals("IPA Actual")) {
            Tier<IPATranscript> ipaTier = (Tier<IPATranscript>)tier;
            List<IPATranscript> words = (ipaTier).getValue().words();
            for (int i = 0; i < words.size(); i++) {
                var word = words.get(i);
                SimpleAttributeSet attrs;
                if (word.matches("\\P")) {
                    // Pause
                    attrs = getIPAPauseAttributes(ipaTier);
                }
                else {
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
            if (syllabificationVisible) {
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
                // Get the string version of the syllabification
                String ipaActualContent = ipaActual.toString(true);
                // Add component factory if needed
                if (syllabificationIsComponent) {
                    tierAttrs.addAttributes(getSyllabificationAttributes());
                }
                // Append the content
                appendBatchString(ipaActualContent, tierAttrs);
            }
        }
        else if (tierName.equals("Segment")) {
            MediaSegment segment = record.getMediaSegment();
            formatSegment(segment, tierAttrs);
        }
        else if (tierName.equals("Orthography")) {
            Tier<Orthography> orthographyTier = (Tier<Orthography>) tier;
            orthographyTier.getValue().accept(new TranscriptOrthographyVisitors.KeywordVisitor(this, tierAttrs));
            //appendBatchString(tierContent, tierAttrs);
        }
        else {
            Tier<TierData> userTier = (Tier<TierData>) tier;
            TierData tierData = userTier.getValue();
            if (tierData != null) {
                for (int i = 0; i < tierData.length(); i++) {
                    TierElement elem = tierData.elementAt(i);
                    String text;
                    SimpleAttributeSet attrs;
                    if (elem instanceof TierString tierString) {
                        text = tierString.text();
                        attrs = getTierStringAttributes();
                    }
                    else if (elem instanceof TierComment comment) {
                        text = comment.toString();
                        attrs = getTierCommentAttributes();
                    }
                    else {
                        TierInternalMedia internalMedia = (TierInternalMedia) elem;
                        text = internalMedia.toString();
                        attrs = getTierInternalMediaAttributes();
                    }

                    attrs.addAttributes(tierAttrs);

                    appendBatchString(text, attrs);

                    if (i < tierData.length() - 1) {
                        appendBatchString(" ", tierAttrs);
                    }
                }
            }
        }
        String alignmentParentTierName = getAlignmentTierView().getTierName();
        if (alignmentVisible && tierName.equals(alignmentParentTierName)) {
            System.out.println(alignmentParentTierName);
            tierAttrs.removeAttribute("componentFactory");
            // Add a newline at the end of the regular tier content
            appendBatchLineFeed(tierAttrs);
            // Get the alignment tier
            Tier<PhoneAlignment> alignmentTier = record.getPhoneAlignmentTier();
            // Set up the tier attributes for the dummy tier
            tierAttrs = new SimpleAttributeSet(tierAttrs);
            tierAttrs.addAttributes(getTierAttributes(alignmentTier));
            // Set up the attributes for its label
            SimpleAttributeSet alignmentLabelAttrs = getTierLabelAttributes(alignmentTier);
            if (recordAttrs != null) {
                alignmentLabelAttrs.addAttributes(recordAttrs);
            }
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
        }

        tierAttrs.removeAttribute("componentFactory");
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
        String transcriptElementType = (String) elem.getAttributes().getAttribute("elementType");
        if (transcriptElementType == null) return -1;

        switch (transcriptElementType) {
            case "record" -> {
                Tier<?> tier = (Tier<?>) elem.getAttributes().getAttribute("tier");
                if (tier == null) return -1;
                int recordStartPos = getTierStart(tier);
                int delta = pos - recordStartPos;
                return delta;
            }
            case "comment" -> {
                Comment comment = (Comment) elem.getAttributes().getAttribute("comment");
                if (comment == null) return -1;
                int commentStartPos = getCommentStart(comment);
                int delta = pos - commentStartPos;
                return delta;
            }
            case "gem" -> {
                Gem gem = (Gem) elem.getAttributes().getAttribute("gem");
                if (gem == null) return -1;
                return pos - getGemStart(gem);
            }
            default -> {
                return -1;
            }
        }
    }

    private void populate() throws BadLocationException {
        Transcript transcript = session.getTranscript();
        var tierView = session.getTierView();

        SimpleAttributeSet newLineAttrs;

        if (singleRecordView) {
            Record record = session.getRecord(singleRecordIndex);
            int recordTranscriptElementIndex = transcript.getElementIndex(record);

            int previousElementIndex = recordTranscriptElementIndex - 1;
            while (previousElementIndex >= 0) {
                Transcript.Element previousElement = transcript.getElementAt(previousElementIndex);
                if (previousElement.isRecord()) {
                    break;
                }

                if (previousElement.isComment()) {
                    newLineAttrs = writeComment(previousElement.asComment());
                }
                else {
                    newLineAttrs = writeGem(previousElement.asGem());
                }

                previousElementIndex--;

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
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {

            // For some reason attrs gets the attributes from the previous character, so this fixes that
            attrs = getCharacterElement(offset).getAttributes();

            if (attrs != null) {
                // Labels and stuff
                if (attrs.getAttribute("notEditable") != null) return;

                // Locked tiers
                Tier<?> tier = (Tier<?>)attrs.getAttribute("tier");
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
                }
            }
            super.replace(fb, offset, length, text, attrs);
        }
    }
}