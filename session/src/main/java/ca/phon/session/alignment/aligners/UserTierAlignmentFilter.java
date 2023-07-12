package ca.phon.session.alignment.aligners;

import ca.phon.session.alignment.TierAlignmentRules;
import ca.phon.session.alignment.TypeAlignmentRules;
import ca.phon.session.usertier.TierString;
import ca.phon.session.usertier.UserTierElement;
import ca.phon.session.usertier.UserTierInternalMedia;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.util.List;

public class UserTierAlignmentFilter extends VisitorAdapter<UserTierElement> {

    private final TierAlignmentRules alignmentRules;

    private List<UserTierElement> elements;

    public UserTierAlignmentFilter(TierAlignmentRules alignmentRules) {
        super();
        this.alignmentRules = alignmentRules;
    }

    @Visits
    public void visitTierString(TierString tierString) {
        if(alignmentRules.getWordAlignmentRules().isIncluded(TypeAlignmentRules.AlignableType.Word))
            elements.add(tierString);
    }

    @Visits
    public void visitInternalMedia(UserTierInternalMedia internalMedia) {
        if(alignmentRules.getWordAlignmentRules().isIncluded(TypeAlignmentRules.AlignableType.InternalMedia))
            elements.add(internalMedia);
    }

    @Override
    public void fallbackVisit(UserTierElement obj) {

    }

    public List<UserTierElement> getElements() {
        return elements;
    }

}
