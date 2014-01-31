package ca.phon.syllabifier.editor;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.phon.syllabifier.opgraph.extensions.SyllabifierSettings;

public class SyllabifierGraphEditorModel extends GraphEditorModel {

	private final SyllabifierSettingsPanel settingsPanel;
	
	public SyllabifierGraphEditorModel() {
		super();
		settingsPanel = new SyllabifierSettingsPanel();
	}
	
	public SyllabifierSettingsPanel getSettingsPanel() {
		return this.settingsPanel;
	}
	
}
