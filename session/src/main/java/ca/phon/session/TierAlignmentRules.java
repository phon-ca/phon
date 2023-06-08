package ca.phon.session;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Create alignment rules for Orthography tier
     *
     * @return orthography alignment rules (default: no alignment)
     */
    public static TierAlignmentRules orthographyTierRules() {
        final TierAlignmentRules retval = new TierAlignmentRules();
        return retval;
    }

    /**
     * Create alignment rules for IPATranscript tiers
     *
     * @return default ipa alignment rules
     */
    public static TierAlignmentRules ipaTierRules() {
        final List<TypeAlignmentRules.AlignableType> alignableTypes =
                List.of(TypeAlignmentRules.AlignableType.Word, TypeAlignmentRules.AlignableType.Pause, TypeAlignmentRules.AlignableType.PhoneticGroup);
        final TypeAlignmentRules typeAlignmentRules = new TypeAlignmentRules(alignableTypes,
                true, false, true, false, false);
        return new TierAlignmentRules(typeAlignmentRules);
    }

    /**
     * Create alignment rules for notes tier
     *
     * @return default notes alignment rules (none)
     */
    public static TierAlignmentRules notesTierRules() {
        final TierAlignmentRules retval = new TierAlignmentRules();
        return retval;
    }

    /**
     * Create default alignment rules for user-defined tiers
     *
     * @param default user defined tier rules
     */
    public static TierAlignmentRules userTierRules() {
        final List<TypeAlignmentRules.AlignableType> alignableTypes = List.of(TypeAlignmentRules.AlignableType.Word);
        final TypeAlignmentRules typeAlignmentRules = new TypeAlignmentRules(alignableTypes,
                false, false, false, false, false);
        return new TierAlignmentRules(typeAlignmentRules);
    }

}
