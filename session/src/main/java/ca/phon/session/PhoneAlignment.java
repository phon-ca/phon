package ca.phon.session;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneAligner;
import ca.phon.ipa.alignment.PhoneMap;

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
     * @return new PhoneAlignment object
     */
    public static PhoneAlignment fromTiers(Tier<IPATranscript> targetTier, Tier<IPATranscript> alignedTier) {
        return fromTiers(targetTier, alignedTier, Transcriber.VALIDATOR);
    }

    /**
     * Return phone alignment for tiers using transcription for provided transcriber
     *
     * @param targetTier
     * @param alignedTier
     * @param transcriber
     * @return new PhoneAlignment object
     */
    public static PhoneAlignment fromTiers(Tier<IPATranscript> targetTier, Tier<IPATranscript> alignedTier, Transcriber transcriber) {
        final IPATranscript targetIpa =
                transcriber != Transcriber.VALIDATOR
                        ? (targetTier.hasBlindTranscription(transcriber.getUsername()) ? targetTier.getBlindTranscription(transcriber.getUsername()) : new IPATranscript())
                        : (targetTier.hasValue() ? targetTier.getValue() : new IPATranscript());
        final IPATranscript alignedIpa =
                transcriber != Transcriber.VALIDATOR
                        ? (alignedTier.hasBlindTranscription(transcriber.getUsername()) ? alignedTier.getBlindTranscription(transcriber.getUsername()) : new IPATranscript())
                        : (alignedTier.hasValue() ? alignedTier.getValue() : new IPATranscript());
        final List<IPATranscript> targetWords = targetIpa.words();
        final List<IPATranscript> alignedWords = alignedIpa.words();
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
        // first check if all elements are in the same word
        final PhoneMap pm = getPhoneMapForElements(elements);
        if(pm != null) {
            return pm.getAligned(elements);
        } else {
            return null;
        }
    }

    /**
     * Get the phone map containing the given elements.  If elements span multiple words
     * a new phone map is created.
     *
     * @param elements
     * @return
     */
    private PhoneMap getPhoneMapForElements(List<IPAElement> elements) {
        PhoneMap tpm1 = null;
        PhoneMap tpm2 = null;
        if(elements.size() > 0) {
            final Optional<PhoneMap> m1 = findPhoneMap(List.of(elements.get(0)));
            final Optional<PhoneMap> m2 = findPhoneMap(List.of(elements.get(elements.size()-1)));
            if(m1.isEmpty() || m2.isEmpty()) return null;
            tpm1 = m1.get();
            tpm2 = m2.get();
        }
        final int firstPhoneMapIdx = tpm1 != null ? getAlignments().indexOf(tpm1) : -1;
        final int lastPhoneMapIdx = tpm2 != null ? getAlignments().indexOf(tpm2) : -1;
        if(firstPhoneMapIdx < 0 || lastPhoneMapIdx < 0) return null;

        PhoneMap pm = getAlignments().get(firstPhoneMapIdx);
        for(int i = firstPhoneMapIdx + 1; i <= lastPhoneMapIdx; i++) {
            pm = pm.append(getAlignments().get(i));
        }
        return pm;
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
        PhoneMap tpm1 = null;
        PhoneMap tpm2 = null;
        PhoneMap apm1 = null;
        PhoneMap apm2 = null;
        if(ipaTarget.length() > 0) {
            final Optional<PhoneMap> m1 = findPhoneMap(List.of(ipaTarget.elementAt(0)));
            final Optional<PhoneMap> m2 = findPhoneMap(List.of(ipaTarget.elementAt(ipaTarget.length() - 1)));
            if(m1.isEmpty() || m2.isEmpty()) return null;
            tpm1 = m1.get();
            tpm2 = m2.get();
        }
        if(ipaActual.length() > 0) {
            final Optional<PhoneMap> m1 = findPhoneMap(List.of(ipaActual.elementAt(0)));
            final Optional<PhoneMap> m2 = findPhoneMap(List.of(ipaActual.elementAt(ipaActual.length() - 1)));
            if(m1.isEmpty() || m2.isEmpty()) return null;
            apm1 = m1.get();
            apm2 = m2.get();
            if(ipaTarget.length() == 0) {
                tpm1 = apm1;
                tpm2 = apm2;
            }
        } else {
            apm1 = tpm1;
            apm2 = tpm2;
        }
        final int firstPhoneMapIdx = Math.min(getAlignments().indexOf(tpm1), getAlignments().indexOf(apm1));
        final int lastPhoneMapIdx = Math.max(getAlignments().indexOf(tpm2), getAlignments().indexOf(apm2));
        if(firstPhoneMapIdx < 0 || lastPhoneMapIdx < 0) return null;

        PhoneMap pm = getAlignments().get(firstPhoneMapIdx);
        for(int i = firstPhoneMapIdx + 1; i <= lastPhoneMapIdx; i++) {
            pm = pm.append(getAlignments().get(i));
        }
        return pm.getSubAlignment(ipaTarget, ipaActual);
    }

    /**
     * Get phone alignment as a single phone map
     *
     * @return phone alignment as a single phone map
     */
    public PhoneMap getFullAlignment() {
        PhoneMap retVal = new PhoneMap(new IPATranscript(), new IPATranscript());
        retVal.setTopAlignment(new Integer[0]);
        retVal.setBottomAlignment(new Integer[0]);
        for(PhoneMap pm:getAlignments()) {
            retVal = retVal.append(pm);
        }
        return retVal;
    }

    @Override
    public void forEach(Consumer<? super PhoneMap> action) {
        getAlignments().forEach(action);
    }

    @Override
    public Spliterator<PhoneMap> spliterator() {
        return getAlignments().spliterator();
    }

    @Override
    public Iterator<PhoneMap> iterator() {
        return getAlignments().iterator();
    }

    @Override
    public String toString() {
        return getAlignments().stream().map(PhoneMap::toString).collect(Collectors.joining(" "));
    }

}
