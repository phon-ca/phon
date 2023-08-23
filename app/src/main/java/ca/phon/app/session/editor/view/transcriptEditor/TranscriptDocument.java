package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.log.LogUtil;
import ca.phon.formatter.MediaTimeFormatter;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.usertier.*;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.PhoneMapDisplay;
import ca.phon.ui.ipa.SyllabificationDisplay;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TranscriptDocument extends DefaultStyledDocument {
    private Session session;
    private boolean targetSyllablesVisible = false;
    private boolean actualSyllablesVisible = false;
    private boolean alignmentVisible = false;
    private BiFunction<Tier<?>, TierViewItem, JComponent> tierLabelFactory;
    private Function<Comment, JComponent> commentLabelFactory;
    private Function<Gem, JComponent> gemLabelFactory;
    private BiFunction<Record, Integer, JComponent> separatorFactory;
    private final SessionFactory sessionFactory;

    public TranscriptDocument() {
        super(new StyleContext());
        sessionFactory = SessionFactory.newFactory();
        setDocumentFilter(new TranscriptDocumentFilter());
    }

    // region Visible Getters/Setters

    public boolean getTargetSyllablesVisible() {
        return targetSyllablesVisible;
    }

    public void setTargetSyllablesVisible(boolean targetSyllablesVisible) {
        this.targetSyllablesVisible = targetSyllablesVisible;
        reload();
    }

    public boolean getActualSyllablesVisible() {
        return actualSyllablesVisible;
    }

    public void setActualSyllablesVisible(boolean actualSyllablesVisible) {
        this.actualSyllablesVisible = actualSyllablesVisible;
        reload();
    }

    public boolean getAlignmentVisible() {
        return alignmentVisible;
    }

    public void setAlignmentVisible(boolean alignmentVisible) {
        if (alignmentVisible != this.alignmentVisible) {
            this.alignmentVisible = alignmentVisible;
            reload();
        }
    }

    // endregion Visible Getters/Setters

    // region Attribute Getters

    private SimpleAttributeSet getRecordAttributes(int recordIndex) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        int recordElementIndex = session.getRecordElementIndex(recordIndex);
        retVal.addAttribute("recordIndex", recordIndex);
        retVal.addAttribute("recordElementIndex", recordElementIndex);

        return retVal;
    }

    private SimpleAttributeSet getTierAttributes(Tier<?> tier, TierViewItem item) {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute("tier", tier);
        String fontString = item.getTierFont();
        if ("default".equalsIgnoreCase(fontString)) {
            fontString = FontPreferences.TIER_FONT;
        }
        var font = Font.decode(fontString);

        StyleConstants.setFontFamily(retVal, font.getFamily());
        StyleConstants.setFontSize(retVal, font.getSize());
        StyleConstants.setBold(retVal, font.isBold());
        StyleConstants.setItalic(retVal, font.isItalic());

        return retVal;
    }

    private SimpleAttributeSet getIPATierAttributes(Tier<IPATranscript> tier) {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute("tier", tier);

        SyllabificationDisplay display = new SyllabificationDisplay();
        display.setTranscript(tier.getValue());
        StyleConstants.setComponent(retVal, display);

        return retVal;
    }

    private SimpleAttributeSet getIPAWordAttributes(Tier<IPATranscript> tier) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute("tier", tier);

        StyleConstants.setForeground(retVal, Color.black);

        return retVal;
    }

    private SimpleAttributeSet getIPAPauseAttributes(Tier<IPATranscript> tier) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute("tier", tier);

        StyleConstants.setForeground(retVal, Color.gray);

        return retVal;
    }

    private SimpleAttributeSet getSegmentTimeAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        return retVal;
    }

    private SimpleAttributeSet getSegmentDashAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        StyleConstants.setForeground(retVal, Color.GRAY);

        return retVal;
    }

    private SimpleAttributeSet getUserTierStringAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        StyleConstants.setForeground(retVal, Color.black);

        return retVal;
    }

    private SimpleAttributeSet getUserTierCommentAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        StyleConstants.setForeground(retVal, Color.gray);

        return retVal;
    }

    private SimpleAttributeSet getUserTierInternalMediaAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        StyleConstants.setForeground(retVal, Color.gray);

        return retVal;
    }

    private SimpleAttributeSet getAlignmentAttributes(PhoneAlignment alignment) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        PhoneMapDisplay display = new PhoneMapDisplay();
        int wordIndex = 0;
        for (Iterator<PhoneMap> i = alignment.iterator(); i.hasNext();) {
            var phoneMap = i.next();
            display.setPhoneMapForWord(wordIndex, phoneMap);
            wordIndex++;
        }
        StyleConstants.setComponent(retVal, display);

        return retVal;
    }

    private SimpleAttributeSet getTierLabelAttributes(Tier<?> tier, TierViewItem tierViewItem) {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();

        if (tierLabelFactory != null) {
            JLabel tierLabel = (JLabel) tierLabelFactory.apply(tier, tierViewItem);
            StyleConstants.setComponent(retVal, tierLabel);
        }

        retVal.addAttribute("locked", tierViewItem.isTierLocked());
        retVal.addAttribute("label", true);
        retVal.addAttribute("tier", tier);

        return retVal;
    }

    private SimpleAttributeSet getSeparatorAttributes(Record record, int recordIndex) {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();

        if (separatorFactory != null) {
            StyleConstants.setComponent(retVal, separatorFactory.apply(record, recordIndex));
        }

        return retVal;
    }

    private SimpleAttributeSet getCommentAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        return retVal;
    }

    private SimpleAttributeSet getCommentLabelAttributes(Comment comment) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        if (commentLabelFactory != null) {
            JLabel commentLabel = (JLabel) commentLabelFactory.apply(comment);
            StyleConstants.setComponent(retVal, commentLabel);
        }

        retVal.addAttribute("comment", comment);
        retVal.addAttribute("label", true);

        return retVal;
    }

    private SimpleAttributeSet getGemAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        return retVal;
    }

    private SimpleAttributeSet getGemLabelAttributes(Gem gem) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        if (gemLabelFactory != null) {
            JLabel gemLabel = (JLabel) gemLabelFactory.apply(gem);
            StyleConstants.setComponent(retVal, gemLabel);
        }

        retVal.addAttribute("gem", gem);
        retVal.addAttribute("label", true);

        return retVal;
    }

    // endregion Attribute Getters

    public void moveTier(List<TierViewItem> movedTiers) {

        List<TierViewItem> movedTiersNewOrder = new ArrayList<>();
        for (TierViewItem item : session.getTierView()) {
            if (movedTiers.contains(item)) {
                movedTiersNewOrder.add(item);
            }
        }

        // Reload the contents of the editor
        int recordCount = session.getRecordCount();
        for (int i = 0; i < recordCount; i++) {
            int offset = -1;
            for (TierViewItem item : movedTiers) {
                try {
                    String tierName = item.getTierName();
                    int labelLength = tierName.length() + 2;

                    int tierStartOffset = getTierStart(i, tierName) - labelLength;
                    int tierEndOffset = getTierEnd(i, tierName);
                    remove(tierStartOffset, tierEndOffset - tierStartOffset);

                    if (offset == -1) {
                        offset = tierStartOffset;
                    }
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            for (TierViewItem item : movedTiersNewOrder) {
                try {
                    offset = insertTier(i, item, offset);
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void deleteTier(List<String> deletedTiers) {
        int recordCount = session.getRecordCount();
        for (int i = 0; i < recordCount; i++) {
            try {
                for (String tierName : deletedTiers) {
                    int labelLength = tierName.length() + 2;
                    int tierStartOffset = getTierStart(i, tierName) - labelLength;
                    int tierEndOffset = getTierEnd(i, tierName);
                    remove(tierStartOffset, tierEndOffset - tierStartOffset);
                }
            }
            catch (Exception e) {
                LogUtil.severe(e);
            }
        }
    }

    public void addTier(List<TierViewItem> addedTiers) {
        int recordCount = session.getRecordCount();
        for (int i = 0; i < recordCount; i++) {
            int offset = getRecordEnd(i, null);
            try {
                for (TierViewItem item : addedTiers) {
                    offset = insertTier(i, item, offset);
                }
                int recordStart = getRecordStart(i, null);
                setParagraphAttributes(
                        recordStart,
                    offset - recordStart,
                    getRecordAttributes(i),
                    true
                );
            }
            catch (Exception e) {
                LogUtil.severe(e);
            }
        }
    }

    public void hideTier(List<String> hiddenTiers) {
        System.out.println(hiddenTiers);
        int recordCount = session.getRecordCount();
        for (int i = 0; i < recordCount; i++) {
            try {
                for (String tierName : hiddenTiers) {
                    int labelLength = tierName.length() + 2;
                    int tierStartOffset = getTierStart(i, tierName) - labelLength;
                    int tierEndOffset = getTierEnd(i, tierName);
                    remove(tierStartOffset, tierEndOffset - tierStartOffset);
                }
            }
            catch (Exception e) {
                LogUtil.severe(e);
            }
        }
    }

    public void showTier(List<TierViewItem> shownTiers) {
        int recordCount = session.getRecordCount();
        for (int i = 0; i < recordCount; i++) {
            int offset = getRecordEnd(i, null);
            try {
                for (TierViewItem item : shownTiers) {
                    offset = insertTier(i, item, offset);
                }
                int recordStart = getRecordStart(i, null);
                setParagraphAttributes(
                    recordStart,
                    offset - recordStart,
                    getRecordAttributes(i),
                    true
                );
            }
            catch (Exception e) {
                LogUtil.severe(e);
            }
        }
    }

    public void tierFontChanged(List<TierViewItem> changedTiers) {
        int recordCount = session.getRecordCount();
        for (int i = 0; i < recordCount; i++) {
            try {
                for (TierViewItem item : changedTiers) {
                    String tierName = item.getTierName();
                    int labelLength = tierName.length() + 2;
                    int tierStartOffset = getTierStart(i, tierName) - labelLength;
                    int tierEndOffset = getTierEnd(i, tierName);

                    remove(tierStartOffset, tierEndOffset - tierStartOffset);

                    insertTier(i, item, tierStartOffset);
                }
            }
            catch (Exception e) {
                System.out.println(i);
                LogUtil.severe(e);
            }
        }
    }

    public void tierNameChanged(List<TierViewItem> changedTiers) {

    }

    public void reload() {
        try {
            // Remove the old stuff
            remove(0, getLength());
            // Put the new stuff back
            populate();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
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

    // region Get Record/Tier Start/End

    public int getRecordStart(int recordIndex, String tierName) {
        Element root = getDefaultRootElement();

        int retVal = -1;

        for (int i = 0; i < root.getElementCount(); i++) {
            Element e = root.getElement(i);
            AttributeSet attrs = e.getAttributes();
            var currentRecordIndex = attrs.getAttribute("recordIndex");
            if (currentRecordIndex != null && recordIndex == (int)currentRecordIndex) {
                if (retVal == -1) {
                    retVal = e.getStartOffset();
                }
                else {
                    retVal = Math.min(retVal, e.getStartOffset());
                }
            }
        }

        return retVal;
    }

    public int getRecordStart(Tier<?> tier) {
        Element root = getDefaultRootElement();

        int retVal = -1;

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            Integer currentRecordIndex = (Integer) elem.getAttributes().getAttribute("recordIndex");
            // If correct record index
            if (currentRecordIndex != null) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> currentTier = (Tier<?>)attrs.getAttribute("tier");
                    Boolean isLabel = (Boolean) attrs.getAttribute("label");
                    // If correct tier
                    if (currentTier != null && currentTier == tier) {
                        if (retVal == -1) {
                            retVal = elem.getStartOffset();
                        }
                        else {
                            retVal = Math.min(retVal, elem.getStartOffset());
                        }
                    }
                }
            }
        }

        return retVal;
    }

    public int getRecordEnd(int recordIndex, String tierName) {
        Element root = getDefaultRootElement();

        int retVal = -1;

        for (int i = 0; i < root.getElementCount(); i++) {
            Element e = root.getElement(i);
            AttributeSet attrs = e.getAttributes();
            var currentRecordIndex = attrs.getAttribute("recordIndex");
            if (currentRecordIndex != null && recordIndex == (int)currentRecordIndex) {
                retVal = Math.max(retVal, e.getEndOffset());
            }
        }

        return retVal;
    }

    public int getRecordEnd(Tier<?> tier) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            var currentRecordIndex = elem.getAttributes().getAttribute("recordIndex");
            // If correct record index
            if (currentRecordIndex != null) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    var currentTier = innerElem.getAttributes().getAttribute("tier");
                    // If correct tier
                    if (currentTier != null && currentTier == tier) {
                        return getRecordEnd((int)currentRecordIndex, tier.getName());
                    }
                }
            }
        }

        return -1;
    }

    public int getTierStart(int recordIndex, String tierName) {
        Element root = getDefaultRootElement();

        int retVal = -1;

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            Integer currentRecordIndex = (Integer)elem.getAttributes().getAttribute("recordIndex");
            // If correct record index
            if (currentRecordIndex != null && currentRecordIndex == recordIndex) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> tier = (Tier<?>) attrs.getAttribute("tier");
                    Boolean isLabel = (Boolean)attrs.getAttribute("label");
                    // If correct tier name
                    if (isLabel == null && tier != null && tier.getName().equals(tierName)) {
                        if (retVal == -1) {
                            retVal = innerElem.getStartOffset();
                        }
                        else {
                            retVal = Math.min(retVal, innerElem.getStartOffset());
                        }
                    }
                }
            }
        }

        return retVal;
    }

    public int getTierStart(Tier<?> tier) {
        Element root = getDefaultRootElement();

        int retVal = -1;

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            Integer currentRecordIndex = (Integer) elem.getAttributes().getAttribute("recordIndex");
            // If correct record index
            if (currentRecordIndex != null) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> currentTier = (Tier<?>)attrs.getAttribute("tier");
                    Boolean isLabel = (Boolean)attrs.getAttribute("label");
                    // If correct tier
                    if (isLabel == null && currentTier != null && currentTier == tier) {
                        if (retVal == -1) {
                            retVal = innerElem.getStartOffset();
                        }
                        else {
                            retVal = Math.min(retVal, innerElem.getStartOffset());
                        }
                    }
                }
            }
        }

        return retVal;
    }

    public int getTierEnd(int recordIndex, String tierName) {
        Element root = getDefaultRootElement();

        int retVal = -1;

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            var currentRecordIndex = elem.getAttributes().getAttribute("recordIndex");
            // If correct record index
            if (currentRecordIndex != null && ((int)currentRecordIndex) == recordIndex) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> tier = (Tier<?>)attrs.getAttribute("tier");
                    Boolean isLabel = (Boolean)attrs.getAttribute("label");
                    // If correct tier name
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
            Integer currentRecordIndex = (Integer) elem.getAttributes().getAttribute("recordIndex");
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

    // endregion Get Record/Tier Start/End

    private int insertTier(int recordIndex, TierViewItem tierViewItem, int offset) throws BadLocationException {
        String tierName = tierViewItem.getTierName();
        Record record = session.getRecord(recordIndex);
        Tier<?> tier = record.getTier(tierName);
        if (tier == null) return offset;

        SimpleAttributeSet tierAttrs = getTierAttributes(tier, tierViewItem);

        SimpleAttributeSet labelAttrs = getTierLabelAttributes(tier, tierViewItem);

        insertString(offset, tierName + ": ", labelAttrs);
        offset += tierName.length() + 2;

        if (tierName.equals("IPA Target")) {
            Tier<IPATranscript> ipaTier = (Tier<IPATranscript>)tier;
            if (targetSyllablesVisible) {
                String ipaTarget = ipaTier.getValue().toString(true);
                insertString(offset++, ipaTarget, getIPATierAttributes(ipaTier));
            }
            else {
                List<IPATranscript> words = (ipaTier).getValue().words();
                for (var word : words) {
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
                    insertString(offset, content, attrs);
                    offset += content.length();

                    insertString(offset++, " ", tierAttrs);
                }

                remove(offset - 1, 1);
                offset--;
            }
        }
        else if (tierName.equals("IPA Actual")) {
            Tier<IPATranscript> ipaTier = (Tier<IPATranscript>)tier;
            if (actualSyllablesVisible) {
                String ipaActual = ipaTier.getValue().toString(true);
                insertString(offset++, ipaActual, getIPATierAttributes(ipaTier));
            }
            else {
                List<IPATranscript> words = (ipaTier).getValue().words();
                for (var word : words) {
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
                    insertString(offset, content, attrs);
                    offset += content.length();

                    insertString(offset++, " ", tierAttrs);
                }
                remove(offset - 1, 1);
                offset--;
            }

        }
        else if (tierName.equals("Alignment")) {
            insertString(offset++, " ", getAlignmentAttributes(record.getPhoneAlignment()));
        }
        else if (tierName.equals("Segment")) {
            MediaSegment segment = record.getMediaSegment();
            String start = MediaTimeFormatter.msToPaddedMinutesAndSeconds(segment.getStartValue());

            var segmentTimeAttrs = getSegmentTimeAttributes();
            segmentTimeAttrs.addAttributes(tierAttrs);
            var segmentDashAttrs = getSegmentDashAttributes();
            segmentDashAttrs.addAttributes(tierAttrs);

            insertString(offset++, "•", segmentDashAttrs);

            insertString(offset, start, segmentTimeAttrs);
            offset += start.length();

            insertString(offset++, "-", segmentDashAttrs);

            String end = MediaTimeFormatter.msToPaddedMinutesAndSeconds(segment.getEndValue());

            insertString(offset, end, segmentTimeAttrs);
            offset += end.length();

            insertString(offset++, "•", segmentDashAttrs);
        }
        else if (tierName.equals("Orthography")) {
            String tierContent = tier.toString();

            insertString(offset, tierContent, tierAttrs);

            offset += tierContent.length();
        }
        else {
            Tier<UserTierData> notesTier = (Tier<UserTierData>) tier;
            UserTierData tierData = notesTier.getValue();
            for (int i = 0; i < tierData.length(); i++) {
                UserTierElement elem = tierData.elementAt(i);
                String text;
                SimpleAttributeSet attrs;
                if (elem instanceof TierString tierString) {
                    text = tierString.text();
                    attrs = getUserTierStringAttributes();
                }
                else if (elem instanceof UserTierComment comment) {
                    text = "[%" + comment.text() + "]";
                    attrs = getUserTierCommentAttributes();
                }
                else {
                    UserTierInternalMedia internalMedia = (UserTierInternalMedia) elem;
                    text = "•" + internalMedia.text() + "•";
                    attrs = getUserTierInternalMediaAttributes();
                }

                attrs.addAttributes(tierAttrs);

                insertString(offset, text, attrs);
                offset += text.length();

                insertString(offset++, " ", tierAttrs);
            }

            remove(offset - 1, 1);
            offset--;
        }

        insertString(offset++, "\n", tierAttrs);

        return offset;
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

    private void populate() throws BadLocationException {

        Transcript transcript = session.getTranscript();
        var tierView = session.getTierView();

        int len = 0;

        for (Transcript.Element elem : transcript) {
            if (elem.isRecord()) {
                int recordStart = len;
                Record record = elem.asRecord();
                int recordIndex = transcript.getRecordPosition(record);

                insertString(len, "-\n", getSeparatorAttributes(record, recordIndex));
                len += 2;

                for (var item : tierView) {
                    if (!item.isVisible()) continue;

                    //String tierName = tv.getTierName();

                    len = insertTier(recordIndex, item, len);
                }

                setParagraphAttributes(
                    recordStart,
                    len - recordStart,
                    getRecordAttributes(recordIndex),
                    false
                );
            }
            else if (elem.isComment()) {
                Comment comment = elem.asComment();
                UserTierData tierData = comment.getValue();

                String labelText = comment.getType().getLabel() + ": ";
                insertString(len, labelText, getCommentLabelAttributes(comment));
                len += labelText.length();

                for (int i = 0; i < tierData.length(); i++) {
                    UserTierElement userTierElement = tierData.elementAt(i);
                    String text;
                    SimpleAttributeSet attrs;
                    if (userTierElement instanceof TierString tierString) {
                        // Text
                        text = tierString.text();
                        attrs = getUserTierStringAttributes();
                    } else if (userTierElement instanceof UserTierComment userTierComment) {
                        // Comment
                        text = "[%" + userTierComment.text() + "]";
                        attrs = getUserTierCommentAttributes();
                    } else {
                        // Internal media
                        UserTierInternalMedia internalMedia = (UserTierInternalMedia) userTierElement;
                        text = "•" + internalMedia.text() + "•";
                        attrs = getUserTierInternalMediaAttributes();
                    }

                    insertString(len, text, attrs);
                    len += text.length();

                    insertString(len++, " ", attrs);
                }

                remove(len - 1, 1);
                len--;

                insertString(len++, "\n", null);
            }
            else {
                Gem gem = elem.asGem();

                String text = gem.getLabel();

                String labelText = gem.getType().toString() + ": ";
                insertString(len, labelText, getGemLabelAttributes(gem));
                len += labelText.length();

                insertString(len, text, getGemAttributes());
                len += text.length();

                insertString(len++, "\n", null);
            }
        }


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
        catch (Exception e) {
            LogUtil.severe(e);
        }
    }

    public BiFunction<Tier<?>, TierViewItem, JComponent> getTierLabelFactory() {
        return tierLabelFactory;
    }

    public void setTierLabelFactory(BiFunction<Tier<?>, TierViewItem, JComponent> tierLabelFactory) {
        this.tierLabelFactory = tierLabelFactory;
    }

    public Function<Comment, JComponent> getCommentLabelFactory() {
        return commentLabelFactory;
    }

    public void setCommentLabelFactory(Function<Comment, JComponent> commentLabelFactory) {
        this.commentLabelFactory = commentLabelFactory;
    }

    public Function<Gem, JComponent> getGemLabelFactory() {
        return gemLabelFactory;
    }

    public void setGemLabelFactory(Function<Gem, JComponent> gemLabelFactory) {
        this.gemLabelFactory = gemLabelFactory;
    }

    public BiFunction<Record, Integer, JComponent> getSeparatorFactory() {
        return separatorFactory;
    }

    public void setSeparatorFactory(BiFunction<Record, Integer, JComponent> separatorFactory) {
        this.separatorFactory = separatorFactory;
    }

    // endregion Getters and Setters

    private class TranscriptDocumentFilter extends DocumentFilter {
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (attrs != null) {
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
