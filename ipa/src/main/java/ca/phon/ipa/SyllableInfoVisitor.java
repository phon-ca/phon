package ca.phon.ipa;

import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.util.ArrayList;
import java.util.List;

/**
 * A phone visitor which add {@link SyllableInfo} information to a sequence of
 * {@link IPAElement}s.
 */
public class SyllableInfoVisitor extends VisitorAdapter<IPAElement> {

    /**
     * Full transcript builder
     */
    private IPATranscriptBuilder builder = new IPATranscriptBuilder();

    private final IPAElementFactory factory = new IPAElementFactory();

    private boolean segregated = false;

    private int syllableIndex = 0;

    /**
     * current syllable
     *
     */
    protected IPATranscriptBuilder currentSyllableBuilder = new IPATranscriptBuilder();

    /**
     * last phone
     */
    private IPAElement lastPhone = null;

    @Override
    public void fallbackVisit(IPAElement obj) {
        // everything but basic phones and
        // compound phones act as syllable boundaries
        breakSyllable();
        builder.append(obj);
        lastPhone = obj;
    }

    @Visits
    public void visitBasicPhone(Phone phone) {
        appendSyllable(phone);
    }

    @Visits
    public void visitCompoundPhone(CompoundPhone phone) {
        appendSyllable(phone);
    }

    @Visits
    public void visitStressMarker(StressMarker stressMarker) {
        breakSyllable();
        appendSyllable(stressMarker);
    }

    @Visits
    public void visitIntraWordPause(IntraWordPause intraWordPause) {
        breakSyllable();
        builder.append(intraWordPause);
        lastPhone = intraWordPause;
        segregated = true;
    }

    protected void breakSyllable() {
        final IPATranscript currentSyllable = currentSyllableBuilder.toIPATranscript();
        if(currentSyllable.length() > 0) {
            final ToneNumber tone = currentSyllable.elementAt(currentSyllable.length()-1) instanceof ToneNumber
                    ? (ToneNumber)currentSyllable.elementAt(currentSyllable.length()-1)
                    : null;
            for(IPAElement element:currentSyllable) {
                final SyllableInfo currentInfo = element.syllableInfo();
                SyllableInfo syllableInfo = new SyllableInfo(
                        currentInfo.constituentType(),
                        currentInfo.isDiphthong(),
                        currentSyllable.initialStress(),
                        syllableIndex,
                        segregated,
                        currentInfo.sonority(),
                        currentInfo.sonorityDistance(),
                        tone
                );
                final IPAElement newElem = factory.cloneElementWithSyllableInfo(element, syllableInfo);
                builder.append(newElem);
            }
            currentSyllableBuilder = new IPATranscriptBuilder();
            ++syllableIndex;
            segregated = false;
        }
    }

    private void appendSyllable(IPAElement p) {
        if(lastPhone != null) {
            final SyllableConstituentType prevType = lastPhone.constituentType();
            final SyllableConstituentType currentType = p.constituentType();

            switch(prevType) {
                case LEFTAPPENDIX:
                    if(currentType != SyllableConstituentType.LEFTAPPENDIX &&
                            currentType != SyllableConstituentType.ONSET) {
                        breakSyllable();
                    }
                    break;

                case ONSET:
                    if(currentType != SyllableConstituentType.ONSET &&
                            currentType != SyllableConstituentType.NUCLEUS) {
                        breakSyllable();
                    }
                    break;

                case NUCLEUS:
                    if(currentType == SyllableConstituentType.NUCLEUS) {
                        if(!p.isDiphthong()) {
                            breakSyllable();
                        }
                    } else if(currentType != SyllableConstituentType.CODA) {
                        breakSyllable();
                    }
                    break;

                case CODA:
                    if(currentType != SyllableConstituentType.CODA &&
                            currentType != SyllableConstituentType.RIGHTAPPENDIX) {
                        breakSyllable();
                    }
                    break;

                case RIGHTAPPENDIX:
                    if(currentType != SyllableConstituentType.RIGHTAPPENDIX) {
                        breakSyllable();
                    }
                    break;

                case OEHS:
                    if(currentType != SyllableConstituentType.OEHS) {
                        breakSyllable();
                    }
                    break;

                case UNKNOWN:
                    breakSyllable();
                    break;

                default:
                    break;
            }
        }
        currentSyllableBuilder.append(p);

        lastPhone = p;
    }

    @Visits
    public void visitPause(Pause pause) {
        // pauses are syllable boundaries
        breakSyllable();
        currentSyllableBuilder.append(pause);
        breakSyllable();
        lastPhone = null;
    }

    @Visits
    public void visitToneNumber(ToneNumber toneNumber) {
    	currentSyllableBuilder.append(toneNumber);
        breakSyllable();
    	lastPhone = toneNumber;
    }

    public IPATranscript toIPATranscript() {
        breakSyllable();
        return builder.toIPATranscript();
    }

    /**
     * Rest syllable list
     */
    public void reset() {
        this.builder = new IPATranscriptBuilder();
        this.segregated = false;
        this.syllableIndex = 0;
        this.currentSyllableBuilder = new IPATranscriptBuilder();
        this.lastPhone = null;
    }

}