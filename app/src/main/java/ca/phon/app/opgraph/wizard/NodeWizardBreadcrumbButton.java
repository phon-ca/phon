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
package ca.phon.app.opgraph.wizard;

import ca.phon.ui.GUIHelper;
import ca.phon.ui.jbreadcrumb.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @deprecated Use {@link BreadcrumbButton}
 */
@Deprecated
public class NodeWizardBreadcrumbButton extends JButton {

	private static final long serialVersionUID = -8963234070761177007L;
	
	public NodeWizardBreadcrumbButton() {
		super();
		
		setOpaque(false);
		setBorder(new BreadcrumbStateBorder());
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		final Insets insets = getInsets();
		final Rectangle2D rect = new Rectangle2D.Double(insets.left, 0, getWidth()-insets.right-insets.left, getHeight());
		
		if(isEnabled())
			g2.setColor(getBackground());
		else
			g2.setColor(UIManager.getColor("Button.background"));
		g2.fill(rect);
		
		if(isEnabled()) {
			g2.setColor(getForeground());
		} else {
			g2.setColor(UIManager.getColor("Button.disabledForeground"));
		}
		Point p = GUIHelper.centerTextInRectangle(g2, getText(),
				new Rectangle((int)rect.getX(), 0, (int)rect.getWidth(), (int)getHeight()));
		g2.drawString(getText(), p.x, p.y);
	}
	
}
