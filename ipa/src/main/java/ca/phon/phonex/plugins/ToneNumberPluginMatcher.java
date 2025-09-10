package ca.phon.phonex.plugins;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.ToneNumber;
import ca.phon.phonex.PhoneMatcher;

/**
 * Provides tone number matching functionality. This matcher
 * matches if the tone number of a phone is in the list of allowed
 * tone numbers and not in the list of unallowed tone numbers.
 * null values for either list may be provided and will match appropriately
 * if no tone is specified for a phone.
 */
public class ToneNumberPluginMatcher implements PhoneMatcher {

    /**
     * The list of allowed tone numbers
     */
    private ToneNumber[] toneNumbers;

    /**
     * The list of unallowed tone numbers
     */
    private ToneNumber[] notToneNumbers;

    public ToneNumberPluginMatcher(ToneNumber[] toneNumbers, ToneNumber[] notToneNumbers) {
        super();
        this.toneNumbers = toneNumbers;
        this.notToneNumbers = notToneNumbers;
    }


    @Override
    public boolean matches(IPAElement p) {
        if(matchesAnything()) return true;
        final ToneNumber phoneTone = p.tone();
        final String phoneToneStr = (phoneTone != null ? phoneTone.getText() : "");
        for(int i = 0; i < toneNumbers.length; i++) {
            final String toneStr = (toneNumbers[i] != null ? toneNumbers[i].getText() : "");
            if(toneStr.equals(phoneToneStr)) {
                // check not tone numbers
                if(notToneNumbers != null) {
                    for(int j = 0; j < notToneNumbers.length; j++) {
                        final String notToneStr = (notToneNumbers[j] != null ? notToneNumbers[j].getText() : "");
                        if(notToneStr.equals(phoneToneStr)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matchesAnything() {
        // return true if both tone number lists are empty
        return (toneNumbers == null || toneNumbers.length == 0) &&
                (notToneNumbers == null || notToneNumbers.length == 0);
    }

}
