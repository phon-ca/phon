package ca.phon.orthography;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OrthoGroup extends AnnotatedOrthographyElement {

    private final List<OrthographyElement> groupElements;

    public OrthoGroup(List<OrthographyElement> elements, List<OrthographyAnnotation> annotations) {
        super(annotations);
        this.groupElements = new ArrayList<>(elements);
    }

    public List<OrthographyElement> getGroupElements() {
        return Collections.unmodifiableList(groupElements);
    }

    @Override
    public AnnotatedOrthographyElement cloneAppendingAnnotation(OrthographyAnnotation annotation) {
        final List<OrthographyAnnotation> annotations = new ArrayList<>(getAnnotations());
        annotations.add(annotation);
        return new OrthoGroup(getGroupElements(), annotations);
    }

    @Override
    public String text() {
        final String groupTxt = getGroupElements().stream().map(ele -> ele.text()).collect(Collectors.joining(" "));
        if(getGroupElements().size() == 1)
            return groupTxt + getAnnotationText();
        else
            return String.format("<%s>%s", groupTxt, getAnnotationText());
    }

}
