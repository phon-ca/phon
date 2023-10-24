package ca.phon.app.session.editor.undo;

import ca.phon.extensions.Extension;
import ca.phon.extensions.ExtensionProvider;
import ca.phon.extensions.IExtendable;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.Tier;

/**
 * Install default dependent tier changes for IPA tiers.  Default behaviour is to update
 * alignment when an IPA value is modified.
 */
@Extension(Tier.class)
public class IPADependentTierChanges implements TierEdit.DependentTierChanges<IPATranscript>, ExtensionProvider {

    @Override
    public void performDependentTierChanges(TierEdit<IPATranscript> tierEdit) {

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
