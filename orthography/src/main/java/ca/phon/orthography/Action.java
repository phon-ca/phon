package ca.phon.orthography;

import java.util.ArrayList;
import java.util.List;

public final class Action extends Event {

    public Action() {
        this(new ArrayList<OrthographyElement>());
    }

    public Action(List<OrthographyElement> annotations) {
        super(annotations);
    }

    @Override
    public String text() {
        return "0" + getAnnotationText();
    }

}
