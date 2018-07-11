/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.session.check;

import java.util.HashMap;
import java.util.Map;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.Tier;
import ca.phon.util.PrefHelper;

@PhonPlugin(name="check", version="1", minPhonVersion="2.1.0")
public class OverlappingSegmentsCheck implements SessionCheck, IPluginExtensionPoint<SessionCheck> {

	/** Overlap tolerance in ms */
	public final static String OVERLAP_TOLERANCE_PROPERTY =
			OverlappingSegmentsCheck.class.getName() + ".overlapTolerance";
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
	public void checkSession(SessionValidator validator, Session session, Map<String, Object> options) {
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
	}

}
