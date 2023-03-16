package ca.phon.session;

import ca.phon.session.AlignedSegment;
import ca.phon.session.MediaSegment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupSegment extends AlignedSegment {

    private List<AlignedSegment> wordSegments;

    public GroupSegment(MediaSegment parentSegment, float startMark, float endMark) {
        super(parentSegment, startMark, endMark);
        wordSegments = new ArrayList<>();
    }

    public AlignedSegment addWordSegment(float end) {
        float start = 0.0f;
        if(wordSegments.size() > 0)
            start = wordSegments.get(wordSegments.size()-1).getEnd();
        return addWordSegment(start, end);
    }

    public AlignedSegment addWordSegment(float start, float end) {
        final AlignedSegment wordSegment = new AlignedSegment(this, start, end);
        // TODO check to make sure segment does not overlap
        wordSegments.add(wordSegment);
        return wordSegment;
    }

    public List<AlignedSegment> getWordSegments() {
        return Collections.unmodifiableList(this.wordSegments);
    }

    public void removeWordSegment(int wIdx) {
        wordSegments.remove(wIdx);
    }

}
