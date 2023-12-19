package ca.phon.util.icons;

public enum GoogleMaterialFonts {
    Outlined("MaterialIconsOutlined", "data/icons/MaterialSymbolsOutlined[FILL,GRAD,opsz,wght].ttf", "data/icons/MaterialSymbolsOutlined[FILL,GRAD,opsz,wght].codepoints"),
    Round("MaterialIconsRounded", "data/icons/MaterialSymbolsRounded[FILL,GRAD,opsz,wght].ttf", "data/icons/MaterialSymbolsRounded[FILL,GRAD,opsz,wght].codepoints"),
    Sharp("MaterialIconsSharp", "data/icons/MaterialSymbolsSharp[FILL,GRAD,opsz,wght].ttf", "data/icons/MaterialSymbolsSharp[FILL,GRAD,opsz,wght].codepoints");

    private final String fontName;

    private final String fontFile;

    private final String codepointFile;

    private GoogleMaterialFonts(String fontName, String fontFile, String codepointFile) {
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

    public static GoogleMaterialFonts fromString(String fontName) {
        for(GoogleMaterialFonts font:values()) {
            if(font.getFontName().equals(fontName)) {
                return font;
            }
        }
        return null;
    }
}
