package ca.phon.syllable;

import ca.phon.extensions.Extension;
import ca.phon.extensions.ExtensionProvider;
import ca.phon.extensions.IExtendable;
import ca.phon.ipa.phone.Phone;

/**
 * Provides the syllable constituent type
 * annotation automagically.
 */
@Extension(Phone.class)
public class SyllabificationInfoProvider implements ExtensionProvider {

	@Override
	public void installExtension(IExtendable obj) {
		final Phone p = Phone.class.cast(obj);
		final SyllabificationInfo scInfo = new SyllabificationInfo(p);
		p.putExtension(SyllabificationInfo.class, scInfo);
	}

}
