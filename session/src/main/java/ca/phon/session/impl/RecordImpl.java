/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.extensions.UnvalidatedValue;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.OrthoElement;
import ca.phon.orthography.Orthography;
import ca.phon.orthography.OrthographyBuilder;
import ca.phon.session.Comment;
import ca.phon.session.Group;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.SessionFactory;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.session.Word;

/**
 * Basic record implementation
 *
 */
public class RecordImpl implements Record {
	
	/* Attributes */
	private final AtomicReference<Participant> participantRef = new AtomicReference<Participant>();
	
	private volatile boolean excludeFromSearches = false;
	
	private final AtomicReference<UUID> uuidRef = new AtomicReference<UUID>(UUID.randomUUID());
	
	/* default tiers */
	private final Tier<Orthography> orthography;
	
	private final Tier<IPATranscript> ipaTarget;
	
	private final Tier<IPATranscript> ipaActual;
	
	private final Tier<MediaSegment> segment;
	
	private final Tier<String> notes;
	
	private final Tier<PhoneMap> alignment;
	
	/* Additional tiers */
	private final Map<String, Tier<?>> userDefined;

	RecordImpl() {
		super();
		
		final SessionFactory factory = SessionFactory.newFactory();
		orthography = factory.createTier(SystemTierType.Orthography.getName(), Orthography.class, SystemTierType.Orthography.isGrouped());
		ipaTarget = factory.createTier(SystemTierType.IPATarget.getName(), IPATranscript.class, SystemTierType.IPATarget.isGrouped());
		ipaActual = factory.createTier(SystemTierType.IPAActual.getName(), IPATranscript.class, SystemTierType.IPAActual.isGrouped());
		segment = factory.createTier(SystemTierType.Segment.getName(), MediaSegment.class, SystemTierType.Segment.isGrouped());
		notes = factory.createTier(SystemTierType.Notes.getName(), String.class, SystemTierType.Notes.isGrouped());
		alignment = factory.createTier(SystemTierType.SyllableAlignment.getName(), PhoneMap.class, SystemTierType.SyllableAlignment.isGrouped());
		
		userDefined = 
				Collections.synchronizedMap(new HashMap<String, Tier<?>>());
		
		extSupport.initExtensions();
	}
	
	@Override
	public UUID getUuid() {
		return this.uuidRef.get();
	}
	
	@Override
	public void setUuid(UUID id) {
		uuidRef.getAndSet(id);
	}

	@Override
	public Participant getSpeaker() {
		return participantRef.get();
	}

	@Override
	public void setSpeaker(Participant participant) {
		participantRef.getAndSet(participant);
	}

	@Override
	public Tier<MediaSegment> getSegment() {
		return segment;
	}

	@Override
	public void setSegment(Tier<MediaSegment> media) {
		this.segment.removeAll();
		for(int i = 0; i < media.numberOfGroups(); i++) {
			this.segment.addGroup(media.getGroup(i));
		}
	}

	@Override
	public boolean isExcludeFromSearches() {
		return this.excludeFromSearches;
	}

	@Override
	public void setExcludeFromSearches(boolean excluded) {
		this.excludeFromSearches = excluded;
	}

	@Override
	public Tier<Orthography> getOrthography() {
		return orthography;
	}

	@Override
	public void setOrthography(Tier<Orthography> ortho) {
		this.orthography.removeAll();
		for(int i = 0; i < ortho.numberOfGroups(); i++) {
			this.orthography.addGroup(ortho.getGroup(i));
		}
	}

	@Override
	public int numberOfGroups() {
		return this.orthography.numberOfGroups();
	}

	@Override
	public Group getGroup(int idx) {
		if(idx >= 0 && idx < numberOfGroups()) {
			return new GroupImpl(this, idx);
		} else {
			throw new IndexOutOfBoundsException("Invalid group index " + idx);
		}
	}

	@Override
	public void removeGroup(int idx) {
		if(idx < 0 && idx >= numberOfGroups()) 
			throw new IndexOutOfBoundsException("Invalid group index " + idx);
		
		if(idx < orthography.numberOfGroups())
			orthography.removeGroup(idx);
		if(idx < ipaActual.numberOfGroups())
			ipaActual.removeGroup(idx);
		if(idx < ipaTarget.numberOfGroups())
			ipaTarget.removeGroup(idx);
		if(idx < alignment.numberOfGroups())
			alignment.removeGroup(idx);
		
		for(String tierName:getExtraTierNames()) {
			final Tier<?> tier = getTier(tierName);
			if(tier.isGrouped() && idx < tier.numberOfGroups()) {
				tier.removeGroup(idx);
			}
		}
	}

	@Override
	public Group addGroup() {
		int gidx = orthography.numberOfGroups();
		
		orthography.addGroup(new Orthography());
		ipaTarget.addGroup(new IPATranscript());
		ipaActual.addGroup(new IPATranscript());
		alignment.addGroup(new PhoneMap(ipaTarget.getGroup(gidx), ipaActual.getGroup(gidx)));
		
		for(String tierName:getExtraTierNames()) {
			final Tier<String> tier = getTier(tierName, String.class);
			if(tier.isGrouped())
				tier.addGroup();
			else if(tier.numberOfGroups() == 0)
				tier.addGroup("");
		}
		
		if(notes.numberOfGroups() == 0)
			notes.addGroup("");
		if(segment.numberOfGroups() == 0)
			segment.addGroup(SessionFactory.newFactory().createMediaSegment());
		
		return new GroupImpl(this, gidx);
	}

	@Override
	public Group addGroup(int idx) {
		if(orthography.numberOfGroups() < idx)
			while(orthography.numberOfGroups() < idx) {
				orthography.addGroup(new Orthography());
			}
		orthography.addGroup(idx, new Orthography());
		
		if(ipaTarget.numberOfGroups() < idx)
			while(ipaTarget.numberOfGroups() < idx) {
				ipaTarget.addGroup(new IPATranscript());
			}
		ipaTarget.addGroup(idx, new IPATranscript());
		
		if(ipaActual.numberOfGroups() < idx)
			while(ipaActual.numberOfGroups() < idx) {
				ipaActual.addGroup(new IPATranscript());
			}
		ipaActual.addGroup(idx, new IPATranscript());
		
		if(alignment.numberOfGroups() < idx)
			while(alignment.numberOfGroups() < idx) {
				alignment.addGroup(new PhoneMap(ipaTarget.getGroup(alignment.numberOfGroups()), ipaActual.getGroup(alignment.numberOfGroups())));
			}
		alignment.addGroup(idx, new PhoneMap(ipaTarget.getGroup(idx), ipaActual.getGroup(idx)));
		for(String tierName:getExtraTierNames()) {
			final Tier<String> tier = getTier(tierName, String.class);
			if(tier.isGrouped()) {
				if(tier.numberOfGroups() < idx)
					while(tier.numberOfGroups() < idx) {
						tier.addGroup("");
					}
				tier.addGroup(idx, "");
			} else if(tier.numberOfGroups() == 0)
				tier.addGroup();
		}
		
		return new GroupImpl(this, idx);
	}

	@Override
	public Tier<IPATranscript> getIPATarget() {
		return this.ipaTarget;
	}

	@Override
	public void setIPATarget(Tier<IPATranscript> ipa) {
		this.ipaTarget.removeAll();
		for(int i = 0; i < ipa.numberOfGroups(); i++) {
			this.ipaTarget.addGroup(ipa.getGroup(i));
		}
	}

	@Override
	public Tier<IPATranscript> getIPAActual() {
		return this.ipaActual;
	}

	@Override
	public void setIPAActual(Tier<IPATranscript> ipa) {
		this.ipaActual.removeAll();
		for(int i = 0; i < ipa.numberOfGroups(); i++) {
			this.ipaActual.addGroup(ipa.getGroup(i));
		}
	}
	
	@Override
	public Tier<PhoneMap> getPhoneAlignment() {
		return this.alignment;
	}
	
	@Override
	public void setPhoneAlignment(Tier<PhoneMap> phoneAlignment) {
		this.alignment.removeAll();
		for(int i = 0; i < phoneAlignment.numberOfGroups(); i++) {
			this.alignment.addGroup(phoneAlignment.getGroup(i));
		}
	}

	@Override
	public Tier<String> getNotes() {
		return this.notes;
	}

	@Override
	public void setNotes(Tier<String> notes) {
		this.notes.removeAll();
		if(notes.numberOfGroups() > 0) {
			this.notes.addGroup(notes.getGroup(0));
		}
	}

	@Override
	public Class<?> getTierType(String name) {
		if(SystemTierType.isSystemTier(name)) {
			return SystemTierType.tierFromString(name).getDeclaredType();
		} else {
			for(Tier<?> t:userDefined.values()) {
				if(t.getName().equals(name)) {
					return t.getDeclaredType();
				}
			}
		}
		return null;
	}

	@Override
	public boolean hasTier(String name) {
		return getExtraTierNames().contains(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Tier<T> getTier(String name, Class<T> type) {
		Tier<T> retVal = null;
		
		final SystemTierType systemTierType = SystemTierType.tierFromString(name);
		final Tier<T> systemTier = getSystemTier(systemTierType, type);
		
		if(systemTier != null) {
			retVal = systemTier;
		} else {
			final Tier<?> userTier = userDefined.get(name);
			if(userTier != null) {
				if(userTier.getDeclaredType() == type) {
					retVal = (Tier<T>)userTier;
				} else if(type == String.class) {
					// create a new string tier to return
					final SessionFactory factory = SessionFactory.newFactory();
					retVal = factory.createTier(name, type, userTier.isGrouped());
					
					final Formatter<Object> formatter = 
							(Formatter<Object>)FormatterFactory.createFormatter(userTier.getDeclaredType());
					
					// copy group data as string
					for(int i = 0; i < userTier.numberOfGroups(); i++) {
						final Object obj = userTier.getGroup(i);
						String val = (formatter != null ? formatter.format(obj) : obj.toString());
						
						if(obj instanceof IExtendable) {
							final UnvalidatedValue uv = ((IExtendable)obj).getExtension(UnvalidatedValue.class);
							if(uv != null) {
								val = uv.getValue();
							}
						}
						retVal.addGroup((T)val);
					}
				}
			}
		}
		
		return retVal;
	}
	
	@SuppressWarnings("unchecked")
	private <T> Tier<T> getSystemTier(SystemTierType systemTierType, Class<T> type) {
		Tier<T> retVal = null;
		
		Tier<?> systemTier = null;
		if(systemTierType != null) {
			switch(systemTierType) {
			case Orthography:
				systemTier = getOrthography();
				break;
				
			case IPATarget:
				systemTier = getIPATarget();
				break;
				
			case IPAActual:
				systemTier = getIPAActual();
				break;
				
			case SyllableAlignment:
				systemTier = getPhoneAlignment();
				break;
				
			case Segment:
				systemTier = getSegment();
				break;
				
			case Notes:
				systemTier = getNotes();
				break;
				
			default:
				break;	
			}
			if(systemTier != null) {
				if(systemTier.getDeclaredType() == type) {
					retVal = (Tier<T>)systemTier;
				} else if(type == String.class) {
					// create a new string tier to return
					final SessionFactory factory = SessionFactory.newFactory();
					retVal = factory.createTier(systemTier.getName(), type, systemTier.isGrouped());
					// copy group data as string
					for(int i = 0; i < systemTier.numberOfGroups(); i++) {
						final Object obj = systemTier.getGroup(i);
						String val = obj.toString();
						
						if(obj instanceof IExtendable) {
							final UnvalidatedValue uv = ((IExtendable)obj).getExtension(UnvalidatedValue.class);
							if(uv != null) {
								val = uv.getValue();
							}
						}
						retVal.addGroup((T)val);
					}
				}
			}
		}
		
		return retVal;
	}
	
	
	@Override
	public Tier<?> getTier(String name) {
		return getTier(name, getTierType(name));
	}

	@Override
	public Set<String> getExtraTierNames() {
		return userDefined.keySet();
	}

	@Override
	public void removeTier(String name) {
		userDefined.remove(name);
	}
	
	// COMMENTS
	private final List<Comment> comments = 
			Collections.synchronizedList(new ArrayList<Comment>());
	
	@Override
	public int getNumberOfComments() {
		return comments.size();
	}

	@Override
	public Comment getComment(int idx) {
		return comments.get(idx);
	}

	@Override
	public void addComment(Comment comment) {
		comments.add(comment);
	}

	@Override
	public void removeComment(Comment comment) {
		comments.remove(comment);
	}

	@Override
	public void removeComment(int idx) {
		comments.remove(idx);
	}
	
	/* Extension support */
	private ExtensionSupport extSupport = new ExtensionSupport(Record.class, this);
	@Override
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}

	@Override
	public void putTier(Tier<?> tier) {
		userDefined.put(tier.getName(), tier);
	}

	@Override
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
		
		// TODO alignment
		
		// other tiers
		for(String tierName:getExtraTierNames()) {
			if(getTier(tierName).isGrouped()) {
				final String tierVal = group1.getTier(tierName, String.class);
				if(tierVal != null) {
					final String newVal = new String(tierVal + " " + group2.getTier(tierName, String.class)).trim();
					group1.setTier(tierName, String.class, newVal);
				}
			}
		}
		
		removeGroup(grp2);
		
		return retVal;
	}
	
	@Override
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

		// TODO alignment
		
		// other tiers
		for(String tierName:getExtraTierNames()) {
			if(getTier(tierName).isGrouped()) {
				final String tierVal = group.getTier(tierName, String.class);
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
					group.setTier(tierName, String.class, val);
					newGroup.setTier(tierName, String.class, newVal);
				}
			}
		}
		
		return newGroup;
	}
}
