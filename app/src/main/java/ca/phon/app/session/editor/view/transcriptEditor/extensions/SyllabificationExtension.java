package ca.phon.app.session.editor.view.transcriptEditor.extensions;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.view.transcriptEditor.*;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.syllable.SyllabificationInfo;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.ui.action.PhonUIAction;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

/**
 * An extension that provides syllabification support to the {@link TranscriptEditor}
 * */
public class SyllabificationExtension implements TranscriptEditorExtension {
    private TranscriptEditor editor;
    private TranscriptDocument doc;

    /* Document property stuff */

    public final static String SYLLABIFICATION_IS_VISIBLE = "isSyllabificationVisible";
    public final static boolean SYLLABIFICATION_IS_VISIBLE_DEFAULT = false;
    public final static String SYLLABIFICATION_IS_COMPONENT = "isSyllabificationComponent";
    public final static boolean SYLLABIFICATION_IS_COMPONENT_DEFAULT = false;

    /* State */

    private boolean syllabificationEditMode = false;


    /**
     * Constructor
     * */
    public SyllabificationExtension() {
        super();
    }

    @Override
    public void install(TranscriptEditor editor) {
        this.editor = editor;
        this.doc = editor.getTranscriptDocument();

        PhonUIAction<Void> syllabificationEditModeAct = PhonUIAction.runnable(() -> {
            String tierName = editor.getCurrentSessionLocation().getLabel();
            if (!tierName.equals(SystemTierType.TargetSyllables.getName()) && !tierName.equals(SystemTierType.ActualSyllables.getName())) return;
            setSyllabificationEditMode(!syllabificationEditMode);
        });

        doc.addInsertionHook(new DefaultInsertionHook() {
            @Override
            public List<DefaultStyledDocument.ElementSpec> endTier(MutableAttributeSet attrs) {
                List<DefaultStyledDocument.ElementSpec> retVal = new ArrayList<>();

                if (!isSyllabificationVisible()) return retVal;

                Tier<?> tier = (Tier<?>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                if (tier != null && tier.getDeclaredType().equals(IPATranscript.class)) {
                    if (tier.getName().equals("IPA Target") || tier.getName().equals("IPA Actual")) {
                        Tier<IPATranscript> ipaTier = (Tier<IPATranscript>) tier;

                        // Add a newline at the end of the regular tier content
                        retVal.addAll(doc.getBatchEndLineFeed(attrs));
                        // Create a dummy tier for the syllabification
                        IPATranscript ipaTarget = ipaTier.getValue();
                        Tier<IPATranscript> syllableTier = doc.getSessionFactory().createTier(
                            tier.getName().equals("IPA Target") ? SystemTierType.TargetSyllables.getName() : SystemTierType.ActualSyllables.getName(),
                            IPATranscript.class
                        );
                        syllableTier.setValue(ipaTarget);
                        // Set up the tier attributes for the dummy tier
                        attrs = new SimpleAttributeSet(attrs);
                        attrs.addAttributes(doc.getTierAttributes(syllableTier));
                        attrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_ENTER_ACTION, syllabificationEditModeAct);
                        // Set up the attributes for its label
                        SimpleAttributeSet syllabificationLabelAttrs = doc.getTierLabelAttributes(syllableTier);
                        Record record = (Record) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
                        if (record != null) {
                            syllabificationLabelAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD, record);
                        }
                        // Get the string for the label
                        String syllabificationLabelText = doc.formatLabelText("Syllabification");
                        // Add the label
                        retVal.add(doc.getBatchString(syllabificationLabelText + ": ", syllabificationLabelAttrs));
                        // Add component factory if needed
                        if (isSyllabificationComponent()) {
                            attrs.addAttributes(doc.getSyllabificationAttributes());
                            // Append the content string
                            retVal.add(doc.getBatchString(syllableTier.getValue().toString(true), attrs));
                        }
                        else {
                            // Append the formatted content
                            retVal.addAll(getFormattedSyllabification(syllableTier.getValue(), attrs));
                        }
                    }
                }

                return retVal;
            }
        });


        doc.addDocumentPropertyChangeListener(SYLLABIFICATION_IS_VISIBLE, evt -> doc.reload());
        doc.addDocumentPropertyChangeListener(SYLLABIFICATION_IS_COMPONENT, evt -> {
            if (isSyllabificationVisible()) {
                doc.reload();
            }
        });


        InputMap inputMap = editor.getInputMap();
        ActionMap actionMap = editor.getActionMap();

        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        inputMap.put(esc, "pressedEsc");
        PhonUIAction<Void> escAct = PhonUIAction.runnable(() -> {
            if (syllabificationEditMode) setSyllabificationEditMode(false);
        });
        actionMap.put("pressedEsc", escAct);

        editor.addCaretListener(e -> {
            if (!syllabificationEditMode) return;
            SessionLocation location = editor.charPosToSessionLocation(e.getDot());
            String tierName = location.getLabel();
            if (!SystemTierType.TargetSyllables.getName().equals(tierName) && !SystemTierType.ActualSyllables.getName().equals(tierName)) {
                setSyllabificationEditMode(false);
            }
        });

        doc.addNotEditableAttribute(TranscriptStyleConstants.ATTR_KEY_SYLLABIFICATION);
    }

    // region Getters and Setters

    public boolean isSyllabificationVisible() {
        return (boolean) doc.getDocumentPropertyOrDefault(
            SYLLABIFICATION_IS_VISIBLE,
            SYLLABIFICATION_IS_VISIBLE_DEFAULT
        );
    }

    public boolean isSyllabificationComponent() {
        return (boolean) doc.getDocumentPropertyOrDefault(
            SYLLABIFICATION_IS_COMPONENT,
            SYLLABIFICATION_IS_COMPONENT_DEFAULT
        );
    }

    private List<DefaultStyledDocument.ElementSpec> getFormattedSyllabification(IPATranscript ipaTranscript, AttributeSet additionalAttrs) {
        List<DefaultStyledDocument.ElementSpec> retVal = new ArrayList<>();

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
            attrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_TRAVERSABLE_SYLLABIFICATION, true);
            attrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);
            if (p.getScType().equals(SyllableConstituentType.UNKNOWN)) {
                StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.IPA_PAUSE));
            }
            retVal.add(doc.getBatchString(p.toString(), attrs));
            final SyllabificationInfo sInfo = p.getExtension(SyllabificationInfo.class);
            if (hiddenConstituent.contains(sInfo.getConstituentType())) continue;
            retVal.add(doc.getBatchString(":", attrs));
            attrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_TRAVERSABLE_SYLLABIFICATION);
            attrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE);
            if(sInfo.getConstituentType() == SyllableConstituentType.NUCLEUS && sInfo.isDiphthongMember()) {
                StyleConstants.setForeground(attrs, Color.RED);
                retVal.add(doc.getBatchString("D", attrs));
            }
            else {
                StyleConstants.setForeground(attrs, sInfo.getConstituentType().getColor());
                retVal.add(doc.getBatchString(String.valueOf(sInfo.getConstituentType().getIdChar()), attrs));
            }
        }
        attrs.removeAttribute(StyleConstants.Foreground);

        return retVal;
    }

    /**
     * Sets whether the editor is in "syllabification edit" mode,
     * and modifies the navigation and document filters accordingly
     *
     * @param enabled whether syllabification edit mode will be enabled
     * */
    private void setSyllabificationEditMode(boolean enabled) {
        syllabificationEditMode = enabled;

        if (enabled) {
            editor.setNavigationFilter(new SyllabificationEditNavigationFilter(editor));
            editor.addNotTraversableAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_TRAVERSABLE_SYLLABIFICATION);
            doc.setDocumentFilter(new SyllabificationEditDocumentFilter(doc));
            doc.removeNotEditableAttribute(TranscriptStyleConstants.ATTR_KEY_SYLLABIFICATION);
        }
        else {
            editor.setNavigationFilter(new TranscriptNavigationFilter(editor));
            editor.removeNotTraversableAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_TRAVERSABLE_SYLLABIFICATION);
            doc.setDocumentFilter(new TranscriptDocument.TranscriptDocumentFilter(doc));
            doc.addNotEditableAttribute(TranscriptStyleConstants.ATTR_KEY_SYLLABIFICATION);
        }

        editor.setCaretPosition(editor.getNextValidIndex(editor.getCaretPosition()-1, false));
    }

    // endregion Getters and Setters

    /**
     * The {@link NavigationFilter} that gets used in syllabification edit mode
     * */
    private static class SyllabificationEditNavigationFilter extends NavigationFilter {
        private final TranscriptEditor editor;

        private SyllabificationEditNavigationFilter(TranscriptEditor editor) {
            this.editor = editor;
        }

        @Override
        public void setDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias) {

            TranscriptDocument doc = editor.getTranscriptDocument();
            if (doc.getLength() == 0) {
                fb.setDot(dot, bias);
            }

            Element elem = doc.getCharacterElement(dot);
            AttributeSet attrs = elem.getAttributes();
            if (attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_TRAVERSABLE) != null) return;

            AttributeSet prevAttrs = doc.getCharacterElement(fb.getCaret().getDot()).getAttributes();
            AttributeSet nextAttrs = doc.getCharacterElement(dot).getAttributes();

            String prevElemType = (String) prevAttrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
            String nextElemType = (String) nextAttrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
            Tier<?> nextTier = (Tier<?>) nextAttrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);

            if (prevElemType != null) {
                try {
                    switch (prevElemType) {
                        case TranscriptStyleConstants.ATTR_KEY_RECORD -> {
                            Tier<?> prevTier = (Tier<?>) prevAttrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                            if (prevTier == null || prevTier.getDeclaredType().equals(PhoneAlignment.class)) break;
                            if (nextElemType != null && nextElemType.equals(TranscriptStyleConstants.ATTR_KEY_RECORD)) {
                                if (nextTier != null && nextTier == prevTier) break;
                            }
                            int start = doc.getTierStart(prevTier);
                            int end = doc.getTierEnd(prevTier) - 1;
                            String newValue = doc.getText(start, end - start);
                            editor.setInternalEdit(true);
                            editor.changeTierData((Record)prevAttrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD), prevTier, newValue);
                        }
                    }
                }
                catch (BadLocationException e) {
                    LogUtil.severe(e);
                }
            }

            if (doc.getLength() == dot) return;

            int prevCaretPos = editor.getCaretPosition();

            if (nextTier != null && nextAttrs.getAttribute("syllabification") != null) {
                int tierEnd = doc.getTierEnd(nextTier)-1;
                fb.setDot(dot, Position.Bias.Forward);
                if (dot != tierEnd) {
                    fb.moveDot(editor.getCaretPosition() + 1, Position.Bias.Forward);
                }
            }
            else {
                fb.setDot(dot, bias);
            }

            TranscriptEditor.SessionLocationChangeData sessionLocationChangeData = new TranscriptEditor.SessionLocationChangeData(
                editor.charPosToSessionLocation(prevCaretPos),
                editor.charPosToSessionLocation(dot)
            );

            SwingUtilities.invokeLater(() -> {
                final EditorEvent<TranscriptEditor.SessionLocationChangeData> e = new EditorEvent<>(
                    TranscriptEditor.sessionLocationChange,
                    editor,
                    sessionLocationChangeData
                );
                editor.getEventManager().queueEvent(e);
            });
        }
        @Override
        public void moveDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias) {}
    }

    /**
     * The {@link DocumentFilter} that gets used in syllabification edit mode
     * */
    private static class SyllabificationEditDocumentFilter extends DocumentFilter {
        private final TranscriptDocument doc;
        private final Set<Character> syllabificationChars;

        private SyllabificationEditDocumentFilter(TranscriptDocument doc) {
            this.doc = doc;

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
            attrs.addAttributes(doc.getCharacterElement(offset).getAttributes());

            // Labels and stuff
            if (doc.containsNotEditableAttribute(attrs)) return;

            // Locked tiers
            Tier<?> tier = (Tier<?>)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
            if (tier != null) {
                String tierName = tier.getName();
                var tierViewItem = doc
                    .getSession()
                    .getTierView()
                    .stream()
                    .filter(item -> item.getTierName().equals(tierName))
                    .findFirst();
                if (tierViewItem.isPresent() && tierViewItem.get().isTierLocked()) {
                    return;
                }

                // Syllabification tiers
                if (attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_SYLLABIFICATION) != null) {
                    if (text == null || text.isEmpty()) return;
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

                                int start = doc.getTierStart(tier);
                                int end = doc.getTierEnd(tier);

                                for (int i = start; i < end; i++) {
                                    var charAttrs = doc.getCharacterElement(i).getAttributes();
                                    IPAElement charPhone = (IPAElement) charAttrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_PHONE);
                                    if (!doc.containsNotEditableAttribute(charAttrs) && charPhone != null && (charPhone.equals(phone) || charPhone.equals(otherNucleus))) {
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

                                int start = doc.getTierStart(tier);
                                int end = doc.getTierEnd(tier);

                                for (int i = start; i < end; i++) {
                                    var charAttrs = doc.getCharacterElement(i).getAttributes();
                                    IPAElement charPhone = (IPAElement) charAttrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_PHONE);
                                    if (!doc.containsNotEditableAttribute(charAttrs) && charPhone != null && (charPhone.equals(phone) || charPhone.equals(otherNucleus))) {
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
                    else return;
                }
            }
            super.replace(fb, offset, length, text, attrs);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {

            if (!doc.isBypassDocumentFilter()) {
                var attrs = doc.getCharacterElement(offset).getAttributes();
                if (doc.containsNotEditableAttribute(attrs)) return;
                if (attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_SYLLABIFICATION) != null) return;
            }

            doc.setBypassDocumentFilter(false);
            super.remove(fb, offset, length);
        }
    }
}
