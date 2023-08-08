package ca.phon.session;

import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.*;
import ca.phon.session.alignment.TierAlignmentRules;
import ca.phon.session.alignment.aligners.*;
import ca.phon.session.usertier.UserTierData;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Map;

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
        final Tier<UserTierData> testTier1 = factory.createTier("Test1", UserTierData.class, TierAlignmentRules.userTierRules());
        testTier1.setText("V N");
        record.putTier(testTier1);
        final Tier<Orthography> testTier2 = factory.createTier("Test2", Orthography.class, TierAlignmentRules.orthographyTierRules());
        testTier2.setText("goodbye (.) sanity !");
        record.putTier(testTier2);
        final Tier<UserTierData> testTier3 = factory.createTier("Test3", UserTierData.class, TierAlignmentRules.userTierRules());
        testTier3.setText("1 [% test] 2");
        record.putTier(testTier3);
        return record;
    }

    @Test
    public void testOrthoToOrthoAligner() {
        final Record testRecord = createTestRecord();
        final Tier<Orthography> tier1 = testRecord.getOrthographyTier();
        final Tier<Orthography> tier2 = testRecord.getTier("Test2", Orthography.class);
        final OrthoToOrthoAligner aligner = new OrthoToOrthoAligner();
        var alignment = aligner.calculateAlignment(tier1, tier2, tier2.getTierAlignmentRules());
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
        final Record testRecord = createTestRecord();
        final Tier<Orthography> tier1 = testRecord.getOrthographyTier();
        final Tier<IPATranscript> tier2 = testRecord.getIPAActualTier();
        final OrthoToIPAAligner aligner = new OrthoToIPAAligner();
        var alignment = aligner.calculateAlignment(tier1, tier2, tier2.getTierAlignmentRules());
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
        final OrthoToPhoneAlignmentAligner aligner = new OrthoToPhoneAlignmentAligner();
        var alignment = aligner.calculateAlignment(tier1, tier2, tier2.getTierAlignmentRules());
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
        final Tier<UserTierData> tier2 = testRecord.getTier("Test1", UserTierData.class);
        final OrthoToUserTierAligner aligner = new OrthoToUserTierAligner();
        var alignment = aligner.calculateAlignment(tier1, tier2, tier2.getTierAlignmentRules());
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
        final IPAtoIPAAligner aligner = new IPAtoIPAAligner();
        var alignment = aligner.calculateAlignment(tier1, tier2, tier2.getTierAlignmentRules());
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
        final IPAToPhoneAlignmentAligner aligner = new IPAToPhoneAlignmentAligner();
        var alignment = aligner.calculateAlignment(tier1, tier2, tier2.getTierAlignmentRules());
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
        final Tier<UserTierData> tier2 = testRecord.getTier("Test1", UserTierData.class);
        final IPAToUserTierAligner aligner = new IPAToUserTierAligner();
        var alignment = aligner.calculateAlignment(tier1, tier2, tier2.getTierAlignmentRules());
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
        final Tier<UserTierData> tier2 = testRecord.getTier("Test1", UserTierData.class);
        final PhoneAlignmentToUserTierAligner aligner = new PhoneAlignmentToUserTierAligner();
        var alignment = aligner.calculateAlignment(tier1, tier2, tier2.getTierAlignmentRules());
//        Assert.assertEquals(2, alignment.length());
    }

    @Test
    public void testUserTierToUserTierAligner() {
        final Record testRecord = createTestRecord();
        final Tier<UserTierData> tier1 = testRecord.getTier("Test1", UserTierData.class);
        final Tier<UserTierData> tier2 = testRecord.getTier("Test3", UserTierData.class);
        final UserTierToUserTierAligner aligner = new UserTierToUserTierAligner();
        var alignment = aligner.calculateAlignment(tier1, tier2, tier2.getTierAlignmentRules());
        Assert.assertEquals(2, alignment.length());
        Assert.assertEquals("V", alignment.getAlignedElements().get(0).getObj1().toString());
        Assert.assertEquals("1", alignment.getAlignedElements().get(0).getObj2().toString());
        Assert.assertEquals("N", alignment.getAlignedElements().get(1).getObj1().toString());
        Assert.assertEquals("2", alignment.getAlignedElements().get(1).getObj2().toString());
    }

}
