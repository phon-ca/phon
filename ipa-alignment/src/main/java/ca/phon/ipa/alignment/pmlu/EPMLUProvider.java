package ca.phon.ipa.alignment.pmlu;

import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.extensions.Extension;
import ca.phon.extensions.ExtensionProvider;
import ca.phon.extensions.IExtendable;

@Extension(PhoneMap.class)
public class EPMLUProvider implements ExtensionProvider {

	@Override
	public void installExtension(IExtendable obj) {
		final PhoneMap pm = (PhoneMap)obj;
		pm.putExtension(EPMLU.class, new EPMLU(pm));
	}

}
