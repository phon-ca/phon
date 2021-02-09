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
package ca.phon.phonex;

import ca.phon.ipa.parser.IPATokenType;
import ca.phon.ipa.parser.IPATokens;
import org.apache.logging.log4j.*;

import ca.phon.ipa.*;
import ca.phon.visitor.*;
import ca.phon.visitor.annotation.*;

import java.util.ArrayList;
import java.util.List;

public class ReplaceExpressionVisitor extends VisitorAdapter<IPAElement> {
	
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(ReplaceExpressionVisitor.class.getName());

	private final PhonexMatcher matcher;
	
	private final IPATranscriptBuilder builder;
	
	public ReplaceExpressionVisitor(PhonexMatcher matcher) {
		super();
		this.matcher = matcher;
		this.builder = new IPATranscriptBuilder();
	}
	
	public IPATranscript getTranscript() {
		return builder.toIPATranscript();
	}

	@Override
	public void fallbackVisit(IPAElement obj) {
		builder.append(obj);
	}
	
	@Visits
	public void visitPhonexMatcherReference(PhonexMatcherReference pmr) {
		int groupIndex = pmr.getGroupIndex();
		if(groupIndex < 0) {
			final String groupName = pmr.getGroupName();
			if(groupName == null) {
				LOGGER.error("Unknown phonex matcher reference " + pmr.getText());
				return;
			}
			groupIndex = matcher.pattern().groupIndex(groupName);
		}

		IPATranscript grpValue = new IPATranscript(matcher.group(groupIndex));

		// check for tone swapping
		List<Diacritic> newToneNumber = new ArrayList<>();
		for(Diacritic dia:pmr.getSuffixDiacritics()) {
			if(IPATokens.getSharedInstance().getTokenType(dia.getCharacter()) == IPATokenType.TONE_NUMBER) {
				newToneNumber.add(dia);
			}
		}

		List<Diacritic> oldToneNumber = new ArrayList<>();
		if(grpValue.length() > 0) {
			IPAElement lastEle = grpValue.elementAt(grpValue.length()-1);
			if(lastEle instanceof Phone) {
				for (Diacritic dia : ((Phone)lastEle).getSuffixDiacritics()) {
					if (IPATokens.getSharedInstance().getTokenType(dia.getCharacter()) == IPATokenType.TONE_NUMBER) {
						oldToneNumber.add(dia);
					}
				}
				// swapping tones, remove current tone
				if (newToneNumber.size() > 0 && oldToneNumber.size() > 0)
					grpValue = grpValue.stripDiacritics(oldToneNumber);
			}
		}

		builder.append(pmr.getPrefix());
		builder.append(grpValue);
		builder.append(pmr.getCombining());
		builder.append(pmr.getSuffix());

	}
	
}
