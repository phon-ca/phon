package ca.phon.session.alignment.aligners;

import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.PhoneAlignment;
import ca.phon.session.Tier;
import ca.phon.session.alignment.ITierAligner;
import ca.phon.session.alignment.TierAligner;
import ca.phon.session.alignment.TierAlignment;
import ca.phon.util.Tuple;

import java.util.List;

public class PhoneAlignmentToPhoneAlignmentAligner implements ITierAligner<PhoneAlignment, PhoneMap, PhoneAlignment, PhoneMap> {

    @Override
    public TierAlignment<PhoneAlignment, PhoneMap, PhoneAlignment, PhoneMap> calculateAlignment(Tier<PhoneAlignment> topTier, Tier<PhoneAlignment> bottomTier) {
        final List<Tuple<PhoneMap, PhoneMap>> elements = TierAligner.mapAlignedElements(topTier.getValue().getAlignments(), bottomTier.getValue().getAlignments());
        return new TierAlignment<>(topTier, bottomTier, elements);
    }

}
