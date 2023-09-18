package ca.phon.orthography;

import javax.swing.text.html.HTML;

public enum TagMarkerType {
    @CHATReference("https://talkbank.org/manuals/CHAT.html#Comma")
    COMMA(',', "comma"),
    @CHATReference({"https://talkbank.org/manuals/CHAT.html#Tag_Satellite_Marker","https://talkbank.org/manuals/MOR.html#MorphologicalTagMarker_Tag"})
    VOCATIVE('‡', "vocative"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#Tag_Satellite_Marker")
    TAG('„', "tag");

    private char ch;

    private String displayName;

    private TagMarkerType(char ch, String displayName) {
        this.ch = ch;
        this.displayName = displayName;
    }

    public char getChar() {
        return ch;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TagMarkerType fromChar(char ch) {
        for(TagMarkerType v:values()) {
            if(v.getChar() == ch) return v;
        }
        return null;
    }

    public static TagMarkerType fromString(String text) {
        TagMarkerType retVal = null;

        for(TagMarkerType v:values()) {
            if((v.getChar() + "").equals(text) || v.getDisplayName().equals(text)) {
                retVal = v;
                break;
            }
        }

        return retVal;
    }

    @Override
    public String toString() {
        return getChar() + "";
    }

}
