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
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import ca.phon.ipa.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.opgraph.app.extensions.*;
import ca.phon.opgraph.exceptions.*;
import ca.phon.phonex.*;

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
