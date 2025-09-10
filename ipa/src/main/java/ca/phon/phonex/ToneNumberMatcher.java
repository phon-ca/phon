package ca.phon.phonex;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.ToneNumber;

/**
 * Matcher for tone numbers. This matcher matches if the given element
 * is a ToneNumber instance.  This matcher does not check for specific
 * tone numbers, use the <code>:tn("toneNumbers")</code> plugin matcher for that functionality.
 * The phonex for this matcher is <code>\t</code>.
 */
public class ToneNumberMatcher implements PhoneMatcher {

    private final static String phonex = "\\t";

    @Override
    public boolean matches(IPAElement p) {
        return p instanceof ToneNumber;
    }

    @Override
    public boolean matchesAnything() {
        return false;
    }

    @Override
    public String toString() {
        return phonex;
    }

}
