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
package ca.phon.ui.syllable;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;

import ca.phon.ipa.IPATranscript;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.basic.*;
import ca.phon.ui.ipa.SyllabificationDisplay;
import ca.phon.util.resources.*;

public class BasicSyllabifierTest extends JFrame {

	private final static Logger LOGGER = Logger
			.getLogger(BasicSyllabifierTest.class.getName());
	
	private JComboBox syllabifierBox;
 	
	private JTextField ipaField;
	
	private JPanel stagePanel;
	
	private ResourceLoader<Syllabifier> basicSyllabifierLoader = new ResourceLoader<Syllabifier>();
	
	public BasicSyllabifierTest() {
		super("Test Syllabifier");
		
		basicSyllabifierLoader.addHandler(new BasicSyllabifierClassLoaderProvider());
		
		init();
	}
	
	public BasicSyllabifierTest(String folder) {
		super("Test Syllabifiers : " + folder);
		
		final ResourceHandler<Syllabifier> folderHandler = new BasicSyllabifierFolderHandler(new File(folder));
		basicSyllabifierLoader.addHandler(folderHandler);
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		final JPanel topPanel = new JPanel(new GridLayout(0, 1));
		ipaField = new JTextField();
		ipaField.setBorder(BorderFactory.createTitledBorder("Enter IPA:"));
		final Action act = new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					syllabify();
				} catch (ParseException e1) {
					LOGGER.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
//					final Toast t = ToastFactory.makeToast(e1.getLocalizedMessage());
//					t.start(ipaField);
				}
			}
			
		};
		act.putValue(Action.NAME, "Go");
		final JButton btn = new JButton(act);
		
		final List<Syllabifier> basicSyllabifiers = new ArrayList<Syllabifier>();
		final Iterator<Syllabifier> itr = basicSyllabifierLoader.iterator();
		while(itr.hasNext()) {
			basicSyllabifiers.add(itr.next());
		}
		syllabifierBox = new JComboBox(basicSyllabifiers.toArray(new Syllabifier[0]));
		syllabifierBox.setRenderer(new SyllabifierCellRenderer());
		syllabifierBox.setBorder(BorderFactory.createTitledBorder("Syllabifier:"));
		
		topPanel.add(ipaField);
		topPanel.add(btn);
		topPanel.add(syllabifierBox);
		
		stagePanel = new JPanel(new GridLayout(0, 1));
		final JScrollPane sp = new JScrollPane(stagePanel);
		sp.setBorder(BorderFactory.createTitledBorder("Stages:"));
		
		add(topPanel, BorderLayout.NORTH);
		add(sp, BorderLayout.CENTER);
	}

	private class SyllabifierCellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 1584799534042871645L;

		@Override
		public Component getListCellRendererComponent(JList list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			final JLabel retVal = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			
			final Syllabifier syllabifier = (Syllabifier)value;
			if(syllabifier != null)
				retVal.setText(syllabifier.getName() + " (" + syllabifier.getLanguage().toString() + ")");
			
			return retVal;
		}

	}
	
	private void syllabify() throws ParseException {
		final IPATranscript ipa = IPATranscript.parseIPATranscript(ipaField.getText());
		final BasicSyllabifier syllabifier = (BasicSyllabifier)syllabifierBox.getSelectedItem();
		syllabifier.syllabify(ipa.toList());
		
		stagePanel.removeAll();
		final BasicSyllabifier.SyllabifierStageResults results = syllabifier.getExtension(BasicSyllabifier.SyllabifierStageResults.class);
		for(String stageName:results.stages.keySet()) {
			final SyllabificationDisplay display = new SyllabificationDisplay();
			final IPATranscript stageIPA = IPATranscript.parseIPATranscript(results.stages.get(stageName));
			
			display.setTranscript(stageIPA);
			display.setBorder(BorderFactory.createTitledBorder(stageName));
			
			stagePanel.add(display);
		}
		
		stagePanel.revalidate();
		stagePanel.repaint();
	}
	
	public static void main(String[] args) {
		final BasicSyllabifierTest f = new BasicSyllabifierTest();
		f.setDefaultCloseOperation(EXIT_ON_CLOSE);
		f.pack();
		f.setVisible(true);
	}
}
