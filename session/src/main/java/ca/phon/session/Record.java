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

package ca.phon.session;

import java.util.*;

import ca.phon.extensions.*;
import ca.phon.ipa.*;
import ca.phon.ipa.alignment.*;
import ca.phon.orthography.*;
import ca.phon.session.spi.*;


/**
 * 
 */
public final class Record extends ExtendableObject {
	
	private final RecordSPI recordImpl;
	
	Record(RecordSPI impl) {
		super();
		this.recordImpl = impl;
	}

	public UUID getUuid() {
		return recordImpl.getUuid();
	}

	public void setUuid(UUID id) {
		recordImpl.setUuid(id);
	}

	public Participant getSpeaker() {
		return recordImpl.getSpeaker();
	}

	public void setSpeaker(Participant participant) {
		recordImpl.setSpeaker(participant);
	}

	public Tier<MediaSegment> getSegment() {
		return recordImpl.getSegment();
	}

	public void setSegment(Tier<MediaSegment> media) {
		recordImpl.setSegment(media);
	}

	public boolean isExcludeFromSearches() {
		return recordImpl.isExcludeFromSearches();
	}

	public void setExcludeFromSearches(boolean excluded) {
		recordImpl.setExcludeFromSearches(excluded);
	}

	public Tier<Orthography> getOrthography() {
		return recordImpl.getOrthography();
	}

	public void setOrthography(Tier<Orthography> ortho) {
		recordImpl.setOrthography(ortho);
	}

	public Tier<IPATranscript> getIPATarget() {
		return recordImpl.getIPATarget();
	}

	public void setIPATarget(Tier<IPATranscript> ipa) {
		recordImpl.setIPATarget(ipa);
	}

	public Tier<IPATranscript> getIPAActual() {
		return recordImpl.getIPAActual();
	}

	public void setIPAActual(Tier<IPATranscript> ipa) {
		recordImpl.setIPAActual(ipa);
	}

	public Tier<PhoneMap> getPhoneAlignment() {
		return recordImpl.getPhoneAlignment();
	}

	public void setPhoneAlignment(Tier<PhoneMap> phoneAlignment) {
		recordImpl.setPhoneAlignment(phoneAlignment);
	}

	public Tier<TierString> getNotes() {
		return recordImpl.getNotes();
	}

	public void setNotes(Tier<TierString> notes) {
		recordImpl.setNotes(notes);
	}

	public Class<?> getTierType(String name) {
		return recordImpl.getTierType(name);
	}

	public <T> Tier<T> getTier(String name, Class<T> type) {
		return recordImpl.getTier(name, type);
	}

	public Tier<?> getTier(String name) {
		return recordImpl.getTier(name);
	}

	public Set<String> getExtraTierNames() {
		return recordImpl.getExtraTierNames();
	}

	public <T> List<Tier<T>> getTiersOfType(Class<T> type) {
		return recordImpl.getTiersOfType(type);
	}

	public boolean hasTier(String name) {
		return recordImpl.hasTier(name);
	}

	public void putTier(Tier<?> tier) {
		recordImpl.putTier(tier);
	}

	public void removeTier(String name) {
		recordImpl.removeTier(name);
	}

	public int getNumberOfComments() {
		return recordImpl.getNumberOfComments();
	}

	public Comment getComment(int idx) {
		return recordImpl.getComment(idx);
	}

	public void addComment(Comment comment) {
		recordImpl.addComment(comment);
	}

	public void removeComment(Comment comment) {
		recordImpl.removeComment(comment);
	}

	public void removeComment(int idx) {
		recordImpl.removeComment(idx);
	}
	
	/* Groups */
	/**
	 * Get the number of aligned groups in the record.
	 * The number of groups for a record is determined by the number
	 * of groups in the orthoraphy tier.
	 * 
	 * @return number of groups
	 */
	public int numberOfGroups() {
		return getOrthography().numberOfGroups();
	}
	
	/**
	 * Get the group at the specified index
	 * 
	 * @param idx
	 * 
	 * @return the specified group
	 */
	public Group getGroup(int idx) {
		if(idx >= 0 && idx < numberOfGroups()) {
			return SessionFactory.newFactory().createGroup(this, idx);
		} else {
			throw new IndexOutOfBoundsException("Invalid group index " + idx);
		}
	}
	
	/**
	 * Delete the group at the specified index.  This will
	 * affect all grouped tiers.
	 * 
	 * @param idx the group to remove
	 */
	public void removeGroup(int idx) {
		if(idx < 0 && idx >= numberOfGroups())
			throw new IndexOutOfBoundsException("Invalid group index " + idx);

		if(idx < getOrthography().numberOfGroups())
			getOrthography().removeGroup(idx);
		if(idx < getIPAActual().numberOfGroups())
			getIPAActual().removeGroup(idx);
		if(idx < getIPATarget().numberOfGroups())
			getIPATarget().removeGroup(idx);
		if(idx < getPhoneAlignment().numberOfGroups())
			getPhoneAlignment().removeGroup(idx);

		for(String tierName:getExtraTierNames()) {
			final Tier<?> tier = getTier(tierName);
			if(tier.isGrouped() && idx < tier.numberOfGroups()) {
				tier.removeGroup(idx);
			}
		}
	}
		
	/**
	 * Add a new group to the end of the record data
	 * 
	 * @return the new group
	 */
	public Group addGroup() {
		int gidx = numberOfGroups();

		SessionFactory factory = SessionFactory.newFactory();
		
		getOrthography().addGroup(new Orthography());
		getIPATarget().addGroup(new IPATranscript());
		getIPAActual().addGroup(new IPATranscript());
		getPhoneAlignment().addGroup(new PhoneMap(getIPATarget().getGroup(gidx), getIPAActual().getGroup(gidx)));

		for(String tierName:getExtraTierNames()) {
			final Tier<TierString> tier = getTier(tierName, TierString.class);
			if(tier.isGrouped())
				tier.addGroup();
			else if(tier.numberOfGroups() == 0)
				tier.addGroup(new TierString());
		}

		if(getNotes().numberOfGroups() == 0)
			getNotes().addGroup(new TierString());
		if(getSegment().numberOfGroups() == 0)
			getSegment().addGroup(factory.createMediaSegment());

		return factory.createGroup(this, gidx);
	}
	
	/**
	 * Add a new group at the specified index.  This method
	 * will add a new group value to each grouped tier.
	 * 
	 * @param idx
	 * @return the new group
	 * 
	 * @throws ArrayIndexOutOfBoundsException if idx is out of bounds
	 */
	public Group addGroup(int idx) {
		if(getOrthography().numberOfGroups() < idx)
			while(getOrthography().numberOfGroups() < idx) {
				getOrthography().addGroup(new Orthography());
			}
		getOrthography().addGroup(idx, new Orthography());

		if(getIPATarget().numberOfGroups() < idx)
			while(getIPATarget().numberOfGroups() < idx) {
				getIPATarget().addGroup(new IPATranscript());
			}
		getIPATarget().addGroup(idx, new IPATranscript());

		if(getIPAActual().numberOfGroups() < idx)
			while(getIPAActual().numberOfGroups() < idx) {
				getIPAActual().addGroup(new IPATranscript());
			}
		getIPAActual().addGroup(idx, new IPATranscript());

		if(getPhoneAlignment().numberOfGroups() < idx)
			while(getPhoneAlignment().numberOfGroups() < idx) {
				getPhoneAlignment().addGroup(new PhoneMap(getIPATarget().getGroup(getPhoneAlignment().numberOfGroups()), getIPAActual().getGroup(getPhoneAlignment().numberOfGroups())));
			}
		getPhoneAlignment().addGroup(idx, new PhoneMap(getIPATarget().getGroup(idx), getIPAActual().getGroup(idx)));
		
		for(String tierName:getExtraTierNames()) {
			final Tier<TierString> tier = getTier(tierName, TierString.class);
			if(tier.isGrouped()) {
				if(tier.numberOfGroups() < idx)
					while(tier.numberOfGroups() < idx) {
						tier.addGroup(new TierString());
					}
				tier.addGroup(idx, new TierString());
			} else if(tier.numberOfGroups() == 0)
				tier.addGroup();
		}

		return SessionFactory.newFactory().createGroup(this, idx);
	}
	
	/**
	 * Merge the two specified gropus.
	 * 
	 * @param grp1
	 * @param grp2
	 * 
	 * @return the word index of grp1 where the merge occured.
	 * 
	 * @throws IllegalArgumentException if <code>grp1</grp1>
	 *  or <code>grp2</code> are not adjacent groups or <code>grp2 <= grp1</code>
	 * @throws ArrayIndexOutOfBoundsException if either <code>grp1</code>
	 *  or <code>grp2</code> is out of bounds
	 */
	public int mergeGroups(int grp1, int grp2) {
		if(grp2 <= grp1) {
			throw new IllegalArgumentException("grp2 must be greater than grp1");
		}
		if(grp2 - grp1 != 1) {
			throw new IllegalArgumentException("groups must be adjacent to merge");
		}
		if(grp1 < 0 || grp1 >= numberOfGroups()) {
			throw new ArrayIndexOutOfBoundsException(grp1);
		}
		if(grp2 < 0 || grp2 >= numberOfGroups()) {
			throw new ArrayIndexOutOfBoundsException(grp2);
		}

		final Group group1 = getGroup(grp1);
		final Group group2 = getGroup(grp2);
		int retVal = group1.getAlignedWordCount();

		// orthography
		final OrthographyBuilder orthoBuilder = new OrthographyBuilder();
		orthoBuilder.append(group1.getOrthography());
		orthoBuilder.append(group2.getOrthography());
		group1.setOrthography(orthoBuilder.toOrthography());

		// ipa target
		final IPATranscriptBuilder tBuilder = new IPATranscriptBuilder();
		tBuilder.append(group1.getIPATarget());
		if(tBuilder.size() > 0) tBuilder.appendWordBoundary();
		tBuilder.append(group2.getIPATarget());
		final IPATranscript ipaTarget = tBuilder.toIPATranscript();
		group1.setIPATarget(ipaTarget);

		final IPATranscriptBuilder aBuilder = new IPATranscriptBuilder();
		aBuilder.append(group1.getIPAActual());
		if(aBuilder.size() > 0) aBuilder.appendWordBoundary();
		aBuilder.append(group2.getIPAActual());
		final IPATranscript ipaActual = aBuilder.toIPATranscript();
		group1.setIPAActual(ipaActual);

		// other tiers
		for(String tierName:getExtraTierNames()) {
			if(getTier(tierName).isGrouped()) {
				final TierString tierVal = group1.getTier(tierName, TierString.class);
				if(tierVal != null) {
					final String newVal = new String(tierVal + " " + group2.getTier(tierName, TierString.class)).trim();
					group1.setTier(tierName, TierString.class, new TierString(newVal));
				}
			}
		}

		removeGroup(grp2);

		return retVal;
	}
	
	/**
	 * Split groups based on group and aligned word index.
	 * 
	 * @param grp
	 * @param wrd
	 * 
	 * @return the new group created by the split
	 * 
	 * @throws ArrayIndexOutOfBoundsException if either <code>grp</code> or <code>wrd</code>
	 *  are out of bounds
	 */
	public Group splitGroup(int grp, int wrd) {
		if(grp < 0 || grp >= numberOfGroups()) {
			throw new ArrayIndexOutOfBoundsException(grp);
		}
		final Group group = getGroup(grp);
		if(wrd < 0 || wrd >= group.getAlignedWordCount()) {
			throw new ArrayIndexOutOfBoundsException(wrd);
		}
		final Word word = group.getAlignedWord(wrd);

		final Group newGroup = addGroup(grp+1);

		// orthography
		final OrthoElement ele = word.getOrthography();
		int wordIdx = group.getOrthography().indexOf(ele);
		final Orthography ortho = group.getOrthography().subsection(0, wordIdx);
		final Orthography newOrtho = group.getOrthography().subsection(wordIdx, group.getOrthography().length());
		group.setOrthography(ortho);
		newGroup.setOrthography(newOrtho);

		// ipa target
		final IPATranscript ipaT = word.getIPATarget();
		int ipaTIdx = group.getIPATarget().indexOf(ipaT);
		IPATranscript ipaTarget = group.getIPATarget();
		IPATranscript newIpaTarget = new IPATranscript();
		if(ipaTIdx >= 0) {
			ipaTarget = group.getIPATarget().subsection(0, (ipaTIdx-1 >= 0 ? ipaTIdx-1 : 0));
			newIpaTarget = group.getIPATarget().subsection(ipaTIdx, group.getIPATarget().length());
		}
		group.setIPATarget(ipaTarget);
		newGroup.setIPATarget(newIpaTarget);

		// ipa actual
		final IPATranscript ipaA = word.getIPAActual();
		int ipaAIdx = group.getIPAActual().indexOf(ipaA);
		IPATranscript ipaActual = group.getIPAActual();
		IPATranscript newIpaActual = new IPATranscript();
		if(ipaAIdx >= 0) {
			ipaActual = group.getIPAActual().subsection(0, (ipaAIdx-1 >= 0 ? ipaAIdx-1 :0));
			newIpaActual = group.getIPAActual().subsection(ipaAIdx, group.getIPAActual().length());
		}
		group.setIPAActual(ipaActual);
		newGroup.setIPAActual(newIpaActual);

		// other tiers
		for(String tierName:getExtraTierNames()) {
			if(getTier(tierName).isGrouped()) {
				final TierString tierVal = group.getTier(tierName, TierString.class);
				if(tierVal != null) {
					final String words[] = tierVal.split("\\p{Space}");

					String val = "";
					String newVal = "";
					for(int i = 0; i < words.length; i++) {
						if(i < wrd) {
							val += (val.length() > 0 ? " " : "") + words[i];
						} else {
							newVal += (newVal.length() > 0 ? " " : "") + words[i];
						}
					}
					group.setTier(tierName, TierString.class, new TierString(val));
					newGroup.setTier(tierName, TierString.class, new TierString(newVal));
				}
			}
		}

		return newGroup;
	}
	
}
