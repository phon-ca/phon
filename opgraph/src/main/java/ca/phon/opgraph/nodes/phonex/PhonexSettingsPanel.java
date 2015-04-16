/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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

import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.edits.node.NodeSettingsEdit;

/**
 * Phonex node settings panel.
 */
public class PhonexSettingsPanel extends JPanel {
	// reference to parent node
	private PhonexNode phonexNode;
	
	public PhonexSettingsPanel(PhonexNode node) {
		super(new BorderLayout());
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
					final GraphDocument document = GraphEditorModel.getActiveDocument();
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