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
package ca.phon.app.opgraph.editor;

import javax.swing.*;

import org.jdesktop.swingx.JXStatusBar;

import com.jgoodies.forms.layout.*;

import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.OSInfo;

public class NodeEditorStatusBar extends JXStatusBar {
	
	private static final long serialVersionUID = 8619512371093438605L;

	private JProgressBar progressBar;
	
	private JLabel progressLabel;
	
	public NodeEditorStatusBar() {
		super();
		
		init();
	}
	
	private void init() {
		JComponent pbar = new JPanel(new FormLayout("pref", 
				(OSInfo.isMacOs() ? "10px" : "pref")));
		progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
		progressBar.setIndeterminate(false);
		progressBar.setValue(0);
		pbar.add(progressBar, (new CellConstraints()).xy(1, 1));
		
		progressLabel = new JLabel();
		progressLabel.setFont(FontPreferences.getSmallFont());
		
		add(progressLabel, new JXStatusBar.Constraint(200));
		add(pbar, new JXStatusBar.Constraint(120));
		add(new JLabel(), new JXStatusBar.Constraint(5));
	}
	
	public JProgressBar getProgressBar() {
		return this.progressBar;
	}
	
	public JLabel getProgressLabel() {
		return this.progressLabel;
	}

}
