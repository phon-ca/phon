package ca.phon.phonex;

import ca.phon.fsa.FSAState;
import ca.phon.ipa.IPAElement;

/**
 * Transition for named references in phonex expressions. (e.g., \{1}, \{C}).
 * The group reference may be negated (e.g., \{^1}, \{^C}).
 * When used in aligned expressions, the group reference may be a group which is not
 * currently matched.  In this case, the fsa will initially follow the transition and
 * defer the match until the information is available.  This is possible because aligned
 * expressions will never increase the tape position and execute on a different tape consisting
 * of the aligned elements.
 */
public class NamedReferenceTransition extends PhonexTransition {

    private final boolean negated;

    private final int groupIndex;

    private final String groupName;

    public NamedReferenceTransition(int groupIndex, String groupName, boolean negated) {
        super(null);
        this.groupIndex = groupIndex;
        this.groupName = groupName;
        this.negated = negated;
    }

    public int getGroupIndex() {
        return groupIndex;
    }

    public String getGroupName() {
        return groupName;
    }

    public boolean isNegated() {
        return negated;
    }

    @Override
    public boolean follow(FSAState<IPAElement> currentState) {
        return false;
    }

    @Override
    public String toString() {
        return "\\{" + (negated ? "^" : "") + (groupIndex >= 0 ? groupIndex : groupName) + "}";
    }

}
