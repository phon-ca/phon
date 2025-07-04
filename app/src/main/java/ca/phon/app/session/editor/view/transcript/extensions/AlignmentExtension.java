package ca.phon.app.session.editor.view.transcript.extensions;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.transcript.*;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.position.TranscriptElementLocation;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.PhoneMapDisplay;
import ca.phon.ui.ipa.SyllabificationDisplay;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.PrefHelper;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

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
    public final static boolean ALIGNMENT_IS_COMPONENT_DEFAULT = true;

    public final static String ALIGNMENT_PARENT = "alignmentParent";
    public final static TierViewItem ALIGNMENT_PARENT_DEFAULT = null;

    @Override
    public void install(TranscriptEditor editor) {
        this.editor = editor;
        doc = editor.getTranscriptDocument();

        doc.addInsertionHook(new DefaultInsertionHook() {
            @Override
            public List<DefaultStyledDocument.ElementSpec> endTier(MutableAttributeSet attrs) {
                TranscriptBatchBuilder builder = new TranscriptBatchBuilder(doc);
                if(!isAlignmentVisible() || !doc.getSingleRecordView()) return builder.getBatch();
                buildAlignmentBatch(builder, attrs);
                return builder.getBatch();
            }
        });

        doc.addDocumentPropertyChangeListener(ALIGNMENT_IS_VISIBLE, this::alignmentVisiblePropertyChangeHandler);
        doc.addDocumentPropertyChangeListener(ALIGNMENT_IS_COMPONENT, this::alignmentComponentPropertyChangeHandler);

        editor.getEventManager().registerActionForEvent(EditorEventType.TierViewChanged, (event) -> {
            doc.putDocumentProperty(ALIGNMENT_PARENT, calculateAlignmentParent());
        }, EditorEventManager.RunOn.AWTEventDispatchThread);
        editor.getEventManager().registerActionForEvent(EditorEventType.TierChange, this::onTierDataChanged, EditorEventManager.RunOn.AWTEventDispatchThread);

        editor.getTranscriptEditorCaret().addCaretHook(new TranscriptEditorCaretHookAdapter() {
            @Override
            public void afterSetDot(int dot, int moveTo) {
                final TranscriptElementLocation previousLocation = editor.charPosToSessionLocation(dot);
                final TranscriptElementLocation currentLocation = editor.charPosToSessionLocation(moveTo);
                if(currentLocation.valid() && !previousLocation.equals(currentLocation)) {
                    onTranscriptLocationChanged(previousLocation, currentLocation);
                }
            }
        });

        PrefHelper.getUserPreferences().addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                if(FontPreferences.FONT_SIZE_DELTA_PROP.equals(evt.getKey())) {
                    updateAlignmentDisplayFont(SystemTierType.PhoneAlignment.getName());
                }
            }
        });
    }

    private void updateAlignmentDisplayFont(String tierName) {
        final int currentRecordIndex = editor.getTranscriptDocument().getSingleRecordIndex();
        final TranscriptDocument.StartEnd tierRange = editor.getTranscriptDocument().getTierContentStartEnd(currentRecordIndex, tierName);
        if(!tierRange.valid()) return;
        final AttributeSet attrs = editor.getTranscriptDocument().getCharacterElement(tierRange.start()).getAttributes();
        final ComponentFactory componentFactory = TranscriptStyleConstants.getComponentFactory(attrs);
        if(componentFactory instanceof AlignmentComponentFactory alignmentComponentFactory) {
            final JComponent parent = alignmentComponentFactory.getComponent();
            Font font = TranscriptStyleConstants.getFont(attrs);
            font = font.deriveFont(font.getSize() + FontPreferences.getFontSizeDelta());
            for(int i = 0; i < parent.getComponentCount(); i++) {
                if(parent.getComponent(i) instanceof PhoneMapDisplay display) {
                    display.setFont(font);
                }
            }
            parent.revalidate();
            final Dimension prefSize = parent.getPreferredSize();
            if(prefSize.getHeight() > 0) {
                int baseline = parent.getComponent(0).getBaseline(prefSize.width, prefSize.height);
                parent.setAlignmentY((float)baseline / (float)prefSize.height);
            }
        }
    }

    private void alignmentVisiblePropertyChangeHandler(PropertyChangeEvent evt) {
        if (isAlignmentVisible()) {
            doc.putDocumentProperty(ALIGNMENT_PARENT, calculateAlignmentParent());
        }
        else {
            doc.putDocumentProperty(ALIGNMENT_PARENT, ALIGNMENT_PARENT_DEFAULT);
        }
        int transcriptElementIndex = -1;
        if(editor.isSingleRecordView()) {
            transcriptElementIndex = editor.getSession().getTranscript().getRecordElementIndex(
                    editor.getTranscriptDocument().getSingleRecordIndex()
            );
        } else {
            final TranscriptElementLocation currentLocation = editor.getCurrentSessionLocation();
            if (currentLocation == null || !currentLocation.valid() || currentLocation.transcriptElementIndex() < 0)
                return;
            final Transcript.Element element = editor.getSession().getTranscript().getElementAt(currentLocation.transcriptElementIndex());
            if (!element.isRecord()) return;
            transcriptElementIndex = currentLocation.transcriptElementIndex();
        }
        if(transcriptElementIndex >= 0) {
            if((boolean)evt.getNewValue()) {
                // insert syllabification tiers
                addAlignmentTiersForRecord(transcriptElementIndex);
            } else {
                removeAlignmentTiersForRecord(transcriptElementIndex);
            }
        }
    }

    private void alignmentComponentPropertyChangeHandler(PropertyChangeEvent evt) {
        if (isAlignmentVisible()) {
            doc.reload();
        }
    }

    /**
     * Gets an attribute set containing a reference to the alignment component factory.
     * Adding the contents of this attribute set to the attributes of an alignment tier will
     * cause it to appear as the {@link ca.phon.ui.ipa.PhoneMapDisplay} component instead of text
     *
     * @return an attribute set containing a reference to the alignment component factory
     */
    public SimpleAttributeSet getAlignmentAttributes() {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_COMPONENT_FACTORY, new AlignmentComponentFactory(editor));
        return retVal;
    }

    public void buildAlignmentBatch(TranscriptBatchBuilder batchBuilder, AttributeSet attrs) {
        Tier<?> tier = (Tier<?>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
        String alignmentParent = getAlignmentParent();

        if (tier != null && alignmentParent != null && tier.getName().equals(alignmentParent)) {
            Record record = TranscriptStyleConstants.getRecord(attrs);
            final SimpleAttributeSet tierAttrs = new SimpleAttributeSet();
            TranscriptStyleConstants.setElementType(tierAttrs, TranscriptStyleConstants.ELEMENT_TYPE_RECORD);
            TranscriptStyleConstants.setRecord(tierAttrs, record);
            TranscriptStyleConstants.setParentTier(tierAttrs, tier);
            TranscriptStyleConstants.setTier(tierAttrs, record.getPhoneAlignmentTier());
            TranscriptStyleConstants.setClickHandler(tierAttrs, AlignmentExtension.this::alignmentTierLabelClickHandler);
            batchBuilder.appendTierLabel(editor.getSession(), record, record.getPhoneAlignmentTier(), record.getPhoneAlignmentTier().getName(), null, doc.isChatTierNamesShown(), tierAttrs);
            batchBuilder.appendAll(getFormattedAlignment(record, record.getPhoneAlignmentTier(), editor.getDataModel().getTranscriber(), tierAttrs));
            final SimpleAttributeSet finalAttrs = new SimpleAttributeSet(batchBuilder.getTrailingAttributes());
            TranscriptStyleConstants.setTier(finalAttrs, record.getPhoneAlignmentTier());
            TranscriptStyleConstants.setNotEditable(finalAttrs, true);
            TranscriptStyleConstants.setNotTraversable(finalAttrs, record.getPhoneAlignment().getFullAlignment().getAlignmentLength() != 0);
            TranscriptStyleConstants.setComponentFactory(finalAttrs, null);
            TranscriptStyleConstants.setComponentFactory(finalAttrs, new ComponentFactory() {
                @Override
                public JComponent createComponent(AttributeSet attrs) {
                    final JPanel retVal = new JPanel();
                    retVal.setPreferredSize(new Dimension(0, 0));
                    return retVal;
                }

                @Override
                public JComponent getComponent() {
                    return null;
                }

                @Override
                public void requestFocusStart() {

                }

                @Override
                public void requestFocusEnd() {

                }

                @Override
                public void requestFocusAtOffset(int offset) {

                }
            });
            batchBuilder.appendEOL(finalAttrs);
        }
    }

    private void alignmentTierLabelClickHandler(MouseEvent e, AttributeSet attrs) {
        // show menu for syllabification tier
        JPopupMenu popup = new JPopupMenu();
        final MenuBuilder builder = new MenuBuilder(popup);

        final Record record = TranscriptStyleConstants.getRecord(attrs);
        final PhonUIAction<Record> resetAlignmentAct = PhonUIAction.eventConsumer(this::resetAlignment, record);
        resetAlignmentAct.putValue(PhonUIAction.NAME, "Reset alignment");
        resetAlignmentAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Reset alignment");
        builder.addItem(".", resetAlignmentAct);

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    private void resetAlignment(PhonActionEvent<Record> pae) {
        final Record record = pae.getData();
        final Tier<IPATranscript> ipaTier = record.getIPATargetTier();
        final Tier<IPATranscript> ipaActualTier = record.getIPAActualTier();
        final Tier<PhoneAlignment> alignmentTier = record.getPhoneAlignmentTier();

        final PhoneAlignment phoneAlignment = PhoneAlignment.fromTiers(ipaTier, ipaActualTier, editor.getDataModel().getTranscriber());

        final TierEdit<PhoneAlignment> edit = new TierEdit<>(editor.getSession(), editor.getEventManager(), editor.getDataModel().getTranscriber(),
                record, alignmentTier, phoneAlignment);
        edit.setValueAdjusting(false);
        editor.getUndoSupport().postEdit(edit);
    }

    /**
     * Runs when tier data gets changed.
     * If the alignment tier is the one that gets changed, update it in the doc.
     *
     * @param editorEvent the event that changed the tier data
     * */
    private void onTierDataChanged(EditorEvent<EditorEventType.TierChangeData> editorEvent) {
        if(editorEvent.data().valueAdjusting() || editorEvent.data().record() == null) return;
        Tier<?> tier = editorEvent.data().record().getPhoneAlignmentTier();
        if (!tier.getDeclaredType().equals(PhoneAlignment.class) || !isAlignmentVisible()) return;

        final int recordIndex = editor.getSession().getTranscript().getRecordIndex(editorEvent.data().record());
        final TranscriptDocument.StartEnd alignmentTierContentRange = doc.getTierContentStartEnd(recordIndex, tier.getName());
        if(!alignmentTierContentRange.valid()) return;

        // update the existing alignment components
        final AttributeSet attrs = doc.getCharacterElement(alignmentTierContentRange.start()).getAttributes();
        final ComponentFactory componentFactory = TranscriptStyleConstants.getComponentFactory(attrs);
        if (componentFactory instanceof AlignmentComponentFactory) {
            final JPanel component = (JPanel) componentFactory.getComponent();
            if(component != null) {
                int i = 0;
                final PhoneAlignment alignment = tier.isBlind()
                        ? editor.getDataModel().getTranscriber() == Transcriber.VALIDATOR
                            ? (PhoneAlignment) tier.getValue()
                            : (PhoneAlignment) tier.getBlindTranscription(editor.getDataModel().getTranscriber().getUsername())
                        : (PhoneAlignment) tier.getValue();
                for(; i < component.getComponentCount() && i < alignment.getAlignments().size(); i++) {
                    final Component comp = component.getComponent(i);
                    if(comp instanceof PhoneMapDisplay) {
                        final PhoneMap pm = alignment.getAlignments().get(i);
                        final PhoneMap cloneAlignment =
                                PhoneMap.fromString(pm.getTargetRep(), pm.getActualRep(), pm.toString());
                        final PhoneMapDisplay phoneMapDisplay = (PhoneMapDisplay) comp;
//                        if(!phoneMapDisplay.isFocusOwner()) {
                            phoneMapDisplay.setPhoneMapForWord(0, cloneAlignment);
//                        }
                    }
                }
                // add new components if necessary
                for(; i < alignment.getAlignments().size(); i++) {
                    final PhoneMapDisplay phoneMapDisplay = new PhoneMapDisplay();
                    final PhoneMap pm = alignment.getAlignments().get(i);
                    final PhoneMap cloneAlignment =
                            PhoneMap.fromString(pm.getTargetRep(), pm.getActualRep(), pm.toString());
                    phoneMapDisplay.setPhoneMapForWord(0, cloneAlignment);
                    component.add(phoneMapDisplay);
                }

                // remove extra components
                for(; i < component.getComponentCount(); i++) {
                    component.remove(i);
                }
            }
        } else {
            // batch update
            try {
                doc.setBypassDocumentFilter(true);
                doc.remove(alignmentTierContentRange.start(), alignmentTierContentRange.length());
                doc.processBatchUpdates(alignmentTierContentRange.start(),
                        getFormattedAlignment(editorEvent.data().record(), (Tier<PhoneAlignment>) tier, editor.getDataModel().getTranscriber(), attrs));
            } catch (BadLocationException e) {
                LogUtil.severe(e);
            } finally {
                doc.setBypassDocumentFilter(false);
            }
        }
    }

    /**
     * Gets a list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} that contains the data for the
     * properly formatted alignment tier content
     *
     * @param record a reference to the record containing the alignment tier
     * @param alignmentTier the alignment tier to format
     * @param attrs the attributes to apply to the tier
     * @return the list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} data
     */
    public List<DefaultStyledDocument.ElementSpec> getFormattedAlignment(Record record, Tier<PhoneAlignment> alignmentTier, Transcriber transcriber, AttributeSet attrs) {
        final TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder(doc);
        // Get the alignment tier
        TranscriptStyleContext transcriptStyleContext = doc.getTranscriptStyleContext();

        // Set up the tier attributes for the dummy tier
        var tierAttrs = new SimpleAttributeSet(attrs);
        tierAttrs.addAttributes(transcriptStyleContext.getTierAttributes(alignmentTier));
        tierAttrs.addAttributes(transcriptStyleContext.getRecordAttributes(record));
        TranscriptStyleConstants.setNotEditable(tierAttrs, true);

        // Get the string version of the alignment
        // Add component factory if needed
        final PhoneAlignment alignment = alignmentTier.getValueForTranscriber(transcriber).orElse(new PhoneAlignment());
        if (isAlignmentComponent() && alignment.getFullAlignment().getAlignmentLength() > 0) {
            tierAttrs.addAttributes(getAlignmentAttributes());
        }
        batchBuilder.appendTierContent(record, alignmentTier, editor.getDataModel().getTranscriber(), tierAttrs);

        return batchBuilder.getBatch();
    }

    private void addAlignmentTiersForRecord(int transcriptElementIndex) {
        final String alignmentParent = getAlignmentParent();
        if (alignmentParent == null) return;

        final int paraEleIdx = editor.getTranscriptDocument().findParagraphElementIndexForTier(transcriptElementIndex, alignmentParent);
        if (paraEleIdx >= 0) {
            final Element paraEle = editor.getTranscriptDocument().getDefaultRootElement().getElement(paraEleIdx);
            final MutableAttributeSet attrs = new SimpleAttributeSet(paraEle.getElement(paraEle.getElementCount() - 1).getAttributes());
            final TranscriptBatchBuilder builder = new TranscriptBatchBuilder(editor.getTranscriptDocument());
            buildAlignmentBatch(builder, attrs);
            try {
                editor.getTranscriptDocument().processBatchUpdates(paraEle.getEndOffset(), builder.getBatch());
            } catch (BadLocationException e) {
                LogUtil.warning(e);
            }
        }
    }

    private void removeAlignmentTiersForRecord(int transcriptElementIndex) {
        final int paraEleIdx = editor.getTranscriptDocument().findParagraphElementIndexForTier(transcriptElementIndex, SystemTierType.PhoneAlignment.getName());
        if (paraEleIdx >= 0) {
            final Element paraEle = editor.getTranscriptDocument().getDefaultRootElement().getElement(paraEleIdx);
            editor.getTranscriptDocument().setBypassDocumentFilter(true);
            editor.getTranscriptEditorCaret().freeze();
            try {
                editor.getTranscriptDocument().remove(paraEle.getStartOffset(), paraEle.getEndOffset() - paraEle.getStartOffset());
            } catch (BadLocationException e) {
                LogUtil.warning(e);
            } finally {
                editor.getTranscriptDocument().setBypassDocumentFilter(false);
                editor.getTranscriptEditorCaret().unfreeze();
            }
        }
    }

    public void onTranscriptLocationChanged(TranscriptElementLocation oldLocation, TranscriptElementLocation newLocation) {
        // handle caret movements into syllabifier tiers
        if (newLocation.tier().equals(SystemTierType.PhoneAlignment.getName())) {
            final TranscriptDocument.StartEnd tierStartEnd = doc.getTierContentStartEnd(
                    editor.getSession().getTranscript().getRecordIndex(newLocation.transcriptElementIndex()), newLocation.tier());
            if (tierStartEnd.valid()) {
                final AttributeSet attrs = doc.getCharacterElement(tierStartEnd.start()).getAttributes();
                final ComponentFactory componentFactory = TranscriptStyleConstants.getComponentFactory(attrs);
                if (componentFactory instanceof AlignmentComponentFactory) {
                    componentFactory.requestFocusAtOffset(newLocation.charPosition());
                }
            }
        }
    }

    /**
     * Calculates which tier the alignment tier line should be parented to
     *
     * @return the {@link TierViewItem} associated with the calculated parent tier
     * */
    public String calculateAlignmentParent() {
        List<TierViewItem> visibleTierView = editor.getSession().getTierView().stream().filter(TierViewItem::isVisible).toList();

        var retVal = visibleTierView.stream().filter(item -> item.getTierName().equals(SystemTierType.IPAActual.getName())).findFirst();
        if (retVal.isPresent()) {
            if(isSyllabificationVisible()) {
                return SystemTierType.ActualSyllables.getName();
            }
            return retVal.get().getTierName();
        }

        retVal = visibleTierView.stream().filter(item -> item.getTierName().equals(SystemTierType.IPATarget.getName())).findFirst();
        if (retVal.isPresent()) {
            if(isSyllabificationVisible()) {
                return SystemTierType.TargetSyllables.getName();
            }
            return retVal.get().getTierName();
        }
        return visibleTierView.size() > 0 ? visibleTierView.get(visibleTierView.size()-1).getTierName() : null;
    }

    // region Getters and Setters

    public boolean isSyllabificationVisible() {
        return (boolean) doc.getDocumentPropertyOrDefault(SyllabificationExtension.SYLLABIFICATION_IS_VISIBLE, SyllabificationExtension.SYLLABIFICATION_IS_VISIBLE_DEFAULT);
    }

    public boolean isAlignmentVisible() {
        return (boolean) doc.getDocumentPropertyOrDefault(ALIGNMENT_IS_VISIBLE, ALIGNMENT_IS_VISIBLE_DEFAULT);
    }

    public boolean isAlignmentComponent() {
        return (boolean) doc.getDocumentPropertyOrDefault(ALIGNMENT_IS_COMPONENT, ALIGNMENT_IS_COMPONENT_DEFAULT);
    }

    public String getAlignmentParent() {
        return (String) doc.getDocumentPropertyOrDefault(ALIGNMENT_PARENT, ALIGNMENT_PARENT_DEFAULT);
    }

    // endregion Getters and Setters
}
