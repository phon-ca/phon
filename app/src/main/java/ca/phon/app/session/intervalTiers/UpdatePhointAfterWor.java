package ca.phon.app.session.intervalTiers;

import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.extensions.Extension;
import ca.phon.extensions.ExtensionProvider;
import ca.phon.extensions.IExtendable;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.InternalMedia;
import ca.phon.orthography.Orthography;
import ca.phon.orthography.OrthographyElement;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.alignment.TierAligner;
import ca.phon.session.alignment.TierAlignment;
import ca.phon.session.tierdata.TierData;
import ca.phon.session.tierdata.TierElement;
import ca.phon.session.tierdata.TierInternalMedia;
import ca.phon.session.tierdata.TierString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Update text and intervals of the pho interval tier (%xphoint) when text of the word intervals
 * tier (%wor) changes.
 */
@Extension(Tier.class)
public class UpdatePhointAfterWor implements TierEdit.DependentTierChanges<Orthography>, ExtensionProvider {

    private InternalMedia getFirstInternalMedia(Orthography ortho) {
        if(ortho == null) return null;
        for(int i = 0; i < ortho.length(); i++) {
            final OrthographyElement ele = ortho.elementAt(i);
            if(ele instanceof InternalMedia) {
                return (InternalMedia)ele;
            }
        }
        return null;
    }

    private InternalMedia getLastInternalMedia(Orthography ortho) {
        if(ortho == null) return null;
        for(int i = ortho.length() - 1; i >= 0; i--) {
            final OrthographyElement ele = ortho.elementAt(i);
            if(ele instanceof InternalMedia) {
                return (InternalMedia)ele;
            }
        }
        return null;
    }

    @Override
    public void performDependentTierChanges(TierEdit<Orthography> tierEdit) {
        final Session session = tierEdit.getSession();
        // check for the phone intervals tier
        final TierDescription phoTierDesc = session.getUserTiers()
                .stream()
                .filter(td -> UserTierType.PhoneIntervals.getPhonTierName().equals(td.getName()))
                .findAny().orElse(null);
        if(phoTierDesc == null) return;

        final Record record = tierEdit.getRecord();
        final Tier<Orthography> wordIntervalsTier = tierEdit.getTier();
        final Tier<IPATranscript> ipaTier = record.getIPAActualTier();
        final Tier<TierData> phoneIntervalsTier = record.getTier(phoTierDesc.getName(), TierData.class);
        if(phoneIntervalsTier == null) return;

        final Orthography oldWordIntervals = tierEdit.getOldValue();

        final OrthoIntervalVisitor oldWordIntervalVisitor = new OrthoIntervalVisitor();
        tierEdit.getOldValue().accept(oldWordIntervalVisitor);
        final List<IntervalTier.Interval> oldWordIntervalsList = oldWordIntervalVisitor.getIntervals();

        final OrthoIntervalVisitor wordIntervalVisitor = new OrthoIntervalVisitor();
        tierEdit.getNewValue().accept(wordIntervalVisitor);
        final List<IntervalTier.Interval> newWordIntervalsList = wordIntervalVisitor.getIntervals();

        final List<TierElement> newPhoneIntervals = new ArrayList<>();
        final TierData oldPhoneIntervals = phoneIntervalsTier.getValueForTranscriber(tierEdit.getTranscriber()).orElse(new TierData());
        if(oldWordIntervalsList.size() == newWordIntervalsList.size()) {
            final Map<IntervalTier.Interval, IntervalTier.Interval[]> alignedMap =
                    WordAndPhoneIntervalAligner.alignedWordAndPhoneIntervals(oldWordIntervals, oldPhoneIntervals);
            for(int i = 0; i < oldWordIntervalsList.size(); i++) {
                if(i > 0) {
                    newPhoneIntervals.add(new TierString("/"));
                }
                final IntervalTier.Interval oldWordInterval = oldWordIntervalsList.get(i);
                final IntervalTier.Interval newWordInterval = newWordIntervalsList.get(i);
                final IntervalTier.Interval[] alignedPhoneIntervals = alignedMap.get(oldWordInterval);
                if (alignedPhoneIntervals == null || alignedPhoneIntervals.length == 0) continue;

                final float oldWordStart = oldWordInterval.getStart();
                final float oldWordEnd = oldWordInterval.getEnd();
                final float newWordStart = newWordInterval.getStart();
                final float newWordEnd = newWordInterval.getEnd();

                if (oldWordStart == newWordStart && oldWordEnd == newWordEnd) {
                    // keep phone intervals the same
                    for(int j = 0; j < alignedPhoneIntervals.length; j++) {
                        final IntervalTier.Interval alignedPhoneInterval = alignedPhoneIntervals[j];
                        final TierString ts = new TierString(alignedPhoneInterval.getLabel());
                        final TierInternalMedia tim = new TierInternalMedia(
                                new InternalMedia(alignedPhoneInterval.getStart(), alignedPhoneInterval.getEnd()));
                        newPhoneIntervals.add(ts);
                        newPhoneIntervals.add(tim);
                    }
                } else {
                    // update phone intervals for word, keeping proportions the same
                    final float oldWordDuration = oldWordEnd - oldWordStart;
                    final float newWordDuration = newWordEnd - newWordStart;
                    for(int j = 0; j < alignedPhoneIntervals.length; j++) {
                        final IntervalTier.Interval alignedPhoneInterval = alignedPhoneIntervals[j];
                        final float phoneStart = alignedPhoneInterval.getStart();
                        final float phoneEnd = alignedPhoneInterval.getEnd();
                        final float startRatio = (phoneStart - oldWordStart) / oldWordDuration;
                        final float endRatio = (phoneEnd - oldWordStart) / oldWordDuration;
                        final float newPhoneStart = newWordStart + (startRatio * newWordDuration);
                        final float newPhoneEnd = newWordStart + (endRatio * newWordDuration);
                        final TierString ts = new TierString(alignedPhoneInterval.getLabel());
                        final TierInternalMedia tim = new TierInternalMedia(
                                new InternalMedia(newPhoneStart, newPhoneEnd));
                        newPhoneIntervals.add(ts);
                        newPhoneIntervals.add(tim);
                    }
                }
            }
        } else {
            final TierAlignment ipaToWorAlignment = TierAligner.alignTiers(ipaTier, wordIntervalsTier);
            final var alignedElementList = ipaToWorAlignment.getAlignedElements();
            for (int alignedIndex = 0; alignedIndex < alignedElementList.size(); alignedIndex++) {
                final var alignedElements = alignedElementList.get(alignedIndex);
                final IPATranscript ipaWord = (IPATranscript) alignedElements.getObj1();
                final Orthography wordIntervalPair = (Orthography) alignedElements.getObj2();
                if (wordIntervalPair == null) continue;
                if (wordIntervalPair.elementAt(wordIntervalPair.length() - 1) instanceof InternalMedia wordInterval) {
                    final float duration = wordInterval.getEndTime() - wordInterval.getStartTime();
                    final IPATranscript audiblePhones = ipaWord != null ? ipaWord.audiblePhones() : new IPATranscript();
                    if (audiblePhones.length() > 0) {
                        final float phoneDuration = duration / audiblePhones.length();
                        for (int phoneIndex = 0; phoneIndex < audiblePhones.length(); phoneIndex++) {
                            final IPAElement ele = audiblePhones.elementAt(phoneIndex);
                            final TierString ipaString = new TierString(ele.toString());
                            final float startTime = wordInterval.getStartTime() + (phoneIndex * phoneDuration);
                            final float endTime = startTime + phoneDuration;
                            final TierInternalMedia phoneInterval = new TierInternalMedia(new InternalMedia(startTime, endTime));
                            newPhoneIntervals.add(ipaString);
                            newPhoneIntervals.add(phoneInterval);
                        }
                    }
                }
                if (alignedIndex != alignedElementList.size() - 1) {
                    newPhoneIntervals.add(new TierString("/"));
                }
            }
        }
        final TierData newPhoneIntervalsTierData = new TierData(newPhoneIntervals);
        phoneIntervalsTier.setValue(newPhoneIntervalsTierData);
        tierEdit.putAdditionalTierChange(phoneIntervalsTier.getName(), oldPhoneIntervals, newPhoneIntervalsTierData);
        tierEdit.fireTierChange(phoneIntervalsTier, oldPhoneIntervals, newPhoneIntervalsTierData);

//        // check to see if we should adjust media segment for the record
//        final Object getPreviousChange = tierEdit.getAdditionalTierChange(SystemTierType.Segment.getName());
//        final boolean isUndo = (getPreviousChange != null);
//        final Orthography oldVal = isUndo ? tierEdit.getNewValue() : tierEdit.getOldValue();
//        final Orthography newVal = isUndo ? tierEdit.getOldValue() : tierEdit.getNewValue();
//        final InternalMedia oldStart = getFirstInternalMedia(oldVal);
//        final InternalMedia oldEnd = getLastInternalMedia(oldVal);
//        final InternalMedia newStart = getFirstInternalMedia(newVal);
//        final InternalMedia newEnd = getLastInternalMedia(newVal);
//        final MediaSegment recordSegment = record.getMediaSegment();
//        // if anything is null or the record segment is null, we can't do anything
//        if (oldStart == null || oldEnd == null || newStart == null || newEnd == null || recordSegment == null)
//            return;
//
//        // if old start time matches record start time and new start time is different, update record start time
//        float recStart = recordSegment.getStartTime();
//        final float currentRecStart = recStart;
//        float recEnd = recordSegment.getEndTime();
//        final float currentRecEnd = recEnd;
//        if (oldStart.getStartTime() == recStart && newStart.getStartTime() != recStart) {
//            recStart = newStart.getStartTime();
//            // if old end time matches record end time and new end time is different, update record end time
//        } else if (oldEnd.getEndTime() == recEnd && newEnd.getEndTime() != recEnd) {
//            recEnd = newEnd.getEndTime();
//        }
//
//        // if start or end time changed, make a new media segment
//        if (tierEdit.isValueAdjusting() && (recStart == currentRecStart && recEnd == currentRecEnd)) return;
//
//        final MediaSegment newRecordSegment = (SessionFactory.newFactory()).createMediaSegment();
//        newRecordSegment.setUnitType(recordSegment.getUnitType());
//        newRecordSegment.setStartTime(recStart);
//        newRecordSegment.setEndTime(recEnd);
//        record.setMediaSegment(newRecordSegment);
//        if(isUndo) {
//            tierEdit.putAdditionalTierChange(SystemTierType.Segment.getName(), recordSegment, newRecordSegment);
//        } else {
//            tierEdit.putAdditionalTierChange(SystemTierType.Segment.getName(), recordSegment, newRecordSegment);
//        }
//        tierEdit.fireTierChange(record.getSegmentTier(), recordSegment, newRecordSegment);
    }

    @Override
    public void installExtension(IExtendable obj) {
        if (obj instanceof Tier<?> tier) {
            if (UserTierType.Wor.getPhonTierName().equals(tier.getName()) && tier.getDeclaredType() == Orthography.class) {
                final UpdatePhointAfterWor extension = new UpdatePhointAfterWor();
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
