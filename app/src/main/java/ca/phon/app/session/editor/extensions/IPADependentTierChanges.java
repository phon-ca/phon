package ca.phon.app.session.editor.extensions;

import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.extensions.Extension;
import ca.phon.extensions.ExtensionProvider;
import ca.phon.extensions.IExtendable;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.*;

/**
 * Install default dependent tier changes for IPA tiers.  Default behaviour is to update
 * alignment when an IPA value is modified.
 */
@Extension(Tier.class)
public class IPADependentTierChanges implements TierEdit.DependentTierChanges<IPATranscript>, ExtensionProvider {

    @Override
    public void performDependentTierChanges(TierEdit<IPATranscript> tierEdit) {
        if(tierEdit.getRecord() == null) return;
        // if tier is IPATarget or IPAActual update default phone alignment
        // TODO update potential user-defined tier alignments
        final SystemTierType systemTierType = SystemTierType.tierFromString(tierEdit.getTier().getName());
        if(systemTierType == SystemTierType.IPATarget || systemTierType == SystemTierType.IPAActual) {
            PhoneAlignment pm = (PhoneAlignment) tierEdit.getAdditionalTierChange(SystemTierType.PhoneAlignment.getName());
            if(pm == null) {
                // update alignment
                pm = PhoneAlignment.fromTiers(tierEdit.getRecord().getIPATargetTier(), tierEdit.getRecord().getIPAActualTier());
            }
            final PhoneAlignment oldVal = tierEdit.getRecord().getPhoneAlignment();
            tierEdit.getRecord().setPhoneAlignment(pm);
            tierEdit.putAdditionalTierChange(SystemTierType.PhoneAlignment.getName(), tierEdit);
            // fire event for phone alignment tier change
            tierEdit.fireTierChange(tierEdit.getRecord().getPhoneAlignmentTier(), oldVal, pm);
        }
    }

    @Override
    public void installExtension(IExtendable obj) {
        if(obj instanceof Tier<?> tier) {
            if(tier.getDeclaredType() != IPATranscript.class) return;
            @SuppressWarnings("unchecked")
            final Tier<IPATranscript> ipaTier = (Tier<IPATranscript>) tier;
            final TierEdit.DependentTierChanges<IPATranscript> otherChanges = ipaTier.getExtension(IPADependentTierChanges.class);
            if(otherChanges == null)
                tier.putExtension(TierEdit.DependentTierChanges.class, this);
            else
                tier.putExtension(TierEdit.DependentTierChanges.class, new TierEdit.DependentTierChangeChain<>(this, otherChanges));
        }
    }

}
