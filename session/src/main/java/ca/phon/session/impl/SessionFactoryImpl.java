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

import ca.phon.plugin.*;
import ca.phon.session.spi.*;

/**
 * Default implementation of a session factory.
 */
@Rank(0)
public class SessionFactoryImpl implements SessionFactorySPI, IPluginExtensionPoint<SessionFactorySPI> {
	
	@Override
	public SessionSPI createSession() {
		return new SessionImpl();
	}

	@Override
	public RecordSPI createRecord() {
		return new RecordImpl();
	}

	@Override
	public ParticipantSPI createParticipant() {
		return new ParticipantImpl();
	}

	@Override
	public MediaSegmentSPI createMediaSegment() {
		return new MediaSegmentImpl();
	}

	@Override
	public <T> TierSPI<T> createTier(String name, Class<T> type, boolean grouped) {
		final TierImpl<T> retVal = new TierImpl<T>(name, type, grouped);
		return retVal;
	}

	@Override
	public TierDescriptionSPI createTierDescription(String name, boolean grouped,
			Class<?> type) {
		return new TierDescriptionImpl(name, grouped, type);
	}

	@Override
	public TranscriberSPI createTranscriber() {
		return new TranscriberImpl();
	}

	@Override
	public TierViewItemSPI createTierViewItem(String name, boolean visible,
			String font, boolean locked) {
		return new TierViewItemImpl(name, visible, font, locked);
	}

	@Override
	public CommentSPI createComment() {
		return new CommentImpl();
	}

	@Override
	public SessionMetadataSPI createSessionMetadata() {
		return new SessionMetadataImpl();
	}

	@Override
	public Class<?> getExtensionType() {
		return SessionFactorySPI.class;
	}

	@Override
	public IPluginExtensionFactory<SessionFactorySPI> getFactory() {
		return (args) -> this;
	}

}
