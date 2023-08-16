package ca.phon.session.alignment;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.PhoneAlignment;
import ca.phon.session.Tier;

import java.util.Collections;
import java.util.List;

public class IPATierElementFilter implements TierElementFilter {

    public enum AlignableType {
        Word,
        Pause
    };

    private final List<AlignableType> alignableTypes;

    public IPATierElementFilter(List<AlignableType> alignableTypes) {
        this.alignableTypes = alignableTypes;
    }

    public List<AlignableType> getAlignableTypes() {
        return Collections.unmodifiableList(alignableTypes);
    }

    public boolean isIncluded(AlignableType alignableType) {
        return this.alignableTypes.contains(alignableType);
    }

    @Override
    public List<?> filterTier(Tier<?> tier) {
        final Class<?> declaredType = tier.getDeclaredType();
        if(declaredType == IPATranscript.class) {
            final IPATranscript transcript = (IPATranscript) tier.getValue();
            return transcript.words().stream().filter(this::test).toList();
        } else if(declaredType == PhoneAlignment.class) {
            final PhoneAlignment phoneAlignment = (PhoneAlignment) tier.getValue();
            return phoneAlignment.getAlignments().stream().filter(this::test).toList();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public boolean test(IPATranscript ipa) {
        if(ipa.matches("\\P")) {
            return isIncluded(AlignableType.Pause);
        } else {
            return isIncluded(AlignableType.Word);
        }
    }

    public boolean test(PhoneMap phoneMap) {
        if(phoneMap.getTargetRep().matches("\\P") && phoneMap.getActualRep().matches("\\P")) {
            return isIncluded(AlignableType.Pause);
        } else {
            return isIncluded(AlignableType.Word);
        }
    }

}
