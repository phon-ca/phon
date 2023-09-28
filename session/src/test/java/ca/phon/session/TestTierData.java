package ca.phon.session;

import ca.phon.session.tierdata.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.ParseException;

@RunWith(JUnit4.class)
public class TestTierData {

    private TierData roundTripTest(String text) throws ParseException {
        final TierData tierData = TierData.parseTierData(text);
        Assert.assertEquals(text, tierData.toString());
        return tierData;
    }

    @Test
    public void testEmptyString() throws ParseException {
        roundTripTest("");
    }

    @Test
    public void testWords() throws ParseException {
        final String text = "hello world";
        final TierData tierData = roundTripTest(text);
        Assert.assertEquals(2, tierData.size());
        Assert.assertEquals(TierString.class, tierData.elementAt(0).getClass());
        final TierString w1 = (TierString) tierData.elementAt(0);
        Assert.assertEquals("hello", w1.toString());
        Assert.assertEquals(TierString.class, tierData.elementAt(1).getClass());
        final TierString w2 = (TierString) tierData.elementAt(1);
        Assert.assertEquals("world", w2.toString());
    }

    @Test
    public void testInternalMedia() throws ParseException {
        final String text = "hello •0.-0.5• world •0.5-1.2•";
        final TierData tierData = roundTripTest(text);
        Assert.assertEquals(4, tierData.size());
        Assert.assertEquals(TierString.class, tierData.elementAt(0).getClass());
        final TierString w1 = (TierString) tierData.elementAt(0);
        Assert.assertEquals("hello", w1.text());
        Assert.assertEquals(TierInternalMedia.class, tierData.elementAt(1).getClass());
        final TierInternalMedia media = (TierInternalMedia) tierData.elementAt(1);
        Assert.assertEquals(0.0f, media.getInternalMedia().getStartTime(), 0.001f);
        Assert.assertEquals(0.5f, media.getInternalMedia().getEndTime(), 0.001f);
        Assert.assertEquals(TierString.class, tierData.elementAt(2).getClass());
        final TierString w2 = (TierString) tierData.elementAt(2);
        Assert.assertEquals("world", w2.text());
        Assert.assertEquals(TierInternalMedia.class, tierData.elementAt(3).getClass());
        final TierInternalMedia media1 = (TierInternalMedia) tierData.elementAt(3);
        Assert.assertEquals(0.5f, media1.getInternalMedia().getStartTime(), 0.001f);
        Assert.assertEquals(1.2f, media1.getInternalMedia().getEndTime(), 0.001f);
    }

    @Test
    public void testComments() throws ParseException {
        final String text = "goodbye [% waves] [% sanity]";
        final TierData tierData = roundTripTest(text);
        Assert.assertEquals(3, tierData.size());
        Assert.assertEquals(TierString.class, tierData.elementAt(0).getClass());
        final TierString w1 = (TierString) tierData.elementAt(0);
        Assert.assertEquals("goodbye", w1.text());
        Assert.assertEquals(TierComment.class, tierData.elementAt(1).getClass());
        final TierComment tc1 = (TierComment) tierData.elementAt(1);
        Assert.assertEquals("waves", tc1.text());
        Assert.assertEquals(TierComment.class, tierData.elementAt(2).getClass());
        final TierComment tc2 = (TierComment) tierData.elementAt(2);
        Assert.assertEquals("sanity", tc2.text());
    }

    @Test
    public void testLinkWithLabel() throws ParseException {
        final String text = "goodbye \uD83D\uDD17pic __res/pics/test.png\uD83D\uDD17";
        final TierData tierData = roundTripTest(text);
        Assert.assertEquals(2, tierData.size());
        Assert.assertEquals(TierString.class, tierData.elementAt(0).getClass());
        Assert.assertEquals(TierLink.class, tierData.elementAt(1).getClass());
        final TierLink link = (TierLink)tierData.elementAt(1);
        Assert.assertEquals("pic", link.getLabel());
        Assert.assertEquals("__res/pics/test.png", link.getHref());
    }

    @Test
    public void testWebLink() throws ParseException {
        final String text = "goodbye \uD83D\uDD17https://phon.ca/ns/session\uD83D\uDD17";
        final TierData tierData = roundTripTest(text);
        Assert.assertEquals(2, tierData.size());
        Assert.assertEquals(TierString.class, tierData.elementAt(0).getClass());
        Assert.assertEquals(TierLink.class, tierData.elementAt(1).getClass());
        final TierLink link = (TierLink)tierData.elementAt(1);
        Assert.assertEquals("https://phon.ca/ns/session", link.getHref());
    }

}
