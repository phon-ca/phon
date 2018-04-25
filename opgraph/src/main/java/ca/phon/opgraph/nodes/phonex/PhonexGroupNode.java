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

import java.awt.*;
import java.text.NumberFormat;
import java.util.Properties;

import javax.swing.*;
import javax.swing.event.*;

import ca.phon.ipa.IPATranscript;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.phonex.PhonexMatcher;

/**
 * Source node for data returned from a phonex
 * matcher.  This node is used within the 
 * {@link PhonexFindNode} macro and requires that
 * the current context provide the matcher as a value
 * under key {@link #MATCHER_KEY}.
 */
@OpNodeInfo(
		name="Phonex Match Data",
		description="Data from a phonex match",
		category="Phonex"
)
public class PhonexGroupNode extends OpNode implements NodeSettings {
	// setup outputs for the group
	private final OutputField ipaOut = 
			new OutputField("ipa", "group data", true, IPATranscript.class);
	private final OutputField startOut = 
			new OutputField("start", "group start index", true, Integer.class);
	private final OutputField endOut = 
			new OutputField("end", "group end index", true, Integer.class);
	
	/**
	 * Matcher key
	 */
	public final static String MATCHER_KEY = "__matcher__";
	
	/**
	 * The group index
	 */
	private int group = 0;
	
	/**
	 * Constructor
	 */
	public PhonexGroupNode() {
		this(0);
	}
	
	public PhonexGroupNode(int group) {
		super();
		this.group = group;
		super.putField(ipaOut);
		super.putField(startOut);
		super.putField(endOut);
		
		super.putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		if(context.get(MATCHER_KEY) == null) return;
		
		PhonexMatcher matcher = 
				(PhonexMatcher)context.get(MATCHER_KEY);
		IPATranscript ipa = new IPATranscript(matcher.group(getGroup()));
		context.put(ipaOut, ipa);
		Integer start = matcher.start(getGroup());
		context.put(startOut, start);
		Integer end = matcher.end(getGroup());
		context.put(endOut, end);
	}
	
	/**
	 * Get group index
	 */
	public int getGroup() {
		return this.group;
	}

	/**
	 * Set group index
	 */
	public void setGroup(int group) {
		this.group = group;
	}
	
	private GroupNumberPanel settingsPanel;
	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) { 
			settingsPanel = new GroupNumberPanel();
		}
		return settingsPanel;
	}

	private final String GROUP_KEY = 
			getClass().getName() + ".group";

	@Override
	public Properties getSettings() {
		final Properties retVal = new Properties();
		retVal.setProperty(GROUP_KEY, getGroup()+"");
		return retVal;
	}

	@Override
	public void loadSettings(Properties arg0) {
		if(arg0.containsKey(GROUP_KEY)) {
			setGroup(Integer.parseInt(arg0.getProperty(GROUP_KEY)));
		}
	}
	
	/**
	 * Setting panel
	 */
	private class GroupNumberPanel extends JPanel {
		private JFormattedTextField groupNumberField;
		
		public GroupNumberPanel() {
			super();
			init();
		}
		
		private void init() {
			final NumberFormat intFormat = NumberFormat.getIntegerInstance();
			groupNumberField = new JFormattedTextField(intFormat);
			groupNumberField.setValue(getGroup());
			groupNumberField.getDocument().addDocumentListener(new DocumentListener() {
				
				@Override
				public void removeUpdate(DocumentEvent arg0) {
					updateGroup();
				}
				
				@Override
				public void insertUpdate(DocumentEvent arg0) {
					updateGroup();
				}
				
				private void updateGroup() {
					Integer grp = (Integer)groupNumberField.getValue();
					setGroup(grp);
				}
				
				@Override
				public void changedUpdate(DocumentEvent arg0) { }
			});
		
			final JLabel lbl = new JLabel("Group number:");
			
			final JPanel topPanel = new JPanel(new GridLayout(1, 2));
			topPanel.add(lbl);
			topPanel.add(groupNumberField);
			
			setLayout(new BorderLayout());
			add(topPanel, BorderLayout.NORTH);
		}
	}

}
