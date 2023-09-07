package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.log.LogUtil;
import ca.phon.formatter.MediaTimeFormatStyle;
import ca.phon.formatter.MediaTimeFormatter;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.tierdata.*;
import ca.phon.session.usertier.*;
import ca.phon.ui.FontFormatter;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.PhoneMapDisplay;
import ca.phon.ui.ipa.SyllabificationDisplay;
import ca.phon.worker.PhonWorker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TranscriptDocument extends DefaultStyledDocument {
    private Session session;
    private BiFunction<Tier<?>, TierViewItem, JComponent> tierLabelFactory;
    private Function<Comment, JComponent> commentLabelFactory;
    private Function<Gem, JComponent> gemLabelFactory;
    private BiFunction<Record, Integer, JComponent> separatorFactory;
    private final SessionFactory sessionFactory;
    private boolean singleRecordView = false;
    private int singleRecordIndex = 0;
    private static final char[] EOL_ARRAY = { '\n' };
    private ArrayList<ElementSpec> batch;
    private boolean syllabificationVisible = false;
    private boolean syllabificationIsComponent = true;
    private boolean alignmentVisible = false;
    private boolean alignmentIsComponent = true;
    private boolean labelsVisible = true;

    public TranscriptDocument() {
        super(new TranscriptStyleContext());
        sessionFactory = SessionFactory.newFactory();
        setDocumentFilter(new TranscriptDocumentFilter());
        batch = new ArrayList<>();
    }

    public void appendBatchString(String str, AttributeSet a) {
        // We could synchronize this if multiple threads
        // would be in here. Since we're trying to boost speed,
        // we'll leave it off for now.

        // Make a copy of the attributes, since we will hang onto
        // them indefinitely and the caller might change them
        // before they are processed.
        a = a.copyAttributes();
        char[] chars = str.toCharArray();
        batch.add(new ElementSpec(a, ElementSpec.ContentType, chars, 0, str.length()));
    }

    public void appendBatchLineFeed(AttributeSet a) {
        // See sync notes above. In the interest of speed, this
        // isn't synchronized.

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

    // region Attribute Getters

    private SimpleAttributeSet getRecordAttributes(int recordIndex) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        int recordElementIndex = session.getRecordElementIndex(recordIndex);
        retVal.addAttribute("recordIndex", recordIndex);
        retVal.addAttribute("recordElementIndex", recordElementIndex);
        retVal.addAttribute("elementType", "record");

        return retVal;
    }

    private SimpleAttributeSet getTierAttributes(Tier<?> tier, TierViewItem item) {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute("tier", tier);
        String fontString = item.getTierFont();
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

    private SimpleAttributeSet getIPATierAttributes(Tier<IPATranscript> tier) {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute("tier", tier);

        SyllabificationDisplay display = new SyllabificationDisplay();
        display.setTranscript(tier.getValue());
        display.setBorder(new EmptyBorder(0,150,0,0));
        StyleConstants.setComponent(retVal, display);

        return retVal;
    }

    private SimpleAttributeSet getAlignmentAttributes(PhoneAlignment alignment) {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();

        PhoneMapDisplay display = new PhoneMapDisplay();
        int wordIndex = 0;
        for (Iterator<PhoneMap> i = alignment.iterator(); i.hasNext();) {
            var phoneMap = i.next();
            display.setPhoneMapForWord(wordIndex, phoneMap);
            wordIndex++;
        }
        display.setBorder(new EmptyBorder(0,150,0,0));
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

    private SimpleAttributeSet getTierStringAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        StyleConstants.setForeground(retVal, Color.black);

        return retVal;
    }

    private SimpleAttributeSet getTierCommentAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        StyleConstants.setForeground(retVal, Color.gray);

        return retVal;
    }

    private SimpleAttributeSet getTierInternalMediaAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        StyleConstants.setForeground(retVal, Color.gray);

        return retVal;
    }

    private SimpleAttributeSet getTierLabelAttributes(Tier<?> tier, TierViewItem tierViewItem) {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();

        if (labelsVisible && tierLabelFactory != null) {
            JLabel tierLabel = (JLabel) tierLabelFactory.apply(tier, tierViewItem);
            StyleConstants.setComponent(retVal, tierLabel);
        }

        retVal.addAttribute("locked", tierViewItem.isTierLocked());
        retVal.addAttribute("label", true);
        retVal.addAttribute("notEditable", true);
        retVal.addAttribute("tier", tier);

        Font font = FontPreferences.getTierFont();
        StyleConstants.setFontFamily(retVal, font.getFamily());
        StyleConstants.setFontSize(retVal, font.getSize());
        StyleConstants.setBold(retVal, font.isBold());
        StyleConstants.setItalic(retVal, font.isItalic());

        return retVal;
    }

    private SimpleAttributeSet getSeparatorAttributes(Record record, int recordIndex) {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();

        if (labelsVisible && separatorFactory != null) {
            StyleConstants.setComponent(retVal, separatorFactory.apply(record, recordIndex));
        }

        retVal.addAttribute("notEditable", true);
        retVal.addAttribute("label", true);
        retVal.addAttribute("sep", true);

        Font font = FontPreferences.getTierFont();
        StyleConstants.setFontFamily(retVal, font.getFamily());
        StyleConstants.setFontSize(retVal, font.getSize());
        StyleConstants.setBold(retVal, font.isBold());
        StyleConstants.setItalic(retVal, font.isItalic());

        return retVal;
    }

    private SimpleAttributeSet getCommentAttributes(Comment comment) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttribute("elementType", "comment");
        retVal.addAttribute("comment", comment);

        Font font = FontPreferences.getTierFont();
        StyleConstants.setFontFamily(retVal, font.getFamily());
        StyleConstants.setFontSize(retVal, font.getSize());
        StyleConstants.setBold(retVal, font.isBold());
        StyleConstants.setItalic(retVal, font.isItalic());

        return retVal;
    }

    private SimpleAttributeSet getCommentLabelAttributes(Comment comment) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        if (labelsVisible && commentLabelFactory != null) {
            JLabel commentLabel = (JLabel) commentLabelFactory.apply(comment);
            StyleConstants.setComponent(retVal, commentLabel);
        }

        retVal.addAttributes(getCommentAttributes(comment));

        retVal.addAttribute("label", true);
        retVal.addAttribute("notEditable", true);

        return retVal;
    }

    private SimpleAttributeSet getGemAttributes(Gem gem) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttribute("elementType", "gem");
        retVal.addAttribute("gem", gem);

        Font font = FontPreferences.getTierFont();
        StyleConstants.setFontFamily(retVal, font.getFamily());
        StyleConstants.setFontSize(retVal, font.getSize());
        StyleConstants.setBold(retVal, font.isBold());
        StyleConstants.setItalic(retVal, font.isItalic());

        return retVal;
    }

    private SimpleAttributeSet getGemLabelAttributes(Gem gem) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        if (labelsVisible && gemLabelFactory != null) {
            JLabel gemLabel = (JLabel) gemLabelFactory.apply(gem);
            StyleConstants.setComponent(retVal, gemLabel);
        }

        retVal.addAttributes(getGemAttributes(gem));

        retVal.addAttribute("label", true);
        retVal.addAttribute("notEditable", true);

        return retVal;
    }

    // endregion Attribute Getters

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
        int recordCount = session.getRecordCount();
        for (int i = 0; i < recordCount; i++) {
            SimpleAttributeSet recordAttrs = getRecordAttributes(i);
            Record record = session.getRecord(i);

            movedTiers = movedTiers.stream().filter(item -> record.hasTier(item.getTierName())).toList();
            if (movedTiers.size() < 2) continue;

            int offset = -1;
            for (TierViewItem item : movedTiers) {
                try {
                    String tierName = item.getTierName();
                    int labelLength = tierName.length() + 2;

                    int tierStartOffset = getTierStart(i, tierName) - labelLength;
                    int tierEndOffset = getTierEnd(i, tierName);

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
                    offset = insertTier(i, item, offset, recordAttrs, true);
                }
                processBatchUpdates(offsetBeforeInsert);
            }
            catch (BadLocationException e) {
                LogUtil.severe(e);
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

                    if (tierStartOffset < 0 || tierEndOffset < 0) continue;

                    remove(tierStartOffset, tierEndOffset - tierStartOffset);
                }
            }
            catch (BadLocationException e) {
                LogUtil.severe(e);
            }
        }
    }

    public void addTier(List<TierViewItem> addedTiers) {
        int recordCount = session.getRecordCount();
        for (int i = 0; i < recordCount; i++) {
            int offset = getRecordEnd(i);
            try {
                appendBatchEndStart();
                for (TierViewItem item : addedTiers) {
                    insertTier(i, item, offset, getRecordAttributes(i), true);
                }
                processBatchUpdates(offset);
            }
            catch (BadLocationException e) {
                LogUtil.severe(e);
            }
        }
    }

    public void hideTier(List<String> hiddenTiers) {
        int recordCount = session.getRecordCount();
        for (int i = 0; i < recordCount; i++) {
            Record record = session.getRecord(i);
            try {
                for (String tierName : hiddenTiers) {
                    if (!record.hasTier(tierName)) continue;

                    int labelLength = tierName.length() + 2;
                    int tierStartOffset = getTierStart(i, tierName) - labelLength;
                    int tierEndOffset = getTierEnd(i, tierName);
                    remove(tierStartOffset, tierEndOffset - tierStartOffset);
                }
            }
            catch (BadLocationException e) {
                LogUtil.severe(e);
            }
        }
    }

    public void showTier(List<TierViewItem> shownTiers, List<TierViewItem> newTierView) {
        int recordCount = session.getRecordCount();
        for (int i = 0; i < recordCount; i++) {
            SimpleAttributeSet recordAttrs = getRecordAttributes(i);
            Record record = session.getRecord(i);
            int offset = getRecordStart(i);
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
                        offset = getTierEnd(i, itemBeforeShownItem.getTierName());
                    }

                    int offsetBeforeInsert = offset;
                    appendBatchEndStart();
                    offset = insertTier(i, item, offset, recordAttrs, true);
                    processBatchUpdates(offsetBeforeInsert);
                }
            }
            catch (BadLocationException e) {
                LogUtil.severe(e);
            }
        }
    }

    public void tierFontChanged(List<TierViewItem> changedTiers) {
        int recordCount = session.getRecordCount();
        for (int i = 0; i < recordCount; i++) {
            SimpleAttributeSet recordAttrs = getRecordAttributes(i);
            Record record = session.getRecord(i);
            try {
                for (TierViewItem item : changedTiers) {
                    String tierName = item.getTierName();
                    if (!record.hasTier(tierName)) continue;

                    int labelLength = tierName.length() + 2;
                    int tierStartOffset = getTierStart(i, tierName) - labelLength;
                    int tierEndOffset = getTierEnd(i, tierName);

                    remove(tierStartOffset, tierEndOffset - tierStartOffset);

                    appendBatchEndStart();
                    insertTier(i, item, tierStartOffset, recordAttrs, true);
                    processBatchUpdates(tierStartOffset);
                }
            }
            catch (BadLocationException e) {
                LogUtil.severe(e);
            }
        }
    }

    public void tierNameChanged(List<TierViewItem> oldTiers, List<TierViewItem> newTiers) {
        int recordCount = session.getRecordCount();
        System.out.println("Session tiers");
        System.out.println(session.getTiers().stream().map(tierDescription -> tierDescription.getName()).toList());
        for (int i = 0; i < recordCount; i++) {
            try {
                SimpleAttributeSet recordAttrs = getRecordAttributes(i);
                Record record = session.getRecord(i);
                for (int j = 0; j < oldTiers.size(); j++) {
                    String oldTierName = oldTiers.get(j).getTierName();
                    TierViewItem newTier = newTiers.get(j);
                    //System.out.println(record.getUserDefinedTierNames());

                    int oldTierLabelLength = oldTierName.length() + 2;
                    int tierStartOffset = getTierStart(i, oldTierName) - oldTierLabelLength;
                    int tierEndOffset = getTierEnd(i, oldTierName);

                    if (tierStartOffset < 0 || tierEndOffset < 0) continue;

                    //System.out.println("Removing " + oldTierName + " from " + tierStartOffset + " to " + tierEndOffset + " in record " + i);

                    remove(tierStartOffset, tierEndOffset - tierStartOffset);

                    //System.out.println(insertTier(i, newTier, tierStartOffset, recordAttrs, false));
                }
            }
            catch (BadLocationException e) {
                LogUtil.severe(e);
            }
        }
    }

    private TierViewItem getAlignmentTierView() {
        List<TierViewItem> visibleTierView = session.getTierView().stream().filter(item -> item.isVisible()).toList();

        var retVal = visibleTierView.stream().filter(item -> item.getTierName().equals("IPA Actual")).findFirst();
        if (retVal.isPresent()) return retVal.get();

        retVal = visibleTierView.stream().filter(item -> item.getTierName().equals("IPA Target")).findFirst();
        if (retVal.isPresent()) return retVal.get();

        return visibleTierView.get(visibleTierView.size()-1);
    }

    public void reload() {
        try {
            // Remove the old stuff
            remove(0, getLength());
            try {
                populate();
            }
            catch (BadLocationException e) {
                LogUtil.severe(e);
            }
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

    // region Get Record/Tier Start/End

    public int getRecordStart(int recordIndex) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            AttributeSet attrs = elem.getElement(0).getAttributes();
            var currentRecordIndex = attrs.getAttribute("recordIndex");
            if (currentRecordIndex != null && recordIndex == (int)currentRecordIndex) {
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
            Integer currentRecordIndex = (Integer)elem.getElement(0).getAttributes().getAttribute("recordIndex");
            // If correct record index
            if (currentRecordIndex != null && currentRecordIndex == recordIndex) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> tier = (Tier<?>) attrs.getAttribute("tier");
                    Boolean isLabel = (Boolean)attrs.getAttribute("label");
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
                    if (StyleConstants.getComponent(attrs) != null) {
                        System.out.println("TEST--------------------------");
                    }
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

    // endregion Get Record/Tier Start/End

    private int insertTier(int recordIndex, TierViewItem tierViewItem, int offset, AttributeSet recordAttrs, boolean partOfLargerBatch) throws BadLocationException {
        String tierName = tierViewItem.getTierName();
        Record record = session.getRecord(recordIndex);
        Tier<?> tier = record.getTier(tierName);
        if (tier == null) return offset;

        final int startOffset = offset;

        SimpleAttributeSet tierAttrs = getTierAttributes(tier, tierViewItem);
        if (recordAttrs != null) {
            tierAttrs.addAttributes(recordAttrs);
        }

        SimpleAttributeSet labelAttrs = getTierLabelAttributes(tier, tierViewItem);
        if (recordAttrs != null) {
            labelAttrs.addAttributes(recordAttrs);
        }
        appendBatchString(tierName + ": ", labelAttrs);
        offset += tierName.length() + 2;

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
                offset += content.length();

                if (i < words.size() - 1) {
                    appendBatchString(" ", tierAttrs);
                    offset++;
                }
            }
            if (syllabificationVisible) {
                appendBatchLineFeed(getTierAttributes(ipaTier, tierViewItem));
                offset++;
                String ipaTarget = ipaTier.getValue().toString(true);
                appendBatchString(ipaTarget, getIPATierAttributes(ipaTier));
                offset += ipaTarget.length();
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
                offset += content.length();

                if (i < words.size() - 1) {
                    appendBatchString(" ", tierAttrs);
                    offset++;
                }
            }
            if (syllabificationVisible) {
                appendBatchLineFeed(getTierAttributes(ipaTier, tierViewItem));
                offset++;
                String ipaActual = ipaTier.getValue().toString(true);
                appendBatchString(ipaActual, getIPATierAttributes(ipaTier));
                offset += ipaActual.length();
            }
        }
        else if (tierName.equals("Segment")) {
            MediaSegment segment = record.getMediaSegment();
            String start = MediaTimeFormatter.msToPaddedMinutesAndSeconds(segment.getStartValue());

            var segmentTimeAttrs = getSegmentTimeAttributes();
            segmentTimeAttrs.addAttributes(tierAttrs);
            var segmentDashAttrs = getSegmentDashAttributes();
            segmentDashAttrs.addAttributes(tierAttrs);

            appendBatchString("•", segmentDashAttrs);
            offset++;

            appendBatchString(start, segmentTimeAttrs);
            offset += start.length();

            appendBatchString("-", segmentDashAttrs);
            offset++;

            String end = MediaTimeFormatter.msToPaddedMinutesAndSeconds(segment.getEndValue());

            appendBatchString(end, segmentTimeAttrs);
            offset += end.length();

            appendBatchString("•", segmentDashAttrs);
            offset++;
        }
        else if (tierName.equals("Orthography")) {
            String tierContent = tier.toString();

            appendBatchString(tierContent, tierAttrs);

            offset += tierContent.length();
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
                        text = "[%" + comment.text() + "]";
                        attrs = getTierCommentAttributes();
                    }
                    else {
                        TierInternalMedia internalMedia = (TierInternalMedia) elem;
                        text = "•" + internalMedia.text() + "•";
                        attrs = getTierInternalMediaAttributes();
                    }

                    attrs.addAttributes(tierAttrs);

                    appendBatchString(text, attrs);
                    offset += text.length();

                    if (i < tierData.length() - 1) {
                        appendBatchString(" ", tierAttrs);
                        offset++;
                    }
                }
            }
        }

        if (alignmentVisible && tierName.equals(getAlignmentTierView().getTierName())) {
            appendBatchLineFeed(getTierAttributes(tier, tierViewItem));
            offset++;
            String alignment = record.getPhoneAlignment().toString();
            appendBatchString(alignment, getAlignmentAttributes(record.getPhoneAlignment()));
            offset += alignment.length();
        }

        appendBatchLineFeed(tierAttrs);
        offset++;

        if (!partOfLargerBatch) processBatchUpdates(startOffset);

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

        if (singleRecordView) {
            writeRecord(0, session.getRecord(singleRecordIndex), transcript, tierView);
        }
        else {
            int offset = 0;

            for (Transcript.Element elem : transcript) {
                if (elem.isRecord()) {
                    offset = writeRecord(offset, elem.asRecord(), transcript, tierView);
                }
                else if (elem.isComment()) {
                    offset = writeComment(offset, elem.asComment());
                }
                else {
                    offset = writeGem(offset, elem.asGem());
                }
            }
        }

        processBatchUpdates(0);
    }

    private int writeRecord(
        int offset,
        Record record,
        Transcript transcript,
        List<TierViewItem> tierView
    ) throws BadLocationException {
        int recordIndex = transcript.getRecordPosition(record);

        SimpleAttributeSet recordAttrs = getRecordAttributes(recordIndex);

        SimpleAttributeSet sepAttrs = getSeparatorAttributes(record, recordIndex);

        sepAttrs.addAttributes(recordAttrs);

        MediaSegment segment = record.getMediaSegment();
        StringBuilder segmentLabelTextBuilder = new StringBuilder();
        segmentLabelTextBuilder.append("•");
        segmentLabelTextBuilder.append(MediaTimeFormatter.timeToString(segment.getStartValue(), MediaTimeFormatStyle.PADDED_MINUTES_AND_SECONDS));
        segmentLabelTextBuilder.append("-");
        segmentLabelTextBuilder.append(MediaTimeFormatter.timeToString(segment.getEndValue(), MediaTimeFormatStyle.PADDED_MINUTES_AND_SECONDS));
        segmentLabelTextBuilder.append("•");

        String sepString = record.getSpeaker().toString() + " " + segmentLabelTextBuilder.toString();
        appendBatchString(sepString, sepAttrs);
        offset += sepString.length();
        appendBatchLineFeed(sepAttrs);
        offset++;

        for (var item : tierView) {
            if (!item.isVisible()) continue;

            offset = insertTier(recordIndex, item, offset, recordAttrs, true);
        }

        return offset;
    }

    private int writeComment(int offset, Comment comment) throws BadLocationException {

        SimpleAttributeSet commentAttrs = getCommentAttributes(comment);

        TierData tierData = comment.getValue();

        String labelText = comment.getType().getLabel() + ": ";
        appendBatchString(labelText, getCommentLabelAttributes(comment));
        offset += labelText.length();

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
                text = "[%" + userTierComment.text() + "]";
                attrs = getTierCommentAttributes();
            } else {
                // Internal media
                TierInternalMedia internalMedia = (TierInternalMedia) userTierElement;
                text = "•" + internalMedia.text() + "•";
                attrs = getTierInternalMediaAttributes();
            }

            attrs.addAttributes(commentAttrs);

            appendBatchString(text, attrs);
            offset += text.length();

            if (i < tierData.length() - 1) {
                appendBatchString(" ", attrs);
                offset++;
            }
        }

        appendBatchLineFeed(commentAttrs);
        offset++;

        return offset;
    }

    private int writeGem(int offset, Gem gem) throws BadLocationException {
        String text = gem.getLabel();

        SimpleAttributeSet gemAttrs = getGemAttributes(gem);

        String labelText = gem.getType().toString() + ": ";
        appendBatchString(labelText, getGemLabelAttributes(gem));
        offset += labelText.length();

        appendBatchString(text, gemAttrs);
        offset += text.length();

        appendBatchLineFeed(gemAttrs);
        offset++;

        return offset;
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

    public boolean getLabelsVisible() {
        return labelsVisible;
    }

    public void setLabelsVisible(boolean labelsVisible) {
        this.labelsVisible = labelsVisible;
        reload();
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
