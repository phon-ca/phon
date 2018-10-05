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
package ca.phon.syllabifier.opgraph.nodes;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import ca.phon.ipa.CompoundPhone;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.Phone;
import ca.phon.opgraph.InputField;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.OpNodeInfo;
import ca.phon.opgraph.OutputField;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;
import ca.phon.phonex.PhonexPatternException;
import ca.phon.syllabifier.phonex.SonorityInfo;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * <p>Marks phones with sonority annotations for use in phonex
 * expressions.  The sonority values are determined by the
 * node settings.  Each line in the node settings {@link String} should
 * be formatted like:
 * <pre>&lt;sonority_value&gt;=&lt;phonex&rt;</pre>
 * E.g.,:
 * <pre>0=[{affricate}{stop,-nasal}{fricative}]</pre>
 * </p>
 * 
 */
@OpNodeInfo(
		name="Mark Sonority",
		description="Add sonority annotation to phones.",
		category="Syllabifier")
public class SonorityNode extends OpNode implements NodeSettings {
	
	private final static String SONORITY_KEY = SonorityNode.class.getName() + ".scale";

	// ipa input
	private final InputField ipaIn = 
			new InputField("ipa", "ipa input", IPATranscript.class);
	private final OutputField ipaOut = 
			new OutputField("ipa", "ipa output", true, IPATranscript.class);
	
	private Settings settingsPanel;
	
	/**
	 * Sonority scale as text
	 */
	private String sonorityScale = "";
	
	/**
	 * Constructor
	 */
	public SonorityNode() {
		super();
		
		putField(ipaIn);
		putField(ipaOut);
		
		putExtension(NodeSettings.class, this);
	}
	
	public String getSonorityScale() {
		return (this.settingsPanel != null ? this.settingsPanel.scaleArea.getText().trim() : this.sonorityScale);
	}
	
	public void setSonorityScale(String scale) {
		this.sonorityScale = scale.trim();
		if(this.settingsPanel != null) {
			this.settingsPanel.scaleArea.setText(sonorityScale);
		}
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		// get ipa from context
		final IPATranscript ipa = 
				IPATranscript.class.cast(context.get(ipaIn));
	
		Map<PhonexPattern, Integer> sonorityMap = new LinkedHashMap<>();
		try {
			sonorityMap = parseSonorityScale(getSonorityScale());
		} catch (PhonexPatternException pe) {
			throw new ProcessingException(null, pe);
		}
		
		// mark sonority
		final SonorityVisitor visitor = new SonorityVisitor(sonorityMap);
		ipa.accept(visitor);
		
		// set output
		context.put(ipaOut, ipa);
	}
	
	/**
	 * Set sonority scale as a string.
	 * 
	 * @param scale
	 */
	public Map<PhonexPattern, Integer> parseSonorityScale(String scale) throws PhonexPatternException {
		final Map<PhonexPattern, Integer> sonorityMap = new LinkedHashMap<>();
		final Pattern scannerPattern = Pattern.compile("([0-9]+)=(.*)");
		final Scanner scanner = new Scanner(scale);
		String line = null;
		while((line = scanner.findInLine(scannerPattern)) != null) {
			final Matcher matcher = scannerPattern.matcher(line);
			if(matcher.matches()) {
				final Integer sonorityValue = Integer.parseInt(matcher.group(1));
				final String phonex = matcher.group(2);
				final PhonexPattern pattern = PhonexPattern.compile(phonex);
				sonorityMap.put(pattern, sonorityValue);
			}
			if(scanner.hasNext())
				scanner.nextLine();
		}
		scanner.close();
		return sonorityMap;
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new Settings();
			settingsPanel.scaleArea.setText(this.sonorityScale);
		}
		return settingsPanel;
	}

	@Override
	public Properties getSettings() {
		final Properties retVal = new Properties();
		retVal.put(SONORITY_KEY, getSonorityScale());
		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		if(properties.containsKey(SONORITY_KEY)) {
			setSonorityScale(properties.getProperty(SONORITY_KEY));
		}
	}
	
	/**
	 * Settings panel
	 */
	private class Settings extends JPanel {
		private static final long serialVersionUID = 1010275554148171791L;

		/**
		 * Text field
		 */
		private final JTextArea scaleArea = new JTextArea();
		
		public Settings() {
			super();
			init();
		}
		
		private void init() {
			setLayout(new BorderLayout());
			
			add(new JLabel("Enter sonority scale:"), BorderLayout.NORTH);
			JScrollPane scaleScroller = new JScrollPane(scaleArea);
			add(scaleScroller, BorderLayout.CENTER);
			
			scaleArea.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent arg0) {
					setSonorityScale(scaleArea.getText());
				}
				
				@Override
				public void focusGained(FocusEvent arg0) {
				}
				
			});
		}
	}
	
	/**
	 * Sonority visitor
	 */
	public class SonorityVisitor extends VisitorAdapter<IPAElement> {
		
		private int lastSonority = 0;

		private Map<PhonexPattern, Integer> sonorityMap;
		
		public SonorityVisitor(Map<PhonexPattern, Integer> sonorityMap) {
			super();
			this.sonorityMap = sonorityMap;
		}
		
		@Override
		public void fallbackVisit(IPAElement obj) {
			// reset sonority
			lastSonority = 0;
		}
		
		@Visits
		public void visitBasicPhone(Phone bp) {
			attachSonority(bp);
		}
		
		@Visits
		public void visitCompoundPhone(CompoundPhone cp) {
			attachSonority(cp);
		}
		
		private void attachSonority(IPAElement p) {
			int value = 0;
			
			for(PhonexPattern pattern:sonorityMap.keySet()) {
				final PhonexMatcher m = pattern.matcher(Collections.singletonList(p));
				if(m.matches()) {
					value = sonorityMap.get(pattern);
					break;
				}
			}
			
			final int distance = value - lastSonority;
			lastSonority = value;
			
			final SonorityInfo info = new SonorityInfo(value, distance);
			p.putExtension(SonorityInfo.class, info);
		}
	}
}
