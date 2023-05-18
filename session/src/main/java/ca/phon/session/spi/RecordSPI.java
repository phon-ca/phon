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

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.Orthography;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.util.Language;

import java.util.*;

public interface RecordSPI {

	/**
	 * @see Record#getUuid()
	 * @return
	 */
	public UUID getUuid();

	/**
	 * @see Record#setUuid(UUID)
	 * @param id
	 */
	public void setUuid(UUID id);

	/**
	 * @see Record#getSpeaker()
	 * @return
	 */
	public Participant getSpeaker();

	/**
	 * @see Record#setSpeaker(Participant)
	 * @param participant
	 */
	public void setSpeaker(Participant participant);

	/**
	 * @see Record#getSegmentTier()
	 * @return
	 */
	public Tier<MediaSegment> getSegmentTier();

	/**
	 * @see Record#isExcludeFromSearches()
	 * @return
	 */
	public boolean isExcludeFromSearches();

	/**
	 * @see Record#setExcludeFromSearches(boolean)
	 * @param excluded
	 */
	public void setExcludeFromSearches(boolean excluded);

	/**
	 * @see Record#getOrthographyTier()
	 * @return
	 */
	public Tier<Orthography> getOrthographyTier();

	/**
	 * @see Record#getIPATargetTier()
	 * @return
	 */
	public Tier<IPATranscript> getIPATargetTier();

	/**
	 * @see Record#getIPAActualTier()
	 * @return
	 */
	public Tier<IPATranscript> getIPAActualTier();

	/**
	 * @see Record#getPhoneAlignmentTier()
	 * @return
	 */
	public Tier<PhoneMap> getPhoneAlignmentTier();

	/**
	 * @see Record#getNotesTier()
	 * @return
	 */
	public Tier<UserTierData> getNotesTier();
	
	/**
	 * @see Record#getTier(String, Class)
	 * @param name
	 * @param type
	 * @return
	 * @param <T>
	 */
	public <T> Tier<T> getTier(String name, Class<T> type);

	/**
	 * @see Record#getUserDefinedTierNames() 
	 * @return
	 */
	public Set<String> getUserDefinedTierNames();

	/**
	 * @see Record#hasTier(String) 
	 * @param name
	 * @return
	 */
	public boolean hasTier(String name);

	/**
	 * @see Record#putTier(Tier) 
	 * @param tier
	 */
	public void putTier(Tier<?> tier);

	/**
	 * @see Record#removeTier(String) 
	 * @param name
	 */
	public void removeTier(String name);

}
