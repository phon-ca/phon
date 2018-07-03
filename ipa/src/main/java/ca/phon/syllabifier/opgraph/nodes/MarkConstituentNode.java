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
package ca.phon.syllabifier.opgraph.nodes;

import java.awt.Component;
import java.util.*;

import ca.phon.ipa.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
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
						grp.forEach( (e) -> {
							e.getExtension(SyllabificationInfo.class).setDiphthongMember(true);
						});
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
