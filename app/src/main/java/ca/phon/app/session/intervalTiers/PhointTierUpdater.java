package ca.phon.app.session.intervalTiers;

import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.extensions.Extension;
import ca.phon.extensions.ExtensionProvider;
import ca.phon.extensions.IExtendable;
import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.alignment.TierAligner;
import ca.phon.session.alignment.TierAlignment;
import ca.phon.session.tierdata.TierData;

/**
 * Update text and intervals of the "IPA Actual" (%pho) tier when text of Orthography
 * changes.
 */
@Extension(Tier.class)
public class PhointTierUpdater implements TierEdit.DependentTierChanges<IPATranscript>, ExtensionProvider {

    @Override
    public void performDependentTierChanges(TierEdit<IPATranscript> tierEdit) {
        if(tierEdit.isValueAdjusting()) return;
        final Session session = tierEdit.getSession();
        // check for the phone intervals tier
        final TierDescription phoTierDesc = session.getUserTiers()
                .stream()
                .filter(td -> UserTierType.PhoneIntervals.getPhonTierName().equals(td.getName()))
                .findAny().orElse(null);
        if(phoTierDesc == null) return;
        // check for word intervals tier
        final TierDescription worTierDesc = session.getUserTiers()
                .stream()
                .filter(td -> UserTierType.Wor.getPhonTierName().equals(td.getName()))
                .findAny().orElse(null);
        if(worTierDesc == null) return;

        final Record record = tierEdit.getRecord();

        final Tier<IPATranscript> ipaTier = tierEdit.getTier();

        final Tier<Orthography> wordIntervalsTier = record.getTier(worTierDesc.getName(), Orthography.class);
        final Tier<TierData> phoneIntervalsTier = record.getTier(phoTierDesc.getName(), TierData.class);
        final TierData oldPhoneIntervals = phoneIntervalsTier.hasValue() ? phoneIntervalsTier.getValue() : new TierData();

        final TierAlignment tierAligner = TierAligner.alignTiers(wordIntervalsTier, ipaTier);
    }

    @Override
    public void installExtension(IExtendable obj) {
        if (obj instanceof Tier<?> tier) {
            if (SystemTierType.IPAActual.getName().equals(tier.getName()) && tier.getDeclaredType() == IPATranscript.class) {
                final PhointTierUpdater extension = new PhointTierUpdater();
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
