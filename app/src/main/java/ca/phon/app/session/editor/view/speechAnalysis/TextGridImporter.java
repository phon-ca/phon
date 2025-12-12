package ca.phon.app.session.editor.view.speechAnalysis;

import ca.hedlund.jpraat.binding.fon.IntervalTier;
import ca.hedlund.jpraat.binding.fon.TextGrid;
import ca.hedlund.jpraat.binding.fon.TextInterval;
import ca.hedlund.jpraat.exceptions.PraatException;
import ca.phon.app.log.LogUtil;
import ca.phon.session.SessionFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Import TextGrid files as session-level timeline tiers in Phon sessions.
 * All tiers in the TextGrid will be imported as interval tiers with the
 * same names as in the TextGrid.
 *
 */
public class TextGridImporter {

    public final static boolean DEFAULT_ALLOW_OVERLAPPING_INTERVALS = false;
    private final boolean allowOverlappingIntervals;

    public final static boolean DEFAULT_IGNORE_EMPTY_LABELS = true;
    private final boolean ignoreEmptyLabels;

    public TextGridImporter() {
        this(DEFAULT_IGNORE_EMPTY_LABELS, DEFAULT_ALLOW_OVERLAPPING_INTERVALS);
    }

    public TextGridImporter(boolean ignoreEmptyLabels, boolean allowOverlappingIntervals) {
        this.ignoreEmptyLabels = ignoreEmptyLabels;
        this.allowOverlappingIntervals = allowOverlappingIntervals;
    }

    public List<ca.phon.session.IntervalTier> importTextGrid(TextGrid textGrid) {
        List<ca.phon.session.IntervalTier> retVal = new ArrayList<>();
        for(int i = 1; i <= textGrid.numberOfTiers(); i++) {
            try {
                final IntervalTier intervalTier = textGrid.checkSpecifiedTierIsIntervalTier(i);
                retVal.add(importTextGridIntervalTier(intervalTier));
            } catch (PraatException e) {
                LogUtil.severe(e);
            }
        }
        return retVal;
    }

    public ca.phon.session.IntervalTier importTextGridIntervalTier(IntervalTier intervalTier) {
        ca.phon.session.IntervalTier sessionTimelineTier = SessionFactory.newFactory().createTimelineTier(intervalTier.getName());

        // add intervals, ensuring that no overlapping intervals are added
        for (int i = 1; i <= intervalTier.numberOfIntervals(); i++) {
            final TextInterval textInterval = intervalTier.interval(i);
            if(textInterval.getText().isBlank() && ignoreEmptyLabels) continue;
            importTextInterval(sessionTimelineTier, textInterval, allowOverlappingIntervals);
        }

        return sessionTimelineTier;
    }

    public ca.phon.session.IntervalTier.Interval importTextInterval(ca.phon.session.IntervalTier sessionTimelineTier, TextInterval textInterval, boolean allowOverlappingIntervals) {
        final ca.phon.session.IntervalTier.InsertionStrategy insertionStrategy =
                allowOverlappingIntervals ? ca.phon.session.IntervalTier.InsertionStrategy.ALLOW_OVERLAPS
                        : ca.phon.session.IntervalTier.InsertionStrategy.ERROR_ON_OVERLAP;
        final ca.phon.session.IntervalTier.Interval newInterval = sessionTimelineTier.addInterval((float) textInterval.getXmin(), (float) textInterval.getXmax(), textInterval.getText(), insertionStrategy);
        return newInterval;
    }

}
