package ca.phon.session;

import ca.phon.orthography.mor.Grasp;
import ca.phon.orthography.mor.GraspTierData;
import ca.phon.orthography.mor.MorTierData;
import ca.phon.session.alignment.CrossTierAlignment;
import ca.phon.session.alignment.TierAligner;
import ca.phon.session.io.xml.OneToOne;
import ca.phon.session.io.xml.XMLFragments;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.HashMap;

@RunWith(JUnit4.class)
public class TestFragments {

    @Test
    public void testOrthographyMor() throws IOException {
        final SessionFactory factory = SessionFactory.newFactory();
        final Record record = factory.createRecord();
        record.getOrthographyTier().setText("hello world .");
        final Tier<MorTierData> morTier = factory.createTier(UserTierType.Mor.getTierName(), MorTierData.class, new HashMap<>(), true);
        morTier.setText("v|hello n|world .");
        final Tier<GraspTierData> graTier = factory.createTier(UserTierType.Gra.getTierName(), GraspTierData.class, new HashMap<>(), true);
        graTier.setText("0|1|FOO 1|0|BAR 3|0|PUNCT");
        record.putTier(morTier);
        record.putTier(graTier);
        OneToOne.annotateRecord(record);

        final String xml = XMLFragments.toXml(record.getOrthography(), false, true);
        System.out.println(xml);
    }

}
