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
		final Map<Participant, Float> endTimes = new HashMap<>();
		endTimes.put(Participant.UNKNOWN, 0.0f);

		for(int rIdx = 0; rIdx < session.getRecordCount(); rIdx++) {
			final Record r = session.getRecord(rIdx);
			Float lastEndTime = endTimes.get(r.getSpeaker());
			if(lastEndTime == null) {
				lastEndTime = 0.0f;
				endTimes.put(r.getSpeaker(), lastEndTime);
			}
			final Tier<MediaSegment> segmentTier = r.getSegment();
			if(segmentTier.numberOfGroups() == 1
					&& segmentTier.getGroup(0) != null) {
				final MediaSegment segment = segmentTier.getGroup(0);

				// ignore non-segments with no length
				if(segment.getStartValue() == 0f && segment.getEndValue() == 0f) continue;

				final float currentStartTime = segment.getStartValue();
				if(currentStartTime < lastEndTime) {
					// issue warning
					final ValidationEvent evt = new ValidationEvent(session, rIdx, "Segment overlaps with previous record for " + r.getSpeaker());
					validator.fireValidationEvent(evt);
				}
				lastEndTime = segment.getEndValue();
				endTimes.put(r.getSpeaker(), lastEndTime);
			}
		}
	}

}
