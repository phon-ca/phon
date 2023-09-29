package ca.phon.orthography.mor;

import ca.phon.util.Documentation;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Morphological tag for word
 */
@Documentation("https://talkbank.org/manuals/MOR.html#Mor_POS")
public final class Pos {

    private final String category;

    private final List<String> subCategories;

    public Pos(String category, List<String> subCategories) {
        super();
        this.category = category;
        this.subCategories = Collections.unmodifiableList(subCategories);
    }

    public String getCategory() {
        return category;
    }

    public List<String> getSubCategories() {
        return subCategories;
    }

    @Override
    public String toString() {
        final String subCategoryText = (getSubCategories().size() > 0 ? ":" : "") +
                getSubCategories().stream().collect(Collectors.joining(":"));
        return String.format("%s%s", getCategory(), subCategoryText);
    }

}
