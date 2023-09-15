package ca.phon.orthography;

/**
 * Code that can only occur at the end of an utterance. Currently arbitrary
 * information, although there are some conventions.
 *
 * <a href="https://talkbank.org/manuals/CHAT.html#IncludedUtterancePostcode">CHAT manual
 *                     section on this topic...</a>
 * <a href="https://talkbank.org/manuals/CHAT.html#ExcludedUtterancePostcode">CHAT manual
 *                     section on this topic...</a>
 */
@CHATReference("https://talkbank.org/manuals/CHAT.html#Postcodes")
public final class Postcode extends AbstractOrthographyElement {

    public final static String POSTCODE_PREFIX = "[+";

    private final String code;

    public Postcode(String code) {
        super();
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    @Override
    public String text() {
        return String.format("%s %s]", POSTCODE_PREFIX, getCode());
    }

}
