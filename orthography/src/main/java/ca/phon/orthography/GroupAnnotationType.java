package ca.phon.orthography;

import ca.phon.util.Documentation;

/**
 * Type of group annotation on main line.
 */
@Documentation("https://talkbank.org/manuals/CHAT.html#DependentOnMain_Scope")
public enum GroupAnnotationType {
    @Documentation("https://talkbank.org/manuals/CHAT.html#AlternativeTranscription_Scope")
    ALTERNATIVE("=?", "alternative"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#Comment_Scope")
    COMMENTS("%", "comments"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#Explanation_Scope")
    EXPLANATION("=", "explanation"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#ParalinguisticMaterial_Scope")
    PARALINGUISTICS("=!", "paralinguistics");

    private String prefix;

    private String displayName;

    private GroupAnnotationType(String prefix, String displayName) {
        this.prefix = prefix;
        this.displayName = displayName;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public GroupAnnotationType fromPrefix(String prefix) {
        for(GroupAnnotationType type:values()) {
            if(type.getPrefix().equals(prefix)) {
                return type;
            }
        }
        return null;
    }

}
