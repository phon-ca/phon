package ca.phon.orthography.mor;

import ca.phon.extensions.ExtendableObject;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Container for a list of Mor items used for tier data
 */
public final class MorTierData extends ExtendableObject {

    private final List<Mor> mors;

    public MorTierData(List<Mor> mors) {
        super();
        this.mors = mors;
    }

    public List<Mor> getMors() {
        return Collections.unmodifiableList(mors);
    }

    public int size() {
        return mors.size();
    }

    public boolean isEmpty() {
        return mors.isEmpty();
    }

    public boolean contains(Mor mor) {
        return mors.contains(mor);
    }

    public Mor get(int index) {
        return mors.get(index);
    }

    public int indexOf(Mor mor) {
        return mors.indexOf(mor);
    }

    @Override
    public String toString() {
        return getMors().stream().map(Mor::text).collect(Collectors.joining(" "));
    }

}
