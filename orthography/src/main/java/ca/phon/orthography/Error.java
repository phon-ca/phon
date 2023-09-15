package ca.phon.orthography;

/**
 * Error coding
 */
@CHATReference("https://talkbank.org/manuals/CHAT.html#ErrorMarking_Scope")
public final class Error extends AbstractOrthographyElement implements OrthographyAnnotation {

    public final static String PREFIX = "[*";

    private final String data;

    public Error(String data) {
        super();
        this.data = data;
    }

    public String getData() {
        return this.data;
    }

    @Override
    public String text() {
        return String.format("%s%s]", PREFIX, getData());
    }

}
