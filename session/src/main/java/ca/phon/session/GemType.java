package ca.phon.session;

public enum GemType {
    Begin("Begin gem", "@Bg"),
    End("End gem", "@Eg"),
    Lazy("Gem", "@G");

    private String phonTierName;

    private String chatTierName;

    private GemType(String phonTierName, String chatTierName) {
        this.phonTierName = phonTierName;
        this.chatTierName = chatTierName;
    }

    public String getChatTierName() { return chatTierName; }

    public String getPhonTierName() { return phonTierName; }

}
