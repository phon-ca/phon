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
package ca.phon.app.opgraph.editor;

import javax.swing.*;

import org.jdesktop.swingx.*;

import com.jgoodies.forms.layout.*;

import ca.phon.ui.fonts.*;
import ca.phon.util.*;

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
