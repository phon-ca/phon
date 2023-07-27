package ca.phon.session;

import ca.phon.ipa.IPAElement;
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

    /**
     * Find phone map
     *
     * @param elements
     * @return phoneMap containing the give elements
     */
    private Optional<PhoneMap> findPhoneMap(List<IPAElement> elements) {
        final IPATranscript testIpa = new IPATranscript(elements);
        return getAlignments().stream().filter(pm ->
            pm.getTargetRep().indexOf(testIpa) >= 0
            || pm.getActualRep().indexOf(testIpa) >= 0).findFirst();
    }

    /**
     * Return the list of elements aligned with the given element
     *
     * @param elements
     * @return list of elements aligned with the given elements.  list must be contiguous
     */
    public List<IPAElement> getAligned(List<IPAElement> elements) {
        final Optional<PhoneMap> phoneMapOpt = findPhoneMap(elements);
        List<IPAElement> retVal = new ArrayList<>();
        if(phoneMapOpt.isPresent()) {
            final PhoneMap pm = phoneMapOpt.get();
            return pm.getAligned(elements);
        }
        return retVal;
    }

    public List<IPAElement> getAligned(IPAElement[] elements) {
        return getAligned(Arrays.asList(elements));
    }

    public List<IPAElement> getAligned(IPATranscript ipa) {
        return getAligned(ipa.toList());
    }

    /**
     * Get sub alignment for given ipa target and actual elements
     *
     * @param ipaTarget
     * @param ipaActual
     * @return sub-alignment (as a PhoneMap) for the given ipa elements, null if not found
     */
    public PhoneMap getSubAlignment(IPATranscript ipaTarget, IPATranscript ipaActual) {
        Optional<PhoneMap> phoneMapOpt = findPhoneMap(ipaTarget.toList());
        if(phoneMapOpt.isEmpty())
            phoneMapOpt = findPhoneMap(ipaActual.toList());
        if(phoneMapOpt.isPresent()) {
            final PhoneMap pm = phoneMapOpt.get();
            return pm.getSubAlignment(ipaTarget, ipaActual);
        }
        return null;
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
