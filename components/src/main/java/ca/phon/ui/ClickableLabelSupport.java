/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * Add an action to {@link JLabel} components.
 *
 */
public class ClickableLabelSupport {
	
	private boolean doubleClick = false;
	
	private JLabel label;
	
	private Action action;
	
	public ClickableLabelSupport() {
		super();
	}
	
	public ClickableLabelSupport(JLabel label) {
		super();
		
		install(label);
	}
	
	public boolean isDoubleClick() {
		return this.doubleClick;
	}
	
	public void setDoubleClick(boolean doubleClick) {
		this.doubleClick = doubleClick;
	}
	
	public void setAction(Action action) {
		if(label != null && action.getValue(Action.SHORT_DESCRIPTION) != null) {
			label.setToolTipText(action.getValue(Action.SHORT_DESCRIPTION).toString());
		}
	}
	
	public void install(JLabel label) {
		if(this.label != null) {
			this.label.removeMouseListener(mouseListener);
		}
		if(label != null) {
			label.addMouseListener(mouseListener);
			label.setForeground(Color.BLUE);
			label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			
			if(action != null) {
				label.setToolTipText(action.getValue(Action.SHORT_DESCRIPTION).toString());
			}
		}
		
		this.label = label;
	}
	
	private final MouseInputAdapter mouseListener = new MouseInputAdapter() {
		
		@Override
		public void mouseClicked(MouseEvent me) {
			if(action != null) {
				action.actionPerformed(new ActionEvent(label, -1, "onClick"));
			}
		}
		
	};
	
}
