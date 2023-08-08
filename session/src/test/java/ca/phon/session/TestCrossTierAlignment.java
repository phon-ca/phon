package ca.phon.session;

import ca.phon.orthography.Orthography;
import ca.phon.session.alignment.CrossTierAlignment;
import ca.phon.session.alignment.TierAligner;
import ca.phon.session.alignment.TierAlignmentRules;
import ca.phon.session.usertier.UserTierData;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierLibrary;
import junit.framework.AssertionFailedError;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestCrossTierAlignment {

    private Record createTestRecord() {
        final SessionFactory factory = SessionFactory.newFactory();
        final Record record = factory.createRecord();
        record.getOrthographyTier().setText("No No chicken nuggets");
        record.getIPATargetTier().setText("ˈnoʊ ˈnoʊ ˈʧɪkən ˈnʌɡəts");
        record.getIPAActualTier().setText("ˈnɔ ˈnoʊ ˈʧɪkə ˈnʌdətʰ");
        final PhoneAlignment phoneAlignment = PhoneAlignment.fromTiers(record.getIPATargetTier(), record.getIPAActualTier());
        record.setPhoneAlignment(phoneAlignment);
        return record;
    }

    @Test
    public void testRepeatedIPAAlignment() {
        final Record record = createTestRecord();
        final CrossTierAlignment crossTierAlignment = TierAligner.calculateCrossTierAlignment(record, record.getIPATargetTier());

        Assert.assertEquals(4, crossTierAlignment.getTopAlignmentElements().size());
    }

}
