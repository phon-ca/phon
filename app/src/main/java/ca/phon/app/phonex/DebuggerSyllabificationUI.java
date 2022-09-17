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
package ca.phon.app.phonex;

import ca.phon.ui.ipa.DefaultSyllabificationDisplayUI;
import ca.phon.util.icons.*;

import javax.swing.*;
import java.awt.*;

public class DebuggerSyllabificationUI extends DefaultSyllabificationDisplayUI {

	private ImageIcon arrowIcon;
	
	private DebuggerSyllabificationDisplay display;
	
	public DebuggerSyllabificationUI(DebuggerSyllabificationDisplay display) {
		super(display);
		
		this.display = display;
		arrowIcon = IconManager.getInstance().getIcon("actions/go-up", IconSize.SMALL);
	}
	
	

	@Override
	public void installUI(JComponent c) {
		super.installUI(c);
		
		c.addPropertyChangeListener("debugIndex", (e) -> {
			c.repaint();
		});
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		super.paint(g, c);
		
		int debugIdx = display.getDebugIndex();
		if(debugIdx >= 0 && debugIdx < display.getNumberOfDisplayedPhones()) {
			Rectangle pRect = super.rectForPhone(debugIdx);
			
			g.drawImage(arrowIcon.getImage(), (int)(pRect.getCenterX() - (arrowIcon.getIconWidth()/2)), (int)pRect.getMaxY(), display);
		}
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		Dimension retVal = super.getPreferredSize(c);
		retVal.height += arrowIcon.getIconHeight();
		return retVal;
	}
	
}
