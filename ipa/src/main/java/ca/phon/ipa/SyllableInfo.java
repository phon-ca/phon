package ca.phon.ipa;

import ca.phon.syllable.SyllableVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Syllable information for {@link IPAElement}s.
 *
 * @param constituentType syllable constituent type
 * @param isDiphthong true if element is a diphthong member (only valid for SyllableConstituentType.NUCLEUS)
 * @param stress syllable stress
 * @param syllableIndex index of syllable in word/phrase
 */
public record SyllableInfo(SyllableConstituentType constituentType,
                           boolean isDiphthong,
                           SyllableStress stress,
                           int syllableIndex,
                           boolean segregated,
                           int sonority,
                           int sonorityDistance,
                           ToneMelody tone) {

    public SyllableInfo() {
        this(SyllableConstituentType.UNKNOWN, false, SyllableStress.NoStress, -1, false, -1, -1, null);
    }

    public SyllableInfo(SyllableConstituentType constituentType) {
        this(constituentType, false, SyllableStress.NoStress, -1, false, -1, -1, null);
    }

    public SyllableInfo(SyllableConstituentType constituentType, SyllableStress stress) {
        this(constituentType, false, stress, -1, false, -1, -1, null);
    }

    /**
     * Return a list of elements where the syllable info has been set.  It's assumed that syllable constituent
     * type information has been applied to the elements already.
     *
     * @param elems list of elements
     * @return list of elements with syllable info set - these elements have been cloned with
     *  new SyllableInfo information
     */
    public static List<IPAElement> annotateElements(List<IPAElement> elems) {
        final SyllableInfoVisitor visitor = new SyllableInfoVisitor();
        for (IPAElement elem : elems) {
            elem.accept(visitor);
        }
        return visitor.toIPATranscript().toList();
    }

}
