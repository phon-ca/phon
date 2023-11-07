package ca.phon.app.session.editor.view.transcriptEditor.extensions;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.view.transcriptEditor.DefaultInsertionHook;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptDocument;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptEditor;
import ca.phon.formatter.Formatter;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.Participant;
import ca.phon.session.Session;
import ca.phon.session.Tier;
import ca.phon.session.TierViewItem;
import ca.phon.session.tierdata.TierData;
import ca.phon.util.Language;
import ca.phon.util.LanguageEntry;
import ca.phon.util.LanguageParser;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class HeaderTierExtension implements TranscriptEditorExtension {
    private final Map<String, Tier<?>> headerTierMap = new HashMap<>();
    private TranscriptEditor editor;
    private TranscriptDocument doc;
    private Session session;

    public static final String HEADERS_VISIBLE = "isHeadersVisible";
    public static final boolean DEFAULT_HEADERS_VISIBLE = true;

    @Override
    public void install(TranscriptEditor editor) {
        this.editor = editor;
        this.doc = editor.getTranscriptDocument();
        this.session = editor.getSession();

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

                    // Add media line if present
                    var sessionMedia = session.getMediaLocation();
                    if (sessionMedia != null) {
                        Tier<TierData> mediaTier = (Tier<TierData>) headerTierMap.get("media");
                        mediaTier.setText(sessionMedia);

                        retVal.addAll(doc.getGeneric("Media", mediaTier, null));
                        newLineAttrs = doc.getTrailingAttributes(retVal);
                        retVal.addAll(doc.getBatchEndLineFeed(newLineAttrs));
                    }

                    // Add languages line if present
                    var sessionLanguages = session.getLanguages();
                    if (sessionLanguages != null && !sessionLanguages.isEmpty()) {
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
                        retVal.addAll(doc.getBatchEndLineFeed(newLineAttrs));
                    }

                    // Add Tiers header
                    retVal.addAll(getTiersHeader());
                    newLineAttrs = doc.getTrailingAttributes(retVal);
                    retVal.addAll(doc.getBatchEndLineFeed(newLineAttrs));

                    // Add Participants header
                    Tier<TierData> participantsTier = (Tier<TierData>) headerTierMap.get("participants");
                    var participants = session.getParticipants();
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
                    retVal.addAll(doc.getBatchEndLineFeed(newLineAttrs));
                }

                return retVal;
            }
        });

        doc.addDocumentPropertyChangeListener(HEADERS_VISIBLE, evt -> doc.reload());

        editor.getEventManager().registerActionForEvent(EditorEventType.TierViewChanged, this::updateTiersHeader, EditorEventManager.RunOn.AWTEventDispatchThread);
    }

    private boolean isHeadersVisible() {
        return (boolean) doc.getDocumentPropertyOrDefault(HEADERS_VISIBLE, DEFAULT_HEADERS_VISIBLE);
    }

    public List<DefaultStyledDocument.ElementSpec> getTiersHeader() {

        List<DefaultStyledDocument.ElementSpec> retVal = new ArrayList<>();

        Tier<TierData> tiersTier = (Tier<TierData>) headerTierMap.get("tiers");

        int start = doc.getGenericStart(tiersTier);
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

    public void updateTiersHeader(EditorEvent<EditorEventType.TierViewChangedData> event) {
        try {
            Tier<?> tiersHeaderTier = headerTierMap.get("tiers");
            int start = doc.getGenericStart(tiersHeaderTier);
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
}
