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

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.io.File;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.app.GraphDocument;
import ca.phon.app.opgraph.editor.OpgraphEditorModel;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.syllabifier.opgraph.OpGraphSyllabifier;
import ca.phon.syllabifier.opgraph.extensions.SyllabifierSettings;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.SyllabificationDisplay;
import ca.phon.util.PrefHelper;
import ca.phon.util.Tuple;

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
			ipaField.setFont(FontPreferences.getUIIpaFont());
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
	public boolean isViewVisibleByDefault(String viewName) {
		boolean retVal = super.isViewVisibleByDefault(viewName);
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
