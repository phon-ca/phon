package ca.phon.orthography;

public enum GroupAnnotationType {
    ALTERNATIVE("=?", "alternative"),
    COMMENTS("%", "comments"),
    EXPLANATION("=", "explanation"),
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
