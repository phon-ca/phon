package ca.phon.ipadictionary.spi;

import ca.phon.extensions.Extension;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.ipadictionary.exceptions.IPADictionaryExecption;

@Extension(IPADictionary.class)
public interface ClearEntries {
	
	public void clear()
		throws IPADictionaryExecption;

}
