/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.project.mergewizard;

import ca.phon.app.project.ParticipantsPanel;
import ca.phon.project.Project;
import ca.phon.session.*;
import ca.phon.ui.decorations.*;
import ca.phon.ui.text.*;
import ca.phon.ui.toast.*;
import ca.phon.ui.wizard.WizardStep;
import com.jgoodies.forms.layout.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Select session name, corpus and sessions for
 * merge.
 *
 */
public class MergeSessionStep1 extends WizardStep {

	/* UI */
	private DialogHeader header;

	private SessionNameField sessionNameField;

	private CorpusNameField corpusNameField;

	private ParticipantsPanel participantsPanel;

	/** Project */
	private Project project;

	public MergeSessionStep1(Project project) {
		super();

		this.project = project;
		setTitle("Select Sessions");

		init();
	}

	private void init() {
		setLayout(new BorderLayout());

		header = new DialogHeader("Derive Session",
				"Specify merged session name, corpus, and sessions for merge.");
		add(header, BorderLayout.NORTH);

		FormLayout formLayout = new FormLayout(
				"right:pref, 3dlu, fill:pref:grow",
				"pref, 3dlu, pref");
		CellConstraints cc = new CellConstraints();

		TitledPanel namePanel = new TitledPanel("New Session name", new JPanel());
		namePanel.getContentContainer().setLayout(formLayout);

		sessionNameField = new SessionNameField(project);
		namePanel.getContentContainer().add(new JLabel("Session name"), cc.xy(1,1));
		namePanel.getContentContainer().add(sessionNameField, cc.xy(3, 1));

		corpusNameField = new CorpusNameField(project);
		namePanel.getContentContainer().add(new JLabel("Corpus"), cc.xy(1,3));
		namePanel.getContentContainer().add(corpusNameField, cc.xy(3, 3));

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(namePanel, BorderLayout.NORTH);

		JPanel sessionSelectorPanel = new JPanel(new BorderLayout());
		participantsPanel = new ParticipantsPanel();
		participantsPanel.setProject(project);
		sessionSelectorPanel.add(participantsPanel, BorderLayout.CENTER);

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
		return participantsPanel.getSessionSelector().getSelectedSessions();
	}

	public List<Participant> getSelectedParticipants() {
		return participantsPanel.getParticipantSelector().getSelectedParticpants();
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
			toast.start(participantsPanel);
			retVal = false;
		} else if(getSelectedParticipants().size() == 0) {
			final Toast toast = ToastFactory.makeToast("Please select at least one participant.");
			toast.start(participantsPanel);
			retVal = false;
		}

		return retVal;
	}

}
