package ca.phon.mor;

import ca.phon.orthography.TerminatorType;
import ca.phon.orthography.mor.Mor;
import ca.phon.orthography.mor.MorTerminator;
import ca.phon.orthography.mor.MorTierData;
import ca.phon.orthography.mor.parser.MorBuilder;
import ca.phon.orthography.mor.parser.MorParserException;
import org.antlr.v4.runtime.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RunWith(JUnit4.class)
public class TestMorParser {

    private MorTierData roundTrip(String text) throws ParseException {
        final MorTierData tierData = MorTierData.parseMorTierData(text);
        Assert.assertEquals(text, tierData.toString());
        return tierData;
    }

    @Test
    public void testMorWord() throws ParseException {
        final String text = "det|the n|people v:aux|be&PRES v|make-ING n|cake-PL .";
        final MorTierData mor = roundTrip(text);
    }

    @Test
    public void testMorPrefix() throws ParseException {
        final String text = "n:prop|Ethan-POSS n|fast mega#un#re#v|work .";
        final MorTierData mor = roundTrip(text);
    }

    @Test
    public void testMorContinuation() throws ParseException {
        final String text = "co|ah co|oui=yes adv|alors pro:subj|je v:mdl|pouvoir&PRES&12s " +
                "v|éteindre-INF prep|dans det:art|le&m&sg n|couloir&m co|oui=yes co|non=no " +
                "pro:subj|elle v:mdl|aller&PRES&3s v|chercher-INF .";
        final MorTierData mor = roundTrip(text);
    }

    @Test
    public void testMorCompound() throws ParseException {
        final String text = "n|+n|ice+n|cream .";
        final MorTierData mor = roundTrip(text);
    }

    @Test
    public void testMorTranslationHyphen() throws ParseException {
        final String text = "co|danke=thank-you .";
        final MorTierData more = roundTrip(text);
    }

    @Test
    public void testMorTranslationUnderscore() throws ParseException {
        final String text = "N|perro-PL=dog_more .";
        final MorTierData mor = roundTrip(text);
    }

    @Test
    public void testMorLong() throws ParseException {
        final String text = "n:prop|Ethan-POSS n|fast anti#dis#v|establish-ment-ari-an-ism .";
        final MorTierData mor = roundTrip(text);
    }

    @Test
    public void testMorPre() throws ParseException {
        final String text = "n:prop|Ethan-POSS n|fast v|da-give$pro|me&dat-me~pro|lo&acc-it .";
        final MorTierData mor = roundTrip(text);
    }

    @Test
    public void testMorClitics() throws ParseException {
        final String text = "pro|you part|go-PROG~inf|to v|put&ZERO det|the " +
                "n|+on|choo+on|choo~v:cop|be&3S n|wheel adv:loc|on ?";
        final MorTierData mor = roundTrip(text);
    }

    @Test
    public void testMorCliticTranslation() throws ParseException {
        final String text = "pro:int|cosa=what v:imp|dice-2S&IMP~pro:clit|1S~pro:clit|3S&MASC=say .";
        final MorTierData mor = roundTrip(text);
    }

    @Test
    public void testMorPost() throws ParseException  {
        final String text = "pro|it~v|be&3S pro|me !";
        final MorTierData mor = roundTrip(text);
    }

    @Test
    public void testTranslations() throws ParseException {
        final String text = "pro:poss|mi=my n|musica=musician/music pro:poss|mi=my .";
        final MorTierData mor = roundTrip(text);
    }

    @Test
    public void testMorTranslationsCompound() throws ParseException {
        final String text = "prep|be=in/at~det|ha=the .";
        final MorTierData mor = roundTrip(text);
    }

    @Test
    public void testMorNumber() throws ParseException {
        final String text = "co|so v|dirty n|soldier~v|be&3S sfp|aa3 .";
        final MorTierData mor = roundTrip(text);
    }

    @Test
    public void testMorOmitted() throws ParseException {
        final String text = "pro|I~v|be&1S det|a n|play 0det|the n|toy-PL .";
        final MorTierData mor = roundTrip(text);
    }

}
