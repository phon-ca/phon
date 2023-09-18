package ca.phon.session;

import ca.phon.orthography.*;
import ca.phon.orthography.Error;
import ca.phon.session.Participant;
import ca.phon.session.ParticipantRole;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.io.SessionOutputFactory;
import ca.phon.session.io.SessionWriter;
import ca.phon.visitor.annotation.Visits;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/* main */
public class CreateOrthographyExampleSession {

    public static class KeywordVisitor extends AbstractOrthographyVisitor {

        final Set<String> keywords = new LinkedHashSet<>();

        @Visits
        @Override
        public void visitLinker(Linker linker) {
            keywords.add("linker");
        }

        @Visits
        @Override
        public void visitUtteranceLanguage(UtteranceLanguage utteranceLanguage) {
            keywords.add("utterance-language");
        }

        @Visits
        @Override
        public void visitCompoundWord(CompoundWord compoundWord) {
            keywords.add("compound-word");
        }

        @Visits
        @Override
        public void visitWord(Word word) {
            keywords.add("word");
            WordElementVisitor visitor = new WordElementVisitor();
            word.getWordElements().forEach(visitor::visit);
            keywords.addAll(visitor.keywords);
            if(word.getPrefix() != null && word.getPrefix().toString().length() > 0) {
                keywords.add("word-prefix");
            } else if(word.getSuffix() != null && word.getSuffix().toString().length() > 0) {
                keywords.add("word-suffix");
            } else if(word.getReplacements().size() > 0) {
                keywords.add("replacement");
            }
        }

        @Visits
        @Override
        public void visitOrthoGroup(OrthoGroup group) {
            keywords.add("group");
            group.getElements().forEach(this::visit);
            visitAnnotations(group);
        }

        @Visits
        @Override
        public void visitPhoneticGroup(PhoneticGroup phoneticGroup) {
            keywords.add("phonetic-group");
            phoneticGroup.getElements().forEach(this::visit);
        }

        @Visits
        @Override
        public void visitQuotation(Quotation quotation) {
            keywords.add("quotation");
        }

        @Visits
        @Override
        public void visitPause(Pause pause) {
            keywords.add("pause");
        }

        @Visits
        @Override
        public void visitInternalMedia(InternalMedia internalMedia) {
            keywords.add("internal-media");
        }

        @Visits
        @Override
        public void visitFreecode(Freecode freecode) {
            keywords.add("freecode");
        }

        private void visitAnnotations(AnnotatedOrthographyElement annotatedOrthographyElement) {
            if(annotatedOrthographyElement.getAnnotations().size() == 0) return;
            AnnotationVisitor visitor = new AnnotationVisitor();
            annotatedOrthographyElement.getAnnotations().forEach(visitor::visit);
            keywords.addAll(visitor.keywords);
        }

        @Visits
        @Override
        public void visitAction(Action action) {
            keywords.add("action");
            visitAnnotations(action);
        }

        @Visits
        @Override
        public void visitHappening(Happening happening) {
            keywords.add("happening");
            visitAnnotations(happening);
        }

        @Visits
        @Override
        public void visitOtherSpokenEvent(OtherSpokenEvent otherSpokenEvent) {
            keywords.add("other-spoken-event");
            visitAnnotations(otherSpokenEvent);
        }

        @Visits
        @Override
        public void visitSeparator(Separator separator) {
            keywords.add("separator");
        }

        @Visits
        @Override
        public void visitToneMarker(ToneMarker toneMarker) {
            keywords.add("tone-marker");
        }

        @Visits
        @Override
        public void visitTagMarker(TagMarker tagMarker) {
            keywords.add("tag-marker");
        }

        @Visits
        @Override
        public void visitOverlapPoint(OverlapPoint overlapPoint) {
            keywords.add("overlap-point");
        }

        @Visits
        @Override
        public void visitUnderline(Underline underline) {
            keywords.add("underline");
        }

        @Visits
        @Override
        public void visitItalic(Italic italic) {
            keywords.add("italic");
        }

        @Visits
        @Override
        public void visitLongFeature(LongFeature longFeature) {
            keywords.add("long-feature");
        }

        @Visits
        @Override
        public void visitNonvocal(Nonvocal nonvocal) {
            keywords.add("nonvocal");
        }

        @Visits
        @Override
        public void visitTerminator(Terminator terminator) {
            keywords.add("terminator");
        }

        @Visits
        @Override
        public void visitPostcode(Postcode postcode) {
            keywords.add("postcode");
        }

        @Override
        public void fallbackVisit(OrthographyElement obj) {

        }
    }

    public static class WordElementVisitor extends AbstractWordElementVisitor {

        Set<String> keywords = new LinkedHashSet<>();

        @Visits
        @Override
        public void visitText(WordText text) {
        }

        @Visits
        @Override
        public void visitCaDelimiter(CaDelimiter caDelimiter) {
            keywords.add("ca-delimiter");
        }

        @Visits
        @Override
        public void visitCaElement(CaElement caElement) {
            keywords.add("ca-element");
        }

        @Visits
        @Override
        public void visitLongFeature(LongFeature longFeature) {
            keywords.add("word-long-feature");
        }

        @Visits
        @Override
        public void visitOverlapPoint(OverlapPoint overlapPoint) {
            keywords.add("word-overlap-point");
        }

        @Visits
        @Override
        public void visitProsody(Prosody prosody) {
            keywords.add("prosody");
        }

        @Visits
        @Override
        public void vistShortening(Shortening shortening) {
            keywords.add("shortening");
        }

        @Visits
        @Override
        public void visitCompoundWordMarker(CompoundWordMarker compoundWordMarker) {
            keywords.add("compound-word-marker");
        }

        @Visits
        @Override
        public void visitUnderline(Underline underline) {
            keywords.add("word-underline");
        }

        @Visits
        @Override
        public void visitItalic(Italic italic) {
            keywords.add("word-italic");
        }

        @Override
        public void fallbackVisit(WordElement obj) {

        }
    }

    public static class AnnotationVisitor extends AbstractOrthographyAnnotationVisitor {

        Set<String> keywords = new LinkedHashSet<>();

        @Visits
        @Override
        public void visitDuration(Duration duration) {
            keywords.add("duration");
        }

        @Visits
        @Override
        public void visitError(Error error) {
            keywords.add("error");
        }

        @Visits
        @Override
        public void visitMarker(Marker marker) {
            keywords.add("marker");
        }

        @Visits
        @Override
        public void visitGroupAnnotation(GroupAnnotation groupAnnotation) {
            keywords.add("group-annotation");
        }

        @Visits
        @Override
        public void visitOverlap(Overlap overlap) {
            keywords.add("overlap");
        }

        @Override
        public void fallbackVisit(OrthographyAnnotation obj) {

        }
    }

    public static void main(String[] args) {
        final String filename = "session/src/test/resources/ca/phon/session/test_main_line.txt";
        final String outputFile = "OrthographyTest.xml";

        final SessionFactory factory = SessionFactory.newFactory();
        final Session session = factory.createSession();
        session.setCorpus("");
        session.setName("OrthographyTest");

        final List<TierViewItem> tierView = new ArrayList<>();
        tierView.add(factory.createTierViewItem(SystemTierType.Orthography.getName(), true));
        tierView.add(factory.createTierViewItem(SystemTierType.IPATarget.getName(), false));
        tierView.add(factory.createTierViewItem(SystemTierType.IPAActual.getName(), false));
        tierView.add(factory.createTierViewItem(SystemTierType.Segment.getName(), false));
        tierView.add(factory.createTierViewItem(SystemTierType.Notes.getName(), true));
        session.setTierView(tierView);

        final Participant chi = factory.createParticipant();
        chi.setId("CHI");
        chi.setRole(ParticipantRole.TARGET_CHILD);
        session.addParticipant(chi);

        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                final Record r = factory.createRecord(chi);
                final Orthography ortho = Orthography.parseOrthography(line);
                r.setOrthography(ortho);

                KeywordVisitor visitor = new KeywordVisitor();
                ortho.accept(visitor);
                String notes = visitor.keywords.stream().collect(Collectors.joining(" "));
                r.getNotesTier().setText(notes);

                session.addRecord(r);
            }
            final SessionWriter writer = (new SessionOutputFactory()).createWriter();
            writer.writeSession(session, new FileOutputStream(outputFile));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}