package ca.phon.app.session.editor.search;

import ca.phon.phonex.PhonexMatcher;
import ca.phon.util.Range;

import java.util.regex.Matcher;

/**
 * Result of a find operation. This contains the expression used to find the
 * result and the range of the result within the transcript.  It also includes
 * the optional Java regex matcher and Phonex matcher for the result.
 *
 * @param range the range of the result within the transcript, if null there is no match
 * @param matcher the Java regex matcher for the result (may be null)
 * @param phonexMatcher the Phonex matcher for the result (may be null)
 */
public record FindExprMatch(Range range, Matcher matcher, PhonexMatcher phonexMatcher) {

    public static FindExprMatch empty() {
        return new FindExprMatch(null, null, null);
    }

    public boolean hasMatch() {
        return range != null;
    }

    public boolean isPlainMatch() {
        return matcher == null && phonexMatcher == null;
    }

    public boolean isRegexMatch() {
        return matcher != null && phonexMatcher == null;
    }

    public boolean isPhonexMatch() {
        return phonexMatcher != null;
    }

}
