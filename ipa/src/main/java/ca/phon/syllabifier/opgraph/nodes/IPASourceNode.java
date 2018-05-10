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

import java.awt.*;
import java.text.ParseException;
import java.util.Properties;

import javax.swing.*;

import ca.phon.ipa.IPATranscript;
import ca.phon.opgraph.*;
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
