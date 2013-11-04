package ca.phon.session;

import ca.phon.util.Tuple;

/**
 * Used to calculate the start/end time values for
 * various segment types.
 */
public class SegmentCalculator {

	/**
	 * TODO description
	 * 
	 * @param session
	 * @param recordIndex
	 */
	public static MediaSegment contiguousSegment(Session session, int recordIndex) {
		final SessionFactory factory = SessionFactory.newFactory();
		final MediaSegment retVal = factory.createMediaSegment();
		
		final Record utt = session.getRecord(recordIndex);
		final Tier<MediaSegment> segmentTier = utt.getSegment();
		final MediaSegment media = segmentTier.getGroup(0);
		
		if(media == null) return retVal;
		
		final long startTime = (long)media.getStartValue();
		final Participant speaker = utt.getSpeaker();
		
		Record endRecord = null;
		for(int i = recordIndex+1; i < session.getRecordCount(); i++) {
			final Record u = session.getRecord(i);
			
			boolean sameSpeaker = 
					(speaker == null && u.getSpeaker() == null) ||
					(speaker != null && u.getSpeaker() != null && speaker.getId().equals(u.getSpeaker().getId()));
			if(!sameSpeaker)
				break;
			endRecord = u;
		}
		if(endRecord == null)
			endRecord = utt;
		
		final Tier<MediaSegment> endSegmentTier = endRecord.getSegment();
		final MediaSegment endMedia = endSegmentTier.getGroup(0);
		if(endMedia == null) return retVal;
		long endTime = (long)endMedia.getEndValue();
		
		retVal.setStartValue(startTime);
		retVal.setEndValue(endTime);
		retVal.setUnitType(MediaUnit.Millisecond);
		
		return retVal;
	}
	
	/**
	 * Calculate conversation period from given record
	 * 
	 * @param t
	 * @param recordIndex
	 * 
	 * @return segment for conversation period starting
	 *  at given record
	 */
	public static MediaSegment conversationPeriod(Session t, int recordIndex) {
		final SessionFactory factory = SessionFactory.newFactory();
		final MediaSegment retVal = factory.createMediaSegment();
		
		final Record utt = t.getRecord(recordIndex);
		final Tier<MediaSegment> segmentTier = utt.getSegment();
		final MediaSegment media = segmentTier.getGroup(0);
		
		if(media == null) return retVal;
		
		final long startTime = (long)media.getStartValue();
		final Participant speaker = utt.getSpeaker();
		
		Record endRecord = null;
		boolean hasSwitched = false;
		for(int i = recordIndex+1; i < t.getRecordCount(); i++) {
			final Record u = t.getRecord(i);
			
			boolean sameSpeaker = 
					(speaker == null && u.getSpeaker() == null) ||
					(speaker != null && u.getSpeaker() != null && speaker.getId().equals(u.getSpeaker().getId()));
			if(hasSwitched && sameSpeaker) {
				endRecord = u;
				break;
			}
			if(!sameSpeaker)
				hasSwitched = true;
		}
		if(endRecord == null)
			endRecord = t.getRecord(t.getRecordCount()-1);
		
		final Tier<MediaSegment> endSegmentTier = endRecord.getSegment();
		final MediaSegment endMedia = endSegmentTier.getGroup(0);
		if(endMedia == null) return retVal;
		
		long endTime = (long)endMedia.getStartValue();
		
		retVal.setStartValue(startTime);
		retVal.setEndValue(endTime);
		retVal.setUnitType(MediaUnit.Millisecond);
		
		return retVal;
	}
	
}
