package ca.phon.autotranscribe;

import ca.phon.ipadictionary.IPADictionary;
import ca.phon.ipadictionary.IPADictionaryLibrary;
import ca.phon.util.Language;

/**
 * IPADictionary Source for automatic transcriptions
 *
 *
 */
public class IPADictionaryAutoTranscribeSource implements AutoTranscribeSource {

    private final IPADictionary ipaDictionary;

    public IPADictionaryAutoTranscribeSource(IPADictionary ipaDictionary) {
        this.ipaDictionary = ipaDictionary;
    }

    public IPADictionaryAutoTranscribeSource(String lang) {
        this(Language.parseLanguage(lang));
    }

    public IPADictionaryAutoTranscribeSource(Language lang) {
        final IPADictionaryLibrary library = IPADictionaryLibrary.getInstance();
        this.ipaDictionary = library.availableLanguages().contains(lang)
                ? library.dictionariesForLanguage(lang).get(0) : library.defaultDictionary();
    }

    public String[] lookup(String text) {
        return ipaDictionary.lookup(text);
    }

}
