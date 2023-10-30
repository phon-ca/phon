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
        final SyllabifierLibrary library = SyllabifierLibrary.getInstance();
        Syllabifier retVal = null;
        if(tier != null && tier.getTierParameters().containsKey(TIER_SYLLABIFIER)) {
            retVal = library.getSyllabifierForLanguage(tier.getTierParameters().get(TIER_SYLLABIFIER));
        } else if(record != null && record.getLanguage() != null) {
            retVal = library.getSyllabifierForLanguage(record.getLanguage());
        } else if(session != null && !session.getLanguages().isEmpty()) {
            retVal = library.getSyllabifierForLanguage(session.getLanguages().get(0));
        } else {
            retVal = library.defaultSyllabifier();
        }
        return retVal;
    }

    /* Old method */
//    /**
//     * Get the correct syllabifier (or default) for given ipa transcript tier.
//     * @param tier
//     * @return tier syllabifier
//     */
//    private Syllabifier getSyllabifier(Session session, Tier<IPATranscript> tier) {
//        Syllabifier retVal = null;
//        // new method
//        // TODO move this key somewhere sensible, currently unused
//        if(tier.getTierParameters().containsKey("syllabifier")) {
//            try {
//                final Language lang = Language.parseLanguage(tier.getTierParameters().get("syllabifier"));
//                if(lang != null && SyllabifierLibrary.getInstance().availableSyllabifierLanguages().contains(lang)) {
//                    retVal = SyllabifierLibrary.getInstance().getSyllabifierForLanguage(lang);
//                }
//            } catch (IllegalArgumentException e) {
//                LogUtil.warning(e);
//            }
//        }
//        if(retVal == null) {
//            // old method
//            final SyllabifierInfo info = session.getExtension(SyllabifierInfo.class);
//            if (info != null) {
//                final Language lang = info.getSyllabifierLanguageForTier(tier.getName());
//                if (lang != null && SyllabifierLibrary.getInstance().availableSyllabifierLanguages().contains(lang)) {
//                    retVal = SyllabifierLibrary.getInstance().getSyllabifierForLanguage(lang);
//                }
//            }
//        }
//        if(retVal == null) {
//            retVal = SyllabifierLibrary.getInstance().defaultSyllabifier();
//        }
//        return retVal;
//    }

}
