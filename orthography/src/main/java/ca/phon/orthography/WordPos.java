package ca.phon.orthography;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Morphological tag for word
 */
public final class WordPos {

    private final String category;

    private final List<String> subCategories;

    public WordPos(String category, List<String> subCategories) {
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
