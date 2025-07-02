package ca.phon.app.session.editor.view.transcript.extensions;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.syllabificationAlignment.ScTypeEdit;
import ca.phon.app.session.editor.view.syllabificationAlignment.ToggleDiphthongEdit;
import ca.phon.app.session.editor.view.transcript.*;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.ipa.Phone;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.Tier;
import ca.phon.session.Transcriber;
import ca.phon.session.position.TranscriptElementLocation;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.SyllabificationDisplay;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.undo.UndoableEditSupport;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;

/**
 * View component factory for syllabification components in the transcript editor.
 */
public class SyllabificationComponentFactory implements ComponentFactory {

    public static String Y_AXIS_ALIGNMENT = "yAxisAlignment";
    public static float Y_AXIS_ALIGNMENT_VALUE = 0.75f;

    final TranscriptEditor editor;

    final Session session;

    final EditorEventManager eventManager;

    final UndoableEditSupport undoSupport;

    final Transcriber transcriber;

    private AttributeSet attrs;

    private JPanel previousComponent;

    public SyllabificationComponentFactory(TranscriptEditor editor) {
        this.editor = editor;
        this.session = editor.getSession();
        this.eventManager = editor.getEventManager();
        this.undoSupport = editor.getUndoSupport();
        this.transcriber = editor.getDataModel().getTranscriber();
    }

    @Override
    public JComponent createComponent(AttributeSet attrs) {
        Tier<IPATranscript> tier = (Tier<IPATranscript>) TranscriptStyleConstants.getTier(attrs);
        Tier<IPATranscript> parentTier = (Tier<IPATranscript>) TranscriptStyleConstants.getParentTier(attrs);
        this.attrs = attrs;

        int breakWidth = -1;
        if(attrs.getAttribute("TranscriptViewFactory.tierWidth") != null) {
            breakWidth = (int)attrs.getAttribute("TranscriptViewFactory.tierWidth");
        }
        final BreakableFlowLayout layout = new BreakableFlowLayout();
        layout.setBreakWidth(breakWidth);
        final JPanel retVal = new JPanel(layout);
        retVal.setBackground(UIManager.getColor("text"));

        int currentIndex = 0;

        final Record record = TranscriptStyleConstants.getRecord(attrs);
        final int transcriptIndex = record != null ? editor.getSession().getRecordElementIndex(record) : -1;
        final TranscriptElementLocation location = editor.getTranscriptEditorCaret().getCurrentLocation();

        // create font from attributes
        Font font = TranscriptStyleConstants.getFont(attrs);
        if(FontPreferences.getFontSizeDelta() != 0) {
            font = font.deriveFont(font.getSize() + FontPreferences.getFontSizeDelta());
        }

        // clone transcript
        IPATranscript origTranscript = tier.getValueForTranscriber(transcriber).orElse(tier.getValue());
        if(origTranscript == null) {
            origTranscript = new IPATranscript();
        }
        final IPATranscript clonedTranscript = (new IPATranscriptBuilder()).append(origTranscript.toString(true)).toIPATranscript();
        for(IPATranscript word:clonedTranscript.words()) {
            final SyllabificationDisplay display = new SyllabificationDisplay();
            display.setFont(font);
            display.setTranscript(word);
            display.setFocusTraversalKeysEnabled(false);
            retVal.add(display);

            // setup tab, shift+tab, up/down key actions
            final InputMap inputMap = display.getInputMap(JComponent.WHEN_FOCUSED);
            final InputMap ancestorInputMap = display.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            final ActionMap actionMap = display.getActionMap();

            final KeyStroke tabKey = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
            inputMap.put(tabKey, "focusNextTier");
            ancestorInputMap.put(tabKey, "focusNextTier");
            actionMap.put("focusNextTier", PhonUIAction.eventConsumer(this::focusNextTier, retVal));

            final KeyStroke shiftTabKey = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK);
            inputMap.put(shiftTabKey, "focusPrevTier");
            ancestorInputMap.put(shiftTabKey, "focusPrevTier");
            actionMap.put("focusPrevTier", PhonUIAction.eventConsumer(this::focusPrevTier, retVal));

            final KeyStroke upKey = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
            inputMap.put(upKey, "focusPrevTier");

            final KeyStroke downKey = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
            inputMap.put(downKey, "focusNextTier");

            display.addPropertyChangeListener("focusNext", (e) -> {
                final int idx = Arrays.asList(retVal.getComponents()).indexOf(display);
                if(idx < retVal.getComponentCount()-1) {
                    if(retVal.getComponent(idx+1) instanceof SyllabificationDisplay nextDisplay) {
                        nextDisplay.requestFocus();
                        nextDisplay.setFocusedPhone(0);
                    }
                }
            });
            display.addPropertyChangeListener("focusPrev", (e) -> {
                final int idx = Arrays.asList(retVal.getComponents()).indexOf(display);
                if(idx > 0) {
                    if(retVal.getComponent(idx-1) instanceof SyllabificationDisplay prevDisplay) {
                        prevDisplay.requestFocus();
                        prevDisplay.setFocusedPhone(prevDisplay.getDisplayedPhones().length()-1);
                    }
                }
            });

            if(this.session != null && this.eventManager != null && this.undoSupport != null) {
                final int phoneIndex = currentIndex;
                display.addPropertyChangeListener(SyllabificationDisplay.SYLLABIFICATION_PROP_ID, (e) -> {
                    final SyllabificationDisplay.SyllabificationChangeData data = (SyllabificationDisplay.SyllabificationChangeData) e.getNewValue();
                    final IPATranscript transcript = parentTier.getValueForTranscriber(transcriber).orElse(parentTier.getValue());
                    final ScTypeEdit edit = new ScTypeEdit(this.session, this.eventManager,
                            transcriptIndex, parentTier.getName(),
                            transcript, phoneIndex + data.position(), data.scType());
                    edit.setSource(display);
                    this.undoSupport.postEdit(edit);
                });

                display.addPropertyChangeListener(SyllabificationDisplay.HIATUS_CHANGE_PROP_ID, (e) -> {
                    final SyllabificationDisplay.HiatusChangeData data = (SyllabificationDisplay.HiatusChangeData) e.getNewValue();
                    final int pIdx = phoneIndex + data.position1();
                    final int pIdx2 = phoneIndex + data.position2();
                    final IPATranscript transcript = parentTier.getValueForTranscriber(transcriber).orElse(parentTier.getValue());
                    final ToggleDiphthongEdit diphthongEdit1 = new ToggleDiphthongEdit(this.session, this.eventManager, transcriptIndex, parentTier.getName(), transcript, pIdx);
                    diphthongEdit1.setSource(display);
                    final ToggleDiphthongEdit diphthongEdit2 = new ToggleDiphthongEdit(this.session, this.eventManager, transcriptIndex, parentTier.getName(), transcript, pIdx2);
                    diphthongEdit2.setSource(display);
                    this.undoSupport.beginUpdate();
                    this.undoSupport.postEdit(diphthongEdit1);
                    this.undoSupport.postEdit(diphthongEdit2);
                    this.undoSupport.endUpdate();
                });
            }
            currentIndex += word.length() + 1;
        }

        previousComponent = retVal;

        final Dimension prefSize = retVal.getPreferredSize();
        if(prefSize.getHeight() > 0) {
            int baseline = retVal.getComponent(0).getBaseline(prefSize.width, prefSize.height);
            retVal.setAlignmentY((float)baseline / (float)prefSize.height);
        }

        return retVal;
    }

    private IPATranscript buildTranscriptFromPanel(JPanel panel) {
        final IPATranscriptBuilder builder = new IPATranscriptBuilder();
        for(int i = 0; i < panel.getComponentCount(); i++) {
            if(panel.getComponent(i) instanceof SyllabificationDisplay display) {
                if (i > 0)
                    builder.appendWordBoundary();
                builder.append(display.getTranscript());
            }
        }
        return builder.toIPATranscript();
    }

    private void focusNextTier(PhonActionEvent<JPanel> pae) {
        final IPATranscript transcript = buildTranscriptFromPanel(pae.getData());

        // get focused element
        int offset = -1;
        for(int i = 0; i < pae.getData().getComponentCount(); i++) {
            if(pae.getData().getComponent(i) instanceof SyllabificationDisplay display) {
                if(display.hasFocus()) {
                    IPAElement focusedElement = display.getDisplayedPhones().elementAt(display.getFocusedPhone());
                    offset = transcript.indexOf(focusedElement);
                }
            }
        }

        if(offset < 0) {
            offset = 0;
        }
        final Record record = TranscriptStyleConstants.getRecord(this.attrs);
        final Tier<?> tier = TranscriptStyleConstants.getTier(this.attrs);
        final int recordIdx = editor.getSession().getRecordIndex(record);
        final TranscriptDocument.StartEnd syllabificationRange =
                editor.getTranscriptDocument().getTierContentStartEnd(recordIdx, tier.getName());
        if(syllabificationRange.valid()) {
            editor.requestFocus();
            editor.offsetInNextTierOrElement(syllabificationRange.start()+offset, transcript.stringIndexOfElement(offset));
        }
    }

    private void focusPrevTier(PhonActionEvent<JPanel> pae) {
        final IPATranscript transcript = buildTranscriptFromPanel(pae.getData());

        // get focused element
        int offset = -1;
        for(int i = 0; i < pae.getData().getComponentCount(); i++) {
            if(pae.getData().getComponent(i) instanceof SyllabificationDisplay display) {
                if(display.hasFocus()) {
                    IPAElement focusedElement = display.getDisplayedPhones().elementAt(display.getFocusedPhone());
                    offset = transcript.indexOf(focusedElement);
                }
            }
        }

        if(offset < 0) {
            offset = 0;
        }
        final Record record = TranscriptStyleConstants.getRecord(this.attrs);
        final Tier<?> tier = TranscriptStyleConstants.getTier(this.attrs);
        final int recordIdx = editor.getSession().getRecordIndex(record);
        final TranscriptDocument.StartEnd syllabificationRange =
                editor.getTranscriptDocument().getTierContentStartEnd(recordIdx, tier.getName());
        if(syllabificationRange.valid()) {
            editor.requestFocus();
            editor.offsetInPrevTierOrElement(syllabificationRange.start()+offset, transcript.stringIndexOfElement(offset));
        }
    }

    @Override
    public void requestFocusStart() {
        if(previousComponent != null && previousComponent.getComponentCount() > 0) {
            final SyllabificationDisplay display = (SyllabificationDisplay) previousComponent.getComponent(0);
            if(display != null) {
                display.requestFocus();
                display.setFocusedPhone(0);
            }
        }
    }

    @Override
    public void requestFocusEnd() {
        if(previousComponent != null && previousComponent.getComponentCount() > 0) {
            final SyllabificationDisplay display = (SyllabificationDisplay) previousComponent.getComponent(previousComponent.getComponentCount()-1);
            if(display != null) {
                display.requestFocus();
                display.setFocusedPhone(display.getDisplayedPhones().length()-1);
            }
        }
    }

    @Override
    public void requestFocusAtOffset(int offset) {
        if(previousComponent != null) {
            final IPATranscript transcript = buildTranscriptFromPanel(previousComponent);
            if(offset >= transcript.length()) {
                requestFocusEnd();
                return;
            } else if(offset == 0) {
                requestFocusStart();
                return;
            }

            IPAElement selectedElement = null;
            int ipaOffset = transcript.ipaIndexOf(offset);
            while(!((selectedElement = transcript.elementAt(ipaOffset)) instanceof Phone)) {
                ipaOffset++;
            }
            for(int i = 0; i < previousComponent.getComponentCount(); i++) {
                if(previousComponent.getComponent(i) instanceof SyllabificationDisplay display) {
                    if(display.getDisplayedPhones().indexOf(selectedElement) >= 0) {
                        display.requestFocus();
                        display.setFocusedPhone(display.getDisplayedPhones().indexOf(selectedElement));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public JComponent getComponent() {
        return previousComponent;
    }

}
