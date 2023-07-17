package ca.phon.session.alignment.aligners;

import ca.phon.ipa.IPATranscript;
import ca.phon.session.alignment.TierAlignmentRules;
import ca.phon.session.alignment.TypeAlignmentRules;
import ca.phon.visitor.VisitorAdapter;

import java.util.function.Predicate;

public class IPAAlignmentFilter implements Predicate<IPATranscript> {

    private final TierAlignmentRules alignmentRules;

    public IPAAlignmentFilter(TierAlignmentRules alignmentRules) {
        super();
        this.alignmentRules = alignmentRules;
    }

    @Override
    public boolean test(IPATranscript ipa) {
        if(alignmentRules.getType() == TierAlignmentRules.TierAlignmentType.None) return false;
        if(ipa.matches("\\P")) {
            return alignmentRules.getWordAlignmentRules().isIncluded(TypeAlignmentRules.AlignableType.Pause);
        } else {
            return alignmentRules.getWordAlignmentRules().isIncluded(TypeAlignmentRules.AlignableType.Word);
        }
    }

}
