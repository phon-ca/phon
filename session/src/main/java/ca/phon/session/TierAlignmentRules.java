package ca.phon.session;

public class TierAlignmentRules {

    public enum TierAlignmentType {
        /**
         * no alignment between word elements (a.k.a, 'flat' tiers)
         */
        None,
        /**
         * Alignment based on WordAlignmentRules
         */
        ByWord
    };

    private final TierAlignmentType type;

    private final WordAlignmentRules wordAlignmentRules;

    public TierAlignmentRules() {
        this(TierAlignmentType.None, null);
    }

    public TierAlignmentRules(TierAlignmentType type, WordAlignmentRules wordAlignmentRules) {
        super();
        this.type = type;
        this.wordAlignmentRules = wordAlignmentRules;
    }

    public TierAlignmentType getType() {
        return type;
    }

    public WordAlignmentRules getWordAlignmentRules() {
        return wordAlignmentRules;
    }

}
