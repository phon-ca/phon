package ca.phon.syllabifier.editor;

import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ca.phon.syllabifier.opgraph.extensions.SyllabifierSettings;
import ca.phon.util.Language;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Panel for modifying syllabifier settings such as language
 * and name.
 */
public class SyllabifierSettingsPanel extends JPanel {

	private static final long serialVersionUID = 8257931751924360390L;
	
	private static final Logger LOGGER = Logger
			.getLogger(SyllabifierSettingsPanel.class.getName());

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
		final FormLayout layout = new FormLayout("right:pref, fill:pref:grow", 
				"pref, 3dlu, pref");
		setLayout(layout);
		
		final CellConstraints cc = new CellConstraints();
		
		add(new JLabel("Syllabifier name:"), cc.xy(1,1));
		nameField = new JTextField();
		add(nameField, cc.xy(2,1));
		
		add(new JLabel("Syllabifier language:"), cc.xy(1,3));
		languageField = new JTextField();
		add(languageField, cc.xy(2,3));
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
