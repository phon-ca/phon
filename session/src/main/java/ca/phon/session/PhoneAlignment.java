package ca.phon.session;

import ca.phon.ipa.alignment.PhoneMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

/**
 * Entity class for phone alignment tier.
 *
 */
public class PhoneAlignment implements Iterable<PhoneMap> {

    /**
     *  Target tier name
     */
    private final String targetTier;

    /**
     * Aligned tier name
     *
     */
    private final String alignedTier;

    /**
     * Alignments (by word)
     *
     */
    private final List<PhoneMap> alignments;

    public PhoneAlignment() {
        this(SystemTierType.IPATarget.getName(), SystemTierType.IPAActual.getName(), new ArrayList<>());
    }

    public PhoneAlignment(PhoneMap ... alignments) {
        this(SystemTierType.IPATarget.getName(), SystemTierType.IPAActual.getName(), List.of(alignments));
    }

    public PhoneAlignment(List<PhoneMap> alignments) {
        this(SystemTierType.IPATarget.getName(), SystemTierType.IPAActual.getName(), alignments);
    }

    public PhoneAlignment(String targetTier, String alignedTier, PhoneMap ... alignments) {
        this(targetTier, alignedTier, List.of(alignments));
    }

    public PhoneAlignment(String targetTier, String alignedTier, List<PhoneMap> alignments) {
        super();
        this.targetTier = targetTier;
        this.alignedTier = alignedTier;
        this.alignments = new ArrayList<>(alignments);
    }

    public String getTargetTier() {
        return targetTier;
    }

    public String getAlignedTier() {
        return alignedTier;
    }

    public List<PhoneMap> getAlignments() {
        return Collections.unmodifiableList(this.alignments);
    }

    @Override
    public void forEach(Consumer<? super PhoneMap> action) {
        getAlignments().forEach(action);
    }

    @Override
    public Spliterator<PhoneMap> spliterator() {
        return getAlignments().spliterator();
    }

    @NotNull
    @Override
    public Iterator<PhoneMap> iterator() {
        return getAlignments().iterator();
    }

}
