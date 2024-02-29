package ca.phon.autotranscribe;

import ca.phon.alignedTypesDatabase.CartesianProduct;
import ca.phon.ipa.IPAElementFactory;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.ipa.IntraWordPause;
import ca.phon.orthography.*;
import ca.phon.session.SessionFactory;
import ca.phon.session.Tier;
import ca.phon.session.alignment.TierElementFilter;
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

    public AutomaticTranscription transcribe(String text, int fromWord) throws ParseException {
        final Orthography orthography = Orthography.parseOrthography(text);
        return transcribe(orthography, fromWord);
    }

    /**
     * Transcribe the given orthography
     *
     * @param orthography
     * @return automatic transcription
     */
    public AutomaticTranscription transcribe(Orthography orthography, int fromWord) {
        final AutoTranscriberVisitor visitor = new AutoTranscriberVisitor();
        TierElementFilter orthoFilter = TierElementFilter.orthographyFilterForIPAAlignment();

        // wrap orthography in a temp tier
        final Tier<Orthography> orthoTier = SessionFactory.newFactory().createTier("orthotemp", Orthography.class, new HashMap<>(), false, false);
        orthoTier.setValue(orthography);

        List<OrthographyElement> orthographyElements = (List<OrthographyElement>) orthoFilter.filterTier(orthoTier);
        for(int i = fromWord; i < orthographyElements.size(); i++) {
            final OrthographyElement element = orthographyElements.get(i);
            visitor.visit(element);
        }

        final Map<OrthographyElement, IPATranscript[]> transcriptionOptions = visitor.transcriptionOptions;
        return new AutomaticTranscription(orthography, transcriptionOptions);
    }

    /**
     * Lookup a single word
     *
     * @param word
     * @return list of transcriptions Word word) {
     *         final List<IPATranscript> retVfrom all sources ordered by the IPA comparator.  If no transcriptions are available,
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
            final Set<String> transcriptions = new LinkedHashSet<>();
            for (AutoTranscribeSource source : sources) {
                final String[] options = source.lookup(text);
                for (String opt : options) {
                    transcriptions.add(opt);
                }
            }
            for (String transcription : transcriptions) {
                try {
                    final IPATranscript ipaTranscript = IPATranscript.parseIPATranscript(transcription);
                    retVal.add(ipaTranscript);
                } catch (ParseException e) {
                    // ignore
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

        private final Map<OrthographyElement, IPATranscript[]> transcriptionOptions = new LinkedHashMap<>();

        @Visits
        @Override
        public void visitPause(Pause pause) {
            final ca.phon.ipa.PauseLength pauseType = switch(pause.getType()) {
                case SIMPLE -> ca.phon.ipa.PauseLength.SIMPLE;
                case LONG -> ca.phon.ipa.PauseLength.LONG;
                case VERY_LONG -> ca.phon.ipa.PauseLength.VERY_LONG;
                case NUMERIC -> ca.phon.ipa.PauseLength.NUMERIC;
            };
            final float length = pause.getLength();
            final IPAElementFactory factory = new IPAElementFactory();

            final ca.phon.ipa.Pause ipaPause =
                    pauseType == ca.phon.ipa.PauseLength.NUMERIC ? factory.createPause(length) : factory.createPause(pauseType);
            final IPATranscript[] ipaTranscripts = new IPATranscript[]{new IPATranscript(new ca.phon.ipa.IPAElement[]{ipaPause})};
            transcriptionOptions.put(pause, ipaTranscripts);
        }

        @Visits
        @Override
        public void visitCompoundWord(CompoundWord compoundWord) {
            visit(compoundWord.getWord1());
            IPATranscript[] word1Opts = transcriptionOptions.remove(compoundWord.getWord1());
            visit(compoundWord.getWord2());
            IPATranscript[] word2Opts = transcriptionOptions.remove(compoundWord.getWord2());

            final String[][] product = CartesianProduct.stringArrayProduct(
                    new String[][]{Arrays.stream(word1Opts).map(IPATranscript::toString).toArray(String[]::new),
                            Arrays.stream(word2Opts).map(IPATranscript::toString).toArray(String[]::new)});
            final List<IPATranscript> transcripts = new ArrayList<>();
            for(String[] p:product) {
                final IPATranscriptBuilder builder = new IPATranscriptBuilder();
                for(String part:p) {
                    if(builder.size() > 0)
                        builder.appendCompoundWordMarker();
                    builder.append(part);
                }
                final IPATranscript ipaTranscript = builder.toIPATranscript();
                transcripts.add(ipaTranscript);
            }
            transcriptionOptions.put(compoundWord, transcripts.toArray(new IPATranscript[0]));
        }

        @Visits
        @Override
        public void visitWord(Word word) {
            if(word.getPrefix() != null && word.getPrefix().getType() == WordType.OMISSION) {
                return;
            }

            final String[] wordParts = word.getWord().split("_");
            if(wordParts.length > 1) {
                List<IPATranscript[]> partTranscriptions = new ArrayList<>();
                for(String part:wordParts) {
                    final Word w = new Word(part);

                    final List<IPATranscript> transcriptions = lookup(w);
                    partTranscriptions.add(transcriptions.toArray(new IPATranscript[0]));
                }
                final String[][] partTranscriptionArray = new String[partTranscriptions.size()][];
                for(int i = 0; i < partTranscriptions.size(); i++) {
                    partTranscriptionArray[i] = new String[partTranscriptions.get(i).length];
                    for (int j = 0; j < partTranscriptions.get(i).length; j++) {
                        partTranscriptionArray[i][j] = partTranscriptions.get(i)[j].toString();
                    }
                }
                final String[][] product = CartesianProduct.stringArrayProduct(partTranscriptionArray);
                final List<IPATranscript> transcripts = new ArrayList<>();
                for(String[] p:product) {
                    final IPATranscriptBuilder builder = new IPATranscriptBuilder();
                    for(String part:p) {
                        if(builder.size() > 0)
                            builder.appendLinker();
                        builder.append(part);
                    }
                    final IPATranscript ipaTranscript = builder.toIPATranscript();
                    transcripts.add(ipaTranscript);
                }
                transcriptionOptions.put(word, transcripts.toArray(new IPATranscript[0]));

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
