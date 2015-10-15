/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.syllabifier.editor;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;

import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.components.ContextViewerPanel;
import ca.phon.syllabifier.opgraph.editor.SyllabifierSettingsPanel;
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
