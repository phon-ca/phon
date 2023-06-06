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

import ca.phon.extensions.*;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.*;
import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;
import ca.phon.session.*;
import ca.phon.session.spi.RecordSPI;

import java.util.*;

/**
 * Basic record implementation
 *
 */
public class RecordImpl implements RecordSPI {

	/* Attributes */
	private UUID uuid = UUID.randomUUID();

	private Participant participant = Participant.UNKNOWN;

	private volatile boolean excludeFromSearches = false;

	/* default tiers */
	private final Tier<Orthography> orthography;

	private final Tier<IPATranscript> ipaTarget;

	private final Tier<IPATranscript> ipaActual;

	private final Tier<MediaSegment> segmentTier;

	private final Tier<UserTierData> notes;

	private final Tier<PhoneAlignment> alignment;

	/* Additional tiers */
	private final Map<String, Tier<?>> userDefined;

	RecordImpl() {
		super();
		final SessionFactory factory = SessionFactory.newFactory();
		orthography = factory.createTier(SystemTierType.Orthography.getName(), Orthography.class);
		ipaTarget = factory.createTier(SystemTierType.IPATarget.getName(), IPATranscript.class);
		ipaActual = factory.createTier(SystemTierType.IPAActual.getName(), IPATranscript.class);
		segmentTier = factory.createTier(SystemTierType.Segment.getName(), MediaSegment.class);
		notes = factory.createTier(SystemTierType.Notes.getName(), UserTierData.class);
		alignment = factory.createTier(SystemTierType.SyllableAlignment.getName(), PhoneAlignment.class);
		userDefined = Collections.synchronizedMap(new HashMap<>());
	}

	@Override
	public UUID getUuid() {
		return this.uuid;
	}

	@Override
	public void setUuid(UUID id) {
		this.uuid = id;
	}

	@Override
	public Participant getSpeaker() {
		return this.participant;
	}

	@Override
	public void setSpeaker(Participant participant) {
		this.participant = participant;
	}

	@Override
	public Tier<MediaSegment> getSegmentTier() {
		return this.segmentTier;
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
	public Tier<Orthography> getOrthographyTier() {
		return this.orthography;
	}

	@Override
	public Tier<IPATranscript> getIPATargetTier() {
		return this.ipaTarget;
	}

	@Override
	public Tier<IPATranscript> getIPAActualTier() {
		return this.ipaActual;
	}

	@Override
	public Tier<PhoneAlignment> getPhoneAlignmentTier() {
		return this.alignment;
	}

	@Override
	public Tier<UserTierData> getNotesTier() {
		return this.notes;
	}

	@Override
	public boolean hasTier(String name) {
		return (SystemTierType.tierFromString(name) != null || getUserDefinedTierNames().contains(name));
	}

	@Override
	public <T> Tier<T> getTier(String name, Class<T> type) {
		final SystemTierType systemTierType = SystemTierType.tierFromString(name);
		final Tier<T> systemTier = getSystemTier(systemTierType, type);
		if(systemTier != null) {
			return systemTier;
		} else {
			final Tier<?> userTier = userDefined.get(name);
			if(userTier != null) {
				if(userTier.getDeclaredType() == type) {
					return (Tier<T>)userTier;
				} else if(type == String.class) {
					// create a new string tier to return
					final SessionFactory factory = SessionFactory.newFactory();
					Tier<T> retVal = factory.createTier(name, type);
					final Formatter<Object> formatter =
							(Formatter<Object>)FormatterFactory.createFormatter(userTier.getDeclaredType());
					final Object obj = userTier.getValue();
					String val = (formatter != null ? formatter.format(obj) : obj.toString());
					if(obj instanceof IExtendable) {
						final UnvalidatedValue uv = ((IExtendable)obj).getExtension(UnvalidatedValue.class);
						if(uv != null) {
							val = uv.getValue();
						}
					}
					retVal.setValue((T)val);
					return retVal;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private <T> Tier<T> getSystemTier(SystemTierType systemTierType, Class<T> type) {
		Tier<T> retVal = null;
		Tier<?> systemTier = null;
		if(systemTierType != null) {
			switch(systemTierType) {
			case Orthography:
				systemTier = getOrthographyTier();
				break;

			case IPATarget:
				systemTier = getIPATargetTier();
				break;

			case IPAActual:
				systemTier = getIPAActualTier();
				break;

			case SyllableAlignment:
				systemTier = getPhoneAlignmentTier();
				break;

			case Segment:
				systemTier = getSegmentTier();
				break;

			case Notes:
				systemTier = getNotesTier();
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
					retVal = factory.createTier(systemTier.getName(), type);
					final Object obj = systemTier.getValue();
					Object val = obj.toString();
					if(obj instanceof IExtendable) {
						final UnvalidatedValue uv = ((IExtendable)obj).getExtension(UnvalidatedValue.class);
						if(uv != null) {
							val = uv.getValue();
						}
					}

					retVal.setValue((T)val);
				}
			}
		}
		return retVal;
	}

	@Override
	public Set<String> getUserDefinedTierNames() {
		return userDefined.keySet();
	}

	@Override
	public void removeTier(String name) {
		userDefined.remove(name);
	}

	@Override
	public void putTier(Tier<?> tier) {
		userDefined.put(tier.getName(), tier);
	}

}
