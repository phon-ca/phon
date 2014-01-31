package ca.phon.syllabifier.editor;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.components.ContextViewerPanel;
import ca.phon.syllabifier.opgraph.extensions.SyllabifierSettings;
import ca.phon.ui.ipa.SyllabificationDisplay;

public class SyllabifierGraphEditorModel extends GraphEditorModel {

	private final SyllabifierSettingsPanel settingsPanel;
	
	private final SyllabificationDisplay syllabificationDisplay;
	

	public SyllabifierGraphEditorModel() {
		super();
		settingsPanel = new SyllabifierSettingsPanel();
		
		syllabificationDisplay = new SyllabificationDisplay();
		syllabificationDisplay.setBackground(Color.white);
		syllabificationDisplay.setBorder(BorderFactory.createTitledBorder("IPA"));
	}
	
	public SyllabifierSettingsPanel getSettingsPanel() {
		return this.settingsPanel;
	}
	
	public SyllabificationDisplay getSyllabificationDisplay() {
		return syllabificationDisplay;
	}

	@Override
	public ContextViewerPanel getDebugInfoPanel() {
		final ContextViewerPanel debugComp = super.getDebugInfoPanel();
		final ContextViewerPanel debugPanel = new ContextViewerPanel();
		debugPanel.setLayout(new BorderLayout());
		final JScrollPane debugScroller = new JScrollPane(debugComp);
		debugScroller.setBorder(BorderFactory.createTitledBorder("Debug info:"));
		debugPanel.add(syllabificationDisplay, BorderLayout.NORTH);
		debugPanel.add(debugScroller, BorderLayout.CENTER);
		return debugPanel;
	}
	
	

}
