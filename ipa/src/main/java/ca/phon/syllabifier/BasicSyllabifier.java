package ca.phon.syllabifier;

import ca.phon.ipa.*;
import ca.phon.phonex.PhonexPattern;
import ca.phon.util.Language;
import ca.phon.visitor.Visitor;

import java.util.List;

/**
 * Basic syllabifier implementation.
 *
 * <p>This syllabifier uses a series of phonex expressions to mark phones with
 * sonority and syllable constituent information.  The syllabifier then uses
 * a series of phonex expressions to identify syllable constituents and syllable
 * boundaries.</p>
 *
 * <p>The returned transcript is a new, annotated version of the transcript
 * object being syllabified.</p>
 */
public class BasicSyllabifier implements Syllabifier {

    private final String name;
    private final Language language;
    private final List<SonorityClass> sonorityClasses;
    private final List<SyllabifierStep> syllabifierSteps;

    public BasicSyllabifier(String name, Language language, List<SonorityClass> sonorityClasses, List<SyllabifierStep> syllabifierSteps) {
        this.name = name;
        this.language = language;
        this.sonorityClasses = sonorityClasses;
        this.syllabifierSteps = syllabifierSteps;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Language getLanguage() {
        return language;
    }

    @Override
    public IPATranscript syllabify(IPATranscript transcript) {
        IPATranscript retVal = markSonority(transcript);
        for(SyllabifierStep step:syllabifierSteps) {
            retVal = runStep(step, retVal);
        }
        // add SyllableInfo
        final SyllableInfoVisitor visitor = new SyllableInfoVisitor();
        retVal.accept(visitor);
        retVal = visitor.toIPATranscript();
        return retVal;
    }

    /**
     * Mark sonority on given elements.  This function will return a new
     * IPATranscript object where the sonority information has been added
     * to each phone in it's SyllableInfo data.
     *
     * @param transcript
     * @return new IPATranscript with sonority information added
     */
    private IPATranscript markSonority(IPATranscript transcript) {
        final SonorityVisitor visitor = new SonorityVisitor();
        transcript.accept(visitor);
        return visitor.builder.toIPATranscript();
    }

    /**
     * Run a syllabifier step on the given elements.  The executes a phonex pattern
     * match and marks the SyllableConstituentType on any phones in named groups
     * in the pattern. The name of the group should be one of the SyllableConstituentType
     * enum values (case insensitive). If the group name is "D" then the phones in that
     * group will also be marked as diphthong members.
     *
     * @param step the syllabifier step
     * @param transcript the transcript
     * @return the modified transcript with syllable constituent information added.  This is
     * a new object, the original transcript is not modified.
     */
    private IPATranscript runStep(SyllabifierStep step, IPATranscript transcript) {
        final PhonexPattern pattern = step.pattern();
        final var m = pattern.matcher(transcript);

        final IPATranscriptBuilder builder = new IPATranscriptBuilder();
        int lastEnd = 0;
        while(m.find()) {
            for(int i = 1; i <= pattern.numberOfGroups(); i++) {
                final String name = pattern.groupName(i);
                if(name != null) {
                    final SyllableConstituentType scType = SyllableConstituentType.fromString(name);
                    if(scType != null) {
                        // add all elements before match
                        for(int j = lastEnd; j < m.start(); j++) {
                            builder.append(transcript.elementAt(j));
                        }
                        final List<IPAElement> group = m.group(i);
                        for(IPAElement ele:group) {
                            if(ele instanceof Phone phone) {
                                boolean isDiphthongMember = "D".equalsIgnoreCase(name);
                                final SyllableInfo syllableInfo = new SyllableInfo(
                                    scType,
                                    isDiphthongMember,
                                    phone.syllableInfo().stress(),
                                    phone.syllableInfo().syllableIndex(),
                                    phone.syllableInfo().segregated(),
                                    phone.syllableInfo().sonority(),
                                    phone.syllableInfo().sonorityDistance(),
                                    phone.syllableInfo().tone()
                                );
                                builder.append((new IPAElementFactory()).clonePhoneWithSyllableInfo(phone, syllableInfo));
                            } else {
                                builder.append(ele);
                            }
                        }
                        // update end
                        lastEnd = m.end();
                    }
                }
            }
        }
        // add remaining elements
        for(int j = lastEnd; j < transcript.length(); j++) {
            builder.append(transcript.elementAt(j));
        }
        return builder.toIPATranscript();
    }

    public record SonorityClass(int sonorityValue, PhonexPattern pattern) {
    }

    public record SyllabifierStep(String name, PhonexPattern pattern) {
    }

    private class SonorityVisitor implements Visitor<IPAElement> {

        private final IPATranscriptBuilder builder = new IPATranscriptBuilder();

        private int lastSonority = 0;

        @Override
        public void visit(IPAElement obj) {
            if (obj instanceof Phone phone) {
                attachSonority(phone);
            } else {
                builder.append(obj);
            }
        }

        private void attachSonority(Phone p) {
            int value = 0;

            for (SonorityClass sc : sonorityClasses) {
                final PhonexPattern pattern = sc.pattern();
                final var m = pattern.matcher(List.of(p));
                if (m.matches()) {
                    value = sc.sonorityValue();
                    break;
                }
            }

            final int distance = value - lastSonority;
            lastSonority = value;


            final SyllableInfo syllInfo = new SyllableInfo(
                SyllableConstituentType.UNKNOWN,
                false,
                SyllableStress.NoStress,
                0,
                false,
                value,
                distance,
                    null
            );
            builder.append((new IPAElementFactory()).clonePhoneWithSyllableInfo(p, syllInfo));
        }

    }
}
