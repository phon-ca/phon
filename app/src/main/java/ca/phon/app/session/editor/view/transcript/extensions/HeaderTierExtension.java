package ca.phon.app.session.editor.view.transcript.extensions;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.undo.MediaLocationEdit;
import ca.phon.app.session.editor.undo.SessionDateEdit;
import ca.phon.app.session.editor.view.transcript.*;
import ca.phon.extensions.UnvalidatedValue;
import ca.phon.formatter.Formatter;
import ca.phon.media.MediaLocator;
import ca.phon.session.*;
import ca.phon.session.tierdata.TierData;
import ca.phon.ui.CalloutWindow;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.text.DatePicker;
import ca.phon.util.Language;
import ca.phon.util.LanguageEntry;
import ca.phon.util.LanguageParser;
import org.apache.derby.impl.sql.catalog.DD_Version;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXMonthView;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An extension that provides header tier support to the {@link TranscriptEditor}
 * */
public class HeaderTierExtension extends DefaultInsertionHook implements TranscriptEditorExtension {
    private TranscriptEditor editor;
    private TranscriptDocument doc;
    private Session session;

    /* Document property stuff */

    public static final String HEADERS_VISIBLE = "isHeadersVisible";
    public static final boolean DEFAULT_HEADERS_VISIBLE = true;

    /* State */

    private final Map<String, Tier<?>> headerTierMap = new HashMap<>();

    private final Map<Tier<?>, Object> errorUnderlineHighlights = new HashMap<>();


    @Override
    public void install(TranscriptEditor editor) {
        this.editor = editor;
        this.doc = editor.getTranscriptDocument();
        this.session = editor.getSession();

        headerTierMap.put("date", doc.getSessionFactory().createTier("Date", LocalDate.class));
        headerTierMap.put("tiers", doc.getSessionFactory().createTier("Tiers", TierData.class));
        headerTierMap.put("participants", doc.getSessionFactory().createTier("Participants", TierData.class));
        headerTierMap.put("languages", doc.getSessionFactory().createTier("Languages", TranscriptDocument.Languages.class));
        headerTierMap.put("media", doc.getSessionFactory().createTier("Media", TierData.class));

        doc.addInsertionHook(this);
        doc.addDocumentPropertyChangeListener(HEADERS_VISIBLE, evt -> doc.reload());

        editor.getEventManager().registerActionForEvent(EditorEventType.TierViewChanged, this::updateTiersHeader, EditorEventManager.RunOn.AWTEventDispatchThread);
        editor.getEventManager().registerActionForEvent(EditorEventType.SessionDateChanged, this::updateDateHeader, EditorEventManager.RunOn.AWTEventDispatchThread);
        editor.getEventManager().registerActionForEvent(TranscriptEditor.transcriptLocationChanged, this::onSessionLocationChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
        editor.getEventManager().registerActionForEvent(EditorEventType.SessionMediaChanged, this::onSessionMediaChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
    }

    @Override
    public List<DefaultStyledDocument.ElementSpec> startSession() {
//                List<DefaultStyledDocument.ElementSpec> retVal = new ArrayList<>();
        TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder(doc.getTranscriptStyleContext(), doc.getInsertionHooks());

        if (isHeadersVisible()) {
            AttributeSet newLineAttrs;

            TranscriptStyleContext transcriptStyleContext = doc.getTranscriptStyleContext();

            // Add date line if present
            if (session.getDate() != null) {
                appendDateHeader(batchBuilder);
                batchBuilder.appendEOL();
            }

            // Add media line if present
            var sessionMedia = session.getMediaLocation();
            if (sessionMedia != null) {
                appendMediaHeader(batchBuilder);
                batchBuilder.appendEOL();
            }

            // Add languages line if present
            var sessionLanguages = session.getLanguages();
            if ((sessionLanguages != null && !sessionLanguages.isEmpty()) || isForceShowLanguageHeader()) {
                Tier<TranscriptDocument.Languages> languagesTier = (Tier<TranscriptDocument.Languages>) headerTierMap.get("languages");
                languagesTier.setFormatter(new Formatter<>() {
                    @Override
                    public String format(TranscriptDocument.Languages obj) {
                        return obj
                                .languageList()
                                .stream()
                                .map(Language::toString)
                                .collect(Collectors.joining(" "));
                    }

                    @Override
                    public TranscriptDocument.Languages parse(String text) throws ParseException {
                        List<Language> languageList = new ArrayList<>();

                        String[] languageStrings = text.split(" ");
                        for (String languageString : languageStrings) {
                            LanguageEntry languageEntry = LanguageParser.getInstance().getEntryById(languageString);
                            if (languageEntry == null) throw new ParseException(text, text.indexOf(languageString));

                            languageList.add(Language.parseLanguage(languageString));
                        }

                        return new TranscriptDocument.Languages(languageList);
                    }
                });
                languagesTier.setValue(new TranscriptDocument.Languages(sessionLanguages));
                batchBuilder.appendGeneric("Languages", languagesTier, null);
                batchBuilder.appendEOL();
//                        retVal.addAll(doc.getGeneric("Languages", languagesTier, null));
//                        newLineAttrs = transcriptStyleContext.getTrailingAttributes(retVal);
//                        retVal.addAll(TranscriptBatchBuilder.getBatchEndLineFeed(newLineAttrs, null));
            }

            // Add Tiers header
            appendTiersHeader(batchBuilder);
            batchBuilder.appendEOL();
//                    retVal.addAll(appendTiersHeader());
//                    newLineAttrs = transcriptStyleContext.getTrailingAttributes(retVal);
//                    retVal.addAll(TranscriptBatchBuilder.getBatchEndLineFeed(newLineAttrs, null));

            // Add Participants header
            Tier<TierData> participantsTier = (Tier<TierData>) headerTierMap.get("participants");
            Participants participants = session.getParticipants();
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
            final SimpleAttributeSet attrs = new SimpleAttributeSet();
            attrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);
            batchBuilder.appendGeneric("Participants", participantsTier, attrs);
            batchBuilder.appendEOL();

//                    retVal.addAll(doc.getGeneric("Participants", participantsTier, transcriptStyleContext.getParticipantsHeaderAttributes()));
//                    newLineAttrs = transcriptStyleContext.getTrailingAttributes(retVal);
//                    retVal.addAll(TranscriptBatchBuilder.getBatchEndLineFeed(newLineAttrs, null));
        }

        return batchBuilder.getBatch();
    }

    private boolean isHeadersVisible() {
        return (boolean) doc.getDocumentPropertyOrDefault(HEADERS_VISIBLE, DEFAULT_HEADERS_VISIBLE);
    }

    /**
     * Gets a list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} that contains the data for
     * the tiers header
     *
     * @return the list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} data
     * */
    public void appendTiersHeader(TranscriptBatchBuilder batchBuilder) {
        Tier<TierData> tiersTier = (Tier<TierData>) headerTierMap.get("tiers");

        int start = doc.getGenericContentStart(tiersTier);
        int end = doc.getGenericEnd(tiersTier);

        List<TierViewItem> visibleTierView = doc.getSession()
            .getTierView()
            .stream()
            .filter(item -> item.isVisible())
            .toList();
        StringJoiner joiner = new StringJoiner(", ");
        for (TierViewItem item : visibleTierView) {
            joiner.add(item.getTierName());
//                boolean isIPATier = doc.getSession()
//                    .getTiers()
//                    .stream()
//                    .filter(td -> td.getName().equals(item.getTierName()))
//                    .anyMatch(td -> td.getDeclaredType().equals(IPATranscript.class));
//                if (isSyllabificationVisible() && isIPATier) {
//                    joiner.add(item.getTierName() + " Syllabification");
//                }
//                if (alignmentVisible && alignmentParent == item) {
//                    joiner.add("Alignment");
//                }
        }
        tiersTier.setText(joiner.toString());
        final SimpleAttributeSet attrs = new SimpleAttributeSet();
        attrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);
        batchBuilder.appendGeneric("Tiers", tiersTier, attrs);
    }

    /**
     * Updates the tiers header when there is a change to the tier view
     *
     * @param event the event that caused the change to the tier view
     * */
    public void updateTiersHeader(EditorEvent<EditorEventType.TierViewChangedData> event) {
        try {
            Tier<?> tiersHeaderTier = headerTierMap.get("tiers");
            int start = doc.getGenericStart(tiersHeaderTier);
            int end = doc.getGenericEnd(tiersHeaderTier);

            if (start > -1 && end > -1) {
                doc.setBypassDocumentFilter(true);
                doc.remove(start, end - start);
            }

            TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder(doc.getTranscriptStyleContext(), doc.getInsertionHooks());
            appendTiersHeader(batchBuilder);
            batchBuilder.appendEOL();
            doc.processBatchUpdates(start > -1 ? start : 0, batchBuilder.getBatch());
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    private void appendMediaHeader(TranscriptBatchBuilder batchBuilder) {
        final Tier<TierData> mediaTier = (Tier<TierData>) headerTierMap.get("media");
        mediaTier.setText(session.getMediaLocation());

        final SimpleAttributeSet attrs = new SimpleAttributeSet(doc.getTranscriptStyleContext().getStyle(TranscriptStyleContext.DEFAULT_STYLE));
        TranscriptStyleConstants.setElementType(attrs, TranscriptStyleConstants.ELEMENT_TYPE_GENERIC);
        TranscriptStyleConstants.setGenericTier(attrs, mediaTier);

        // start paragraph
        batchBuilder.appendBatchEndStart(batchBuilder.getTrailingAttributes(), attrs);

        // label
        final SimpleAttributeSet labelAttrs = new SimpleAttributeSet(attrs);
        labelAttrs.addAttributes(doc.getTranscriptStyleContext().getLabelAttributes());
        TranscriptStyleConstants.setClickHandler(labelAttrs, (e, tier) -> {
            showMediaTierMenu(e, mediaTier);
        });
        String labelText = batchBuilder.formatLabelText("Media");
        TranscriptStyleConstants.setUnderlineOnHover(labelAttrs, true);
        batchBuilder.appendBatchString(labelText, labelAttrs);
        TranscriptStyleConstants.setUnderlineOnHover(labelAttrs, false);
        batchBuilder.appendBatchString(": ", labelAttrs);

        // value
        batchBuilder.appendBatchString(mediaTier.toString(), attrs);
    }

    /**
     * Gets a list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} that contains the data for
     * the date header
     *
     * @return the list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} data
     * */
    private void appendDateHeader(TranscriptBatchBuilder batchBuilder) {
        Tier<LocalDate> dateTier = (Tier<LocalDate>) headerTierMap.get("date");
        dateTier.setValue(editor.getSession().getDate());

        final SimpleAttributeSet attrs = new SimpleAttributeSet(doc.getTranscriptStyleContext().getStyle(TranscriptStyleContext.DEFAULT_STYLE));
        TranscriptStyleConstants.setElementType(attrs, TranscriptStyleConstants.ELEMENT_TYPE_GENERIC);
        TranscriptStyleConstants.setGenericTier(attrs, dateTier);

        // start paragraph
        batchBuilder.appendBatchEndStart(batchBuilder.getTrailingAttributes(), attrs);

        // label
        final SimpleAttributeSet labelAttrs = new SimpleAttributeSet(attrs);
        labelAttrs.addAttributes(doc.getTranscriptStyleContext().getLabelAttributes());
        TranscriptStyleConstants.setClickHandler(labelAttrs, (e, tier) -> {
            showDateTierMenu(e, dateTier);
        });
        String labelText = batchBuilder.formatLabelText("Date");
        TranscriptStyleConstants.setUnderlineOnHover(labelAttrs, true);
        batchBuilder.appendBatchString(labelText, labelAttrs);
        TranscriptStyleConstants.setUnderlineOnHover(labelAttrs, false);
        batchBuilder.appendBatchString(": ", labelAttrs);

        // value
        final PhonUIAction showDatePickerAct = PhonUIAction.runnable(this::showDatePicker);
        TranscriptStyleConstants.setEnterAction(attrs, showDatePickerAct);
        batchBuilder.appendBatchString(dateTier.toString(), attrs);
    }

    /**
     * Show date picker Callout for date header
     *
     */
    private void showDatePicker() {
        final Tier<LocalDate> dateTier = (Tier<LocalDate>) headerTierMap.get("date");
        final JXMonthView monthView = new JXMonthView();
        monthView.setTraversable(true);
        monthView.setFocusable(false);

        JComboBox<Integer> yearSelectionBox = new JComboBox<>(new YearComboBoxModel());
        if(dateTier.hasValue()) {
            final Date date = Date.from(dateTier.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            monthView.setFirstDisplayedDay(date);
            monthView.setSelectionDate(date);
            yearSelectionBox.setSelectedItem(dateTier.getValue().getYear());
        } else {
            final Date date = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
            monthView.setFirstDisplayedDay(date);
            yearSelectionBox.setSelectedItem(LocalDate.now().getYear());
        }

        yearSelectionBox.addItemListener((evt) -> {
            if(evt.getStateChange() == ItemEvent.SELECTED) {
                final Date javaDate = monthView.getSelectionDate();
                if(javaDate == null) return;
                final LocalDate localDate = javaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                final LocalDate newDate = LocalDate.of((int)yearSelectionBox.getSelectedItem(), localDate.getMonth(), localDate.getDayOfMonth());
                final Date newJavaDate = Date.from(newDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                monthView.setFirstDisplayedDay(newJavaDate);
            }
        });

        monthView.getSelectionModel().addDateSelectionListener((ev) -> {
            final Date javaDate = monthView.getSelectionDate();
            if (javaDate == null) return;
            final LocalDate localDate = javaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            final SessionDateEdit edit = new SessionDateEdit(session, editor.getEventManager(), localDate, dateTier.getValue());
            editor.getUndoSupport().postEdit(edit);
        });

        final JPanel calloutPanel = new JPanel(new BorderLayout());
        calloutPanel.add(yearSelectionBox, BorderLayout.NORTH);
        calloutPanel.add(monthView, BorderLayout.CENTER);

        final TranscriptDocument.StartEnd startEnd = doc.getGenericContentStartEnd(dateTier);
        if(startEnd.valid()) {
            try {
                final var pos = editor.modelToView2D(startEnd.start());
                final var pt = new Point((int)pos.getX(), (int)pos.getMaxY());
                SwingUtilities.convertPointToScreen(pt, editor);
                CalloutWindow.showCallout(CommonModuleFrame.getCurrentFrame(), true, calloutPanel,
                        SwingUtilities.NORTH, SwingConstants.CENTER, pt);
            } catch (BadLocationException e) {
                LogUtil.warning(e);
            }
        }
    }

    /**
     * Updates the date header when the session date changes
     *
     * @param event the event that caused the change to the session date
     * */
    public void updateDateHeader(EditorEvent<EditorEventType.SessionDateChangedData> event) {
        try {
            Tier<LocalDate> dateHeaderTier = (Tier<LocalDate>) headerTierMap.get("date");
            if(errorUnderlineHighlights.containsKey(dateHeaderTier)) {
                editor.getHighlighter().removeHighlight(errorUnderlineHighlights.get(dateHeaderTier));
                errorUnderlineHighlights.remove(dateHeaderTier);
            }

            final UnvalidatedValue uv = dateHeaderTier.getUnvalidatedValue();
            dateHeaderTier.setValue(event.getData().get().newDate());
            if(!dateHeaderTier.hasValue() && uv != null) {
                dateHeaderTier.putExtension(UnvalidatedValue.class, uv);
            }
            TranscriptDocument.StartEnd startEnd = doc.getGenericContentStartEnd(dateHeaderTier);

            if (startEnd.valid()) {
                doc.setBypassDocumentFilter(true);
                editor.getTranscriptEditorCaret().freeze();
                doc.remove(startEnd.start(), startEnd.length());

                final SimpleAttributeSet attrs = new SimpleAttributeSet(doc.getTranscriptStyleContext().getStyle(TranscriptStyleContext.DEFAULT_STYLE));
                TranscriptStyleConstants.setElementType(attrs, TranscriptStyleConstants.ELEMENT_TYPE_GENERIC);
                TranscriptStyleConstants.setGenericTier(attrs, dateHeaderTier);

                TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder(doc.getTranscriptStyleContext(), doc.getInsertionHooks());
                final PhonUIAction showDatePickerAct = PhonUIAction.runnable(this::showDatePicker);
                TranscriptStyleConstants.setEnterAction(attrs, showDatePickerAct);
                batchBuilder.appendBatchString(dateHeaderTier.toString(), attrs);
                doc.processBatchUpdates(startEnd.start(), batchBuilder.getBatch());

                if(dateHeaderTier.isUnvalidated()) {
                    errorUnderlineHighlights.put(dateHeaderTier,
                            editor.getHighlighter().addHighlight(startEnd.start(), startEnd.end(), new TranscriptEditor.ErrorUnderlinePainter()));
                }
            }
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        } finally {
            editor.getTranscriptEditorCaret().unfreeze();
        }
    }

    private void onSessionMediaChanged(EditorEvent<EditorEventType.SessionMediaChangedData> evt) {
        try {
            Tier<TierData> mediaTier = (Tier<TierData>) headerTierMap.get("media");
            if(errorUnderlineHighlights.containsKey(mediaTier)) {
                editor.getHighlighter().removeHighlight(errorUnderlineHighlights.get(mediaTier));
                errorUnderlineHighlights.remove(mediaTier);
            }

            mediaTier.setValue(TierData.parseTierData(evt.getData().get().newMedia()));
            final File file = MediaLocator.findMediaFile(mediaTier.getValue().toString(), editor.getDataModel().getProject(), editor.getSession().getCorpus());
            if(file == null || !file.exists()) {
                final UnvalidatedValue uv = new UnvalidatedValue(mediaTier.getValue().toString(), new ParseException("Media file does not exist", 0));
                mediaTier.getValue().putExtension(UnvalidatedValue.class, uv);
            }

            TranscriptDocument.StartEnd startEnd = doc.getGenericContentStartEnd(mediaTier);
            if (startEnd.valid()) {
                doc.setBypassDocumentFilter(true);
                editor.getTranscriptEditorCaret().freeze();
                doc.remove(startEnd.start(), startEnd.length());

                final SimpleAttributeSet attrs = new SimpleAttributeSet(doc.getTranscriptStyleContext().getStyle(TranscriptStyleContext.DEFAULT_STYLE));
                TranscriptStyleConstants.setElementType(attrs, TranscriptStyleConstants.ELEMENT_TYPE_GENERIC);
                TranscriptStyleConstants.setGenericTier(attrs, mediaTier);

                TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder(doc.getTranscriptStyleContext(), doc.getInsertionHooks());
                batchBuilder.appendBatchString(mediaTier.toString(), attrs);
                doc.processBatchUpdates(startEnd.start(), batchBuilder.getBatch());

                if(mediaTier.isUnvalidated()) {
                    errorUnderlineHighlights.put(mediaTier,
                            editor.getHighlighter().addHighlight(startEnd.start(), startEnd.end(), new TranscriptEditor.ErrorUnderlinePainter()));
                }
            }
        } catch (BadLocationException | ParseException e) {
            LogUtil.severe(e);
        } finally {
            editor.getTranscriptEditorCaret().unfreeze();
        }
    }

    private void onSessionLocationChanged(EditorEvent<TranscriptEditor.TranscriptLocationChangeData> evt) {
        if(evt.getData().get().oldLoc().transcriptElementIndex() == -1) {
            if(evt.getData().get().oldLoc().tier().equals("Date")) {
                final Tier<LocalDate> dateTier = (Tier<LocalDate>) headerTierMap.get("date");
                final String currentDateString = dateTier.toString();
                final TranscriptDocument.StartEnd startEnd = doc.getGenericContentStartEnd(dateTier);
                if(startEnd.valid()) {
                    try {
                        final LocalDate date = dateTier.getValue();
                        final String dateStr = doc.getText(startEnd.start(), startEnd.length());
                        if(!dateStr.equals(currentDateString)) {
                            dateTier.setText(dateStr);

                            if (dateTier.isUnvalidated()) {
                                final SessionDateEdit edit = new SessionDateEdit(session, editor.getEventManager(), null, date);
                                editor.getUndoSupport().postEdit(edit);
                            } else {
                                final SessionDateEdit edit = new SessionDateEdit(session, editor.getEventManager(), dateTier.getValue(), date);
                                editor.getUndoSupport().postEdit(edit);
                            }
                        }
                    } catch (BadLocationException e) {
                        LogUtil.warning(e);
                    }
                }
            } else if(evt.getData().get().oldLoc().tier().equals("Media")) {
                final Tier<TierData> mediaTier = (Tier<TierData>) headerTierMap.get("media");
                final TranscriptDocument.StartEnd startEnd = doc.getGenericContentStartEnd(mediaTier);
                if(startEnd.valid()) {
                    try {
                        final String currentMediaString = mediaTier.toString();
                        final String mediaStr = doc.getText(startEnd.start(), startEnd.length());
                        if(!mediaStr.equals(currentMediaString)) {
                            mediaTier.setText(mediaStr);

                            final MediaLocationEdit edit = new MediaLocationEdit(session, editor.getEventManager(), mediaTier.getValue().toString());
                            editor.getUndoSupport().postEdit(edit);
                        }
                    } catch (BadLocationException e) {
                        LogUtil.warning(e);
                    }
                }
            }
        }
    }

    private void clearDate() {
        final Tier<LocalDate> dateTier = (Tier<LocalDate>) headerTierMap.get("date");
        var oldDate = dateTier.getValue();
        dateTier.setValue(null);

        final SessionDateEdit edit = new SessionDateEdit(session, editor.getEventManager(), null, oldDate);
        editor.getUndoSupport().postEdit(edit);
    }

    private void showDateTierMenu(MouseEvent e, Tier<LocalDate> dateTier) {
        final JMenu menu = new JMenu("Date");
        final MenuBuilder builder = new MenuBuilder(menu);

        final PhonUIAction<Void> clearDateAct = PhonUIAction.runnable(this::clearDate);
        clearDateAct.putValue(PhonUIAction.NAME, "Remove date");
        clearDateAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Remove date from session");
        builder.addItem(".", clearDateAct);

        final PhonUIAction<Void> showDatePickerAct = PhonUIAction.runnable(this::showDatePicker);
        showDatePickerAct.putValue(PhonUIAction.NAME, "Edit");
        showDatePickerAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Edit date using date picker");
        builder.addItem(".", showDatePickerAct);

        menu.getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
    }

    private void showMediaTierMenu(MouseEvent e, Tier<TierData> mediaTier) {
//        final JMenu menu = new JMenu("Media");
//        final MenuBuilder builder = new MenuBuilder(menu);
//
//        final JMenuItem editItem = new JMenuItem("Edit");
//        editItem.addActionListener( e1 -> {
//            final String oldValue = mediaTier.getValue();
//            final String newValue = JOptionPane.showInputDialog(editor, "Enter new media location", oldValue);
//            if(newValue != null) {
//                mediaTier.setValue(newValue);
//                final SessionMediaEdit edit = new SessionMediaEdit(session, editor.getEventManager(), oldValue, newValue);
//                editor.getUndoSupport().postEdit(edit);
//            }
//        });
//        builder.addItem(".", editItem);
//
//        menu.show(editor, e.getX(), e.getY());
    }

    /**
     * Get whether the language header is currently being forced to be shown without any content
     * */
    private boolean isForceShowLanguageHeader() {
        return (boolean) doc.getDocumentPropertyOrDefault("forceShowLanguageHeader", false);
    }

    private class YearComboBoxModel extends DefaultComboBoxModel<Integer> {

        private int numYears;

        private int selectedYear = 0;

        public YearComboBoxModel() {
            this(150);
        }

        public YearComboBoxModel(int numYears) {
            this.numYears = numYears;
        }

        @Override
        public int getSize() {
            return this.numYears;
        }

        @Override
        public Integer getElementAt(int index) {
            return LocalDate.now().getYear() - ((this.numYears-1)-index);
        }

        @Override
        public void setSelectedItem(Object anItem) {
            this.selectedYear = (Integer)anItem;
        }

        @Override
        public Object getSelectedItem() {
            return this.selectedYear;
        }
    }

}
