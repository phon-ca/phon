package ca.phon.session.io.xml.v2_0;

import ca.phon.ipa.*;
import ca.phon.syllable.SyllabificationInfo;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;
import jakarta.xml.bind.JAXBElement;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class XmlPhoneticTranscriptVisitor extends VisitorAdapter<Object> {

    final IPATranscriptBuilder builder = new IPATranscriptBuilder();

    final IPAElementFactory factory = new IPAElementFactory();

    @Visits
    public void visitPhog(XmlPhoGroupType phog) {
        if(builder.size() > 0 && !(builder.last() instanceof PhoneticGroupMarker pgm && pgm.getType() == PhoneticGroupMarkerType.BEGIN))
            builder.appendWordBoundary();
        builder.appendPgStart();
        for(Object pwOrPause:phog.getPwOrPause()) {
            visit(pwOrPause);
        }
        builder.appendPgEnd();
    }

    @Visits
    public void visitPw(XmlPhoneticWord pw) {
        if(builder.size() > 0 && !(builder.last() instanceof PhoneticGroupMarker pgm && pgm.getType() == PhoneticGroupMarkerType.BEGIN))
            builder.appendWordBoundary();
        pw.getStressOrPhOrCmph().forEach(this::visit);
    }

    @Visits
    public void visitStress(XmlStressType stressType) {
        final StressType type = switch (stressType.getType()) {
            case PRIMARY -> StressType.PRIMARY;
            case SECONDARY -> StressType.SECONDARY;
        };
        builder.appendStress(type);
    }

    @Visits
    public void visitPhone(XmlPhoneType phoneType) {
        final IPAElementFactory factory = new IPAElementFactory();
        final List<String> prefixDiacriticChars = phoneType.getPrefix() != null
                ? phoneType.getPrefix().stream().toList()
                : new ArrayList<>();
        final Diacritic[] prefixDiacritics =
                prefixDiacriticChars.stream().map(factory::createDiacritic).toArray(Diacritic[]::new);

        final Character basePhone = phoneType.getBase().charAt(0);

        final List<Character> combiningDiacriticChars = new ArrayList<>();
        if(phoneType.getCombining() != null) {
            for(String comb:phoneType.getCombining()) {
                combiningDiacriticChars.add(comb.charAt(0));
            }
        }
        final Diacritic[] combiningDiacritics = combiningDiacriticChars.stream().map(factory::createDiacritic).toArray(Diacritic[]::new);

        final List<Character> lengthDiacriticChars = new ArrayList<>();
        if(phoneType.getPhlen() != null) {
            for(char ch:phoneType.getPhlen().toCharArray()) {
                lengthDiacriticChars.add(ch);
            }
        }

        final List<Character> toneNumberDiacriticChars = new ArrayList<>();
        if(phoneType.getToneNumber() != null) {
            for(char ch:phoneType.getToneNumber().toCharArray()) {
                toneNumberDiacriticChars.add(ch);
            }
        }

        final List<Object> suffixDiacriticChars = new ArrayList<>();
        if(phoneType.getSuffix() != null) {
            for(String s:phoneType.getSuffix()) {
                suffixDiacriticChars.add(s);
            }
        }
        suffixDiacriticChars.addAll(lengthDiacriticChars);
        suffixDiacriticChars.addAll(toneNumberDiacriticChars);
        final Diacritic[] suffixDiacritics =
                suffixDiacriticChars.stream().map(obj -> factory.createDiacritic(obj.toString())).toArray(Diacritic[]::new);

        SyllableConstituentType scType = SyllableConstituentType.UNKNOWN;
        if(phoneType.getScType() != null) {
            scType = switch (phoneType.getScType()) {
                case AMBISYLLABIC -> SyllableConstituentType.AMBISYLLABIC;
                case CODA -> SyllableConstituentType.CODA;
                case DIPHTHONG, NUCLEUS -> SyllableConstituentType.NUCLEUS;
                case LEFT_APPENDIX -> SyllableConstituentType.LEFTAPPENDIX;
                case OEHS -> SyllableConstituentType.OEHS;
                case ONSET -> SyllableConstituentType.ONSET;
                case RIGHT_APPENDIX -> SyllableConstituentType.RIGHTAPPENDIX;
            };
        }
        builder.appendPhone(prefixDiacritics, basePhone, combiningDiacritics, suffixDiacritics);
        builder.last().setScType(scType);
        if(phoneType.getScType() == XmlSyllableConstituentType.DIPHTHONG) {
            final SyllabificationInfo syllabificationInfo = builder.last().getExtension(SyllabificationInfo.class);
            syllabificationInfo.setDiphthongMember(true);
        }
    }

    @Visits
    public void visitCompoundPhone(XmlCompoundPhoneType xmlCompoundPhoneType) {
        char ligCh = '\u0361';
        for(JAXBElement<?> ele:xmlCompoundPhoneType.getContent()) {
            if(!ele.getName().getLocalPart().equals("lig")) {
                visit(ele.getValue());
            } else {
                final JAXBElement<XmlLigatureType> ligEle = (JAXBElement<XmlLigatureType>) ele;
                ligCh = switch (ligEle.getValue().getType()) {
                    case BREVE -> '\u0361';
                    case BREVE_BELOW -> '\u035c';
                    case RIGHT_ARROW_BELOW -> '\u0362';
                };
            }
        }
        builder.makeCompoundPhone(ligCh);
    }

    @Visits
    public void visitProsody(XmlPhoneticProsodyType ppType) {
        final IPAElement p = switch (ppType.getType()) {
            case PAUSE, BLOCKING -> factory.createIntraWordPause();
            case CMP -> factory.createCompoundWordMarker();
            case SYLLABLE_BREAK -> factory.createSyllableBoundary();
            case MAJOR_INTONATION_GROUP -> factory.createMajorIntonationGroup();
            case MINOR_INTONATION_GROUP -> factory.createMinorIntonationGroup();
        };
        builder.append(p);
    }

    @Visits
    public void visitSandhi(XmlSandhiType sandhiType) {
        final IPAElement tie = switch (sandhiType.getType()) {
            case LINKER -> factory.createLinker();
            case CONTRACTION -> factory.createContraction();
        };
        builder.append(tie);
    }

    @Visits
    public void visitPause(XmlPauseType pause) {
        if(builder.size() > 0 && !(builder.last() instanceof PhoneticGroupMarker pgm && pgm.getType() == PhoneticGroupMarkerType.BEGIN))
            builder.appendWordBoundary();
        if(pause.getLength() != null) {
            float len = pause.getLength().floatValue();
            builder.appendPause(len);
        } else if(pause.getSymbolicLength() != null) {
            PauseLength len = switch (pause.getSymbolicLength()) {
                case SIMPLE -> PauseLength.SIMPLE;
                case VERY_LONG -> PauseLength.VERY_LONG;
                case LONG -> PauseLength.LONG;
            };
            builder.appendPause(len);
        } else {
            builder.appendPause(PauseLength.SIMPLE);
        }
    }

    public IPATranscript toIPATranscript() throws ParseException {
        return builder.toIPATranscript();
    }

    public String toString() {
        return builder.toString();
    }

    @Override
    public void fallbackVisit(Object obj) {
        throw new IllegalStateException("Type not expected " + obj.getClass());
    }

}
