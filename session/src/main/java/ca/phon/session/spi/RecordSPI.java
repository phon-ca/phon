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
import ca.phon.util.Language;

import java.util.*;

public interface RecordSPI {
	
	public UUID getUuid();
	
	public void setUuid(UUID id);
	
	public Participant getSpeaker();
	
	public void setSpeaker(Participant participant);

	public Tier<MediaSegment> getSegmentTier();

	public Tier<GroupSegment> getGroupSegment();

	public boolean isExcludeFromSearches();
	
	public void setExcludeFromSearches(boolean excluded);
	
	public Tier<Orthography> getOrthographyTier();

	public Tier<IPATranscript> getIPATargetTier();
	
	public Tier<IPATranscript> getIPAActualTier();
	
	public Tier<PhoneMap> getPhoneAlignmentTier();
	
	public Tier<UserTierData> getNotesTier();
	
	public Class<?> getTierType(String name);
	
	public <T> Tier<T> getTier(String name, Class<T> type);

	public Tier<?> getTier(String name);

	public Set<String> getUserDefinedTierNames();

	public <T> List<Tier<T>> getTiersOfType(Class<T> type);

	public boolean hasTier(String name);

	public void putTier(Tier<?> tier);
	
	/**
	 * Remove the dependent tier with the given name.
	 * 
	 * @param name
	 */
	public void removeTier(String name);
	
	/**
	 * number of comments
	 * 
	 * @return number of comments
	 */
	public int getNumberOfComments();
	
	/**
	 * get comment at given index
	 * 
	 * @param idx
	 * @return comment
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public Comment getComment(int idx);
	
	/**
	 * Add comment
	 * 
	 * @param comment
	 */
	public void addComment(Comment comment);
	
	/**
	 * Remove comment
	 * 
	 * @param comment
	 */
	public void removeComment(Comment comment);

	public void removeComment(int idx);

}
