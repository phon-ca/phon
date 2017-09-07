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
package ca.phon.session.impl;

import java.util.logging.Logger;

import ca.phon.session.*;

/**
 * Default implementation of a session factory.
 */
public class SessionFactoryImpl extends SessionFactory {
	
	private static final Logger LOGGER = Logger
			.getLogger(SessionFactoryImpl.class.getName());

	@Override
	public Session createSession() {
		return new SessionImpl();
	}

	@Override
	public Record createRecord() {
		return new RecordImpl();
	}

	@Override
	public Participant createParticipant() {
		return new ParticipantImpl();
	}

	@Override
	public MediaSegment createMediaSegment() {
		return new MediaSegmentImpl();
	}

	@Override
	public <T> Tier<T> createTier(String name, Class<T> type, boolean grouped) {
		final TierImpl<T> retVal = new TierImpl<T>(name, type, grouped);
		return retVal;
	}

	@Override
	public TierDescription createTierDescription(String name, boolean grouped) {
		return new TierDescriptionImpl(name, grouped);
	}

	@Override
	public TierDescription createTierDescription(String name, boolean grouped,
			Class<?> type) {
		return new TierDescriptionImpl(name, grouped, type);
	}

	@Override
	public Transcriber createTranscriber() {
		return new TranscriberImpl();
	}

	@Override
	public TierViewItem createTierViewItem(String name) {
		return new TierViewItemImpl(name);
	}

	@Override
	public TierViewItem createTierViewItem(String name, boolean visible) {
		return new TierViewItemImpl(name, visible);
	}

	@Override
	public TierViewItem createTierViewItem(String name, boolean visible,
			String font) {
		return new TierViewItemImpl(name, visible, font);
	}

	@Override
	public TierViewItem createTierViewItem(String name, boolean visible,
			boolean locked) {
		return new TierViewItemImpl(name, visible, locked);
	}

	@Override
	public TierViewItem createTierViewItem(String name, boolean visible,
			String font, boolean locked) {
		return new TierViewItemImpl(name, visible, font, locked);
	}

	@Override
	public Comment createComment() {
		return new CommentImpl();
	}

	@Override
	public SessionMetadata createSessionMetadata() {
		return new SessionMetadataImpl();
	}

}
