package ca.phon.ipa;

public enum PhoneticGroupMarkerType {
    BEGIN('\u2039'),
    END('\u203a');

    private char makerChar;

    private PhoneticGroupMarkerType(char ch) {
        this.makerChar = ch;
    }

    public char getMakerChar() {
        return makerChar;
    }

    @Override
    public String toString() {
        return "" + getMakerChar();
    }

}
