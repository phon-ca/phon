package ca.phon.orthography;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.ParseException;

@RunWith(JUnit4.class)
public class TestOrthoElementLocator {

    @Test
    public void testGroups() throws ParseException {
        final String text = "[- eng] <this is> [!!] ‹a test› !";
        final int[] primaryIndexes = new int[]{0, 8, 23, 32};
        final int[] groupIndexes = new int[] {9, 14};
        final int annotationIndex = 18;
        final int[] pgIndexes = new int[] {24, 26};
        final Orthography orthography = Orthography.parseOrthography(text);
        Assert.assertEquals(primaryIndexes.length, orthography.length());
        for(int i = 0; i < primaryIndexes.length; i++) {
            final OrthographyElement element = orthography.elementAt(i);
            Assert.assertEquals(primaryIndexes[i], orthography.stringIndexOf(element));
        }

        Assert.assertEquals(OrthoGroup.class, orthography.elementAt(1).getClass());
        final OrthoGroup group = (OrthoGroup) orthography.elementAt(1);
        Assert.assertEquals(groupIndexes.length, group.getElements().size());
        for(int i = 0; i < group.getElements().size(); i++) {
            final OrthographyElement element = group.getElements().get(i);
            Assert.assertEquals(groupIndexes[i], orthography.stringIndexOf(element));
        }
        Assert.assertEquals(1, group.getAnnotations().size());
        Assert.assertEquals(annotationIndex, orthography.stringIndexOf(group.getAnnotations().get(0)));

        Assert.assertEquals(PhoneticGroup.class, orthography.elementAt(2).getClass());
        final PhoneticGroup pg = (PhoneticGroup) orthography.elementAt(2);
        Assert.assertEquals(pgIndexes.length, pg.getElements().size());
        for(int i = 0; i < pgIndexes.length; i++) {
            final OrthographyElement element = pg.getElements().get(i);
            Assert.assertEquals(pgIndexes[i], orthography.stringIndexOf(element));
        }
    }

    @Test
    public void testAction() throws ParseException {
        final String text = "what do 0 [% foobar] . [+ bar]";
        final Orthography orthography = Orthography.parseOrthography(text);
        final int[] primaryIndexes = new int[]{0, 5, 8, 21, 23};
        final int commentIndex = 10;
        Assert.assertEquals(primaryIndexes.length, orthography.length());
        for(int i = 0; i < primaryIndexes.length; i++) {
            final OrthographyElement element = orthography.elementAt(i);
            Assert.assertEquals(primaryIndexes[i], orthography.stringIndexOf(element));
        }

        Assert.assertEquals(Action.class, orthography.elementAt(2).getClass());
        Action action = (Action) orthography.elementAt(2);
        Assert.assertEquals(1, action.getAnnotations().size());
        Assert.assertEquals(commentIndex, orthography.stringIndexOf(action.getAnnotations().get(0)));
    }

    @Test
    public void testHappening() throws ParseException {
        final String text = "what do &=test [# 1.] . [+ bar]";
        final Orthography orthography = Orthography.parseOrthography(text);
        final int[] primaryIndexes = new int[]{0, 5, 8, 22, 24};
        final int durationIndex = 15;
        Assert.assertEquals(primaryIndexes.length, orthography.length());
        for(int i = 0; i < primaryIndexes.length; i++) {
            final OrthographyElement element = orthography.elementAt(i);
            Assert.assertEquals(primaryIndexes[i], orthography.stringIndexOf(element));
        }

        Assert.assertEquals(Happening.class, orthography.elementAt(2).getClass());
        Happening h = (Happening) orthography.elementAt(2);
        Assert.assertEquals(1, h.getAnnotations().size());
        Assert.assertEquals(durationIndex, orthography.stringIndexOf(h.getAnnotations().get(0)));
    }

    @Test
    public void testOtherSpokenEvent() throws ParseException {
        final String text = "what do &*CHI:test [# 1.] . [+ bar]";
        final Orthography orthography = Orthography.parseOrthography(text);
        final int[] primaryIndexes = new int[]{0, 5, 8, 26, 28};
        final int durationIndex = 19;
        Assert.assertEquals(primaryIndexes.length, orthography.length());
        for(int i = 0; i < primaryIndexes.length; i++) {
            final OrthographyElement element = orthography.elementAt(i);
            Assert.assertEquals(primaryIndexes[i], orthography.stringIndexOf(element));
        }

        Assert.assertEquals(OtherSpokenEvent.class, orthography.elementAt(2).getClass());
        OtherSpokenEvent ote = (OtherSpokenEvent) orthography.elementAt(2);
        Assert.assertEquals(1, ote.getAnnotations().size());
        Assert.assertEquals(durationIndex, orthography.stringIndexOf(ote.getAnnotations().get(0)));
    }

    @Test
    public void testReplacement() throws ParseException {
        final String text = "hello [: goodbye] world [:: sanity] !";
        final Orthography orthography = Orthography.parseOrthography(text);
        final int[] primaryIndexes = new int[]{0, 18, 36};
        final int r1Idx = 6;
        final int r2Idx = 24;
        Assert.assertEquals(primaryIndexes.length, orthography.length());
        for(int i = 0; i < primaryIndexes.length; i++) {
            final OrthographyElement element = orthography.elementAt(i);
            Assert.assertEquals(primaryIndexes[i], orthography.stringIndexOf(element));
        }

        Assert.assertEquals(Word.class, orthography.elementAt(0).getClass());
        Word w1 = (Word)orthography.elementAt(0);
        Assert.assertEquals(1, w1.getReplacements().size());
        Assert.assertEquals(r1Idx, orthography.stringIndexOf(w1.getReplacements().get(0)));

        Assert.assertEquals(Word.class, orthography.elementAt(1).getClass());
        Word w2 = (Word)orthography.elementAt(1);
        Assert.assertEquals(1, w2.getReplacements().size());
        Assert.assertEquals(r2Idx, orthography.stringIndexOf(w2.getReplacements().get(0)));
    }

}
