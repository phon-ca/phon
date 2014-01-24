/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.app.project.mergewizard;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import ca.phon.app.session.SessionSelector;
import ca.phon.project.Project;
import ca.phon.session.SessionPath;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.toast.Toast;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.ui.wizard.WizardStep;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Select session name, corpus and sessions for 
 * merge.
 *
 */
public class MergeSessionStep1 extends WizardStep {

	/** Default merge directory */
	private static final String defaultMergeCorpus = "Merged Sessions";
	
	/** Merged session prefix */
	private static final String mergedSessionPrefix = "Merged-";
	
	/* UI */
	private DialogHeader header;
	
	private JTextField sessionNameField;
	
	private JTextField corpusNameField;
	
	private SessionSelector sessionSelector;
	
	/** Project */
	private Project project;
	
	public MergeSessionStep1(Project project) {
		super();
		
		this.project = project;
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		header = new DialogHeader("Merge Sessions", 
				"Specify merged session name, corpus, and sessions for merge.");
		add(header, BorderLayout.NORTH);
		
		FormLayout formLayout = new FormLayout(
				"right:pref, 3dlu, fill:pref:grow",
				"pref, 3dlu, pref");
		CellConstraints cc = new CellConstraints();
		
		JPanel namePanel = new JPanel();
		namePanel.setBorder(BorderFactory.createTitledBorder("Session name and corpus"));
		namePanel.setLayout(formLayout);
		
		sessionNameField = new JTextField();
		sessionNameField.setText(mergedSessionPrefix);
		namePanel.add(new JLabel("Session name"), cc.xy(1,1));
		namePanel.add(sessionNameField, cc.xy(3, 1));
		
		corpusNameField = new JTextField();
		corpusNameField.setText(defaultMergeCorpus);
		namePanel.add(new JLabel("Corpus"), cc.xy(1,3));
		namePanel.add(corpusNameField, cc.xy(3, 3));
		
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(namePanel, BorderLayout.NORTH);
		
		JPanel sessionSelectorPanel = new JPanel(new BorderLayout());
		sessionSelectorPanel.setBorder(BorderFactory.createTitledBorder("Selection sessions"));
		
		sessionSelector = new SessionSelector(project);
		sessionSelectorPanel.add(new JScrollPane(sessionSelector), BorderLayout.CENTER);
		
		centerPanel.add(sessionSelectorPanel, BorderLayout.CENTER);
		add(centerPanel, BorderLayout.CENTER);
	}

	public String getMergedSessionName() {
		return sessionNameField.getText();
	}
	
	public String getMergedCorpusName() {
		return corpusNameField.getText();
	}
	
	public List<SessionPath> getSelectedSessions() {
		return sessionSelector.getSelectedSessions();
	}
	
	@Override
	public boolean validateStep() {
		boolean retVal = true;
		
		if(sessionNameField.getText().length() == 0) {
			final Toast toast = ToastFactory.makeToast("Session name required.");
			toast.start(sessionNameField);
			retVal = false;
		} else if(corpusNameField.getText().length() == 0) {
			final Toast toast = ToastFactory.makeToast("Corpus name required.");
			toast.start(corpusNameField);
			retVal = false;
		} else if(getSelectedSessions().size() == 0) {
			final Toast toast = ToastFactory.makeToast("Please select at least one session.");
			toast.start(sessionSelector);
			retVal = false;
		}
		
		return retVal;
	}
	
}
