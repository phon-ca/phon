package ca.phon.orthography;

public enum OverlapPointType {
    TOP_START('⌈', "top-start"),
    TOP_END('⌉', "top-end"),
    BOTTOM_START('⌊', "bottom-start"),
    BOTTOM_END('⌋', "bottom-end");

    private char ch;

    private String displayName;

    private OverlapPointType(char ch, String displayName) {
        this.ch = ch;
        this.displayName = displayName;
    }

    public char getChar() {
        return ch;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return getChar() + "";
    }

    public static OverlapPointType fromString(String text) {
        for(OverlapPointType type:values()) {
            if(type.toString().equals(text)) {
                return type;
            }
        }
        return null;
    }

    public static OverlapPointType fromDescription(String topBottom, String startEnd) {
        OverlapPointType type = null;
        if("top".equals(topBottom)) {
            if("start".equals(startEnd)) {
                type = OverlapPointType.TOP_START;
            } else if("end".equals(startEnd)) {
                type = OverlapPointType.TOP_END;
            } else {
                throw new IllegalArgumentException(startEnd);
            }
        } else if("bottom".equals(topBottom)) {
            if("start".equals(startEnd)) {
                type = OverlapPointType.BOTTOM_START;
            } else if("end".equals(startEnd)) {
                type = OverlapPointType.BOTTOM_END;
            } else {
                throw new IllegalArgumentException(startEnd);
            }
        } else {
            throw new IllegalArgumentException(topBottom);
        }
        return type;
    }

}
