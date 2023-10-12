package ca.phon.session;

import ca.phon.orthography.mor.GraspTierData;
import ca.phon.orthography.mor.MorTierData;
import ca.phon.session.io.xml.OneToOne;
import ca.phon.session.io.xml.XMLFragments;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.HashMap;

@RunWith(JUnit4.class)
public class TestFragments {

    @Test
    public void testOrthographyIPA() throws IOException {
        final String expected = """
                <u>
                  <w>hello<mod><pw><ph><base>h</base></ph><ph><base>e</base></ph><ph><base>l</base></ph><ph><base>l</base></ph><ph><base>o</base></ph></pw></mod><pho><pw><ph><base>e</base></ph><ph><base>l</base></ph><ph><base>o</base></ph></pw></pho></w>
                  <pause symbolic-length="simple"><mod><pause symbolic-length="simple"></pause></mod><pho><pause symbolic-length="simple"></pause></pho></pause>
                  <w>world<mod><pw><ph><base>w</base></ph><ph><base>o</base></ph><ph><base>r</base></ph><ph><base>l</base></ph><ph><base>d</base></ph></pw></mod><pho><pw><ph><base>o</base></ph><ph><base>r</base></ph><ph><base>d</base></ph></pw></pho></w>
                  <t type="p"></t>
                </u>""";

        final SessionFactory factory = SessionFactory.newFactory();
        final Record record = factory.createRecord();
        record.getOrthographyTier().setText("hello (.) world .");
        record.getIPATargetTier().setText("hello (.) world");
        record.getIPAActualTier().setText("elo (.) ord");
        OneToOne.annotateRecord(record);

        final String xml = XMLFragments.toXml(record.getOrthography(), false, true);
        System.out.println(xml);
        Assert.assertEquals(expected, xml);
    }

    @Test
    public void testOrthographyMor() throws IOException {
        final String expected = """
                <u>
                  <w>hello<mor type="mor"><mw><pos><c>v</c></pos><stem>hello</stem></mw><gra type="gra" index="0" head="1" relation="FOO"></gra></mor></w>
                  <w>world<mor type="mor"><mw><pos><c>n</c></pos><stem>world</stem></mw><gra type="gra" index="1" head="0" relation="BAR"></gra></mor></w>
                  <t type="p">
                    <mor type="mor"><mt type="p"></mt><gra type="gra" index="3" head="0" relation="PUNCT"></gra></mor>
                  </t>
                </u>""";

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
        Assert.assertEquals(expected, xml);
    }

    @Test
    public void testOrthographyTrn() throws IOException {
        final String expected = """
                <u>
                  <w>hello<mor type="trn"><mw><pos><c>v</c></pos><stem>hello</stem></mw><gra type="grt" index="0" head="1" relation="FOO"></gra></mor></w>
                  <w>world<mor type="trn"><mw><pos><c>n</c></pos><stem>world</stem></mw><gra type="grt" index="1" head="0" relation="BAR"></gra></mor></w>
                  <t type="p">
                    <mor type="trn"><mt type="p"></mt><gra type="grt" index="3" head="0" relation="PUNCT"></gra></mor>
                  </t>
                </u>""";

        final SessionFactory factory = SessionFactory.newFactory();
        final Record record = factory.createRecord();
        record.getOrthographyTier().setText("hello world .");
        final Tier<MorTierData> morTier = factory.createTier(UserTierType.Trn.getTierName(), MorTierData.class, new HashMap<>(), true);
        morTier.setText("v|hello n|world .");
        final Tier<GraspTierData> graTier = factory.createTier(UserTierType.Grt.getTierName(), GraspTierData.class, new HashMap<>(), true);
        graTier.setText("0|1|FOO 1|0|BAR 3|0|PUNCT");
        record.putTier(morTier);
        record.putTier(graTier);
        OneToOne.annotateRecord(record);

        final String xml = XMLFragments.toXml(record.getOrthography(), false, true);
        System.out.println(xml);
        Assert.assertEquals(expected, xml);
    }

    @Test
    public void testMorCompoundAndPostclitic() throws IOException {
        final String expected = """
                <u>
                  <w>you<mor type="trn"><mw><pos><c>pro</c></pos><stem>you</stem></mw><gra type="grt" index="1" head="2" relation="SUBJ"></gra></mor></w>
                  <w>gonna<mor type="trn"><mw><pos><c>part</c></pos><stem>go</stem><mk type="sfx">PROG</mk></mw><gra type="grt" index="2" head="0" relation="ROOT"></gra><mor-post><mw><pos><c>inf</c></pos><stem>to</stem></mw><gra type="grt" index="3" head="4" relation="INF"></gra></mor-post></mor></w>
                  <w>put<mor type="trn"><mw><pos><c>v</c></pos><stem>put</stem><mk type="sfxf">ZERO</mk></mw><gra type="grt" index="4" head="2" relation="XCOMP"></gra></mor></w>
                  <w>the<mor type="trn"><mw><pos><c>det</c></pos><stem>the</stem></mw><gra type="grt" index="5" head="6" relation="DET"></gra></mor></w>
                  <w>choo<wk type="cmp"></wk>choo's<mor type="trn"><mwc><pos><c>n</c></pos><mw><pos><c>on</c></pos><stem>choo</stem></mw><mw><pos><c>on</c></pos><stem>choo</stem></mw></mwc><gra type="grt" index="6" head="7" relation="SUBJ"></gra><mor-post><mw><pos><c>v</c><subc>cop</subc></pos><stem>be</stem><mk type="sfxf">3S</mk></mw><gra type="grt" index="7" head="4" relation="OBJ"></gra></mor-post></mor></w>
                  <w>wheel<mor type="trn"><mw><pos><c>n</c></pos><stem>wheel</stem></mw><gra type="grt" index="8" head="7" relation="PRED"></gra></mor></w>
                  <w>on<mor type="trn"><mw><pos><c>adv</c><subc>loc</subc></pos><stem>on</stem></mw><gra type="grt" index="9" head="8" relation="JCT"></gra></mor></w>
                  <t type="q">
                    <mor type="trn"><mt type="q"></mt><gra type="grt" index="10" head="2" relation="PUNCT"></gra></mor>
                  </t>
                </u>""";

        final SessionFactory factory = SessionFactory.newFactory();
        final Record record = factory.createRecord();
        record.getOrthographyTier().setText("you gonna put the choo+choo's wheel on ?");
        final Tier<MorTierData> morTier = factory.createTier(UserTierType.Trn.getTierName(), MorTierData.class, new HashMap<>(), true);
        morTier.setText("pro|you part|go-PROG~inf|to v|put&ZERO det|the n|+on|choo+on|choo~v:cop|be&3S n|wheel adv:loc|on ?");
        final Tier<GraspTierData> graTier = factory.createTier(UserTierType.Grt.getTierName(), GraspTierData.class, new HashMap<>(), true);
        graTier.setText("1|2|SUBJ 2|0|ROOT 3|4|INF 4|2|XCOMP 5|6|DET 6|7|SUBJ 7|4|OBJ 8|7|PRED 9|8|JCT 10|2|PUNCT");
        record.putTier(morTier);
        record.putTier(graTier);
        OneToOne.annotateRecord(record);

        final String xml = XMLFragments.toXml(record.getOrthography(), false, true);
        System.out.println(xml);
        Assert.assertEquals(expected, xml);
    }

}
