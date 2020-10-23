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
package ca.phon.app.session.check;

import java.awt.*;

import javax.swing.*;

import org.jdesktop.swingx.*;

import ca.phon.session.check.*;

public class CheckAlignmentUI extends JPanel implements SessionCheckUI {

	private final CheckAlignment check;
	
	private JCheckBox resetAlignmentBox;
	
	public CheckAlignmentUI(CheckAlignment check) {
		super();
		this.check = check;
	
		init();
	}
	
	private void init() {
		setLayout(new VerticalLayout());
		
		resetAlignmentBox = new JCheckBox("Reset alignments");
		resetAlignmentBox.setSelected(check.isResetAlignment());
		resetAlignmentBox.addActionListener( (e) -> {
			check.setResetAlignment(resetAlignmentBox.isSelected());
		});
		add(resetAlignmentBox);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		resetAlignmentBox.setEnabled(enabled);
	}
	
	@Override
	public Component getComponent() {
		return this;
	}

}
