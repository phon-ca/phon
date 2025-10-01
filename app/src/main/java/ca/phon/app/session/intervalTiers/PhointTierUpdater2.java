package ca.phon.app.session.intervalTiers;

import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.extensions.Extension;
import ca.phon.extensions.ExtensionProvider;
import ca.phon.extensions.IExtendable;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.InternalMedia;
import ca.phon.orthography.Orthography;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.Tier;
import ca.phon.session.TierDescription;
import ca.phon.session.UserTierType;
import ca.phon.session.alignment.TierAligner;
import ca.phon.session.alignment.TierAlignment;
import ca.phon.session.tierdata.TierData;
import ca.phon.session.tierdata.TierElement;
import ca.phon.session.tierdata.TierInternalMedia;
import ca.phon.session.tierdata.TierString;

import java.util.ArrayList;
import java.util.List;

/**
 * Update text and intervals of the pho interval tier (%xphoint) when text of the word intervals
 * tier (%wor) changes.
 */
@Extension(Tier.class)
public class PhointTierUpdater2 implements TierEdit.DependentTierChanges<Orthography>, ExtensionProvider {

    @Override
    public void performDependentTierChanges(TierEdit<Orthography> tierEdit) {
//        if(tierEdit.isValueAdjusting()) return;
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

        final TierData oldPhoneIntervals = phoneIntervalsTier.hasValue() ? phoneIntervalsTier.getValue() : new TierData();
        final List<TierElement> newPhoneIntervals = new ArrayList<>();
        final TierAlignment ipaToWorAlignment = TierAligner.alignTiers(ipaTier, wordIntervalsTier);
        final var alignedElementList = ipaToWorAlignment.getAlignedElements();
        for(int alignedIndex = 0; alignedIndex < alignedElementList.size(); alignedIndex++) {
            final var alignedElements = alignedElementList.get(alignedIndex);
            final IPATranscript ipaWord = (IPATranscript) alignedElements.getObj1();
            final Orthography wordIntervalPair = (Orthography) alignedElements.getObj2();
            if(wordIntervalPair == null) continue;
            if(wordIntervalPair.elementAt(wordIntervalPair.length()-1) instanceof InternalMedia wordInterval) {
                final float duration = wordInterval.getEndTime() - wordInterval.getStartTime();
                final IPATranscript audiblePhones = ipaWord != null ? ipaWord.audiblePhones() : new IPATranscript();
                if(audiblePhones.length() > 0) {
                    final float phoneDuration = duration / audiblePhones.length();
                    for(int phoneIndex = 0; phoneIndex < audiblePhones.length(); phoneIndex++) {
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
            if(alignedIndex != alignedElementList.size() - 1) {
                newPhoneIntervals.add(new TierString("/"));
            }
        }
        final TierData newPhoneIntervalsTierData = new TierData(newPhoneIntervals);
        phoneIntervalsTier.setValue(newPhoneIntervalsTierData);
        tierEdit.putAdditionalTierChange(phoneIntervalsTier.getName(), newPhoneIntervalsTierData);
        tierEdit.fireTierChange(phoneIntervalsTier, oldPhoneIntervals, newPhoneIntervalsTierData);
    }

    @Override
    public void installExtension(IExtendable obj) {
        if (obj instanceof Tier<?> tier) {
            if (UserTierType.Wor.getPhonTierName().equals(tier.getName()) && tier.getDeclaredType() == Orthography.class) {
                final PhointTierUpdater2 extension = new PhointTierUpdater2();
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
