package ca.phon.session.io.xml.v13;

import ca.phon.orthography.OrthographyBuilder;
import ca.phon.orthography.WordFormType;
import ca.phon.orthography.WordPrefix;
import ca.phon.orthography.WordType;
import ca.phon.orthography.xml.XMLOrthographyW;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

public class XMLOrthographyVisitor extends VisitorAdapter<Object> {

    private OrthographyBuilder builder = new OrthographyBuilder();

    @Visits
    public void visitWord(XMLOrthographyW word) {
        final WordType wordType = word.getType() != null ? WordType.fromString(word.getType()) : null;
        final WordPrefix prefix = wordType != null ? new WordPrefix(wordType) : null;

        final WordFormType formType = word.getFormType() != null ? WordFormType.fromCode(word.getFormType()) : null;
    }

    @Override
    public void fallbackVisit(Object obj) {

    }

}
