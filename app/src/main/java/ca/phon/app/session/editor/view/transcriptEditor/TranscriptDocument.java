package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.log.LogUtil;
import ca.phon.formatter.MediaTimeFormatter;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.ui.DropDownIcon;
import ca.phon.ui.EmptyIcon;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.PhoneMapDisplay;
import ca.phon.ui.ipa.SyllabificationDisplay;
import org.jdesktop.swingx.HorizontalLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.function.Function;

public class TranscriptDocument extends DefaultStyledDocument {
    private Session session;
    private boolean targetSyllablesVisible = false;
    private boolean actualSyllablesVisible = false;
    private boolean alignmentVisible = false;
    private Function<String, JComponent> tierLabelFactory = this::createLabel;
    private final SessionFactory sessionFactory;

    public TranscriptDocument() {
        super(new StyleContext());
        sessionFactory = SessionFactory.newFactory();
        setDocumentFilter(new TranscriptDocumentFilter());
    }

    private JComponent createLabel(String tierName) {
        JLabel tierLabel = new JLabel(tierName + ":");
        var labelFont = new Font(tierLabel.getFont().getFontName(), tierLabel.getFont().getStyle(), 12);
        tierLabel.setFont(labelFont);
        tierLabel.setAlignmentY(.8f);
        tierLabel.setMaximumSize(new Dimension(150, tierLabel.getPreferredSize().height));
        tierLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        EmptyBorder tierLabelPadding = new EmptyBorder(0,8,0,8);
        tierLabel.setBorder(tierLabelPadding);

        return tierLabel;
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

    private SimpleAttributeSet getTierAttributes(Tier tier, TierViewItem item) {
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

        StyleConstants.setForeground(retVal, Color.blue);

        return retVal;
    }

    private SimpleAttributeSet getIPAPauseAttributes(Tier<IPATranscript> tier) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute("tier", tier);

        StyleConstants.setForeground(retVal, Color.red);

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

    private SimpleAttributeSet getTierLabelAttributes(TierViewItem tierViewItem) {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();

        JLabel tierLabel = (JLabel) tierLabelFactory.apply(tierViewItem.getTierName());
        tierLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        DropDownIcon dropDownIcon = new DropDownIcon(new EmptyIcon(0, 16), 0, SwingConstants.BOTTOM);
        tierLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        tierLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        tierLabel.setIcon(dropDownIcon);
        StyleConstants.setComponent(retVal, tierLabel);

        retVal.addAttribute("locked", tierViewItem.isTierLocked());

        retVal.addAttribute("label", true);

        return retVal;
    }

    private SimpleAttributeSet getSeparatorAttributes(Record record, int recordIndex) {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();
        JPanel separatorPanel = new JPanel(new HorizontalLayout());
        separatorPanel.setBorder(new EmptyBorder(0,8,0,8));
        separatorPanel.setBackground(Color.WHITE);

        DropDownIcon dropDownIcon = new DropDownIcon(new EmptyIcon(0, 16), 0, SwingConstants.BOTTOM);

        JLabel recordNumberLabel = new JLabel("#" + (recordIndex+1));
        recordNumberLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        recordNumberLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        recordNumberLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        recordNumberLabel.setIcon(dropDownIcon);
        var labelFont = new Font(
            recordNumberLabel.getFont().getFontName(),
            recordNumberLabel.getFont().getStyle(),
            12
        );
        recordNumberLabel.setFont(labelFont);
        recordNumberLabel.setAlignmentY(.8f);
        separatorPanel.add(recordNumberLabel);
        recordNumberLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JPopupMenu menu = new JPopupMenu();
                JCheckBoxMenuItem excludeMenuItem = new JCheckBoxMenuItem("Exclude");
                menu.add(excludeMenuItem);
                excludeMenuItem.setState(record.isExcludeFromSearches());
                excludeMenuItem.addActionListener(evt -> {
                    record.setExcludeFromSearches(excludeMenuItem.getState());
                });
                menu.add(new JMenuItem("Move"));
                menu.show(recordNumberLabel, e.getX(), e.getY());
            }
        });

        JLabel speakerNameLabel = new JLabel(record.getSpeaker().getName());
        speakerNameLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        speakerNameLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        speakerNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        speakerNameLabel.setIcon(dropDownIcon);
        speakerNameLabel.setFont(labelFont);
        speakerNameLabel.setAlignmentY(.8f);
        separatorPanel.add(speakerNameLabel);
        speakerNameLabel.setBorder(new EmptyBorder(0,8,0,8));
        speakerNameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JPopupMenu menu = new JPopupMenu();
                ButtonGroup buttonGroup = new ButtonGroup();

                var unknownMenuItem = new JRadioButtonMenuItem(Participant.UNKNOWN.getName());
                buttonGroup.add(unknownMenuItem);
                if (Participant.UNKNOWN.equals(record.getSpeaker())) {
                    buttonGroup.setSelected(unknownMenuItem.getModel(), true);
                }
                menu.add(unknownMenuItem);

                for (Participant participant : session.getParticipants()) {
                    var menuItem = new JRadioButtonMenuItem(participant.getName());
                    buttonGroup.add(menuItem);
                    if (participant.equals(record.getSpeaker())) {
                        buttonGroup.setSelected(menuItem.getModel(), true);
                    }
                    menu.add(menuItem);
                }

                menu.show(recordNumberLabel, e.getX(), e.getY());
            }
        });

        var sep = new JSeparator(JSeparator.HORIZONTAL);
        sep.setPreferredSize(new Dimension(10000, 1));
        separatorPanel.add(sep);
        StyleConstants.setComponent(retVal, separatorPanel);
        return retVal;
    }

    private SimpleAttributeSet getCommentAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        return retVal;
    }

    private SimpleAttributeSet getCommentLabelAttributes(String commentType) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        JLabel commentLabel = (JLabel) createLabel(commentType);
        commentLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        DropDownIcon dropDownIcon = new DropDownIcon(new EmptyIcon(0, 16), 0, SwingConstants.BOTTOM);
        commentLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        commentLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        commentLabel.setIcon(dropDownIcon);
        commentLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JPopupMenu menu = new JPopupMenu();

                menu.add(new JMenuItem("Comment Menu"));

                menu.show(commentLabel, e.getX(), e.getY());
            }
        });
        StyleConstants.setComponent(retVal, commentLabel);

        retVal.addAttribute("label", true);

        return retVal;
    }

    private SimpleAttributeSet getGemAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        return retVal;
    }

    private SimpleAttributeSet getGemLabelAttributes(String gemType) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        JLabel gemLabel = (JLabel) createLabel(gemType);

        gemLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        DropDownIcon dropDownIcon = new DropDownIcon(new EmptyIcon(0, 16), 0, SwingConstants.BOTTOM);
        gemLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        gemLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gemLabel.setIcon(dropDownIcon);

        gemLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JPopupMenu menu = new JPopupMenu();

                menu.add(new JMenuItem("Gem Menu"));

                menu.show(gemLabel, e.getX(), e.getY());
            }
        });
        StyleConstants.setComponent(retVal, gemLabel);

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
                    int tierEndOffset = getTierEnd(i, tierName) + 1;
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
        int recordCount = session.getRecordCount();
        for (int i = 0; i < recordCount; i++) {
            try {
                for (String tierName : hiddenTiers) {
                    int labelLength = tierName.length() + 2;
                    int tierStartOffset = getTierStart(i, tierName) - labelLength;
                    int tierEndOffset = getTierEnd(i, tierName) + 1;
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

    public Tier getTier(int offset) {
        AttributeSet attributes = getCharacterElement(offset).getAttributes();
        var tier = attributes.getAttribute("tier");
        if (tier == null) {
            return null;
        }
        return ((Tier)tier);
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

    public int getRecordStart(Tier tier) {
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
                    Tier currentTier = (Tier)attrs.getAttribute("tier");
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

    public int getRecordEnd(Tier tier) {
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
                    Tier tier = (Tier) attrs.getAttribute("tier");
                    Boolean isLabel = (Boolean)attrs.getAttribute("label");
                    // If correct tier name
                    if (isLabel == null && tier != null && ((Tier)tier).getName().equals(tierName)) {
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

    public int getTierStart(Tier tier) {
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
                    Tier currentTier = (Tier)attrs.getAttribute("tier");
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
                    Tier tier = (Tier)attrs.getAttribute("tier");
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

    public int getTierEnd(Tier tier) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            Integer currentRecordIndex = (Integer) elem.getAttributes().getAttribute("recordIndex");
            // If correct record index
            if (currentRecordIndex != null) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    Tier currentTier = (Tier)innerElem.getAttributes().getAttribute("tier");
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
        Tier tier = record.getTier(tierName);
        if (tier == null) return offset;

        SimpleAttributeSet tierAttrs = getTierAttributes(tier, tierViewItem);

        SimpleAttributeSet labelAttrs = getTierLabelAttributes(tierViewItem);
        labelAttrs.addAttribute("tier", tier);

        insertString(offset, tierName + ": ", labelAttrs);
        offset += tierName.length() + 2;

        if (tierName.equals("IPA Target") && targetSyllablesVisible) {
            String ipaTarget = ((Tier<IPATranscript>)tier).getValue().toString(true);
            insertString(offset++, ipaTarget, getIPATierAttributes(tier));
        }
        else if (tierName.equals("IPA Actual")) {
            if (actualSyllablesVisible) {
                String ipaActual = ((Tier<IPATranscript>)tier).getValue().toString(true);
                insertString(offset++, ipaActual, getIPATierAttributes(tier));
            }
            else {
                var ipa = ((Tier<IPATranscript>)tier).getValue();
                for (var word : ipa.words()) {
                    SimpleAttributeSet attrs;
                    if (word.matches("\\P")) {
                        // Pause
                        attrs = getIPAPauseAttributes(((Tier<IPATranscript>)tier));
                    }
                    else {
                        // Word
                        attrs = getIPAWordAttributes(((Tier<IPATranscript>)tier));
                    }
                    attrs.addAttributes(tierAttrs);
                    String content = word.toString();
                    insertString(offset, content, attrs);
                    offset += content.length();
                }

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

            insertString(offset, start, segmentTimeAttrs);
            offset += start.length();

            var segmentDashAttrs = getSegmentDashAttributes();
            segmentDashAttrs.addAttributes(tierAttrs);

            insertString(offset++, "-", segmentDashAttrs);

            String end = MediaTimeFormatter.msToPaddedMinutesAndSeconds(segment.getEndValue());

            insertString(offset, end, segmentTimeAttrs);
            offset += end.length();
        }
        else {
            String tierContent = tier.toString();

            insertString(offset, tierContent, tierAttrs);

            offset += tierContent.length();
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

                String text = comment.getValue().toString();

                insertString(len++, " ", getCommentLabelAttributes(comment.getType().getLabel()));

                insertString(len, text, getCommentAttributes());
                len += text.length();

                insertString(len++, "\n", null);
            }
            else {
                Gem gem = elem.asGem();

                String text = gem.getLabel();

                insertString(len++, " ", getGemLabelAttributes(gem.getType().toString()));

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

    public Function<String, JComponent> getTierLabelFactory() {
        return tierLabelFactory;
    }

    public void setTierLabelFactory(Function<String, JComponent> tierLabelFactory) {
        this.tierLabelFactory = tierLabelFactory;
        reload();
    }

    // endregion Getters and Setters

    private class TranscriptDocumentFilter extends DocumentFilter {
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (attrs != null) {
                Tier tier = (Tier)attrs.getAttribute("tier");
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
