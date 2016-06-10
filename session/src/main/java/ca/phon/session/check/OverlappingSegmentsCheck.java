package ca.phon.session.check;

import java.util.Map;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.session.MediaSegment;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.Tier;

@PhonPlugin(name="check", version="1", minPhonVersion="2.1.0")
public class OverlappingSegmentsCheck implements SessionCheck, IPluginExtensionPoint<SessionCheck> {

	@Override
	public Class<?> getExtensionType() {
		return SessionCheck.class;
	}

	@Override
	public IPluginExtensionFactory<SessionCheck> getFactory() {
		return (args) -> this;
	}

	@Override
	public void checkSession(SessionValidator validator, Session session, Map<String, Object> options) {
		float lastEndTime = 0f;
		
		for(int rIdx = 0; rIdx < session.getRecordCount(); rIdx++) {
			final Record r = session.getRecord(rIdx);
			final Tier<MediaSegment> segmentTier = r.getSegment();
			if(segmentTier.numberOfGroups() == 1
					&& segmentTier.getGroup(0) != null) {
				final MediaSegment segment = segmentTier.getGroup(0);
				
				// ignore non-segments with no length
				if(segment.getStartValue() == 0f && segment.getEndValue() == 0f) continue;
				
				final float currentStartTime = segment.getStartValue();
				if(currentStartTime < lastEndTime) {
					// issue warning
					final ValidationEvent evt = new ValidationEvent(session, rIdx, "Segment overlaps with previous record");
					validator.fireValidationEvent(evt);
				}
				lastEndTime = segment.getEndValue();
			}
		}
	}

}
