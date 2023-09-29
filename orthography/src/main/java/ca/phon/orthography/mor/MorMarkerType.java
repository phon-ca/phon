package ca.phon.orthography.mor;

import ca.phon.util.Documentation;

/**
 * Mor marker types
 */
@Documentation("https://talkbank.org/manuals/MOR.html#Mor_Markers")
public enum MorMarkerType {
    /**
     *  suffix marker, CHAT equivalent is -suffix
     */
    @Documentation("https://talkbank.org/manuals/MOR.html#Mor_Markers_Suffix")
    Suffix("sfx", "-"),
    /**
     * uffix fusion marker, CHAT equivalent is &amp;suffix
     */
    @Documentation("https://talkbank.org/manuals/MOR.html#Mor_Markers_Suffix_Fusional")
    SuffixFusion("sfxf", "&"),
    /**
     * morphological category, CHAT equivalent is :suffix
     */
    @Documentation("https://talkbank.org/manuals/MOR.html#Mor_Markers_Category")
    MorCategory("mc", ":");

    private final String shorthand;

    private String prefix;

    MorMarkerType(String shorthand, String prefix) {
        this.shorthand = shorthand;
        this.prefix = prefix;
    }

    public String getShorthand() {
        return shorthand;
    }

    public String getPrefix() {
        return this.prefix;
    }

}
