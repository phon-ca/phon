package ca.phon.ipa;

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
                           ToneNumber tone) {

    SyllableInfo() {
        this(SyllableConstituentType.UNKNOWN, false, SyllableStress.NoStress, -1, false, -1, -1, null);
    }

    SyllableInfo(SyllableConstituentType constituentType) {
        this(constituentType, false, SyllableStress.NoStress, -1, false, -1, -1, null);
    }

    SyllableInfo(SyllableConstituentType constituentType, boolean isDiphthong) {
        this(constituentType, isDiphthong, SyllableStress.NoStress, -1, false, -1, -1, null);
    }

    SyllableInfo(SyllableConstituentType constituentType, SyllableStress stress) {
        this(constituentType, false, stress, -1, false, -1, -1, null);
    }

    /**
     * Create a new builder instance with default values.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a builder initialized with the values from this SyllableInfo.
     *
     * @return builder pre-populated with this record's values
     */
    public Builder toBuilder() {
        return new Builder()
                .constituentType(this.constituentType)
                .isDiphthong(this.isDiphthong)
                .stress(this.stress)
                .syllableIndex(this.syllableIndex)
                .segregated(this.segregated)
                .sonority(this.sonority)
                .sonorityDistance(this.sonorityDistance)
                .tone(this.tone);
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

    /**
     * Builder for {@link SyllableInfo}. All fields are optional and default to values
     * used by the no-arg constructor.
     */
    public static final class Builder {
        private SyllableConstituentType constituentType = SyllableConstituentType.UNKNOWN;
        private boolean isDiphthong = false;
        private SyllableStress stress = SyllableStress.NoStress;
        private int syllableIndex = -1;
        private boolean segregated = false;
        private int sonority = -1;
        private int sonorityDistance = -1;
        private ToneNumber tone = null;

        private Builder() { }

        public Builder constituentType(SyllableConstituentType constituentType) {
            this.constituentType = (constituentType != null ? constituentType : SyllableConstituentType.UNKNOWN);
            return this;
        }

        public Builder isDiphthong(boolean isDiphthong) {
            this.isDiphthong = isDiphthong; return this; }

        public Builder stress(SyllableStress stress) {
            this.stress = (stress != null ? stress : SyllableStress.NoStress); return this; }

        public Builder syllableIndex(int syllableIndex) { this.syllableIndex = syllableIndex; return this; }

        public Builder segregated(boolean segregated) { this.segregated = segregated; return this; }

        public Builder sonority(int sonority) { this.sonority = sonority; return this; }

        public Builder sonorityDistance(int sonorityDistance) { this.sonorityDistance = sonorityDistance; return this; }

        public Builder tone(ToneNumber tone) { this.tone = tone; return this; }

        /**
         * Build the immutable {@link SyllableInfo} record.
         *
         * @return new SyllableInfo instance
         */
        public SyllableInfo build() {
            return new SyllableInfo(constituentType, isDiphthong, stress, syllableIndex, segregated, sonority, sonorityDistance, tone);
        }
    }
}
