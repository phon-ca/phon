package ca.phon.session.alignment;

import ca.phon.util.Tuple;

import java.util.ArrayList;
import java.util.List;

public final class TierAligner {

    /**
     * Return a list of left-aligned tuples
     *
     * @param topElements
     * @param bottomElements
     * @return list of aligned tuples
     * @param <S> type of top element
     * @param <C> type of bottom element
     */
    public static <S, C> List<Tuple<S, C>> mapAlignedElements(List<S> topElements, List<C> bottomElements) {
        final List<Tuple<S, C>> retVal = new ArrayList<>();
        final int n = Math.max(topElements.size(), bottomElements.size());
        for(int i = 0; i < n; i++) {
            final S topElement = i < topElements.size() ? topElements.get(i) : null;
            final C bottomElement = i < bottomElements.size() ? bottomElements.get(i) : null;
            final Tuple<S, C> tuple = new Tuple<>(topElement, bottomElement);
            retVal.add(tuple);
        }
        return retVal;
    }

}
