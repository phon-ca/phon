package ca.phon.app.session.editor.view.transcriptEditor.extensions;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.view.transcriptEditor.DefaultInsertionHook;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptDocument;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptEditor;
import ca.phon.formatter.Formatter;
import ca.phon.session.*;
import ca.phon.session.tierdata.TierData;
import ca.phon.util.Language;
import ca.phon.util.LanguageEntry;
import ca.phon.util.LanguageParser;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.*;
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
                List<DefaultStyledDocument.ElementSpec> retVal = new ArrayList<>();

                if (isHeadersVisible()) {
                    AttributeSet newLineAttrs;

                    // Add date line if present
                    if (session.getDate() != null) {
                        retVal.addAll(getDateHeader());
                        newLineAttrs = doc.getTrailingAttributes(retVal);
                        retVal.addAll(doc.getBatchEndLineFeed(newLineAttrs, null));
                    }

                    // Add media line if present
                    var sessionMedia = session.getMediaLocation();
                    if (sessionMedia != null) {
                        Tier<TierData> mediaTier = (Tier<TierData>) headerTierMap.get("media");
                        mediaTier.setText(sessionMedia);

                        retVal.addAll(doc.getGeneric("Media", mediaTier, null));
                        newLineAttrs = doc.getTrailingAttributes(retVal);
                        retVal.addAll(doc.getBatchEndLineFeed(newLineAttrs, null));
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

                        retVal.addAll(doc.getGeneric("Languages", languagesTier, null));
                        newLineAttrs = doc.getTrailingAttributes(retVal);
                        retVal.addAll(doc.getBatchEndLineFeed(newLineAttrs, null));
                    }

                    // Add Tiers header
                    retVal.addAll(getTiersHeader());
                    newLineAttrs = doc.getTrailingAttributes(retVal);
                    retVal.addAll(doc.getBatchEndLineFeed(newLineAttrs, null));

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

                    retVal.addAll(doc.getGeneric("Participants", participantsTier, doc.getParticipantsHeaderAttributes()));
                    newLineAttrs = doc.getTrailingAttributes(retVal);
                    retVal.addAll(doc.getBatchEndLineFeed(newLineAttrs, null));
                }

                return retVal;
            }
        });

        doc.addDocumentPropertyChangeListener(HEADERS_VISIBLE, evt -> doc.reload());

        editor.getEventManager().registerActionForEvent(EditorEventType.TierViewChanged, this::updateTiersHeader, EditorEventManager.RunOn.AWTEventDispatchThread);
        editor.getEventManager().registerActionForEvent(EditorEventType.SessionDateChanged, this::updateDateHeader, EditorEventManager.RunOn.AWTEventDispatchThread);
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
    public List<DefaultStyledDocument.ElementSpec> getTiersHeader() {

        List<DefaultStyledDocument.ElementSpec> retVal = new ArrayList<>();

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

        retVal.addAll(doc.getBatchEndStart());
        retVal.addAll(doc.getGeneric("Tiers", tiersTier, doc.getTiersHeaderAttributes()));
        return retVal;
    }

    /**
     * Updates the tiers header when there is a change to the tier view
     *
     * @param event the event that caused the change to the tier view
     * */
    public void updateTiersHeader(EditorEvent<EditorEventType.TierViewChangedData> event) {
        try {
            Tier<?> tiersHeaderTier = headerTierMap.get("tiers");
            int start = doc.getGenericContentStart(tiersHeaderTier);
            int end = doc.getGenericEnd(tiersHeaderTier);

            if (start > -1 && end > -1) {
                start -= doc.getLabelColumnWidth() + 2;
                doc.setBypassDocumentFilter(true);
                doc.remove(start, end - start);
            }

            List<DefaultStyledDocument.ElementSpec> inserts = getTiersHeader();
            doc.getBatch().addAll(inserts);
            doc.processBatchUpdates(start > -1 ? start : 0);
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
    private List<DefaultStyledDocument.ElementSpec> getDateHeader() {
        List<DefaultStyledDocument.ElementSpec> retVal = new ArrayList<>();

        Tier<LocalDate> dateTier = (Tier<LocalDate>) headerTierMap.get("date");
        dateTier.setValue(editor.getSession().getDate());

        retVal.addAll(doc.getBatchEndStart());
        retVal.addAll(doc.getGeneric("Date", dateTier, null));

        return retVal;
    }

    /**
     * Updates the date header when the session date changes
     *
     * @param event the event that caused the change to the session date
     * */
    public void updateDateHeader(EditorEvent<EditorEventType.SessionDateChangedData> event) {
        try {
            Tier<?> dateHeaderTier = headerTierMap.get("date");
            int start = doc.getGenericContentStart(dateHeaderTier);
            int end = doc.getGenericEnd(dateHeaderTier);

            if (start > -1 && end > -1) {
                start -= doc.getLabelColumnWidth() + 2;
                doc.setBypassDocumentFilter(true);
                doc.remove(start, end - start);
            }

            List<DefaultStyledDocument.ElementSpec> inserts = getDateHeader();
            doc.getBatch().addAll(inserts);
            var newLineAttrs = doc.getTrailingAttributes(doc.getBatch());
            doc.appendBatchLineFeed(newLineAttrs, null);
            doc.processBatchUpdates(start > -1 ? start : 0);
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    /**
     * Get whether the language header is currently being forced to be shown without any content
     * */
    private boolean isForceShowLanguageHeader() {
        return (boolean) doc.getDocumentPropertyOrDefault("forceShowLanguageHeader", false);
    }
}
