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
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import ca.phon.opgraph.app.*;
import ca.phon.opgraph.app.edits.node.*;

/**
 * Phonex node settings panel.
 */
public class PhonexSettingsPanel extends JPanel {
	private GraphDocument document;
	
	// reference to parent node
	private PhonexNode phonexNode;
	
	public PhonexSettingsPanel(final GraphDocument document, PhonexNode node) {
		super(new BorderLayout());
		
		this.document = document;
		phonexNode = node;
		
		init();
	}
	
	private JTextPane phonexEditor;
	public JTextPane getPhonexEditor() {
		if(phonexEditor == null) {
			phonexEditor = new JTextPane();
			phonexEditor.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					// Post an undoable edit
					String phonex = phonexEditor.getText();
					if(document != null) {
						final Properties settings = new Properties();
						settings.put(phonexNode.getClass().getName() + ".phonex", phonex);
						document.getUndoSupport().postEdit(new NodeSettingsEdit(phonexNode, settings));
					} else {
						phonexNode.setPhonex(phonex);
					}
				}
				
				@Override
				public void focusGained(FocusEvent e) {}
			});
		}
		return this.phonexEditor;
	}
	
	private final JScrollPane phonexScroller = new JScrollPane(getPhonexEditor());
	private JScrollPane getPhonexScroller() {
		return this.phonexScroller;
	}
	
	private void init() {
		final JLabel lbl = new JLabel("Enter phonex:");
		add(lbl, BorderLayout.NORTH);
		add(getPhonexScroller(), BorderLayout.CENTER);
		
		getPhonexEditor().setText(phonexNode.getPhonex());
	}
}