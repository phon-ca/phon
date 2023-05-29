package ca.phon.orthography;

import ca.phon.visitor.annotation.Visits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This visitor will split an utterance into separate Orthography instances
 *
 */
public class PhraseVisitor extends AbstractOrthographyVisitor {

    private OrthographyBuilder builder = new OrthographyBuilder();

    private final List<Orthography> phrases = new ArrayList<>();

    @Visits
    @Override
    public void visitTagMarker(TagMarker tagMarker) {
        phrases.add(builder.toOrthography());
        builder = new OrthographyBuilder();
    }

    @Override
    public void fallbackVisit(OrthographyElement obj) {
        builder.append(obj);
    }

    /**
     * Return phrases present in the utterance
     *
     * @return phrases
     */
    public List<Orthography> getPhrases() {
        if(builder.size() > 0) {
            phrases.add(builder.toOrthography());
        }
        return Collections.unmodifiableList(phrases);
    }

}
