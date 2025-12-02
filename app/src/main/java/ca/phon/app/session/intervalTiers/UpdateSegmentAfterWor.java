package ca.phon.app.session.intervalTiers;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.extensions.Extension;
import ca.phon.extensions.ExtensionProvider;
import ca.phon.extensions.IExtendable;
import ca.phon.orthography.Orthography;
import ca.phon.session.*;
import ca.phon.session.Record;

import java.util.List;

/**
 * Update segment after Word interval tier changes.
 */
@Extension(Tier.class)
public class UpdateSegmentAfterWor implements TierEdit.DependentTierChanges<Orthography>, ExtensionProvider {

    @Override
    public void performDependentTierChanges(TierEdit<Orthography> tierEdit) {
        if(tierEdit.isValueAdjusting()) return;
        if(!tierEdit.getTier().getName().equals(UserTierType.Wor.getPhonTierName())) {
            return;
        }

        final Record record = tierEdit.getRecord();
        if(record == null) return;
        final OrthoIntervalVisitor visitor = new OrthoIntervalVisitor();
        tierEdit.getNewValue().accept(visitor);
        final List<IntervalTier.Interval> wordIntervals = visitor.getIntervals();

        final MediaSegment currentSegment = record.getMediaSegment();
        // adjust segment time if needed
        float minTime = currentSegment.getStartTime();
        float maxTime = currentSegment.getEndTime();
        for(final IntervalTier.Interval wordInterval : wordIntervals) {
            final float wordStart = wordInterval.getStart();
            final float wordEnd = wordInterval.getEnd();
            minTime = Math.min(minTime, wordStart);
            maxTime = Math.max(maxTime, wordEnd);
        }
        if(currentSegment != null || (currentSegment.getStartTime() > minTime || currentSegment.getEndTime() < maxTime)) {
            final MediaSegment newSegment = SessionFactory.newFactory().createMediaSegment();
            newSegment.setUnitType(MediaUnit.Second);
            newSegment.setStartTime(minTime);
            newSegment.setEndTime(maxTime);
            record.setMediaSegment(newSegment);
            tierEdit.putAdditionalTierChange(SystemTierType.Segment.getName(), currentSegment, newSegment);
            tierEdit.fireTierChange(record.getSegmentTier(), currentSegment, newSegment);
        }
    }

    @Override
    public void installExtension(IExtendable obj) {
        if (obj instanceof Tier<?> tier) {
            if (UserTierType.Wor.getPhonTierName().equals(tier.getName()) && tier.getDeclaredType() == Orthography.class) {
                final UpdateSegmentAfterWor extension = new UpdateSegmentAfterWor();
                final TierEdit.DependentTierChanges existingExtension =  obj.getExtension(TierEdit.DependentTierChanges.class);
                if(existingExtension == null) {
                    obj.putExtension(TierEdit.DependentTierChanges.class, extension);
                } else {
                    obj.putExtension(TierEdit.DependentTierChanges.class, new TierEdit.DependentTierChangeChain(existingExtension, extension));
                }
            }
        }
    }
}
