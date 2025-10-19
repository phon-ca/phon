package ca.phon.app.session.intervalTiers;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.ipadictionary.impl.TransliterationDictionary;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierLibrary;

public class IntervalTierToIPATier {

    private final IntervalTierToIPATierSettings settings;

    public IntervalTierToIPATier(IntervalTierToIPATierSettings settings) {
        this.settings = settings;
    }

    public void importTier(Session session, EditorEventManager eventManager, SessionEditUndoSupport undoSupport) {
        importTier(session, eventManager, Transcriber.VALIDATOR, undoSupport);
    }

    public void importTier(Session session, EditorEventManager eventManager, Transcriber transcriber, SessionEditUndoSupport undoSupport) {
        final IntervalTier importTier = session.getTimeline().getTier(settings.intervalTierName());
        if(importTier == null) {
            throw new IllegalArgumentException("Interval tier '" + settings.intervalTierName() + "' not found in session");
        }
        undoSupport.beginUpdate("Import intervals from tier '" + settings.intervalTierName() + "' to IPA tier '" + settings.recordTierName() + "'");

        for(Record record:session.getRecords()) {
            final MediaSegment segment = record.getMediaSegment();
            if(segment.isPoint()) continue;

            final IntervalTier.Interval recordInterval = new IntervalTier.Interval(segment.getStartTime(), segment.getEndTime());
            final int[] containedIntervals = importTier.overlappingIntervals(recordInterval, IntervalTier.OverlapType.FULLY_CONTAINS);
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

            if(settings.transliterationScheme() != null) {
                // TODO use transliteration dictionary to convert to IPA
            }

            IPATranscript ipa = new IPATranscriptBuilder().append(ipaString).toIPATranscript();

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
                final TierEdit<IPATranscript> tierEdit = new TierEdit<>(session, eventManager, transcriber, record, ipaTier, ipa);
                undoSupport.postEdit(tierEdit);
            }
        }

        undoSupport.endUpdate();
    }

}
