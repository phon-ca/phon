package ca.phon.session;

public final class TierAlignmentRules {

    public enum TierAlignmentType {
        /**
         * no alignment between word elements (a.k.a, 'flat' tiers)
         */
        None,
        /**
         * Alignment based on TypeAlignmentRules
         */
        ByType,
        /**
         * Use for user-defined tiers which include parameters
         * for identifying subtypes in the user data
         */
        ByTypeThenSubType;
    };

    private final TierAlignmentType type;

    private final TypeAlignmentRules typeAlignmentRules;

    private final String[] subtypeDelim;

    private final String subtypeExpr;

    public TierAlignmentRules() {
        this(TierAlignmentType.None, null, null, null);
    }

    public TierAlignmentRules(TypeAlignmentRules typeAlignmentRules) {
        this(TierAlignmentType.ByType, typeAlignmentRules, null, null);
    }

    public TierAlignmentRules(TypeAlignmentRules typeAlignmentRules, String[] subtypeDelim) {
        this(TierAlignmentType.ByTypeThenSubType, typeAlignmentRules, subtypeDelim, null);
    }

    public TierAlignmentRules(TypeAlignmentRules typeAlignmentRules, String subtypeExpr) {
        this(TierAlignmentType.ByTypeThenSubType, typeAlignmentRules, null, subtypeExpr);
    }

    private TierAlignmentRules(TierAlignmentType type, TypeAlignmentRules typeAlignmentRules,
                               String[] subtypeDelim, String subtypeExpr) {
        super();
        this.type = type;
        this.typeAlignmentRules = typeAlignmentRules;
        this.subtypeDelim = subtypeDelim;
        this.subtypeExpr = subtypeExpr;
    }

    public TierAlignmentType getType() {
        return type;
    }

    public TypeAlignmentRules getWordAlignmentRules() {
        return typeAlignmentRules;
    }

}
