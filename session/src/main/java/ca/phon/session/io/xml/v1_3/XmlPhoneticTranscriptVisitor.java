package ca.phon.session.io.xml.v1_3;

import ca.phon.ipa.IPATranscript;
import ca.phon.session.io.xml.v13.*;
import ca.phon.syllable.SyllableStress;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.text.ParseException;

public class XmlPhoneticTranscriptVisitor extends VisitorAdapter<Object> {

    final StringBuilder builder = new StringBuilder();

    @Visits
    public void visitPw(PhoneticWord pw) {
        if(builder.length() > 0)
            builder.append(' ');
        pw.getStressOrPhOrPp().forEach(this::visit);
    }

    @Visits
    public void visitStress(StressType stressType) {
        builder.append(stressType.getType() == StressTypeType.PRIMARY ?
                SyllableStress.PrimaryStress.getIpa() : SyllableStress.SecondaryStress.getIpa());
    }

    @Visits
    public void visitPhone(PhoneType phoneType) {
        final String phoneText = phoneType.getContent();
        String scType = "";
        if(phoneType.getScType() != null) {
            scType = switch (phoneType.getScType()) {
                case AMBISYLLABIC -> ":A";
                case CODA -> ":C";
                case DIPHTHONG -> ":D";
                case LEFT_APPENDIX -> ":L";
                case NUCLEUS -> ":N";
                case OEHS -> ":E";
                case ONSET -> ":O";
                case RIGHT_APPENDIX -> ":R";
            };
        }
        builder.append(phoneText).append(scType);
    }

    @Visits
    public void visitProsody(PhoneticProsodyType ppType) {
        final String p = switch (ppType.getType()) {
            case PAUSE, BLOCKING -> "^";
            case CMP -> "+";
            case SYLLABLE_BREAK -> ".";
            case MAJOR_INTONATION_GROUP -> "‖";
            case MINOR_INTONATION_GROUP -> "|";
        };
        builder.append(p);
    }

    @Visits
    public void visitSandhi(SandhiType sandhiType) {
        final String tie = switch (sandhiType.getType()) {
            case LINKER -> "‿";
            case CONTRACTION -> "⁀";
        };
        builder.append(tie);
    }

    @Visits
    public void visitToneNumber(ToneNumberType toneNumberType) {
        // TODO
    }

    @Visits
    public void visitPause(Pause pause) {

    }

    public IPATranscript toIPATranscript() throws ParseException {
        final IPATranscript retVal = IPATranscript.parseIPATranscript(builder.toString());
        return retVal;
    }

    public String toString() {
        return builder.toString();
    }

    @Override
    public void fallbackVisit(Object obj) {
        throw new IllegalStateException("Type not expected " + obj.getClass());
    }

}