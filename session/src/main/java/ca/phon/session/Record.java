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

import ca.phon.extensions.ExtendableObject;
import ca.phon.ipa.*;
import ca.phon.orthography.*;
import ca.phon.orthography.Error;
import ca.phon.orthography.mor.MorTierData;
import ca.phon.session.spi.RecordSPI;
import ca.phon.session.tierdata.TierData;
import ca.phon.util.Language;

import java.util.*;

/**
 * Record for phon {@link Session}s composed of a number of tiers. By
 * default, every record has the following tier:
 *
 * <ul>
 *     <li>Orthography - orthography transcription of utterance in the CHAT transcription format</li>
 *     <li>IPA Target - model transcription of words in the utterance</li>
 *     <li>IPA Actual - actual transcription of words</li>
 *     <li>Segment - media segment time for record</li>
 * </ul>
 *
 * As of Phon 4.0 the Notes tiers has been deprecated, use {@link UserTierType#Comments} instead.
 *
 * Additional tiers, called user-defined tiers, may also be added to the record.
 */
public final class Record extends ExtendableObject {

	public final static String RECORD_XCL_POSTCODE = "xcl";
	
	private final RecordSPI recordImpl;

	// uuid should no longer be used
	private UUID uuid;
	
	Record(RecordSPI impl) {
		super();
		this.recordImpl = impl;
	}

	/**
	 * Unique id for record
	 *
	 * @return id
	 * @deprecated
	 */
	@Deprecated
	public UUID getUuid() {
		if(this.uuid == null) {
			this.uuid = UUID.randomUUID();
		}
		return this.uuid;
	}

	/**
	 *
	 * @param id
	 * @deprecated
	 */
	@Deprecated
	public void setUuid(UUID id) {
		this.uuid = id;
	}

	/**
	 * Speaker for record, default {@link Participant#UNKNOWN}
	 *
	 * @return speaker
	 */
	public Participant getSpeaker() {
		return recordImpl.getSpeaker();
	}

	public void setSpeaker(Participant participant) {
		recordImpl.setSpeaker(participant);
	}

	/**
	 * Language for record (if specified).  Record language is written in orthography
	 * as '[- lang]' at the beginning of the utterance.
	 *
	 * @return language or null not specified
	 */
	public Language getLanguage() {
		final Orthography ortho = getOrthographyTier().getValue();
		if(ortho.length() > 0 && ortho.elementAt(0) instanceof UtteranceLanguage uttLang) {
			return uttLang.getLanguage();
		} else {
			return null;
		}
	}

	public void setLanguage(Language language) {
		// update orthography with UtteranceLanguage annotation
		final UtteranceLanguage utteranceLanguage = new UtteranceLanguage(language);
		final Orthography currentOrtho = getOrthography();
		int startIdx = 0;
		if(currentOrtho.length() > 0 && currentOrtho.elementAt(0) instanceof UtteranceLanguage) {
			++startIdx;
		}
		final OrthographyBuilder builder = new OrthographyBuilder();
		builder.append(utteranceLanguage);
		int i = 0;
		for(OrthographyElement ele:currentOrtho) {
			if(i++ >= startIdx) {
				builder.append(ele);
			}
		}
		setOrthography(builder.toOrthography());
	}

	/**
	 * Get media segment for record
	 *
	 * @return media segment
	 */
	public MediaSegment getMediaSegment() { return getSegmentTier().getValue(); }

	/**
	 * Set media segment for record
	 *
	 * @param segment
	 */
	public void setMediaSegment(MediaSegment segment) {
		getSegmentTier().setValue(segment);
	}

	/**
	 * Get media segment tier
	 *
	 * @return segment tier
	 */
	public Tier<MediaSegment> getSegmentTier() {
		return recordImpl.getSegmentTier();
	}

	/**
	 * Should we exclude this record from searches?  This will be true if utterance-level annotations in
	 * the Orthography tier include <pre>[e]</pre> (exclude) or <pre>[* error]</pre> (error).
	 *
	 * @return true if record should be excluded
	 */
	public boolean isExcludeFromSearches() {
		final Orthography orthography = getOrthography();
		boolean exclude = false;
		for(OrthographyElement orthographyElement:orthography) {
			if(orthographyElement instanceof Postcode postcode) {
				exclude = postcode.getCode().equals(Record.RECORD_XCL_POSTCODE);
			}
		}
		return exclude;
	}

	/**
	 * Get orthographic transcription for record. Utterances are written in the CHAT tanscription
	 * format.
	 *
	 * @return utterance
	 */
	public Orthography getOrthography() {
		return getOrthographyTier().getValue();
	}

	public void setOrthography(Orthography orthography) {
		getOrthographyTier().setValue(orthography);
	}

	/**
	 * Get orthography tier
	 *
	 * @return orthography tier
	 */
	public Tier<Orthography> getOrthographyTier() {
		return recordImpl.getOrthographyTier();
	}

	/**
	 * Get validated value of IPA Target tier
	 *
	 * @return ipa target
	 */
	public IPATranscript getIPATarget() {
		return getIPATargetTier().getValue();
	}

	/**
	 * Set validated value of IPA Target tier
	 *
	 * @param ipa
	 */
	public void setIPATarget(IPATranscript ipa) {
		getIPATargetTier().setValue(ipa);
	}

	/**
	 * Get IPA Target tier
	 *
	 * @return ipa target tier
	 */
	public Tier<IPATranscript> getIPATargetTier() {
		return recordImpl.getIPATargetTier();
	}

	/**
	 * Get validated value of IPA Actual tier
	 *
	 * @return ipa target
	 */
	public IPATranscript getIPAActual() {
		return getIPAActualTier().getValue();
	}

	/**
	 * Set validated value of IPA Actual tier
	 *
	 * @param ipa
	 */
	public void setIPAActual(IPATranscript ipa) {
		getIPAActualTier().setValue(ipa);
	}

	/**
	 * Get IPA Actual tier
	 *
	 * @return IPA Actual tier
	 */
	public Tier<IPATranscript> getIPAActualTier() {
		return recordImpl.getIPAActualTier();
	}

	/**
	 * Get validated phone alignment between IPA Target and IPA Actual
	 *
	 * @return phone alignment
	 */
	public PhoneAlignment getPhoneAlignment() {
		return getPhoneAlignmentTier().getValue();
	}

	/**
	 * Set validated phone alignment between IPA Target and IPA Actual
	 *
	 * @param phoneAlignment
	 */
	public void setPhoneAlignment(PhoneAlignment phoneAlignment) {
		getPhoneAlignmentTier().setValue(phoneAlignment);
	}

	/**
	 * Get phone alignment tier
	 *
	 * @return phone alignment tier
	 */
	public Tier<PhoneAlignment> getPhoneAlignmentTier() {
		return recordImpl.getPhoneAlignmentTier();
	}

	/**
	 * Set validated value of Notes tier
	 *
	 * @return notes value
	 * @deprecated since Phon 4.0, use 'Comments' tier instead
	 */
	@Deprecated
	public TierData getNotes() {
		return getNotesTier().getValue();
	}

	/**
	 * Get validated value of Notes tier
	 *
	 * @param tierData
	 * @deprecated since Phon 4.0, use 'Comments' tier instead
	 */
	@Deprecated
	public void setNotes(TierData tierData) {
		getNotesTier().setValue(tierData);
	}


	/**
	 * Get Notes tier
	 *
	 * @return notes tier
	 * @deprecated since Phon 4.0, use 'Comments' tier instead
	 */
	@Deprecated
	public Tier<TierData> getNotesTier() {
		return recordImpl.getNotesTier();
	}

	/**
	 * Get an unmodifiable list of all user tiers
	 *
	 * @return list of all user-defined tiers in record
	 */
	public List<Tier<?>> getUserTiers() {
		return recordImpl.getUserTiers();
	}

	/**
	 * Get the register type of the given tier.
	 *
	 * @param name
	 * @return the tier type
	 */
	public Class<?> getTierType(String name) {
		if(SystemTierType.isSystemTier(name)) {
			return SystemTierType.tierFromString(name).getDeclaredType();
		} else {
			final Optional<Tier<?>> userTierOpt = getUserTiers().stream().filter(t -> t.getName().equals(name)).findAny();
			if (userTierOpt.isPresent()) {
				return userTierOpt.get().getDeclaredType();
			}
		}
		return null;
	}

	/**
	 * Get the given tier with the expected type.  Optional will
	 * be empty if tier does not exist in the record or the
	 * incorrect type was specified.
	 *
	 * @param name
	 * @param type
	 *
	 * @return optional tier
	 */
	public <T> Tier<T> getTier(String name, Class<T> type) {
		return recordImpl.getTier(name, type);
	}

	/**
	 * Get the given tier (type unspecified)
	 *
	 * @return name
	 */
	public Tier<?> getTier(String name) {
		return getTier(name, getTierType(name));
	}

	/**
	 * Return a list of user-defined tiers that are present
	 * in this record.
	 *
	 * @return the list of tier user-defined tier names
	 *  present in this record
	 */
	public Set<String> getUserDefinedTierNames() {
		return recordImpl.getUserDefinedTierNames();
	}

	/**
	 * Return a list of all present tiers which have the given
	 * type.
	 *
	 * @param type
	 * @return list of tiers
	 */
	public <T> List<Tier<T>> getTiersOfType(Class<T> type) {
		List<Tier<T>> retVal = new ArrayList<>();
		if(type == Orthography.class) {
			retVal.add((Tier<T>) getOrthographyTier());
		} else if(type == IPATranscript.class) {
			retVal.add((Tier<T>) getIPATargetTier());
			retVal.add((Tier<T>) getIPAActualTier());
		} else if(type == MediaSegment.class) {
			retVal.add((Tier<T>) getSegmentTier());
		} else if(type == TierData.class) {
			retVal.add((Tier<T>) getNotesTier());
		} else if(type == PhoneAlignment.class) {
			retVal.add((Tier<T>) getPhoneAlignmentTier());
		}
		for(String tierName:getUserDefinedTierNames()) {
			Tier<?> userTier = getTier(tierName);
			if(userTier.getDeclaredType() == type) {
				retVal.add((Tier<T>)userTier);
			}
		}
		return retVal;
	}

	/**
	 * @param name
	 * @return <code>true</code> if this record contains
	 *  the specified tier
	 */
	public boolean hasTier(String name) {
		return recordImpl.hasTier(name);
	}

	/**
	 * Set value for tier with name and type.  No effect
	 * if the specified tier was not found.
	 *
	 * @param name
	 * @param type
	 * @param value
	 *
	 * @param <T>
	 */
	public <T> void setTierValue(String name, Class<T> type, T value) {
		final Tier<T> tier = getTier(name, type);
		if(tier != null) {
			tier.setValue(value);
		}
	}

	/**
	 * Get value for tier with type unspecified
	 *
	 * @param tierName
	 * @return tier value if present, null otherwise
	 */
	public Object getTierValue(String tierName) {
		final Tier<?> tier = getTier(tierName);
		if(tier != null && tier.hasValue()) {
			return tier.getValue();
		}
		return null;
	}

	/**
	 * Get value for tier with name and type.  No effect
	 * if the specified tier was not found.
	 *
	 * @param tierName
	 * @param type
	 *
	 * @return value for tier or null if specified tier was not found
	 * @param <T>
	 */
	public <T> T getTierValue(String tierName, Class<T> type) {
		final Tier<T> tier = getTier(tierName, type);
		if(tier != null) {
			return tier.getValue();
		} else {
			return null;
		}
	}
	
	/**
	 * Add/set the given tier to the list of user defined
	 * tiers.
	 *
	 * @param tier
	 */
	public void putTier(Tier<?> tier) {
		recordImpl.putTier(tier);
	}

	/**
	 * Remove user-defined tier from record
	 *
	 * @param name
	 */
	public void removeTier(String name) {
		recordImpl.removeTier(name);
	}

}
