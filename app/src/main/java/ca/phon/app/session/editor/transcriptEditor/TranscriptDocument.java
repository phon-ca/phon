package ca.phon.app.session.editor.transcriptEditor;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.PhoneMapDisplay;
import ca.phon.ui.ipa.SyllabificationDisplay;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;

public class TranscriptDocument extends DefaultStyledDocument {
    private final Session session;
    private final List<List<String>> tierOrder;
    private SetCaretCallback setCaretPos;
    private GetCaretCallback getCaretPos;
    private boolean targetSyllablesVisible = false;
    private boolean actualSyllablesVisible = false;
    private boolean alignmentVisible = false;

    public int getLabelMaxWidth() {
        return labelMaxWidth;
    }

    private int labelMaxWidth = -1;

    // region Get/Set Caret Pos

    public interface GetCaretCallback {
        public int op();
    }
    public interface SetCaretCallback {
        public void op(int pos);
    }
    public void setGetCaretPosCallback(GetCaretCallback lambda) {
        getCaretPos = lambda;
    }
    public void setSetCaretPosCallback(SetCaretCallback lambda) {
        setCaretPos = lambda;
    }

    // endregion Get/Set Caret Pos

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
            for (var recordTierOrder : tierOrder) {
                if (alignmentVisible) {
                    recordTierOrder.add("Alignment");
                }
                else {
                    recordTierOrder.remove("Alignment");
                }
            }
            reload();
        }
    }

    // endregion Visible Getters/Setters

    public TranscriptDocument(Session session) {
        super(new StyleContext());
        this.session = session;
        this.tierOrder = new ArrayList<>();
        initTierOrderMap();

        try {
            populate();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initTierOrderMap() {
        List<TierViewItem> tierViewList = session.getTierView();
        List<Record> records = session.getRecords().stream().toList();

        for (Record record : records) {
            List<String> recordTierOrder = new ArrayList<>();

            for (var tv : tierViewList) {
                if (tv.isVisible()) {
                    recordTierOrder.add(tv.getTierName());
                }
            }

            tierOrder.add(recordTierOrder);
        }
    }

    private SimpleAttributeSet getTierAttributes(Tier tier) {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute("tier", tier);
        StyleConstants.setFontFamily(retVal, FontPreferences.TIER_FONT);
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

    private SimpleAttributeSet getTierLabelAttributes(int recordIndex, String tierName) {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();
        JLabel tierLabel = new JLabel(tierName + ":");
        var labelFont = new Font(tierLabel.getFont().getFontName(), tierLabel.getFont().getStyle(), 12);
        tierLabel.setFont(labelFont);
        tierLabel.setAlignmentY(.8f);
        tierLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        EmptyBorder tierLabelPadding = new EmptyBorder(0,8,0,8);
        tierLabel.setBorder(tierLabelPadding);
        labelMaxWidth = Math.max(
            labelMaxWidth,
            tierLabel.getPreferredSize().width +
                tierLabelPadding.getBorderInsets().left +
                tierLabelPadding.getBorderInsets().right
        );
        tierLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                createTierLabelPopup(recordIndex, tierLabel, e);
            }

            // region unused

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }

            // endregion unused
        });
        StyleConstants.setComponent(retVal, tierLabel);
        return retVal;
    }

    private void createTierLabelPopup(int recordIndex, JLabel tierLabel, MouseEvent mouseEvent) {
        JPopupMenu menu = new JPopupMenu();

        var recordTierOrder = tierOrder.get(recordIndex);
        String tierName = tierLabel.getText().substring(0, tierLabel.getText().length()-1);
        int tierStartIndex = recordTierOrder.indexOf(tierName);

        JMenuItem hideTier = new JMenuItem("Hide tier");
        menu.add(hideTier);
        hideTier.addActionListener(e -> hideTier(recordIndex, tierName, tierStartIndex));

        // If it's not the top tier in a record
        if (tierStartIndex > 0) {
            JMenuItem moveUp = new JMenuItem("Move up");
            menu.add(moveUp);
            moveUp.addActionListener(e -> moveTierUp(recordIndex, tierName, tierStartIndex));
        }

        // If it's not the bottom tier in a record
        if (tierStartIndex < recordTierOrder.size() - 1) {
            JMenuItem moveDown = new JMenuItem("Move down");
            menu.add(moveDown);
            moveDown.addActionListener(e -> moveTierDown(recordIndex, tierName, tierStartIndex));
        }

        menu.show(tierLabel, mouseEvent.getX(), mouseEvent.getY());
    }

    // region Hide/Move Tiers

    private void hideTier (int recordIndex, String tierName, int tierStartIndex) {
        var recordTierOrder = tierOrder.get(recordIndex);

        int startCaretPos = getCaretPos.op();
        var elem = getCharacterElement(startCaretPos);
        Tier caretTier = (Tier)elem.getAttributes().getAttribute("tier");
        Integer caretRecordIndex = (Integer)elem.getAttributes().getAttribute("recordIndex");

        int caretTierOffset = -1;
        // If the caret has a valid tier and record index
        if (caretTier != null && caretRecordIndex != null) {
            String caretTierName = caretTier.getName();
            int caretTierIndex = recordTierOrder.indexOf(caretTierName);
            // If the caret is on a later line
            if (caretRecordIndex > recordIndex ||
                (caretRecordIndex == recordIndex && caretTierIndex > tierStartIndex)
            ) {
                caretTierOffset = startCaretPos - elem.getStartOffset();
            }
        }

        // Remove the tier
        recordTierOrder.remove(tierName);
        // Reload the contents of the editor
        reload();

        if (caretTierOffset > -1) {
            // Move the caret so that it has the same offset from the tiers new pos
            setCaretPos.op(getTierStart(caretTier) + caretTierOffset);
        }
        else {
            // Put the caret back where it was before the move
            setCaretPos.op(startCaretPos);
        }
    }

    private void moveTierUp (int recordIndex, String tierName, int tierStartIndex) {
        var recordTierOrder = tierOrder.get(recordIndex);

        int startCaretPos = getCaretPos.op();
        var elem = getCharacterElement(startCaretPos);
        Tier caretTier = (Tier)elem.getAttributes().getAttribute("tier");
        Integer caretRecordIndex = (Integer)elem.getAttributes().getAttribute("recordIndex");
        int caretTierOffset = -1;

        if (caretTier != null && caretRecordIndex != null && caretRecordIndex == recordIndex) {
            String caretTierName = caretTier.getName();
            if (caretTierName.equals(tierName) || caretTierName.equals(recordTierOrder.get(tierStartIndex - 1))) {
                caretTierOffset = startCaretPos - elem.getStartOffset();
            }
        }

        // Move the tier
        recordTierOrder.remove(tierName);
        recordTierOrder.add(tierStartIndex - 1, tierName);
        // Reload the contents of the editor
        reload();

        if (caretTierOffset > -1) {
            // Move the caret so that it has the same offset from the tiers new pos
            setCaretPos.op(getTierStart(caretTier) + caretTierOffset);
        }
        else {
            // Put the caret back where it was before the move
            setCaretPos.op(startCaretPos);
        }
    }

    private void moveTierDown (int recordIndex, String tierName, int tierStartIndex) {
        var recordTierOrder = tierOrder.get(recordIndex);

        int startCaretPos = getCaretPos.op();
        var elem = getCharacterElement(startCaretPos);
        Tier caretTier = (Tier)elem.getAttributes().getAttribute("tier");
        Integer caretRecordIndex = (Integer)elem.getAttributes().getAttribute("recordIndex");
        int caretTierOffset = -1;

        if (caretTier != null && caretRecordIndex != null && caretRecordIndex == recordIndex) {
            String caretTierName = caretTier.getName();
            if (caretTierName.equals(tierName) || caretTierName.equals(recordTierOrder.get(tierStartIndex + 1))) {
                caretTierOffset = startCaretPos - elem.getStartOffset();
            }
        }

        // Move the tier
        recordTierOrder.remove(tierName);
        recordTierOrder.add(tierStartIndex + 1, tierName);
        // Reload the contents of the editor
        reload();

        if (caretTierOffset > -1) {
            // Move the caret so that it has the same offset from the tiers new pos
            setCaretPos.op(getTierStart(caretTier) + caretTierOffset);
        }
        else {
            // Put the caret back where it was before the move
            setCaretPos.op(startCaretPos);
        }
    }

    // endregion Hide/Move Tiers

    private SimpleAttributeSet getSeparatorAttributes() {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();
        StyleConstants.setComponent(retVal, new JSeparator(JSeparator.HORIZONTAL));
        return retVal;
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

    private int getRecordStart(int recordIndex, String tierName) {
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

    private int getRecordStart(Tier tier) {
        Element root = getDefaultRootElement();

        int retVal = -1;

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

    private int getRecordEnd(int recordIndex, String tierName) {
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

    private int getRecordEnd(Tier tier) {
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

    private int getTierStart(int recordIndex, String tierName) {
        Element root = getDefaultRootElement();

        int retVal = -1;

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            var currentRecordIndex = elem.getAttributes().getAttribute("recordIndex");
            // If correct record index
            if (currentRecordIndex != null && ((int)currentRecordIndex) == recordIndex) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    var tier = innerElem.getAttributes().getAttribute("tier");
                    // If correct tier name
                    if (tier != null && ((Tier)tier).getName().equals(tierName)) {
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

    private int getTierStart(Tier tier) {
        Element root = getDefaultRootElement();

        int retVal = -1;

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

    private int getTierEnd(int recordIndex, String tierName) {
        Element root = getDefaultRootElement();

        int retVal = -1;

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            var currentRecordIndex = elem.getAttributes().getAttribute("recordIndex");
            // If correct record index
            if (currentRecordIndex != null && ((int)currentRecordIndex) == recordIndex) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    var tier = innerElem.getAttributes().getAttribute("tier");
                    // If correct tier name
                    if (tier != null && ((Tier)tier).getName().equals(tierName)) {
                        retVal = Math.max(retVal, innerElem.getEndOffset());
                    }
                }
            }
        }

        return retVal;
    }

    private int getTierEnd(Tier tier) {
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
                        return getTierEnd((int)currentRecordIndex, tier.getName());
                    }
                }
            }
        }

        return -1;
    }

    private void populate() throws BadLocationException {

        Transcript transcript = session.getTranscript();

        List<Record> records = session.getRecords().stream().toList();

        int len = 0;

        for (int i = 0; i < 2; i++) {
            int recordStart = len;
            Record record = records.get(i);
            int recordElementIndex = transcript.getRecordElementIndex(record);

            for (String tierName : tierOrder.get(i)) {
                Tier tier = record.getTier(tierName);

                if (tier != null || tierName.equals("Alignment")) {
                    insertString(len++, " ", getTierLabelAttributes(i, tierName));

                    if (tierName.equals("IPA Target") && targetSyllablesVisible) {
                        insertString(len++, " ", getIPATierAttributes(tier));
                    }
                    else if (tierName.equals("IPA Actual") && actualSyllablesVisible) {
                        insertString(len++, " ", getIPATierAttributes(tier));
                    }
                    else if (tierName.equals("Alignment")) {
                        insertString(len++, " ", getAlignmentAttributes(record.getPhoneAlignment()));
                    }
                    else {
                        String tierContent = tier.toString();
                        if (tierContent.strip().equals("")) tierContent = "FILLER";

                        SimpleAttributeSet tierAttrs = getTierAttributes(tier);
                        insertString(len, tierContent, tierAttrs);

                        len += tierContent.length();
                    }
                }

                insertString(len++, "\n ", null);
            }

            if (i < records.size() - 1) {
                insertString(len, "-\n", getSeparatorAttributes());
                len += 2;
            }

            SimpleAttributeSet recordAttrs = new SimpleAttributeSet();
            recordAttrs.addAttribute("recordIndex", i);
            recordAttrs.addAttribute("recordElementIndex", recordElementIndex);
            setParagraphAttributes(recordStart, len - recordStart, recordAttrs, false);
        }
    }

    @Override
    protected void insert(int offset, ElementSpec[] data) throws BadLocationException {
        super.insert(offset, data);
    }

    @Override
    public void removeElement(Element elem) {
        super.removeElement(elem);
    }
}
