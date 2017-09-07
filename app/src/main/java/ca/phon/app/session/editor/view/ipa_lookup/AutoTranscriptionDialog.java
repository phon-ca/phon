/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.session.editor.view.ipa_lookup;

import java.awt.*;

import javax.swing.*;

import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;

public class AutoTranscriptionDialog extends JDialog {
	
	private static final long serialVersionUID = -1911013892296544588L;

	/*
	 * UI Components
	 */
	private DialogHeader dialogHeader;
	
	private AutoTranscriptionForm form;
	
	private JButton okButton;
	
	private JButton cancelButton;
	
	private boolean canceled = true;
	
	public AutoTranscriptionDialog(Project project, Session session) {
		super();
		
		form = new AutoTranscriptionForm(project, session);
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		dialogHeader = new DialogHeader("Automatic Transcription", "Automatically transcribe IPA Target/Actual tiers");
		add(dialogHeader, BorderLayout.NORTH);
		
		add(form, BorderLayout.CENTER);
		
		final PhonUIAction okAct = new PhonUIAction(this, "onOk");
		okAct.putValue(PhonUIAction.NAME, "Ok");
		okButton = new JButton(okAct);
		
		final PhonUIAction cancelAct = new PhonUIAction(this, "onCancel");
		cancelAct.putValue(PhonUIAction.NAME, "Cancel");
		cancelButton = new JButton(cancelAct);
		
		final JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnPanel.add(cancelButton);
		btnPanel.add(okButton);
		
		add(btnPanel, BorderLayout.SOUTH);
	}
	
	public AutoTranscriptionForm getForm() {
		return this.form;
	}
	
	/*
	 * Button callbacks
	 */
	public void onOk() {
		canceled = false;
		setVisible(false);
	}
	
	public void onCancel() {
		canceled = true;
		setVisible(false);
	}
	
	public boolean wasCanceled() {
		return this.canceled;
	}
	
}
