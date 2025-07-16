package ca.phon.app.session.editor.search;

import ca.phon.phonex.PhonexMatcher;
import ca.phon.session.position.TranscriptElementRange;

import java.util.regex.Matcher;

/**
 * Result of a find operation.  This contains the expression used to find the
 * result and the range of the result within the transcript.
 *
 * @param expr the expression used to find the result
 * @param range the range of the result within the transcript
 * @param matcher the Java regex matcher for the result (may be null)
 * @param phonexMatcher the Phonex matcher for the result (may be null)
 */
public record FindResult(FindExpr expr, TranscriptElementRange range, Matcher matcher, PhonexMatcher phonexMatcher) {

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
