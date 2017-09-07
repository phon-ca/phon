/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.session.check;

import java.util.*;

import ca.phon.extensions.UnvalidatedValue;
import ca.phon.ipa.IPATranscript;
import ca.phon.plugin.*;
import ca.phon.session.*;

/**
 * Check IPA transcriptions for a session. 
 *
 */
@PhonPlugin(name="check", version="1", minPhonVersion="2.1.0")
public class CheckTranscripts implements SessionCheck, IPluginExtensionPoint<SessionCheck> {

	@Override
	public void checkSession(SessionValidator validator, 
			Session session, Map<String, Object> options) {
		
		for(int i = 0; i < session.getRecordCount(); i++) {
			final Record r = session.getRecord(i);
			
			List<String> ipaTiers = new ArrayList<>();
			ipaTiers.add(SystemTierType.IPATarget.getName());
			ipaTiers.add(SystemTierType.IPAActual.getName());
			for(String depTier:r.getExtraTierNames()) {
				if(r.getTierType(depTier) == IPATranscript.class) {
					ipaTiers.add(depTier);
				}
			}
			
			for(String ipaTier:ipaTiers) {
				final Tier<IPATranscript> tier = r.getTier(ipaTier, IPATranscript.class);
				for(int gIdx = 0; gIdx < tier.numberOfGroups(); gIdx++) {
					final IPATranscript ipa = tier.getGroup(gIdx);
					
					final UnvalidatedValue uv = ipa.getExtension(UnvalidatedValue.class);
					if(uv != null) {
						// error in this transcription
						final ValidationEvent ve = new ValidationEvent(session, i, ipaTier, gIdx,
								uv.getParseError().getMessage());
						validator.fireValidationEvent(ve);
					}
					
				}
			}
		}
		
	}

	@Override
	public Class<?> getExtensionType() {
		return SessionCheck.class;
	}

	@Override
	public IPluginExtensionFactory<SessionCheck> getFactory() {
		final IPluginExtensionFactory<SessionCheck> factory = (Object ... args) -> {
			return CheckTranscripts.this;
		};
		return factory;
	}

}
