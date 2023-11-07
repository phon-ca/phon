package ca.phon.orthography;

import ca.phon.orthography.parser.OrthoParserException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.ParseException;

@RunWith(JUnit4.class)
public class TestOrthographyParserErrors {

    public void testError(String text, OrthoParserException.Type type, int positionInLine) {
        try {
            final Orthography ortho = Orthography.parseOrthography(text);
            Assert.fail("Passed parsing");
        } catch (ParseException pe) {
            Assert.assertEquals(positionInLine, pe.getErrorOffset());
            Assert.assertTrue(pe.getSuppressed().length > 0);
            Assert.assertEquals(OrthoParserException.class, pe.getSuppressed()[0].getClass());
            Assert.assertEquals(type, ((OrthoParserException)pe.getSuppressed()[0]).getType());
        }
    }

    // region Linkers
    @Test
    public void testLinkerOutOfPlace() {
        final String txt = "hello " + LinkerType.LAZY_OVERLAP_MARK.getText() + " .";
        testError(txt, OrthoParserException.Type.OutOfPlace, 6);
    }
    // endregion Linkers

    // region Terminators
    @Test
    public void testDoubleTerminator() {
        final String txt = "hello world . ?";
        testError(txt, OrthoParserException.Type.TerminatorAlreadySpecified, 14);
    }

    @Test
    public void testWordAfterTerminator() {
        final String txt = "hello . world";
        testError(txt, OrthoParserException.Type.ContentAfterTerminator, 8);
    }

    @Test
    public void testGroupAfterTerminator() {
        final String txt = "hello . <world>";
        testError(txt, OrthoParserException.Type.ContentAfterTerminator, 8);
    }

    @Test
    public void testPhoneticGroupAfterTerminator() {
        final String txt = "hello . \u2039world\u203a";
        testError(txt, OrthoParserException.Type.ContentAfterTerminator, 8);
    }

    @Test
    public void testSymbolicPauseAfterTerminator() {
        final String txt = "hello . (..)";
        testError(txt, OrthoParserException.Type.ContentAfterTerminator, 8);
    }

    @Test
    public void testNumericPauseAfterTerminator() {
        final String txt = "hello . (3.)";
        testError(txt, OrthoParserException.Type.ContentAfterTerminator, 8);
    }

    @Test
    public void testFreecodeAfterTerminator() {
        final String txt = "hello . [^ foobar]";
        testError(txt, OrthoParserException.Type.ContentAfterTerminator, 8);
    }

    @Test
    public void testOteAfterTeminator() {
        final String txt = "hello . &*CHI=test";
        testError(txt, OrthoParserException.Type.ContentAfterTerminator, 8);
    }

    @Test
    public void testHappeningAfterTerminator() {
        final String txt = "hello . &=test";
        testError(txt, OrthoParserException.Type.ContentAfterTerminator, 8);
    }

    @Test
    public void testSeparatorAfterTerminator() {
        final String txt = "hello . ;";
        testError(txt, OrthoParserException.Type.ContentAfterTerminator, 8);
    }

    @Test
    public void testToneMarkerAfterTerminator() {
        final String txt = "hello . ;";
        testError(txt, OrthoParserException.Type.ContentAfterTerminator, 8);
    }

    @Test
    public void testTagMarkerAfterTerminator() {
        final String txt = "hello . ,";
        testError(txt, OrthoParserException.Type.ContentAfterTerminator, 8);
    }

    @Test
    public void testLongFeatureAfterTerminator() {
        final String txt = "hello . &{l=Test hello &}l=Test";
        testError(txt, OrthoParserException.Type.ContentAfterTerminator, 8);
    }

    @Test
    public void testNonvocalAfterTerminator() {
        final String txt = "hello . &{n=Test &}n=Test";
        testError(txt, OrthoParserException.Type.ContentAfterTerminator, 8);
    }

    @Test
    public void testQuotationAfterTerminator() {
        final String txt = "hello . \u201c";
        testError(txt, OrthoParserException.Type.ContentAfterTerminator, 8);
    }
    // endregion Terminators

    // region InternalMedia
    @Test
    public void testTooManyMedia() {
        final String txt = "hello . •3.-4.1• •3.-4.1•";
        testError(txt, OrthoParserException.Type.MediaAlreadySpecified, 17);
    }

    @Test
    public void testInvalidStartTime() {
        final String txt = "hello . •3-4.1•";
        testError(txt, OrthoParserException.Type.InvalidTimeString, 9);
    }

    @Test
    public void testInvalidEndTime() {
        final String txt = "hello . •3.-4s.1•";
        testError(txt, OrthoParserException.Type.InvalidTimeString, 12);
    }

    @Test
    public void testMissingEndBullet() {
        final String txt = "hello •3.-4.1 .";
        testError(txt, OrthoParserException.Type.MissingMediaBullet, 13);
    }

    @Test
    public void testMissingStartBullet() {
        final String txt = "hello 3.-4.1• .";
        testError(txt, OrthoParserException.Type.InvalidToken, 8);
    }
    // endregion InternalMedia

    @Test
    public void testReplacementWithoutContent() {
        final String txt = "[: hello] .";
        testError(txt, OrthoParserException.Type.ReplacementWithoutContent, 0);
    }

    @Test
    public void testErrorWithoutContent() {
        final String txt = "[*] .";
        testError(txt, OrthoParserException.Type.AnnotationWithoutContent, 0);
    }

    @Test
    public void testPostcodeBeforeTerminator() {
        final String txt = "hello [+ foo] .";
        testError(txt, OrthoParserException.Type.OutOfPlace, 6);
    }

    @Test
    public void testHangingGroup() {
        final String txt = "hello <goodbye .";
        testError(txt, OrthoParserException.Type.MissingGroupEnd, 6);
    }

    @Test
    public void testHangingPhoneticGroup() {
        final String txt = "hello \u2039goodbye .";
        testError(txt, OrthoParserException.Type.MissingPgEnd, 6);
    }

    @Test
    public void testOpenGroup() {
        final String txt = "hello> !";
        testError(txt, OrthoParserException.Type.MissingGroupStart, 5);
    }

    @Test
    public void testOpenPg() {
        final String txt = "hello\u203a !";
        testError(txt, OrthoParserException.Type.MissingPgStart, 5);
    }

    @Test
    public void testHangingBracket() {
        final String txt = "hello [ world !";
        testError(txt, OrthoParserException.Type.InvalidAnnotation, 7);
    }

    @Test
    public void testHangingComment() {
        final String txt = "hello [% world !";
        testError(txt, OrthoParserException.Type.MissingCloseBracket, 7);
    }

    @Test
    public void testHangingFreecode() {
        final String txt = "hello [^ foobar !";
        testError(txt, OrthoParserException.Type.MissingCloseBracket, 7);
    }

    @Test
    public void testHangingPostcode() {
        final String txt = "hello foobar ! [+ xcl";
        testError(txt, OrthoParserException.Type.MissingCloseBracket, 15);
    }

    @Test
    public void testMissingOpenBracket() {
        final String txt = "hello world] !";
        testError(txt, OrthoParserException.Type.MissingOpenBracket, 11);
    }

}
