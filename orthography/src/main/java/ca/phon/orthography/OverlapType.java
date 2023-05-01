package ca.phon.orthography;

public enum OverlapType {
    OVERLAP_PRECEEDS("[<", "overlap preceeds"),
    OVERLAP_FOLLOWS("[>", "overlap follows");

    private String prefix;

    private String displayName;

    private OverlapType(String prefix, String displayName) {
        this.prefix = prefix;
        this.displayName = displayName;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDisplayName() {
        return displayName;
    }

}
