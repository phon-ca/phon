package ca.phon.session;

import ca.phon.session.spi.TierSPI;

import javax.print.attribute.standard.Media;

public final class SegmentTier {

    private MediaSegment recordSegment;

    private final Tier<MediaSegment> recordSegmentTier;

    private Tier<GroupSegment> groupSegmentTier;

    SegmentTier(MediaSegment recordSegment) {
        this.recordSegment = recordSegment;
        this.recordSegmentTier = new Tier<>(new RecordSegmentSPI());
    }

    public MediaSegment getRecordSegment() {
        return this.recordSegment;
    }

    public void setRecordSegment(MediaSegment recordSegment) {
        this.recordSegment = recordSegment;
    }

    public Tier<MediaSegment> getRecordSegmentTier() {
        return this.recordSegmentTier;
    }

    public Tier<GroupSegment> getGroupSegmentTier() {
        return this.groupSegmentTier;
    }

    public void putGroupSegmentTier(Tier<GroupSegment> groupSegmentTier) {
        this.groupSegmentTier = groupSegmentTier;
    }

    private final class RecordSegmentSPI implements TierSPI<MediaSegment> {
        @Override
        public String getName() {
            return SystemTierType.Segment.getName();
        }

        @Override
        public Class<?> getDeclaredType() {
            return MediaSegment.class;
        }

        @Override
        public boolean isGrouped() {
            return false;
        }

        @Override
        public int numberOfGroups() {
            return 1;
        }

        @Override
        public MediaSegment getGroup(int idx) {
            return getRecordSegment();
        }

        @Override
        public void setGroup(int idx, MediaSegment val) {
            if(idx == 0) {
                setRecordSegment(val);
            }
        }

        @Override
        public void addGroup() {}

        @Override
        public void addGroup(int idx) {}

        @Override
        public void addGroup(MediaSegment val) {}

        @Override
        public void addGroup(int idx, MediaSegment val) {}

        @Override
        public MediaSegment removeGroup(int idx) { return null; }

        @Override
        public void removeAll() { }
    };

}
