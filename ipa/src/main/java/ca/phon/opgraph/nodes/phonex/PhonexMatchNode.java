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
package ca.phon.opgraph.nodes.phonex;

import java.awt.*;
import java.util.*;

import ca.phon.ipa.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.opgraph.app.extensions.*;
import ca.phon.opgraph.exceptions.*;
import ca.phon.phonex.*;

@OpNodeInfo(
		name="Phonex Matcher",
		description="Accepts a phonex string as a setting and an ipa transcript " +
				"as an input.  Ouputs are dynamic based on the number of groups in the phonex.  " +
				"A boolean output called 'matches' and group 0 are always available.",
		category="Phonex"
)
public class PhonexMatchNode extends OpNode implements PhonexNode {
	// input field
	private final static InputField ipaInput = 
			new InputField("ipa", "ipa input", IPATranscript.class);
	
	// 'fixed' output fields
	private final static OutputField matchesOut = 
			new OutputField("matches", "true if ipa matches pattern", true, Boolean.class);
	private final static OutputField g0Out = 
			new OutputField("g0", "Content of group 0.  Valid only if matches is true", true, IPATranscript.class);
	
	/**
	 * Compiled phonex pattern
	 */
	private PhonexPattern pattern;
	
	public PhonexMatchNode() {
		super();
		
		super.putField(ipaInput);
		super.putField(matchesOut);
		super.putField(g0Out);
		
		super.putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext arg0) throws ProcessingException {
		// get input 
		IPATranscript transcript = 
				IPATranscript.class.cast(arg0.get(ipaInput));
		
		// create a new matcher
		PhonexMatcher matcher = pattern.matcher(transcript);
		if(matcher.matches()) {
			// set matches output
			arg0.put(matchesOut, Boolean.TRUE);
			
			arg0.put(g0Out, new IPATranscript(matcher.group(0)));
			
			for(int gIdx = 1; gIdx <= pattern.numberOfGroups(); gIdx++) {
				String outputName = 
						(pattern.groupName(gIdx) != null ? pattern.groupName(gIdx) : "g" + gIdx);
				arg0.put(outputName, new IPATranscript(matcher.group(gIdx)));
			}
		}
	}
	
	/**
	 * Set (and compile) phonex expression.
	 * 
	 * @param phonex
	 * @throws PhonexPatternException if the given
	 *  phonex is not valid
	 */
	@Override
	public void setPhonex(String phonex) throws PhonexPatternException {
		pattern = PhonexPattern.compile(phonex);
		if(pattern != null)
			updateOutputs();
	}
	
	@Override
	public String getPhonex() {
		String retVal = "";
		if(pattern != null)
			retVal = pattern.pattern();
		return retVal;
	}
	
	/**
	 * Updates outputs based on current pattern's 
	 * group settings.
	 * 
	 * 
	 */
	private void updateOutputs() {
		for(int i = 1; i <= pattern.numberOfGroups(); i++) {
			// try to find output field
			int outputFieldIdx = i + 1;
			final String outName = (pattern.groupName(i) != null ? pattern.groupName(i) : "g" + i);
			OutputField outField = null;
			if(outputFieldIdx < getOutputFields().size()) {
				outField = getOutputFields().get(outputFieldIdx);
			} else {
				outField = new OutputField(outName, "output for group " + i, false, IPATranscript.class);
				putField(outField);
			}
			outField.setKey(outName);
		}
		
		// remove extras
		for(int i = pattern.numberOfGroups()+2; i < getOutputFields().size(); i++) {
			removeField(getOutputFields().get(i));
		}
		
//		// add a new output field for each group
//		// in our pattern
//		for(int gIdx = 1; gIdx <= pattern.numberOfGroups(); gIdx++) {
//			String outputName = 
//					(pattern.groupName(gIdx) != null ? pattern.groupName(gIdx) : "g" + gIdx);
//			OutputField groupOut = 
//					new OutputField(outputName, "Content of group " + gIdx + ". Valid only if matches is true", 
//							false, IPATranscript.class);
//			super.putField(groupOut);
//		}
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

}
