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

import java.util.*;
import java.util.concurrent.atomic.*;

import ca.phon.extensions.*;
import ca.phon.formatter.*;
import ca.phon.formatter.Formatter;
import ca.phon.ipa.*;
import ca.phon.ipa.alignment.*;
import ca.phon.orthography.*;
import ca.phon.session.*;
import ca.phon.session.spi.*;

/**
 * Basic record implementation
 *
 */
public class RecordImpl implements RecordSPI {

	/* Attributes */
	private final AtomicReference<Participant> participantRef = new AtomicReference<Participant>();

	private volatile boolean excludeFromSearches = false;

	private final AtomicReference<UUID> uuidRef = new AtomicReference<UUID>(UUID.randomUUID());

	/* default tiers */
	private final Tier<Orthography> orthography;

	private final Tier<IPATranscript> ipaTarget;

	private final Tier<IPATranscript> ipaActual;

	private final Tier<MediaSegment> segment;

	private final Tier<TierString> notes;

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
		notes = factory.createTier(SystemTierType.Notes.getName(), TierString.class, SystemTierType.Notes.isGrouped());
		alignment = factory.createTier(SystemTierType.SyllableAlignment.getName(), PhoneMap.class, SystemTierType.SyllableAlignment.isGrouped());

		userDefined =
				Collections.synchronizedMap(new HashMap<String, Tier<?>>());
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
		participantRef.getAndSet(
				(participant != null ? participant : Participant.UNKNOWN)
				);
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
	public Tier<TierString> getNotes() {
		return this.notes;
	}

	@Override
	public void setNotes(Tier<TierString> notes) {
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
		return (SystemTierType.tierFromString(name) != null || getExtraTierNames().contains(name));
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
				} else if(type == String.class || type == TierString.class) {
					// create a new string tier to return
					final SessionFactory factory = SessionFactory.newFactory();
					retVal = factory.createTier(name, type, userTier.isGrouped());

					final Formatter<Object> formatter =
							(Formatter<Object>)FormatterFactory.createFormatter(userTier.getDeclaredType());

					// copy group data as string
					for(int i = 0; i < userTier.numberOfGroups(); i++) {
						final Object obj = userTier.getGroup(i);
						String val = (formatter != null ? formatter.format(obj) : obj.toString());
						Object tierVal = (type == TierString.class ? new TierString(val) : val);

						if(obj instanceof IExtendable) {
							final UnvalidatedValue uv = ((IExtendable)obj).getExtension(UnvalidatedValue.class);
							if(uv != null) {
								tierVal = (type == TierString.class ? new TierString(uv.getValue()) : uv.getValue());
							}
						}
						retVal.addGroup((T)tierVal);
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
				} else if(type == String.class || type == TierString.class) {
					// create a new string tier to return
					final SessionFactory factory = SessionFactory.newFactory();
					retVal = factory.createTier(systemTier.getName(), type, systemTier.isGrouped());
					// copy group data as string
					for(int i = 0; i < systemTier.numberOfGroups(); i++) {
						final Object obj = systemTier.getGroup(i);
						Object val = (type == TierString.class ? new TierString(obj.toString()) : obj.toString());

						if(obj instanceof IExtendable) {
							final UnvalidatedValue uv = ((IExtendable)obj).getExtension(UnvalidatedValue.class);
							if(uv != null) {
								val = (type == TierString.class ? new TierString(uv.getValue()) : uv.getValue());
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

	@Override
	public void putTier(Tier<?> tier) {
		userDefined.put(tier.getName(), tier);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<Tier<T>> getTiersOfType(Class<T> type) {
		List<Tier<T>> retVal = new ArrayList<>();
		
		if(type == Orthography.class) {
			retVal.add((Tier<T>)getOrthography());
		} else if(type == IPATranscript.class) {
			retVal.add((Tier<T>)getIPATarget());
			retVal.add((Tier<T>)getIPAActual());
		} else if(type == MediaSegment.class) {
			retVal.add((Tier<T>)getSegment());
		} else if(type == TierString.class) {
			retVal.add((Tier<T>)getNotes());
		} else if(type == PhoneMap.class) {
			retVal.add((Tier<T>)getPhoneAlignment());
		}
		
		for(String tierName:userDefined.keySet()) {
			Tier<?> userTier = userDefined.get(tierName);
			if(userTier.getDeclaredType() == type) {
				retVal.add((Tier<T>)userTier);
			}
		}
		
		return retVal;
	}
}
