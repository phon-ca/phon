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
package ca.phon.opgraph.nodes.phonex;

import java.awt.Component;
import java.util.Properties;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.ipa.IPATranscript;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;
import ca.phon.phonex.PhonexPatternException;

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
			settingsPanel = new PhonexSettingsPanel(this);
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
