package ca.phon.app.session.editor.view.transcript.extensions;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.undo.SessionDateEdit;
import ca.phon.app.session.editor.view.transcript.*;
import ca.phon.extensions.UnvalidatedValue;
import ca.phon.formatter.Formatter;
import ca.phon.session.*;
import ca.phon.session.tierdata.TierData;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.text.DatePicker;
import ca.phon.util.Language;
import ca.phon.util.LanguageEntry;
import ca.phon.util.LanguageParser;
import org.apache.derby.impl.sql.catalog.DD_Version;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An extension that provides header tier support to the {@link TranscriptEditor}
 * */
public class HeaderTierExtension implements TranscriptEditorExtension {
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

        doc.addInsertionHook(new DefaultInsertionHook() {
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
//                        newLineAttrs = transcriptStyleContext.getTrailingAttributes(retVal);
//                        retVal.addAll(TranscriptBatchBuilder.getBatchEndLineFeed(newLineAttrs, null));
                    }

                    // Add media line if present
                    var sessionMedia = session.getMediaLocation();
                    if (sessionMedia != null) {
                        Tier<TierData> mediaTier = (Tier<TierData>) headerTierMap.get("media");
                        mediaTier.setText(sessionMedia);
                        batchBuilder.appendGeneric("Media", mediaTier, null);
                        batchBuilder.appendEOL();
//                        retVal.addAll(doc.getGeneric("Media", mediaTier, null));
//                        newLineAttrs = transcriptStyleContext.getTrailingAttributes(retVal);
//                        retVal.addAll(TranscriptBatchBuilder.getBatchEndLineFeed(newLineAttrs, null));
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
        });

        doc.addDocumentPropertyChangeListener(HEADERS_VISIBLE, evt -> doc.reload());

        editor.getEventManager().registerActionForEvent(EditorEventType.TierViewChanged, this::updateTiersHeader, EditorEventManager.RunOn.AWTEventDispatchThread);
        editor.getEventManager().registerActionForEvent(EditorEventType.SessionDateChanged, this::updateDateHeader, EditorEventManager.RunOn.AWTEventDispatchThread);
        editor.getEventManager().registerActionForEvent(TranscriptEditor.transcriptLocationChanged, this::onSessionLocationChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
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
        batchBuilder.appendBatchString(dateTier.toString(), attrs);
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
            dateHeaderTier.putExtension(UnvalidatedValue.class, uv);
            TranscriptDocument.StartEnd startEnd = doc.getGenericContentStartEnd(dateHeaderTier);

            if (startEnd.valid()) {
                doc.setBypassDocumentFilter(true);
                editor.getTranscriptEditorCaret().freeze();
                doc.remove(startEnd.start(), startEnd.length());

                final SimpleAttributeSet attrs = new SimpleAttributeSet(doc.getTranscriptStyleContext().getStyle(TranscriptStyleContext.DEFAULT_STYLE));
                TranscriptStyleConstants.setElementType(attrs, TranscriptStyleConstants.ELEMENT_TYPE_GENERIC);
                TranscriptStyleConstants.setGenericTier(attrs, dateHeaderTier);

                TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder(doc.getTranscriptStyleContext(), doc.getInsertionHooks());
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

    private void onSessionLocationChanged(EditorEvent<TranscriptEditor.TranscriptLocationChangeData> evt) {
        if(evt.getData().get().oldLoc().transcriptElementIndex() == -1) {
            if(evt.getData().get().oldLoc().tier().equals("Date")) {
                final Tier<LocalDate> dateTier = (Tier<LocalDate>) headerTierMap.get("date");
                final TranscriptDocument.StartEnd startEnd = doc.getGenericContentStartEnd(dateTier);
                if(startEnd.valid()) {
                    try {
                        final LocalDate date = dateTier.getValue();
                        final String dateStr = doc.getText(startEnd.start(), startEnd.length());
                        dateTier.setText(dateStr);

                        if(dateTier.isUnvalidated()) {
                            final SessionDateEdit edit = new SessionDateEdit(session, editor.getEventManager(), null, date);
                            editor.getUndoSupport().postEdit(edit);
                        } else {
                            final SessionDateEdit edit = new SessionDateEdit(session, editor.getEventManager(), dateTier.getValue(), date);
                            editor.getUndoSupport().postEdit(edit);
                        }
                    } catch (BadLocationException e) {
                        LogUtil.warning(e);
                    }
                }
            }
        }
    }

    private void showDateTierMenu(MouseEvent e, Tier<LocalDate> dateTier) {
//        final JMenu menu = new JMenu("Date");
//        final MenuBuilder builder = new MenuBuilder(menu);
//
//        final DatePicker
    }

    /**
     * Get whether the language header is currently being forced to be shown without any content
     * */
    private boolean isForceShowLanguageHeader() {
        return (boolean) doc.getDocumentPropertyOrDefault("forceShowLanguageHeader", false);
    }

}
