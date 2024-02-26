package ca.phon.autotranscribe;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.orthography.*;
import ca.phon.visitor.annotation.Visits;

import java.text.ParseException;
import java.util.*;

/**
 * Create automatic IPA transcriptions for a given orthography
 */
public class AutoTranscriber {

    private final List<AutoTranscribeSource> sources;

    /**
     * Comparator for IPA transcription results
     *
     * If not set, transcriptions are ordered by the order they are returned from the sources
     */
    private Comparator<IPATranscript> ipaComparator = null;

    public AutoTranscriber() {
        this(new ArrayList<>());
    }

    /**
     * Create a new auto transcriber for the given orthography and sources
     * @param sources
     */
    public AutoTranscriber(List<AutoTranscribeSource> sources) {
        this.sources = sources;
    }

    public List<AutoTranscribeSource> getSources() {
        return Collections.unmodifiableList(this.sources);
    }

    public void setIpaComparator(Comparator<IPATranscript> comparator) {
        this.ipaComparator = comparator;
    }

    public Comparator<IPATranscript> getIpaComparator() {
        return this.ipaComparator;
    }

    /**
     * Add a new source for transcriptions
     *
     * @param source
     */
    public void addSource(AutoTranscribeSource source) {
        this.sources.add(source);
    }

    /**
     * Remove a source for transcriptions
     *
     * @param source
     */
    public void removeSource(AutoTranscribeSource source) {
        this.sources.remove(source);
    }

    public void clearSources() {
    	this.sources.clear();
    }

    public void setSources(List<AutoTranscribeSource> sources) {
    	this.sources.clear();
    	this.sources.addAll(sources);
    }

    /**
     * Transcribe the given orthography
     *
     * @param text
     * @return automatic transcription
     * @throws ParseException
     */
    public AutomaticTranscription transcribe(String text) throws ParseException {
        final Orthography orthography = Orthography.parseOrthography(text);
        return transcribe(orthography);
    }


    /**
     * Transcribe the given orthography
     *
     * @param orthography
     * @return automatic transcription
     */
    public AutomaticTranscription transcribe(Orthography orthography) {
        return transcribe(orthography, 0);
    }

    /**
     * Transcribe the given orthography
     *
     * @param orthography
     * @return automatic transcription
     */
    public AutomaticTranscription transcribe(Orthography orthography, int fromWord) {
        final AutoTranscriberVisitor visitor = new AutoTranscriberVisitor(fromWord);
        orthography.accept(visitor);

        final Map<Word, IPATranscript[]> transcriptionOptions = visitor.transcriptionOptions;
        return new AutomaticTranscription(orthography, transcriptionOptions);
    }

    /**
     * Lookup a single word
     *
     * @param word
     * @return list of transcriptions from all sources ordered by the IPA comparator.  If no transcriptions are available,
     * a list with a single '*' is returned.
     */
    public List<IPATranscript> lookup(Word word) {
        final List<IPATranscript> retVal = new ArrayList<>();
        final String text = word.getWord();

        if(word.getSuffix() != null && word.getSuffix().getType() == WordFormType.UNIBET) {
            try {
                final IPATranscript ipaTranscript = IPATranscript.parseIPATranscript(text);
                retVal.add(ipaTranscript);
            } catch (ParseException e) {
                retVal.add((new IPATranscriptBuilder()).append("*").toIPATranscript());
            }
        } else {
            for (AutoTranscribeSource source : sources) {
                final String[] transcriptions = source.lookup(text);
                for (String transcription : transcriptions) {
                    try {
                        final IPATranscript ipaTranscript = IPATranscript.parseIPATranscript(transcription);
                        retVal.add(ipaTranscript);
                    } catch (ParseException e) {
                        // ignore
                    }
                }
            }

            if (retVal.isEmpty()) {
                retVal.add((new IPATranscriptBuilder()).append("*").toIPATranscript());
            }
            if (ipaComparator != null)
                retVal.sort(ipaComparator);
        }
        return retVal;
    }

    public class AutoTranscriberVisitor extends AbstractOrthographyVisitor {

        private final int fromWord;

        private int wordCount = 0;

        private final Map<Word, IPATranscript[]> transcriptionOptions = new LinkedHashMap<>();

        public AutoTranscriberVisitor() {
            this(0);
        }

        public AutoTranscriberVisitor(int fromWord) {
            this.fromWord = fromWord;
        }

        @Visits
        @Override
        public void visitCompoundWord(CompoundWord compoundWord) {
            visitWord(compoundWord.getWord1());
            wordCount--;
            visitWord(compoundWord.getWord2());
        }

        @Visits
        @Override
        public void visitWord(Word word) {
            if(word.getPrefix() != null && word.getPrefix().getType() == WordType.OMISSION) {
                return;
            }

            if(wordCount++ < fromWord) {
                wordCount++;
                return;
            }
            final List<IPATranscript> transcriptions = lookup(word);
            transcriptionOptions.put(word, transcriptions.toArray(new IPATranscript[0]));
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
            for(OrthographyElement element:phoneticGroup.getElements()) {
                visit(element);
            }
        }

        @Override
        public void fallbackVisit(OrthographyElement obj) {

        }
    }

}
