package ca.phon;

public enum GemType {
    Begin("Bg"),
    End("Eg"),
    Lazy("G");

    private String prefix;

    private GemType(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() { return prefix; }
}
