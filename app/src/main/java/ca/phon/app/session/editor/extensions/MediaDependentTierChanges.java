package ca.phon.app.session.editor.extensions;

import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.extensions.Extension;
import ca.phon.extensions.ExtensionProvider;
import ca.phon.extensions.IExtendable;
import ca.phon.session.MediaSegment;
import ca.phon.session.Tier;

@Extension(Tier.class)
public class MediaDependentTierChanges implements TierEdit.DependentTierChanges<MediaSegment>, ExtensionProvider {

    @Override
    public void performDependentTierChanges(TierEdit<MediaSegment> tierEdit) {
        // TODO adjust internal-media semgments on all tiers

    }

    @Override
    public void installExtension(IExtendable obj) {

    }

}
