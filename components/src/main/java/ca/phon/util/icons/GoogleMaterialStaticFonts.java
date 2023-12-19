package ca.phon.util.icons;

public enum GoogleMaterialStaticFonts {
    Regular("MaterialIcons-Regular", "data/icons/MaterialIcons-Regular.ttf", "data/icons/MaterialIcons-Regular.codepoints"),
    Outlined("MaterialIconsOutlined-Regular", "data/icons/MaterialIconsOutlined-Regular.otf", "data/icons/MaterialIconsOutlined-Regular.codepoints"),
    Round("MaterialIconsRound-Regular", "data/icons/MaterialIconsRound-Regular.otf", "data/icons/MaterialIconsRound-Regular.codepoints"),
    Sharp("MaterialIconsSharp-Regular", "data/icons/MaterialIconsSharp-Regular.otf", "data/icons/MaterialIconsSharp-Regular.codepoints");

    private final String fontName;

    private final String fontFile;

    private final String codepointFile;

    private GoogleMaterialStaticFonts(String fontName, String fontFile, String codepointFile) {
        this.fontName = fontName;
        this.fontFile = fontFile;
        this.codepointFile = codepointFile;
    }

    public String getFontName() {
        return this.fontName;
    }

    public String getFontFile() {
        return this.fontFile;
    }

    public String getCodepointFile() {
        return this.codepointFile;
    }

    public static GoogleMaterialStaticFonts fromString(String fontName) {
        for(GoogleMaterialStaticFonts font:values()) {
            if(font.getFontName().equals(fontName)) {
                return font;
            }
        }
        return null;
    }
}
