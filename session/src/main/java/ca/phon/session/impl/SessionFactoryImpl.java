/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
import ca.phon.session.CommentType;
import ca.phon.session.GemType;
import ca.phon.session.spi.*;
import ca.phon.session.tierdata.TierData;

import java.util.List;
import java.util.Map;

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
	public <T> TierSPI<T> createTier(String name, Class<T> type, Map<String, String> tierParameters, boolean excludeFromAlignment, boolean blind, List<String> subtypeDelim, String subtypeExpr) {
        return new TierImpl<T>(name, type, tierParameters, excludeFromAlignment, blind, subtypeDelim, subtypeExpr);
	}

	@Override
	public TierDescriptionSPI createTierDescription(String name, Class<?> type, Map<String, String> tierParameters, boolean excludeFromAlignment, boolean blind, List<String> subtypeDelim, String subtypeExpr) {
		return new TierDescriptionImpl(name, type, tierParameters, excludeFromAlignment, blind, subtypeDelim, subtypeExpr);
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
	public GemSPI createGem(GemType gemType, String label) {
		return new GemImpl(gemType, label);
	}

	@Override
	public CommentSPI createComment(CommentType commentType, TierData value) {
		return new CommentImpl(commentType, value);
	}

	@Override
	public TranscriptSPI createTranscript() { return new TranscriptImpl(); }

	@Override
	public Class<?> getExtensionType() {
		return SessionFactorySPI.class;
	}

	@Override
	public IPluginExtensionFactory<SessionFactorySPI> getFactory() {
		return (args) -> this;
	}

}
