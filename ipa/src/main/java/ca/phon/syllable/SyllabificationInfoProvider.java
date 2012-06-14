package ca.phon.syllable;

import ca.phon.extensions.Extension;
import ca.phon.extensions.ExtensionProvider;
import ca.phon.extensions.IExtendable;
import ca.phon.ipa.IPAElement;

/**
 * Provides the syllable constituent type
 * annotation automagically.
 */
@Extension(IPAElement.class)
public class SyllabificationInfoProvider implements ExtensionProvider {

	@Override
	public void installExtension(IExtendable obj) {
		final IPAElement p = IPAElement.class.cast(obj);
		final SyllabificationInfo scInfo = new SyllabificationInfo(p);
		p.putExtension(SyllabificationInfo.class, scInfo);
	}

}
