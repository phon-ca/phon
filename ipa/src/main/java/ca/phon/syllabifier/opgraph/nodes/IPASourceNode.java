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
import java.text.ParseException;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import ca.phon.ipa.IPATranscript;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.OpNodeInfo;
import ca.phon.opgraph.OutputField;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.syllabifier.opgraph.OpGraphSyllabifier;

/**
 * IPA source node for the syllabifier.
 *
 */
@OpNodeInfo(
		name="IPA Source",
		description="IPA source node for syllabifier and stages.",
		category="Syllabifier")
public class IPASourceNode extends OpNode implements NodeSettings {
	
	// context value
	public final static String IPA_KEY = OpGraphSyllabifier.IPA_CONTEXT_KEY;
	
	// single output
	private final OutputField ipaOut = 
			new OutputField("ipa", "ipa source", true, IPATranscript.class);
	
	public IPASourceNode() {
		super();
		putField(ipaOut);
		
		putExtension(NodeSettings.class, this);
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		// map context value to output
		if(getIpa() != null) {
			IPATranscript ipa;
			try {
				ipa = IPATranscript.parseIPATranscript(getIpa());
				context.put(ipaOut, ipa);
				context.getParent().put(IPA_KEY, ipa);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else if(context.containsKey(IPA_KEY)) {
			context.put(ipaOut, (IPATranscript)context.get(IPA_KEY));
		}
	}
	
	public String getIpa() {
		String retVal = null;
		if(settings != null && settings.ipaArea.getText().length() > 0) {
			retVal = settings.ipaArea.getText();
		}
		return retVal;
	}

	private final Settings settings = new Settings();
	@Override
	public Component getComponent(GraphDocument document) {
		return settings;
	}

	// don't save any settings
	@Override
	public Properties getSettings() {
		return new Properties();
	}

	@Override
	public void loadSettings(Properties properties) {
	}
	
	/**
	 * Settings panel
	 */
	private class Settings extends JPanel {
		
		private static final long serialVersionUID = -2070336588978115879L;
		
		private final JTextArea ipaArea = new JTextArea();
		
		public Settings() {
			super();
			init();
		}
		
		private void init() {
			setLayout(new BorderLayout());
			
			add(new JLabel("Enter IPA:"), BorderLayout.NORTH);
			add(new JScrollPane(ipaArea), BorderLayout.CENTER);
		}
		
	}

}
