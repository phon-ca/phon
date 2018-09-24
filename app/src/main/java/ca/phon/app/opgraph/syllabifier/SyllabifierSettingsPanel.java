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
package ca.phon.app.opgraph.syllabifier;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.logging.log4j.LogManager;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.syllabifier.opgraph.extensions.SyllabifierSettings;
import ca.phon.util.Language;

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
