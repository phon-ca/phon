package ca.phon.orthography;

public class Underline extends AbstractOrthographyElement {

    private final BeginEnd beginEnd;

    public Underline(BeginEnd beginEnd) {
        super();
        this.beginEnd = beginEnd;
    }

    public BeginEnd getBeginEnd() {
        return beginEnd;
    }

    @Override
    public String text() {
        return "";
    }

}
