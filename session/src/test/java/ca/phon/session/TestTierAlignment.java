package ca.phon.session;

import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.*;
import ca.phon.session.alignment.TierAligner;
import ca.phon.session.tierdata.TierData;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestTierAlignment {

    private Record createTestRecord() {
        final SessionFactory factory = SessionFactory.newFactory();
        final Record record = factory.createRecord();
        record.getOrthographyTier().setText("hello (.) world !");
        record.getIPATargetTier().setText("hello (.) world");
        record.getIPAActualTier().setText("hello (.)");
        final PhoneAlignment phoneAlignment = PhoneAlignment.fromTiers(record.getIPATargetTier(), record.getIPAActualTier());
        record.setPhoneAlignment(phoneAlignment);
        record.getNotesTier().setText("This is a test");
        final Tier<TierData> testTier1 = factory.createTier("Test1", TierData.class);
        testTier1.setText("V N");
        record.putTier(testTier1);
        final Tier<Orthography> testTier2 = factory.createTier("Test2", Orthography.class);
        testTier2.setText("goodbye (.) sanity !");
        record.putTier(testTier2);
        final Tier<TierData> testTier3 = factory.createTier("Test3", TierData.class);
        testTier3.setText("1 [% test] 2");
        record.putTier(testTier3);
        return record;
    }

    @Test
    public void testOrthoToOrthoAligner() {
        final Record testRecord = createTestRecord();
        final Tier<Orthography> tier1 = testRecord.getOrthographyTier();
        final Tier<Orthography> tier2 = testRecord.getTier("Test2", Orthography.class);
        var alignment = TierAligner.alignTiers(tier1, tier2);
        Assert.assertEquals(4, alignment.length());
        Assert.assertEquals("hello", alignment.getAlignedElements().get(0).getObj1().toString());
        Assert.assertEquals("goodbye", alignment.getAlignedElements().get(0).getObj2().toString());
        Assert.assertEquals("(.)", alignment.getAlignedElements().get(1).getObj1().toString());
        Assert.assertEquals("(.)", alignment.getAlignedElements().get(1).getObj2().toString());
        Assert.assertEquals("world", alignment.getAlignedElements().get(2).getObj1().toString());
        Assert.assertEquals("sanity", alignment.getAlignedElements().get(2).getObj2().toString());
    }

    @Test
    public void testOrthoToIPAAligner() {
        final Record testRecord = createTestRecord();
        final Tier<Orthography> tier1 = testRecord.getOrthographyTier();
        final Tier<IPATranscript> tier2 = testRecord.getIPAActualTier();
        var alignment = TierAligner.alignTiers(tier1, tier2);
        Assert.assertEquals(3, alignment.length());
        Assert.assertEquals(3, alignment.length());
        Assert.assertEquals("hello", alignment.getAlignedElements().get(0).getObj1().toString());
        Assert.assertEquals("hello", alignment.getAlignedElements().get(0).getObj2().toString());
        Assert.assertEquals("(.)", alignment.getAlignedElements().get(1).getObj1().toString());
        Assert.assertEquals("(.)", alignment.getAlignedElements().get(1).getObj2().toString());
        Assert.assertEquals("world", alignment.getAlignedElements().get(2).getObj1().toString());
        Assert.assertNull(alignment.getAlignedElements().get(2).getObj2());
    }

    @Test
    public void testOrthoToPhoneAlignmentAligner() {
        final Record testRecord = createTestRecord();
        final Tier<Orthography> tier1 = testRecord.getOrthographyTier();
        final Tier<PhoneAlignment> tier2 = testRecord.getPhoneAlignmentTier();
        var alignment = TierAligner.alignTiers(tier1, tier2);
        Assert.assertEquals(3, alignment.length());
        Assert.assertEquals("hello", alignment.getAlignedElements().get(0).getObj1().toString());
        Assert.assertEquals("h↔h,e↔e,l↔l,l↔l,o↔o", alignment.getAlignedElements().get(0).getObj2().toString());
        Assert.assertEquals("(.)", alignment.getAlignedElements().get(1).getObj1().toString());
        Assert.assertEquals("(.)↔(.)", alignment.getAlignedElements().get(1).getObj2().toString());
        Assert.assertEquals("world", alignment.getAlignedElements().get(2).getObj1().toString());
        Assert.assertEquals("w↔∅,o↔∅,r↔∅,l↔∅,d↔∅", alignment.getAlignedElements().get(2).getObj2().toString());
    }

    @Test
    public void testOrthoToUserTierAligner() {
        final Record testRecord = createTestRecord();
        final Tier<Orthography> tier1 = testRecord.getOrthographyTier();
        final Tier<TierData> tier2 = testRecord.getTier("Test1", TierData.class);
        var alignment = TierAligner.alignTiers(tier1, tier2);
        Assert.assertEquals(2, alignment.length());
        Assert.assertEquals("hello", alignment.getAlignedElements().get(0).getObj1().toString());
        Assert.assertEquals("V", alignment.getAlignedElements().get(0).getObj2().toString());
        Assert.assertEquals("world", alignment.getAlignedElements().get(1).getObj1().toString());
        Assert.assertEquals("N", alignment.getAlignedElements().get(1).getObj2().toString());
    }

    @Test
    public void testIPAtoIPAAligner() {
        final Record testRecord = createTestRecord();
        final Tier<IPATranscript> tier1 = testRecord.getIPATargetTier();
        final Tier<IPATranscript> tier2 = testRecord.getIPAActualTier();
        var alignment = TierAligner.alignTiers(tier1, tier2);
        Assert.assertEquals(3, alignment.length());
        Assert.assertEquals("hello", alignment.getAlignedElements().get(0).getObj1().toString());
        Assert.assertEquals("hello", alignment.getAlignedElements().get(0).getObj2().toString());
        Assert.assertEquals("(.)", alignment.getAlignedElements().get(1).getObj1().toString());
        Assert.assertEquals("(.)", alignment.getAlignedElements().get(1).getObj2().toString());
        Assert.assertEquals("world", alignment.getAlignedElements().get(2).getObj1().toString());
        Assert.assertNull(alignment.getAlignedElements().get(2).getObj2());
    }

    @Test
    public void testIPAtoPhoneAlignmentAligner() {
        final Record testRecord = createTestRecord();
        final Tier<IPATranscript> tier1 = testRecord.getIPATargetTier();
        final Tier<PhoneAlignment> tier2 = testRecord.getPhoneAlignmentTier();
        var alignment = TierAligner.alignTiers(tier1, tier2);
        Assert.assertEquals(3, alignment.length());
        Assert.assertEquals("hello", alignment.getAlignedElements().get(0).getObj1().toString());
        Assert.assertEquals("h↔h,e↔e,l↔l,l↔l,o↔o", alignment.getAlignedElements().get(0).getObj2().toString());
        Assert.assertEquals("(.)", alignment.getAlignedElements().get(1).getObj1().toString());
        Assert.assertEquals("(.)↔(.)", alignment.getAlignedElements().get(1).getObj2().toString());
        Assert.assertEquals("world", alignment.getAlignedElements().get(2).getObj1().toString());
        Assert.assertEquals("w↔∅,o↔∅,r↔∅,l↔∅,d↔∅", alignment.getAlignedElements().get(2).getObj2().toString());
    }

    @Test
    public void testIPAtoUserTierAligner() {
        final Record testRecord = createTestRecord();
        final Tier<IPATranscript> tier1 = testRecord.getIPATargetTier();
        final Tier<TierData> tier2 = testRecord.getTier("Test1", TierData.class);
        var alignment = TierAligner.alignTiers(tier1, tier2);
        Assert.assertEquals(2, alignment.length());
        Assert.assertEquals("hello", alignment.getAlignedElements().get(0).getObj1().toString());
        Assert.assertEquals("V", alignment.getAlignedElements().get(0).getObj2().toString());
        Assert.assertEquals("world", alignment.getAlignedElements().get(1).getObj1().toString());
        Assert.assertEquals("N", alignment.getAlignedElements().get(1).getObj2().toString());
    }

    @Test
    public void testPhoneAlignmentToUserTierAligner() {
        final Record testRecord = createTestRecord();
        final Tier<PhoneAlignment> tier1 = testRecord.getPhoneAlignmentTier();
        final Tier<TierData> tier2 = testRecord.getTier("Test1", TierData.class);
        var alignment = TierAligner.alignTiers(tier1, tier2);
        Assert.assertEquals(2, alignment.length());
    }

    @Test
    public void testUserTierToUserTierAligner() {
        final Record testRecord = createTestRecord();
        final Tier<TierData> tier1 = testRecord.getTier("Test1", TierData.class);
        final Tier<TierData> tier2 = testRecord.getTier("Test3", TierData.class);
        var alignment = TierAligner.alignTiers(tier1, tier2);
        Assert.assertEquals(2, alignment.length());
        Assert.assertEquals("V", alignment.getAlignedElements().get(0).getObj1().toString());
        Assert.assertEquals("1", alignment.getAlignedElements().get(0).getObj2().toString());
        Assert.assertEquals("N", alignment.getAlignedElements().get(1).getObj1().toString());
        Assert.assertEquals("2", alignment.getAlignedElements().get(1).getObj2().toString());
    }

}
