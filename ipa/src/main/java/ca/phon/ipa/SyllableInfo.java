package ca.phon.ipa;

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
                           int syllableIndex) {

    public SyllableInfo() {
        this(SyllableConstituentType.UNKNOWN, false, SyllableStress.NoStress, -1);
    }

    public SyllableInfo(SyllableConstituentType constituentType) {
        this(constituentType, false, SyllableStress.NoStress, -1);
    }

    public SyllableInfo(SyllableConstituentType constituentType, SyllableStress stress) {
        this(constituentType, false, stress, -1);
    }

}
