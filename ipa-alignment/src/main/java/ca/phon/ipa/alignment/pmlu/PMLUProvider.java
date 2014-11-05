package ca.phon.ipa.alignment.pmlu;

import ca.phon.extensions.Extension;
import ca.phon.extensions.ExtensionProvider;
import ca.phon.extensions.IExtendable;
import ca.phon.ipa.alignment.PhoneMap;

/**
 * Attaches a new PMLU object to every PhoneMap when initialized.
 * 
 */
@Extension(PhoneMap.class)
public class PMLUProvider implements ExtensionProvider {

	@Override
	public void installExtension(IExtendable obj) {
		// we are ensured that obj will be of the correct type
		final PhoneMap pm = (PhoneMap)obj;
		pm.putExtension(PMLU.class, new PMLU(pm));
	}

}
