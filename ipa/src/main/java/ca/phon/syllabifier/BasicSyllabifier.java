package ca.phon.syllabifier;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.Phone;
import ca.phon.phonex.PhonexPattern;
import ca.phon.syllabifier.phonex.SonorityInfo;
import ca.phon.syllable.SyllableConstituentType;
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
    public void syllabify(List<IPAElement> phones) {
        markSonority(phones);
        for (SyllabifierStep step : syllabifierSteps) {
            runStep(step, phones);
        }
    }

    private void markSonority(List<IPAElement> elements) {
        final SonorityVisitor visitor = new SonorityVisitor();
        for (IPAElement ele : elements) {
            ele.accept(visitor);
        }
    }

    private void runStep(SyllabifierStep step, List<IPAElement> elements) {
        final PhonexPattern pattern = step.pattern();
        final var m = pattern.matcher(elements);

        while(m.find()) {
            for(int i = 1; i <= pattern.numberOfGroups(); i++) {
                final String name = pattern.groupName(i);
                if(name != null) {
                    final SyllableConstituentType scType = SyllableConstituentType.fromString(name);
                    if(scType != null) {
                        final List<IPAElement> group = m.group(i);
                        for(IPAElement ele:group) {
                            if(ele instanceof Phone phone) {
                                phone.setScType(scType);
                                if("D".equalsIgnoreCase(name)) {
                                    phone.setDiphthongMember(true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public record SonorityClass(int sonorityValue, PhonexPattern pattern) {
    }

    public record SyllabifierStep(String name, PhonexPattern pattern) {
    }

    private class SonorityVisitor implements Visitor<IPAElement> {

        private int lastSonority = 0;

        @Override
        public void visit(IPAElement obj) {
            if (obj instanceof Phone phone) {
                attachSonority(phone);
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

            final var info = new SonorityInfo(value, distance);
            p.putExtension(SonorityInfo.class, info);
        }

    }
}
