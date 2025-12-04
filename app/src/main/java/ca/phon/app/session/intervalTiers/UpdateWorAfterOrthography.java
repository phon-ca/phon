package ca.phon.app.session.intervalTiers;

import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.extensions.Extension;
import ca.phon.extensions.ExtensionProvider;
import ca.phon.extensions.IExtendable;
import ca.phon.orthography.InternalMedia;
import ca.phon.orthography.OrthoWordExtractor;
import ca.phon.orthography.Orthography;
import ca.phon.orthography.Word;
import ca.phon.session.*;
import ca.phon.session.Record;

import java.util.ArrayList;
import java.util.List;


/**
 * Update text and intervals of the "Word Intervals" (%wor) tier when text of Orthography
 * changes.  If the number of words remains the same, the text will be updated and the
 * intervals will remain the same.  If the number of words changes, the intervals
 * will be updated to match the new text.
 */
@Extension(Tier.class)
public class UpdateWorAfterOrthography implements TierEdit.DependentTierChanges<Orthography>, ExtensionProvider {

    /**
     * Generate a new word intervals tier from an orthography tier using the provided
     * segment.
     *
     * @param orthography the orthography tier
     * @param segment the media segment for the record
     * @return the new word intervals tier
     */
    public static Orthography worFromOrthography(Orthography orthography, MediaSegment segment) {
        // extract words from wordIntervalsTier
        final OrthoWordExtractor orthoWordExtractor = new OrthoWordExtractor();
        orthography.accept(orthoWordExtractor);
        final List<Word> orthoWords = orthoWordExtractor.getWordList();

        final List<InternalMedia> internalMediaList = new ArrayList<>();
        if(segment.isPoint()) {
            for(int i = 0; i < orthoWords.size(); i++) {
                final InternalMedia internalMedia = new InternalMedia(segment.getStartTime(), segment.getEndTime());
                internalMediaList.add(internalMedia);
            }
        } else {
            final float segmentDuration = segment.getEndTime() - segment.getStartTime();
            final float wordDuration = segmentDuration / (float)orthoWords.size();
            float lastEndTime = segment.getStartTime();
            for(int i = 0; i < orthoWords.size(); i++) {
                float startTime = lastEndTime;
                float endTime = Math.min(segment.getEndTime(), startTime + wordDuration);

                // convert to 3 decimal places
                startTime = Math.round(startTime * 1000f) / 1000f;
                endTime = Math.round(endTime * 1000f) / 1000f;
                lastEndTime = endTime;

                final InternalMedia internalMedia = new InternalMedia(startTime, endTime);
                internalMediaList.add(internalMedia);
            }
        }
        final WorTierUpdateVisitor updateVisitor = new WorTierUpdateVisitor(internalMediaList);
        orthography.accept(updateVisitor);
        final Orthography wordIntervals = updateVisitor.getOrthography();
        return wordIntervals;
    }

    @Override
    public void performDependentTierChanges(TierEdit<Orthography> tierEdit) {
        // only update word intervals if the text is being committed
        if(tierEdit.isValueAdjusting()) return;
        final Session session = tierEdit.getSession();
        // check to ensure the word intervals tier is in the session
        final TierDescription worTierDesc = session.getUserTiers()
                        .stream()
                        .filter(td -> UserTierType.Wor.getPhonTierName().equals(td.getName()))
                        .findAny().orElse(null);
        if(worTierDesc == null) return;
        final Record record = tierEdit.getRecord();

        final Tier<Orthography> orthoTier = tierEdit.getTier();
        final Tier<Orthography> wordIntervalsTier = record.getTier(worTierDesc.getName(), Orthography.class);
        final Orthography oldWordIntervals = wordIntervalsTier.getValue();

        // extract words from wordIntervalsTier
        final OrthoWordExtractor orthoWordExtractor = new OrthoWordExtractor();
        orthoTier.getValue().accept(orthoWordExtractor);
        final List<Word> orthoWords = orthoWordExtractor.getWordList();

        final OrthoWordExtractor worWordExtractor = new OrthoWordExtractor();
        final List<Word> worWords = new ArrayList<>();
        if(wordIntervalsTier.hasValue()) {
            wordIntervalsTier.getValue().accept(worWordExtractor);
            worWords.addAll(worWordExtractor.getWordList());
        }

        final List<InternalMedia> internalMediaList = new ArrayList<>();
        if(orthoWords.size() == worWords.size()) {
            // keep segments the same
            final OrthoIntervalVisitor visitor = new OrthoIntervalVisitor();
            wordIntervalsTier.getValue().accept(visitor);
            internalMediaList.addAll(visitor.getInternalMediaList());
        } else {
            // generate a new list of internal media segments
            final MediaSegment segment = record.getMediaSegment();
            if(segment.isPoint()) {
                for(int i = 0; i < orthoWords.size(); i++) {
                    final InternalMedia internalMedia = new InternalMedia(segment.getStartTime(), segment.getEndTime());
                    internalMediaList.add(internalMedia);
                }
            } else {
                final float segmentDuration = segment.getEndTime() - segment.getStartTime();
                final float wordDuration = segmentDuration / (float)orthoWords.size();
                float lastEndTime = segment.getStartTime();
                for(int i = 0; i < orthoWords.size(); i++) {
                    float startTime = lastEndTime;
                    float endTime = Math.min(segment.getEndTime(), startTime + wordDuration);
                    lastEndTime = endTime;

                    final InternalMedia internalMedia = new InternalMedia(startTime, endTime);
                    internalMediaList.add(internalMedia);
                }
            }
        }
        final WorTierUpdateVisitor updateVisitor = new WorTierUpdateVisitor(internalMediaList);
        orthoTier.getValue().accept(updateVisitor);
        final Orthography newWordIntervals = updateVisitor.getOrthography();
        if(newWordIntervals == null) return;
        wordIntervalsTier.setValue(newWordIntervals);
        tierEdit.putAdditionalTierChange(wordIntervalsTier.getName(), oldWordIntervals, newWordIntervals);
        tierEdit.fireTierChange(wordIntervalsTier, oldWordIntervals, newWordIntervals);
    }

    @Override
    public void installExtension(IExtendable obj) {
        if(obj instanceof Tier<?> tier) {
            if (SystemTierType.Orthography.getName().equals(tier.getName()) && tier.getDeclaredType() == Orthography.class) {
                final UpdateWorAfterOrthography extension = new UpdateWorAfterOrthography();
                final TierEdit.DependentTierChanges existingExtension =  obj.getExtension(TierEdit.DependentTierChanges.class);
                if(existingExtension == null) {
                    obj.putExtension(TierEdit.DependentTierChanges.class, extension);
                } else {
                    obj.putExtension(TierEdit.DependentTierChanges.class, new TierEdit.DependentTierChangeChain(existingExtension, extension));
                }
            }
        }
    }

}
