package ca.phon.session;

import ca.phon.orthography.Orthography;
import ca.phon.orthography.parser.OrthoParserException;
import ca.phon.session.tierdata.TierData;
import ca.phon.session.tierdata.TierDataParserException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.ParseException;

@RunWith(JUnit4.class)
public class TestTierDataErrors {

    public void testError(String text, TierDataParserException.Type type, int positionInLine) {
        try {
            final TierData ortho = TierData.parseTierData(text);
            Assert.fail("Passed parsing");
        } catch (ParseException pe) {
            Assert.assertEquals(positionInLine, pe.getErrorOffset());
            Assert.assertTrue(pe.getSuppressed().length > 0);
            Assert.assertEquals(TierDataParserException.class, pe.getSuppressed()[0].getClass());
            Assert.assertEquals(type, ((TierDataParserException)pe.getSuppressed()[0]).getType());
        }
    }

    @Test
    public void testInvalidStartTime() {
        final String txt = "hello •3-4.1•";
        testError(txt, TierDataParserException.Type.InvalidTimeString, 7);
    }

    @Test
    public void testInvalidEndTime() {
        final String txt = "hello •3.-4s.1•";
        testError(txt, TierDataParserException.Type.InvalidTimeString, 10);
    }

    @Test
    public void testMissingEndBullet() {
        final String txt = "hello •3.-4.1 .";
        testError(txt, TierDataParserException.Type.MissingMediaBullet, 13);
    }

    @Test
    public void testMissingStartBullet() {
        final String txt = "hello 3.-4.1• .";
        testError(txt, TierDataParserException.Type.MissingMediaBullet, 12);
    }

}
