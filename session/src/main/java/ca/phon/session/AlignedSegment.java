package ca.phon.session;

import ca.phon.extensions.ExtendableObject;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;

import javax.print.attribute.standard.Media;

public class AlignedSegment extends ExtendableObject {

    private final Record record;

    private final MediaSegment parentSegment;

    private final AlignedSegment parentAlignedSegment;

    private float start;

    private float end;

    public AlignedSegment(Record record, float startMark, float endMark) {
        this.record = record;
        this.parentSegment = null;
        this.parentAlignedSegment = null;
        setStart(startMark);
        setEnd(endMark);
    }

    public AlignedSegment(MediaSegment parentSegment, float startMark, float endMark) {
        this.record = null;
        this.parentSegment = parentSegment;
        this.parentAlignedSegment = null;
        setStart(startMark);
        setEnd(endMark);
    }
    public AlignedSegment(AlignedSegment parentAlignedSegment, float startMark, float endMark) {
        this.record = null;
        this.parentSegment = null;
        this.parentAlignedSegment = parentAlignedSegment;
        setStart(startMark);
        setEnd(endMark);
    }

    public MediaSegment getParentSegment() {
        if(this.record != null) {
            return record.getMediaSegment();
        } else if(this.parentSegment != null) {
            return this.parentSegment;
        } else if(this.parentAlignedSegment != null) {
            SessionFactory factory = SessionFactory.newFactory();
            MediaSegment retVal = factory.createMediaSegment();
            retVal.setStartValue(this.parentAlignedSegment.getStartTime());
            retVal.setEndValue(this.parentAlignedSegment.getEndTime());
            retVal.setUnitType(this.parentAlignedSegment.getParentSegment().getUnitType());
            return retVal;
        } else {
            throw new IllegalStateException("Aligned segment has not parent");
        }
    }

    public float getStartTime() {
        final MediaSegment parentSeg = getParentSegment();
        return Math.max(parentSeg.getStartValue(),
                parentSeg.getStartValue() + ((parentSeg.getEndValue() - parentSeg.getStartValue()) * this.getStart()));
    }

    public float getEndTime() {
        final MediaSegment parentSeg = getParentSegment();
        return Math.min(parentSeg.getEndValue(),
                parentSeg.getStartValue() + ((parentSeg.getEndValue() - parentSeg.getStartValue()) * this.getEnd()));
    }

    public float getStart() {
        return this.start;
    }

    public float getEnd() {
        return this.end;
    }

    public float setStart(float start) {
        this.start = (float)Math.max(0.0, start);
        return this.start;
    }

    public float setEnd(float end) {
        this.end = (float)Math.min(1.0, end);
        return this.end;
    }

    @Override
    public String toString() {
        final Formatter<MediaSegment> segmentFormatter = FormatterFactory.createFormatter(MediaSegment.class);
        final MediaSegment segment = SessionFactory.newFactory().createMediaSegment();
        segment.setSegment(getStartTime(), getEndTime());
        return segmentFormatter.format(segment);
    }

}
