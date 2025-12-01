package ca.phon.app.session.intervalTiers;

import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;
import ca.phon.session.IntervalTier;
import ca.phon.session.Tier;
import ca.phon.session.Transcriber;
import ca.phon.session.tierdata.TierData;
import ca.phon.session.tierdata.TierElement;
import ca.phon.util.SegmentOverlapUtil.OverlapType;
import ca.phon.visitor.VisitorAdapter;

import java.util.*;

/**
 * Class which will align word and phone intervals.
 *
 */
public final class WordAndPhoneIntervalAligner {

    public static Map<IntervalTier.Interval, IntervalTier.Interval[]> alignedWordAndPhoneIntervals(Tier<Orthography> wordIntervalsTier, Tier<TierData> phoneIntervalsTier, Transcriber transcriber) {
        final Optional<Orthography> wordIntervalsData = wordIntervalsTier.getValueForTranscriber(transcriber);
        if(!wordIntervalsData.isPresent()) return Map.of();
        final Orthography wordIntervalsOrthography = wordIntervalsData.get();
        final Optional<TierData> phoneIntervalsData = phoneIntervalsTier.getValueForTranscriber(transcriber);
        final TierData phoneIntervalsTierData = phoneIntervalsData.orElse(new TierData());
        return alignedWordAndPhoneIntervals(wordIntervalsOrthography, phoneIntervalsTierData);
    }

    public static Map<IntervalTier.Interval, IntervalTier.Interval[]> alignedWordAndPhoneIntervals(Orthography wordIntervalsOrthography, TierData phoneIntervalsTierData) {
        final OrthoIntervalVisitor wordIntervalVisitor = new OrthoIntervalVisitor();
        wordIntervalsOrthography.accept(wordIntervalVisitor);
        final List<IntervalTier.Interval> wordIntervals = wordIntervalVisitor.getIntervals();
        final TierDataIntervalVisitor phoneIntervalVisitor = new TierDataIntervalVisitor();
        phoneIntervalsTierData.accept(phoneIntervalVisitor);
        final List<IntervalTier.Interval> phoneIntervals = phoneIntervalVisitor.getIntervals();

        final Map<IntervalTier.Interval, IntervalTier.Interval[]> alignedMap = new LinkedHashMap<>();
        int lastPhoneIntervalIdx = 0;
        for(int i = 0; i < wordIntervals.size(); i++) {
            final IntervalTier.Interval wordInterval = wordIntervals.get(i);
            List<IntervalTier.Interval> phoneIntervalsForWord = new ArrayList<>();
            for(int j = lastPhoneIntervalIdx; j < phoneIntervals.size(); j++) {
                final IntervalTier.Interval phoneInterval = phoneIntervals.get(j);
                final OverlapType overlapType = wordInterval.overlapType(phoneInterval);
                if(overlapType == OverlapType.FULLY_CONTAINS) {
                    phoneIntervalsForWord.add(phoneInterval);
                    lastPhoneIntervalIdx = j + 1;
                } else if(phoneInterval.getStart() > wordInterval.getEnd()) {
                    // no more overlapping phone intervals for this word interval
                    break;
                }
            }
            alignedMap.put(wordInterval, phoneIntervalsForWord.toArray(new IntervalTier.Interval[0]));
        }
        if(lastPhoneIntervalIdx < phoneIntervals.size()) {
            // remaining phone intervals that do not align with any word intervals
            final List<IntervalTier.Interval> phoneIntervalsForWord = new ArrayList<>();
            for(int j = lastPhoneIntervalIdx; j < phoneIntervals.size(); j++) {
                final IntervalTier.Interval phoneInterval = phoneIntervals.get(j);
                phoneIntervalsForWord.add(phoneInterval);
            }
            alignedMap.put(new IntervalTier.Interval(-1, -1), phoneIntervalsForWord.toArray(new IntervalTier.Interval[0]));
        }
        return alignedMap;
    }

}
