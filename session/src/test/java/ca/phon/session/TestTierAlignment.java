package ca.phon.session;

import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;
import ca.phon.orthography.OrthographyElement;
import ca.phon.session.alignment.TierAlignment;
import ca.phon.session.alignment.TierAlignmentRules;
import ca.phon.session.alignment.aligners.OrthoToIPAAligner;
import ca.phon.session.alignment.aligners.OrthoToOrthoAligner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestTierAlignment {

    @Test
    public void testOrthoToOrthoAligner() {
        final SessionFactory factory = SessionFactory.newFactory();
        final Tier<Orthography> tier1 = factory.createTier("Tier1", Orthography.class, TierAlignmentRules.orthographyTierRules());
        tier1.setText("hello (.) world !");
        final Tier<Orthography> tier2 = factory.createTier("Tier2", Orthography.class, TierAlignmentRules.orthographyTierRules());
        tier2.setText("goodbye (.) sanity !");
        final OrthoToOrthoAligner aligner = new OrthoToOrthoAligner();
        final TierAlignment<Orthography, OrthographyElement, Orthography, OrthographyElement> alignment =
                aligner.calculateAlignment(tier1, tier2);
        Assert.assertEquals(3, alignment.length());
        Assert.assertEquals("hello", alignment.getAlignedElements().get(0).getObj1().toString());
        Assert.assertEquals("goodbye", alignment.getAlignedElements().get(0).getObj2().toString());
        Assert.assertEquals("(.)", alignment.getAlignedElements().get(1).getObj1().toString());
        Assert.assertEquals("(.)", alignment.getAlignedElements().get(1).getObj2().toString());
        Assert.assertEquals("world", alignment.getAlignedElements().get(2).getObj1().toString());
        Assert.assertEquals("sanity", alignment.getAlignedElements().get(2).getObj2().toString());
    }

    @Test
    public void testOrthoToIPAAligner() {
        final SessionFactory factory = SessionFactory.newFactory();
        final Tier<Orthography> tier1 = factory.createTier("Tier1", Orthography.class, TierAlignmentRules.orthographyTierRules());
        tier1.setText("hello (.) world !");
        final Tier<IPATranscript> tier2 = factory.createTier("Tier2", IPATranscript.class, TierAlignmentRules.ipaTierRules());
        tier2.setText("hello (.)");
        final OrthoToIPAAligner aligner = new OrthoToIPAAligner();
        final TierAlignment<Orthography, OrthographyElement, IPATranscript, IPATranscript> alignment =
                aligner.calculateAlignment(tier1, tier2);
        Assert.assertEquals(3, alignment.length());
        Assert.assertEquals(3, alignment.length());
        Assert.assertEquals("hello", alignment.getAlignedElements().get(0).getObj1().toString());
        Assert.assertEquals("hello", alignment.getAlignedElements().get(0).getObj2().toString());
        Assert.assertEquals("(.)", alignment.getAlignedElements().get(1).getObj1().toString());
        Assert.assertEquals("(.)", alignment.getAlignedElements().get(1).getObj2().toString());
        Assert.assertEquals("world", alignment.getAlignedElements().get(2).getObj1().toString());
        Assert.assertNull(alignment.getAlignedElements().get(2).getObj2());
    }

}
