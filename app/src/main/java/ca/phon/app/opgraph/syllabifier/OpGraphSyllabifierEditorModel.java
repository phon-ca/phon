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

import ca.phon.app.opgraph.editor.OpgraphEditorModel;
import ca.phon.ipa.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.syllabifier.opgraph.OpGraphSyllabifier;
import ca.phon.syllabifier.opgraph.extensions.SyllabifierSettings;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.SyllabificationDisplay;
import ca.phon.util.*;
import com.jgoodies.forms.layout.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.File;
import java.util.Map;

/**
 * Editor model for syllabifiers implemented with opgraph.
 *
 */
public class OpGraphSyllabifierEditorModel extends OpgraphEditorModel {

	/**
	 * Settings panel
	 */
	private JPanel syllabifierPanel;

	private SyllabifierSettingsPanel settingsPanel;

	private JTextField ipaField;

	private SyllabificationDisplay syllabificationDisplay;

	public OpGraphSyllabifierEditorModel() {
		this(new OpGraph());
	}

	public OpGraphSyllabifierEditorModel(OpGraph opgraph) {
		super(opgraph);
	}

	private void updateIPA() {
		final String txt = ipaField.getText().trim();
		final IPATranscript transcript =
				(new IPATranscriptBuilder()).append(txt).toIPATranscript();
		syllabificationDisplay.setTranscript(transcript);
	}

	protected JPanel getDebugSettings() {
		if(syllabifierPanel == null) {
			syllabifierPanel = new JPanel();

			ipaField = new JTextField();
			ipaField.setFont(FontPreferences.getTierFont());
			ipaField.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void removeUpdate(DocumentEvent e) {
					updateIPA();
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					updateIPA();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
				}

			});

			syllabificationDisplay = new SyllabificationDisplay();
			getDocument().addPropertyChangeListener(GraphDocument.PROCESSING_CONTEXT, (e) -> {
				SwingUtilities.invokeLater(() -> {
					syllabificationDisplay.revalidate();
					syllabificationDisplay.repaint();
				});
			});

			final FormLayout layout = new FormLayout(
					"right:pref, 3dlu, fill:pref:grow", "pref, 3dlu, pref, pref");
			final CellConstraints cc = new CellConstraints();
			final JPanel ipaPanel = new JPanel(layout);
			ipaPanel.add(new JLabel("Transcript:"), cc.xy(1, 1));
			ipaPanel.add(ipaField, cc.xy(3, 1));
			ipaPanel.add(new JLabel("Syllabification:"), cc.xy(1, 3));
			ipaPanel.add(syllabificationDisplay, cc.xyw(1, 4, 3));
			ipaPanel.setBorder(BorderFactory.createTitledBorder("Debug Settings"));

			syllabifierPanel.setLayout(new BorderLayout());
			syllabifierPanel.add(new JScrollPane(ipaPanel), BorderLayout.CENTER);
		}
		return syllabifierPanel;
	}

	protected SyllabifierSettingsPanel getSyllabifierSettings() {
		if(settingsPanel == null) {
			settingsPanel = new SyllabifierSettingsPanel();
			final SyllabifierSettings settings = getDocument().getGraph().getExtension(SyllabifierSettings.class);
			if(settings != null) {
				settingsPanel.loadSettings(settings);
			}
		}
		return settingsPanel;
	}

	@Override
	public void setupContext(OpContext context) {
		updateIPA();
		context.put(OpGraphSyllabifier.IPA_CONTEXT_KEY, syllabificationDisplay.getTranscript());
	}

	@Override
	protected Map<String, JComponent> getViewMap() {
		final Map<String, JComponent> retVal = super.getViewMap();
		retVal.put("Syllabifier", getSyllabifierSettings());
		retVal.put("Debug Settings", getDebugSettings());
		return retVal;
	}

	@Override
	public boolean isInitiallyMinimized(String viewName) {
		boolean retVal = super.isInitiallyMinimized(viewName);
		if(viewName.equals("Syllabifier") || viewName.equals("Debug Settings")) retVal = true;
		return retVal;
	}

	@Override
	public boolean validate() {
		boolean retVal = super.validate();

		// setup extensions
		final SyllabifierSettings settings = settingsPanel.getSyllabifierSettings();
		getDocument().getGraph().putExtension(SyllabifierSettings.class, settings);

		return retVal;
	}

	@Override
	public Rectangle getInitialViewBounds(String viewName) {
		Rectangle retVal = new Rectangle();
		switch(viewName) {
		case "Canvas":
			retVal.setBounds(200, 0, 600, 400);
			break;

		case "Debug Settings":
			retVal.setBounds(200, 400, 600, 200);
			break;

		case "Syllabifier":
			retVal.setBounds(0, 0, 200, 100);
			break;

		case "Console":
			retVal.setBounds(0, 200, 200, 200);
			break;

		case "Debug":
			retVal.setBounds(200, 400, 600, 200);
			break;

		case "Defaults":
			retVal.setBounds(800, 200, 200, 200);
			break;

		case "Library":
			retVal.setBounds(0, 100, 200, 500);
			break;

		case "Settings":
			retVal.setBounds(800, 0, 200, 200);
			break;

		default:
			retVal.setBounds(0, 0, 200, 200);
			break;
		}
		return retVal;
	}

	@Override
	public String getDefaultFolder() {
		return PrefHelper.getUserDataFolder() + File.separator + "syllabifier";
	}

	@Override
	public String getTitle() {
		return "Composer (Syllabifier)";
	}

	@Override
	public Tuple<String, String> getNoun() {
		return new Tuple<>("syllabifier", "sylalbifiers");
	}

}
