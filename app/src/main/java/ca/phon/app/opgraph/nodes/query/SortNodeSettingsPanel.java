package ca.phon.app.opgraph.nodes.query;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import ca.phon.app.opgraph.nodes.query.SortNodeSettings.FeatureFamily;
import ca.phon.app.opgraph.nodes.query.SortNodeSettings.SortType;
import ca.phon.ui.text.PromptedTextField;

public class SortNodeSettingsPanel extends JPanel {

	private static final long serialVersionUID = 4289280424233502931L;
	
	private final SortNodeSettings settings;
	
	public SortNodeSettingsPanel(SortNodeSettings settings) {
		super();
		this.settings = settings;
		
		init();
	}
	
	private void init() {
		
	}

	public SortNodeSettings getSettings() {
		return this.settings;
	}
	
	private PromptedTextField createColumnField() {
		final PromptedTextField retVal = new PromptedTextField();
		retVal.setPrompt("Enter column name or number");
		return retVal;
	}
	
	private JComboBox<SortType> createSortTypeBox() {
		final SortType[] boxVals = new SortType[SortType.values().length + 1];
		int idx = 0;
		boxVals[idx++] = null;
		for(SortType v:SortType.values()) boxVals[idx++] = v;
		
		final JComboBox<SortType> retVal = new JComboBox<>(boxVals);
		retVal.setSelectedItem(null);
		return retVal;
	}
	
	private JComboBox<FeatureFamily> createFeatureBox() {
		final FeatureFamily[] boxVals = new FeatureFamily[FeatureFamily.values().length + 1];
		int idx = 0;
		boxVals[idx++] = null;
		for(FeatureFamily v:FeatureFamily.values()) boxVals[idx++] = v;
		
		final JComboBox<FeatureFamily> retVal = new JComboBox<>(boxVals);
		retVal.setSelectedItem(null);
		return retVal;
	}
	
}
