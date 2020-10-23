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
package ca.phon.app.session.editor.view.timeline;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

public class TimelineTierDivider extends JSeparator {

	private JComponent comp;

	public TimelineTierDivider(JComponent toResize) {
		super(SwingConstants.HORIZONTAL);
		this.comp = toResize;
		
		setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
		
		if(this.comp != null) {
			SeparatorMouseListener l = new SeparatorMouseListener();
			addMouseListener(l);
			addMouseMotionListener(l);
		}
		
	}

	private class SeparatorMouseListener extends MouseInputAdapter {
		
		private boolean valueAdjusting = false;
		
		public SeparatorMouseListener() {
			super();
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			valueAdjusting = true;
			((JComponent)e.getSource()).firePropertyChange("valueAdjusting", false, valueAdjusting);
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			valueAdjusting = false;
			((JComponent)e.getSource()).firePropertyChange("valueAdjusting", true, valueAdjusting);
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			Dimension currentSize = comp.getSize();
			Dimension prefSize = comp.getPreferredSize();

			prefSize.height = currentSize.height + e.getY();
			if(prefSize.height < 0) prefSize.height = 0;
			
			comp.setPreferredSize(prefSize);
			comp.revalidate();
		}
		
	}
	
}
