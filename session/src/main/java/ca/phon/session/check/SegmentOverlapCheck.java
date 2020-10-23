/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.session.check;

import java.util.*;

import ca.phon.plugin.*;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.util.*;

@PhonPlugin(name="Check Segment Overlaps", comments="Check for overlapping media segments")
@Rank(3)
public class SegmentOverlapCheck implements SessionCheck, IPluginExtensionPoint<SessionCheck> {

	/** Overlap tolerance in ms */
	public final static String OVERLAP_TOLERANCE_PROPERTY =
			SegmentOverlapCheck.class.getName() + ".overlapTolerance";
	public final static int DEFAULT_OVERLAP_TOLERANCE = 200;
	private int overlapTolerance =
			PrefHelper.getInt(OVERLAP_TOLERANCE_PROPERTY, DEFAULT_OVERLAP_TOLERANCE);

	public int getOverlapTolerance() {
		return this.overlapTolerance;
	}

	public void setOverlapTolerance(int overlapTolerance) {
		this.overlapTolerance = overlapTolerance;
	}

	@Override
	public Class<?> getExtensionType() {
		return SessionCheck.class;
	}

	@Override
	public IPluginExtensionFactory<SessionCheck> getFactory() {
		return (args) -> this;
	}

	@Override
	public boolean checkSession(SessionValidator validator, Session session) {
		boolean modified = false;
		final Map<Participant, Float> endTimes = new HashMap<>();
		final Map<Participant, Integer> lastRecords = new HashMap<>();
		endTimes.put(Participant.UNKNOWN, 0.0f);
		lastRecords.put(Participant.UNKNOWN, 0);

		for(int rIdx = 0; rIdx < session.getRecordCount(); rIdx++) {
			final Record r = session.getRecord(rIdx);
			Float lastEndTime = endTimes.get(r.getSpeaker());
			if(lastEndTime == null) {
				lastEndTime = 0.0f;
				endTimes.put(r.getSpeaker(), lastEndTime);
			}
			if(lastRecords.get(r.getSpeaker()) == null) {
				lastRecords.put(r.getSpeaker(), 0);
			}
			final Tier<MediaSegment> segmentTier = r.getSegment();
			if(segmentTier.numberOfGroups() == 1
					&& segmentTier.getGroup(0) != null) {
				final MediaSegment segment = segmentTier.getGroup(0);

				// ignore non-segments with no length
				if(segment.getStartValue() == 0f && segment.getEndValue() == 0f) continue;

				final float currentStartTime = segment.getStartValue();
				final float diffMs = currentStartTime - lastEndTime;

				if( (diffMs < 0) && (Math.abs(diffMs) > getOverlapTolerance()) ) {
					// issue warning
					final ValidationEvent evt = new ValidationEvent(session, rIdx, "Segment overlaps with previous record for " + r.getSpeaker() + " (#" + (lastRecords.get(r.getSpeaker())+1) + ")");
					validator.fireValidationEvent(evt);
				}
				lastEndTime = segment.getEndValue();
				endTimes.put(r.getSpeaker(), lastEndTime);
				lastRecords.put(r.getSpeaker(), rIdx);
			}
		}
		return modified;
	}

	@Override
	public Properties getProperties() {
		return new Properties();
	}

	@Override
	public void loadProperties(Properties props) {
		
	}

}
