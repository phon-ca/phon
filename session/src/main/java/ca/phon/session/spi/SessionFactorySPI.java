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
package ca.phon.session.spi;

import ca.phon.session.TierAlignmentRules;

public interface SessionFactorySPI {

	/**
	 * Create a new empty session.
	 * Tier view 
	 * 
	 * @return a new session impl
	 */
	public SessionSPI createSession();
	
	/**
	 * Create comment
	 * 
	 * @return new comment impl
	 */
	public CommentSPI createComment();
	
	/**
	 * Session metadata
	 * 
	 * @return
	 */
	public SessionMetadataSPI createSessionMetadata();

	public TranscriptSPI createTranscript();

	/**
	 * Create a new record.
	 * 
	 * @return a new empty record
	 */
	public RecordSPI createRecord();
	
	/**
	 * Create a new participant object.
	 * 
	 * @return new participant object
	 */
	public ParticipantSPI createParticipant();
	
	/**
	 * Create a new transcriber object.
	 * 
	 * @return new transcriber
	 */
	public TranscriberSPI createTranscriber();
	
	/**
	 * Create a new media segment
	 */
	public MediaSegmentSPI createMediaSegment();
	
	/**
	 * Create a new tier object with the specified type.
	 * 
	 * @param name
	 * @param type
	 * @param grouped
	 * @return the new tier
	 */
	public <T> TierSPI<T> createTier(String name, Class<T> type, TierAlignmentRules tierAlignmentRules);
	
	/**
	 * Create tier description.
	 * 
	 * @param name
	 * @param type
	 * @param tierAlignmentRules
	 * 
	 * @return new tier description
	 */
	public TierDescriptionSPI createTierDescription(String name, Class<?> type, TierAlignmentRules tierAlignmentRules);
	
	/**
	 * Create a tier display and ordering object
	 * @param name
	 * @param visible
	 * @param font
	 * @param locked
	 * @return
	 */
	public TierViewItemSPI createTierViewItem(String name, boolean visible, String font, boolean locked);
	
}
