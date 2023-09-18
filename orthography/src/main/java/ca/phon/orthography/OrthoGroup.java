package ca.phon.orthography;

import ca.phon.util.Documentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Grouped contented within Orthography with annotations
 * A group of material that is annotated. May be nested, i.e., a group may
 * contain groups as well as words and other material.
 */
@Documentation("https://talkbank.org/manuals/CHAT.html#Group")
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
    public String elementText() {
        final String groupTxt = getElements().stream().map(ele -> ele.text()).collect(Collectors.joining(" "));
        if(getElements().size() == 1 && getElements().get(0) instanceof Word)
            return groupTxt;
        else
            return String.format("<%s>", groupTxt);
    }

}
