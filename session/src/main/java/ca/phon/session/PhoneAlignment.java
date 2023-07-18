package ca.phon.session;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneAligner;
import ca.phon.ipa.alignment.PhoneMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

    /**
     * Create PhoneAlignment from given ipa tiers
     *
     * @param targetTier
     * @param alignedTier
     *
     * @return new PhoneAlignment tier object
     */
    public static PhoneAlignment fromTiers(Tier<IPATranscript> targetTier, Tier<IPATranscript> alignedTier) {
        final List<IPATranscript> targetWords = targetTier.getValue().words();
        final List<IPATranscript> alignedWords = alignedTier.getValue().words();
        final int n = Math.max(targetWords.size(), alignedWords.size());
        final PhoneAligner aligner = new PhoneAligner();
        final List<PhoneMap> alignments = new ArrayList<>();
        for(int i = 0; i < n; i++) {
            final int tidx = i < targetWords.size() ? i : -1;
            final int aidx = i < alignedWords.size() ? i : -1;
            final IPATranscript tipa = tidx >= 0 ? targetWords.get(tidx) : new IPATranscript();
            final IPATranscript aipa = aidx >= 0 ? alignedWords.get(aidx) : new IPATranscript();
            final PhoneMap pm = aligner.calculatePhoneAlignment(tipa, aipa);
            alignments.add(pm);
        }
        return new PhoneAlignment(targetTier.getName(), alignedTier.getName(), alignments);
    }

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

    @Override
    public String toString() {
        return getAlignments().stream().map(PhoneMap::toString).collect(Collectors.joining(" "));
    }

}
