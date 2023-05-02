package ca.phon.orthography;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class OrthoGroup extends AnnotatedOrthographyElement {

    private final List<OrthographyElement> elements;

    public OrthoGroup(List<OrthographyElement> elements, List<OrthographyAnnotation> annotations) {
        super(annotations);
        this.elements = new ArrayList<>(elements);
    }

    public List<OrthographyElement> getElements() {
        return Collections.unmodifiableList(elements);
    }

    @Override
    public AnnotatedOrthographyElement cloneAppendingAnnotation(OrthographyAnnotation annotation) {
        final List<OrthographyAnnotation> annotations = new ArrayList<>(getAnnotations());
        annotations.add(annotation);
        return new OrthoGroup(getElements(), annotations);
    }

    @Override
    public String text() {
        final String groupTxt = getElements().stream().map(ele -> ele.text()).collect(Collectors.joining(" "));
        if(getElements().size() == 1)
            return groupTxt + getAnnotationText();
        else
            return String.format("<%s>%s", groupTxt, getAnnotationText());
    }

}
