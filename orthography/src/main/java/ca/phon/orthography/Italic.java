package ca.phon.orthography;

public class Italic extends AbstractOrthographyElement {

    private final BeginEnd beginEnd;

    public Italic(BeginEnd beginEnd) {
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
