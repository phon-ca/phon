package ca.phon.app.session.timeline;

import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.extensions.Extension;
import ca.phon.extensions.ExtensionProvider;
import ca.phon.extensions.IExtendable;
import ca.phon.orthography.Orthography;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.tierdata.TierData;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension for record media segment changes. This extension will update all record tiers
 * which are included in the session IntervalTiers. Internal media segments will be updated to
 * reflect the new record segment.
 */
@Extension(Tier.class)
public class TimelineDependentTierChanges implements TierEdit.DependentTierChanges<MediaSegment>, ExtensionProvider {

    @Override
    public void performDependentTierChanges(TierEdit<MediaSegment> tierEdit) {
        final Session session = tierEdit.getSession();
        final Record record = tierEdit.getRecord();
        final Tier<MediaSegment> segmentTier = tierEdit.getTier();
        MediaSegment segment = tierEdit.getNewValue();
        MediaSegment oldSegment = tierEdit.getOldValue();
        if(oldSegment.equals(segmentTier.getValue())) {
            oldSegment = segment;
            segment = segmentTier.getValue();
        }

        for(String tierName:session.getTimeline().getRecordIntervalTiers()) {
            final Tier<?> tier = record.getTier(tierName);
            if(tier == null) continue;

            if(tier.getValue() instanceof Orthography orthography) {
                final OrthoIntervalVisitor visitor = new OrthoIntervalVisitor();
                orthography.accept(visitor);
                final List<IntervalTier.Interval> internalMediaList = visitor.getIntervals();
                final List<IntervalTier.Interval> newInternalMediaList = updateInternalMedia(segment, oldSegment, internalMediaList);
                final OrthoIntervalUpdateVisitor updateVisitor = new OrthoIntervalUpdateVisitor(newInternalMediaList);
                orthography.accept(updateVisitor);
                final Orthography newOrtho = updateVisitor.getUpdatedOrthography();
                ((Tier<Orthography>)tier).setValue(newOrtho);
                tierEdit.putAdditionalTierChange(tierName, newOrtho);
                tierEdit.fireTierChange((Tier<Orthography>)tier, orthography, newOrtho);
            } else if(tier.getValue() instanceof TierData tierData) {
                final TierDataIntervalVisitor visitor = new TierDataIntervalVisitor();
                tierData.accept(visitor);
                final List<IntervalTier.Interval> internalMediaList = visitor.getIntervals();
                final List<IntervalTier.Interval> newInternalMediaList = updateInternalMedia(segment, oldSegment, internalMediaList);
                final TierDataIntervalUpdateVisitor updateVisitor = new TierDataIntervalUpdateVisitor(newInternalMediaList);
                tierData.accept(updateVisitor);
                final TierData newTierData = updateVisitor.getUpdatedTierData();
                ((Tier<TierData>)tier).setValue(newTierData);
                tierEdit.putAdditionalTierChange(tierName, newTierData);
                tierEdit.fireTierChange((Tier<TierData>)tier, tierData, newTierData);
            }
        }
    }

    /**
     * Update the provided list of media segments such that the new segments have the same proportions
     * as the old segments to the original media segment.
     *
     * @param segment
     * @param oldSegment
     * @param internalMediaList
     * @return
     */
    private List<IntervalTier.Interval> updateInternalMedia(MediaSegment segment, MediaSegment oldSegment, List<IntervalTier.Interval> internalMediaList) {
        final double oldDuration = oldSegment.getEndTime() - oldSegment.getStartTime();
        final double newDuration = segment.getEndTime() - segment.getStartTime();

        final List<IntervalTier.Interval> retVal = new ArrayList<>();
        for(IntervalTier.Interval internalMedia:internalMediaList) {
            final double startTime = segment.getStartTime() + ((internalMedia.getStart() - oldSegment.getStartTime()) / oldDuration) * newDuration;
            final double endTime = segment.getStartTime() + ((internalMedia.getEnd() - oldSegment.getStartTime()) / oldDuration) * newDuration;
            final String label = internalMedia.getLabel();
            final IntervalTier.Interval newInterval = new IntervalTier.Interval((float)startTime, (float)endTime, label);
            retVal.add(newInterval);
        }
        return retVal;
    }

    @Override
    public void installExtension(IExtendable obj) {
        if(obj instanceof Tier<?> tier) {
            if(SystemTierType.Segment.getName().equals(tier.getName()) && tier.getDeclaredType() == MediaSegment.class) {
                final TimelineDependentTierChanges extension = new TimelineDependentTierChanges();
                tier.putExtension(TierEdit.DependentTierChanges.class, extension);
            }
        }
    }

}
