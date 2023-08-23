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
import ca.phon.session.spi.TierDescriptionSPI;
import ca.phon.session.usertier.UserTierData;

import java.util.List;
import java.util.Map;

/**
 * Description of a tier including name, type, parameters and alignment rules.
 *
 */
public final class TierDescription extends ExtendableObject {
	
	private TierDescriptionSPI tierDescriptionImpl;
	
	TierDescription(TierDescriptionSPI impl) {
		super();
		this.tierDescriptionImpl = impl;
	}

	public String getName() {
		return tierDescriptionImpl.getName();
	}

	public Class<?> getDeclaredType() {
		return tierDescriptionImpl.getDeclaredType();
	}

	/**
	 * Parameters for the tier description. Parameters are defined by individual tier
	 * types.  For example, the IPA tiers include an optional parameter for syllabifier
	 * language.
	 *
	 * @return map of tier parameters
	 */
	public Map<String, String> getTierParameters() {
		return tierDescriptionImpl.getTierParameters();
	}

	/**
	 * Should this tier be excluded from cross tier alignment
	 *
	 * @return true if tier is not part of cross tier alignment
	 */
	public boolean isExcludeFromAlignment() {
		return tierDescriptionImpl.isExcludeFromAlignment();
	}

	/**
	 * Is this tier a 'blind' tier or included in blind transcription.  Blind tiers
	 * are invisible for different transcribers (if blind mode is active) and may
	 * be validated when not in blind mode.
	 *
	 * @return true if tier is included in blind transcription
	 */
	public boolean isBlind() { return tierDescriptionImpl.isBlind(); }

	/**
	 * Does this tier have subtype information.  This means either subtypeDelim
	 * or subtypeExpr have been specified.
	 *
	 * @return true if subtype delimiters or regular expression is available and
	 * the declared type of the tier is {@link ca.phon.session.usertier.UserTierData}
	 */
	public boolean hasSubtypeInformation() {
		return getDeclaredType() == UserTierData.class &&
				(hasSubtypeDelim() || hasSubtypeExpr());
	}

	/**
	 * Return true if this tier include subtype delimiters
	 *
	 * @return true if the number of subtype delimiters > 0
	 */
	public boolean hasSubtypeDelim() {
		return getSubtypeDelim() != null && !getSubtypeDelim().isEmpty();
	}

	/**
	 * Return true if a subtype expression has been specified.
	 *
	 * @return true if this tier specified a subtype expression
	 */
	public boolean hasSubtypeExpr() {
		return getSubtypeExpr() != null && !getSubtypeExpr().isEmpty();
	}

	/**
	 * Get subtype delimiters (if any)
	 *
	 * @return list of subtype delimiters
	 */
	public List<String> getSubtypeDelim() {
		return tierDescriptionImpl.getSubtypeDelim();
	}

	/**
	 * Get subtype regluar expression. Groups in the expression will be used to
	 * identify part of the expression to use
	 *
	 * @return subtype expr or null if not set
	 */
	public String getSubtypeExpr() {
		return tierDescriptionImpl.getSubtypeExpr();
	}
	
}
