package ca.phon.ipa.parser;

enum TokenType {

    CONSONANT,
    VOWEL,
    COMBINING_DIACRITIC,
    PREFIX_DIACRITIC,
    SUFFIX_DIACRITIC,
    OPEN_PAREN,
    CLOSE_PAREN,
    COVER_SYMBOL,
    HALF_LONG,
    LONG,
    MAJOR_GROUP,
    MINOR_GROUP,
    PERIOD,
    PLUS,
    TILDE,
    PRIMARY_STRESS,
    SECONDARY_STRESS,
    SPACE,
    ROLE_REVERSAL,
    LIGATURE,
    GLIDE,
    TONE_NUMBER,
    SANDHI,
    OPEN_BRACE,
    CLOSE_BRACE,
    BACKSLASH,
    GROUP_NAME,
    DIGIT,
    PG_START,
    PG_END,
    COLON,
    INTRA_WORD_PAUSE,
    ALIGNMENT;

    public String value() {
        return name();
    }

    public static TokenType fromValue(String v) {
        return valueOf(v);
    }

}