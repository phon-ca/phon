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
package ca.phon.session.impl;

import org.apache.logging.log4j.LogManager;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.Rank;
import ca.phon.session.Comment;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SessionMetadata;
import ca.phon.session.Tier;
import ca.phon.session.TierDescription;
import ca.phon.session.TierViewItem;
import ca.phon.session.Transcriber;

/**
 * Default implementation of a session factory.
 */
@Rank(0)
public class SessionFactoryImpl extends SessionFactory implements IPluginExtensionPoint<SessionFactory> {
	
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SessionFactoryImpl.class.getName());

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

	@Override
	public Class<?> getExtensionType() {
		return SessionFactory.class;
	}

	@Override
	public IPluginExtensionFactory<SessionFactory> getFactory() {
		return (args) -> this;
	}

}
