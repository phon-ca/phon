package ca.phon.autotranscribe;

import ca.phon.ipa.CompoundWordMarker;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.orthography.*;
import ca.phon.visitor.annotation.Visits;

import javax.swing.plaf.ButtonUI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Container for automatic transcription results.
 *
 */
public class AutomaticTranscription {

    private final Orthography orthography;

    private final Map<Word, IPATranscript[]> transcriptionOptions;

    private final Map<Word, Integer> selectedTranscriptions;

    public AutomaticTranscription(Orthography orthography, Map<Word, IPATranscript[]> transcriptionOptions) {
        this.orthography = orthography;
        this.transcriptionOptions = transcriptionOptions;
        this.selectedTranscriptions = new HashMap<>();

        for(Word w:this.transcriptionOptions.keySet()) {
            this.selectedTranscriptions.put(w, 0);
        }
    }

    public Orthography getOrthography() {
        return this.orthography;
    }

    public Map<Word, IPATranscript[]> getTranscriptionOptions() {
        return this.transcriptionOptions;
    }

    public IPATranscript[] getTranscriptionOptions(Word word) {
        return this.transcriptionOptions.get(word);
    }

    /**
     * Return the current selected transcription for Orthography
     *
     * @return the selected transcription
     */
    public IPATranscript getTranscription() {
        final AutomaticTranscriptionVisitor visitor = new AutomaticTranscriptionVisitor();
        getOrthography().accept(visitor);
        return visitor.builder.toIPATranscript();
    }

    public IPATranscript getSelectedTranscription(Word word) {
        return this.transcriptionOptions.get(word)[this.selectedTranscriptions.get(word)];
    }

    public void setSelectedTranscription(Word word, int idx) {
        this.selectedTranscriptions.put(word, idx);
    }

    public List<Word> getWords() {
        final List<Word> retVal = new ArrayList<>(getTranscriptionOptions().keySet());
        return retVal;
    }

    public class AutomaticTranscriptionVisitor extends AbstractOrthographyVisitor {

        final IPATranscriptBuilder builder = new IPATranscriptBuilder();

        @Visits
        @Override
        public void visitCompoundWord(CompoundWord compoundWord) {
            visitWord(compoundWord);
        }

        @Visits
        @Override
        public void visitWord(Word word) {
            if(word.getPrefix() != null && word.getPrefix().getType() == WordType.OMISSION) {
                return;
            }

            if(!getTranscriptionOptions().containsKey(word)) {
                return;
            }

            if(builder.size() > 0 && !(builder.last() instanceof CompoundWordMarker))
                builder.appendWordBoundary();
            builder.append(getSelectedTranscription(word));
        }

        @Visits
        @Override
        public void visitOrthoGroup(OrthoGroup group) {
            for(OrthographyElement element:group.getElements()) {
                visit(element);
            }
        }

        @Visits
        @Override
        public void visitPhoneticGroup(PhoneticGroup phoneticGroup) {
            builder.appendPgStart();
            for(OrthographyElement element:phoneticGroup.getElements()) {
                visit(element);
            }
            builder.appendPgEnd();
        }

        @Override
        public void fallbackVisit(OrthographyElement obj) {

        }

    }

}
