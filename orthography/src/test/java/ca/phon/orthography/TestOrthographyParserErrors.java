package ca.phon.orthography;

import ca.phon.orthography.parser.OrthoParserException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.ParseException;

@RunWith(JUnit4.class)
public class TestOrthographyParserErrors {

    @Test
    public void testDoubleTerminator() {
        final String txt = "hello world . ?";
        try {
            Orthography.parseOrthography(txt);
            Assert.fail("Passed parsing");
        } catch (ParseException pe) {
            Assert.assertEquals(14, pe.getErrorOffset());
            Assert.assertTrue(pe.getSuppressed().length > 0);
            Assert.assertEquals(OrthoParserException.class, pe.getSuppressed()[0].getClass());
            Assert.assertEquals(OrthoParserException.Type.TooManyTerminators, ((OrthoParserException)pe.getSuppressed()[0]).getType());
        }
    }

    @Test
    public void testWordAfterTerminator() {
        final String txt = "hello . world";
        try {
            Orthography.parseOrthography(txt);
            Assert.fail("Passed parsing");
        } catch (ParseException pe) {
            Assert.assertEquals(8, pe.getErrorOffset());
            Assert.assertTrue(pe.getSuppressed().length > 0);
            Assert.assertEquals(OrthoParserException.class, pe.getSuppressed()[0].getClass());
            Assert.assertEquals(OrthoParserException.Type.ContentAfterTerminator, ((OrthoParserException)pe.getSuppressed()[0]).getType());
        }
    }

}
