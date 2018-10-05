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
package ca.phon.syllabifier.basic;

import java.util.ArrayList;
import java.util.List;

import ca.phon.ipa.IPAElement;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;
import ca.phon.syllabifier.basic.io.ConstituentType;
import ca.phon.syllabifier.basic.io.MarkGroup;
import ca.phon.syllabifier.basic.io.StageType;
import ca.phon.syllable.SyllabificationInfo;
import ca.phon.syllable.SyllableConstituentType;

public class Stage implements SyllabifierStage {
	
	private final StageType stageType;
	
	private final List<PhonexPattern> compiledPatterns = new ArrayList<PhonexPattern>();
	
	public Stage(StageType stage) {
		super();
		this.stageType = stage;
	
		compile();
	}
	
	private void compile() {
		for(String phonex:stageType.getPhonex()) {
			final PhonexPattern pattern = PhonexPattern.compile(phonex);
			compiledPatterns.add(pattern);
		}
	}

	@Override
	public String getName() {
		return stageType.getName();
	}
	
	@Override
	public boolean run(List<IPAElement> phones) {
		boolean retVal = false;
		for(PhonexPattern pattern:compiledPatterns) {
			final PhonexMatcher matcher = pattern.matcher(phones);
			while(matcher.find()) {
				for(MarkGroup mg:stageType.getGroup()) {
					final SyllableConstituentType scType = scTypeFromXMLType(mg.getMark());
					int groupIdx = pattern.groupIndex(mg.getName());
					if(groupIdx > 0) {
						final List<IPAElement> grp = matcher.group(groupIdx);
						for(IPAElement ele:grp) {
							retVal = true;
							final SyllabificationInfo scInfo = ele.getExtension(SyllabificationInfo.class);
							scInfo.setConstituentType(scType);
							scInfo.setDiphthongMember(mg.getMark() == ConstituentType.DIPHTHONG);
						}
					}
				}
			}
		}
		return retVal;
	}
	
	private SyllableConstituentType scTypeFromXMLType(ConstituentType type) {
		SyllableConstituentType retVal = SyllableConstituentType.UNKNOWN;
		if(type == ConstituentType.AMBISYLLABIC) {
			retVal = SyllableConstituentType.AMBISYLLABIC;
		} else if(type == ConstituentType.CODA) {
			retVal = SyllableConstituentType.CODA;
		} else if(type == ConstituentType.LEFT_APPENDIX) {
			retVal = SyllableConstituentType.LEFTAPPENDIX;
		} else if(type == ConstituentType.NUCLEUS || type == ConstituentType.DIPHTHONG) {
			retVal = SyllableConstituentType.NUCLEUS;
		} else if(type == ConstituentType.OEHS) {
			retVal = SyllableConstituentType.OEHS;
		} else if(type == ConstituentType.ONSET) {
			retVal = SyllableConstituentType.ONSET;
		} else if(type == ConstituentType.RIGHT_APPENDIX) {
			retVal = SyllableConstituentType.RIGHTAPPENDIX;
		}
		return retVal;
	}

	@Override
	public boolean repeatWhileChanges() {
		return stageType.isContinueUntilFail();
	}
	
}
