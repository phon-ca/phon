package ca.phon.app.session.editor.view.transcript;

import ca.phon.orthography.*;
import ca.phon.orthography.Action;
import ca.phon.orthography.Error;
import ca.phon.visitor.annotation.Visits;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.util.List;

public class TranscriptOrthographyVisitors {
    /**
     * Creates a list of keywords, one keyword for every element type in CHAT.  This class is an example of how to
     * iterate through data in tiers with the Orthography type.  Some types have inner-elements: OrthoGroup and PhoneticGrouip,
     * and some have annotations: groups and events (action, happening, other spoken events.)
     */
    public static class KeywordVisitor extends AbstractOrthographyVisitor {

//        private final TranscriptDocument doc;
        private final SimpleAttributeSet attrs;
        private boolean firstVisit = true;

        private final TranscriptBatchBuilder batchBuilder;

        public KeywordVisitor(SimpleAttributeSet attrs, TranscriptBatchBuilder batchBuilder) {
//            this.doc = doc;
            this.attrs = new SimpleAttributeSet(attrs);
            this.batchBuilder = batchBuilder;
        }

        @Override
        public void visit(OrthographyElement obj) {
            if (firstVisit) {
                firstVisit = false;
            }
            else {
                batchBuilder.appendBatchString(" ", attrs);
            }
            super.visit(obj);
        }

        @Visits
        @Override
        public void visitLinker(Linker linker) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_LINKER));
            batchBuilder.appendBatchString(linker.text(), attrs);
        }

        @Visits
        @Override
        public void visitUtteranceLanguage(UtteranceLanguage utteranceLanguage) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_UTTERANCE_LANGUAGE));
            batchBuilder.appendBatchString(utteranceLanguage.text(), attrs);
        }

        @Visits
        @Override
        public void visitCompoundWord(CompoundWord compoundWord) {
            if(compoundWord.getPrefix() != null && compoundWord.getPrefix().toString().length() > 0) {
                StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_WORD_PREFIX));
                batchBuilder.appendBatchString(compoundWord.getPrefix().toString(), attrs);
            }

            WordElementVisitor visitor = new WordElementVisitor(attrs, batchBuilder);
            compoundWord.getWordElements().forEach(visitor::visit);

            if(compoundWord.getSuffix() != null && compoundWord.getSuffix().toString().length() > 0) {
                StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_WORD_SUFFIX));
                batchBuilder.appendBatchString(compoundWord.getSuffix().toString(), attrs);
            }

            if(compoundWord.getReplacements().size() > 0) {
                batchBuilder.appendBatchString(" ", attrs);
                StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_WORD_REPLACEMENT));
                batchBuilder.appendBatchString(compoundWord.getReplacementsText(), attrs);
            }
        }

        /**
         * Visit word, words are composed of elements which use another listener defined below
         * @param word
         */
        @Visits
        @Override
        public void visitWord(Word word) {
            if(word.getPrefix() != null && word.getPrefix().toString().length() > 0) {
                StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_WORD_PREFIX));
                batchBuilder.appendBatchString(word.getPrefix().toString(), attrs);
            }

            WordElementVisitor visitor = new WordElementVisitor(attrs, batchBuilder);
            word.getWordElements().forEach(visitor::visit);

            if(word.getSuffix() != null && word.getSuffix().toString().length() > 0) {
                StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_WORD_SUFFIX));
                batchBuilder.appendBatchString(word.getSuffix().toString(), attrs);
            }

            if(word.getReplacements().size() > 0) {
                batchBuilder.appendBatchString(" ", attrs);
                StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_WORD_REPLACEMENT));
                batchBuilder.appendBatchString(word.getReplacementsText(), attrs);
            }
        }

        /**
         * Groups have inner-elements and annotations
         * @param group
         */
        @Visits
        @Override
        public void visitOrthoGroup(OrthoGroup group) {
            List<OrthographyElement> elements = group.getElements();

            if (elements.size() > 1) {
                StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_GROUP_START));
                batchBuilder.appendBatchString("<", attrs);
            }

            firstVisit = true;
            group.getElements().forEach(this::visit);

            if (elements.size() > 1) {
                StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_GROUP_END));
                batchBuilder.appendBatchString(">", attrs);
            }

            visitAnnotations(group);
        }

        /**
         * Phonetic groups only have inner-elements
         * @param phoneticGroup
         */
        @Visits
        @Override
        public void visitPhoneticGroup(PhoneticGroup phoneticGroup) {
            List<OrthographyElement> elements = phoneticGroup.getElements();

            if (elements.size() > 1) {
                StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_PHONETIC_GROUP_START));
                batchBuilder.appendBatchString(PhoneticGroup.PHONETIC_GROUP_START, attrs);
            }

            firstVisit = true;
            phoneticGroup.getElements().forEach(this::visit);

            if (elements.size() > 1) {
                StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_PHONETIC_GROUP_END));
                batchBuilder.appendBatchString(PhoneticGroup.PHONETIC_GROUP_END, attrs);
            }
        }

        @Visits
        @Override
        public void visitQuotation(Quotation quotation) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_QUOTATION));
            batchBuilder.appendBatchString(quotation.text(), attrs);
        }

        @Visits
        @Override
        public void visitPause(Pause pause) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_PAUSE));
            batchBuilder.appendBatchString(pause.text(), attrs);
        }

        @Visits
        @Override
        public void visitInternalMedia(InternalMedia internalMedia) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_INTERNAL_MEDIA));
            batchBuilder.appendFormattedInternalMedia(internalMedia, attrs);
        }

        @Visits
        @Override
        public void visitFreecode(Freecode freecode) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_FREECODE));
            batchBuilder.appendBatchString(freecode.text(), attrs);
        }

        private void visitAnnotations(AnnotatedOrthographyElement annotatedOrthographyElement) {
            if(annotatedOrthographyElement.getAnnotations().size() == 0) return;
            AnnotationVisitor visitor = new AnnotationVisitor(attrs, batchBuilder);
            for (OrthographyAnnotation annotation : annotatedOrthographyElement.getAnnotations()) {
                batchBuilder.appendBatchString(" ", attrs);
                visitor.visit(annotation);
            }
        }

        /**
         * Event - events have possible annotations
         */
        @Visits
        @Override
        public void visitAction(Action action) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_ACTION));
            batchBuilder.appendBatchString(action.elementText(), attrs);
            visitAnnotations(action);
        }

        /**
         * Event - events have possible annotations
         */
        @Visits
        @Override
        public void visitHappening(Happening happening) {
            String elementText = happening.elementText();

            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_HAPPENING_PREFIX));
            batchBuilder.appendBatchString(elementText.substring(0,2), attrs);

            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_HAPPENING));
            batchBuilder.appendBatchString(elementText.substring(2, elementText.length()), attrs);

            visitAnnotations(happening);
        }

        /**
         * Event - events have possible annotations
         */
        @Visits
        @Override
        public void visitOtherSpokenEvent(OtherSpokenEvent otherSpokenEvent) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_OTHER_SPOKEN_EVENT));
            batchBuilder.appendBatchString(otherSpokenEvent.text(), attrs);
            visitAnnotations(otherSpokenEvent);
        }

        @Visits
        @Override
        public void visitSeparator(Separator separator) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_SEPARATOR));
            batchBuilder.appendBatchString(separator.text(), attrs);
        }

        @Visits
        @Override
        public void visitToneMarker(ToneMarker toneMarker) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_TONE_MARKER));
            batchBuilder.appendBatchString(toneMarker.text(), attrs);
        }

        @Visits
        @Override
        public void visitTagMarker(TagMarker tagMarker) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_TAG_MARKER));
            batchBuilder.appendBatchString(tagMarker.text(), attrs);
        }

        @Visits
        @Override
        public void visitOverlapPoint(OverlapPoint overlapPoint) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_OVERLAP_POINT));
            batchBuilder.appendBatchString(overlapPoint.text(), attrs);
        }

        /**
         * Italic and underline are stored as objects in the Orthography data structure with zero text and a begin/end
         * attribute.  They may also appear as word elements.
         * @param underline
         */
        @Visits
        @Override
        public void visitUnderline(Underline underline) {
            // TODO: Implement underline
        }

        /**
         * Italic and underline are stored as objects in the Orthography data structure with zero text and a begin/end
         * attribute.  They may also appear as word elements.
         * @param italic
         */
        @Visits
        @Override
        public void visitItalic(Italic italic) {
            // TODO: Implement italics
        }

        @Visits
        @Override
        public void visitLongFeature(LongFeature longFeature) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_LONG_FEATURE));
            batchBuilder.appendBatchString(longFeature.text(), attrs);
        }

        @Visits
        @Override
        public void visitNonvocal(Nonvocal nonvocal) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_NONVOCAL));
            batchBuilder.appendBatchString(nonvocal.text(), attrs);
        }

        @Visits
        @Override
        public void visitTerminator(Terminator terminator) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_TERMINATOR));
            batchBuilder.appendBatchString(terminator.text(), attrs);
        }

        @Visits
        @Override
        public void visitPostcode(Postcode postcode) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_POSTCODE));
            batchBuilder.appendBatchString(postcode.text(), attrs);
        }

        @Override
        public void fallbackVisit(OrthographyElement obj) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_FALLBACK));
            batchBuilder.appendBatchString(obj.text(), attrs);
        }
    }

    /**
     * Visit possible word elements
     */
    public static class WordElementVisitor extends AbstractWordElementVisitor {
        private final SimpleAttributeSet attrs;
        private final TranscriptBatchBuilder batchBuilder;

        public WordElementVisitor(SimpleAttributeSet attrs, TranscriptBatchBuilder batchBuilder) {
            this.attrs = attrs;
            this.batchBuilder = batchBuilder;
        }

        @Visits
        @Override
        public void visitText(WordText text) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_WORD_TEXT));
            batchBuilder.appendBatchString(text.text(), attrs);
        }

        @Visits
        @Override
        public void visitCaDelimiter(CaDelimiter caDelimiter) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_CA_DELIMITER));
            batchBuilder.appendBatchString(caDelimiter.text(), attrs);
        }

        @Visits
        @Override
        public void visitCaElement(CaElement caElement) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_CA_ELEMENT));
            batchBuilder.appendBatchString(caElement.text(), attrs);
        }

        @Visits
        @Override
        public void visitLongFeature(LongFeature longFeature) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_LONG_FEATURE));
            batchBuilder.appendBatchString(longFeature.text(), attrs);
        }

        @Visits
        @Override
        public void visitOverlapPoint(OverlapPoint overlapPoint) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_OVERLAP_POINT));
            batchBuilder.appendBatchString(overlapPoint.text(), attrs);
        }

        @Visits
        @Override
        public void visitProsody(Prosody prosody) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_PROSODY));
            batchBuilder.appendBatchString(prosody.text(), attrs);
        }

        @Visits
        @Override
        public void vistShortening(Shortening shortening) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_SHORTENING));
            batchBuilder.appendBatchString(shortening.text(), attrs);
        }

        @Visits
        @Override
        public void visitCompoundWordMarker(CompoundWordMarker compoundWordMarker) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_COMPOUND_WORD_MARKER));
            batchBuilder.appendBatchString(compoundWordMarker.text(), attrs);
        }

        @Visits
        @Override
        public void visitUnderline(Underline underline) {

        }

        @Visits
        @Override
        public void visitItalic(Italic italic) {

        }

        @Override
        public void fallbackVisit(WordElement obj) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_FALLBACK));
            batchBuilder.appendBatchString(obj.text(), attrs);
        }
    }

    /**
     * Visit possible annotations
     */
    public static class AnnotationVisitor extends AbstractOrthographyAnnotationVisitor {

        private final SimpleAttributeSet attrs;
        private final TranscriptBatchBuilder batchBuilder;

        public AnnotationVisitor(SimpleAttributeSet attrs, TranscriptBatchBuilder batchBuilder) {
            this.attrs = attrs;
            this.batchBuilder = batchBuilder;
        }

        @Visits
        @Override
        public void visitDuration(Duration duration) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_DURATION));
            batchBuilder.appendBatchString(duration.text(), attrs);
        }

        @Visits
        @Override
        public void visitError(Error error) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_ERROR));
            batchBuilder.appendBatchString(error.text(), attrs);
        }

        @Visits
        @Override
        public void visitMarker(Marker marker) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_MARKER));
            batchBuilder.appendBatchString(marker.text(), attrs);
        }

        @Visits
        @Override
        public void visitGroupAnnotation(GroupAnnotation groupAnnotation) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_GROUP_ANNOTATION));
            batchBuilder.appendBatchString(groupAnnotation.text(), attrs);
        }

        @Visits
        @Override
        public void visitOverlap(Overlap overlap) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_OVERLAP));
            batchBuilder.appendBatchString(overlap.text(), attrs);
        }

        @Override
        public void fallbackVisit(OrthographyAnnotation obj) {
            StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.ORTHOGRAPHY_FALLBACK));
            batchBuilder.appendBatchString(obj.text(), attrs);
        }
    }
}
