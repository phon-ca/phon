package ca.phon.ipa;

import ca.phon.ipa.features.FeatureMatrix;
import ca.phon.ipa.features.FeatureSet;

/**
 * Represents a tone number in an IPA transcription.
 * Tone numbers are represented as superscript numbers
 * from 0 to 9 at the end of a syllable.  A tone melody
 * is a sequence of tone numbers.
 */
public class ToneNumber extends IPAElement {

    public static final String TONE_NUMBER_AMBIGUOUS = "\u02e3\u02e3"; // ˣˣ

    private final char[] toneChars;

    /**
     * Get tone number character from integer value.
     * Valid values are 0 to 9.
     *
     * @param number the tone number
     * @return the corresponding superscript character
     */
    public static char[] fromNumber(int number) {
        if(number < 0) {
            return TONE_NUMBER_AMBIGUOUS.toCharArray(); // error tone number
        }
        final StringBuffer sb = new StringBuffer();
        while(number > 0) {
            final int firstDigit = number % 10;
            final char ch = switch(firstDigit) {
                case 0 -> '\u2070';
                case 1 -> '\u00B9';
                case 2 -> '\u00B2';
                case 3 -> '\u00B3';
                case 4, 5, 6, 7, 8, 9 -> (char)('\u2070' + firstDigit);
                default -> throw new IllegalArgumentException("Tone number must be between 0 and 9");
            };
            sb.append(ch);
            number = (number - firstDigit) / 10;
        }
        return sb.reverse().toString().toCharArray();
    }

    /**
     * Return a ToneNumber from a string of digits. The string may be composed of superscript
     * or normal digits.  Anything that is not a digit is ignored. If no digits are found, or '-1',
     * an error tone number is returned.
     *
     * @param toneStr the tone number string
     * @return the corresponding ToneNumber
     */
    public static ToneNumber fromString(String toneStr) {
        if(toneStr == null || toneStr.isEmpty()) {
            throw new IllegalArgumentException("Tone number string cannot be null or empty");
        }
        if(toneStr.equals("-1")) {
            return new ToneNumber(-1);
        }
        if(TONE_NUMBER_AMBIGUOUS.equals(toneStr)) {
            return new ToneNumber(TONE_NUMBER_AMBIGUOUS.toCharArray());
        }
        final StringBuilder sb = new StringBuilder();
        for(char ch:toneStr.toCharArray()) {
            switch(ch) {
                case '0', '\u2070' -> sb.append('\u2070');
                case '1', '\u00B9' -> sb.append('\u00B9');
                case '2', '\u00B2' -> sb.append('\u00B2');
                case '3', '\u00B3' -> sb.append('\u00B3');
                case '4', '\u2074' -> sb.append('\u2074');
                case '5', '\u2075' -> sb.append('\u2075');
                case '6', '\u2076' -> sb.append('\u2076');
                case '7', '\u2077' -> sb.append('\u2077');
                case '8', '\u2078' -> sb.append('\u2078');
                case '9', '\u2079' -> sb.append('\u2079');
                default -> {
                    // ignore
                }
            }
        }
        if(sb.length() == 0) {
            return new ToneNumber(-1);
        } else {
            return new ToneNumber(sb.toString().toCharArray());
        }
    }

    public ToneNumber(int number) {
        this(fromNumber(number));
    }

    public ToneNumber(char[] toneChars) {
        this(toneChars, null, new SyllableInfo(SyllableConstituentType.TONENUMBER));
    }

    public ToneNumber(char[] toneChars, FeatureSet overrideFeatureSet, SyllableInfo syllableInfo) {
        super(overrideFeatureSet, syllableInfo != null ? syllableInfo : new SyllableInfo(SyllableConstituentType.TONENUMBER));
        this.toneChars = toneChars;
    }

    /**
     * Get the tone number characters.
     *
     * @return the tone number characters
     */
    public char[] toneChars() {
        return toneChars;
    }

    /**
     * Get the length of the tone number character array.
     *
     * @return the length of the tone number character array
     */
    public int length() {
    	return toneChars.length;
    }

    /**
     * Check if this tone number is a melody (i.e. has more than one tone number).
     *
     * @return true if this is a melody, false otherwise
     */
    public boolean isMelody() {
        return toneChars.length > 1;
    }

    /**
     * Check if this tone number represents an ambiguous tone error.
     * An error tone number is represented by two combining
     * superscript x characters: ˣˣ
     *
     * @return true if this is an ambiguous tone number, false otherwise
     */
    public boolean isAmbiguous() {
        return TONE_NUMBER_AMBIGUOUS.equals(getText());
    }

    @Override
    protected FeatureSet _getFeatureSet() {
        FeatureSet fs = new FeatureSet();
        for(char toneChar: toneChars) {
            fs = FeatureSet.union(fs, FeatureMatrix.getInstance().getFeatureSet(toneChar));
        }
        if(isAmbiguous()) {
            fs = FeatureSet.union(fs, FeatureSet.fromArray(new String[]{"toneerr"}));
        }
        if(isMelody()) {
            fs = FeatureSet.union(fs, FeatureSet.fromArray(new String[]{"tonemelody"}));
        }
        return fs;
    }

    @Override
    public String getText() {
        return new String(toneChars);
    }

    /**
     * Get tone number as integer value.
     * If the tone number is a sequence of digits, the
     * integer value is the concatenation of the digits.
     * If the tone number is an error, -1 is returned.
     *
     * @return the tone number as an integer
     */
    public int asInt() {
        if(isAmbiguous()) {
            return -1;
        }
        int retVal = 0;
        for(char toneChar:toneChars) {
            final int digit = switch(toneChar) {
                case '\u2070' -> 0;
                case '\u00B9' -> 1;
                case '\u00B2' -> 2;
                case '\u00B3' -> 3;
                case '\u2074' -> 4;
                case '\u2075' -> 5;
                case '\u2076' -> 6;
                case '\u2077' -> 7;
                case '\u2078' -> 8;
                case '\u2079' -> 9;
                default -> throw new IllegalArgumentException("Tone number must be between 0 and 9");
            };
            retVal = retVal * 10 + digit;
        }
        return retVal;
    }

}
