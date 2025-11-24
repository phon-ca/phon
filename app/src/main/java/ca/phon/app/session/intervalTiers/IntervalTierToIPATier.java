package ca.phon.app.session.intervalTiers;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.undo.AddTierEdit;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.fontconv.TranscriptConverter;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.ipadictionary.TransliterationDictionaryProvider;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.filter.RecordFilter;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.util.SegmentOverlapUtil.OverlapType;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Import intervals from an interval tier into an IPA tier. The intervals
 * that fully contain each record's media segment will be concatenated
 * and added to the specified IPA tier for that record. Optionally, the
 * resulting text can be syllabified using a specified syllabifier.
 *
 * If the interval tier labels are in a different script, a transliteration scheme
 * can be specified to convert the text to Unicode IPA.
 */
public final class IntervalTierToIPATier extends IntervalTierImporter {

    private static final Logger LOGGER = Logger.getLogger(IntervalTierToIPATier.class.getName());

    private final IntervalTierToIPATierSettings settings;

    public IntervalTierToIPATier(IntervalTierToIPATierSettings settings) {
        this.settings = settings;
    }

    public int[] importTier(Session session, EditorEventManager eventManager, SessionEditUndoSupport undoSupport, RecordFilter recordFilter) {
        return importTier(session, eventManager, Transcriber.VALIDATOR, undoSupport, recordFilter);
    }

    public int[] importTier(Session session, EditorEventManager eventManager, Transcriber transcriber, SessionEditUndoSupport undoSupport, RecordFilter recordFilter) {
        final IntervalTier importTier = session.getTimeline().getTier(settings.intervalTierName());
        if(importTier == null) {
            throw new IllegalArgumentException("Interval tier '" + settings.intervalTierName() + "' not found in session");
        }

        TranscriptConverter fontConverter = null;
        if(settings.fontConversionScheme() != null) {
            fontConverter = TranscriptConverter.getInstanceOf(settings.fontConversionScheme());
            if(fontConverter == null) {
                LOGGER.log(Level.WARNING, "Font converter not found: " + settings.fontConversionScheme());
            }
        }

        IPADictionary transliterationDict = null;
        if(settings.transliterationScheme() != null) {
            TransliterationDictionaryProvider provider = new TransliterationDictionaryProvider();
            for(IPADictionary dict : provider) {
                if(dict.getName().equals(settings.transliterationScheme())) {
                    transliterationDict = dict;
                    break;
                }
            }
            if(transliterationDict == null) {
                LOGGER.log(Level.WARNING, "Transliteration dictionary not found: " + settings.transliterationScheme());
            }
        }

        // add tier to session if necessary
        TierDescription td = session.getTier(settings.recordTierName());
        if(td == null) {
            final SessionFactory factory = SessionFactory.newFactory();
            td = factory.createTierDescription(settings.recordTierName(), IPATranscript.class, new HashMap<>(), false, false);
            final TierViewItem tvi = factory.createTierViewItem(settings.recordTierName(), true);
            final AddTierEdit addTierEdit = new AddTierEdit(session, eventManager, td,  tvi);
            undoSupport.postEdit(addTierEdit);
        } else {
            if(!td.getDeclaredType().equals(IPATranscript.class)) {
                throw new IllegalArgumentException("Tier '" + settings.recordTierName() + "' is not of type IPATranscript");
            }
        }

        List<Integer> updatedRecords = new ArrayList<>();
        for(int recordIndex = 0; recordIndex < session.getRecordCount(); recordIndex++) {
            final Record record = session.getRecord(recordIndex);
            if(!recordFilter.checkRecord(record)) continue;

            final MediaSegment segment = record.getMediaSegment();
            if(segment.isPoint()) continue;

            final IntervalTier.Interval recordInterval = new IntervalTier.Interval(segment.getStartTime(), segment.getEndTime());
            final int[] containedIntervals = importTier.overlappingIntervals(recordInterval, OverlapType.FULLY_CONTAINS);
            if(containedIntervals.length == 0) continue;

            final StringBuilder sb = new StringBuilder();
            for(int idx:containedIntervals) {
                final IntervalTier.Interval interval = importTier.getIntervals().get(idx);
                if(interval.getLabel() != null && !interval.getLabel().isBlank()) {
                    if(!sb.isEmpty()) sb.append(" ");
                    sb.append(interval.getLabel());
                }
            }
            String ipaString = sb.toString().trim();

            // Apply font conversion first (if specified)
            if(fontConverter != null && !ipaString.isEmpty()) {
                try {
                    ipaString = fontConverter.convert(ipaString);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error during font conversion for: " + ipaString, e);
                }
            }

            if(transliterationDict != null) {
                try {
                    String[] lookupResults = transliterationDict.lookup(ipaString);
                    if(lookupResults != null && lookupResults.length > 0) {
                        // Transliteration dictionaries always return one transcription
                        ipaString = lookupResults[0];
                    } else {
                        LOGGER.log(Level.WARNING, "No transliteration found for: " + ipaString);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error during transliteration lookup for: " + ipaString, e);
                }
            }

            IPATranscript ipa = null;
            try {
                ipa = IPATranscript.parseIPATranscript(ipaString);
            } catch (ParseException e) {
                LOGGER.log(Level.WARNING, "Error parsing IPA transcript: " + ipaString, e);
                // Fallback to builder which is more lenient
                ipa = new IPATranscriptBuilder().append(ipaString).toIPATranscript();
            }

            Syllabifier syllabifier = SyllabifierOptions.findSyllabifier(session, record, settings.recordTierName());
            if(settings.language() != null) {
                final Syllabifier selectedSyllabifier = SyllabifierLibrary.getInstance().getSyllabifierForLanguage(settings.language());
                if(selectedSyllabifier != null) {
                    syllabifier = selectedSyllabifier;
                }
            }
            if(syllabifier != null) {
                ipa = syllabifier.syllabify(ipa);
            }

            final Tier<IPATranscript> ipaTier = record.getTier(settings.recordTierName(), IPATranscript.class);
            if(ipaTier != null) {
                final TierEdit<IPATranscript> tierEdit = new TierEdit<>(session, eventManager, transcriber, record, ipaTier, ipa, false);
                undoSupport.postEdit(tierEdit);

                updatedRecords.add(recordIndex);
            }
        }

        return updatedRecords.stream().mapToInt(i->i).toArray();
    }

}
