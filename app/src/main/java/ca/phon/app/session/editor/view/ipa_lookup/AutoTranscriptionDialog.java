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
package ca.phon.app.session.editor.view.ipa_lookup;

import java.awt.*;

import javax.swing.*;

import ca.phon.project.*;
import ca.phon.session.*;
import ca.phon.ui.action.*;
import ca.phon.ui.decorations.*;
import ca.phon.util.Language;

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
	
	public AutoTranscriptionDialog(Project project, Session session, Language dictionaryLanguage) {
		super();
		
		form = new AutoTranscriptionForm(project, session);
		form.setDictionaryLanguage(dictionaryLanguage);
		
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
