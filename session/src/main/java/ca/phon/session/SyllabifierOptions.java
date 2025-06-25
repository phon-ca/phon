package ca.phon.session;

import ca.phon.ipa.IPATranscript;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierLibrary;

/**
 * Useful methods for finding the correct syllabifier in different situations
 *
 */
public final class SyllabifierOptions {

    /**
     * IPA Transcript tier parameters key used for custom syllabifier settings
     */
    public final static String TIER_SYLLABIFIER = "syllabifier";

    /**
     * Return syllabifier based on the following rules:
     * <ul>
     *     <ul>If tier parameters has a value for key TIER_SYLLABIFIER, use syllabifier for that language if found</ul>
     *     <ul>If record language is set, attempt to find syllabifier for that language</ul>
     *     <ul>If session language is set, attempt to find syllabifier for that language</ul>
     *     <ul>Finally, return default syllabifier</ul>
     * </ul>
     *
     * @param session session, may be null
     * @param record record, may be null
     * @param tier tier, may be null
     * @return syllabifier based on rules
     */
    public static Syllabifier findSyllabifier(Session session, Record record, Tier<IPATranscript> tier) {
        return findSyllabifier(session, record, tier != null ? tier.getName() : null);
    }

    /**
     * Return syllabifier based on the following rules:
     * <ul>
     *     <ul>If tier parameters has a value for key TIER_SYLLABIFIER, use syllabifier for that language if found</ul>
     *     <ul>If record language is set, attempt to find syllabifier for that language</ul>
     *     <ul>If session language is set, attempt to find syllabifier for that language</ul>
     *     <ul>Finally, return default syllabifier</ul>
     * </ul>
     *
     * @param session session, may be null
     * @param record record, may be null
     * @param tierName tier name, may be null
     * @return syllabifier based on rules
     */
    public static Syllabifier findSyllabifier(Session session, Record record, String tierName) {
        final SyllabifierLibrary library = SyllabifierLibrary.getInstance();
        Syllabifier retVal = null;
        final TierDescription td = session != null ? session.getTier(tierName) : null;
        if(td != null && td.getTierParameters().containsKey(TIER_SYLLABIFIER)) {
            retVal = library.getSyllabifierForLanguage(td.getTierParameters().get(TIER_SYLLABIFIER));
        }
        if(retVal == null) {
            if (record != null && record.getLanguage() != null) {
                retVal = library.getSyllabifierForLanguage(record.getLanguage());
            }
        }
        if(retVal == null) {
            if(session != null && !session.getLanguages().isEmpty()) {
                retVal = library.getSyllabifierForLanguage(session.getLanguages().get(0));
            }
        }
        if(retVal == null) {
            retVal = library.defaultSyllabifier();
        }
        return retVal;
    }

    /**
     * Gets the custom syllabifier language for the given tier in the provided session.
     * If the tier does not have a custom syllabifier set, this will return null.
     *
     * @param session session to get syllabifier for
     * @param tierName name of tier to get syllabifier for
     *
     * @return syllabifier language for tier, or null if not set
     */
    public static String getSyllabifierForTier(Session session, String tierName) {
        if (session == null || tierName == null) return null;
        final TierDescription td = session.getTier(tierName);
        if (td != null && td.getTierParameters().containsKey(TIER_SYLLABIFIER)) {
            return td.getTierParameters().get(TIER_SYLLABIFIER);
        }
        return null;
    }

    /**
     * Set syllabifier language for tier in the provided session.  This will set the TIER_SYLLABIFIER
     * property in the tier parameters for the session.
     *
     * @param session session to set syllabifier for
     * @param tierName name of tier to set syllabifier for
     * @param syllabifier language of syllabifier to set (may be null)
     */
    public static void setSyllabifierForTier(Session session, String tierName, Syllabifier syllabifier) {
        setSyllabifierForTier(session, tierName, syllabifier != null ? syllabifier.getLanguage().toString() : null);
    }

    /**
     * Set syllabifier language for tier in the provided session.  This will set the TIER_SYLLABIFIER
     * property in the tier parameters for the session.
     *
     * @param session session to set syllabifier for
     * @param tierName name of tier to set syllabifier for
     * @param syllabifierLanguage language of syllabifier to set (may be null)
     */
    public static void setSyllabifierForTier(Session session, String tierName, String syllabifierLanguage) {
        if (session == null || tierName == null) return;
        final TierDescription td = session.getTier(tierName);
        if (td != null) {
            td.getTierParameters().put(TIER_SYLLABIFIER, syllabifierLanguage);
        }
    }

}
