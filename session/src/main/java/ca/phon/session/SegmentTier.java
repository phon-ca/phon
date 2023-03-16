package ca.phon.session;

public final class SegmentTier {

    private final MediaSegment recordSegment;

    private Tier<GroupSegment> groupSegmentTier;

    SegmentTier(MediaSegment recordSegment) {
        this.recordSegment = recordSegment;
    }

    public MediaSegment getRecordSegment() {
        return this.recordSegment;
    }

    public void setRecordSegment(float start, float end) {
        this.recordSegment.setStartValue(start);
        this.recordSegment.setEndValue(end);
    }

    public void setRecordSegment(MediaSegment recordSegment) {
        this.recordSegment.setStartValue(recordSegment.getStartValue());
        this.recordSegment.setEndValue(recordSegment.getEndValue());
        this.recordSegment.setUnitType(recordSegment.getUnitType());
    }

    public Tier<GroupSegment> getGroupSegmentTier() {
        return this.groupSegmentTier;
    }

    public void putGroupSegmentTier(Tier<GroupSegment> groupSegmentTier) {
        this.groupSegmentTier = groupSegmentTier;
    }

}
