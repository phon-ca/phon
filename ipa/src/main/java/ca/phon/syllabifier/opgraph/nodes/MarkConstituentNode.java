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
package ca.phon.syllabifier.opgraph.nodes;

import java.awt.*;
import java.util.*;
import java.util.List;

import ca.phon.ipa.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.opgraph.app.extensions.*;
import ca.phon.opgraph.exceptions.*;
import ca.phon.opgraph.nodes.phonex.*;
import ca.phon.phonex.*;
import ca.phon.syllable.*;

@OpNodeInfo(
		name="Mark Constituent Type",
		description="Use phonex expressions to mark constituent type for phones. Use group names to determine syllabification information.",
		category="Syllabifier"
)
public class MarkConstituentNode extends OpNode implements PhonexNode {

	// input field
	private final static InputField ipaInput = 
			new InputField("ipa", "ipa input", IPATranscript.class);
	// pass-through output
	private final static OutputField ipaOut =
			new OutputField("ipa out", "pass-through ipa ouptput", true, IPATranscript.class);
	
	/**
	 * Compiled phonex pattern
	 */
	private PhonexPattern pattern;
	
	public MarkConstituentNode() {
		super();
		
		putField(ipaInput);
		putField(ipaOut);
		
		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		if(pattern == null)
			throw new ProcessingException(null, "No pattern");
		final IPATranscript ipa = (IPATranscript)context.get(ipaInput);
		
		final PhonexMatcher matcher = pattern.matcher(ipa);
		while(matcher.find()) {
			for(int i = 1; i <= matcher.groupCount(); i++) {
				final String grpName = pattern.groupName(i);
				if(grpName != null) {
					final SyllableConstituentType scType = 
							(SyllableConstituentType.fromString(grpName) != null ? SyllableConstituentType.fromString(grpName) : SyllableConstituentType.UNKNOWN);
					final List<IPAElement> grp = matcher.group(i);
					grp.forEach( (e) -> e.setScType(scType) );
					
					if(grpName.equalsIgnoreCase("D")) {
						for(int eleIdx = 0; eleIdx < grp.size(); eleIdx++) {
							grp.get(eleIdx).getExtension(SyllabificationInfo.class).setDiphthongMember(true);
						}
					}
				}
			}
		}
		
		context.put(ipaOut, ipa);
	}

	@Override
	public String getPhonex() {
		String retVal = "";
		if(pattern != null)
			retVal = pattern.pattern();
		return retVal;
	}

	private PhonexSettingsPanel settingsPanel;
	@Override
	public Component getComponent(GraphDocument arg0) {
		if(settingsPanel == null) {
			settingsPanel = new PhonexSettingsPanel(arg0, this);
		}
		return settingsPanel;
	}
	
	private final String PHONEX_KEY = 
			getClass().getName() + ".phonex";

	@Override
	public Properties getSettings() {
		final Properties retVal = new Properties();
		if(pattern != null) {
			retVal.setProperty(PHONEX_KEY, pattern.pattern());
		}
		return retVal;
	}

	@Override
	public void loadSettings(Properties arg0) {
		if(arg0.containsKey(PHONEX_KEY)) {
			setPhonex(arg0.getProperty(PHONEX_KEY));
		}
	}

	@Override
	public void setPhonex(String phonex) throws PhonexPatternException {
		pattern = PhonexPattern.compile(phonex);
		
	}
}
