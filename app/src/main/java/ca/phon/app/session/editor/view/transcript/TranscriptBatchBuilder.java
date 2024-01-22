package ca.phon.app.session.editor.view.transcript;

import ca.phon.formatter.MediaTimeFormatStyle;
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

import javax.swing.*;
import javax.swing.text.*;
import java.util.ArrayList;
import java.util.List;

public class TranscriptBatchBuilder {

    /**
     * A character array containing just the newline character
     */
    private static final char[] EOL_ARRAY = {'\n'};
    private final List<DefaultStyledDocument.ElementSpec> batch = new ArrayList<>();
    private final List<InsertionHook> insertionHooks;

    private final TranscriptStyleContext styleContext;

    public TranscriptBatchBuilder() {
        this(new TranscriptStyleContext(), new ArrayList<>());
    }

    public TranscriptBatchBuilder(TranscriptDocument document) {
        this(document.getTranscriptStyleContext(), document.getInsertionHooks());
    }

    public TranscriptBatchBuilder(TranscriptStyleContext transcriptStyleContext) {
        this(transcriptStyleContext, new ArrayList<>());
    }

    public TranscriptBatchBuilder(List<InsertionHook> insertionHooks) {
        this(new TranscriptStyleContext(), insertionHooks);
    }

    public TranscriptBatchBuilder(TranscriptStyleContext transcriptStyleContext, List<InsertionHook> insertionHooks) {
        this.styleContext = transcriptStyleContext;
        this.insertionHooks = insertionHooks;
    }

    public List<DefaultStyledDocument.ElementSpec> getBatch() {
        return batch;
    }

    public List<InsertionHook> getInsertionHooks() {
        return insertionHooks;
    }

    public void append(DefaultStyledDocument.ElementSpec elementSpec) {
        batch.add(elementSpec);
    }

    public void appendAll(List<DefaultStyledDocument.ElementSpec> elementSpecList) {
        batch.addAll(elementSpecList);
    }

    /**
     * Clears the batch and returns the number of elements removed
     *
     * @return the number of elements removed
     */
    public int clear() {
        int size = batch.size();
        batch.clear();
        return size;
    }

    /**
     * Gets a list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} containing
     * the {@code ElementSpec.EndTagType} and {@code ElementSpec.StartTagType} tags
     *
     * @return a list containing the end and start tags
     */
    static public List<DefaultStyledDocument.ElementSpec> getBatchEndStart() {
        return getBatchEndStart(null, null);
    }

    /**
     * Gets a list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} containing
     * the {@code ElementSpec.EndTagType} and {@code ElementSpec.StartTagType} tags
     *
     * @param endAttrs attributes for end tag (may be null)
     * @param startAttrs attributes for start tag (may be null)
     * @return a list containing the end and start tags
     */
    static public List<DefaultStyledDocument.ElementSpec> getBatchEndStart(AttributeSet endAttrs, AttributeSet startAttrs) {
        List<DefaultStyledDocument.ElementSpec> retVal = new ArrayList<>();
        retVal.add(new DefaultStyledDocument.ElementSpec(endAttrs != null ? endAttrs.copyAttributes() : null, DefaultStyledDocument.ElementSpec.EndTagType));
        retVal.add(new DefaultStyledDocument.ElementSpec(startAttrs != null ? startAttrs.copyAttributes() : null, DefaultStyledDocument.ElementSpec.StartTagType));
        return retVal;
    }

    /**
     * Appends a {@link javax.swing.text.DefaultStyledDocument.ElementSpec} containing the specified string and
     * attributes to the end of the batch
     */
    public void appendBatchString(String str, MutableAttributeSet a) {
        final StringBuilder builder = new StringBuilder();
        builder.append(str);
        final List<DefaultStyledDocument.ElementSpec> additionalInsertions = new ArrayList<>();
        for (InsertionHook hook : insertionHooks) {
            additionalInsertions.addAll(hook.batchInsertString(builder, a));
        }
        batch.add(getBatchString(builder.toString(), a));
        batch.addAll(additionalInsertions);
    }

    /**
     * Appends the end and start tags to the end of the batch
     */
    public void appendBatchEndStart() {
        appendBatchEndStart(null, null);
    }

    /**
     * Appends the end and start tags to the end of the batch
     *
     * @param endAttrs attributes for end tag (may be null)
     * @param startAttrs attributes for starting paragraph (may be null)
     * @poaram batch the batch to append the tags to
     */
    public void appendBatchEndStart(AttributeSet endAttrs, AttributeSet startAttrs) {
        batch.addAll(getBatchEndStart(endAttrs, startAttrs));
    }

    /**
     * Appends a newline character with the given attributes and the start and end tags to the end of the batch
     */
    public void appendBatchLineFeed(AttributeSet endAttrs, AttributeSet startAttrs) {
        batch.addAll(getBatchEndLineFeed(endAttrs, startAttrs));
    }

    /**
     * Gets a list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} containing a newline character
     * with the specified attributes and the {@code ElementSpec.EndTagType} and {@code ElementSpec.StartTagType} tags
     *
     * @param endAttrs attributes for end tag (may be null)
     * @param startAttrs attributes for start tag (may be null)
     * @return a list with the newline character and the end and start tags
     */
    static public List<DefaultStyledDocument.ElementSpec> getBatchEndLineFeed(AttributeSet endAttrs, AttributeSet startAttrs) {
        List<DefaultStyledDocument.ElementSpec> retVal = new ArrayList<>();
        retVal.add(new DefaultStyledDocument.ElementSpec(endAttrs != null ? endAttrs.copyAttributes() : null, DefaultStyledDocument.ElementSpec.ContentType, EOL_ARRAY, 0, 1));
        retVal.addAll(getBatchEndStart(endAttrs, startAttrs));
        return retVal;
    }

    /**
     * Appends an end of line
     * @return this builder
     */
    public TranscriptBatchBuilder appendEOL() {
        batch.add(new DefaultStyledDocument.ElementSpec(getTrailingAttributes(), DefaultStyledDocument.ElementSpec.ContentType, EOL_ARRAY, 0, 1));
        return this;
    }

    /**
     * Gets a {@link javax.swing.text.DefaultStyledDocument.ElementSpec} containing the specified string and attributes
     *
     * @return a {@link javax.swing.text.DefaultStyledDocument.ElementSpec} containing the specified
     * string and attributes
     */
    static public DefaultStyledDocument.ElementSpec getBatchString(String str, MutableAttributeSet a) {
        char[] chars = str.toCharArray();
        return new DefaultStyledDocument.ElementSpec(new SimpleAttributeSet(a), DefaultStyledDocument.ElementSpec.ContentType, chars, 0, str.length());
    }

    /**
     * Formats the provided text to be used as a label by appending the appropriate number of
     * spaces at the beginning or ellipses at the end
     *
     * @param labelText the text to be formatted
     * @return the formatted text
     */
    public String formatLabelText(String labelText) {
//        int labelTextLen = labelText.length();
//        if (labelTextLen < labelColumnWidth) {
//            return " ".repeat((labelColumnWidth - labelTextLen)) + labelText;
//        } else if (labelTextLen > labelColumnWidth) {
//            String remaining = labelText.substring(labelTextLen - labelColumnWidth + 3, labelTextLen);
//            return "..." + remaining;
//        }
        return "\t" + labelText;
    }

    /**
     * Gets a list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} for a specified "generic tier"
     *
     * @param label                the text for the label
     * @param tier                 the generic tier that the list will contain the data for
     * @param additionalAttributes any additional attributes to be added to the contents of the returned list
     *                             (none added if {@code null})
     * @return a list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} representing the specified
     * generic tier
     */
    public TranscriptBatchBuilder appendGeneric(String label, Tier<?> tier, AttributeSet additionalAttributes) {
        SimpleAttributeSet attrs = new SimpleAttributeSet(styleContext.getStyle(TranscriptStyleContext.DEFAULT_STYLE));
        attrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE, TranscriptStyleConstants.ATTR_KEY_GENERIC);
        attrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_GENERIC, tier);
        if (additionalAttributes != null) attrs.addAttributes(additionalAttributes);

        appendBatchEndStart(getTrailingAttributes(), attrs);

        SimpleAttributeSet labelAttrs = new SimpleAttributeSet(attrs);
        labelAttrs.addAttributes(styleContext.getLabelAttributes());
        String labelText = formatLabelText(label);

        labelAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE, true);
        appendBatchString(labelText, labelAttrs);

        labelAttrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE);
        appendBatchString(": ", labelAttrs);

        if(tier.hasValue()) {
            appendBatchString(tier.toString(), attrs);
        } else if (tier.isUnvalidated()) {
            appendBatchString(tier.getUnvalidatedValue().toString(), attrs);
        } else {
            appendBatchString("", attrs);
        }

        return this;
    }

    /**
     * Appends the specified internal media to the batch
     *
     * @param internalMedia   the internal media that gets appended
     * @param additionalAttrs any additional attributes to be added to the segment (none added if {@code null})
     */
    public void appendFormattedInternalMedia(InternalMedia internalMedia, AttributeSet additionalAttrs) {
        MediaSegment segment = SessionFactory.newFactory().createMediaSegment();
        segment.setSegment(internalMedia.getStartTime(), internalMedia.getEndTime(), MediaUnit.Second);
        appendFormattedSegment(segment, additionalAttrs, MediaTimeFormatStyle.MINUTES_AND_SECONDS);
    }

    /**
     * Appends a formatted representation of the provided segment in the provided style to the batch
     *
     * @param segment the segment that will be appended
     * @param additionalAttrs any additional attributes to be added to the segment (none added if {@code null})
     * @param style the style to format the times of the segment
     */
    public void appendFormattedSegment(MediaSegment segment, AttributeSet additionalAttrs, MediaTimeFormatStyle style) {
        var formatter = new MediaSegmentFormatter(style);
        String value = formatter.format(segment);

        var segmentTimeAttrs = styleContext.getSegmentTimeAttributes(segment);
        TranscriptStyleConstants.setNotEditable(segmentTimeAttrs, true);
        var segmentDashAttrs = styleContext.getSegmentDashAttributes(segment);
        TranscriptStyleConstants.setNotEditable(segmentDashAttrs, true);
        if (additionalAttrs != null) {
            segmentTimeAttrs.addAttributes(additionalAttrs);
            segmentDashAttrs.addAttributes(additionalAttrs);
        }

        SimpleAttributeSet firstDashAttrs = new SimpleAttributeSet(segmentDashAttrs);
        firstDashAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_FIRST_SEGMENT_DASH, true);

        appendBatchString("•", firstDashAttrs);
        appendBatchString(value, segmentTimeAttrs);
        appendBatchString("•", segmentDashAttrs);
    }

    /**
     * Appends a formatted representation of the provided segment to the batch
     *
     * @param segment         the segment that will be appended
     * @param additionalAttrs any additional attributes to be added to the segment (none added if {@code null})
     */
    public void appendFormattedSegment(MediaSegment segment, AttributeSet additionalAttrs) {
        appendFormattedSegment(segment, additionalAttrs, MediaTimeFormatStyle.PADDED_MINUTES_AND_SECONDS);
    }

    /**
     * Writes a given comment to the batch
     *
     * @param comment the comment that will be written
     * @return a mutable attribute set containing the attributes of the last character of the comment to add a
     * newline after if need be
     */
    public TranscriptBatchBuilder appendComment(Comment comment) {
        final SessionFactory sessionFactory = SessionFactory.newFactory();
        Tier<TierData> commentTier = sessionFactory.createTier("commentTier", TierData.class);
        commentTier.setValue(comment.getValue() == null ? new TierData() : comment.getValue());
        SimpleAttributeSet commentAttrs = styleContext.getCommentAttributes(comment);
        TranscriptStyleConstants.setCommentTier(commentAttrs, commentTier);
        appendBatchEndStart(getTrailingAttributes(), commentAttrs);

        List<DefaultStyledDocument.ElementSpec> additionalInsertions = new ArrayList<>();
        for (var hook : getInsertionHooks()) {
            additionalInsertions.addAll(hook.startComment());
        }
        if (!additionalInsertions.isEmpty()) {
            appendAll(additionalInsertions);
            additionalInsertions.clear();
        }

        SimpleAttributeSet labelAttrs = styleContext.getCommentLabelAttributes(comment);
        String labelText = comment.getType().getLabel();
        labelText = formatLabelText(labelText);

        TierData tierData = commentTier.getValue();

        TranscriptStyleConstants.setClickable(labelAttrs, true);
        appendBatchString(labelText, labelAttrs);

        TranscriptStyleConstants.setClickable(labelAttrs, false);
        appendBatchString(": ", labelAttrs);

        for (int i = 0; i < tierData.length(); i++) {
            TierElement userTierElement = tierData.elementAt(i);
            String text = null;
            SimpleAttributeSet attrs;
            if (userTierElement instanceof TierString tierString) {
                // Text
                text = tierString.text();
                attrs = styleContext.getTierStringAttributes();
            } else if (userTierElement instanceof TierComment userTierComment) {
                // Comment
                text = userTierComment.toString();
                attrs = styleContext.getTierCommentAttributes();
            } else if (userTierElement instanceof TierInternalMedia internalMedia) {
                // Internal media
                attrs = styleContext.getTierInternalMediaAttributes();
                appendFormattedInternalMedia(internalMedia.getInternalMedia(), attrs);
            } else if (userTierElement instanceof TierLink link) {
                // Link
                text = link.toString();
                attrs = styleContext.getTierLinkAttributes();
            } else {
                throw new RuntimeException("Invalid type");
            }

            attrs.addAttributes(commentAttrs);

            if (text != null) {
                appendBatchString(text, attrs);
            }

            if (i < tierData.length() - 1) {
                appendBatchString(" ", attrs);
            }
        }

        for (var hook : getInsertionHooks()) {
            additionalInsertions.addAll(hook.endComment());
        }
        if (!additionalInsertions.isEmpty()) {
            appendAll(additionalInsertions);
        }

        return this;
    }

    /**
     * Writes a given gem to the batch
     *
     * @param gem the comment that will be written
     * @return this builder
     */
    public TranscriptBatchBuilder appendGem(Gem gem) {
        String text = gem.getLabel();
        SimpleAttributeSet gemAttrs = styleContext.getGemAttributes(gem);
        appendBatchEndStart(getTrailingAttributes(), gemAttrs);

        List<DefaultStyledDocument.ElementSpec> additionalInsertions = new ArrayList<>();
        for (var hook : getInsertionHooks()) {
            additionalInsertions.addAll(hook.startGem());
        }
        if (!additionalInsertions.isEmpty()) {
            appendAll(additionalInsertions);
            additionalInsertions.clear();
        }

        SimpleAttributeSet labelAttrs = styleContext.getGemLabelAttributes(gem);
        String labelText = gem.getType().toString() + " Gem";
        labelText = formatLabelText(labelText);

        TranscriptStyleConstants.setClickable(labelAttrs, true);
        appendBatchString(labelText, labelAttrs);

        TranscriptStyleConstants.setClickable(labelAttrs, false);
        appendBatchString(": ", labelAttrs);

        appendBatchString(text, gemAttrs);

        for (var hook : getInsertionHooks()) {
            additionalInsertions.addAll(hook.endGem());
        }
        if (!additionalInsertions.isEmpty()) {
            appendAll(additionalInsertions);
        }

        return this;
    }

    /**
     * Gets the text value of a given tier for a given transcriber
     *
     * @param tier        the tier that the value will come from
     * @param transcriber the transcriber whose text will be returned
     * @return the text value of a given tier for a given transcriber (or the regular text for the tier if the transcriber is the validator or {@code null})
     */
    public String getTierText(Tier<?> tier, String transcriber) {
        if (tier.isBlind() && transcriber != null && !Transcriber.VALIDATOR.getUsername().equals(transcriber)) {
            if (tier.hasBlindTranscription(transcriber)) {
                if (tier.isBlindTranscriptionUnvalidated(transcriber)) {
                    return tier.getBlindUnvalidatedValue(transcriber).getValue();
                }
                return tier.getBlindTranscription(transcriber).toString();
            }
        } else {
            if (tier.hasValue()) {
                if (tier.isUnvalidated()) {
                    return tier.getUnvalidatedValue().getValue();
                }
                return tier.getValue().toString();
            }
        }

        return "";
    }

    /**
     * Inserts a given tier at the end of the batch
     *
     * @param record
     * @param tier         the tier that will be inserted
     * @param tierViewItem a reference to a {@link TierViewItem} used to get font info if any is present
     * @param chatTierNamesShown whether or not chat tier names are shown
     * @param additionaAttrs  an attribute set containing attributes for the containing record to be added to the tier
     *                     attributes (none will be added if {@code null})
     * @return a mutable attribute set containing the attributes of the last character of the tier to add a
     * newline after if need be
     */
    public TranscriptBatchBuilder appendTier(Session session, Record record, Tier<?> tier, TierViewItem tierViewItem, boolean chatTierNamesShown, AttributeSet additionaAttrs) {
        String tierName = tier.getName();
        SimpleAttributeSet tierAttrs = styleContext.getTierAttributes(tier, tierViewItem);
        tierAttrs.addAttributes(styleContext.getRecordAttributes(record));
        if (additionaAttrs != null) {
            tierAttrs.addAttributes(additionaAttrs);
        }
        appendBatchEndStart(getTrailingAttributes(), tierAttrs);

        SimpleAttributeSet labelAttrs = styleContext.getTierLabelAttributes(tier);
        if (additionaAttrs != null) {
            labelAttrs.addAttributes(additionaAttrs);
        }

        String labelText = tierName;
        if (chatTierNamesShown) {
            SystemTierType systemTierType = SystemTierType.tierFromString(tierName);
            if (systemTierType != null) {
                if (systemTierType == SystemTierType.Orthography) {
                    labelText = "*" + record.getSpeaker().getId();
                } else {
                    labelText = systemTierType.getChatTierName();
                }
            } else {
                UserTierType userTierType = UserTierType.fromPhonTierName(tierName);
                if (userTierType != null) {
                    labelText = userTierType.getChatTierName();
                } else {
                    labelText = UserTierType.determineCHATTierName(session, tierName);
                }
            }
        }
        labelText = formatLabelText(labelText);

        TranscriptStyleConstants.setClickable(labelAttrs, true);
        appendBatchString(labelText, labelAttrs);

        TranscriptStyleConstants.setClickable(labelAttrs, false);
        appendBatchString(": ", labelAttrs);

        Class<?> tierType = tier.getDeclaredType();

        if (!tier.hasValue()) {
            appendBatchString("", tierAttrs);
        } else if (tier.isUnvalidated()) {
            appendBatchString(tier.getUnvalidatedValue().getValue(), tierAttrs);
        } else {
            if (tierType.equals(IPATranscript.class)) {
                Tier<IPATranscript> ipaTier = (Tier<IPATranscript>) tier;
                List<IPATranscript> words = (ipaTier).getValue().words();
                for (int i = 0; i < words.size(); i++) {
                    var word = words.get(i);
                    SimpleAttributeSet attrs;
                    if (word.matches("\\P")) {
                        // Pause
                        attrs = styleContext.getIPAPauseAttributes(ipaTier);
                    } else {
                        // Word
                        attrs = styleContext.getIPAWordAttributes(ipaTier);
                    }
                    attrs.addAttributes(tierAttrs);
                    String content = word.toString();
                    appendBatchString(content, attrs);

                    if (i < words.size() - 1) {
                        appendBatchString(" ", tierAttrs);
                    }
                }
            } else if (tierType.equals(MediaSegment.class)) {
                MediaSegment segment = record.getMediaSegment();
                appendFormattedSegment(segment, tierAttrs);
            } else if (tierType.equals(Orthography.class)) {
                Tier<Orthography> orthographyTier = (Tier<Orthography>) tier;
                orthographyTier.getValue().accept(new TranscriptOrthographyVisitors.KeywordVisitor(tierAttrs, this));
            } else if (tierType.equals(MorTierData.class)) {
                Tier<MorTierData> morTier = (Tier<MorTierData>) tier;
                MorTierData mors = morTier.getValue();

                for (int i = 0; i < mors.size(); i++) {
                    Mor mor = mors.get(i);
                    appendBatchString(mor.toString(), tierAttrs);
                    if (i < mors.size() - 1) {
                        appendBatchString(" ", tierAttrs);
                    }
                }
            } else if (tierType.equals(GraspTierData.class)) {
                Tier<GraspTierData> graspTier = (Tier<GraspTierData>) tier;
                GraspTierData grasps = graspTier.getValue();

                for (int i = 0; i < grasps.size(); i++) {
                    Grasp grasp = grasps.get(i);
                    appendBatchString(grasp.toString(), tierAttrs);
                    if (i < grasps.size() - 1) {
                        appendBatchString(" ", tierAttrs);
                    }
                }
            } else if (tierType.equals(TierData.class)) {
                Tier<TierData> userTier = (Tier<TierData>) tier;
                TierData tierData = userTier.getValue();
                if (tierData != null) {
                    for (int i = 0; i < tierData.length(); i++) {
                        TierElement elem = tierData.elementAt(i);
                        String text = null;
                        SimpleAttributeSet attrs;
                        if (elem instanceof TierString tierString) {
                            text = tierString.text();
                            attrs = styleContext.getTierStringAttributes();
                        } else if (elem instanceof TierComment comment) {
                            text = comment.toString();
                            attrs = styleContext.getTierCommentAttributes();
                        } else if (elem instanceof TierInternalMedia internalMedia) {
                            attrs = styleContext.getTierInternalMediaAttributes();
                            appendFormattedInternalMedia(internalMedia.getInternalMedia(), attrs);
                        } else if (elem instanceof TierLink link) {
                            text = link.toString();
                            attrs = styleContext.getTierLinkAttributes();
                        } else {
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

        return this;
    }

    /**
     * Writes the contents of the given record to the batch
     *
     * @param session      the session that the record belongs to
     * @param record       the record that will be written to the batch
     * @param chatTierNamesShown   whether or not chat tier names are shown
     * @return this builder
     */
    public TranscriptBatchBuilder appendRecord(Session session, Record record, boolean chatTierNamesShown) {
        SimpleAttributeSet recordAttrs = styleContext.getRecordAttributes(record);

        List<DefaultStyledDocument.ElementSpec> additionalInsertions = new ArrayList<>();
        for (var hook : getInsertionHooks()) {
            additionalInsertions.addAll(hook.startRecord());
        }
        if (!additionalInsertions.isEmpty()) {
            appendAll(additionalInsertions);
            additionalInsertions.clear();
        }

        for (var hook : getInsertionHooks()) {
            additionalInsertions.addAll(hook.startRecordHeader());
        }
        if (!additionalInsertions.isEmpty()) {
            appendAll(additionalInsertions);
            additionalInsertions.clear();
        }

//        SimpleAttributeSet tierAttrs = styleContext.getTierAttributes(record.getSegmentTier());
//        tierAttrs.addAttributes(styleContext.getSeparatorAttributes());
//        tierAttrs.addAttributes(recordAttrs);
//
//        SimpleAttributeSet labelAttrs = styleContext.getTierLabelAttributes(record.getSegmentTier());
//        labelAttrs.addAttributes(styleContext.getSeparatorAttributes());
//        labelAttrs.addAttributes(recordAttrs);

        for (var hook : getInsertionHooks()) {
            additionalInsertions.addAll(hook.endRecordHeader());
        }
        if (!additionalInsertions.isEmpty()) {
            appendAll(additionalInsertions);
            additionalInsertions.clear();
        }

//        TranscriptStyleConstants.setSeparator(tierAttrs, false);

        final List<TierViewItem> tierView = session.getTierView();
        List<TierViewItem> visibleTierView = tierView.stream().filter(TierViewItem::isVisible).toList();

        final SessionFactory sessionFactory = SessionFactory.newFactory();

        for (int i = 0; i < visibleTierView.size(); i++) {
            TierViewItem item = visibleTierView.get(i);
            Tier<?> tier = record.getTier(item.getTierName());
            // Create a tier if it doesn't exist in the record
            // This tier will be added to the record when a change is made in the TierEdit
            if (tier == null) {
                TierDescription td = session.getUserTiers().get(item.getTierName());
                if (td == null) continue;
                tier = sessionFactory.createTier(td);
            }

            for (var hook : getInsertionHooks()) {
                additionalInsertions.addAll(hook.startTier());
            }
            if (!additionalInsertions.isEmpty()) {
                appendAll(additionalInsertions);
                additionalInsertions.clear();
            }

            if(i == 0) {
                TranscriptStyleConstants.setBorder(recordAttrs, BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Button.background")));
            } else {
                TranscriptStyleConstants.setBorder(recordAttrs, BorderFactory.createEmptyBorder());
            }
            appendTier(session, record, tier, item, chatTierNamesShown, recordAttrs);

            for (var hook : getInsertionHooks()) {
                additionalInsertions.addAll(hook.endTier(getTrailingAttributes()));
            }
            if (!additionalInsertions.isEmpty()) {
                appendAll(additionalInsertions);
            }
            appendEOL();
        }

        for (var hook : getInsertionHooks()) {
            additionalInsertions.addAll(hook.endRecord());
        }
        if (!additionalInsertions.isEmpty()) {
            appendAll(additionalInsertions);
        }

        return this;
    }

    /**
     * Gets the attributes of the last {@link javax.swing.text.DefaultStyledDocument.ElementSpec} in a provided list
     *
     * @return the attributes of the last elementspec
     */
    public SimpleAttributeSet getTrailingAttributes() {
        if (batch.isEmpty()) return new SimpleAttributeSet();
        SimpleAttributeSet attrs = new SimpleAttributeSet(batch.get(batch.size() - 1).getAttributes());
        TranscriptStyleConstants.setComponentFactory(attrs, null);
        TranscriptStyleConstants.setEnterAction(attrs, null);
        TranscriptStyleConstants.setClickable(attrs, false);
        return attrs;
    }

    public int size() {
        return batch.size();
    }

    public boolean isEmpty() {
        return batch.isEmpty();
    }

}
