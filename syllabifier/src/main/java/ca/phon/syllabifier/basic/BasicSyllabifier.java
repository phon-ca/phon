/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.syllabifier.basic;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ca.phon.extensions.Extension;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.basic.io.StageType;
import ca.phon.syllabifier.basic.io.SyllabifierDef;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.util.Language;

/**
 * 
 * Class to perform syllabification based on a
 * syllabifier definition.
 * 
 *
 */
public class BasicSyllabifier implements Syllabifier {
	
	private final SyllabifierDef def;
	
	/** Constructor */
	BasicSyllabifier(SyllabifierDef def) {
		super();
		
		this.def = def;
	}
	
	@Extension(IPAElement.class)
	public static class SyllabifierStageResults {
		final Map<String, IPATranscript> stages = new TreeMap<String, IPATranscript>();
	}

	@Override
	public String getName() {
		return def.getName();
	}

	@Override
	public Language getLanguage() {
		return Language.fromString(def.getLanguage());
	}

	@Override
	public void syllabify(List<IPAElement> phones) {
		final SonorityScale scale = new SonorityScale(def.getSonorityScale());
		for(StageType stage:def.getStage()) {
			Stage currentStage = new Stage(stage);
			
			if(stage.isContinueUntilFail()) {
				boolean hasChanged = true;
				while(hasChanged) {
					for(IPAElement ele:phones) {
						LastScType lastType = ele.getExtension(LastScType.class);
						if(lastType == null) {
							lastType = new LastScType();
							ele.putExtension(LastScType.class, lastType);
						}
						lastType.lastType = ele.getScType();
					}
					currentStage.run(phones, scale);
					hasChanged = false;
					for(IPAElement ele:phones) {
						LastScType lastType = ele.getExtension(LastScType.class);
						if(lastType.lastType != ele.getScType()) {
							hasChanged = true;
							break;
						}
					}
				}
			} else {
				currentStage.run(phones, scale);
			}
		}

		for(IPAElement p:phones) {
			if(p.getFeatureSet().hasFeature("Untranscribed")) {
				p.setScType(SyllableConstituentType.UNKNOWN);
			}
		}
	}

	@Extension(IPAElement.class)
	private class LastScType {
		SyllableConstituentType lastType = SyllableConstituentType.UNKNOWN;
	}

}
