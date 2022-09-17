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
package ca.phon.app.opgraph.syllabifier;

import ca.phon.syllabifier.opgraph.extensions.SyllabifierSettings;
import ca.phon.util.Language;
import com.jgoodies.forms.layout.*;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;

/**
 * Panel for modifying syllabifier settings such as language
 * and name.
 */
public class SyllabifierSettingsPanel extends JPanel {

	private static final long serialVersionUID = 8257931751924360390L;
	
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SyllabifierSettingsPanel.class.getName());

	/*
	 * UI
	 */
	private JTextField nameField;
	
	private JTextField languageField;
	
	public SyllabifierSettingsPanel() {
		super();
		init();
		reset();
	}
	
	private void init() {
		final FormLayout layout = new FormLayout("right:pref, 3dlu, fill:pref:grow", 
				"pref, 3dlu, pref");
		setLayout(layout);
		
		final CellConstraints cc = new CellConstraints();
		
		add(new JLabel("Syllabifier name:"), cc.xy(1,1));
		nameField = new JTextField();
		add(nameField, cc.xy(3,1));
		
		add(new JLabel("Syllabifier language:"), cc.xy(1,3));
		languageField = new JTextField();
		add(languageField, cc.xy(3,3));
	}
	
	public String getSyllabifierName() {
		return nameField.getText();
	}
	
	public Language getLanguage() {
		Language retVal = new Language();
		try {
			retVal = Language.parseLanguage(languageField.getText());
		} catch (IllegalArgumentException e) {
		}
		return retVal;
	}
	
	public SyllabifierSettings getSyllabifierSettings() {
		final SyllabifierSettings retVal = new SyllabifierSettings();
		retVal.setName(getSyllabifierName());
		retVal.setLanguage(getLanguage());
		return retVal;
	}

	public void reset() {
		nameField.setText("Untitled");
		languageField.setText("xxx");
	}
	
	public void loadSettings(SyllabifierSettings settings) {
		nameField.setText(settings.getName());
		languageField.setText(settings.getLanguage().toString());
	}
}
