package ca.phon.orthography;

import ca.phon.visitor.annotation.Visits;

import java.util.stream.Collectors;

/**
 * Find string index of {@link OrthographyElement}s
 *
 * Usage:
 * <pre>
 *     ortho = Orthography.parseOrthography("hello world");
 *     var visitor = new OrthoElementLocator(ortho.elementAt(1));
 *     ortho.accept(visitor);
 *     // stringIndex will be 6
 *     int stringIndex = visitor.getStringIndex()
 * </pre>
 */
public class OrthoElementLocator extends AbstractOrthographyVisitor {

    private int currentIndex = 0;

    private int retVal = -1;

    private final OrthographyElement element;

    public OrthoElementLocator(OrthographyElement ele) {
        this.element = ele;
    }

    @Visits
    @Override
    public void visitWord(Word word) {
        final OrthoElementLocator locator = new OrthoElementLocator(element);
        word.getReplacements().forEach(locator::visit);
        if(locator.retVal >= 0) {
            retVal = currentIndex + word.elementText().length() + 1 + locator.retVal;
        }
    }

    @Visits
    @Override
    public void visitOrthoGroup(OrthoGroup group) {
        final OrthoElementLocator locator = new OrthoElementLocator(element);
        group.getElements().forEach(locator::visit);
        if(locator.retVal >= 0) {
            retVal = currentIndex + 1 + locator.retVal;
        }

        final OrthoElementLocator annotationLocator = new OrthoElementLocator(element);
        group.getAnnotations().forEach(annotationLocator::visit);
        if(annotationLocator.retVal >= 0) {
            retVal = currentIndex + 2 + locator.currentIndex + 1 + annotationLocator.retVal;
        }
    }

    @Visits
    @Override
    public void visitAction(Action action) {
        visitEvent(action);
    }

    @Visits
    @Override
    public void visitHappening(Happening happening) {
        visitEvent(happening);
    }

    @Visits
    @Override
    public void visitOtherSpokenEvent(OtherSpokenEvent otherSpokenEvent) {
        visitEvent(otherSpokenEvent);
    }

    @Visits
    public void visitEvent(Event event) {
        final OrthoElementLocator locator = new OrthoElementLocator(element);
        event.getAnnotations().forEach(locator::visit);
        if(locator.retVal >= 0) {
            retVal = currentIndex + event.elementText().length() + 1 + locator.retVal;
        }
    }

    @Visits
    @Override
    public void visitPhoneticGroup(PhoneticGroup phoneticGroup) {
        final OrthoElementLocator locator = new OrthoElementLocator(element);
        phoneticGroup.getElements().forEach(locator::visit);
        if(locator.retVal >= 0) {
            retVal = currentIndex + 1 + locator.retVal;
        }
    }

    @Override
    public void visit(OrthographyElement obj) {
        if(retVal < 0) {
            if(currentIndex > 0) {
                // space
                ++currentIndex;
            }
            if(obj == element) {
                retVal = currentIndex;
                return;
            }
            super.visit(obj);
            currentIndex += obj.text().length();
        }
    }

    @Override
    public void fallbackVisit(OrthographyElement obj) {
    }

    public int getStringIndex() {
        return retVal;
    }

}
